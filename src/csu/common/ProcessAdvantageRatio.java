//package csu.common;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityURN;
//import rescuecore2.worldmodel.EntityID;
//import csu.model.AdvancedWorldModel;
//import csu.model.object.CSUBuilding;
//
//public class ProcessAdvantageRatio {
//
//	private AdvancedWorldModel world;
//	private List<EntityID> refugeIdList;
//	
//	
//	public ProcessAdvantageRatio(AdvancedWorldModel world) {
//		this.world = world;
//		this.refugeIdList = new ArrayList<>();
//	}
//	
//	public void process() {
//		getRefugeList();
//		for (CSUBuilding csuBuilding : world.getCsuBuildings()) {
//			double distanceToNearestRefuge = Double.MAX_VALUE;
//			for (EntityID refugeId : refugeIdList) {
//				double distance = world.getDistance(csuBuilding.getId(), refugeId);
//				if (distance < distanceToNearestRefuge)
//					distanceToNearestRefuge = distance;
//			}
//			csuBuilding.setAdvantageRatio(distanceToNearestRefuge);
//		}
//	}
//	
//	private void getRefugeList() {
//		for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.REFUGE)) {
//			refugeIdList.add(next.getID());
//		}
//	}
//}
