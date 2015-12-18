package csu.communication;

import csu.io.BitArrayInputStream;
import csu.model.AdvancedWorldModel;
import csu.model.Uniform;
import csu.model.ConfigConstants.RadioChannel;
import csu.model.ConfigConstants.VoiceChannel;
import csu.util.BitUtil;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.worldmodel.EntityID;

/**
 * Communication util class. This class contains many communication parameter constants
 * which was gained from world model.
 * <p>
 * When you write an object into a message, you need to encoding those objects. Due to each
 * message has a limited bit size, you need to avoid wasting when you want to write more info
 * within a message.
 * <p>
 * And this class calculate the minimum needed bit size you can used to encoding a certain
 * class of object in this simulation system.
 * 
 * @author nale
 *
 */
public class CommunicationUtil {

	public final int UINT_BIT_SIZE = 31;
	public final int FIRERYNESS_BIT_SIZE = Fieryness.values().length;
	public final int CIVILIAN_ID_BIT_SIZE = 31;
	public final int BURIEDNESS_BIT_SIZE = 31;
	/* According to ruler, there are no more than 50 of each Agent, so 6-bit is enough.*/
	public final int AGENT_UNIFORM_BIT_SIZE = 6;
//	public final int AT_UNIFORM_BIT_SIZE;
//	public final int FB_UNIFORM_BIT_SIZE;
//	public final int PF_UNIFORM_BIT_SIZE;
	
	public final int HUMAN_UNIFORM_BIT_SIZE;
	
	public final int ROAD_UNIFORM_BIT_SIZE;
	public final int BUILDING_UNIFORM_BIT_SIZE;
	public final int AREA_UNIFORM_BIT_SIZE;
	
	/**
	 * To avoid wasting, when encoding etity IDs, we assume the minimun ID is 0. So there is a offset
	 * between the encoded value and actual value. And this offset's value is the value of minimum ID.
	 */
	public final int ENTITY_ID_OFFSET;
	public final int ENTITY_ID_BIT_SIZE;
	/** The offset of X coordnate. */
	public final int X_OFFSET;
	public final int X_BIT_SIZE;
	/** The offset of Y coordinate. */
	public final int Y_OFFSET;
	public final int Y_BIT_SIZE;
	
	public final int TIME_BIT_SIZE;
	
	public final int HP_BIT_SIZE;
	
	public final int WATER_POWER_BIT_SIZE;
	
	public final int NO_RADIO_TIME_TO_LIVE = 5;
	public final int NORMAL_RADIO_TIME_TO_LIVE = 1;
	public final int VERY_LIMIT_RADIO_TIME_TO_LIVE = 3;
	
	private final boolean noRadio;
	private final boolean noVoice;

	public CommunicationUtil(final AdvancedWorldModel world) {
		
		this.HUMAN_UNIFORM_BIT_SIZE = BitUtil.needBitSize(1000);
		
		final int BUILDING_SIZE = world.getEntitiesOfType(StandardEntityURN.BUILDING).size();
		this.BUILDING_UNIFORM_BIT_SIZE = BitUtil.needBitSize(BUILDING_SIZE);
		
		final int ROAD_SIZE = world.getEntitiesOfType(StandardEntityURN.ROAD).size();
		this.ROAD_UNIFORM_BIT_SIZE = BitUtil.needBitSize(ROAD_SIZE);
		
		this.AREA_UNIFORM_BIT_SIZE = Math.max(ROAD_UNIFORM_BIT_SIZE, BUILDING_UNIFORM_BIT_SIZE);
		
		int maxID = 0;
		int minID = Integer.MAX_VALUE;
		// get the maximum and minimum numeric value of all entity ID 
		for (StandardEntity se : world.getAllEntities()) {
			final int v = se.getID().getValue();
			maxID = Math.max(maxID, v);
			minID = Math.min(minID, v);
		}
		// the offset of entity ID
		this.ENTITY_ID_OFFSET = minID;
		// the maximum bit size needed when encoding entity IDs
		this.ENTITY_ID_BIT_SIZE = BitUtil.needBitSize(maxID - minID);
		
		/** The X coordinate of the top left corners.*/
		final int MIN_X = world.getConfig().MIN_X;
		/** The Y coordinate of the top left corners.*/
		final int MIN_Y = world.getConfig().MIN_Y;
		/** The X coordinate of the bottom right corners.*/
		final int MAX_X = world.getConfig().MAX_X;
		/** The Y coordinate of the bottom right corners.*/
		final int MAX_Y = world.getConfig().MAX_Y;
		
		// the offset of X coordinate
		this.X_OFFSET = MIN_X;
		// the maximum bit size needed when encodig X coordinate
		this.X_BIT_SIZE = BitUtil.needBitSize(MAX_X - MIN_X);
		// the offset of Y coordinate
		this.Y_OFFSET = MIN_Y;
		// the maximum bit size needed when encodig X coordinate
		this.Y_BIT_SIZE = BitUtil.needBitSize(MAX_Y - MIN_Y);
		
		this.TIME_BIT_SIZE = 9;
		
		this.HP_BIT_SIZE = BitUtil.needBitSize(world.getConfig().hpMax / world.getConfig().hpPrecision);
		
		this.WATER_POWER_BIT_SIZE = BitUtil.needBitSize(world.getConfig().maxPower);
		
		//the total bandwidth of all radio message
		int totalRadioBandwidth = 0;
		for (RadioChannel c : world.getConfig().radioChannels.values()) {
			totalRadioBandwidth += c.bandwidth;
		}
		noRadio = (totalRadioBandwidth <= 200) || (world.getConfig().subscribePlatoonSize <= 0);
		
		int totalVoiceSize = 0;	// the total size of all voice message in bytes
		int maxRange = 0;		// the maximum propagation range of a voice message in mm(millimeter)
		int maxNum = 0;			// The maximum number of a voice message an agent can send in one timestep
		for (VoiceChannel c : world.getConfig().voiceChannels.values()) {
			totalVoiceSize += c.size;
			maxRange = Math.max(maxRange, c.range);
			maxNum = Math.max(maxNum, c.maxNum);
		}
		noVoice = (totalVoiceSize < 32) || (maxNum <= 0) || (maxRange <= 50);
	}

	/**
	 * Determine whether agents can use radio channel to communicate.
	 * @return  true when there is no radio channel, and false when radio channel exist
	 */
	public boolean isNoRadio() {
		return noRadio;
	}
	/**
	 * Determine whether an agent can use voice channel to communicate.
	 * @return true when there is no voice channel, and false when voice channel exist
	 */
	public boolean isNoVoice() {
		return noVoice;
	}
	

	/**
	 * Write an Area into message. For a Area, its either a Building or a Road. And I set 0 stands
	 * for Building and 1 stands for Road. So when reading a Area, I can know which will be read. 
	 * @param sec  MessageBitSection which store message
	 * @param area  the Area need to be writing
	 * @param uniform  the Uniform number of this Area
	 */
	public void writeArea(MessageBitSection sec, final Area area, final Uniform uniform) {
		final int u = uniform.toUniform(area.getID());
		if (area instanceof Building) {
			sec.add(0, 1);
			sec.add(u, this.BUILDING_UNIFORM_BIT_SIZE);
		} else /*if (location instanceof Road)*/ {
			sec.add(1, 1);
			sec.add(u, this.ROAD_UNIFORM_BIT_SIZE);
		}
	}

	/**
	 * Read an Area object from BitArrayInputStream.
	 * @param stream  the BitArrayInputStream
	 * @param uniform  the Uniform which can get the ID of this Area.
	 * @return the ID of this Area
	 */
	public EntityID readArea(BitArrayInputStream stream, final Uniform uniform) {
		int areaFlg = stream.readBit(1);
		if (areaFlg == 0) {
			return uniform.toID(StandardEntityURN.BUILDING, stream.readBit(this.BUILDING_UNIFORM_BIT_SIZE));
		}
		else /*if (areaFlg == 1)*/ {
			return uniform.toID(StandardEntityURN.ROAD, stream.readBit(this.ROAD_UNIFORM_BIT_SIZE));
		}
	}
}
