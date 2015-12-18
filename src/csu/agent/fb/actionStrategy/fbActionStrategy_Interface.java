package csu.agent.fb.actionStrategy;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.extinguishBehavior.ExtinguishBehavior_Interface;
import csu.common.TimeOutException;

/**
 * This interface defines the action strategy of FB Agent. And you should
 * implement it when you write a actual action strategy.
 * 
 * @author appreciation-csu
 * 
 */
public interface fbActionStrategy_Interface {

	/** This method defines what this Fb actually need to do.*/
	public void execute() throws ActionCommandException, TimeOutException;
	
	/** Extinguish behavior when stucked.*/
	public void extinguishNearbyWhenStuck() throws ActionCommandException, TimeOutException;
	
	/** Get extinguish behavior of this action strategy because communication needed.*/
	public ExtinguishBehavior_Interface getExtinguishBehavior();
	
	/** Get the action strategy type of this fb Agent.*/
	public ActionStrategyType getFbActionStrategyType();
	
	/** Move to fire site.*/
	public void moveToFires() throws ActionCommandException;
}
