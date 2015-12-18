package csu.agent.pf.cluster;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.model.AdvancedWorldModel;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.standard.Ruler;

/**
 * The class is used to get the clusters of the map for PF.
 * 
 * @author nale
 *
 */
public class Clustering {
	
	/** The most large distance between center and building among all clusters*/
	public static double MIN_RADIUS = Double.MAX_VALUE;
	
	/**
	 * To return the Clusters of the giving collection of CsuZone using
	 * KmeansPluPlus. And the times of iteration is 100.
	 * 
	 * @param numberofClusters
	 *            the number of clusters to be devided
	 * @param points
	 *            the set of <code>CsuZone</code> to be clustered
	 * @return <code>ArrayList</code> of <code>Cluster</code>
	 */
	public static List<Cluster> KMeansPlusPlus(final int numberofClusters, final Collection<CsuZone> points){
		List<Cluster> clusters = KMeansPlusPlus(numberofClusters, points, 100);
		
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			for (CsuZone zone : cluster.getZoneList()) {
				zone.setBelongPfClusterIndex(i);
			}
		}
		
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			for (CsuZone zone : cluster.getZoneList()) {
				for (CsuZone neig_zone : zone.getNeighbourZones()) {
					int n_z_b_c_index = neig_zone.getBelongPfClusterIndex();
					if (n_z_b_c_index != i && n_z_b_c_index != -1)
						cluster.addNeighbours(n_z_b_c_index);
				}
			}
		}
		
		return clusters;
	}
	
	/**
	 * To return the Clusters of the giving collection of CsuZone using KmeansPluPlus.
	 * It needs a integer number which represent the times to iterate
	 * @param numberofClusters
	 * 					the number of clusters to be devided
	 * @param points
	 * 					the set of <code>CsuZone</code> to be clustered
	 * @param timesOfIteration
	 * 					the times to iterate
	 * @return
	 * 					<code>ArrayList</code> of <code>Cluster</code>
	 */
	public static ArrayList<Cluster> KMeansPlusPlus(final int numberOfClusters , 
			final Collection<CsuZone> points , final int timesOfIteration){
		ArrayList<Cluster> clusters = ChooseInitialClusters(numberOfClusters ,points);
		for (CsuZone zone : points){
			Cluster nearestCluster= getClosestClusterForZone(clusters , zone);
			nearestCluster.getZoneList().add(zone);
		}
		for (int step=1 ; step <= timesOfIteration ; step++){
			boolean isChanged = false;
			for (Cluster cluster : clusters){
				Point newCentroid = getNewCentroid(cluster);
				Point oldCentroid = cluster.getCentroid();
				if (!newCentroid.equals(oldCentroid)){
					isChanged = true;
					cluster.setCentroid(newCentroid);
				}
			}
			if (!isChanged){
				System.out.println("#Times of iteration : " + step);
				return clusters;
			} else {
				for (Cluster cluster : clusters) 
					cluster.getZoneList().clear();
				for (CsuZone zone : points){
					Cluster nearestCluster= getClosestClusterForZone(clusters , zone);
					nearestCluster.getZoneList().add(zone);
				}
				for (Cluster cluster : clusters) if (cluster.getZoneList().isEmpty()){
						clusters.remove(cluster);
				}
			}
		}
		System.out.println("#Times of iteration : " + 50);
		return clusters;
	}
	
	/**
	 * To choose <code>numberOfClusters</code> points randomly from the giving
	 * <code>points</code> as the centers of the initial clusters
	 * 
	 * @param numberOfClusters
	 *            the number of clusters
	 * @param points
	 *            the initial points you need to cluster
	 * @return the initial cluster for all points
	 */
	public static ArrayList<Cluster> ChooseInitialClusters(final int numberOfClusters , final Collection<CsuZone> points){
		ArrayList<Cluster> result = new ArrayList<Cluster>(); 
		ArrayList<CsuZone> pointSet = new ArrayList<CsuZone> (points);
		Random random = new Random(points.size());
		CsuZone firstPoint= pointSet.remove(random.nextInt(pointSet.size()));
		Point centroid = new Point(firstPoint.getZoneCenter());
		result.add(new Cluster(null ,centroid));
		double distances[] = new double[pointSet.size()];
		while (result.size() < numberOfClusters){
			double sum=0;
			/**	For each point in the pointSet , calculate the distance with the nearest cluster*/
			for (int i = 0; i < pointSet.size() ; i++){
				CsuZone nextZone = pointSet.get(i);
				final Point point = nextZone.getZoneCenter();
				Cluster nearestCluster = getClosestClusterForZone(result , nextZone);
				Double dis = Ruler.getDistance(point, nearestCluster.getCentroid());
				sum+=dis*dis;
				distances[i]=sum;
			}
			final double r = random.nextDouble()*sum;
			for (int i = 0 ; i < distances.length ; i++){
				if (distances[i] >= r){
					CsuZone zone = pointSet.remove(i);
					final Point newCentroid = new Point(zone.getZoneCenter());
					result.add(new Cluster(null , newCentroid));
					break;
				}
			}
		}
		return result;
		
	}
	
	/**
	 * To find the closest cluster beyond the giving <code>point</code> from the
	 * <code>clusters</code>.
	 * 
	 * @param clusters
	 *            a list of clusters
	 * @param area
	 *            the target point
	 * @return the closest cluster to the target point
	 */
	public static Cluster getClosestClusterForZone(final List<Cluster> clusters, final CsuZone zone){
		final Point point = zone.getZoneCenter();
		Cluster nearest = null;
		double minDistance = Double.MAX_VALUE;
		for (Cluster cluster : clusters){
			double distance = Ruler.getDistance(point , cluster.getCentroid());
			if (distance < minDistance) {
				minDistance = distance;
				nearest = cluster;
			}
		}
		return nearest;
	}
	
	/**
	 * To find the closest cluster beyond the gave clusterPF from clusterPFs
	 * 
	 * @param clusters
	 *            a list of clusters
	 * @param clusterPF
	 *            the target cluster
	 * @return the closest cluster to the target cluster
	 */
	public static Cluster getClosestCluster (final Collection<Cluster> clusters, final Cluster clusterPF) {
		final Point point = clusterPF.getCentroid();
		Cluster nearest = null;
		double minDistance = Double.MAX_VALUE;
		for (Cluster cluster : clusters){
			double distance = Ruler.getDistance(point , cluster.getCentroid());
			if (distance < minDistance) {
				minDistance = distance;
				nearest = cluster;
			}
		}
		return nearest;
	}
	
	/**
	 * To calculate the centroid of the giving cluster.
	 * 
	 * @param cluster
	 *            the target cluster
	 * @return the center point of the target cluster
	 */
	public static Point getNewCentroid(final Cluster cluster){
		
		int sumX = 0;
		int sumY = 0;
		for (CsuZone zone : cluster.getZoneList()){
			final Point point = zone.getZoneCenter();
			sumX += point.getX();
			sumY += point.getY();
		}
		int size = cluster.getZoneList().size();
		Point centroid = new Point(sumX/size , sumY/size);
		return centroid;
		
	}
	

	/**
	 * To assign a agent for each cluster . The result is each cluster has only
	 * one assigned agent , namely , there may exist some agents which are not
	 * assigned.
	 * 
	 * @param agents
	 *            a list of agents to assign
	 * @param clusters
	 *            target clusters
	 * @param model
	 *            the world model
	 */
	public static void assignAgentsToClusters(final Collection<StandardEntity> agents , 
			final List<Cluster> clusters , final AdvancedWorldModel model){
		
		ArrayList<StandardEntity> agentsCopy = new ArrayList<StandardEntity>(agents);
		for (Cluster cluster : clusters){
			double minDistance = Double.MAX_VALUE;
			StandardEntity closest = null;
			for (StandardEntity se : agentsCopy){
				Human human = (Human)se;
				Point point = new Point(human.getX(),human.getY());
				double distance = Ruler.getDistance(point, cluster.getCentroid());
				if (distance < minDistance){
					minDistance = distance;
					closest = human;
				}
			}
			if (closest != null)
			cluster.getAgents().add(closest.getID());
			agentsCopy.remove(closest);
			
			cluster.setWorldModel(model);		// May 17, 2014
			cluster.initialize();				// May 24, 2014
		}
	}
	
	
	/**
	 * To find the cluster index which the giving agent belongs to. If found ,
	 * return the index. If not found , return the size of the giving ArrayList
	 * of Cluster
	 * 
	 * @param id
	 *            the target agent
	 * @param clusters
	 *            a list of clusters
	 * @return the index of the cluster target agent belongs to
	 */
	public static int getClusterIndexAgentBelongTo(EntityID id , List<Cluster> clusters){
		
		int index = -1;
		for (int i = 0 ; i < clusters.size() ; i++){
			Cluster cluster = clusters.get(i);
			if (cluster.getAgents().contains(id)){
				index = i;
				break;
			}
		}
		return index;
	}
}