package csu.agent.fb.actionStrategy;

import java.util.EnumSet;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.extinguishBehavior.ExtinguishBehavior_Interface;
import csu.agent.fb.tools.DirectionManager;
import csu.agent.fb.tools.FbUtilities;
import csu.common.TimeOutException;
import csu.model.route.pov.CostFunction;

/**
 * The old action stratege which used the energy flow to help dispatch.
 * 
 * @author appreciation-csu
 *
 */
public class CsuOldBasedActionStrategy extends fbActionStrategy{

	public CsuOldBasedActionStrategy(FireBrigadeWorld world, 
			DirectionManager directionManager, FbUtilities fbUtil) {
		super(world, directionManager, fbUtil);
	}

	@Override
	public ExtinguishBehavior_Interface getExtinguishBehavior() {
		return null;
	}

	@Override
	public ActionStrategyType getFbActionStrategyType() {
		return ActionStrategyType.CSU_OLD_BASED;
	}

	@Override
	public void execute() throws ActionCommandException, TimeOutException {
		this.giveUpPosition();
		this.visibleExtingish();
		if (world.isNoRadio())
			this.radioExtingish();
		moveToFires();
	}
	
	@Override
	public void extinguishNearbyWhenStuck() throws ActionCommandException, TimeOutException{
		this.errorExtinguish();
	}

	private void giveUpPosition() throws ActionCommandException {
		if (isAllGiveUp()) {
			System.out.println("Agent: " + controlledEntity + " stay in its position and extinguish in " +
					"time: " + world.getTime() + 
					" ----- class: CsuOldBasedActionStrategy, method: giveUpPosition()");
			return;
		}
		for (EntityID id : underlyingAgent.getVisibleEntities()) {
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if (!giveUpFieryness.contains(building.getFierynessEnum())) {
					System.out.println("Agent: " + controlledEntity + " do not give up his position in " +
							"time: " + world.getTime() + 
							" ----- class: CsuOldBasedActionStrategy, method: giveUpPosition()");
					return;
				}
			}
		}
		System.out.println("Agent: " + controlledEntity + " give up his position and move to new fires in " +
				"time: " + world.getTime() + " ----- class: FireBrigadeAgent, method: giveUpPosition()");
		this.moveToFires();
	}	
	
	private boolean isAllGiveUp() {
		for (Building building : world.getBurningBuildings()) {
			if (!giveUpFieryness.contains(building.getFierynessEnum())) {
				return true;
			}
		}
		return false;
	}
	
	/** Building with those fieryness will be give up.*/
	private static final EnumSet<Fieryness> giveUpFieryness = EnumSet.of(
			Fieryness.BURNT_OUT,
			Fieryness.SEVERE_DAMAGE
	);
	
	private void visibleExtingish() throws ActionCommandException {
		/* the building with minimum extinguish difficulty.*/
		Building minDifficultyBuilding = null;
		/* the minimum extinguish difficulty*/
		double minDifficulty = Integer.MAX_VALUE;
		/* the energy minDifficultyBuilding released to outside world*/
		double minDifficultyAffect = 0.0;
		
		for (EntityID id : underlyingAgent.getVisibleEntities()) {
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building){
				Building building = (Building) se;
				/* total area of this building*/
				double area = (building.isTotalAreaDefined()) ? building.getTotalArea() : 1.0;
				/* the energy gained from outside world of this building*/
				double affected = world.getEnergyFlow().getIn(building);
				/* the extinguish difficulty of this building*/
				double difficulty = area * affected;
				
				if (world.getBurningBuildings().contains(building) && difficulty < minDifficulty) {
					minDifficultyBuilding = building;
					minDifficulty = difficulty;//
					minDifficultyAffect = world.getEnergyFlow().getOut(building);
				}else if (Math.abs(minDifficulty - difficulty) < 500.0) {
					/* 
					 * If two building has colser extinguish difficulty, then compare the energy they
					 * released to outside world. The building which release more energy is the new
					 * minimum extinguish difficulty building.
					 */
					double affect = world.getEnergyFlow().getOut(building);
					if (minDifficultyAffect < affect) {
						minDifficultyBuilding = building;
						minDifficulty = difficulty;
						minDifficultyAffect = world.getEnergyFlow().getOut(building);
					}
				}
			}
		}
		if (minDifficultyBuilding != null) {
			/* extinguish the minDifficultyBuilding*/
			System.out.println("Agent: " + controlledEntity + " is visible extinguishing in time: " 
			       + world.getTime() + " ----- class: CsuOldBasedActionStrategy, method: visibleExtinguish()");
			underlyingAgent.extinguish(minDifficultyBuilding);
		}
		System.out.println("Agent: " + controlledEntity + " has no visible building to extinguish in time: " + 
		     world.getTime() + " ----- class: CsuOldBasedActionStrategy, method: visibleExtinguish()");
	}

	private void radioExtingish() throws ActionCommandException {
//		Blockade blockade = world.getStuckHandle().isLocateInBlockade(world.getSelfLocation());
//		final int time = (blockade != null) ? 8 : 3;
		
		Building minDifficultyBuilding = null;
		double minDifficulty = Integer.MAX_VALUE;
		double minDifficultyAffect = 0.0;
		
		for (Building building : world.getBurningBuildings()) {
			if (!underlyingAgent.isExtinguishable(building)
					|| world.getTime() - world.getTimestamp().getLastChangedTime(building.getID()) >= 3) {
				continue;
			}
			if (building.isFierynessDefined() && building.getFierynessEnum() == Fieryness.INFERNO) {
				continue;
			}
			double area = (building.isTotalAreaDefined()) ? building.getTotalArea() : 1.0;
			double affected = world.getEnergyFlow().getIn(building);
			double difficulty = area * affected;
			
			if (world.getBurningBuildings().contains(building) && difficulty < minDifficulty) {
				minDifficultyBuilding = building;
				minDifficulty = difficulty;
				minDifficultyAffect = world.getEnergyFlow().getOut(building);
			} else if (Math.abs(minDifficulty - difficulty) < 500.0) {
				double affect = world.getEnergyFlow().getOut(building);
				if (minDifficultyAffect < affect) {
					minDifficultyBuilding = building;
					minDifficulty = difficulty;
					minDifficultyAffect = world.getEnergyFlow().getOut(building);
				}
			}
		}
		if (minDifficultyBuilding != null) {
			System.out.println("Agent: " + controlledEntity + " is radio extinguishing a building in time: " 
		           + world.getTime() +  " ----- class: CsuOldBasedActionStrategy, method: radioExtinguish()");
			underlyingAgent.extinguish(minDifficultyBuilding);
		}
		System.out.println("Agent: " + controlledEntity + " has no radio building to extinguish in time: " 
		       + world.getTime() + " ----- class: CsuOldBasedActionStrategy, method: radioExtinguish()");
	}
	
	public void moveToFires() throws ActionCommandException {
		if (world.getBurningBuildings().isEmpty()){
			System.out.println("Agent: " + controlledEntity + " has no burning building in his world model " +
				"----- time: " + world.getTime() + ", class: CsuOldBasedActionStrategy, method: moveToFires()");
			return;
		}
		
		Building minValueBuilding = null;
		double minValue = Integer.MAX_VALUE;
		
		for (Building building : world.getBurningBuildings()) {
			final double affect = world.getEnergyFlow().getOut(building);
			final double distance = world.getDistance(building, controlledEntity);
			final double value = affect * distance;
			
			if (value < minValue) {
				minValueBuilding = building;
				minValue = value;
			}
		}
		if (minValueBuilding != null) {
			System.out.println("Agent: " + controlledEntity + " moving to a burning building in his world model " +
					"----- time: " + world.getTime() + ", class: CsuOldBasedActionStrategy, method: moveToFires()");
			CostFunction costFunction = underlyingAgent.getRouter().getFbCostFunction(minValueBuilding);
			underlyingAgent.moveFront(minValueBuilding, costFunction);
		}
		System.out.println("In time: " + world.getTime() + " Agent: " + controlledEntity + " can not find " +
				"a burning building to move. ----- class: CsuOldBasedActionStrategy, method: moveToFires()");
	}
	
	/** 
	 * errorExtinguish can ensure Agents to extinguish fires near him when he was stucked.
	 * Otherwise, Agents just trying to go out and do nothing else. That is bad when there
	 * are fires around him.
	 */
	private void errorExtinguish() throws ActionCommandException {
		final int extinguishableDistance = world.getConfig().extinguishableDistance;
		for (StandardEntity se : world.getObjectsInRange(controlledEntity, extinguishableDistance)) {
			if (se instanceof Building) {
				Building building = (Building) se;
				if (building.getFieryness() > 0 && building.getFieryness() < 4) {
					System.out.println("Agent: " + controlledEntity + " is error extinguishing in time: " 
						     + world.getTime() + " ----- class:CsuOldBasedActionStrategy, method: errorExtinguish()");
					underlyingAgent.extinguish(building);
				}
			}
			System.out.println("In time: " + world.getTime() + " Agent: " + controlledEntity + " can not " +
					"error extinguish and leave.  ----- class:CsuOldBasedActionStrategy, method: errorExtinguish()");
			// underlyingAgent.move(underlyingAgent.getCannotLeaveBuildingEntrance());
		}
	}
}
