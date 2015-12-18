package csu.communication.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntity;
import csu.communication.CommunicationCondition;
import csu.model.ConfigConstants;
import csu.model.ConfigConstants.RadioChannel;
import csu.model.ConfigConstants.VoiceChannel;

/**
 * 
 * <p>
 * Date: Mar 11, 2014  Time: 11:54pm
 * 
 * @author appreciation-csu
 */
public class DynamicAssignedChannelManager implements ChannelManager{
	
	private int[] voiceChannelIds;
	private int[] radioChannelIds;
	
	private int[] fireChannel;
	private int[] ambulanceChannel;
	private int[] policeChannel;
	
	private int subscribeVoiceChannel;
	private int[] subscribeRadioChannel;
	
	private int platoonCanSubscribeSize;
	private int centerCanSubscribeSize;
	
	private StandardEntity me;
	private CommunicationCondition communicationCondition;
	private List<RadioChannel> radioChannels;
	
	public DynamicAssignedChannelManager(final StandardEntity me, final ConfigConstants conf) {
		this.me = me;
		this.platoonCanSubscribeSize = conf.subscribePlatoonSize;
		this.centerCanSubscribeSize = conf.subscribePlatoonSize;

		// get all voice channel, and store them
		final int voiceChannelSize = conf.voiceChannels.size();
		Map<Integer, VoiceChannel> voiceChannelMap = conf.voiceChannels;
		if (voiceChannelSize != 0) {
			voiceChannelIds = toArray(voiceChannelMap.keySet());
		}

		// get all radio channel, and store them
		final int radioChannelSize = conf.radioChannels.size();
		Map<Integer, RadioChannel> radioChannelMap = conf.radioChannels;
		if (radioChannelSize != 0) {
			radioChannelIds = toArray(radioChannelMap.keySet());
		}
		
		this.subscribeVoiceChannel = voiceChannelIds[0];
		this.radioChannels = new ArrayList<>(radioChannelMap.values());
		setCommunicationCondition(radioChannels);
	}
	
	@Override
	public void update() {
		fireChannel = this.decideRadioChannel(radioChannels, 'f');
		ambulanceChannel = this.decideRadioChannel(radioChannels, 'a');
		policeChannel = this.decideRadioChannel(radioChannels, 'p');
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
	private int[] decideRadioChannel(List<RadioChannel> channels, char type) {
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
				double value1 = o1.bandwidth * (1 - o1.inputDropoutRate)
						* (1 - o1.inputFailureRate)
						* (1 - o1.outputDropoutRate)
						* (1 - o1.outputFailureRate);
				double value2 = o2.bandwidth * (1 - o2.inputDropoutRate)
						* (1 - o2.inputFailureRate)
						* (1 - o2.outputDropoutRate)
						* (1 - o2.outputFailureRate);
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
		case 10:
			switch (type) {
			case 'f':
				results[0] = channels.get(9).id;
				results[1] = channels.get(6).id;
				results[2] = channels.get(3).id;
				results[3] = channels.get(0).id;
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
			;
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


	@Override
	public int[] getFireChannel() {
		return fireChannel;
	}

	@Override
	public int[] getAmbulanceChannel() {
		return ambulanceChannel;
	}

	@Override
	public int[] getPoliceChannel() {
		return policeChannel;
	}

	@Override
	public int getSubscribeVoiceChannel() {
		if (me instanceof FireBrigade) {
			subscribeRadioChannel = new int[platoonCanSubscribeSize];
			for (int i = 0; i < subscribeRadioChannel.length; i++) {
				subscribeRadioChannel[i] = fireChannel[i];
			}
		} else if (me instanceof FireStation) {
			subscribeRadioChannel = new int[centerCanSubscribeSize];
			for (int i = 0; i < subscribeRadioChannel.length; i++) {
				subscribeRadioChannel[i] = fireChannel[i];
			}
		} else if (me instanceof AmbulanceTeam) {
			subscribeRadioChannel = new int[platoonCanSubscribeSize];
			for (int i = 0; i < subscribeRadioChannel.length; i++) {
				subscribeRadioChannel[i] = ambulanceChannel[i];
			}
		} else if (me instanceof AmbulanceCentre) {
			subscribeRadioChannel = new int[centerCanSubscribeSize];
			for (int i = 0; i < subscribeRadioChannel.length; i++) {
				subscribeRadioChannel[i] = ambulanceChannel[i];
			}
		} else if (me instanceof PoliceForce) {
			subscribeRadioChannel = new int[platoonCanSubscribeSize];
			for (int i = 0; i < subscribeRadioChannel.length; i++) {
				subscribeRadioChannel[i] = policeChannel[i];
			}
		} else if (me instanceof PoliceOffice) {
			subscribeRadioChannel = new int[centerCanSubscribeSize];
		}
		
		return subscribeVoiceChannel;
	}

	@Override
	public int[] getSubscribeChannels() {
		return subscribeRadioChannel;
	}

	@Override
	public boolean isVoiceChannel(int channel) {
		for (int i = 0; i < voiceChannelIds.length; i++) {
			if (voiceChannelIds[i] == channel)
				return true;
		}
		return false;
	}

	@Override
	public boolean isRadioChannel(int channel) {
		for (int i = 0; i < radioChannelIds.length; i++) {
			if (radioChannelIds[i] == channel)
				return true;
		}
		return false;
	}

	@Override
	public CommunicationCondition getCommunicationCondition() {
		return communicationCondition;
	}
}
