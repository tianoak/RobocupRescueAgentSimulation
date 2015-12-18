package csu.agent.fb.targetPart;

import rescuecore2.standard.entities.Area;

import csu.common.clustering.Cluster;
import csu.model.object.CSUBuilding;

/**
 * This class stands for the target of FBs. It encapsulate the target building
 * to extinguish, the best location to extinguish this building and the fire
 * cluster target building belongs to.
 * 
 * @author apprreciation-csu
 * 
 */
public class FireBrigadeTarget {
	/** Target building to extinguish.*/
	private CSUBuilding csuBuilding;
	/** The fire cluster target building belongs to.*/
	private Cluster cluster;
	/** The best location to extinguish this building.*/
	private Area locationToExtinguish;
	
	public FireBrigadeTarget(Cluster cluster, CSUBuilding csuBuilding) {
		this.cluster = cluster;
		this.csuBuilding = csuBuilding;
	}
	
	public void setLocationToExtinguish(Area locationToExtinguish) {
		this.locationToExtinguish = locationToExtinguish;
	}
	
	public Area getLocationToExtinguish() {
		return this.locationToExtinguish;
	}
	
	public Cluster getCluster() {
		return this.cluster;
	}
	
	public CSUBuilding getCsuBuilding() {
		return this.csuBuilding;
	}
}
