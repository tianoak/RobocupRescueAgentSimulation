package csu.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import csu.common.TimeOutException;
import csu.communication.CommunicationManager;
import csu.communication.CommunicationManagerPortsBuilder;
import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.MessageConstant;
import csu.communication.Port;
import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import csu.util.BitUtil;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * 这个类主要处理和通信相关的操作。
 * 
 * @author appreciation-csu
 *
 * @param <E>
 */
public abstract class CommunicationAgent<E extends StandardEntity> extends Agent<E> {
	protected CommunicationManager comManager = null;
	
	/**
	 * 分配给FB的radio信道列表。数组默认长度为4，当实际分配到的信道个数小于4时，多余的位补零。
	 */
	protected int[] fireChannel;
	/**
	 * 分配给AT的radio信道列表。数组默认长度为4，当实际分配到的信道个数小于4时，多余的位补零。
	 */
	protected int[] ambulanceChannel;
	/**
	 * 分配给PF的radio信道列表。数组默认长度为4，当实际分配到的信道个数小于4时，多余的位补零。
	 */
	protected int[] policeChannel;
	/**
	 * voice信道，通常称零信道。它一定存在，且只有一条。radio可能不存在，具体由config决定。
	 */
	protected int voiceChannel;
	
	/**
	 * 实际分配给FB的radio信道个数。
	 */
	protected int fireChannelRealSize = 0;
	/**
	 * 实际分配给AT的radio信道个数。
	 */
	protected int ambulanceChannelRealSize = 0;
	/**
	 * 实际分配给AT的radio信道个数。
	 */
	protected int policeChannelRealSize = 0;
	
	@Override
	protected void initialize() {
		super.initialize();
		CommunicationManagerPortsBuilder builder = new CommunicationManagerPortsBuilder(me(), world);
		CommunicationUtil comUtil = builder.getCommunicationUtil();
		world.setNoRadio(comUtil.isNoRadio());
		if (comUtil.isNoRadio()) {
			// decideNoComAgents();
			final Port ownInfoPort = createNoComPort(comUtil);
			builder.addVoiceEventListener(ownInfoPort);

			final Port burningBuildingsNoCommuPort = world.getBurningBuildings().createNoComPort(comUtil, comUtil.NO_RADIO_TIME_TO_LIVE);
			builder.addVoiceEventListener(burningBuildingsNoCommuPort);
			
			final Port burningBuildingVeryLimitPort = world.getBurningBuildings().createVeryLimitComPort(comUtil, comUtil.VERY_LIMIT_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(burningBuildingVeryLimitPort);
			
			final Port extinguishedBuildingsPort = world.getExtinguishedBuildings().createAddPortNoRadio(comUtil, comUtil.NO_RADIO_TIME_TO_LIVE * 2);
			builder.addRadioEventListener(extinguishedBuildingsPort);
			builder.addVoiceEventListener(extinguishedBuildingsPort);
			
			
			final Port collapsedBuildingsPort = world.getCollapsedBuildings().createAddPortNoRadio(comUtil, comUtil.NO_RADIO_TIME_TO_LIVE, 10);
			// builder.addRadioEventListener(collapsedBuildingsPort);
			builder.addVoiceEventListener(collapsedBuildingsPort);
			
			final Port buriedHumanInfosPort = world.getBuriedHumans().createBHIPortForNoRadio(comUtil);
			builder.addVoiceEventListener(buriedHumanInfosPort);
			
			final Port buriedHumanAddPort = world.getBuriedHumans().createAddPortForNoRadio(comUtil, comUtil.NO_RADIO_TIME_TO_LIVE);
			builder.addVoiceEventListener(buriedHumanAddPort);
			
			final Port buriedHumanRemovePort = world.getBuriedHumans().createRemovePortForNoRadio(comUtil);
			builder.addVoiceEventListener(buriedHumanRemovePort);
			
			final Port buriedHumanRescuedPort = world.getBuriedHumans().createRescuedPortForNorRadio(comUtil);
			builder.addVoiceEventListener(buriedHumanRescuedPort);
			
			final Port buriedHumanRescuingPort = world.getBuriedHumans().createRescuingPortForNorRadio(comUtil);
			builder.addVoiceEventListener(buriedHumanRescuingPort);
			
			final Port removeClusterPort = world.getRemainCluster().createRemovePortForNoRadio(comUtil);
			builder.addVoiceEventListener(removeClusterPort);
			
			final Port waterPort = world.getWaterPort().createWaterPort(comUtil, comUtil.NO_RADIO_TIME_TO_LIVE * 2);
			builder.addRadioEventListener(waterPort);
			builder.addVoiceEventListener(waterPort);
			
		} else {

			final Port ownInfoPort = createPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(ownInfoPort);
			
			final Port criticalAreaPort = world.getCriticalArea().createPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(criticalAreaPort);
			
			final Port burningBuildingsAddPort = world.getBurningBuildings().createAddPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(burningBuildingsAddPort);
			builder.addVoiceEventListener(burningBuildingsAddPort);
			
			final Port extinguishedBuildingAddPort = world.getExtinguishedBuildings().createAddPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(extinguishedBuildingAddPort);
			builder.addVoiceEventListener(extinguishedBuildingAddPort);

			// NeedRescueHuman
			final Port buriedHumanAddPort = world.getBuriedHumans().createAddPort(comUtil);
			builder.addRadioEventListener(buriedHumanAddPort);
			builder.addVoiceEventListener(buriedHumanAddPort);

			final Port buriedHumanRemovePort = world.getBuriedHumans().createRemovePort(comUtil);
			builder.addRadioEventListener(buriedHumanRemovePort);
			builder.addVoiceEventListener(buriedHumanRemovePort);
			
			final Port buriedHumanRescuedPort = world.getBuriedHumans().createRescuedPort(comUtil);
			builder.addRadioEventListener(buriedHumanRescuedPort);
			builder.addVoiceEventListener(buriedHumanRescuedPort);
			
			final Port buriedHumanRescuingPort = world.getBuriedHumans().createRescuingPortForNorRadio(comUtil);
			builder.addVoiceEventListener(buriedHumanRescuingPort);
			
			final Port removeClusterPort = world.getRemainCluster().createRemovePort(comUtil);
			builder.addRadioEventListener(removeClusterPort);

			final Port waterPort = world.getWaterPort().createWaterPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE);
			builder.addRadioEventListener(waterPort);
			
			final Port collapsedBuildingPort = world.getCollapsedBuildings().createAddPort(comUtil, comUtil.NORMAL_RADIO_TIME_TO_LIVE, 10);
			builder.addVoiceEventListener(collapsedBuildingPort);
		}

		builder.addCivilianVoiceListener(world.createNewCivilianListener());
		builder.addCivilianVoiceListener(world.getBuriedHumans().createCivilianVoiceListener());
		
		comManager = new CommunicationManager(builder);
		voiceChannel = comManager.getVoiceChannel();
		fireChannel = comManager.getFireChannel();
		policeChannel = comManager.getPoliceChannel();
		ambulanceChannel = comManager.getAmbulanceChannel();
		
		fireChannelRealSize = channelRealSize(fireChannel);
		ambulanceChannelRealSize = channelRealSize(ambulanceChannel);
		policeChannelRealSize = channelRealSize(policeChannel);
	}
	
	/**
	 * 统计分配给某个agent的实际radio信道个数。
	 * 
	 * @param channels
	 *            分配到的信道列表
	 * @return 实际的radio信道个数
	 */
	private int channelRealSize(int[] channels) {
		int count = 0;
		for (int i = 0; i < channels.length; i++) {
			if (channels[i] != 0)
				count++;
		}
		return count;
	}
	
	/**
	 * 发送agent当前的位置信息，这个Port在通信状况不佳的时候使用.
	 * 
	 * @param comUtil
	 *            the CommunicationUtil
	 * @return a Port handle Agent's position infors
	 */
	protected Port createNoComPort(final CommunicationUtil comUtil) {
		
		return new Port() {
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";

			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
			}

			@Override
			public void init(ChangeSet changed) {
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}
			
			private MessageBitSection createNewPacket() {
				if (!(me() instanceof Human))
					return null;
				MessageBitSection sec = new MessageBitSection(15);
				sec.setTimeToLive(1);
				comUtil.writeArea(sec, (Area) location(), world.getUniform());
				if (isStucked((Human)me()) || isBlocked()) {
					sec.add(1, 1);			// 1 means the agent is stucked
				} else {
					sec.add(0, 1);			// 0 means the agent is not stucked
				}
				return sec;
			}

			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// TODO in other port, you should not decrement TTL of packet here. but in this 
				// port you must do this to avoid repeating to send same messages
				next.decrementTTL();
				counter++;
				return next;
			}

			@Override
			public void read(final EntityID senderID, final int time, final BitArrayInputStream stream) {
				final StandardEntity sender = world.getEntity(senderID);
				readString = "sender: " + sender + ", sendTime = " + time;
				final EntityID areaID = comUtil.readArea(stream, world.getUniform());
				readString = ", areaId: " + areaID;
				final int stuckMarker = stream.readBit(1);
				if (stuckMarker == 1) {
					world.getStuckedAgents().add(senderID);
					readString = readString + ", isStucked = true";
				} else {
					world.getStuckedAgents().remove(senderID);
					readString = readString + ", isStucked = false";
				}
				 
				if (areaID == null)
					return;
				final Area area = (Area) world.getEntity(areaID);
				if (sender == null || area == null)
					return;
				if (world.getTimestamp().getLastChangedTime(senderID) < time) {
					if (sender instanceof Human && senderID.getValue() != getID().getValue()) {
						Human sendHuman = (Human) sender;
						sendHuman.setPosition(area.getID(), area.getX(), area.getY());
					}
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				/*try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					Integer marker = dataSizePair.get(0).first();
					Integer uniform = dataSizePair.get(1).first();
					int areaId = marker.intValue() == 0 ? 
							world.getUniform().toID(StandardEntityURN.BUILDING, uniform.intValue()).getValue() : 
								world.getUniform().toID(StandardEntityURN.ROAD,  uniform.intValue()).getValue();
					// int stuckMarker = dataSizePair.get(2).first();
					int id = getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + time + " agent: " + me());
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write(" write in createNoComPort, CommunicationAgent\n");
					writer.write("marker(0 for building, 1 for road): " + marker.intValue() + "\n");
					writer.write("uniform = " + uniform.intValue() + ", id = " + areaId + "\n\n");
					if (stuckMarker == 1) {
						writer.write("isStuck = true");
					} else {
						writer.write("isStuck = false");
					}
					writer.close();
					
				} catch (IOException e) {
					e.printStackTrace();				
				}*/
			}
			
			@Override
			public void printRead(int channel) {
				/*try {
					int id = getID().getValue();
					String fileName = "commOutput/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + time + " agent: " + me());
					writer.write(" read in createNoComPort, CommunicationAgent\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
		};
	}

	/**
	 * 发送agent当前的位置信息. 对于FB, 同时还发送它的水量信息。
	 * <p>
	 * 这个Port在通信状况较好的情况下使用。
	 * 
	 * @param comUtil
	 *            the CommunicationUtil
	 * @param timeToLive
	 *            the time this message will live
	 * @return a Port handle Agent's position infors
	 */
	protected Port createPort(final CommunicationUtil comUtil, final int timeToLive) {
		final int tankCapacity = world.getConfig().maxTankCapacity;
		final int waterPower = world.getConfig().maxPower;
		final int FB_TANK_BIT_SIZE = BitUtil.needBitSize(tankCapacity / waterPower);
		
		return new Port() {
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";

			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
			}

			@Override
			public void init(ChangeSet changed) {
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}

			private MessageBitSection createNewPacket() {
				if (!(me() instanceof Human))
					return null;
				MessageBitSection sec;
				if (comUtil.isNoRadio()) {
					sec = new MessageBitSection(5);
				} else {
					sec = new MessageBitSection(1);
				}
				
				sec.setTimeToLive(timeToLive);
				comUtil.writeArea(sec, (Area)location(), world.getUniform());
				if (isStucked((Human)me()) || isBlocked()) {
					sec.add(1, 1);			// 1 means the agent is stucked
				} else {
					sec.add(0, 1);			// 0 means the agent is not stucked
				}
				
				if (me() instanceof FireBrigade) {
					sec.add(((FireBrigade) me()).getWater() / waterPower, FB_TANK_BIT_SIZE);
				}
				return sec;
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// TODO in other port, you should not decrement TTL of packet here. but in this 
				// port you must do this to avoid repeating to send same messages
				next.decrementTTL();
				counter++;
				return next;
			}

			@Override
			public void read(final EntityID senderID, final int time, final BitArrayInputStream stream) {
				final StandardEntity sender = world.getEntity(senderID);
				readString = "sender: " + sender + ", sendTime = " + time;
				if (sender == null) {
					return;
				}
					
				final EntityID areaID = comUtil.readArea(stream, world.getUniform());
				readString = readString + ", areaId: " + areaID;
				final int stuckMarker = stream.readBit(1);
				if (stuckMarker == 1) {
					world.getStuckedAgents().add(senderID);
					readString = readString + ", isStuck = true";
				} else {
					world.getStuckedAgents().remove(senderID);
					readString = readString + ", isStuck = false";
				}
			
				int tank = 0;
				FireBrigade senderFB = null;
				if (sender instanceof FireBrigade) {
					senderFB = (FireBrigade) sender;
					tank = stream.readBit(FB_TANK_BIT_SIZE);
					readString = readString + ", water = " + tank  + " * " + waterPower;
					readString = readString + " = " + tank * waterPower;
				}
				
				Area area = null;
				if (areaID != null) {
					area = (Area) world.getEntity(areaID);
				}
				
				if (world.getTimestamp().getLastChangedTime(senderID) < time) {
					if (sender instanceof FireBrigade) {
						if (senderFB != null && tank <= tankCapacity)
							senderFB.setWater(tank * world.getConfig().maxPower);
					}
					
					if (sender instanceof Human) {
						Human sendHuman = (Human) sender;
						if (area != null)
							sendHuman.setPosition(area.getID(), area.getX(), area.getY());
					}
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				/*try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					Integer marker = dataSizePair.get(0).first();
					Integer uniform = dataSizePair.get(1).first();
					int areaId = marker.intValue() == 0 ? 
							world.getUniform().toID(StandardEntityURN.BUILDING, uniform.intValue()).getValue() : 
								world.getUniform().toID(StandardEntityURN.ROAD,  uniform.intValue()).getValue();
					Integer water = null;
					if (me() instanceof FireBrigade) {
						water = dataSizePair.get(2).first();
					}
					
					int id = getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + time + " agent: " + me());
					writer.write(" write in createPort, CommunicationAgent\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("marker(0 for building, 1 for road): " + marker.intValue() + "\n");
					writer.write("uniform = " + uniform.intValue() + ", id = " + areaId + "\n");
					if (me() instanceof FireBrigade) {
						writer.write("water = " + water + " * " + waterPower + " = " + water * waterPower + "\n");
					}
					writer.write("\n");
					writer.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			
			@Override
			public void printRead(int channel) {
				/*try {
					int id = getID().getValue();
					String fileName = "commOutput/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + time + " agent: " + me());
					writer.write(" read in createPort, CommunicationAgent\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
		};
	}
	
	@Override
	protected void hear(Collection<Command> heard) {
		comManager.read(heard);
	}
	
	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
		if (time == world.getConfig().ignoreUntil) {
			sendSubscribe(time, comManager.getSubscribeChannels());
		}
		if (time >= world.getConfig().ignoreUntil) {
			comManager.update();
		}
	}
	
	@Override
	protected void afterAct() {
		super.afterAct();
		if (world.getConfig().radioChannels.size() != 0) {
			
			byte[] messageForAll = null, messageForAT_PF = null;
			byte[] messageForAT = null, messageForPF = null, messageForFB = null;
			
			if (fireChannel[0] == policeChannel[0] && fireChannel[0] == ambulanceChannel[0]) {
				messageForAll = comManager.createRadioMessage(fireChannel[0], changed);
				sendSpeak(time, fireChannel[0], messageForAll);
				
			} else if (fireChannel[0] != policeChannel[0] && policeChannel[0] == ambulanceChannel[0]) {
				messageForFB = comManager.
						createRadioMessage(fireChannel[0], changed, MessageConstant.MESSAGE_FOR_FB);
				messageForAT_PF = comManager.
						createRadioMessage(ambulanceChannel[0], changed, MessageConstant.MESSAGE_FOR_AT_PF);
				
				sendSpeak(time, fireChannel[0], messageForFB);
				sendSpeak(time, ambulanceChannel[0], messageForAT_PF);
				
			} else if (fireChannel[0] != policeChannel[0] && policeChannel[0] != ambulanceChannel[0]){
				int canSubscribeSize = world.getConfig().subscribePlatoonSize;
				int canUse = Math.min(4, canSubscribeSize);  ///why
				
				comManager.initRadioMessage(changed, MessageConstant.MESSAGE_FOR_FB);
				int fbCanUse = Math.min(canUse, fireChannelRealSize);
				for (int i = 0; i < fbCanUse; i++) {
					if (world.getUniform().toUniform(me().getID()) % fbCanUse == i) {
						messageForFB = comManager.createRadioMessage(fireChannel[i]);
						sendSpeak(time, fireChannel[i], messageForFB);
					}
				}
				
				comManager.initRadioMessage(changed, MessageConstant.MESSAGE_FOR_PF);
				int pfCanUse = Math.min(canUse, policeChannelRealSize);
				for (int i = 0; i < pfCanUse; i++) {
					if (world.getUniform().toUniform(me().getID()) % pfCanUse == i) {
						messageForPF = comManager.createRadioMessage(policeChannel[i]);
						sendSpeak(time, policeChannel[i], messageForPF);
					}
				}
				
				comManager.initRadioMessage(changed, MessageConstant.MESSAGE_FOR_AT);
				int atCanUse = Math.min(canUse, ambulanceChannelRealSize);
				for (int i = 0; i < atCanUse; i++) {
					if (world.getUniform().toUniform(me().getID()) % atCanUse == i) {
						messageForAT = comManager.createRadioMessage(ambulanceChannel[i]);
						sendSpeak(time, ambulanceChannel[i], messageForAT);
					}
				}
			}
		}	
		
		if (me() instanceof Human) {
			sendSpeak(time, voiceChannel, comManager.createVoiceMessage(voiceChannel, changed));
		}
		
		for (Port next : comManager.getRadioEventListers()) {
			next.resetCounter();
			for (; next.hasNext(); ) {
				MessageBitSection sec = next.next();
				if (sec != null)
					sec.decrementTTL();
			}
			next.resetCounter();
		}

		for (Port next : comManager.getVoiceEventListeners()) {
			next.resetCounter();
			for (; next.hasNext(); ) {
				MessageBitSection sec = next.next();
				if (sec != null)
					sec.decrementTTL();
			}
			next.resetCounter();
		}
	}
	
	/*protected Map<EntityID, EntityID> aggregator = null;
	protected Set<EntityID> messenger = null;
	
	private void decideNoComAgents() {
		Set<EntityID> messengersID = new HashSet<EntityID>();
		List<EntityID> aggregatorList = new ArrayList<EntityID>();
		final Collection<StandardEntity> at = world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
		final int atSize = at.size();
		
		if (2 < atSize) {
			for (StandardEntity se : at) {
				final EntityID id = se.getID();
				if (atSize < 5) {
					if (world.getUniform().toUniform(id) == 1) {
						aggregatorList.add(id);
					}
				} else {
					if (world.getUniform().toUniform(se.getID()) < atSize / 3) {
						aggregatorList.add(id);
					}
				}
			}
		} else
			return;
		
		final Collection<StandardEntity> pf = world.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
		final int pfSize = pf.size();
		
		if (2 < pfSize) {
			for (StandardEntity se : pf) {
				final EntityID id = se.getID();
				if (pfSize < 5) {	
					if (world.getUniform().toUniform(id) == 1) {
						aggregatorList.add(id);
					}
				} else {
					int uniform = world.getUniform().toUniform(id);
					if (uniform < pfSize / 4) {
						messengersID.add(id);
					} else if (uniform < Math.max(pfSize / 3, pfSize / 4 + 1)) {
						aggregatorList.add(id);
					}
				}
			}
		}
		aggregator = Collections.unmodifiableMap(createAggregators(aggregatorList));
		messenger = Collections.unmodifiableSet(messengersID);
	}
	
	private Map<EntityID, EntityID> createAggregators(List<EntityID> aggregatorList) {
		final int aggregatorSize = aggregatorList.size();
		final Map<EntityID, EntityID> result = new HashMap<EntityID, EntityID>(aggregatorSize);
		
		Collections.sort(aggregatorList, new Comparator<EntityID>() {
			@Override
			public int compare(EntityID id1, EntityID id2) {
				return id1.getValue() - id2.getValue();
			}
		});
		
		Rectangle[] rects = (new AreaSplit(world, aggregatorSize)).getRects();
		//The aggregator position is the center of each areasplit 
		for (int i = 0; i < aggregatorSize; i++) {
			Area near = world.getNearestRoad((int) rects[i].getCenterX(), (int) rects[i].getCenterY());
			assert (near != null);
			result.put(aggregatorList.get(i), near.getID());
		}
		return result;
	}
	
	
	public boolean isNoRadio() {
		return aggregator != null;
	}
	
	public boolean isAggregator() {
		return isNoRadio() && aggregator.containsKey(getID());
	}
	
	public EntityID getAggregatorPosition(EntityID id) {
		return aggregator.get(id);
	}
	
	public Collection<EntityID> getAggregators() {
		return aggregator.keySet();
	}
	
	public Collection<EntityID> getMessengers() {
		return messenger;
	}
	
	public boolean isMessenger() {
		return isNoRadio() && messenger.contains(getID());
	}*/
	
	public void doNothing() {
		
	}
}
