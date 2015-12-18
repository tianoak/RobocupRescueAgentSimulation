package csu.communication;

import java.util.ArrayList;
import java.util.List;

import csu.communication.channel.StaticAssignedChannelManager;
import csu.communication.channel.ChannelManager;
import csu.model.AdvancedWorldModel;
import rescuecore2.standard.entities.StandardEntity;

/**
 * This class used to add Port for <code>radioEventListener</code>,
 * <code>voiceEventListener</code>, <code>civilianVoiceListener</code>. And
 * provide method to get the result Port of each listener.
 * <p>
 * The world communication condition was set in this class, too.
 * 
 * @author nale
 * 
 */
public class CommunicationManagerPortsBuilder {

	/** The world model. */
	private final AdvancedWorldModel world;
	/** The entity controlled by me. */
	private final StandardEntity me;
	private final CommunicationUtil comUtil;
	private ChannelManager channelManager;

	/** 
	 * Stores Ports of radioEventListener. 
	 */
	private final List<Port> radioEventListener;
	/** 
	 * All Port that radio event listened. 
	 */
	private Port[] radioEventListenerResult;

	/** 
	 * Stores Ports of voiceEventListener. 
	 */
	private final List<Port> voiceEventListener;
	/** 
	 * All Port that voice event listened. 
	 */
	private Port[] voiceEventListenerResult;

	private final List<CivilianVoiceListener> civilianVoiceListener;
	private CivilianVoiceListener[] civilianListenerResult;

	// constructor
	public CommunicationManagerPortsBuilder(final StandardEntity me, final AdvancedWorldModel world) {
		this.world = world;
		this.me = me;
		this.comUtil = new CommunicationUtil(world);
		this.channelManager = new StaticAssignedChannelManager(me, world.getConfig());
		this.radioEventListener = new ArrayList<>();
		this.voiceEventListener = new ArrayList<>();
		this.civilianVoiceListener = new ArrayList<>();
		
		this.setWorldCommunicationCondition();
	}

	public void addRadioEventListener(Port l) {
		radioEventListener.add(l);
	}

	public void addVoiceEventListener(Port l) {
		voiceEventListener.add(l);
	}

	public void addCivilianVoiceListener(CivilianVoiceListener l) {
		civilianVoiceListener.add(l);
	}

	/**
	 * Instantiate radioEventListener, voiceEventListener, civilianVoiceListener
	 * and channelManager
	 */
	public void build() {
		int radioSize = radioEventListener.size();
		int voiceSize = voiceEventListener.size();
		int civilianSize = civilianVoiceListener.size();
		radioEventListenerResult = radioEventListener.toArray(new Port[radioSize]);
		voiceEventListenerResult = voiceEventListener.toArray(new Port[voiceSize]);
		civilianListenerResult = civilianVoiceListener.toArray(new CivilianVoiceListener[civilianSize]);
	}

	/**
	 * Set the world communication condition.
	 */
	private void setWorldCommunicationCondition() {
		CommunicationCondition condition = channelManager.getCommunicationCondition();
		switch (condition) {
		case Less:
			this.world.setCommunicationLess(true);
			break;
		case Low:
			this.world.setCommunicationLess(false);
			this.world.setCommunicationLow(true);
			break;
		case Medium:
			this.world.setCommunicationLess(false);
			this.world.setCommunicationMedium(true);
			break;
		case High:
			this.world.setCommunicationLess(false);
			this.world.setCommunicationHigh(true);
			break;
		default:
			break;
		}
	}

	AdvancedWorldModel getWorld() {
		return world;
	}

	StandardEntity getMe() {
		return me;
	}

	/**
	 * Return the result of radio event listened Port.
	 * 
	 * @return results of radio event listened Port
	 */
	Port[] getRadioEventListener() {
		return radioEventListenerResult;
	}

	/**
	 * Return the result of voice event listened Port.
	 * 
	 * @return results of voice event listened Port
	 */
	Port[] getVoiceEventListener() {
		return voiceEventListenerResult;
	}

	/**
	 * Return the result of civilian voice event listened Port.
	 * 
	 * @return results of civilian voice event listened Port
	 */
	CivilianVoiceListener[] getCivilianVoiceListener() {
		return civilianListenerResult;
	}

	/**
	 * Return an instance of CommunicationUtil associate with this
	 * CommunicationManagerPartsBuilder.
	 * 
	 * @return an instance of CommunicationUtil
	 */
	public CommunicationUtil getCommunicationUtil() {
		return comUtil;
	}

	/**
	 * Return an instance of ChannelManager associate with this
	 * CommunicationManagerPartsBuilder.
	 * 
	 * @return an instance of ChannelManager
	 */
	public ChannelManager getChannelManager() {
		return channelManager;
	}
}
