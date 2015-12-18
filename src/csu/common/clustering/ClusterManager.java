package csu.common.clustering;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import csu.common.TimeOutException;
import csu.model.AdvancedWorldModel;
import csu.standard.Ruler;

public abstract class ClusterManager<subCluster extends Cluster> {
	protected static int CLUSTER_RANGE_THRESHOLD;
	
	/**
	 * The world model.
	 */
	protected AdvancedWorldModel world;
	
	/**
	 * A list of all subClusters of current cycle.
	 */
	protected List<subCluster> clusters;
	
	protected Set<subCluster> dyingClusterList;
	
	/**
	 * A map stores entity and subCluster pairs.
	 */
	protected Map<EntityID, subCluster> entityClusterMap;
	
	/**
	 * Ignored border entities of all subCLuster of current cycle.
	 */
	protected Set<StandardEntity> allIgnoredBorderEntities;
	
	/**
	 * A checker used to check whether an entity can be put into this subCluster.
	 */
	protected ClusterMembershipChecker_Interface clusterMembershipChecker;
	
	// constructor
	public ClusterManager(AdvancedWorldModel world) {
		this.world = world;
		this.clusters = new ArrayList<subCluster>();
		this.dyingClusterList = new HashSet<>();
		this.entityClusterMap = new FastMap<EntityID, subCluster>();
		this.allIgnoredBorderEntities = new HashSet<>();
	}
	
	/**
	 * Update all subClusters.
	 * <pre>
	 * 1.Get all entities this subCluster care about, and check the membership
	 * of those entities.
	 * 
	 * 2.If the entity can't be put into this subCluster currently, check
	 * whether it is previously in a subCluster. If true, remove it from its
	 * previous subCluster, otherwise, do nothing.
	 * 
	 * 3.If the entity can be put into this subCluster currently, and it is also
	 * in a subCluster previously, do nothing.
	 * 
	 * 4.If the entity can be put into this subCluster currently, and it is not
	 * in a subCluster previously. Then create a new instance of subCluster, and
	 * put this entity into it.
	 * 
	 * 5.If the newly created subCluster has adjacent subClusters, merge this
	 * newly created subCluster into the nearest adjacent subCluster. Otherwise,
	 * this newly created subCluster is a isolated one and add it into
	 * {@link #clusters clusters}.
	 * 
	 * 6.After processing all entities, merge some small clusters into big clusters.
	 * 
	 * 7.Update the convex hull of each subCluster.
	 * 
	 * 7.Find the ignored border entities of all subCluster.
	 * </pre>
	 * 
	 * @throws TimeOutException
	 */
	public abstract void updateClusters() throws TimeOutException;
	
	/**
	 * Method determines whether the first subCluster can merge the second subCluster.
	 * 
	 * @param first
	 *            the first cluster
	 * @param second
	 *            the second cluster
	 * @return true if the first cluster can merge the second cluster.
	 *         Otherwise, false
	 */
	protected abstract boolean canMerge(subCluster first, subCluster second);
	
	/**
	 * Find mutual entities of two clusters. The mutual entities should be
	 * removed from those two clusters' border entities.
	 * 
	 * @param first
	 *            the first subCluster
	 * @param second
	 *            the second subCluster
	 * @param ig
	 *            a set of mutual border entities which should be ignored
	 */
	protected abstract void findMutualEntity(subCluster first, subCluster second, Set<StandardEntity> ig);
	
	/**
	 * Find the nearest FireCluster for the given Agent.
	 * 
	 * @param location
	 *            the location of the given Agent
	 * @return the nearest FireCluster of this Agent
	 */
	public abstract subCluster findNearestCluster(Pair<Integer,Integer> location);
	
	/**
	 * Merge a newly created cluster into its' nearest adjacent cluster. A newly
	 * created cluster only has one entity.
	 * 
	 * @param adjacentClusters
	 *            a set of adjacent clusters
	 * @param cluster
	 *            the newly created cluster
	 * @param entity
	 *            the only entity this newly created cluster contains
	 */
	protected void merge(Set<subCluster> adjacentClusters, subCluster cluster, StandardEntity entity) {
		subCluster nearestCluster = 
				this.findNearestCluster(entity.getLocation(world), adjacentClusters);
		nearestCluster.merge(cluster);
		
		for (StandardEntity enti : cluster.caredEntities) {
			this.entityClusterMap.remove(enti.getID());
			this.entityClusterMap.put(enti.getID(), nearestCluster);
		}
		this.clusters.remove(cluster);
	}
	
	/**
	 * Find the nearest cluster for a newly created cluster from its adjacent
	 * clusters.
	 * 
	 * @param location
	 *            the center point of the newly created cluster
	 * @param clusters
	 *            a set of adjacent clusters
	 * @return the nearest adjacent cluster
	 */
	protected subCluster findNearestCluster(Pair<Integer, Integer> location, Set<subCluster> clusters) {
		subCluster resultCluster = null;
		double minDistance = Double.MAX_VALUE;
		
		for (subCluster cluster : clusters) {
			double distance = Ruler.getDistance(cluster.getCenter(), new Point(location.first(), location.second()));
			if (distance < minDistance) {
				minDistance = distance;
				resultCluster = cluster;
			}
		}
		return resultCluster;
	}
	
	/**
	 * Add a newly created subCluster into subCluster list.
	 * 
	 * @param cluster
	 *            the newly created subCluster
	 * @param entity
	 *            the entity this newly created subCluster contains
	 */
	public void addToClusterList(subCluster cluster, EntityID entity) {
		cluster.updateConvexHull();
		this.clusters.add(cluster);
		this.entityClusterMap.put(entity, cluster);
	}
	
	/** Get the cluster this entity belongs to. Null if this entity does not belongs to any cluster.*/
	public subCluster getCluster(EntityID entity) {
		return this.entityClusterMap.get(entity);
	}
	
	/**
	 * Get all subClusters.
	 * 
	 * @return a list contains all subClusters
	 */
	public List<subCluster> getClusters() {
		return java.util.Collections.unmodifiableList(this.clusters);
	}
	
	/**
	 * Find the cluster with the smallest area within all clusters.
	 * 
	 * @return the smallest subCluster
	 */
	public subCluster findSmallestCluster() {
		subCluster smallestCluster = null;
		double clusterArea = Double.MAX_VALUE;
		for (subCluster cluster : this.clusters) {
			double area = cluster.getBoundingBoxArea();
			if (area < clusterArea) {
				clusterArea = area;
				smallestCluster = cluster;
			}
		}
		return smallestCluster;
	}
	
	/**
	 * Get the border entities of all subCluster with ignored entities removed.
	 * 
	 * @return a set of border entities of all subCluster
	 */
	public Set<StandardEntity> getAllBorderEntities() {
		Set<StandardEntity> borderEntities = new HashSet<StandardEntity>();
		for (subCluster cluster : this.clusters) {
			borderEntities.addAll(cluster.getBorderEntities());
		}
		return borderEntities;
	}
	
	/**
	 * Get the ignored border entities of all subClusters.
	 * 
	 * @return a set of ignored entities of all subClusters
	 */
	public Set<StandardEntity> getAllIgnoredBorderEntities() {
		return this.allIgnoredBorderEntities;
	}
	
//	public subCluster findMostValueCluster() {
//        subCluster result = null;
//        double value = 0;
//        for (subCluster cluster : this.clusters) {
//            double val = cluster.getValue();
//            if (val > value) {
//                value = val;
//                result = cluster;
//            }
//        }
//        return result;
//    }
}	
		