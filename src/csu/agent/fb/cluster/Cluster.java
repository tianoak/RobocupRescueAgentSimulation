package csu.agent.fb.cluster;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.model.AdvancedWorldModel;
import csu.model.CriticalArea;

/**
 * The class represents the cluster generate using KmeansPlusPlus
 * 
 * @author nale
 * 
 */
public class Cluster implements Comparable<Cluster> {

	/** The list of targets in the cluster, it could be a list of buildings, roads*/
	private List<EntityID> points;
	
	/**The coordinate of the centroid of this cluster.*/
	private Pair<Integer , Integer> centroid;
	
	private List<EntityID> buildingAreas = new ArrayList<>();
	private List<EntityID> roadAreas = new ArrayList<>();
	
	private List<EntityID> criticalAreas = new ArrayList<>();
	
	/** The target whose center is the closest to the cluster's centroid*/
	private Area centerEntity;
	
	/** The rate of the number clusters in this cluster */
	private double perc;
	
	/** list of agents assigned to this cluster*/
	private List<EntityID> agents;
	
	private AdvancedWorldModel world;

	public Cluster(List<EntityID> points, Pair<Integer , Integer> centroid,
			StandardEntity center, double perc, AdvancedWorldModel world) {
		if (points == null)
			this.points = new ArrayList<EntityID>();
		else 
			this.points = points;
		this.world = world;
		this.centroid = centroid;
		this.centerEntity = (Area) center;
		if (this.centerEntity != null && this.centroid == null){
			this.centroid = new Pair<Integer , Integer>(this.centerEntity.getX() , this.centerEntity.getY());
		}
		this.perc = perc;
		agents = new ArrayList<EntityID>();
	}
	
	public void classifyPoints() {
		CriticalArea critical = world.getCriticalArea();
		for (EntityID next : points) {
			StandardEntity en = world.getEntity(next);
			if (en instanceof Road) {
				// only roads can be critical areas
				if (critical.isCriticalArea(next))
					criticalAreas.add(next);
				roadAreas.add(next);
			} else if (en instanceof Building) {
				buildingAreas.add(next);
			}
		}
	}

	@Override
	public int compareTo(Cluster c) {

		if (perc > c.perc)
			return 1;

		if (perc < c.perc)
			return -1;

		return 0;
	}
	
	public List<EntityID> getPoints(){
		return this.points;
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
	
	public List<EntityID> getAgents(){
		return this.agents;
	}
	
	public void setPoints(ArrayList<EntityID> points){
		this.points = points;
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
	
	public List<EntityID> getBuildingAreas() {
		return this.buildingAreas;
	}
	
	public List<EntityID> getRoadAreas() {
		return this.roadAreas;
	}
	
	public List<EntityID> getCriticalAreas() {
		return this.criticalAreas;
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
