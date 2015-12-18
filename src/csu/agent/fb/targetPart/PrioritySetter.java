package csu.agent.fb.targetPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rescuecore.CenterAgent;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.GasStation;


import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;
import firesimulator.world.Building;

//oak, sos
public class PrioritySetter extends TargetSelector {
	public int[] fireness = { 2, 3, 3, -1, 1, 1, 1, 0, -1 }; 
	private static final double MEAN_VELOCITY_DISTANCE = 31445.392; ///15700
	
	public PrioritySetter(FireBrigadeWorld world) {
		super(world);
		// TODO Auto-generated constructor stub
	}
	 
	
	public void setPriority(CSUBuilding bu) {
        bu.priority = 0;
        
		setPriorityForAllFiery(bu, 500);///oak
		setPriorityForFirey1(bu, 2000); ///1000
		setPriorityForDistance(bu, -250);
		///setPriorityForImportance(bu, 200);//added
		if(bu.getSelfBuilding() instanceof GasStation)
			setPriorityForGS(bu, 500);
		if(bu.isBurning()) {
			setPriorityForNearGS(bu, 1000);///500 
			setPriorityForIgniteTime(bu, 500);
			setPriorityForNeighborsUnBurning(bu, 50);
			setPriorityForBigArea(bu, -1000);
			setPriorityForOutFire(bu, 1500); ///added
			setPriorityForMapBorder(bu, -500); ///added
		}
		else{
			P_setProrityForLargNearSmallFire(bu, 100);
			P_setPriorityForBigArea(bu);
			P_setPriorityForCriticalTemprature(bu, 200);
			P_setPriorityForNearGS(bu, 500);
			if (bu.getSelfBuilding() instanceof GasStation)
				P_setPriorityForGS(bu, 1000);
		}
		
		EXTsetPriorityForRandombld(bu, 50);// select random CSUBuilding by sum of CSUBuilding ID and Fire Brigade ID
		
		bu.BUILDING_VALUE = bu.priority;
}



/**-------------------------------------------------------------------------------------------------------------------------------*/
protected void setPriorityForAllFiery(CSUBuilding bu, int i) {
	addPriority(bu, i * fireness[bu.getEstimatedFieryness()]);
}

private void setPriorityForFirey1(CSUBuilding bu, int i) {
	if (bu.getEstimatedFieryness() != 1)
		return;
	if (world.getTime() - bu.getIgnitionTime() > 5)
		return;
	if(world.isNoRadio()) 
		addPriority(bu, i);
	else
		addPriority(bu, i/2);
}

protected void setPriorityForDistance(CSUBuilding bu, int priority) {
	double timecost = world.getDistance(this.agentId, bu.getId()) /  MEAN_VELOCITY_DISTANCE;
	addPriority(bu, (int) (timecost * priority));

}

protected void setPriorityForGS(CSUBuilding c, int priority) {
	if (c.getEstimatedTemperature() >= 10 && (c.getEstimatedFieryness() == 0 || c.getEstimatedFieryness() == 4)) {
		addPriority(c, priority);
	}
}


//private void setPriorityForImportance(CSUBuilding bu, int i) {
//	if((bu.getSelfBuilding()).isImportanceDefined())
//		addPriority(bu, i);
//	}

/**-------------------------------------------------------------------------------------------------------------------------------*/
private void setPriorityForNearGS(CSUBuilding bu, int priority) {
	for (CSUBuilding n : bu.getConnectedBuildings()) {
		if (n.getSelfBuilding() instanceof GasStation) {
			addPriority(bu, priority);
		}
	}

}
protected void setPriorityForIgniteTime(CSUBuilding bu, int priority) {
	if (bu.getEstimatedFieryness() != 1)
		return;
	
	if (bu.isBurning() && bu.getExtinguishableCycle() == 1) {
		if (world.getDistance(this.agentId, bu.getId()) < world.getConfig().extinguishableDistance) {
			addPriority(bu, priority);
		}
		else {
			addPriority(bu, priority / 2);
		}
	}
	if(bu.isBurning() && world.getTime() - bu.getIgnitionTime() > 5)
		addPriority(bu, -1*priority / 2);
}

protected void setPriorityForNeighborsUnBurning(CSUBuilding bu, int i) {
	int num = 0;
	for (CSUBuilding n : bu.getConnectedBuildings()) {
		if (n.getEstimatedFieryness() == 0 || n.getEstimatedFieryness() == 4) {
			num++;
		}
	}
	addPriority(bu, num * i);

}

protected void setPriorityForBigArea(CSUBuilding bu, int i){
	if (! bu.isBigBuilding())
		return;
	
	if (bu.getEstimatedTemperature() < 500 )
		return;
			
	if(bu.getEstimatedFieryness()==1 || bu.getEstimatedFieryness()==2 )
		addPriority(bu, i);
	else
		addPriority(bu, i/2);
	
}

private void setPriorityForOutFire(CSUBuilding bu, int i) {
	if(bu.isOutFire() && bu.getEstimatedFieryness() == 1)
		addPriority(bu, i+500);
	else
		addPriority(bu, i); 
		
	}

private void setPriorityForMapBorder(CSUBuilding bu, int i) {
	if(world.getBorderBuildings().contains(world.getEntity(bu.getId())))
		addPriority(bu, i);
	else
		return;
}
/**-------------------------------------------------------------------------------------------------------------------------------*/
protected void P_setProrityForLargNearSmallFire(CSUBuilding bu, int priority) {
	double num = 0;
	if (! bu.isBigBuilding())
		return;
	for (CSUBuilding n : bu.getConnectedBuildings()) {
		if (bu.isBurning()) {
			double d = bu.getSelfBuilding().getGroundArea()/ n.getSelfBuilding().getGroundArea();
			if (d >= 3d) {
				num += d / 3;
			}
		}
	}
	addPriority(bu, (int) (num * priority));
}

protected void P_setPriorityForBigArea(CSUBuilding bu) {
	if (bu.getEstimatedTemperature() > 30) {		
		if (bu.isBigBuilding()) {
			if (world.getDistance(this.agentId, bu.getId()) < world.getConfig().extinguishableDistance) {
				addPriority(bu, 1000);
			}
			else {
				addPriority(bu, 700);
			}
		}
	}

}

private void P_setPriorityForNearGS(CSUBuilding bu, int priority) {
	if (bu.getEstimatedFieryness() < 35)
		return;

	for (CSUBuilding n : bu.getConnectedBuildings()) {
		if (n.getSelfBuilding() instanceof GasStation) {
			addPriority(bu, priority);
		}
	}
}

@SuppressWarnings("unchecked")
protected void P_setPriorityForCriticalTemprature(CSUBuilding n, int priority) {

	if (!(n.getEstimatedTemperature() > 30 && n.getEstimatedTemperature() < 50))
		return;

	if (world.getDistance(n.getId(), world.getMapCenter().getID()) >  2/3 *Math.hypot(world.getMapHeight(), world.getMapWidth()))
		return;

	if (n.getEstimatedTemperature() <= 40)
		return;

	if (!this.underlyingAgent.getVisibleEntities().contains(n))
		return;

	if (world.getDistance(this.agentId, n.getId())>world.getConfig().extinguishableDistance)
		return;

	if (n.getSelfBuilding().getTotalArea() > 3000)
		priority *= 2;

	addPriority(n, priority);

}

protected void P_setPriorityForGS(CSUBuilding c, int priority) {
	if (c.getEstimatedTemperature() >= 20 && (c.getEstimatedFieryness() == 0 || c.getEstimatedFieryness() == 4)) {
		addPriority(c, priority);
	}
}
/**-------------------------------------------------------------------------------------------------------------------------------*/
private void EXTsetPriorityForRandombld(CSUBuilding bu, int i) {
	int Random = world.me.getID().getValue() + bu.getId().getValue();
	int x = Random % 10;
	addPriority(bu, i * x);
}
/**-------------------------------------------------------------------------------------------------------------------------------*/

private void addPriority(CSUBuilding bu, int priority) {
	bu.addPriority(priority);
}
	
	@Override
	public FireBrigadeTarget selectTarget() throws ActionCommandException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CSUBuilding selectTargetWhenStuck(
			Collection<CSUBuilding> burnBuildings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CSUBuilding getOverallBestBuilding(
			Collection<CSUBuilding> burnBuildings) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public CSUBuilding getBestTarget(List<CSUBuilding> buildings) {
		double max = Integer.MIN_VALUE;
		CSUBuilding best = null;

		for (CSUBuilding e : buildings) {
			if (e.priority > max) {
				best = e;
				max = e.priority;
			}
		}
		return best;
	}
	

}
