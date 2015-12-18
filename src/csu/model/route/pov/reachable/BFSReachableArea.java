//package csu.model.route.pov.reachable;
//
//import java.util.ArrayDeque;
//import java.util.Collection;
//import java.util.Deque;
//import java.util.HashSet;
//import java.util.Set;
//
//import csu.model.AdvancedWorldModel;
//import csu.model.AgentConstants;
//import csu.model.route.pov.graph.AreaNode;
//import csu.model.route.pov.graph.EdgeNode;
//import csu.model.route.pov.graph.PassableDictionary;
//import csu.model.route.pov.graph.PointNode;
//import csu.model.route.pov.graph.PassableDictionary.PassableLevel;
//
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.worldmodel.EntityID;
//
///**
// * Unused. Note by appreciation-csu. 
// * 
// * Date: June 11, 2014  Time: 0:24am
// */
//public class BFSReachableArea {
//	/**
//	 * Reachabe areas. Here we think areas with {@link PassableLevel#UNKNOWN}
//	 * are reachable, too.
//	 */
//	private final Set<EntityID> reachableArea;
//	
//	/**
//	 * Areas with {@link PassableLevel#SURE_PASSABLE},
//	 * {@link PassableLevel#COMMUNICATION_PASSABLE} and
//	 * {@link PassableLevel#LOGICAL_PASSABLE} are sure passable areas.
//	 */
//	private final Set<EntityID> sureReachableArea;
//	private final int limit;
//	
//	public BFSReachableArea(AdvancedWorldModel world) {
//		limit = (int)world.getConfig().thinkTime / 2;
//		Collection<StandardEntity> areas = world.getEntitiesOfType(AgentConstants.AREAS);
//		reachableArea = new HashSet<EntityID>(areas.size());
//		sureReachableArea = new HashSet<EntityID>(areas.size());
//	}
//	
//	/**
//	 * Update the reachability.
//	 * 
//	 * @param pos
//	 *            the current position of this Agent
//	 * @param world
//	 *            the world model
//	 */
//	public void update(final AreaNode pos, final AdvancedWorldModel world) {
//		updateSureReachable(pos, world);
//		updateReachable(pos, world);
//	}
//	
//	/**
//	 * Update the sure reachable areas.
//	 * 
//	 * @param pos
//	 *            the current position of this Agent
//	 * @param world
//	 *            the world model
//	 * @throws IllegalStateException
//	 */
//	private void updateSureReachable(AreaNode pos, AdvancedWorldModel world) throws IllegalStateException {
//		sureReachableArea.clear();
//		Deque<PointNode> que = new ArrayDeque<PointNode>();
//		Set<PointNode> visited = new HashSet<PointNode>();
//		PassableDictionary passableDic = world.getRouter().getPassableDic();
//		que.push(pos);
//		final long t = System.currentTimeMillis();
//		do {
//			PointNode currentNode = que.pop();
//			visited.add(currentNode);
//			if (currentNode instanceof AreaNode) {
//				AreaNode areaNode = (AreaNode) currentNode;
//				sureReachableArea.add(areaNode.getBelong().getID());
//				for (EdgeNode edgeNode : areaNode.getNeighbours()) {
//					if (visited.contains(edgeNode)) 
//						continue;
//					if (passableDic.getPassableLevel(areaNode, edgeNode).ordinal()
//							< PassableLevel.UNPASSABLE.ordinal()) {
//						que.push(edgeNode);
//					}
//				}
//			} else {
//				EdgeNode edgeNode = (EdgeNode) currentNode;
//				for (AreaNode areaNode : edgeNode.getNeighbours()) {
//					if (visited.contains(areaNode)) 
//						continue;
//					if (passableDic.getPassableLevel(areaNode, edgeNode).ordinal()
//							< PassableLevel.UNPASSABLE.ordinal()) {
//						que.push(areaNode);
//					}
//				}
//			}
//			if (System.currentTimeMillis() - t > limit) {
//				throw new IllegalStateException();
//			}
//		} while (!que.isEmpty());
//	}
//
//	/**
//	 * Update the reachable areas.
//	 * 
//	 * @param pos
//	 *            the current position of this Agent
//	 * @param world
//	 *            the world model
//	 * @throws IllegalStateException
//	 */
//	private void updateReachable(final AreaNode pos, AdvancedWorldModel world) throws IllegalStateException {
//		reachableArea.clear();
//		Deque<PointNode> que = new ArrayDeque<PointNode>();
//		Set<PointNode> visited = new HashSet<PointNode>();
//		que.push(pos);
//		PassableDictionary passableDic = world.getRouter().getPassableDic();
//		final long t = System.currentTimeMillis();
//		do {
//			PointNode currentNode = que.pop();
//			visited.add(currentNode);
//			if (currentNode instanceof AreaNode) {
//				AreaNode areaNode = (AreaNode) currentNode;
//				reachableArea.add(areaNode.getBelong().getID());
//				for (EdgeNode edgeNode : areaNode.getNeighbours()) {
//					if (visited.contains(edgeNode)) continue;
//					if (passableDic.getPassableLevel(areaNode, edgeNode)
//							!= PassableLevel.UNPASSABLE) {
//						que.push(edgeNode);
//					}
//				}
//			} else {
//				EdgeNode edgeNode = (EdgeNode) currentNode;
//				for (AreaNode areaNode : edgeNode.getNeighbours()) {
//					if (visited.contains(areaNode)) continue;
//					if (passableDic.getPassableLevel(areaNode, edgeNode) != PassableLevel.UNPASSABLE) {
//						que.push(areaNode);
//					}
//				}
//			}
//			if (System.currentTimeMillis() - t > limit) {
//				throw new IllegalStateException();
//			}
//		} while (!que.isEmpty());
//	}
//
//	public boolean isSureReachable(EntityID id) {
//		return sureReachableArea.contains(id);
//	}
//	
//	public boolean isReachable(EntityID id) {
//		return reachableArea.contains(id);
//	}
//}
