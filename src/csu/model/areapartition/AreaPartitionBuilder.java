//package csu.model.areapartition;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//import csu.model.AdvancedWorldModel;
//
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityURN;
//import rescuecore2.worldmodel.EntityID;
//
//public class AreaPartitionBuilder {
//
//
//
//	private final ArrayList<PartedArea> areaPartitionsResults = new ArrayList<PartedArea>();
//
//	public AreaPartitionBuilder(AdvancedWorldModel world) {
//		ArrayList<HashSet<EntityID>> separeteds = separeteArea(world);
//		for (int i = 0; i < separeteds.size(); i++) {
//			PartedArea part = new PartedArea(world, i, separeteds.get(i));//部分
//			System.out.println("part"+part);
//			areaPartitionsResults.add(part);//区域分区结果
//		}
//	}
//
//	public ArrayList<PartedArea> getAreaPartitionResult() {
//		return areaPartitionsResults;
//	}
//
//	/**
//	 * Buildingを分为区域的。
//	 * 
//	 * @return 区域要素集合排列
//	 */
//	private ArrayList<HashSet<EntityID>> separeteArea(AdvancedWorldModel world) {
//		ArrayList<HashSet<EntityID>> result = new ArrayList<HashSet<EntityID>>();
//		ArrayList<StandardEntity> searchQueue = new ArrayList<StandardEntity>();
//		HashSet<StandardEntity> remains = new HashSet<StandardEntity>(world.getEntitiesOfType(StandardEntityURN.BUILDING));
//		HashMap<StandardEntity, Set<StandardEntity>> neighbor = new HashMap<StandardEntity, Set<StandardEntity>>();
//		for (StandardEntity se : remains) {
//			neighbor.put(se, world.getNeighbours(se, 5));
//		}
//		while (!remains.isEmpty()) {
//			// AreaPartitioner的1単位
//			HashSet<EntityID> entityIDs = new HashSet<EntityID>();
//			while (!searchQueue.isEmpty()) {
//				StandardEntity se = searchQueue.get(0);
//				if (se instanceof Building) {
//					entityIDs.add(se.getID());
//				}
//				if (neighbor.get(se) != null) {
//					searchQueue.addAll(neighbor.get(se));
//				}
//				for (Iterator<StandardEntity> iterator = searchQueue.iterator(); iterator
//				.hasNext();) {
//					StandardEntity remover = iterator.next();
//					if (entityIDs.contains(remover.getID())
//							|| !(remains.contains(remover))
//							|| !(remover instanceof Building)) {
//						iterator.remove();
//					}
//				}
//			}
//			if (!entityIDs.isEmpty()) {
//				result.add(new HashSet<EntityID>(entityIDs));
//			}
//			for (Iterator<StandardEntity> iterator = remains.iterator(); iterator.hasNext();) {
//				if (entityIDs.contains(iterator.next().getID())) {
//					iterator.remove();
//				}
//			}
//			if (!remains.iterator().hasNext()) {
//				break;
//			}
//			searchQueue.add(remains.iterator().next());
//		}
//		return result;
//	}
//
//
//
//}
