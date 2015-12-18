package csu.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import rescuecore2.config.Config;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;

/**
 * This class handle some useful constants</code> which gets from config files.
 */
public final class ConfigConstants {
	
	/* Config value for rays.*/
//	public float rayRate;
//	public int maxRayDistance;
//	public int randomSeed;
	
	/** The debug flag.*/
	public final boolean debug = false;
	
	/** The total number of simulation cycles. */
	public final int timestep;
	/**
	 * The time an Agent can used to think. And for each Agent, it must be
	 * handle all data and then send commands to kernel within that time,
	 * otherwise, the commnad will be ignored.
	 */
	public final int thinkTime;
	
	/** Channel key perfix. */
	private static final String CHANNELS_KEY_PREFIX = "comms.channels.";

	public final int ignoreUntil;
	/** The number of available channels.*/
	public final int channelCount;
	
	/** The number of channels a platoon Agent can subscribe to. */
	public final int subscribePlatoonSize;
	
	/** The number of channels a centre Agent can subscribe to. */
	public final int subscribeCenterSize;
	
	/** A map of voice channels which store channel's ID and channel instance pairs.*/
	public final Map<Integer, VoiceChannel> voiceChannels = new HashMap<Integer, VoiceChannel>();
	/** A map of radio channels which store channel's ID and channel instance pairs.*/
	public final Map<Integer, RadioChannel> radioChannels = new HashMap<Integer, RadioChannel>();
	
	/** Ignition temperature of wood. */
	public final double woodenIgnition;
	/** Ignition temperature of the steel. */
	public final double steelIgnition;
	/** Ignition temperature of the concrete. */
	public final double concreteIgnition;
	
	/** Maximum distance the Agent can perceive world changes. */
	public final int viewDistance;
	/** HP's precision unit. */
	public final int hpPrecision;
	/** HP's maximum value. */
	public final int hpMax;
	/** Damage's precision unit. */
	public final int damagePrecision;
	
	/** Building's maximum value*/
	public final int buildingIDMax;
	
	/** Maximum capacity of FB's water tank. */
	public final int maxTankCapacity;
	/** Maximum amount of water that can be used per timestep. */
	public final int maxPower;
	/** Water tank refill rate. */
	public final int tankRefillRate;
	/** Water tank refill rate in hydrant. */
	public final int tankRefillHydrantRate;
	/** Maximum extinguish distance. */
	public final int extinguishableDistance;

	/** Maximun distance a PF can clear in mm per timestep. */
	public final int repairDistance;
	/** The rate of road clearing per police force agent in square m per time timestep. */
	public final int repairRate;
	/** The maximum width this agent can clear.*/
	public final int repairRad;

	/** The X coordinate of the top left corners. */
	public final int MIN_X;
	/** The X coordinate of the bottom right corners. */
	public final int MAX_X;
	/** The Y coordinate of the top left corners. */
	public final int MIN_Y;
	/** The Y coordinate of the bottom right corners. */
	public final int MAX_Y;
	
	/** the random number generator*/
	public final Random random;
	
	/** collapse damage progression : k*/
	public final double collapse_k; 
	
	/** collapse damage progression : l*/
	public final double collapse_l;
	
	/** collapse damage progression : noise mean*/
	public final double collapse_mean;
	
	/** collapse damage progression : noise sd*/
	public final double collapse_sd;
	
	/** bury damage progression : k*/
	public final double bury_k;
	
	/** bury damage progression : l*/
	public final double bury_l;
	
	/** bury damage progression : noise mean*/
	public final double bury_mean;
	
	/** bury damage progression : noise sd*/
	public final double bury_sd;
	
	// bury injury rate
	/** slight Injury rate for bury damage in wood building*/
	public final double bury_wood_slight;
	
	/** serious Injury rate for bury damage in wood building*/
	public final double bury_wood_serious;
	
	/** critical Injury rate for bury damage in wood building*/
	public final double bury_wood_critical;
	
	/** slight Injury rate for bury damage in steel building*/
	public final double bury_steel_slight;
	
	/** serious Injury rate for bury damage in steel building*/
	public final double bury_steel_serious;
	
	/** critical Injury rate for bury damage in steel building*/
	public final double bury_steel_critical;
	
	/** slight Injury rate for bury damage in concrete building*/
	public final double bury_concrete_slight;
	
	/** serious Injury rate for bury damage in concrete building*/
	public final double bury_concrete_serious;
	
	/** critical Injury rate for bury damage in concrete building*/
	public final double bury_concrete_critical;
	
	// collapse injury rate
	/** slight Injury rate for collapse damage in wood building*/
	public final double collapse_wood_slight;
	
	/** serious Injury rate for collapse damage in wood building*/
	public final double collapse_wood_serious;
	
	/** critical Injury rate for collapse damage in wood building*/
	public final double collapse_wood_critical;
	
	/** slight Injury rate for collapse damage in steel building*/
	public final double collapse_steel_slight;
	
	/** serious Injury rate for collapse damage in steel building*/
	public final double collapse_steel_serious;
	
	/** critical Injury rate for collapse damage in steel building*/
	public final double collapse_steel_critical;
	
	/** slight Injury rate for collapse damage in concrete building*/
	public final double collapse_concrete_slight;
	
	/** serious Injury rate for collapse damage in concrete building*/
	public final double collapse_concrete_serious;
	
	/** critical Injury rate for collapse damage in concrete building*/
	public final double collapse_concrete_critical;
	
	//Injury value
	/** slight Injury value for bury damage*/
	public final int bury_slight;
	
	/** serious Injury value for bury damage*/
	public final int bury_serious;
	
	/** critical Injury value for bury damage*/
	public final int bury_critical;
	
	/** slight Injury value for collapse damage*/
	public final int collapse_slight;
	
	/** serious Injury value for collapse damage*/
	public final int collapse_serious;
	
	/** critical Injury value for collapse damage*/
	public final int collapse_critical;
	
	/** the max value of round hp*/
	public final int maxRoundHP ;
	
	//=====================Ambulance Team=================================
	
	static public abstract class Channel {
		public final int channel;
		protected Channel(int c) {
			channel = c;
		}
	}
	
	/**
	 * Class for the voice communication channels. An Agent can utter nature
	 * vocie through this channel. So all Agents within a certain range of the
	 * voice source can hear this nature voice. Just like you are shouting all
	 * people around you will know the contents you shouted.
	 */
	static public class VoiceChannel extends Channel {
		/**
		 * The id of this voice channel.
		 */
		public final int id;
		/** 
		 * The maximum propagation range of a voice message in mm(millimeter). 
		 */
		public final int range;
		/** 
		 * The maximum size of a voice message in bytes. 
		 */
		public final int size;
		/**
		 * The maximum number of voice message an agent can send in one cycle.
		 */
		public final int maxNum;

		/**
		 * Constructor a voice channel.
		 * 
		 * @param channel
		 *            the ID of the voice channel
		 * @param config
		 *            config file
		 */
		private VoiceChannel(int channel, final Config config) {
			super(channel);
			this.id = channel;
			
			final String RANGE_SUFFIX = ".range";
			final String MESSAGE_SIZE_SUFFIX = ".messages.size";
			final String MESSAGE_MAX_SUFFIX = ".messages.max";
			
			range = config.getIntValue(CHANNELS_KEY_PREFIX + channel + RANGE_SUFFIX);
			size = config.getIntValue(CHANNELS_KEY_PREFIX + channel + MESSAGE_SIZE_SUFFIX);
			maxNum = config.getIntValue(CHANNELS_KEY_PREFIX + channel + MESSAGE_MAX_SUFFIX);
		}
	}
	
	/**
	 * Class for the radio communication channels. 
	 */
	static public class RadioChannel extends Channel {
		/**
		 * The id of this voice channel.
		 */
		public final int id;
		/** 
		 * The maximum capacity of the channel in bytes.. 
		 */
		final public int bandwidth;
		/** 
		 * The rate a input message failure. 
		 */
		public double inputFailureRate;
		/** 
		 * The rate a output message failure. 
		 */
		public double outputFailureRate;
		/** 
		 * The rate a input message dropout. 
		 */
		public double inputDropoutRate;
		/** 
		 * The rate a output message dropout. 
		 */
		public double outputDropoutRate;

		final String INPUT_FAILURE_SUFFIX = ".noise.input.failure.p";
		final String INPUT_FAILURE_USE_SUFFIX = ".noise.input.failure.use";
		final String OUTPUT_FAILURE_SUFFIX = ".noise.output.failure.p";
		final String OUTPUT_FAILURE_USE_SUFFIX = ".noise.output.failure.use";

		final String INPUT_DROPOUT_SUFFIX = ".noise.input.dropout.p";
		final String INPUT_DROPOUT_USE_SUFFIX = ".noise.input.dropout.use";
		final String OUTPUT_DROPOUT_SUFFIX = ".noise.output.dropout.p";
		final String OUTPUT_DROPOUT_USE_SUFFIX = ".noise.output.dropout.use";

		/**
		 * Constructor of a radio channel.
		 * 
		 * @param channel
		 *            the ID of a radio channel
		 * @param config
		 *            config file
		 */
		private RadioChannel(int channel, final Config config) {
			super(channel);
			this.id = channel;
			
			final String RADIO_BAND_WIDTH_KEY = ".bandwidth";
			bandwidth = config.getIntValue(CHANNELS_KEY_PREFIX + channel + RADIO_BAND_WIDTH_KEY);

			try {
				if (config.getBooleanValue(CHANNELS_KEY_PREFIX + channel + INPUT_FAILURE_USE_SUFFIX)) {
					inputFailureRate = config.getFloatValue(CHANNELS_KEY_PREFIX
							+ channel + INPUT_FAILURE_SUFFIX);
				} else {
					inputFailureRate = 0.0;
				}
			} catch (Exception e) {
				inputFailureRate = 0.0;
			}

			try {
				if (config.getBooleanValue(CHANNELS_KEY_PREFIX + channel + OUTPUT_FAILURE_USE_SUFFIX)) {
					outputFailureRate = config.getFloatValue(CHANNELS_KEY_PREFIX 
							+ channel + OUTPUT_FAILURE_SUFFIX);
				} else {
					outputFailureRate = 0.0;
				}
			} catch (Exception e) {
				outputFailureRate = 0.0;
			}

			try {
				if (config.getBooleanValue(CHANNELS_KEY_PREFIX + channel + INPUT_DROPOUT_USE_SUFFIX)) {
					inputDropoutRate = config.getFloatValue(CHANNELS_KEY_PREFIX
							+ channel + INPUT_DROPOUT_SUFFIX);
				} else {
					inputDropoutRate = 0.0;
				}
			} catch (Exception e) {
				inputDropoutRate = 0.0;
			}

			try {
				if (config.getBooleanValue(CHANNELS_KEY_PREFIX + channel + OUTPUT_DROPOUT_USE_SUFFIX)) {
					outputDropoutRate = config.getFloatValue(CHANNELS_KEY_PREFIX 
							+ channel + OUTPUT_DROPOUT_SUFFIX);
				} else {
					outputDropoutRate = 0.0;
				}
			} catch (Exception e) {
				outputDropoutRate = 0.0;
			}
		}
	}
	
	public ConfigConstants(final Config config, AdvancedWorldModel world) {
		
//		final String RAY_RATE_KEY = "resq-fire.ray_rate";
//		rayRate = (float)config.getFloatValue(RAY_RATE_KEY);
//		final String MAX_RAY_DISTANCE_KEY = "resq-fire.max_ray_distance";
//		maxRayDistance = config.getIntValue(MAX_RAY_DISTANCE_KEY);
//		final String RANDOM_SEED_KEY = "resq-fire.randomseed";
//		randomSeed = config.getIntValue(RANDOM_SEED_KEY);
		
		// general
		final String IGNORE_UNTIL_KEY = "kernel.agents.ignoreuntil";
		/** The default ignore time. */
		final int DEFAULT_IGNORE_UNTIL = 3;
		ignoreUntil = config.getIntValue(IGNORE_UNTIL_KEY, DEFAULT_IGNORE_UNTIL);
		
		final String TIMESTEP_KEY = "kernel.timesteps";
		/** The default simulation circles. */
		final int DEFAULT_TIMESTEP = 300;
		timestep = config.getIntValue(TIMESTEP_KEY, DEFAULT_TIMESTEP);
		
		final String THINK_TIME_KEY = "kernel.agents.think-time";
		/** An Agent's default think time. */
		final int DEFAULT_THINK_TIME = 1000;
		thinkTime = config.getIntValue(THINK_TIME_KEY, DEFAULT_THINK_TIME);
		
		// communication
		final String CHANNELS_COUNT_KEY = CHANNELS_KEY_PREFIX + "count";
		channelCount = config.getIntValue(CHANNELS_COUNT_KEY);
		
		final String SUBSCRIBE_PLATOON_KEY = CHANNELS_KEY_PREFIX + "max.platoon";
		subscribePlatoonSize = config.getIntValue(SUBSCRIBE_PLATOON_KEY, 0);
		
		final String SUBSCRIBE_CENTER_KEY = CHANNELS_KEY_PREFIX + "max.centre";
		subscribeCenterSize = config.getIntValue(SUBSCRIBE_CENTER_KEY, 0);
		
		final String CHANNELS_TYPE_KEY = ".type";
		for (int i = 0; i < channelCount; i++) {
			final String type = config.getValue(CHANNELS_KEY_PREFIX + i + CHANNELS_TYPE_KEY);
			if (type.startsWith("voice")) {
				voiceChannels.put(i, new VoiceChannel(i, config));
			} else if (type.startsWith("radio")) {
				radioChannels.put(i, new RadioChannel(i, config));
			}
		}
		
		// building
		final String WOODEN_IGNITION_KEY = "resq-fire.wooden_ignition";
		final double DEFAULT_WOODEN_IGNITION = 47.0f;
		woodenIgnition = config.getFloatValue(WOODEN_IGNITION_KEY, DEFAULT_WOODEN_IGNITION);
		
		final String STEEL_IGNITION_KEY = "resq-fire.steel_ignition";
		final double DEFAULT_STEEL_IGNITION = 47.0f;
		steelIgnition = config.getFloatValue(STEEL_IGNITION_KEY, DEFAULT_STEEL_IGNITION);
		
		final String CONCRETE_IGNITION_KEY = "resq-fire.concrete_ignition";
		final double DEFAULT_CONCRETE_IGNITION = 47.0f;
		concreteIgnition = config.getFloatValue(CONCRETE_IGNITION_KEY, DEFAULT_CONCRETE_IGNITION);
		
		// platoon
		final String VIEW_DISTANCE_KEY = "perception.los.max-distance";
	    final int DEFAULT_VIEW_DISTANCE = 30000;
		viewDistance = config.getIntValue(VIEW_DISTANCE_KEY, DEFAULT_VIEW_DISTANCE);
		
		final String HP_PERSEPTION_KEY = "perception.los.precision.hp";
		final int DEFAULT_HP_PERSEPTION = 1000;
		hpPrecision = config.getIntValue(HP_PERSEPTION_KEY, DEFAULT_HP_PERSEPTION); 
		
		final String DAMAGE_PERSEPTION_KEY = "perception.los.precision.damage";
		final int DEFAULT_DAMAGE_PERSEPTION = 100;
		damagePrecision = config.getIntValue(DAMAGE_PERSEPTION_KEY, DEFAULT_DAMAGE_PERSEPTION);
		
		// fire brigade
		final String MAX_WATER_KEY = "fire.tank.maximum";
		final int DEFAULT_TANK_CAPACITY = 15000;
		maxTankCapacity = config.getIntValue(MAX_WATER_KEY, DEFAULT_TANK_CAPACITY);
		
		final String MAX_EXTINGUISHABLE_KEY = "fire.extinguish.max-distance";
		final int DEFAULT_EXTINGUISHABLE_DISTANCE = 30000;
		extinguishableDistance = config.getIntValue(MAX_EXTINGUISHABLE_KEY, DEFAULT_EXTINGUISHABLE_DISTANCE);
		
		final String MAX_POWER_KEY = "fire.extinguish.max-sum";
		final int DEFAULT_POWER = 1000;
		maxPower = config.getIntValue(MAX_POWER_KEY, DEFAULT_POWER);
		
		final String WATER_REFILL_KEY = "fire.tank.refill-rate";
		final int DEFAULT_REFILL_RATE = 2000;
		tankRefillRate = config.getIntValue(WATER_REFILL_KEY, DEFAULT_REFILL_RATE);

		final 	String WATER_REFILL_HYDRANT_KEY = "fire.tank.refill_hydrant_rate";
		final int DEFAULT_REFILL_HUDRANT_RATE = 150;
		tankRefillHydrantRate = config.getIntValue(WATER_REFILL_HYDRANT_KEY, DEFAULT_REFILL_HUDRANT_RATE);
		
		// police force
		final String DISTANCE_KEY = "clear.repair.distance";
		final int DEFAULT_REPAIR_DISTANCE = 10000;
		repairDistance = config.getIntValue(DISTANCE_KEY, DEFAULT_REPAIR_DISTANCE);
		
		final int DEFAULT_REPAIR_RATE = 10;
		final String REPAIR_RATE_KEY = "clear.repair.rate";
		repairRate = config.getIntValue(REPAIR_RATE_KEY, DEFAULT_REPAIR_RATE);
		
		final String REPAIR_RAD_KEY = "clear.repair.rad";
		repairRad = config.getIntValue(REPAIR_RAD_KEY);
		
		MIN_X = world.getWorldBounds().first().first();
		MIN_Y = world.getWorldBounds().first().second();
		MAX_X = world.getWorldBounds().second().first();
		MAX_Y = world.getWorldBounds().second().second();
		
		int maxHP = 0;
		for (StandardEntity se: world.getEntitiesOfType(AgentConstants.HUMANOIDS)) {
			Human hm = (Human) se;
			if (hm.isHPDefined()) {
				maxHP = Math.max(maxHP, hm.getHP());
			}
		}
		
		hpMax = maxHP;
		
		int maxBuildingID = 0;
		for (StandardEntity se : world.getEntitiesOfType(AgentConstants.BUILDINGS)) {
			if (maxBuildingID < se.getID().getValue())
				maxBuildingID = se.getID().getValue();
		}
		buildingIDMax = maxBuildingID;
		
		/* -------------- at -------------*/
		random = config.getRandom();
		collapse_k = 0.00025;
		collapse_l = 0.01;
		collapse_mean = 0.1;
		collapse_sd = 0.01;
		
		bury_k = 0.000035;
		bury_l = 0.01;
		bury_mean = 0.1;
		bury_sd = 0.01;
		
		collapse_wood_slight = 0.5;
		collapse_wood_serious = 0.3;
		collapse_wood_critical = 0.02;
		
		collapse_steel_slight = 0.1;
		collapse_steel_serious = 0.03;
		collapse_steel_critical = 0.005;
		
		collapse_concrete_slight = 0.1;
		collapse_concrete_serious = 0.03;
		collapse_concrete_critical = 0.005;
		
		bury_wood_slight = 0.4;
		bury_wood_serious = 0.5;
		bury_wood_critical = 0.1;
		
		bury_steel_slight = 0.4;
		bury_steel_serious = 0.5;
		bury_steel_critical = 0.1;
		
		bury_concrete_slight = 0.4;
		bury_concrete_serious = 0.5;
		bury_concrete_critical = 0.1;
		
		collapse_slight = 2;
		collapse_serious = 10;
		collapse_critical = 10000;
		
		bury_slight = 3;
		bury_serious = 15;
		bury_critical = 100;
		
		if ((this.hpMax % this.hpPrecision) > 0){
			maxRoundHP = (hpMax / this.hpPrecision + 1) * this.hpPrecision;
		}
		else maxRoundHP = hpMax;
	}
	
}