package csu.common.clustering;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import math.geom2d.polygon.SimplePolygon2D;

import javolution.util.FastMap;
import javolution.util.FastSet;

import rescuecore2.misc.Pair;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.Viewer.layers.CSU_ConvexHullLayer;
import csu.Viewer.layers.CSU_PartitionLayer;
import csu.agent.fb.FireBrigadeWorld;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.standard.Ruler;

public class FireClusterManager extends ClusterManager<FireCluster>{
	private static final double MEAN_VELOCITY_OF_MOVING = 31445.392;
	
	// TODO
	private Map<String, Map<Integer, Integer>> timeTemperature = new LazyMap<String, Map<Integer, Integer>>() {

		@Override
		public Map<Integer, Integer> createValue() {
			return new TreeMap<>();
		}
	};

	private Map<String, Map<Integer, Integer>> timeFieryness = new LazyMap<String, Map<Integer, Integer>>() {

		@Override
		public Map<Integer, Integer> createValue() {
			return new TreeMap<>();
		}
	};
	// TODO
	
	// constructor
	public FireClusterManager(FireBrigadeWorld world) {
		super(world);
		this.clusterMembershipChecker = new FireclusterMembershipCheckerEstimatorBased();
		if (world.isMapHuge())
			CLUSTER_RANGE_THRESHOLD = (int) MEAN_VELOCITY_OF_MOVING * 5;
		else if (world.isMapMedium())
			CLUSTER_RANGE_THRESHOLD = (int) MEAN_VELOCITY_OF_MOVING * 3;
		else if (world.isMapSmall())
			CLUSTER_RANGE_THRESHOLD = (int) MEAN_VELOCITY_OF_MOVING;
		
		// TODO
		CSU_PartitionLayer.CLUSTER_RANGE_THRESHOLD = CLUSTER_RANGE_THRESHOLD;
	}

	@Override
	public void updateClusters() throws TimeOutException{
		FireCluster fireCluster;
		FireCluster tempCluster;
		Set<FireCluster> adjacentClusters = new FastSet<FireCluster>();
		Building building = null;
		((FireBrigadeWorld)world).getEstimatedBurningBuildings().clear();
		
		// TODO
		Integer time;
		int estimatedTemperature, estimatedFieryness;
		String buildingId;
		// TODO
		
		for (Iterator<FireCluster> itor = dyingClusterList.iterator(); itor.hasNext(); ) {
			FireCluster cluster = itor.next();
			cluster.increaseDyingTimeLock();
			if (cluster.getAllEntities().isEmpty() || cluster.getDyingTimeLock() >= 5) {
				this.clusters.remove(cluster);
				itor.remove();
			}
		}
		
		for (CSUBuilding csuBuilding : world.getCsuBuildings()) {
			// TODO
			if (AgentConstants.PRINT_BUILDING_INFO) {
				buildingId = csuBuilding.getId().toString();
				time = new Integer(world.getTime());
				estimatedTemperature = (int) csuBuilding.getEstimatedTemperature();
				estimatedFieryness = csuBuilding.getEstimatedFieryness();
				
				timeTemperature.get(buildingId).put(time, estimatedTemperature);
				timeFieryness.get(buildingId).put(time, estimatedFieryness);
			}
			// TODO
			
			csuBuilding.BUILDING_VALUE = Double.MIN_VALUE;
			
			if (this.clusterMembershipChecker.membershipCheck(world, csuBuilding)) {
				if (csuBuilding.getEstimatedFieryness() > 0 && csuBuilding.getEstimatedFieryness() < 4)
					((FireBrigadeWorld)world).getEstimatedBurningBuildings().add(csuBuilding);
				
				building = csuBuilding.getSelfBuilding();
				
				fireCluster = this.getCluster(building.getID());
				if (fireCluster == null) {
					fireCluster = new FireCluster(world);
					fireCluster.add(building);
					
					for (StandardEntity entity : world.getObjectsInRange(building, CLUSTER_RANGE_THRESHOLD)) {
						if (!(entity instanceof Building))
							continue;
						tempCluster = this.getCluster(entity.getID());
						if (tempCluster != null)
							adjacentClusters.add(tempCluster);
					}
					if (adjacentClusters.isEmpty()) 
						addToClusterList(fireCluster, building.getID());
					else
						merge(adjacentClusters, fireCluster, building);
				} else {
					// do nothing
				}
			} else {
				// Was it previously in any cluster?
				building = csuBuilding.getSelfBuilding();
				fireCluster = this.getCluster(csuBuilding.getId());
				if (fireCluster != null) {
					fireCluster.remove(building);
					this.entityClusterMap.remove(building.getID());
					if (fireCluster.getEntities().isEmpty()) {
						fireCluster.setDying(true);
						
						Polygon polygon = fireCluster.getConvexObject().getConvexHullPolygon();
						Set<EntityID> re = ((FireBrigadeWorld)world).getAreaInShape(polygon);
						fireCluster.setDyingClusterAllEntities(re);
						this.dyingClusterList.add(fireCluster);
					}
				}
			}
			world.getAgent().isThinkTimeOver("updateCluster");
		}
		
		Map<FireCluster, Set<FireCluster>> canMerge = new FastMap<>();
		List<FireCluster> beenMergedCluster = new ArrayList<>();
		for (FireCluster cluster1 : clusters) {
			if (cluster1.isDying)
				continue;
			if (beenMergedCluster.contains(cluster1))
				continue;
			Set<FireCluster> food = new FastSet<>();
			for (FireCluster cluster2 : clusters) {
				if (cluster2.isDying)
					continue;
				if (cluster1.equals(cluster2))
					continue;
				if (beenMergedCluster.contains(cluster2))
					continue;
				if (this.canMerge(cluster1, cluster2)) {
					food.add(cluster2);
					beenMergedCluster.add(cluster2);
				}
			}
			canMerge.put(cluster1, food);
		}
		
		for (FireCluster next : canMerge.keySet()) {
			for (FireCluster nextFood : canMerge.get(next)) {
				next.merge(nextFood);
				for (StandardEntity entity : nextFood.caredEntities) {
					entityClusterMap.put(entity.getID(), next);
				}
				this.clusters.remove(nextFood);
			}
		} 
		
		for (FireCluster cluster : clusters) {
			if (cluster.isDying)
				continue;
			cluster.updateConvexHull();
			Polygon polygon = cluster.getConvexObject().getConvexHullPolygon();
			cluster.updateFireCondition();
			cluster.setAllEntities(((FireBrigadeWorld)world).getAreaInShape(polygon));
		}
		 
		this.allIgnoredBorderEntities.clear();
		for (FireCluster cluster : clusters) {
			cluster.getSingleIgnoreBorderEntities().clear();
		}
		for (int i = 0; i < clusters.size(); i++) {
			for (int j = i + 1; j < clusters.size(); j++) {
				this.findMutualEntity(clusters.get(i), clusters.get(j), allIgnoredBorderEntities);
			}
		}
		
		
		if (AgentConstants.LAUNCH_VIEWER) {
			EntityID id = world.getAgent().getID();
			CSU_ConvexHullLayer.IGNORE_BORDER_BUILDING.put(id, allIgnoredBorderEntities);
			CSU_ConvexHullLayer.CONVEX_HULLS_MAP.put(id, clusters);
			
			Point2D point_2D;
			Pair<Point2D, String> pair;
			List<Pair<Point2D, String>> firecluster_condition = new ArrayList<>();
			Set<StandardEntity> border_buildings = new FastSet<>();
			List<Polygon> big_polygons = new ArrayList<>();
			List<Polygon> small_polygons = new ArrayList<>();
			
			for (FireCluster next : clusters) {
				
				point_2D = new Point2D(next.getCenter().x, next.getCenter().y);
				pair = new Pair<Point2D, String>(point_2D, next.getFireCondition().toString()); ///NullPointerException
				firecluster_condition.add(pair);
				
				border_buildings.addAll(next.getBorderEntities());
				big_polygons.add(next.getBigBorderPolygon());
				small_polygons.add(next.getSmallBorderPolygon());
				next.getFireCondition().toString();
			}
			
			CSU_ConvexHullLayer.FIRE_CLUSTER_CONDITIONS.put(id, firecluster_condition);
			CSU_ConvexHullLayer.BORDER_BUILDINGS.put(id, border_buildings);
			CSU_ConvexHullLayer.BIG_BORDER_HULLS.put(id, big_polygons);
			CSU_ConvexHullLayer.SMALL_BORDER_HULLS.put(id, small_polygons);
		}
	}
	
	@Override
	protected boolean canMerge(FireCluster cluster_1, FireCluster cluster_2) {
		Polygon polygonOfCluster_1 = cluster_1.getConvexObject().getConvexHullPolygon();
		Polygon polugonOfCluster_2 = cluster_2.getConvexObject().getConvexHullPolygon();
		java.awt.Point centerOfCluster_2 = cluster_2.getCenter();
		
		int nPointsOfCluster1 = polygonOfCluster_1.npoints;
		int nPointsOfCluster2 = polugonOfCluster_2.npoints;
		
		double[] xPointsOfCluster2 = new double[nPointsOfCluster2];
		double[] yPointsOfCluster2 = new double[nPointsOfCluster2];
		/* convert int array to double array*/
		for (int i = 0; i < nPointsOfCluster2; i++) {
			xPointsOfCluster2[i] = polugonOfCluster_2.xpoints[i];
			yPointsOfCluster2[i] = polugonOfCluster_2.ypoints[i];
		}
		SimplePolygon2D cluster2Polygon = new SimplePolygon2D(xPointsOfCluster2, yPointsOfCluster2);
		double mapArea = world.getMapDimension().getHeight() * world.getMapDimension().getWidth() / 1000000;
		if (cluster2Polygon.getArea() > mapArea * 0.1)
			return false;
		
		if (polygonOfCluster_1.contains(centerOfCluster_2))
			return true;
		
//		Point2D center = new Point2D(centerOfCluster_2.getX(), centerOfCluster_2.getY());
		for (int i = 0; i < nPointsOfCluster1; i++) {
			
			java.awt.Point point1 = 
					new java.awt.Point(polygonOfCluster_1.xpoints[i], polygonOfCluster_1.ypoints[i]);
			
//			Point2D point1 = new Point2D(polygonOfCluster_1.xpoints[i], polygonOfCluster_1.ypoints[i]);
//			
//			double x = polygonOfCluster_1.xpoints[(i + 1) % nPointsOfCluster1];
//			double y = polygonOfCluster_1.ypoints[(i + 1) % nPointsOfCluster1];
//			Point2D point2 = new Point2D(x, y);
//			
//			Line2D line = new Line2D(point1, point2);
			if (Ruler.getDistance(point1, centerOfCluster_2) < 30000)
				return true;
		}
		
		return false;
	}

	@Override
	protected void findMutualEntity(FireCluster first, FireCluster second, Set<StandardEntity> ignored){
		
		Set<StandardEntity> secondBorderBuilding = second.getBorderEntities();
		for (StandardEntity building : first.getBorderEntities()) {
			if (secondBorderBuilding.contains(building)) {
				first.getSingleIgnoreBorderEntities().add(building);
				first.getBorderEntities().remove(building);
				
				second.getSingleIgnoreBorderEntities().add(building);
				second.getBorderEntities().remove(building);
				
				ignored.add(building);
			}
		}
	}
	///prefer cluster with border buildings
	@Override
	public FireCluster findNearestCluster(Pair<Integer, Integer> location) {
		if (clusters == null || clusters.isEmpty())
			return null;
		FireCluster resultCluster = null;
		double minDistance = Double.MAX_VALUE;
		
		Point position = new Point(location.first(), location.second());
		
		Set<FireCluster> surroundByOtherCluster = new HashSet<>();
		// Set<FireCluster> dyingOrNotExpanableCluster = new HashSet<>();
		for (FireCluster cluster : clusters) {
			if (cluster.getBorderEntities().isEmpty()) {
				surroundByOtherCluster.add(cluster);
				continue;
			}
//			if (cluster.isDying() || cluster.isExpandableToMapCenter()) {
//				dyingOrNotExpanableCluster.add(cluster);
//				continue;
//			}
//			if (cluster.isDying()) {
//				dyingOrNotExpanableCluster.add(cluster);
//				continue;
//			}
			double distance = Ruler.getDistance(cluster.getCenter(), position);
			if (distance < minDistance) {
				resultCluster = cluster;
				minDistance = distance;
			}
		}
		
//		minDistance = Double.MAX_VALUE;
//		if (resultCluster == null) {
//			for (FireCluster cluster : dyingOrNotExpanableCluster) {
//				// Polygon polygon = cluster.getConvexObject().getConvexHullPolygon();
//				double distance = Ruler.getDistance(cluster.getCenter(), position);
//				if (distance < minDistance) {
//					resultCluster = cluster;
//					minDistance = distance;
//				}
//			}
//		}
		
		minDistance = Double.MAX_VALUE;
		if (resultCluster == null) {
			for (FireCluster cluster : surroundByOtherCluster) {
				// Polygon polygon = cluster.getConvexObject().getConvexHullPolygon();
				double distance = Ruler.getDistance(cluster.getCenter(), position);
				if (distance < minDistance) {
					resultCluster = cluster;
					minDistance = distance;
				}
			}
		}
		
		return resultCluster;
	}
	
	// TODO
	public Map<String, Map<Integer, Integer>> getTimeEstimatedTemperature() {
		return timeTemperature;
	}
	public Map<String, Map<Integer, Integer>> getTimeEstimatedFieryness() {
		return timeFieryness;
	}
	// TODO
}
