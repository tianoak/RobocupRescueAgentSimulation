package csu.standard;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Refuge;
import csu.model.AdvancedWorldModel;

/**
 * This class handles the infos of a building.
 * 
 * @author appreciation-csu
 *
 */
public class BuildingInfo {
	public static final int WARM_TEMPERATURE = 25;
	private static final int MAX_DIST_NEAR_BUILDINGS = 7000;

	/** Target Building of this BuildingInfo.*/
	private Building building;
	/** The condition of target Building.*/
	private BUILDING_CONDITION condition;
	/** The fireyness of target Building.*/
	private int fireyness;
	/** The temperature of target Building.*/
	private int temperature;
	/** Generate or update time of this BuildingInfo.*/
	public int time;
	
	/** The neighbored buildings of this building.*/
	private List<Building> neighbours;
	
	public static enum BUILDING_CONDITION {
		/** Building not on fire, but its temperature is greater than a certain value which is 25.*/
		BUILDING_WARM,
		/** Building with Fireyness HEATING, BURNING or INFERNO*/
		BUILDING_ON_FIRE,
		/** Building with Fireyness WATER_DAMAGE, MINOR_DAMAGE, MODERATE_DAMAGE or SEVERE_DAMAGE.*/
		BUILDING_EXTINGUISHED,
		/** Building with Fireyness BURNT_OUT are collapsed.*/
		BUILDING_COLLAPSED,
		/** BUilding not o fire and with temperature less than a certain value which is 25.*/
		BUILDING_UNBURNT
	};
	
	// constructor
	public BuildingInfo(Building building, BUILDING_CONDITION condition, int time) {
		this.building = building;
		
		if (building.isFierynessDefined())
			this.fireyness = building.getFieryness();
		else
			this.fireyness = 0;
		
		if (building.isTemperatureDefined())
			this.temperature = building.getTemperature();
		else 
			this.temperature = 0;
		
		if (condition != null)
			this.condition = condition;
		else
			this.condition = predicateBuildingCondition(building);
		
		this.time = time;
	}
	
	/** Update the properties of target Building.*/
	public void updateProperties(BUILDING_CONDITION condition, int time){
		if (building.isTemperatureDefined())
			this.temperature = building.getTemperature();
		else 
			this.temperature = 0;
		
		if (building.isFierynessDefined())
			this.fireyness = building.getFieryness();
		else
			this.fireyness = 0;
		
		if (condition != null)
			this.condition = condition;
		else
			this.condition = predicateBuildingCondition(building);

		this.time = time;
	}
	
	/**
	 * This static method used to predicate the condition of the given Building
	 * according to the Fieryness of this Building. Those condition are:
	 * 
	 * <pre>
	 * 		BUILDING_WARM --- not on fire, but with temperature > 25
	 * 		BUILDING_ON_FIRE --- on fire
	 * 	BUILDING_EXTINGUISHED --- extinguished
	 * 		BUILDING_COLLAPSED --- burnt out
	 * 		BUILDING_UNBURNT --- not on fire and with temperature <= 25
	 * </pre>
	 * 
	 * @param target
	 *            the target building
	 * @return the <b>BUILDING_CONDITION</b> of target Building
	 */
	private static BUILDING_CONDITION predicateBuildingCondition(Building target){
		switch (target.getFierynessEnum()) {
		case UNBURNT:
			if (target.getTemperature() <= WARM_TEMPERATURE)
				return BUILDING_CONDITION.BUILDING_UNBURNT;
			else ///gas station
				if (!(target instanceof Refuge)
						&& !(target instanceof PoliceOffice)
						&& !(target instanceof AmbulanceCentre)
						&& !(target instanceof FireStation))
					return BUILDING_CONDITION.BUILDING_WARM;
			break;
		case HEATING:
		case BURNING:
		case INFERNO:
			return BUILDING_CONDITION.BUILDING_ON_FIRE;
		case WATER_DAMAGE:
		case MINOR_DAMAGE:
		case MODERATE_DAMAGE:
		case SEVERE_DAMAGE:
			if (target.getTemperature() <= WARM_TEMPERATURE)
				return BUILDING_CONDITION.BUILDING_EXTINGUISHED;
			else 
				if (!(target instanceof Refuge)
						&& !(target instanceof PoliceOffice)
						&& !(target instanceof AmbulanceCentre)
						&& !(target instanceof FireStation))
					return BUILDING_CONDITION.BUILDING_WARM;
			break;
		case BURNT_OUT:
			return BUILDING_CONDITION.BUILDING_COLLAPSED;
		default:
			return null;
		}
		return null;
	}

	/**
	 * If a <b>Building</b> is on fire, then the buildings arounds this building may catch a fire too.
	 * So those building are in dangerous.
	 * <p>
	 * This method get all dangerous building in a certain range around a fired building. Here the radius
	 * of this range is 7m which is defined by <b>MAX_DIST_NEAR_BUILDINGS</b>
	 * <p>
	 * Currently, this method was not been used.
	 * 
	 * @return a list of dangerous buildings
	 */
	public ArrayList<Building> inDangerNeighbors() {
		// 
		if (building.getFieryness() == 4 || building.getFieryness() == 0
				|| !building.isFierynessDefined()) {
			return null;
		}
		
		ArrayList<Building> ret = new ArrayList<Building>();

		if (neighbours == null) {
			System.err.println("The Building Have No Neighbours");
			return null;
		}

		for (Building b : neighbours) {
			if (b.getFieryness() == 0) {  ///all gas stations are dangerous
				if (!(b instanceof Refuge) && !(b instanceof AmbulanceCentre)
						&& !(b instanceof PoliceOffice)
						&& !(b instanceof FireStation)) {
					ret.add(b);
				}
			} else if (b.getFieryness() >= 4 && b.getFieryness() <= 7) {
				ret.add(b);
			}
		}
		return ret;
	}

	/**
	 * Get all neighbored buildings of this building within a given range.
	 * 
	 * @param world &nbsp;the world model
	 * @return all neighbored buildings
	 */
	public List<Building> getNeighbours(AdvancedWorldModel world) {
		if (neighbours == null) {
			neighbours = world.findNearBuildings(building, MAX_DIST_NEAR_BUILDINGS);
			
			return neighbours;
		}
		return neighbours;
	}

	/**
	 * Determines whether a specified building is the neighbour of this building. 
	 * 
	 * @param building &nbsp;the building needs determines
	 * @param world &nbsp;the world model
	 * @return true when the specified building is this building's neighbour, false otherwise
	 */
	public boolean isNeighbourBuilding(Building building, AdvancedWorldModel world) {
		if (neighbours == null) {
			neighbours = getNeighbours(world);
		}
		if (!neighbours.isEmpty()) {
			for (Building neighbour : neighbours) {
				if (building.getID().getValue() == neighbour.getID().getValue()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Determines whether a building is on fire.
	 * 
	 * @return true when this building is on fire, false otherwise
	 */
	public boolean isBurning() {
		// if the Fieryness of this building is undefined, we assume that this buildig is not on fire
		if (!building.isFierynessDefined())
			return false;
		return (1 <= building.getFieryness() && building.getFieryness() <= 3);
	}
	
	/** Get current building. */
	public Building getBuilding() {
		return building;
	}
	/** Get the condition of this Building.*/
	public BUILDING_CONDITION getBuildingCondition(){
		return this.condition;
	}
	public int getTemperature(){
		return this.temperature;
	}
	public int getFireyness(){
		return this.fireyness;
	}

	/*
	 *                         ------------------ Handle by Appreciation --- csu
	 * 
	 * private static final int MIN_HEATING_VALUE = 0;
	 * 
	 * private int latestTemperature; 
	 * private int temperatureUpdateTime; 
	 * if(building.isTemperatureDefined()) { 
	 *     latestTemperature = building.getTemperature(); 
	 * } else { 
	 *     latestTemperature = 0; 
	 * } 
	 * 
	 * public int getLatestTemperature() { 
	 *     return latestTemperature; 
	 * }
	 * 
	 * public void setLatestTemperature(int latestTemperature) {
	 *     this.latestTemperature = latestTemperature; 
	 * }
	 * 
	 * ---Determines whether this Building is heating. 
	 * public boolean isHeating() { 
	 *     if (building.isFierynessDefined() && building.getFierynessEnum() != Fieryness.BURNT_OUT) { 
	 *         int d; 
	 *         boolean heating;
	 * 
	 *         d = getTemperatureDifference(); 
	 *         heating = (d > MIN_HEATING_VALUE); 
	 *         return heating; 
	 *     } else { 
	 *         return false; 
	 *     } 
	 * }
	 * 
	 * --- Get the heating speed of this building. public double
	 * getHeatingSpeed(int timeStep) { 
	 *     int timeDifference;
	 *     timeDifference = timeStep - temperatureUpdateTime; 
	 *     if (timeDifference > 0) { 
	 *         int temperatureDifference; 
	 *         double r;
	 * 
	 *         temperatureDifference = getCurrentTemperature() - latestTemperature; 
	 *         r = (double) temperatureDifference / timeDifference; return r; 
	 *     } else {
	 *         return 0; 
	 *     } 
	 * }
	 * 
	 * 
	 * --- Get current temperature of this building. public int
	 * getCurrentTemperature() { 
	 *     if (building.isTemperatureDefined()) { 
	 *         return building.getTemperature(); 
	 *     } else { 
	 *         return 0; 
	 *     } 
	 * }
	 * 
	 * public int getTemperatureDifference() { 
	 *     int temperature, difference;
	 * 
	 *     temperature = getCurrentTemperature(); 
	 *     difference = temperature - latestTemperature; 
	 *     return difference; 
	 * }
	 * 
	 * public void updateTemperature(int timeStep) { 
	 *     if (building.isTemperatureDefined()) { 
	 *         latestTemperature = building.getTemperature(); 
	 *         temperatureUpdateTime = timeStep; 
	 *     } 
	 * }
	 */
}
