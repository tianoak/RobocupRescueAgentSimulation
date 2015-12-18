package csu.agent.fb.actionStrategy;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.extinguishBehavior.ExtinguishBehavior_Interface;
import csu.agent.fb.tools.DirectionManager;
import csu.agent.fb.tools.FbUtilities;

/**
 * There is no need to implement this action strategy. When we do a action
 * strategy, we always consider the stuck case.
 * 
 * @author appreciation-cau
 * 
 */
public class StuckFbActionStrategy extends fbActionStrategy{

	public StuckFbActionStrategy(FireBrigadeWorld world, DirectionManager directionManager, FbUtilities fbUtil) {
		super(world, directionManager, fbUtil);
	}

	@Override
	public void execute() throws ActionCommandException {
		
	}

	@Override
	public ActionStrategyType getFbActionStrategyType() {
		return ActionStrategyType.STUCK_SITUATION;
	}

	@Override
	public ExtinguishBehavior_Interface getExtinguishBehavior() {
		return null;
	}

	@Override
	public void moveToFires() throws ActionCommandException {
		
	}
}
