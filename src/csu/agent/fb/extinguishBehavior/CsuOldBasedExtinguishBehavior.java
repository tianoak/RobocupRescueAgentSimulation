package csu.agent.fb.extinguishBehavior;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.targetPart.FireBrigadeTarget;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;

public class CsuOldBasedExtinguishBehavior implements ExtinguishBehavior_Interface{
	
	public CsuOldBasedExtinguishBehavior(FireBrigadeWorld world) {
		
	}

	@Override
	public void extinguish(FireBrigadeWorld world, FireBrigadeTarget target)
			throws ActionCommandException, TimeOutException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extinguishNearbyWhenStuck(CSUBuilding target)
			throws ActionCommandException, TimeOutException {
		// TODO Auto-generated method stub
		
	}

}
