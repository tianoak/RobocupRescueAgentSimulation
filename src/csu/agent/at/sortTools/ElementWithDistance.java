package csu.agent.at.sortTools;

import rescuecore2.worldmodel.EntityID;

/**
 * This file defines a class extending AbstractSortElement whose data represents the distance
 * @author Nale
 * Jun 26, 2014
 */
public class ElementWithDistance extends AbstractSortElement{

	public ElementWithDistance (EntityID entityID , int distance){
		this.entityID = entityID;
		this.data = distance;
	}
	
	public EntityID getEntityID(){
		return entityID;
	}
}
