package csu.agent.at.rescueTools;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.agent.at.cluster.Cluster;
import csu.agent.at.rescueTools.EntityFreezer.Freeze_Tab;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.util.Util;

public class SearchSpaceManager {
	
	/** the world model*/
	private AdvancedWorldModel world;
	
	/** The clusters of map generate by Kmeans-Plus-Plus */
	protected final ArrayList<Cluster> clusters ;
	
	/** The index of the cluster which the agent assigned to at the beginning*/
	protected int assignedClusterIndex;
	
	/** The index of the cluster which the agent currently locate*/
	protected int currentClusterIndex;
	
	/** the current cluster the agent belongs to */
	protected Area currentClusterCenter;
	
	/** The EntityFreezer*/
	protected EntityFreezer freezer;
	
	/** To store the buildings which are no need to visit for rescuing*/
	protected HashSet<EntityID> needlessBuildingForSearch ;
	
	/** To store the buildings whose buried human are no need to rescue*/
	protected HashSet<EntityID> needlessBuildingForRescue;
	
	/** To store the buildings which have been searched*/
	protected HashSet<EntityID> searchedBuilding;
	
	/** To store the buildings which are in the cluster the agent currently located in*/
	protected HashSet<EntityID> buildingForSearch ;
	
	/** To store the buildings which are not reachable*/
	protected HashSet<EntityID> notReachableBuilding;
	
	/** To store the building which is reachable*/
	protected HashSet<EntityID> availableBuildingForSeach;
	
	/** To store the number of collapse buildings in each cluster*/
	protected int[] numberOfCollapseBuildingInCluster;
	
	/** To store the index of cluster which the buildings belongs to*/
	protected HashMap<EntityID , Integer> clusterIndexOfBuilding;
	
	/** To store the minimum static cost of some entity*/
	protected HashMap<EntityID , Double> minStaticCost;
	
	/** True if <code>notReachableBuilding</code> has been cleared.
	 *  The ATs will not search the building more than twice which were blocked up in the entrances .*/
	protected boolean FlagOfClearNotReachableBuilding;
	
	/** The large cost for cost function*/
	private final double LARGE_COST = Double.MAX_VALUE * 0.5;
	
	/** The dangerous temperature upon which a building will soon be on fire*/
	private final int DANGEROUS_TEMPERATURE = 35;
	
	private static final int k = 7;
	
	public SearchSpaceManager(AdvancedWorldModel world , ArrayList<Cluster> clusters , 
			int assignedClusterIndex , EntityFreezer freezer){
		this.world = world;
		this.clusters = clusters;
		this.assignedClusterIndex = assignedClusterIndex;
		this.currentClusterIndex = assignedClusterIndex;
		this.currentClusterCenter = clusters.get(currentClusterIndex).getCenterEntity();
		this.freezer = freezer;
		buildingForSearch = new HashSet<EntityID>(clusters.get(currentClusterIndex).getCluster());
		buildingForSearch.removeAll(world.getEntitiesOfType(StandardEntityURN.REFUGE));
		searchedBuilding = new HashSet<EntityID>();
		needlessBuildingForSearch = new HashSet<EntityID>();
		needlessBuildingForRescue = new HashSet<EntityID>();
		numberOfCollapseBuildingInCluster = new int[clusters.size()];
		notReachableBuilding = new HashSet<EntityID>();
		availableBuildingForSeach = new HashSet<EntityID>();
		clusterIndexOfBuilding = new HashMap<EntityID , Integer>();
		for (Cluster cluster : clusters){
			int clusterIndex = clusters.indexOf(cluster);
			for (EntityID buildingID : cluster.getCluster()){
				clusterIndexOfBuilding.put(buildingID , clusterIndex);
			}
		}
		minStaticCost = new HashMap<EntityID , Double>();
		FlagOfClearNotReachableBuilding = false;
	}
	
	public void update(ChangeSet changed){
		if (currentClusterIndex == -1)
			return ;
		minStaticCost.clear();
		updateMinStaticCost();
		updateNeedlessBuilding(changed);
		updateNotReachableBuilding(changed);
		updateBuildingforSearch();
		if (buildingForSearch.isEmpty() || isTheClusterMainlyCollapsed())
			updateSearchSpace();
		updateAvailableBuildingForSeach();
		
	}
	
	protected void updateMinStaticCost(){
		if (world.me.getPosition(world) instanceof Building){
			Building building = (Building)world.me.getPosition(world);
			for (Road road : world.getEntrance().getEntrance(building)){
				if (building.getNeighbours().contains(road.getID()) && 
						(!world.getCsuRoad(road).isPassable() || !isEntranceReachable(road)))
					freezer.addNewfreezingStuff(Freeze_Tab.AREA , road.getID() , 10);
			}
		}
		Set<EntityID> freezingArea = freezer.getEntitiesWithTab(Freeze_Tab.AREA);
		if (freezingArea == null) return ;
		for (EntityID areaID : freezingArea){
//			System.out.println(world.me + "  Time : " + world.getTime() + "  add to minStaticCost : " + areaID);
			minStaticCost.put(areaID, LARGE_COST);
		}
	}
	
	protected void updateNeedlessBuilding(ChangeSet changed){
//		System.out.println(me() + " time : " + time + "  updateNeedlessBuilding");
		handleBurningBuildingAndNeighbours();
		handleExtinguishedBuildingAndNeighbours();
		handleCollapsedBuildingAndNeighbours();
		handleAgentPosition();
		handleBuriedHumanPosition();
		handleBuildingInChangedSet(changed);
		minStaticCost.remove(world.me.getPosition());
	}
	
	protected void handleBurningBuildingAndNeighbours(){
		for (Building burningBuilding : world.getBurningBuildings()){
			needlessBuildingForSearch.add(burningBuilding.getID());
			needlessBuildingForRescue.add(burningBuilding.getID());
			minStaticCost.put(burningBuilding.getID() , LARGE_COST);
			if (burningBuilding.isFierynessDefined() && (burningBuilding.getFierynessEnum().equals(Fieryness.BURNING) || 
					burningBuilding.getFierynessEnum().equals(Fieryness.INFERNO))){
				for (EntityID negID1 : world.getCsuBuilding(burningBuilding).getObservableAreas()){
					Area neg1 = (Area)world.getEntity(negID1);
					if (neg1 instanceof Building){
						needlessBuildingForSearch.add(negID1);
						needlessBuildingForRescue.add(negID1);
						minStaticCost.put(negID1 , LARGE_COST);
						if (burningBuilding.getFierynessEnum().equals(Fieryness.INFERNO)){
							for (EntityID negID2 : world.getCsuBuilding(neg1).getObservableAreas()){
								Area neg2 = (Area)world.getEntity(negID2);
								if (neg2 instanceof Building){
									Building buildingNeg = (Building)neg2;
									if (buildingNeg.isTemperatureDefined() && buildingNeg.getTemperature() < DANGEROUS_TEMPERATURE)
										continue;
									needlessBuildingForSearch.add(negID2);
									needlessBuildingForRescue.add(negID2);
									minStaticCost.put(negID2 , LARGE_COST);
								}
							}
						}

					}
				}
			}
		}
	}
	
	protected void handleExtinguishedBuildingAndNeighbours(){
		for (Building extinguishedBuilding : world.getExtinguishedBuildings()){
			minStaticCost.remove(extinguishedBuilding.getID());
			if (extinguishedBuilding.isFierynessDefined() && extinguishedBuilding.getFierynessEnum().equals(Fieryness.WATER_DAMAGE))
				continue;
			needlessBuildingForSearch.add(extinguishedBuilding.getID());
			needlessBuildingForRescue.add(extinguishedBuilding.getID());
			if (extinguishedBuilding.isFierynessDefined() && extinguishedBuilding.getFierynessEnum().equals(Fieryness.SEVERE_DAMAGE)){
				for (EntityID areaID : world.getCsuBuilding(extinguishedBuilding).getObservableAreas()){
					Area area = (Area)world.getEntity(areaID);
					if (area instanceof Building){
						needlessBuildingForSearch.add(areaID);
						needlessBuildingForRescue.add(areaID);
					}
				}
			}
		}
	}
	
	protected void handleCollapsedBuildingAndNeighbours(){
		for (Building collapsedBuilding : world.getCollapsedBuildings()){
			numberOfCollapseBuildingInCluster[clusterIndexOfBuilding.get(collapsedBuilding.getID())] ++ ;
			needlessBuildingForSearch.add(collapsedBuilding.getID());
			needlessBuildingForRescue.add(collapsedBuilding.getID());
			minStaticCost.remove(collapsedBuilding.getID());
			for (EntityID areaID : world.getCsuBuilding(collapsedBuilding).getObservableAreas()){
				Area area = (Area)world.getEntity(areaID);
				if (area instanceof Building){
					needlessBuildingForSearch.add(areaID);
					needlessBuildingForRescue.add(areaID);
				}
			}
		}
	}
	
	protected void handleAgentPosition(){
		for (StandardEntity entity : world.getEntitiesOfType(AgentConstants.PLATOONS)){
			 Human human = (Human)entity;
			 if (!human.isPositionDefined())
				 continue ; 
			 StandardEntity pos = human.getPosition(world);
			 if (pos instanceof Building )
				 searchedBuilding.add(pos.getID());
		 }
	}
	
	protected void handleBuriedHumanPosition(){
		for (EntityID humanID : world.getBuriedHumans().getBuriedHumanFromChangeSet()){
			Human human = (Human)world.getEntity(humanID);
			if (human.isPositionDefined())
				searchedBuilding.add(human.getPosition());
		}
		for (EntityID humanID : world.getBuriedHumans().getBuriedHumanFromVoiceOfCivilian()){
			Human human = (Human)world.getEntity(humanID);
			if (human.isPositionDefined())
				searchedBuilding.add(human.getPosition());
		}
		for (EntityID humanID : world.getBuriedHumans().getBuriedHumanFromCommunication().keySet()){
			Human human = (Human)world.getEntity(humanID);
			if (human.isPositionDefined())
				searchedBuilding.add(human.getPosition());
		}
	}
	
	protected void handleBuildingInChangedSet(ChangeSet changed){
		for (EntityID entityID : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(entityID);
			if (!(se instanceof Building))
				continue;
			Building building = (Building)se;
			if (world.me.getPosition(world) instanceof Road){
				Road road = (Road)world.me.getPosition(world);
				if (world.getEntrance().getEntrance(building).contains(road) &&
						road.getNeighbours().contains(building.getID())){
					if (road.getNeighbours().size() == 2){
						if (world.getDistance(world.me , building) < world.getConfig().viewDistance){
							needlessBuildingForSearch.add(building.getID());
							System.out.println(world.me + "   Time : " + world.getTime() + "   remove building #1#" + building);
						}
					}
					else if (road.getNeighbours().size() == 3){
						List<EntityID> neighbours = new ArrayList<EntityID>(road.getNeighbours());
						neighbours.remove(building.getID());
						Area area1 = (Area)world.getEntity(neighbours.get(0));
						Area area2 = (Area)world.getEntity(neighbours.get(1));
						if (area1 instanceof Road && area2 instanceof Road && area1.getNeighbours().contains(area2.getID())){
							if (world.getDistance(world.me , building) < world.getConfig().viewDistance){
								needlessBuildingForSearch.add(building.getID());
								System.out.println(world.me + "   Time : " + world.getTime() + "   remove building #2#" + building);
							}
						}
					}
				}
			}
			if (building.isTemperatureDefined() && building.getTemperature() >= DANGEROUS_TEMPERATURE)
				freezer.addNewfreezingStuff(Freeze_Tab.WARM_BUILDING , building.getID() , 2);
			if (building.getFierynessEnum().equals(Fieryness.UNBURNT) || building.getFierynessEnum().equals(Fieryness.WATER_DAMAGE)){
				needlessBuildingForSearch.remove(building.getID());
				needlessBuildingForRescue.remove(building.getID());
			}
		}
	}
	
	/**
	 * To update <code>buildingForSearch</code>
	 */
	protected void updateBuildingforSearch(){
//		System.out.println(me() + " time : " + time + "  updateBuildingforSearch");
		for (Iterator<EntityID> iterator = buildingForSearch.iterator() ; iterator.hasNext() ;){
			EntityID buildingID = iterator.next();
			if (needlessBuildingForSearch.contains(buildingID)){
				iterator.remove();
				continue;
			}
			if (searchedBuilding.contains(buildingID)){
				iterator.remove();
				continue;
			}
			Set<EntityID> warmBuildings = freezer.getEntitiesWithTab(Freeze_Tab.WARM_BUILDING);
			if (warmBuildings != null && warmBuildings.contains(buildingID)){
				iterator.remove();
				continue;
			}
		}
	}
	
	/**
	 * To update <code>notReachableBuilding</code>
	 */
	protected void updateNotReachableBuilding(ChangeSet changed){
		Area position = (Area)world.me.getPosition(world);
		for (EntityID id : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(id);
			if (se instanceof Road){
				Road road = (Road)se;
				if (!world.getEntrance().isEntrance(road))
					continue;
				if (!world.getCsuRoad(road).isPassable() || !isEntranceReachable(road)){
					boolean isImpassable = true;
					List<Building> buildings = world.getEntrance().getBuilding(road);
					Building oneBuilding = buildings.get(0);
					for (Road otherRoad : world.getEntrance().getEntrance(oneBuilding)){
						if (otherRoad.equals(road))
							continue ;
						if (changed.getChangedEntities().contains(otherRoad.getID()) && (world.getCsuRoad(otherRoad).isPassable() || isEntranceReachable(otherRoad))){
							isImpassable = false;
							break;
						}
					}
					if (isImpassable)
						for (Building building : buildings)
							notReachableBuilding.add(building.getID());
					else 
						for (Building building : buildings)
							notReachableBuilding.remove(building.getID());
				}
				else {
					for (Building building : world.getEntrance().getBuilding(road)){
						notReachableBuilding.remove(building.getID());
					}
				}
			}
		}
		if (position instanceof Building){
			Set<Road> entrances = world.getEntrance().getEntrance((Building)position);
			Road oneEntrance = entrances.iterator().next();
			//belong to this building's entrance.
			for (Building building : world.getEntrance().getBuilding(oneEntrance))
				notReachableBuilding.remove(building.getID());
		}
//		System.out.println(world.me + "  Time : " + world.getTime() + "  notReachable Building : " + notReachableBuilding);
	}

	/**
	 * To update <code>availableBuildingForSeachForSeaching</code>
	 */
	protected void updateAvailableBuildingForSeach(){
		availableBuildingForSeach.clear();
		//cyw only ATs.
//		availableBuildingForSeach.add(new EntityID(47685));
		//cywEnd
		
		for (EntityID buildingID : buildingForSearch){
			if (!notReachableBuilding.contains(buildingID))
				availableBuildingForSeach.add(buildingID);		
		}
		//?????
		if (availableBuildingForSeach.isEmpty() && !notReachableBuilding.isEmpty() && !FlagOfClearNotReachableBuilding){
			for (EntityID buildingID : notReachableBuilding){
				if (buildingForSearch.contains(buildingID))
					availableBuildingForSeach.add(buildingID);
			}
			notReachableBuilding.clear();
			FlagOfClearNotReachableBuilding = true;
		}
	}
	
	protected void updateSearchSpace(){
		
		while (buildingForSearch.isEmpty()){
			world.getRemainCluster().remove(currentClusterIndex);
			currentClusterIndex = getNewSearchCluster();
			if (currentClusterIndex == -1)
				return ;
			buildingForSearch.addAll(clusters.get(currentClusterIndex).getCluster());
			for (StandardEntity se : world.getEntitiesOfType(StandardEntityURN.REFUGE))
				buildingForSearch.remove(se.getID());
			updateBuildingforSearch();
		}
		currentClusterCenter = clusters.get(currentClusterIndex).getCenterEntity();
		FlagOfClearNotReachableBuilding = false;
	}
	
	protected int getNewSearchCluster(){
		Set<Cluster> remainClusters = world.getRemainCluster().getRemainClusters();
//		System.out.println(world.me + "  Time : " + world.getTime() + "  remainClusters.size " + remainClusters.size());
		int minDistance = Integer.MAX_VALUE;
		int clusterIndex = -1;
		for (Cluster cluster : remainClusters){
//			int distance =  world.getDistance(world.me , cluster.getCenterEntity());
			int distance =  world.getDistance(currentClusterCenter, cluster.getCenterEntity());
			if (distance < minDistance){
				minDistance = distance;
				clusterIndex = clusters.indexOf(cluster);
			}
		}
//		System.out.println(world.me + "  Time : " + world.getTime() + "   new cluster index : " + clusterIndex);
		return clusterIndex;
	}
	
	/**
	 * To return the set : needlessBuildingForRescue
	 * @return
	 */
	public HashSet<EntityID> getNeedlessBuildingForRescue(){
		HashSet<EntityID> result = new HashSet<EntityID>();
		result.addAll(needlessBuildingForRescue);
		result.addAll(notReachableBuilding);
		return result;
	}
	
	/**
	 * To check if the buildingForSearch is empty
	 * @return
	 */
	public boolean isBuildingForSearchEmpty(){
		return availableBuildingForSeach.isEmpty();
	}
	
	/**
	 * To get the destinations for searching
	 * @return
	 */
	public ArrayList<StandardEntity> getSearchingDestination(){
		ArrayList<StandardEntity> destinations = new ArrayList<StandardEntity>();
		for (EntityID buildingID : availableBuildingForSeach){
			StandardEntity se = world.getEntity(buildingID);
			destinations.add(se);
		}
		return destinations;
	}
	
	/**
	 * To get the map : minStaticCost
	 * @return
	 */
	public HashMap<EntityID , Double> getMinStaticCost(){
		return minStaticCost;
	}
	
	protected boolean isEntranceReachable(Road entrance){
		double buildingEntranceLength = 0.0;
		double maxUnpassableEdgeLength = Double.MIN_VALUE;
		double length;
		
		Edge buildingEntrance = null;
		
		for (Edge next : entrance.getEdges()) {
			if (next.isPassable()) {
				StandardEntity entity = world.getEntity(next.getNeighbour());
				if (entity instanceof Building) {
					buildingEntranceLength = GeometryTools2D.getDistance(next.getStart(), next.getEnd());
					buildingEntrance = next;
				}
			} else {
				length = GeometryTools2D.getDistance(next.getStart(), next.getEnd());
				if (length > maxUnpassableEdgeLength) {
					maxUnpassableEdgeLength = length;
				}
			}
		}
		
		if (buildingEntrance == null)
			return true;
		double rad = buildingEntranceLength + maxUnpassableEdgeLength;
		java.awt.geom.Area entranceArea = entranceArea(buildingEntrance.getLine(), rad);
		
		Set<EntityID> blockadeIds = new HashSet<>();
		
		for (EntityID next : entrance.getNeighbours()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof Road) {
				Road road = (Road) entity;
				if (road.isBlockadesDefined()) 
					blockadeIds.addAll(road.getBlockades());
			}
		}
		
		for (EntityID next : blockadeIds) {
			Blockade blockade = (Blockade)world.getEntity(next);
			if (blockade == null)
				continue;
			Polygon po = Util.getPolygon(blockade.getApexes());
			java.awt.geom.Area blocArea = new java.awt.geom.Area(po);
			blocArea.intersect(entranceArea);
			if (!blocArea.getPathIterator(null).isDone()){
//				System.out.println(world.me + "   Time : " + world.getTime() + "   unreachable entrance : " + entrance);
				return false;
			}
		}
		return true;
	}
	
	private java.awt.geom.Area entranceArea(Line2D line, double rad) {
		double theta = Math.atan2(line.getEndPoint().getY() - line.getOrigin().getY(), 
				line.getEndPoint().getX() - line.getOrigin().getX());
		theta = theta - Math.PI / 2;
		while (theta > Math.PI || theta < -Math.PI) {
			if (theta > Math.PI)
				theta -= 2 * Math.PI;
			else
				theta += 2 * Math.PI;
		}
		int x = (int)(rad * Math.cos(theta)), y = (int)(rad * Math.sin(theta));
		
		Polygon polygon = new Polygon();
		polygon.addPoint((int)(line.getOrigin().getX() + x), (int)(line.getOrigin().getY() + y));
		polygon.addPoint((int)(line.getEndPoint().getX() + x), (int)(line.getEndPoint().getY() + y));
		polygon.addPoint((int)(line.getEndPoint().getX() - x), (int)(line.getEndPoint().getY() - y));
		polygon.addPoint((int)(line.getOrigin().getX() - x), (int)(line.getOrigin().getY() - y));
		
		return new java.awt.geom.Area(polygon);
	}
	
	/**
	 * True if all the buildings in the assigned cluster have been searched 
	 * @return
	 */
	public boolean isAssignedClusterNearlyClear(){
		if(assignedClusterIndex == currentClusterIndex){
			if (buildingForSearch.size() <= 0.3 * clusters.get(currentClusterIndex).getCluster().size())
				return true;
			return false;
		}
		return true;
	}
	
	/**
	 * To check if the buildings in the current cluster is mainly collapsed
	 * @return
	 */
	public boolean isTheClusterMainlyCollapsed(){
		if (numberOfCollapseBuildingInCluster[currentClusterIndex] * 1.0 / 
				clusters.get(currentClusterIndex).getCluster().size()  > 4.0 / k)
			return true;
		return false;
	}
	
//	protected int getNewClusterForMe(int[] numberOfBuriedHumanInCluster){
//	ArrayList<Cluster> remainClusters = new ArrayList<Cluster>(world.getRemainCluster().getRemainClusters());
//	for (Iterator<Cluster> it = remainClusters.iterator() ; it.hasNext() ;){
//		Cluster cluster = it.next();
//		int index = clusters.indexOf(cluster);
//		if ((numberOfCollapseBuildingInCluster[index] * 1.0) / (cluster.getCluster().size() * 1.0) > 1.0/k){
//			it.remove();
//		}
//	}
//	int maxBuriedHuman = 0;
//	int maxDistance = 0;
//	for (Cluster cluster : remainClusters){
//		int index = clusters.indexOf(cluster);
//		int distance = world.getDistance(me , cluster.getCenterEntity());
//		if (distance > maxDistance) 
//			maxDistance = distance;
//		if (numberOfBuriedHumanInCluster[index] > maxBuriedHuman)
//			maxBuriedHuman = numberOfBuriedHumanInCluster[index];
//	}
//	Collections.sort(remainClusters , new ClusterSorter(maxBuriedHuman , maxDistance , 
//			numberOfBuriedHumanInCluster));
//	if (remainClusters.isEmpty()) {
//		return currentClusterIndex;
//	}
//	return clusters.indexOf(remainClusters.get(0));
//}
	
//	/**
//	 * The sorter to sort the clusters
//	 * @author nale
//	 *
//	 */
//	private class ClusterSorter implements Comparator<Cluster> {
//
//		/** The maximum number of the buried human in clusters*/
//		private int maxBuriedHuman ;
//		
//		/** The maximum distance between me and clusters*/
//		private int maxDistance;
//		
//		/** To store the number of buried human in each cluster*/
//		private int[] numberOfBuriedHumanInCluster;
//		
//		public ClusterSorter(int maxBuriedHuman , int maxDistance ,
//				int[] numberOfBuriedHumanInCluster){
//			this.maxBuriedHuman = maxBuriedHuman;
//			this.maxDistance = maxDistance;
//			this.numberOfBuriedHumanInCluster = numberOfBuriedHumanInCluster;
//		}
//		
//		@Override
//		public int compare(Cluster cluster1, Cluster cluster2) {
//			int index1 = clusters.indexOf(cluster1);
//			int index2 = clusters.indexOf(cluster2);
//			int distance1 = world.getDistance(me , cluster1.getCenterEntity());
//			int distance2 = world.getDistance(me , cluster2.getCenterEntity());
//			int value1 = (int)(numberOfBuriedHumanInCluster[index1] - 
//					maxBuriedHuman * (distance1 * 1.0 / maxDistance) + 
//					k * maxBuriedHuman * (numberOfCollapseBuildingInCluster[index1] * 1.0/ cluster1.getCluster().size()));
//			int value2 = (int)(numberOfBuriedHumanInCluster[index2] - 
//					maxBuriedHuman * (distance2 * 1.0 / maxDistance) + 
//					k * maxBuriedHuman * (numberOfCollapseBuildingInCluster[index2] * 1.0/ cluster2.getCluster().size()));
//			if (value1 > value2)
//				return -1;
//			else if (value1 < value2)
//				return 1;
//			else
//				return 0;
//		}
//	}
	
}
