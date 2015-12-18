package csu.agent.fb.actionStrategy;

import java.awt.Point;

import java.util.List;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;
 
import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.extinguishBehavior.CsuOldBasedExtinguishBehavior;
import csu.agent.fb.extinguishBehavior.DirectionBasedExtinguishBehavior;
import csu.agent.fb.extinguishBehavior.ExtinguishBehavior_Interface;
import csu.agent.fb.targetPart.DirectionBasedTargetSelector;
//import csu.agent.fb.extinguishBehavior.GreedyExtinguishBehavior;
import csu.agent.fb.targetPart.DirectionBasedTargetSelector_OLD;
import csu.agent.fb.targetPart.FireBrigadeTarget;
import csu.agent.fb.targetPart.MATargetSelector;
import csu.agent.fb.targetPart.MuitiFactorsTargetSeletor;
import csu.agent.fb.tools.DirectionManager;
import csu.agent.fb.tools.FbUtilities;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.util.Util;

public class DefaultFbActionStrategy extends fbActionStrategy{
	
	public DefaultFbActionStrategy(FireBrigadeWorld world, 
			DirectionManager directionManager, FbUtilities fbUtil) {
		super(world, directionManager, fbUtil);
		this.setTargetSelector();
		this.setExtinguishBehavior();
	}

	@Override
	public void execute() throws ActionCommandException, TimeOutException {
		if (isTimeToRefreshEstimator())
			FbUtilities.refreshFireEstimator(world);
	
		FireBrigadeTarget fbTarget = this.targetSelector.selectTarget();
		///FireBrigadeTarget fbTarget = null;
///		Logger.info("info..........only update the fireEstimator in execute(), and then move to fires if not timeout");
///		Logger.debug("debug...........only update the fireEstimator in execute(), and then move to fires if not timeout");
///		Logger.warn("warn..........only update the fireEstimator in execute(), and then move to fires if not timeout");
///		Logger.trace("trace..........only update the fireEstimator in execute(), and then move to fires if not timeout");
/// 	Logger.fatal("fatal..........only update the fireEstimator in execute(), and then move to fires if not timeout");

	   if (fbTarget != null) {                                                               ///NullPointerException
		   if(AgentConstants.FB)
			   System.out.println(world.getTime() + ", " + world.me + ", extinguish in execute");
		   this.extinguishBehavior.extinguish(world, fbTarget);
		} else {
			if(AgentConstants.FB)  ///world.me or controlledEntity
				System.out.println(world.getTime() + ", " + world.me + ", no target in execute" );
			
		}
	}

	@Override
	public ActionStrategyType getFbActionStrategyType() {
		return ActionStrategyType.DEFAULT;
	}
	
	@Override
	public void moveToFires() throws ActionCommandException {
		this.lastTarget = this.target; ///
		CSUBuilding csuBuilding;
		csuBuilding = targetSelector.getOverallBestBuilding(Util.burnBuildingToCsuBuilding(world));
		
		if(csuBuilding != null) {
			this.target = csuBuilding;///
			List<EntityID> path;
			path = world.getRouter().getAStar(underlyingAgent.location(), csuBuilding.getSelfBuilding(), 
					new Point(world.getSelfLocation().first(), world.getSelfLocation().second()));
			///the path has at lease one id, the start meaning the current position
			int pathSize = path.size(); 
			path.remove(pathSize - 1); ///the path include the start, exclude the destination
			if(AgentConstants.FB) {
				System.out.println(world.getTime() +  ", "  + world.me + ",  move to fires");
				System.out.println(csuBuilding + "" + csuBuilding.getEstimatedTemperature());
				System.out.println(csuBuilding.getSelfBuilding() + "" + csuBuilding.getSelfBuilding().getTemperature() );
				System.out.println("the path: " + path);
			}
			///test:   fbTarget getCsuBuilding is null ,the cluster is dying, but there are still building to extinguish
			if(path.size() == 1 && world.getSelfPosition().getID().equals(path.get(0))) {
				if(AgentConstants.FB)
					System.out.println(world.getTime() +  ", "  + world.me + ", at the location to extinguish");
			    underlyingAgent.extinguish(csuBuilding.getId());
			}
			else
			underlyingAgent.moveOnPlan(path);
		}
		else if(AgentConstants.FB)
			System.out.println(world.getTime() +  ", "  + world.me + ", " + "getOverallBestBuillding is null, move to fires()");
			
	}
	
	private boolean isTimeToRefreshEstimator() {
		if (world.getTime() > 120 && world.getTime() % 30 == 0)
			return true;
		return false;
	}
	
	private void setTargetSelector() {
		switch (this.targetSelectorType) {
		case DIRECTION_BASED:
			targetSelector = new DirectionBasedTargetSelector(this.world);
			break;
		case DIRECTION_BASED_OLD:
			targetSelector = new DirectionBasedTargetSelector_OLD(this.world);
		///oak
		case MUITIFACTORS:
			targetSelector = new MuitiFactorsTargetSeletor(this.world);
			break;
		default:
			targetSelector = new DirectionBasedTargetSelector_OLD(this.world);
			break;
		}
	}
	
	private void setExtinguishBehavior() {
		switch (this.extinguishBehaviorType) {
		case CLUSTER_BASED:
			extinguishBehavior = new DirectionBasedExtinguishBehavior(world);
			break;
		case MUTUAL_LOCATION:
			extinguishBehavior = new DirectionBasedExtinguishBehavior(world);
			break;
		case CSU_OLD_BASED:
			extinguishBehavior = new CsuOldBasedExtinguishBehavior(world);
			break;
	///	case GREEDY:
		///	extinguishBehavior = new GreedyExtinguishBehavior(world); 
		default:
			extinguishBehavior = new DirectionBasedExtinguishBehavior(world);
			break;
		}
	}

	public ExtinguishBehavior_Interface getExtinguishBehavior() {
		return this.extinguishBehavior;
	}
}
