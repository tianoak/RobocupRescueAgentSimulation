package csu.agent.pf;

import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;

public class PFLastTaskTarget {
	private Human helpStuckHuman;
	private Building traversalRefuge;
	private Area traversalCriticalArea;
	private Road traversalEntrance;
	private List<EntityID> entlityList;
	
	public Human getHelpStuckHuman() {
		return helpStuckHuman;
	}
	public void setHelpStuckHuman(Human helpStuckHuman) {
		this.helpStuckHuman = helpStuckHuman;
	}
	
	public Building getTraversalRefuge() {
		return traversalRefuge;
	}
	public void setTraversalRefuge(Building traversalRefuge) {
		this.traversalRefuge = traversalRefuge;
	}
	
	public Area getTraversalCriticalArea() {
		return this.traversalCriticalArea;
	}
	public void setTraversalCriticalArea(Area criticalArea) {
		this.traversalCriticalArea = criticalArea;
	}
	
	public Road getTraversalEntrance() {
		return traversalEntrance;
	}
	public void setTraversalEntrance(Road traversalEntrance) {
		this.traversalEntrance = traversalEntrance;
	}
	
	public List<EntityID> getEntlityList() {
		return entlityList;
	}
	public void setEntlityList(List<EntityID> entlityList) {
		this.entlityList = entlityList;
	}
	
}
