package csu.common.clustering;

import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBuilding;

public class FireclusterMembershipCheckerEstimatorBased implements ClusterMembershipChecker_Interface{

	@Override
	public boolean membershipCheck(AdvancedWorldModel world, CSUBuilding csuBuilding) {
		boolean isMember = false;
		int estimatefFieryness = csuBuilding.getEstimatedFieryness();
		csuBuilding.BUILDING_VALUE = Double.MIN_VALUE;
		
		double estimateTemperature = csuBuilding.getEstimatedTemperature();
		if (csuBuilding.getSelfBuilding() != null && csuBuilding.isInflammable())
			if (estimatefFieryness != 8 && estimateTemperature > 47)
				isMember = true;
			else
				world.getBurningBuildings().remove(csuBuilding.getSelfBuilding());
		return isMember;
	}
}
