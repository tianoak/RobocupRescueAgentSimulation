package csu.agent.at.sortTools;

import rescuecore2.worldmodel.EntityID;

/**
 * This file defines a abstract class as the element to sort
 * @author Nale
 * Jun 26, 2014
 * @param <E>
 */
public abstract class AbstractSortElement {

	/** The EntityID of the element standing for*/
	EntityID entityID;
	
	/** The data by which the comparator sorts*/
	int data;
	
	/** To get the data*/
	protected int getData(){
		return this.data;
	}
	
	/** To get the value of EntityID*/
	protected int getIDValue(){
		return entityID.getValue();
	}
}
