package csu.standard.unused;
//package csu.standard;
//
//import java.util.Comparator;
//import rescuecore2.standard.entities.Building;
//import csu.model.AdvancedWorldModel;
//
//
//public class BurningBuildingComparator implements Comparator<Building> {
//
//	public AdvancedWorldModel world;
//	public int time;
//
//	public BurningBuildingComparator(AdvancedWorldModel model, int timeStep) {
//		world = model;
//		time = timeStep;
//	}
//
//	public int compare(Building o1, Building o2) {
//		double r1, r2;
//		BuildingInfo info1, info2;
//		int d;
//
//		d = 0;
//
//		if (d == 0) {
//
//			info1 = world.getBuildingInfo(o1.getID());
//			info2 = world.getBuildingInfo(o2.getID());
//			r1 = info1.getHeatingSpeed(time);
//			r2 = info2.getHeatingSpeed(time);
//			
//			d = (int) (r2 - r1);
//		}
//		return d;
//	}
//
//}
