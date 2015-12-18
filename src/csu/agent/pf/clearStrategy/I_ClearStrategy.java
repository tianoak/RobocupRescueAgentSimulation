package csu.agent.pf.clearStrategy;

import java.util.List;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;

import csu.agent.Agent.ActionCommandException;
import csu.model.object.CSUEdge;

public interface I_ClearStrategy {

	public void clear() throws ActionCommandException;
	
	public void doClear(Road location, CSUEdge dir, Blockade target) throws ActionCommandException;
	
	/**
	 * @return
	 * 返回最近的blockade
	 */
	public Blockade blockedClear();
	
	public void updateClearPath(List<EntityID> path);
}
