package csu.model.object;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastSet;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.tools.FbUtilities;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.util.Util;

public class CSUBuilding {
	/** TestViewer will use this to test CSUBuilding realted properties. */
	public static Map<EntityID, Map<EntityID, CSUBuilding>> VIEWER_BUILDING_MAP = new HashMap<>();

	/** A list of Areas in extinguishable range of this building. */
	private List<EntityID> areasInExtinguishableRange;

	/**
	 * Buildings will radiate energy to other buildings. And here, we stores all
	 * buildings within the radiation range of this building. And the range we
	 * used is larger than the real radiation range.
	 */
	private List<CSUBuilding> radiationNeighbourBuildings;
	/**
	 * See {@link #radiationNeighbourBuildings radiationNeighbourBuildings}.
	 */
	private List<EntityID> radiationNeighbourBuildingsId;

	private CsuLineOfSightPerception lineOfSightPerception;
	private List<EntityID> observableAreas;

	/** A list of walls of this building. */
	private List<CSUWall> walls;
	/** The total wall area of this building. */
	private double totalWallArea;
	/** All walls of this building and its neighbours. */
	private List<CSUWall> allWalls;

	/**
	 * This table's key is the connected building of this building. And this
	 * table's value is the number of rays, which is emitted by this building,
	 * connected building received.
	 */
	private Hashtable<CSUBuilding, Integer> connectedBuildingTable;
	/**
	 * A list of buildings that was hitted by rays emitted by this building. And
	 * this list is consistent with {@link #connectedValues}.
	 */
	private List<CSUBuilding> connectedBuildings;
	/**
	 * The value is the ratio of rays, which is emitted by this building, each
	 * connected building received. And the sorted order is the order connected
	 * buildings sorted in {@link #connectedBuildings}.
	 */
	private List<Float> connectedValues;

	/** The underlying building of this CSUBuilding object. */
	private Building selfBuilding;
	private AdvancedWorldModel world;
	/**
	 * Flag to determine whether this building have been visited by other Agent.
	 */
	private boolean visited;

	/** Danger buildings are fired buildings and warm buildings. */
	private Set<CSUBuilding> neighbourDangerBuildings;

	/**
	 * The distance between agent current position and this building. Also
	 * consider the fieryness of this building.
	 */
	public double BUILDING_VALUE = Double.MIN_VALUE;///
	/**new priority for buildings*/
	public double priority =Double.MIN_VALUE;
	/** The real ignition time of this building. */
	private int ignitionTime = -1;

	private int zoneId;

	/** The distance between Agent current location to nearest refuge.. */
	private double advantageRatio;

	private boolean isVisible = false;
	

	public static final EnumSet<StandardEntityConstants.Fieryness> ESTIMATED_BURNING = EnumSet
			.of(StandardEntityConstants.Fieryness.HEATING,
					StandardEntityConstants.Fieryness.BURNING,
					StandardEntityConstants.Fieryness.INFERNO);

	public CSUBuilding() {
		// do nothing
	};

	public CSUBuilding(StandardEntity entity, AdvancedWorldModel world) {
		this.world = world;
		this.selfBuilding = (Building) entity;
		this.connectedBuildingTable = new Hashtable<CSUBuilding, Integer>(30);
		this.connectedBuildings = new ArrayList<CSUBuilding>();
		this.connectedValues = new ArrayList<Float>();

		this.radiationNeighbourBuildings = new ArrayList<CSUBuilding>();
		this.radiationNeighbourBuildingsId = new ArrayList<EntityID>();
		this.neighbourDangerBuildings = new FastSet<CSUBuilding>();

		this.lineOfSightPerception = new CsuLineOfSightPerception(world);

		this.visited = false;

		this.initWalls(world);
		this.initSimulatorValues();
	}

	/**
	 * This method was invoked in
	 * {@link csu.model.AdvancedWorldModel#createCsuBuildings()}
	 * 
	 * @param neighbour
	 *            a neighboured building of this building
	 */
	public void addNeighbourBuilding(CSUBuilding neighbour) {
		this.radiationNeighbourBuildings.add(neighbour);
		this.radiationNeighbourBuildingsId.add(neighbour.getSelfBuilding()
				.getID());
		this.allWalls.addAll(neighbour.getWalls());
	}

	/**
	 * Initialise the {@link CSUWall} of this <code>CSUBuilding</code>.
	 * 
	 * @param world
	 *            a reference of world model
	 */
	private void initWalls(AdvancedWorldModel world) {
		int[] apexList = this.selfBuilding.getApexList();
		int firstX = apexList[0], firstY = apexList[1];
		int lastX = firstX, lastY = firstY;
		CSUWall wall;
		this.walls = new ArrayList<>();
		this.allWalls = new ArrayList<>();

		for (int i = 2; i < apexList.length; i++) {
			int tempX = apexList[i], tempY = apexList[++i];
			wall = new CSUWall(lastX, lastY, tempX, tempY, this, world);
			if (wall.validate()) {
				this.walls.add(wall);
				this.totalWallArea += FLOOR_HEIGHT * wall.length * 1000;
			} else {
				Logger.warn("Ignoring odd wall at building "
						+ selfBuilding.getID().getValue());
			}
			lastX = tempX;
			lastY = tempY;
		}
		wall = new CSUWall(lastX, lastY, firstX, firstY, this, world);
		if (wall.validate()) {
			this.walls.add(wall);
		}
		allWalls.addAll(walls);
		this.totalWallArea = this.totalWallArea / 1000000d;
	}

	/**
	 * This method is invoked in
	 * {@link FireBrigadeWorld#initialize(csu.agent.Agent, rescuecore2.config.Config, csu.standard.GroupingType)
	 * initialize()} method of FireBrigadeWorld.
	 * <p>
	 * 
	 * 
	 * @param world
	 */
	public void initWallValue(FireBrigadeWorld world) {
		int totalRays = 0; // total number of rays this building emitted
		for (CSUWall wall : this.walls) {
			wall.findHits(world, this);
			totalRays += wall.rays;
		}

		CSUBuilding building;
		for (Enumeration<CSUBuilding> e = this.connectedBuildingTable.keys(); e
				.hasMoreElements();) {
			building = (CSUBuilding) e.nextElement();
			float value = this.connectedBuildingTable.get(building);
			this.connectedBuildings.add(building);
			this.connectedValues.add(value / (float) totalRays);
		}
	}

	/** Get total radiated value to other buildings. */
	public double getBuildingRadiation() {
		double value = 0;
		CSUBuilding building;

		for (int i = 0; i < this.connectedValues.size(); i++) {
			building = this.connectedBuildings.get(i);
			if (building.isBurning()) {
				value += connectedValues.get(i);
			}
		}
		return value * this.getEstimatedTemperature() / 1000;
	}

	/** Get total radiation values from other buildings. */
	public double getNeighbourRadiation() {
		double value = 0;
		int index = 0;
		for (CSUBuilding building : this.radiationNeighbourBuildings) {
			index = building.getConnectedBuildings().indexOf(this);
			if (index >= 0) {
				value += building.getConnectedValues().get(index)
						* building.getEstimatedTemperature();
			}
		}
		return value / 1000;
	}

	/** Determines whether this building is burning. */
	public boolean isBurning() {
		return getEstimatedFieryness() > 0 && getEstimatedFieryness() < 4;
	}

	public List<CSUWall> getWalls() {
		return this.walls;
	}

	public List<CSUWall> getAllWalls() {
		return this.allWalls;
	}

	public List<EntityID> getAreasInExtinguishableRange() {
		if (areasInExtinguishableRange == null
				|| areasInExtinguishableRange.isEmpty()) {
			areasInExtinguishableRange = new ArrayList<>();
			int range = (int) (world.getConfig().extinguishableDistance * 0.9);
			// if (world.isNoRadio()) {
			// range = (int)(world.getConfig().viewDistance * 0.9);
			// } else {
			// range = (int)(world.getConfig().extinguishableDistance * 0.9);
			// }
			for (StandardEntity next : world.getObjectsInRange(getId(), range)) {
				if (next instanceof Area)
					areasInExtinguishableRange.add(next.getID());
			}
		}

		return areasInExtinguishableRange;
	}
	
	/**
	 * In a fire area, if one building is left extinguished, the area is easily get fired soon.
	 * Since the fire brigade selects this area, it must extinguish entirely.
	 * added by oak
	 * @return
	 */
	public Set<CSUBuilding> getNeighbourDangerBuildings() {
		neighbourDangerBuildings = new HashSet<>();
		for(EntityID id : this.selfBuilding.getNeighbours()) {
			if(world.getEntity(id) instanceof Building) {
				Building build = (Building) world.getEntity(id);
				if(build.isOnFire()) ///|| build.isTemperatureDefined() && build.getTemperature() > 35)
					neighbourDangerBuildings.add(world.getCsuBuilding(build));
			}
		}
		return neighbourDangerBuildings;
	}

//	public void setNeighbourDangerBuildings(
//			Set<CSUBuilding> neighbourDangerBuildings) {
//		this.neighbourDangerBuildings = neighbourDangerBuildings;
//	}

	
	
	public List<CSUBuilding> getRadiationNeighbourBuildings() {
		return radiationNeighbourBuildings;
	}

	public List<EntityID> getRadiationNeighbourBuildinsId() {
		return radiationNeighbourBuildingsId;
	}

	public Hashtable<CSUBuilding, Integer> getConnectedBuildingTable() {
		return connectedBuildingTable;
	}

	public void setConnectedBuildingTable(
			Hashtable<CSUBuilding, Integer> connectedBuildingTable) {
		this.connectedBuildingTable = connectedBuildingTable;
	}

	public List<CSUBuilding> getConnectedBuildings() {
		return connectedBuildings;
	}

	public void setConnectedBuildins(List<CSUBuilding> connected) {
		this.connectedBuildings = connected;
	}

	public List<Float> getConnectedValues() {
		return connectedValues;
	}

	public void setConnectedValues(List<Float> connectedValues) {
		this.connectedValues = connectedValues;
	}

	
	public int getIgnitionTime() {
		return ignitionTime;
	}

	public void setIgnitionTime(int ignitionTime) {
		this.ignitionTime = ignitionTime;
	}

	public boolean isVisited() {
		return this.visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public List<EntityID> getObservableAreas() {
		if (observableAreas == null || observableAreas.isEmpty()) {
			observableAreas = lineOfSightPerception.getVisibleAreas(getId());
		}

		return observableAreas;
	}

	/*
	 * ---------------------------------------- fire simulator part
	 * ---------------------------------------
	 */
	static final int FLOOR_HEIGHT = 3;
	static float RADIATION_COEFFICIENT = 0.011f;
	static final double STEFAN_BOLTZMANN_CONSTANT = 0.000000056704;

	private int startTime = -1;

	/** The fuel of this building. */
	private float fuel;
	/** Initial fuel of this building. */
	private float initFuel = -1;

	/** The volume of this building. */
	private float volume;
	/** The energy of this building. */
	private double energy;

	/** The total thermo capacity of this building. */
	private float capacity;

	/** The number of previous burned. */
	private float prevBurned;

	/**
	 * The water quantity in this building, default is zero. Every time this
	 * building was extinguished, it water quantity will increase. If the effect
	 * of those water less than the energy difference which is current energy
	 * value decreases by the initial energy value, all the water will be
	 * evaporated. If the effect of those water more than the energy difference,
	 * the water will has surplus.
	 */
	private int waterQuantity = 0;

	private int lwater = 0;
	private int lwTime = -1;
	private boolean wasEverWatered = false;
	/** Unflammable buildings are Refuge and three kind of center. */
	private boolean inflammable = true;

	/* The ignition point of different code. */
	public static float woodIgnition = 47.0f;
	public static float steelIgnition = 47.0f;
	public static float concreteIgnition = 47.0f;

	/* The thermo capacity of different code. */
	public static float woodCapacity = 1.1f;
	public static float steelCapacity = 1.0f;
	public static float concreteCapacity = 1.5f;

	/* The energy density of diffeeent code. */
	public static float woodEnergy = 2400.0f;
	public static float steelEnergy = 800.0f;
	public static float concreteEnergy = 350.0f;

	/* Unhnown member variables, and has not been used. */
	public static float woodBurning = 800.0f;
	public static float steelBurning = 850.0f;
	public static float concreteBurning = 800.0f;

	public void initSimulatorValues() {
		volume = selfBuilding.getGroundArea() * selfBuilding.getFloors()
				* FLOOR_HEIGHT;
		fuel = getInitialFuel();
		capacity = (volume * getThermoCapacity());
		energy = 0;
		initFuel = -1;
		prevBurned = 0;

		lwater = 0;
		lwTime = -1;
		wasEverWatered = false;

		Logger.info("Initialised the simulator values for building "
				+ selfBuilding.getID() + ": ground area = "
				+ selfBuilding.getGroundArea() + ", floors = "
				+ selfBuilding.getFloors() + ", volume = " + volume
				+ ", initial fuel = " + initFuel + ", energy capacity = "
				+ getCapacity());
	}

	/** Get the initial fuel of this building. (fuel_density * building_vilume). */
	public float getInitialFuel() {
		if (initFuel < 0)
			initFuel = getFuelDensity() * volume;
		return initFuel;
	}

	/** Get the thermo capacity per unit volume of this building. */
	public float getThermoCapacity() {
		switch (selfBuilding.getBuildingCode()) {
		case 0:
			return woodCapacity;
		case 1:
			return steelCapacity;
		default:
			return concreteCapacity;
		}
	}

	/** Get the ignition point of this building. */
	public float getIgnitionPoint() {
		switch (selfBuilding.getBuildingCode()) {
		case 0:
			return woodIgnition;
		case 1:
			return steelIgnition;
		default:
			return concreteIgnition;
		}
	}

	/** Get the fuel density of this building. */
	public float getFuelDensity() {
		switch (selfBuilding.getBuildingCode()) {
		case 0:
			return woodEnergy;
		case 1:
			return steelEnergy;
		default:
			return concreteEnergy;
		}
	}

	/** Get the consumed fuel of this cycle. */
	public float getConsume(double burnRate) {
		if (fuel == 0)
			return 0;
		float tf = (float) (getEstimatedTemperature() / 1000f);
		float lf = fuel / getInitialFuel();
		float f = (float) (tf * lf * burnRate);
		if (f < 0.005f)
			f = 0.005f;
		return getInitialFuel() * f;

		// float f = (float)(tf * lf * 0.2);
		// if (f < 0.005f)
		// f = 0.005f;
		// return (float)(getInitialFuel() * f * burnRate);
	}

	public double getEstimatedTemperature() {
		double rv = energy / capacity;

		if (Double.isNaN(rv)) {
			Logger.warn("Building " + selfBuilding.getID()
					+ " getTemperature returned NaN");
			new RuntimeException().printStackTrace();
			Logger.warn("Energy: " + energy);
			Logger.warn("Capacity: " + getCapacity());
			Logger.warn("Volume: " + volume);
			Logger.warn("Thermal capacity: " + getThermoCapacity());
			Logger.warn("Ground area: " + selfBuilding.getGroundArea());
			Logger.warn("Floors: " + selfBuilding.getFloors());

		}
		if (rv == Double.NaN || rv == Double.POSITIVE_INFINITY
				|| rv == Double.NEGATIVE_INFINITY)
			rv = Double.MAX_VALUE * 0.75;
		return rv;
	}

	public int getEstimatedFieryness() {
		if (!isInflammable())
			return 0;
		if (getEstimatedTemperature() >= getIgnitionPoint()) {
			if (fuel >= getInitialFuel() * 0.66)
				return 1; // HEATING
			if (fuel >= getInitialFuel() * 0.33)
				return 2; // BURNINGS
			if (fuel > 0)
				return 3; // INFERNO
		}
		if (fuel == getInitialFuel())
			if (wasEverWatered)
				return 4;
			else
				return 0;
		if (fuel >= getInitialFuel() * 0.66)
			return 5;
		if (fuel >= getInitialFuel() * 0.33)
			return 6;
		if (fuel > 0)
			return 7;
		return 8;
	}

	/**
	 * Get the radiated energy of this building. And assume the ambient
	 * temperature is 293 Kelvin.
	 * 
	 * @return the radiated energy of this energy
	 */
	public double getRadiationEnergy() { // /273 293
		double t = this.getEstimatedTemperature() + 273; // Assume ambient
															// temperature is
															// 293 Kelvin.
		double radEn = (t * t * t * t) * RADIATION_COEFFICIENT
				* STEFAN_BOLTZMANN_CONSTANT * totalWallArea;

		if (selfBuilding.getID().getValue() == 23545) {
			Logger.debug("Getting radiation energy for building "
					+ selfBuilding.getID().getValue());
			Logger.debug("t = " + t);
			Logger.debug("t^4 = " + (t * t * t * t));
			Logger.debug("Total wall area: " + totalWallArea);
			Logger.debug("Radiation coefficient: " + RADIATION_COEFFICIENT);
			Logger.debug("Stefan-Boltzmann constant: "
					+ STEFAN_BOLTZMANN_CONSTANT);
			Logger.debug("Radiation energy: " + radEn);
			Logger.debug("Building energy: " + getEnergy());
		}

		if (radEn == Double.NaN || radEn == Double.POSITIVE_INFINITY
				|| radEn == Double.NEGATIVE_INFINITY)
			radEn = Double.MAX_VALUE * 0.75;
		if (radEn > getEnergy()) {
			radEn = getEnergy();
		}
		return radEn;
	}

	public Building getSelfBuilding() {
		return selfBuilding;
	}

	public float getVolum() {
		return this.volume;
	}

	public float getCapacity() {
		return this.capacity;
	}

	public int getRealFieryness() {
		return this.selfBuilding.getFieryness();
	}

	public int getRealTemperature() {
		return this.selfBuilding.getTemperature();
	}

	/**
	 * Get the energy of this building.
	 * 
	 * @return the energy of this building
	 */
	public double getEnergy() {
		if (energy == Double.NaN || energy == Double.POSITIVE_INFINITY
				|| energy == Double.NEGATIVE_INFINITY)
			energy = Double.MAX_VALUE * 0.75d;

		return this.energy;
	}

	/**
	 * Set the energy of this building.
	 * 
	 * @param value
	 *            the new value of energy
	 */
	public void setEnergy(double value, String invokeMethod) {
		if (value == Double.NaN || value == Double.POSITIVE_INFINITY
				|| value == Double.NEGATIVE_INFINITY)
			value = Double.MAX_VALUE * 0.75d;

		// if (getId().getValue() == 939) {
		// System.out.println("in time: " + world.getTime()
		// + " setEnergy: " + value + " --- " + world.getAgent().getID() + " " +
		// invokeMethod);
		// }

		this.energy = value;
	}

	public float getPrevBurned() {
		return this.prevBurned;
	}

	public void setPrevBurned(float consumed) {
		this.prevBurned = consumed;
	}

	/**
	 * Get the water quantity of this building.
	 * 
	 * @return the water quantity of this building
	 */
	public int getWaterQuantity() {
		return this.waterQuantity;
	}

	/**
	 * Set the total water quantity of this building.
	 * 
	 * @param i
	 *            the new water quantity
	 */
	public void setWaterQuantity(int i) {
		if (i > this.waterQuantity) {
			this.lwTime = world.getTime();
			this.lwater = i - waterQuantity;
			this.wasEverWatered = true;
		}
		this.waterQuantity = i;
	}

	/**
	 * Get the increased quantity of water in last cycle.
	 * 
	 * @return the increased quantity of water in last cycle
	 */
	public int getLastWater() {
		return lwater;
	}

	/**
	 * Determines whether this building is watered by FB in last cycle.
	 * 
	 * @return true when this building is watered in last cycle. Otherwise,
	 *         false.
	 */
	public boolean getLastWatered() {
		return lwTime == world.getTime();
	}

	public void setWasEverWatered(boolean wasEverWatered) {
		this.wasEverWatered = wasEverWatered;
	}

	public float getFuel() {
		return this.fuel;
	}

	public void setFuel(float fuel) {
		this.fuel = fuel;
	}

	public boolean isInflammable() {
		return this.inflammable;
	}

	public void setInflammable(boolean inflammable) {
		this.inflammable = inflammable;
	}

	public int getStartTime() {
		return this.startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public boolean isEstimatedOnFire() {
		return ESTIMATED_BURNING.contains(getEstimatedFieryness());
	}

	/*
	 * ----------------------------------- temporary simulation part
	 * -----------------------------------------
	 */
	private float tempFuel;
	private double tempEnergy;
	private float tempPrevBurned;
	private boolean tempInflammable;

	public void initTempSimulatorValues() {
		tempFuel = fuel;
		tempEnergy = energy;
		tempPrevBurned = prevBurned;
		tempInflammable = inflammable;
	}

	public float getTempConsume(double bRate) {
		if (tempFuel == 0) {
			return 0;
		}
		float tf = (float) (getTempEstimatedTemperature() / 1000f);
		float lf = tempFuel / getInitialFuel();
		float f = (float) (tf * lf * bRate);
		if (f < 0.005f)
			f = 0.005f;
		return getInitialFuel() * f;
	}

	public float getTempFlue(double burnRate) {
		if (tempFuel == 0) {
			return 0;
		}
		float tf = (float) (getTempEstimatedTemperature() / 1000f);
		float lf = tempFuel / getInitialFuel();
		float f = (float) (tf * lf * burnRate);
		if (f < 0.005f)
			f = 0.005f;
		return getInitialFuel() * f;
	}

	public double getTempEstimatedTemperature() {
		double rv = tempEnergy / capacity;
		if (Double.isNaN(rv)) {
			new RuntimeException().printStackTrace();
		}
		if (rv == Double.NaN || rv == Double.POSITIVE_INFINITY
				|| rv == Double.NEGATIVE_INFINITY)
			rv = Double.MAX_VALUE * 0.75;
		return rv;
	}

	public int getTempEstimatedFieryness() {
		if (!isInflammable())
			return 0;
		if (getTempEstimatedTemperature() >= getIgnitionPoint()) {
			if (tempFuel >= getInitialFuel() * 0.66)
				return 1; // burning, slightly damaged
			if (tempFuel >= getInitialFuel() * 0.33)
				return 2; // burning, more damaged
			if (tempFuel > 0)
				return 3; // burning, severly damaged
		}
		if (tempFuel == getInitialFuel())
			if (wasEverWatered)
				return 4; // not burnt, but watered-damaged
			else
				return 0; // not burnt, no water damage
		if (tempFuel >= getInitialFuel() * 0.66)
			return 5; // extinguished, slightly damaged
		if (tempFuel >= getInitialFuel() * 0.33)
			return 6; // extinguished, more damaged
		if (tempFuel > 0)
			return 7; // extinguished, severely damaged
		return 8; // completely burnt down
	}

	public double getTempRadiationEnergy() { // /273.15
		double t = getTempEstimatedTemperature() + 293;
		double radEn = (t * t * t * t) * totalWallArea * RADIATION_COEFFICIENT
				* STEFAN_BOLTZMANN_CONSTANT;
		if (radEn == Double.NaN || radEn == Double.POSITIVE_INFINITY
				|| radEn == Double.NEGATIVE_INFINITY)
			radEn = Double.MAX_VALUE * 0.75;
		if (radEn > tempEnergy) {
			radEn = tempEnergy;
		}
		return radEn;
	}

	public float getTempFuel() {
		return this.tempFuel;
	}

	public void setTempFuel(float tempFuel) {
		this.tempFuel = tempFuel;
	}

	public double getTempEnergy() {
		return this.tempEnergy;
	}

	public void setTempEnergy(double tempEnergy) {
		this.tempEnergy = tempEnergy;
	}

	public float getTempPrevBurned() {
		return this.tempPrevBurned;
	}

	public void setTempPrevBurned(float tempPrevBurned) {
		this.tempPrevBurned = tempPrevBurned;
	}

	public boolean isTempFlammable() {
		return this.tempInflammable;
	}

	public void setTempFlammable(boolean tempFlammable) {
		this.tempInflammable = tempFlammable;
	}

	/*
	 * ------------------------------------------- other operations
	 * ------------------------------------------
	 */
	
	public boolean wasEverWatered() {
		return this.wasEverWatered;
	}
	public boolean isExtinguished() {
		return this.getEstimatedFieryness() >= 4
				&& this.getEstimatedFieryness() < 8;
	}

	public boolean isCollapsed() {
		return this.getEstimatedFieryness() == 8;
	}

	public EntityID getId() {
		return this.selfBuilding.getID();
	}

	public AdvancedWorldModel getWorldModel() {
		return this.world;
	}

	/** Used in fire zone. */
	public double getBuildingAreaTempValue() {
		double areaTempValue = this.selfBuilding.getTotalArea()
				* this.getEstimatedTemperature();
		return Util.gauss2mf(areaTempValue, 10000, 30000, 20000, 40000);
	}

	public Integer getZoneId() {
		return zoneId;
	}

	public void setZoneId(Integer zoneId) {
		this.zoneId = zoneId;
	}

	public boolean isBurned() {
		return getEstimatedFieryness() == 8;
	}

	public double getAdvantageRatio() {
		return this.advantageRatio;
	}

	public void setAdvantageRatio(double advantageRatio) {
		this.advantageRatio = advantageRatio;
	}

	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	@Override
	public String toString() {
		return "CSUBuilding: [" + this.selfBuilding + "]";
	}
	///oak, sos
	public boolean isBigBuilding() {
		int sum = 0;
		int num = 0;
		for(Building build : world.getEntitiesOfType(Building.class, StandardEntityURN.BUILDING)) {
			num++;
			sum += build.getGroundArea();
		}
		int avg = sum / num;
		return this.selfBuilding.getGroundArea() > Math.min(2300, 2*avg);
		
	}
	
	///oak
	public int getExtinguishableCycle() {
		int waterNeeded = FbUtilities.waterNeededToExtinguish(this);
		int cycle = waterNeeded / world.getConfig().maxPower;
		return cycle;
	}

	///oak 
	public void addPriority(int value) {
		this.priority += value;
	}
	public void addValue(double value) {
		this.BUILDING_VALUE += value;
	}
	
	public boolean isOutFire() {
		/**exclude the border*/
		if(world.getBorderBuildings().contains(world.getEntity(this.getId())))
			return false;
		
		if(! this.inflammable || ! this.isBurning())
			return false;
		/**list to set, avoid repeat*/
		Set<CSUBuilding> fireConnectedBuildings = new HashSet<>();
		for(CSUBuilding build : this.getConnectedBuildings()) {
			if(build.isBurning()) 
				fireConnectedBuildings.add(build);
		}
	
		int i = 0;
		int result0 = 0;
		int result = 0;
		int changed = 0;
		
		for(CSUBuilding build : fireConnectedBuildings) {
			if(i++ == 0)
				result0 = isOneVerticalSide(this, build);
			result = isOneVerticalSide(this, build);
			if(result != result0) {
				changed = 1;
				break;
			}
		}
		if(changed == 0)
			return true;
		
		i=0;
		result0 =0;
		result = 0; 
		changed = 0;
		
		for(double rad = -9/20 * Math.PI; rad <= 9/20 * Math.PI; rad += Math.PI/10) {
			for(CSUBuilding build : fireConnectedBuildings) {
				if(i++ == 0)
					result0 = underLine(this, build, rad);
				result = underLine(this, build, rad);
				if(result != result0) {
					changed = 1;
					break;
				}
			}
			if(changed == 0)
				return true;
		}
		return false;
	}
		
	public int isOneVerticalSide(CSUBuilding one, CSUBuilding two) {
		int x1 = one.selfBuilding.getLocation(world).first();
		int x2 = two.selfBuilding.getLocation(world).first();
		if(Math.abs(x1-x2) < 8000)
			return 0;
		else 
			return 1;
	}
	
	public int underLine(CSUBuilding one, CSUBuilding two, double k) {
		int x1 = one.selfBuilding.getLocation(world).first();
		int y1 = one.selfBuilding.getLocation(world).second();
		int x2 = two.selfBuilding.getLocation(world).first();
		int y2 = two.selfBuilding.getLocation(world).second();
		double result = k * (x2 - x1) + y1 - y2;
		if(result <= 0)
			return 0;
		else 
			return 1;
	}
		

	
}