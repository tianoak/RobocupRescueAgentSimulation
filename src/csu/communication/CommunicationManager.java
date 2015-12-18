package csu.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import csu.communication.MessageConstant.MessageReportedType;
import csu.communication.channel.ChannelManager;
import csu.communication.channel.DynamicAssignedChannelManager;
import csu.communication.channel.RandomChannelManager;
import csu.communication.channel.StaticAssignedChannelManager;
import csu.io.BitArrayInputStream;
import csu.io.BitArrayOutputStream;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.ConfigConstants;
import csu.util.BitUtil;

import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;

/**
 * 在发送消息时，我们把所有需要发送的{@link MessageBitSection}集中起来，并按优先级排序，然后
 * 将它们写到信道中去。当你读消息的时候，系统给你的是一个字节流，，你需要将这个字节流翻译成有用的信
 * 息。
 * <p>
 * 目前，再每个Port中都有一个Read方法来转换与之相对应的MessageBitSection。接下来的问题就是，如
 * 何将这些MessageBitSection从字节流里分离出来，并确定那个MessageBitSection与哪个Port相对应。
 * 因此，对于每个MessageBitSection，我们需要一个标志来表示它属于那个哪个Port，这个标志我们称为
 * 消息头(message head)。发送是，首先会发送消息头，然后在发送与之相关的MessageBitSection。
 * <p>
 * 对于每个agent，它们所需要发送的消息是一样的，区别在于，它们需要接受的消息不一样。因此，每个agent
 * 都会有一个相同的Port列表。于是，我们可以将某个特定Port在这个Port列表中的位置(index)作为消息头。
 * 读消息的时候，我们先读出消息头，根据读出的消息头找到对应的Port，然后调用这个Port的Read方法读出一
 * 个MessageBitSection。接着读第二个消息头，然后读出第二个MessageBitSection。依次类推。
 * 
 * @see ConfigConstants.VoiceChannel
 * @see ConfigConstants.RadioChannel
 * @see ChannelManager
 * @see RandomChannelManager
 * @see StaticAssignedChannelManager
 * @see DynamicAssignedChannelManager
 * @see CommunicationManagerPortsBuilder
 * @see MessageBitSection
 * @see Port
 */
public class CommunicationManager {
	/** The world model. */
    final private AdvancedWorldModel world;
	/** Channel manager which do the channel assign task.*/
	final private ChannelManager chManager;
	
	/** radioEventListener */
	final private Port[] radioEventListener;
	private List<Port> radioEventListeners = null;
	/** The header is the index of its Port in radioEventListener.*/
	final private int radioHeaderBitSize;
	
	/** voiceEventListener */
	final private Port[] voiceEventListener;
	private List<Port> voiceEventListeners = null;
	/** The header is the index of its Port in the voiceEventListener.*/
	final private int voiceHeaderBitSize;
	
	/** civilianVoiceListener */
	final private CivilianVoiceListener[] civilianListener;
	
	/**
	 * 这个变量用于存储agent在当前周期所有需要发送的MessageBitSection。当agent订阅了不止一个
	 * 信道的时候，就需要借助这个变量来发送消息。
	 * <p>
	 * 首先初始化消息，获得所有要发送的MessageBitSection，然后我们从订阅的第一个信道发送消息。
	 * 在第一个订阅的信道中的消息从这个队列中移除。对于剩下的MessageBitSection，从第二个订阅
	 * 的信道发送，依次类推。
	 */
	final PriorityQueue<MessageBitSection> allMessage = new PriorityQueue<MessageBitSection>();
	
	/**
	 * 对于agent订阅多个信道的情况，我们需要一个临时变量来存储在上一个信道没有发出去的MessageBitSection，
	 * 这些MessageBitSection将在下一个可能的信道被发送出去。
	 * <p>
	 * 当一个信道处理完之后，{@code allMessage}将会是一个空的队列，这时候，我们将{@code tempMessage}
	 * 中的MessageBitSection放回到{@code allMessage}中，并将{@code tempMessage}清空，然后处理第
	 * 二个可能的信道，依次类推。
	 */
	final PriorityQueue<MessageBitSection> tempMessage = new PriorityQueue<MessageBitSection>();
	
	// consturctor
	public CommunicationManager(CommunicationManagerPortsBuilder parts) {
		parts.build();
		world = parts.getWorld();                               
		chManager = parts.getChannelManager();                  
		radioEventListener = parts.getRadioEventListener();     
		voiceEventListener = parts.getVoiceEventListener();     
		radioHeaderBitSize = BitUtil.needBitSize(radioEventListener.length);   
		voiceHeaderBitSize = BitUtil.needBitSize(voiceEventListener.length);   
		civilianListener = parts.getCivilianVoiceListener();    
	}

	/**
	 * When you assign channel dynamicly, you should update channel manager in
	 * each cycle before you send a message.
	 */
	public void update() {
		chManager.update();
	}
	
	/** Get the voice communication channel. */
	public int getVoiceChannel() {
		return chManager.getSubscribeVoiceChannel();
	}

	/**
	 * Get all channels a FB Agent can subscribe to.
	 * 
	 * @return all channels a FB Agent can subscribe to
	 */
	public int[] getFireChannel(){
		return chManager.getFireChannel();
	}
	/**
	 * Get all channels a AT Agent can subscribe to.
	 * 
	 * @return all channels a AT Agent can subscribe to
	 */
	public int[] getAmbulanceChannel(){
		return chManager.getAmbulanceChannel();
	}
	/**
	 * Get all channels a PF Agent can subscribe to.
	 * 
	 * @return all channels a PF Agent can subscribe to
	 */
	public int[] getPoliceChannel(){
		return chManager.getPoliceChannel();
	}
	
	/**
	 * Get all channels this Agent can subscribe to.
	 * 
	 * @return all channels this Agent can subscribe to
	 */
	public int[] getSubscribeChannels() {
		return chManager.getSubscribeChannels();
	}

	
	
/* --------------------------- create message when all agent share one channel ----------------------------- */	
	
	
	public byte[] createRadioMessage(final int channel, final ChangeSet changed) {  
		
		final int limit = world.getConfig().radioChannels.get(channel).bandwidth;
		return createMessage(radioEventListener, radioHeaderBitSize, limit, changed, channel);
	}
	
	public byte[] createVoiceMessage(final int channel, final ChangeSet changed) {
		
		final int limit = world.getConfig().voiceChannels.get(channel).size;
		return createMessage(voiceEventListener, voiceHeaderBitSize, limit, changed, channel);
	}
	
	private byte[] createMessage(final Port[] listener, final int headerSize, 
			final int limit, final ChangeSet changed, int channel) {
		
		final PriorityQueue<MessageBitSection> que = new PriorityQueue<MessageBitSection>();
		final BitArrayOutputStream stream = new BitArrayOutputStream();
		
		for (int i = 0; i < listener.length; i++) {
			listener[i].resetCounter();
			for (listener[i].init(changed); listener[i].hasNext();) {
				MessageBitSection sec = listener[i].next();
				sec.setHeaderNumber(i);
				que.add(sec);
			}
		}
		
		final ArrayList<String> ports = new ArrayList<String>();
		
		while (!que.isEmpty()) {
			final MessageBitSection sec = que.poll();
			if ((stream.getBitLength() + sec.getBitLength()) / 8 + 1 > limit) {
				continue;
			}
			
			// TODO print the write message
			if (AgentConstants.PRINT_COMMUNICATION)  {
				listener[sec.getHeaderNumber()].printWrite(sec, channel);
			}
			// TODO
			
			ports.add(listener[sec.getHeaderNumber()].getClass().getName());
			stream.writeBit(sec.getHeaderNumber(), headerSize);
			for (Pair<Integer, Integer> writePair : sec) {
				stream.writeBit(writePair.first(), writePair.second());
			}
		}
		
		return stream.getData();
	}
	
	
	
	
/* ------------------------ create message for each agent has no more than one channel --------------------- */
	
	
	public byte[] createRadioMessage(final int channel, 
			final ChangeSet changed, EnumSet<MessageReportedType> reportedTypes) {  
		
		final int limit = world.getConfig().radioChannels.get(channel).bandwidth;
		return createMessage(radioEventListener, radioHeaderBitSize, channel, limit, changed, reportedTypes);
	}
	
	public byte[] createVoiceMessage(final int channel, 
			final ChangeSet changed, EnumSet<MessageReportedType> reportedTypes) {
		
		final int limit = world.getConfig().voiceChannels.get(channel).size;
		return createMessage(voiceEventListener, voiceHeaderBitSize, channel, limit, changed, reportedTypes);
	}
	
	private byte[] createMessage(final Port[] listener, final int headerSize, int channel,
			final int limit, final ChangeSet changed, EnumSet<MessageReportedType> reportedType) {
		
		final PriorityQueue<MessageBitSection> que = new PriorityQueue<MessageBitSection>();
		final BitArrayOutputStream stream = new BitArrayOutputStream();
		
		for (int i = 0; i < listener.length; i++) {
			Port flag = null;
			for(MessageReportedType next : reportedType) {
				if (listener[i].getMessageReportedType() == next) {
					flag = listener[i];
					break;
				}
			}
			if (flag == null)
				continue;
			listener[i].resetCounter();
			for (listener[i].init(changed); listener[i].hasNext();) {
				MessageBitSection sec = listener[i].next();
				sec.setHeaderNumber(i);
				que.add(sec);
			}
		}
		
		final ArrayList<String> ports = new ArrayList<String>();
		while (!que.isEmpty()) {
			final MessageBitSection sec = que.poll();
			if ((stream.getBitLength() + sec.getBitLength()) / 8 + 1 > limit) {
				continue;
			}	
			
			// TODO print the write message
			if (AgentConstants.PRINT_COMMUNICATION) {
				listener[sec.getHeaderNumber()].printWrite(sec, channel);
			}
			// TODO
			
			ports.add(listener[sec.getHeaderNumber()].getClass().getName());
			stream.writeBit(sec.getHeaderNumber(), headerSize);
			for (Pair<Integer, Integer> writePair : sec) {
				stream.writeBit(writePair.first(), writePair.second());
			}
		}
		
		return stream.getData();
	}
	
	
	
	
/* ---------------------- create message when each agent has many channels -------------------------------- */
	
	
	public void initRadioMessage(final ChangeSet changed, final EnumSet<MessageReportedType> reportedTypes) {
		this.initMessage(radioEventListener, changed, reportedTypes);
	}
	
	public void initVoiceMessage(final ChangeSet changed, final EnumSet<MessageReportedType> reportedTypes) {
		this.initMessage(voiceEventListener, changed, reportedTypes);
	}
	
	private void initMessage(final Port[] listener, final ChangeSet changed, 
			final EnumSet<MessageReportedType> reportedTypes) {
		
		allMessage.clear();
		for (int i = 0; i < listener.length; i++) {
			Port flag = null;
			for(MessageReportedType next : reportedTypes) {
				if (listener[i].getMessageReportedType() == next) {
					flag = listener[i];
					break;
				}
			}
			if (flag == null)
				continue;
			listener[i].resetCounter();
			for (listener[i].init(changed); listener[i].hasNext();) {
				MessageBitSection sec = listener[i].next();
				sec.setHeaderNumber(i);
				allMessage.add(sec);
			}
		}
	}
	
	public byte[] createRadioMessage(final int channel) {
		final int limit = world.getConfig().radioChannels.get(channel).bandwidth;
		return createMessage(radioHeaderBitSize, limit, radioEventListener, channel);
	}
	
	public byte[] createVoiceMessage(final int channel) {
		final int limit = world.getConfig().voiceChannels.get(channel).size;
		return createMessage(voiceHeaderBitSize, limit, voiceEventListener, channel);
	}
	
	private byte[] createMessage(final int headerSize, final int limit, final Port[] listener, int channel) {
		tempMessage.clear();
		final BitArrayOutputStream stream = new BitArrayOutputStream();
		
		final ArrayList<String> ports = new ArrayList<String>();
		while (!allMessage.isEmpty()) {
			final MessageBitSection sec = allMessage.poll();
			if ((stream.getBitLength() + sec.getBitLength()) / 8 + 1 > limit) {
				tempMessage.add(sec);
				continue;
			}
			
			// TODO print the write message
			if (AgentConstants.PRINT_COMMUNICATION) {
				listener[sec.getHeaderNumber()].printWrite(sec, channel);
			}
			// TODO
			
			ports.add(listener[sec.getHeaderNumber()].getClass().getName());
			stream.writeBit(sec.getHeaderNumber(), headerSize);
			for (Pair<Integer, Integer> writePair : sec) {
				stream.writeBit(writePair.first(), writePair.second());
			}
		}
		
		allMessage.addAll(tempMessage);
		
		return stream.getData();
	}
	
	
	
/* ----------------------------------------- read message ----------------------------------------------- */
	
	/**
	 * Read message. Including voice and radio message. This method also handle messages
	 * send by civilian.
	 * 
	 * @param heard  the received messages
	 */
	public void read(final Collection<Command> heard) {
		for (Command command : heard) {
			if (command instanceof AKSpeak) {
				final AKSpeak message = (AKSpeak) command;
				final StandardEntity sender = world.getEntity(message.getAgentID());
				final int channel = message.getChannel();

				if (sender instanceof Civilian) { 
					for (CivilianVoiceListener l : civilianListener) { 
						l.hear(message);                
					}
				} else if (chManager.isVoiceChannel(channel)) { // read voice meaasge
					readMessage(message, voiceEventListener, voiceHeaderBitSize, channel);
				} else { // read radio message
					readMessage(message, radioEventListener, radioHeaderBitSize, channel);
				}
			}
		}
	}
	
	/** Read the message send by an Agent. It can be a voice or radio message.*/
	private void readMessage(final AKSpeak message, final Port[] listener, final int headerSize, int channel) {
		
		final BitArrayInputStream stream = new BitArrayInputStream(message.getContent());
		
		final ArrayList<String> ports = new ArrayList<String>();    
		
		while (stream.hasNext()) {
			final int header = stream.readBit(headerSize);

			ports.add(listener[header].getClass().getName());
			listener[header].read(message.getAgentID(), message.getTime(),
					stream);

			// TODO print the readed message
			if (AgentConstants.PRINT_COMMUNICATION) {
				listener[header].printRead(channel);
			}
			// TODO
		}
	}
	
	public List<Port> getRadioEventListers() {
		if (radioEventListeners == null) {
			radioEventListeners = new ArrayList<>();
			for(Port next : radioEventListener) {
				radioEventListeners.add(next);
			}
		}
		
		return Collections.unmodifiableList(radioEventListeners);
	}
	
	public List<Port> getVoiceEventListeners() {
		if (voiceEventListeners == null) {
			voiceEventListeners = new ArrayList<>();
			for (Port next : voiceEventListener) {
				voiceEventListeners.add(next);
			}
		}
		return Collections.unmodifiableList(voiceEventListeners);
	}
	
	/*
	 * Because I have changed the channel selection model, there is no need of those method.
	 *                                                 ------------------ appreciation-csu
	 * 
	 * public int getRadioChannel() { 
	 *     return chManager.getRadioChannel(); 
	 * }
	 */
	
	/*
	 * I give a type to each message, and then I can use this type to determines which Agent(FB,PF,AT) 
	 * this message to send. But is was a fail try.
	 *                                                                      ----------- appreciation-csu
	 * 
	 * ------ Create a radio message for the target radio channel using a specified kind of packet.
	 * public byte[] createRadioMessage(final int channel, final ChangeSet changed, final MessageType type) {
	 *     final int limit = world.getConfigConstants().radioChannels.get(channel).bandwidth;
	 *     return createMessage(radioEventListener, radioHeaderBitSize, limit, changed, type);
	 * }              
	 * ------ Create a voice message for the target voice channel using a specified kind of packet.
	 * public byte[] createVoiceMessage(final int channel, final ChangeSet changed, final MessageType type) {
	 *     final int limit = world.getConfigConstants().voiceChannels.get(channel).size;
	 *     return createMessage(voiceEventListener, voiceHeaderBitSize, limit, changed, type);
	 * }
	 * ----- This method used to create message(radio and voice) using a specified kind of packet.
	 * static private byte[] createMessage(final Port[] listener, final int headerSize, 
	 *                             final int limit, final ChangeSet changed, final State.MessageType type) {
	 *     final PriorityQueue<MessageBitSection> que = new PriorityQueue<MessageBitSection>();
	 *     final BitArrayOutputStream stream = new BitArrayOutputStream();
	 *     
	 *     for (int i = 0; i < listener.length; i++ ) {
	 *         if (listener[i].getMessageType() != type)
	 *             continue;
	 *         for (listener[i].init(changed); listener[i].hasNext();) {
	 *             MessageBitSection sec = listener[i].next();
	 *             sec.setHeaderNumber(i);
	 *             que.add(sec);
	 *         }
	 *     }
	 *     while (!que.isEmpty()) {
	 *         final MessageBitSection sec = que.poll();
	 *         if ((stream.getBitLength() + sec.getBitLength()) / 8 + 1 > limit)
	 *             continue;
	 *         stream.writeBit(sec.getHeaderNumber(), headerSize);
	 *         for (Pair<Integer, Integer> writePair : sec) {
	 *             stream.writeBit(writePair.first(), writePair.second());
	 *         }
	 *     }
	 *     return stream.getData();
	 * }
	 * 
	 * 
	 * ----- Create a radio message for the target radio channel using a set of packets with differnent types.
	 * public byte[] createRadioMessage(final int channel, 
	 * 					final ChangeSet changed, final EnumSet<MessageType> types) {
	 *     final int limit = world.getConfigConstants().radioChannels.get(channel).bandwidth;
	 *     return createMessage(radioEventListener, radioHeaderBitSize, limit, changed, types);
	 * }
	 * 
	 * ----- Create a voice message for the target voice channel using a set of packets with different types.
	 * public byte[] createVoiceMessage(final int channel, 
	 * 					final ChangeSet changed, final EnumSet<MessageType> types) {
	 *     final int limit = world.getConfigConstants().voiceChannels.get(channel).size;
	 *     return createMessage(voiceEventListener, voiceHeaderBitSize, limit, changed, types);
	 * }
	 * 
	 * ----- This method used to create message(radio and voice) using a set of packets with differnet types.
	 * static private byte[] createMessage(final Port[] listener, final int headerSize, final int limit, 
	 * 											final ChangeSet changed, final EnumSet<MessageType> types){
	 *     final PriorityQueue<MessageBitSection> que = new PriorityQueue<MessageBitSection>();
	 *     final BitArrayOutputStream stream = new BitArrayOutputStream();
	 *     for (int i = 0; i < listener.length; i++) {
	 *         Port port = null;
	 *         for (MessageType type : types) {
	 *             if (listener[i].getMessageType() != type)
	 *                 continue;
	 *             port = listener[i];
	 *         }
	 *         if (port == null)
	 *             continue;
	 *             
	 *         for (listener[i].init(changed); listener[i].hasNext();) {
	 *             MessageBitSection sec = listener[i].next();
	 *             sec.setHeaderNumber(i);
	 *             que.add(sec);
	 *         }
	 *     }
	 *     
	 *     while (!que.isEmpty()) {
	 *         final MessageBitSection sec = que.poll();
	 *         if ((stream.getBitLength() + sec.getBitLength()) / 8 + 1 > limit)
	 *             continue;
	 *         stream.writeBit(sec.getHeaderNumber(), headerSize);
	 *         for (Pair<Integer, Integer> writePair : sec) {
	 *             stream.writeBit(writePair.first(), writePair.second());
	 *         }
	 *     }
	 *     return stream.getData();
	 * }
	 */
	
	public void doNothing() {
		
	}
}
