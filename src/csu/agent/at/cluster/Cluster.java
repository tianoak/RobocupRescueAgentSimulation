package csu.agent.at.cluster;

import java.util.ArrayList;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * The class represents the cluster generate using KmeansPlusPlus
 * 
 * @author nale
 * 
 */
public class Cluster implements Comparable<Cluster> {

	/** The list of targets in the cluster, it could be a list of buildings, roads*/
	private ArrayList<EntityID> cluster;
	
	/**The coordinate of the centroid of this cluster.*/
	private Pair<Integer , Integer> centroid;
	
	/** The target whose center is the closest to the cluster's centroid*/
	private Area centerEntity;
	
	/** The rate of the number clusters in this cluster */
	private double perc;
	
	/** list of agents assigned to this cluster*/
	private ArrayList<EntityID> agents;

	public Cluster(ArrayList<EntityID> cluster, Pair<Integer , Integer> centroid,
			StandardEntity center, double perc) {
		if (cluster == null)
			this.cluster = new ArrayList<EntityID>();
		else this.cluster = cluster;
		this.centroid = centroid;
		this.centerEntity = (Area) center;
		if (this.centerEntity != null && this.centroid == null){
			this.centroid = new Pair<Integer , Integer>(this.centerEntity.getX() , this.centerEntity.getY());
		}
		this.perc = perc;
		agents = new ArrayList<EntityID>();
	}

	@Override
	public int compareTo(Cluster c) {

		if (perc > c.perc)
			return 1;

		if (perc < c.perc)
			return -1;

		return 0;
	}
	
	public ArrayList<EntityID> getCluster(){
		return this.cluster;
	}
	
	public Pair<Integer , Integer> getCentroid(){
		return this.centroid;
	}
	
	public Area getCenterEntity(){
		return this.centerEntity;
	}
	
	public double getPerc(){
		return this.perc;
	}
	
	public ArrayList<EntityID> getAgents(){
		return this.agents;
	}
	
	public void setCluster(ArrayList<EntityID> cluster){
		this.cluster=cluster;
	}
	
	public void setCentroid(Pair<Integer , Integer> centroid){
		this.centroid=centroid;
	}
	
	public void setCenterEntity(Area centerEntity){
		this.centerEntity=centerEntity;
	}
	
	public void setPerc(double perc){
		this.perc=perc;
	}
	
	public void setAgents(ArrayList<EntityID> agents){
		this.agents=agents;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof Cluster){
			Cluster c = (Cluster)o;
			if (c.getCenterEntity().equals(this.getCenterEntity()))
				return true;
		}
		return false;
	}

}
