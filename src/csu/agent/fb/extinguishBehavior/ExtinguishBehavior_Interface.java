package csu.agent.fb.extinguishBehavior;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.targetPart.FireBrigadeTarget;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;

/**
 * An interface that defines the extinguish behaviors of FB.
 * 
 * @author appreciation-csu 
 *
 */
public interface ExtinguishBehavior_Interface {

	/** This is the platoon controlled extinguish bebaviours.*/
	public void extinguish(FireBrigadeWorld world, FireBrigadeTarget target) throws ActionCommandException, TimeOutException;
	
	/** This method handle case when fb was stucked.*/
	public void extinguishNearbyWhenStuck(CSUBuilding target) throws ActionCommandException, TimeOutException;
}