package csu.communication.channel;

import java.util.Collection;
import java.util.Map;
import csu.communication.CommunicationCondition;
import csu.model.ConfigConstants;
import csu.model.ConfigConstants.RadioChannel;
import csu.model.ConfigConstants.VoiceChannel;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;

/**
 * This allocation model is abandoned.
 */
final public class RandomChannelManager implements ChannelManager {
	
//	private final Random random;
	
	private int[] subscribeChannels;
	
	private int[] voiceChannelNumbers = null;
	
	private int[] radioChannelNumbers = null;
	
	
	protected int bandwidthAll;

//	private int sendChannel;
//
//	private int speakChannel;


	public RandomChannelManager(final StandardEntity me, final ConfigConstants conf) {
//		random = new Random(me.getID().getValue());
		
		final int voiceChannelSize = conf.voiceChannels.size();
		Map<Integer, VoiceChannel> voiceChannels = conf.voiceChannels;
		if (voiceChannelSize != 0) {
			voiceChannelNumbers = toArray(voiceChannels.keySet());
			//System.out.println(Arrays.toString(voiceChannelNumbers));
		}
		final int radioChannelSize = conf.radioChannels.size();
		Map<Integer, RadioChannel> radioChannels = conf.radioChannels;
		if (radioChannelSize != 0) {
			radioChannelNumbers = toArray(radioChannels.keySet());
			//System.out.println(Arrays.toString(radioChannelNumbers));
		}
		
		final int subscribeSize = Math.max(1, (me instanceof Human) ? conf.subscribePlatoonSize : conf.subscribeCenterSize);
		subscribeChannels = new int[subscribeSize];
		
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
	
	@Override
	public void update() {
//		int radioLength = setSendChannel();
//		int voiceLength = setSpeakChennel();
//		setSubscribeChannel(radioLength, voiceLength);
	}
//	private void setSubscribeChannel(final int radioLength, final int voiceLength) {
//		List<Integer> subscribeList = new ArrayList<Integer>();
//		if (voiceChannelNumbers != null) {
//			for (int x : voiceChannelNumbers) {
//				subscribeList.add(x);
//			}
//		}
//		if (radioChannelNumbers != null) {
//			for (int x : radioChannelNumbers) {
//				subscribeList.add(x);
//			}
//		}
//		if (!subscribeList.isEmpty()) {
//			Collections.shuffle(subscribeList, random);
//			for (int i = 0; i < subscribeChannels.length; i++) {
//				subscribeChannels[i] = subscribeList.get(i);
//			}
//		}
//	}
//	private int setSpeakChennel() {
//		final int voiceLength;
//		if (voiceChannelNumbers == null) {
//			speakChannel = -1;
//			voiceLength = 0;
//			
//		}
//		else {
//			speakChannel = voiceChannelNumbers[random.nextInt(voiceChannelNumbers.length)];
//			voiceLength = voiceChannelNumbers.length;
//		}
//		return voiceLength;
//	}
//	private int setSendChannel() {
//		final int radioLength;
//		if (radioChannelNumbers == null) {
//			sendChannel = -1;
//			radioLength = 0;
//		}
//		else {
//			sendChannel = radioChannelNumbers[random.nextInt(radioChannelNumbers.length)];
//			radioLength = radioChannelNumbers.length;
//		}
//		return radioLength;@Override
//	}
	
//	@Override
//	public int getSendChannel() {
//		return sendChannel;
//	}
//
//	@Override
//	public int getSpeakChannel() {
//		return speakChannel;
//	}

	@Override
	public int[] getSubscribeChannels() {
		return subscribeChannels;
	}
	
	@Override
	public boolean isVoiceChannel(final int channel) {
		for (int c : voiceChannelNumbers) {
			if (c == channel) return true;
		}
		return false;
	}
	
	@Override
	public boolean isRadioChannel(final int channel) {
		for (int c : radioChannelNumbers) {
			if (c == channel) return true;
		}
		return false;
	}



	@Override
	public int[] getFireChannel() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int[] getAmbulanceChannel() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int[] getPoliceChannel() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public int getSubscribeVoiceChannel() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public CommunicationCondition getCommunicationCondition() {
		// TODO Auto-generated method stub
		return null;
	}
}
