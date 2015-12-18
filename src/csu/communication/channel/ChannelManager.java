package csu.communication.channel;

import csu.communication.CommunicationCondition;

/**
 * 信道分配接口类。当你实现一种新的信道分配模型时，需要实现该接口。
 * 
 * @author CSU --- Wang Shang
 */
public interface ChannelManager {
	
	/**
	 * Get all radio channels a FB Agent can subscribe to.
	 * 
	 * @return all radio channels a FB Agent can subscribe to
	 */
	public abstract int[] getFireChannel();
	
	/**
	 * Get all radio channels a AT Agent can subscribe to.
	 * 
	 * @return all radio channels a AT Agent can subscribe to
	 */
	public abstract int[] getAmbulanceChannel();
	
	/**
	 * Get all radio channels a PF Agent can subscribe to.
	 * 
	 * @return all radio channels a PF Agent can subscribe to
	 */
	public abstract int[] getPoliceChannel();
	
	/** Get the voice channel.*/
	public abstract int getSubscribeVoiceChannel();
	
	/**
	 * Get all radio channels this Agent can subscribe to.
	 * 
	 * @return all radio channels this Agent can subscribe to
	 */
	public abstract int[] getSubscribeChannels();
	
	/**
	 * Determines whether a channel is a voice channel.
	 * 
	 * @param channel
	 *            the id of the channel to determines
	 * @return true when this channel is a voice channel. Otherwise, false.
	 */
	public abstract boolean isVoiceChannel(final int channel);
	
	/**
	 * Determines whether a channel is a radio channel.
	 * 
	 * @param channel
	 *            the id of the channel to determines
	 * @return true when this channel is a radio channel. Otherwise, false.
	 */
	public abstract boolean isRadioChannel(final int channel);
	
	/**
	 * Get the communication condition of current map.
	 * 
	 * @return a enmu represent the communication of this map
	 */
	public abstract CommunicationCondition getCommunicationCondition();
	
	public abstract void update();

	/*
	 * The following method is used to implement RandomChannelManager. And I abandom
	 * RandonChannelManager, so there is no need to write them any more.
	 *                                                     ------ Appreciation - csu
	 * 
	 *  --- Update. 
	 * public abstract void update(); 
	 * 
	 *  --- Get a <b>radioChannel</b> for an Agent to send radio message. 
	 * public abstract int getRadioChannel(); 
	 * 
	 *  --- Get a voiceChannel for an Agent to send voice message. 
	 * public abstract int getVoiceChannel();
	 */
}