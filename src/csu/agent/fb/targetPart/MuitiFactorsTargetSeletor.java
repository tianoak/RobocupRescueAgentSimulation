package csu.agent.fb.targetPart;

import java.util.Collection;
import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javolution.util.FastSet;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.GasStation;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import csu.agent.fb.tools.FbUtilities;
import csu.common.clustering.FireCluster;
import csu.geom.CompositeConvexHull;
import csu.geom.ConvexObject;
import csu.model.AgentConstants;
import csu.model.object.CSURoad;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.model.route.pov.CostFunction;
import csu.standard.Ruler;
import csu.util.Util;

public class MuitiFactorsTargetSeletor extends PrioritySetter {

		private static final double MEAN_VELOCITY_DISTANCE = 31445.392; ///15700
		
		public MuitiFactorsTargetSeletor(FireBrigadeWorld world) {
			super(world);
		}

		@Override
		public FireBrigadeTarget selectTarget() throws ActionCommandException, TimeOutException{
		    for(CSUBuilding build : Util.burnBuildingToCsuBuilding(world))
		    	this.setPriority(build);
			CSUBuilding build = getBestTarget(Util.burnBuildingToCsuBuilding(world));
			return new FireBrigadeTarget(null, build);
		}
	
		
		@Override
		public CSUBuilding selectTargetWhenStuck(Collection<CSUBuilding> burnBuildings) {
			if (controlledEntity.getWater() < underlyingAgent.waterPower)
				return null;
			
			if (burnBuildings == null || burnBuildings.isEmpty())
				return null;

			CSUBuilding targetBuilding = this.getOverallBestBuilding(burnBuildings);
			return targetBuilding;
		}

		@Override
		public CSUBuilding getOverallBestBuilding(Collection<CSUBuilding> burnBuildings) {
			  for(CSUBuilding build : Util.burnBuildingToCsuBuilding(world))
			    	this.setPriority(build);
			CSUBuilding build = getBestTarget(Util.burnBuildingToCsuBuilding(world));
			return build;
		}
}

