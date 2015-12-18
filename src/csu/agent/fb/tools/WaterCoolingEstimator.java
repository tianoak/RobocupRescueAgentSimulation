package csu.agent.fb.tools;

import csu.model.object.CSUBuilding;

public class WaterCoolingEstimator {
	public static double WATER_COEFFICIENT = 20f;
	public static int FLOOR_HEIGHT = 3;

	protected static double getBuildingEnergy(int buildingCode, int groundArea,
			int floors, double temperature) {
		
		return temperature * getBuildingCapacity(buildingCode, groundArea, floors);
	}

	protected static double getBuildingCapacity(int buildingCode, int groundArea, int floors) {
		double thermoCapacity;
		switch (buildingCode) {
		case 0:
			// wooden
			thermoCapacity = 1.1;
			break;
		case 1:
			// steel
			thermoCapacity = 1.0;
			break;
		default:
			// concrete
			thermoCapacity = 1.5;
			break;
		}
		return thermoCapacity * groundArea * floors * FLOOR_HEIGHT;
	}

	public static int getWaterNeeded(int groundArea, int floors,
			int buildingCode, double temperature, double finalTemperature) {
		int waterNeeded = 0;
		double currentTemperature = temperature;
		int step = 500;
		while (true) {
			currentTemperature = waterCooling(groundArea, floors, buildingCode, currentTemperature, step);
			waterNeeded += step;
			if (currentTemperature <= finalTemperature) {
				break;
			}
		}
		return waterNeeded;
	}

	private static double waterCooling(int groundArea, int floors,
			int buildingCode, double temperature, int water) {
		if (water > 0) {
			double effect = water * WATER_COEFFICIENT;
			return (getBuildingEnergy(buildingCode, groundArea, floors, temperature) - effect)
					/ getBuildingCapacity(buildingCode, groundArea, floors);
		} else
			throw new RuntimeException("WTF water=" + water);
	}

	public static int waterNeededToExtinguish(CSUBuilding building) {
		return getWaterNeeded(building.getSelfBuilding().getGroundArea(),
				building.getSelfBuilding().getFloors(), building.getSelfBuilding().getBuildingCode(),
				building.getEstimatedTemperature(), 20); ///temperature
	}
}
