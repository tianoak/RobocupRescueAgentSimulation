package csu.agent.fb.targetPart;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.tools.MAAbstractUtility;
import csu.agent.fb.tools.MAFirstUtility;
import csu.agent.fb.tools.MASecondUtility;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;
import rescuecore2.worldmodel.EntityID;

public class MATargetSelector extends TargetSelector {

	public MATargetSelector(FireBrigadeWorld world) {
		super(world);
		// TODO Auto-generated constructor stub
	}

	public CSUBuilding getOverallBestBuilding(
			Collection<CSUBuilding> burnBuildings) {
				return null;}

	@Override
	public FireBrigadeTarget selectTarget() throws ActionCommandException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CSUBuilding selectTargetWhenStuck(
			Collection<CSUBuilding> burnBuildings) {
		System.out.println("using new targetSelector when stucked");
		Random rand = new Random();
		int r = rand.nextInt(2);
		MAAbstractUtility utility;
		switch (r) {
		case 0:
			utility = new MAFirstUtility();
			utility.setWorld(world);
			break;
		case 1:
		default:
			utility = new MASecondUtility();
			utility.setWorld(world);
		}

		double best = -Double.MAX_VALUE;
		CSUBuilding target = lastTarget;///maybe better
		for (CSUBuilding fire : burnBuildings) {
			EntityID t = fire.getId();
			// final double util =
			// first.getFireUtility(controlledEntity.getID(), t);
			final double util = utility.getFireUtility(
					controlledEntity.getID(), t);
			if (util > best) {
				best = util;
				target = fire;
			}
		}
		return target;
	}

}
