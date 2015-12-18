package csu.communication.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import csu.communication.CommunicationCondition;
import csu.model.ConfigConstants;
import csu.model.ConfigConstants.RadioChannel;
import csu.model.ConfigConstants.VoiceChannel;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntity;

/**
 * 静态信道分配模型。由于只有一条零信道(voice channel)，并且每个智能体都能订阅零信道，所以零信道
 * 不需要分配。这里，我仅仅分配radio channel。
 * <p>
 * 一个agent可以订阅一定数量的radio channel，具体能订阅多少个radio channel是由config决定的。
 * 只有当agent订阅了某个radio channel。才能从这个radio channel中接受到消息。没有订阅的信道的
 * 消息是接受不到的。所以这里分配的就是三种agent需要订阅的信道。
 * <p>
 * 而对于发送方，一个agent是可以向任意radio channel发送消息的。例如：一个pf要向at发送消息，这个
 * pf只需要把消息发送到at订阅的那些信道中的某一个就行了。这样at就会接受到那个消息，如果发错了信道，
 * at就不会收到那个消息。而是被其他agent接受到了。
 * 
 * @author appreciation-csu
 * 
 */
public class StaticAssignedChannelManager implements ChannelManager {
	/** The IDs of all voice channel. */
	private int[] voiceChannelIDs = null;
	
	/** The IDs of all radio channel. */
	private int[] radioChannelIDs = null;

	private int voiceChannel;
	
	/** The ID of radio channel that FB will subscribe to. */
	private int[] fireChannel;
	
	/** The ID of radio channel that AT will subscribe to. */
	private int[] ambulanceChannel;
	
	/** The ID of radio channel that PF will subscribe to. */
	private int[] policeChannel;
	
	/**
	 * All radio channels this Agent will subscribe to.
	 */
	private int[] subscribeChannels;
	
	/** The entity controled by an Agent. */
	private StandardEntity me;

	private CommunicationCondition communicationCondition;

	// constructor
	public StaticAssignedChannelManager(final StandardEntity me, final ConfigConstants conf) {
		this.me = me;

		// handle voice channel
		final int voiceChannelSize = conf.voiceChannels.size();
		Map<Integer, VoiceChannel> voiceChannels = conf.voiceChannels;
		if (voiceChannelSize != 0) {
			voiceChannelIDs = toArray(voiceChannels.keySet());
		}

		// handle radio channel
		final int radioChannelSize = conf.radioChannels.size();
		Map<Integer, RadioChannel> radioChannels = conf.radioChannels;
		if (radioChannelSize != 0) {
			radioChannelIDs = toArray(radioChannels.keySet());
		}

		voiceChannel = voiceChannels.get(0).id;
		
		ArrayList<RadioChannel> channels = new ArrayList<RadioChannel>(radioChannels.values());
		this.setCommunicationCondition(channels);
		
		fireChannel = this.decideRadioChannel(channels, 'f');
		ambulanceChannel = this.decideRadioChannel(channels, 'a');
		policeChannel = this.decideRadioChannel(channels, 'p');
		
		final int subscribeSize = Math.max(1,
				(me instanceof Human) ? conf.subscribePlatoonSize : conf.subscribeCenterSize);
		subscribeChannels = new int[subscribeSize];
	}
	
	public void update() {
		// in static allocation model, there is no need to update
	}

	private static int[] toArray(Collection<Integer> c) {
		int[] result = new int[c.size()];
		int i = 0;
		for (Integer n : c) {
			result[i] = n;
			i++;
		}
		return result;
	}

	/**
	 * Get an channel for a specified type of Agent.
	 * 
	 * @param channels
	 *            :a list of all radio channel
	 * @param type
	 *            :the type of RCR Agent
	 * @return the ID of channel that a specified type(PF,FB,AT) Agnet will
	 *         subscribe to
	 */
	private int[] decideRadioChannel(ArrayList<RadioChannel> channels, char type) {
		int size = channels.size();
		int[] results = new int[4];

		/*
		 * Comparator<RadioChannel> comp = new Comparator<RadioChannel>() {
		 *     public int compare(RadioChannel o1, RadioChannel o2) {
		 *	       if (o1.bandwidth < o2.bandwidth)
		 *             return -1;
		 *		   if (o1.bandwidth > o2.bandwidth)
		 *			   return 1;
		 *		   return 0;
		 *	   }
		 * };
		 */
		

		Comparator<RadioChannel> comp = new Comparator<RadioChannel>() {
			public int compare(RadioChannel o1, RadioChannel o2) {
				double value1 = o1.bandwidth * (1 - o1.inputDropoutRate) * (1 - o1.inputFailureRate) 
						* (1 - o1.outputDropoutRate) * (1 - o1.outputFailureRate);
				double value2 = o2.bandwidth * (1 - o2.inputDropoutRate) * (1 - o2.inputFailureRate)
						* (1 - o2.outputDropoutRate) * (1 - o2.outputFailureRate);
				
				if (value1 < value2)
					return -1;
				if (value1 > value2)
					return 1;
				return 0;
			}
		};
		Collections.sort(channels, comp);
		switch (size) {
		case 0:
			results[0] = -1;
			break;
		case 1:
			results[0] = channels.get(0).id;
			break;
		case 2:
			switch (type) {
			case 'f':
				results[0] = channels.get(1).id;
				break;
			case 'p':
				results[0] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(0).id;
				break;
			}
			;
			break;
		case 3:
			switch (type) {
			case 'f':
				results[0] = channels.get(2).id;
				break;
			case 'p':
				results[0] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(1).id;
				break;
			}
			;
			break;
		case 4:
			switch (type) {
			case 'f':
				results[0] = channels.get(3).id;
				break;
			case 'p':
				results[0] = channels.get(1).id;
				break;
			case 'a':
				results[0] = channels.get(2).id;
				break;
			}
			results[1] = channels.get(0).id;
			break;
		case 5:
			switch (type) {
			case 'f':
				results[0] = channels.get(4).id;
				results[1] = channels.get(1).id;
				break;
			case 'p':
				results[0] = channels.get(2).id;
				results[1] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(3).id;
				results[1] = channels.get(0).id;
				break;
			}
			;
			break;
		case 6:
			switch (type) {
			case 'f':
				results[0] = channels.get(5).id;
				results[1] = channels.get(2).id;
				break;
			case 'p':
				results[0] = channels.get(3).id;
				results[1] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(4).id;
				results[1] = channels.get(1).id;
				break;
			}
			;
			break;
		case 7:
			switch (type) {
			case 'f':
				results[0] = channels.get(6).id;
				results[1] = channels.get(3).id;
				break;
			case 'p':
				results[0] = channels.get(4).id;
				results[1] = channels.get(1).id;
				break;
			case 'a':
				results[0] = channels.get(5).id;
				results[1] = channels.get(2).id;
				break;
			}
			results[2] = channels.get(0).id;
			break;
		case 8:
			switch (type) {
			case 'f':
				results[0] = channels.get(7).id;
				results[1] = channels.get(4).id;
				results[2] = channels.get(1).id;
				break;
			case 'p':
				results[0] = channels.get(5).id;
				results[1] = channels.get(2).id;
				results[2] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(6).id;
				results[1] = channels.get(3).id;
				results[2] = channels.get(0).id;
				break;
			}
			;
			break;
		case 9:
			switch (type) {
			case 'f':
				results[0] = channels.get(8).id;
				results[1] = channels.get(5).id;
				results[2] = channels.get(2).id;
				break;
			case 'p':
				results[0] = channels.get(6).id;
				results[1] = channels.get(3).id;
				results[2] = channels.get(0).id;
				break;
			case 'a':
				results[0] = channels.get(7).id;
				results[1] = channels.get(4).id;
				results[2] = channels.get(1).id;
				break;
			}
			;
			break;
		case 10:
			switch (type) {
			case 'f':
				results[0] = channels.get(9).id;
				results[1] = channels.get(6).id;
				results[2] = channels.get(3).id;
				break;
			case 'p':
				results[0] = channels.get(7).id;
				results[1] = channels.get(4).id;
				results[2] = channels.get(1).id;
				break;
			case 'a':
				results[0] = channels.get(8).id;
				results[1] = channels.get(5).id;
				results[2] = channels.get(2).id;
				break;
			}
			results[3] = channels.get(0).id;
			break;
		default:
			switch (type) {
			case 'f':
				results[0] = channels.get(size - 1).id;
				break;
			case 'p':
				results[0] = channels.get(size - 3).id;
				break;
			case 'a':
				results[0] = channels.get(size - 2).id;
			}
			;
			break;
		}
		return results;
	}

	/**
	 * If there is no radio channel, communication condition is <i>Less</i>.
	 * <p>
	 * If the maximum bandwidth within all radio channel is less than 128, the
	 * communication condition is <i>Low</i>.
	 * <p>
	 * If the maximum bandwidth within all radio channel is between 128 and
	 * 1024, the communication condition is <i>Medium</i>.
	 * <p>
	 * If the maximum bandwidth within all radio channel is greater than 1024,
	 * the communication condition is <i>High</i>.
	 */
	private void setCommunicationCondition(List<RadioChannel> radioChannels) {
		if (radioChannels.size() == 0) {
			this.communicationCondition = CommunicationCondition.Less;
		} else {
			Comparator<RadioChannel> bandwidthComparator = new Comparator<RadioChannel>() {

				@Override
				public int compare(RadioChannel arg0, RadioChannel arg1) {
					if (arg0.bandwidth < arg1.bandwidth)
						return -1;
					if (arg0.bandwidth > arg1.bandwidth)
						return 1;
					return 0;
				}
			};
			
			Collections.sort(radioChannels, bandwidthComparator);
			int bandWidth = radioChannels.get(radioChannels.size() - 1).bandwidth;
			if (bandWidth <= 128)
				this.communicationCondition = CommunicationCondition.Low;
			else if (bandWidth < 1024)
				this.communicationCondition = CommunicationCondition.Medium;
			else
				this.communicationCondition = CommunicationCondition.High;
		}
	}

	/**
	 * Get all channels that an Agent will subscribe to. The first one is a
	 * radio channel and the second one is a voice channel.
	 */
	@Override
	public int[] getSubscribeChannels() {
		int[] radioChannel = new int[4];
		if (me instanceof FireBrigade || me instanceof FireStation)
			radioChannel = fireChannel;
		if (me instanceof PoliceForce || me instanceof PoliceOffice)
			radioChannel = policeChannel;
		if (me instanceof AmbulanceTeam || me instanceof AmbulanceCentre)
			radioChannel = ambulanceChannel;
		
		for (int i = 0; i < subscribeChannels.length; i++) {
			if (i <  4)
				subscribeChannels[i] = radioChannel[i];
		}

		return subscribeChannels;
	}

	public CommunicationCondition getCommunicationCondition() {
		return this.communicationCondition;
	}

	/** Get the radio channel that FB will subscribe to. */
	@Override
	public int[] getFireChannel() {
		return fireChannel;
	}

	/** Get the radio channel that AT will subscribe to. */
	@Override
	public int[] getAmbulanceChannel() {
		return ambulanceChannel;
	}

	/** Get the radio channel that PF will subscribe to. */
	@Override
	public int[] getPoliceChannel() {
		return policeChannel;
	}

	/** Get the voice channel. */
	@Override
	public int getSubscribeVoiceChannel() {
		return voiceChannel;
	}

	/** Determines whether a specified channel is a voiceChannel. */
	@Override
	public boolean isVoiceChannel(int channel) {
		for (int c : voiceChannelIDs)
			if (c == channel)
				return true;
		return false;
	}

	/** Determines whether a specified channel is a radioChannel. */
	@Override
	public boolean isRadioChannel(int channel) {
		for (int c : radioChannelIDs)
			if (c == channel)
				return true;
		return false;
	}
}
