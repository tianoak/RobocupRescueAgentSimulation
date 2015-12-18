package csu.agent.at.cluster;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.swing.JFrame;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import csu.standard.Ruler;

/**
 * The class is used to get the clusters of the map
 * 
 * @author nale
 *
 */
public class Clustering {
	
	/** The most large distance between center and building among all clusters*/
	public static double MAX_RADIUS = 0.0;
	
//	/**
//	 * A map stores the minimum travel distance between all the areas in world model .
//	 */
//	public static HashMap<EntityID , Map<EntityID , Long>> travelDistanceMap = new HashMap<EntityID , Map<EntityID , Long>>();
//	
//	/**
//	 * To calculate the minimum travel distance between
//	 * @param areas
//	 * @param model
//	 */
//	public static void getTravelDistanceMap(Collection<StandardEntity> areas , StandardWorldModel model){
//	}
	
	
	
	/**
	 * To return the Clusters of the giving collection of StandardEntity using KmeansPluPlus.
	 * And the times of iteration is 50.
	 * @param numberofClusters
	 * 					the number of clusters to be devided
	 * @param nodes
	 * 					the set of <code>StandardEntiy</code> to be clustered
	 * @param model
	 * 					the <code>StandardWorldModel</code> to be used
	 * @return
	 * 					<code>ArrayList</code> of <code>Cluster</code>
	 */
	public static ArrayList<Cluster> KMeansPlusPlus(final int numberofClusters, final Collection<StandardEntity> points,
			final StandardWorldModel model){
		return KMeansPlusPlus(numberofClusters, points, model, 100);
	}
	
	/**
	 * To return the Clusters of the giving collection of StandardEntity using KmeansPluPlus.
	 * It needs a int number which represent the times to iterate
	 * @param numberofClusters
	 * 					the number of clusters to be devided
	 * @param nodes
	 * 					the set of <code>StandardEntiy</code> to be clustered
	 * @param model
	 * 					the <code>StandardWorldModel</code> to be used
	 * @param timesOfIteration
	 * 					the times to iterate
	 * @return
	 * 					<code>ArrayList</code> of <code>Cluster</code>
	 */
	public static ArrayList<Cluster> KMeansPlusPlus(final int numberOfClusters , final Collection<StandardEntity> points ,
			final StandardWorldModel model , final int timesOfIteration){
		ArrayList<Area> areas = new ArrayList<Area>(points.size());
		for (StandardEntity se : points){
			Area area = (Area) se;
			areas.add(area);
		}
		ArrayList<Cluster> clusters = ChooseInitialClusters(numberOfClusters , areas, model);
		for (Area area : areas){
			Cluster nearestCluster= getClosestClusterForArea(clusters , area);
			nearestCluster.getCluster().add(area.getID());
		}
		for (int step=1 ; step<=timesOfIteration ; step++){
			boolean isChanged = false;
			for (Cluster cluster : clusters){
				Pair<Integer , Integer> newCentroid = getNewCentroid(cluster , model);
				Pair<Integer , Integer> oldCentroid = cluster.getCentroid();
				if (newCentroid.first().doubleValue() != oldCentroid.first().doubleValue() ||
						newCentroid.second().doubleValue() != oldCentroid.second().doubleValue()){
					isChanged = true;
					cluster.setCentroid(newCentroid);
				}
			}
			if (!isChanged){
				for (Cluster cluster : clusters){
					cluster.setPerc(cluster.getCluster().size()/points.size());
					cluster.setCenterEntity(getClosestEntity(cluster , model));
				}
				System.out.println("#Times of iteration : "+step);
				return clusters;
			}
			else {
				for (Cluster cluster : clusters) 
					cluster.getCluster().clear();
				for (Area area : areas){
					Cluster nearestCluster= getClosestClusterForArea(clusters , area );
					nearestCluster.getCluster().add(area.getID());
				}
				for (Cluster cluster : clusters) if (cluster.getCluster().isEmpty()){
						clusters.remove(cluster);
				}
			}
		}
		for (Cluster cluster : clusters){
			cluster.setPerc(cluster.getCluster().size()/points.size());
			cluster.setCenterEntity(getClosestEntity(cluster , model));
		}
		System.out.println("#Times of iteration : "+50);
		return clusters;
	}
	
	/**
	 * To choose <code>numberOfClusters</code> points randomly from the giving <code>points</code> 
	 * as the centers of the initial clusters
	 * @param numberOfClusters
	 * @param points
	 * @param model
	 * @return
	 */
	public static ArrayList<Cluster> ChooseInitialClusters(final int numberOfClusters , final Collection<Area> points, 
			final StandardWorldModel model){
		
		ArrayList<Cluster> result = new ArrayList<Cluster>(); 
		ArrayList<Area> pointSet = new ArrayList<Area> (points);
		Random random = new Random(points.size());
		Area firstPoint= (Area)pointSet.remove(random.nextInt(pointSet.size()));
		Pair<Integer , Integer> centroid = new Pair<Integer , Integer>
										(firstPoint.getX(),firstPoint.getY());
		result.add(new Cluster(null , centroid , firstPoint , 0));
		double distances[] = new double[pointSet.size()];
		while (result.size() < numberOfClusters){
			double sum=0;
			/**	For each point in the pointSet , calculate the distance with the nearest cluster*/
			for (int i = 0; i < pointSet.size() ; i++){
				Area area = pointSet.get(i);
				Cluster nearest = getClosestClusterForArea(result , area );
				Pair<Integer , Integer> pointOfArea = new Pair<Integer , Integer> (area.getX() , area.getY());
				Double dis = Ruler.getDistance(pointOfArea, nearest.getCentroid());
				sum+=dis*dis;
				distances[i]=sum;
			}
			final double r = random.nextDouble()*sum;
			for (int i = 0 ; i < distances.length ; i++){
				if (distances[i] >= r){
					Area area = pointSet.remove(i);
					final Pair<Integer , Integer> newCentroid = new Pair<Integer , Integer> (area.getX() , area.getY());
					result.add(new Cluster(null , newCentroid , area , 0));
					break;
				}
			}
		}
		return result;
		
	}
	
	/**
	 * To find the closest cluster beyond the giving <code>area</code> 
	 * from the <code>clusters</code>.
	 * @param clusters
	 * @param area
	 * @return
	 */
	public static Cluster getClosestClusterForArea(final ArrayList<Cluster> clusters, final Area area){
		
		Pair<Integer , Integer> point = new Pair<Integer , Integer>(area.getX() , area.getY());
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
	 * @param cluster
	 * @param model
	 * @return
	 */
	public static Pair<Integer , Integer> getNewCentroid(final Cluster cluster , 
			final StandardWorldModel model){
		
		int sumX = 0;
		int sumY = 0;
		for (EntityID id : cluster.getCluster()){
			Area area = (Area)model.getEntity(id);
			sumX += area.getX();
			sumY += area.getY();
		}
		int size = cluster.getCluster().size();
		Pair<Integer , Integer> centroid = new Pair<Integer , Integer>(sumX/size , sumY/size);
		return centroid;
		
	}
	

	/**
	 * To find the cloest point beyond the giving <code>cluster</code> from the <code>targets</code>
	 * @param cluster
	 * @param targets
	 * @param model
	 * @return
	 */
	public static Area getClosestEntity(final Cluster cluster , final StandardWorldModel model){
		
		Area cloest = null;
		double minDistance = Double.MAX_VALUE;
		double maxDistance = 0.0;
		for (EntityID id : cluster.getCluster()){
			Area area = (Area)model.getEntity(id);
			Pair<Integer , Integer> point = new Pair<Integer , Integer>(area.getX() , area.getY());
			double distance = Ruler.getDistance(point, cluster.getCentroid());
			if (distance < minDistance){
				minDistance = distance;
				cloest = area;
			}
			if (distance > maxDistance)
				maxDistance = distance ;
		}
		if (maxDistance > MAX_RADIUS)
			MAX_RADIUS = maxDistance;
		return cloest;
	}
	
	/**
	 * To assign a agent for each cluster . The result is each cluster has only one assigned agent , 
	 * that is to said , there may exist some agents which are not assigned.
	 * @param agents
	 * @param clusters
	 * @param model
	 */
	public static void assignAgentsToClusters(final Collection<StandardEntity> agents , final ArrayList<Cluster> clusters ,
			final StandardWorldModel model){
		
		ArrayList<StandardEntity> agentsCopy = new ArrayList<StandardEntity>(agents);
		for (Cluster cluster : clusters){
			double minDistance = Double.MAX_VALUE;
			StandardEntity closest = null;
			for (StandardEntity se : agentsCopy){
				Human human = (Human)se;
				Pair<Integer , Integer> point = new Pair<Integer , Integer>(human.getX() , human.getY());
				double distance = Ruler.getDistance(point, cluster.getCentroid());
				if (distance < minDistance){
					minDistance = distance;
					closest = human;
				}
			}
			if (closest != null)
			cluster.getAgents().add(closest.getID());
			agentsCopy.remove(closest);
	}
	}
	
	/**
	 * To get the closest cluster beyond the giving Huaman (specified by EntityID).
	 * @param id the ID of the human
	 * @param clusters
	 * @param model
	 * @return
	 */
	public static int getCloestClusterForHuman(EntityID id , ArrayList<Cluster> clusters , StandardWorldModel model){
		
		Human human = (Human)model.getEntity(id);
		Pair<Integer , Integer> point = null;
		int index = -1;
		if (human.isXDefined() && human.isYDefined())
			point = new Pair<Integer , Integer>(human.getX() , human.getY());
		else if (human.isPositionDefined()){
			StandardEntity pos = human.getPosition(model);
			if (pos instanceof Area){
				Area area = (Area)pos;
				point = new Pair<Integer , Integer>(area.getX() , area.getY());
			}
		}
		if (point != null)
			index = getClosestClusterForHuman(point , clusters , model , -1 , false , false);
		return index;
	}
	
	/**
	 * To return a not empty cluster's index from the giving clusters
	 * The cluster is closest to the giving position. 
	 * @param position the ID of the position of the giving human
	 * @param lists
	 * @param model
	 * @return
	 */
	public static int getClosestNotEmptyClusterIndexForHuman(EntityID position , ArrayList<Cluster> clusters , 
			 StandardWorldModel model){
		Area area = (Area)model.getEntity(position);
		Pair<Integer , Integer> point = new Pair<Integer , Integer>(area.getX() , area.getY());
		return getClosestClusterForHuman(point , clusters , model , -1 , true , false);
	}
	
	/**
	 * To return a not empty cluster's index from the giving clusters
	 * The cluster is closest to the giving position. 
	 * The index is different from the giving cluster index.
	 * @param position
	 * @param lists
	 * @param model
	 * @param clusterIndex
	 * @return
	 */
	public static int getClosestNotEmptyDiffClusterIndexForHuman(EntityID position , ArrayList<Cluster> clusters ,
			StandardWorldModel model , int clusterIndex){
		
		Area area = (Area)model.getEntity(position);
		Pair<Integer , Integer> point = new Pair<Integer , Integer>(area.getX() , area.getY());
		return getClosestClusterForHuman(point , clusters , model , clusterIndex , true , false);
		
	}
	
	/**
	 * To return a cluster's index from the giving clusters.
	 * The cluster is closest to the giving position. 
	 * The cluster must contains at least one road which is a building entrance 
	 * The cluster index is different from the giving cluder index.
	 * @param position
	 * @param lists
	 * @param model
	 * @param clusterIndex
	 * @return
	 */
	public static int getClosestNotEmptyClusterIndexPolicForHuman(EntityID position , ArrayList<Cluster> clusters , 
			StandardWorldModel model , int clusterIndex){
		
		Area area = (Area)model.getEntity(position);
		Pair<Integer , Integer> point = new Pair<Integer , Integer>(area.getX() , area.getY());
		return getClosestClusterForHuman(point , clusters , model , -1 , true , true);
	}
	
	/**
	 * To get the closest cluster beyond the giving position (specified by EntityID).
	 * The method analyses three situation.
	 * @param point
	 * @param clusters
	 * @param model
	 * @param clusterIndex
	 * @param isEmpty
	 * @param isPolic
	 * @return
	 */
	public static int getClosestClusterForHuman(Pair<Integer , Integer> point , ArrayList<Cluster> clusters , 
			StandardWorldModel model , int clusterIndex , boolean isEmpty , boolean isPolic){
		
		double minDistance = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0 ; i < clusters.size() ; i++){
			if (clusterIndex == i) continue;
			Cluster cluster = clusters.get(i);
			if (isEmpty && cluster.getCluster().isEmpty()) continue;
//			if (getClusterBuildingsEntrance(model, cluster)).isEmpty())
//				continue;
			double distance = Ruler.getDistance(point,cluster.getCentroid());
			if (distance < minDistance){
				minDistance = distance;
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * To find the cluster index which the giving agent belongs to.
	 * If found , return the index.
	 * If not found , return the size of the giving ArrayList of Cluster
	 * @param id
	 * @param clusters
	 * @return
	 */
	public static int getClusterIndexAgentBelong(EntityID id , ArrayList<Cluster> clusters){
		
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
	
	
//	public static int getTravelDistance( ){
//		return 0;
//	}
	
	/**
	 * To write the information of clusters by the first agent connected to kernel.
	 * So the follow agents are no need to do the same thing again when connecting to kernel.
	 * They can read from this file to get the information of clusters.
	 * By this way , it will cut down a lot of time .<br>
	 * <p>
	 * The form of the information of clusters in this file is as follow:<br>
	 * * There are n lines in the file , where the number "n" is the size of the clusters.<br>
	 * * For each cluster , there are one lines to store it's information.<br>
	 * * The line of each cluster represents the area IDs belong to the cluster.<br>
	 * * All the IDs are seprated by one blank.
	 * @param clusters
	 * @param filename
	 */
	public static void writeToFile(ArrayList<Cluster> clusters , String filename){
		try {
			FileWriter fw = new FileWriter(filename);
			for (Cluster cluster : clusters){
				String clusterMessage = "";
				for (EntityID clusterID : cluster.getCluster()){
					clusterMessage += " "+clusterID.toString();
				}
				clusterMessage = clusterMessage.substring(1);
				fw.append(clusterMessage+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * To read the information of clusters from the giving file .
	 * @param filename
	 * @param model
	 * @return
	 */
	public static ArrayList<Cluster> readFromFile(String filename , StandardWorldModel model){
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader reader = new BufferedReader(fr);
			String temString = null;
			while ((temString = reader.readLine()) != null){
				Cluster newCluster = new Cluster(null , null , null , 0);
				//To get the list of cluster ID.
				String[] clusterMessage = temString.split(" ");
				for (int i=0 ; i<clusterMessage.length ; i++){
					EntityID id = new EntityID(Integer.parseInt(clusterMessage[i]));
					newCluster.getCluster().add(id);
				}
				//To get the center entity and centroid of the new cluster
				newCluster.setCentroid(getNewCentroid(newCluster, model));
				newCluster.setCenterEntity(Clustering.getClosestEntity(newCluster , model));
				clusters.add(newCluster);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clusters;
	}
	
	
	
	/**
	 * It is used to check the Kmeans Plus Plus by simulating the result.
	 * A colorful picture will be showed to you. The form of the picture is as follow : <br>
	 * * There are many points in the picture .<br>
	 * * The bigger points are the center area of each cluster.<br>
	 * * The smaller points are the area of their coresponding cluster.<br>
	 * * There are n kinds of color in the picture ，where the number "n" stands for the number of the clusters.<br>
	 * * Each color stands for a cluster.<br>
	 * * The points have the same kind of color if they belong to the same cluster.<br>
	 * <p>
	 * NOTE ： <br>
	 * Because there exists an extra thread in this method , 
	 * so don't run this method on your poor computer !
	 * And if you don't want to see the simulation of Kmeans Plus Plus , don't use it !<br>
	 * If you want to see the result , it is no need to connect all agents to the kernel , 
	 * just one is enough !<br>
	 * If you don't follow my suggestion , 呵呵......
	 * @param model
	 * @param clusters
	 */
	public static void printForTest(final StandardWorldModel model , final ArrayList<Cluster> clusters){
		System.out.println("=============================KmeansPlusPlus=================================");
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		Collection<StandardEntity> allbuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		System.out.println("The size of all buildings : "+allbuildings.size());
		System.out.println("The size of clusters : "+clusters.size());
		for (StandardEntity se : allbuildings){
			Building building = (Building)se;
			if (building.getX() < minX) minX = building.getX();
			if (building.getX() > maxX) maxX = building.getX();
			if (building.getY() < minY) minY = building.getY();
			if (building.getY() > maxY) maxY = building.getY();
		}
		System.out.println("minX : "+minX+"      maxX : "+maxX+"       minY : "+minY+"          maxY : "+maxY);
		int[] markX = new int[maxX-minX+1];
		int[] markY = new int[maxY-minY+1];
		int[] indexX = new int[maxX-minX+1];
		int[] indexY = new int[maxY-minY+1];
		boolean[] inX = new boolean[maxX-minX+1];
		boolean[] inY = new boolean[maxY-minY+1];
		int mX=0,mY=0;
		for (Cluster cluster : clusters){
			ArrayList<EntityID> buildings = cluster.getCluster();
			for (EntityID id : buildings){
				Building building = (Building)model.getEntity(id);
				if (!inX[building.getX()-minX]) {
					markX[mX++]=building.getX()-minX;
					inX[building.getX()-minX]=true;
				}
				if (!inY[building.getY()-minY]){
					markY[mY++]=building.getY()-minY;
					inY[building.getY()-minY]=true;
				}
			}
		}
		System.out.println("mX : "+mX+"             mY : "+mY);
		Arrays.sort(markX , 0 , mX);
		Arrays.sort(markY , 0 , mY);
		for (int i=0 ; i<mX ; i++) indexX[markX[i]]=i;
		for (int i=0 ; i<mY ; i++) indexY[markY[i]]=i;
		int[][] map = new int[mX][mY];
		int sum=0;
		for (int i=1 ; i<=clusters.size() ; i++){
			ArrayList<EntityID> buildings = clusters.get(i-1).getCluster();
			sum+=buildings.size();
			System.out.println("Cluster index : "+i+"           size of builidngs : "+buildings.size());
			for (EntityID id : buildings){
				Building building = (Building)model.getEntity(id);
				if (building.getID().getValue() == clusters.get(i-1).getCenterEntity().getID().getValue())
					map[indexX[building.getX()-minX]][mY-1-indexY[building.getY()-minY]]=i*-1;
				else map[indexX[building.getX()-minX]][mY-1-indexY[building.getY()-minY]]=i;
			}
		}
		System.out.println("The total number of buildings in all clusters : "+ sum);
		JFrame frame = new JFrame();
		frame.setSize(1300,700);
		frame.setVisible(true);
		final Graphics gp = frame.getGraphics();
		final int tX = mX;
		final int tY = mY;
		final int[][] tmap = map;
		final int[] tmarkX = markX;
		final int[] tmarkY = markY;
		Thread thread = new Thread(new Runnable(){
			public void run() {
				while (true){
					for (int i=0 ; i<tX ; i++){
						for (int j=0 ; j<tY ; j++){
							if (tmap[i][j]!=0){
								gp.setColor(new Color(Math.abs(tmap[i][j])*600000+700000));
								if (tmap[i][j]>0)
									gp.fillOval((int)(tmarkX[i]*1000/(1.5*tmarkX[tX-1])+50), 
										(int)(tmarkY[j]*900/(1.5*tmarkY[tY-1])+50), 10, 10);
								else gp.fillOval((int)(tmarkX[i]*1000/(1.5*tmarkX[tX-1])+50), 
										(int)(tmarkY[j]*900/(1.5*tmarkY[tY-1])+50), 20, 20);
							}
						}
					}
				}
			}
		});
		thread.start();
//		for (int i=0; i<1122; i++)
//		System.out.println(tmarkX[i]+"       "+tmarkY[i]);
	}
	
}