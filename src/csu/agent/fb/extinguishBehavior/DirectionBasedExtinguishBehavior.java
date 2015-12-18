package csu.agent.fb.extinguishBehavior;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javolution.util.FastMap;
import javolution.util.FastSet;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.GasStation;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeAgent;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.targetPart.FireBrigadeTarget;
import csu.agent.fb.tools.FbUtilities;
import csu.common.TimeOutException;
import csu.common.clustering.FireCluster;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.model.object.CSURoad;
import csu.model.route.pov.CostFunction;
import csu.util.Util;

public class DirectionBasedExtinguishBehavior implements ExtinguishBehavior_Interface{

	private int waterPower;
	private CSUBuilding target;
	private FireBrigadeWorld world;
	
	private FireBrigadeAgent underlyingAgent;
	private FireBrigade controlledEntity;
	private EntityID agentId;
	
	protected Map<Integer, CSUBuilding> extinguishTargetAndTimeMap = new FastMap<>();
	
	public DirectionBasedExtinguishBehavior(FireBrigadeWorld world) {
		this.world = world;
		this.underlyingAgent = (FireBrigadeAgent) world.getAgent();
		this.controlledEntity = (FireBrigade)world.getControlledEntity();
		this.agentId = world.getAgent().getID();
		this.target = this.underlyingAgent.getThisFire();///oak
	}
	
	@Override
		
	public void extinguish(FireBrigadeWorld model, FireBrigadeTarget fbTarget) throws ActionCommandException, TimeOutException {
//		currentLocationExtinguish();
		
		if (fbTarget == null) {
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity + 
						" has no target to extinguish.");
				System.out.println("------DirectionBasedExtinguishBehavior: extinguish()");
			}
			return;
		}
		
		if (fbTarget.getCsuBuilding() == null) { 
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity 
						+ " will searching dying cluster"); 
				System.out.println("------DirectionBasedExtinguishBehavoir: extinguish()");
			}
			searchDyingCluster((FireCluster)fbTarget.getCluster());///
			return;
		}
	
		this.target = fbTarget.getCsuBuilding();
		
		EntityID targetId = fbTarget.getCsuBuilding().getId();
		
		int distanceToTarget = model.getDistance(this.agentId, targetId);
		int locationDistanceToTarget = model.getDistance(controlledEntity.getPosition(), targetId);
		int maxExtinguishDistance;
		
		if (target.getSelfBuilding() instanceof GasStation) {
			maxExtinguishDistance = model.getConfig().viewDistance;
		} else {
			maxExtinguishDistance = model.getConfig().extinguishableDistance;
		}
		
		if (distanceToTarget > maxExtinguishDistance && locationDistanceToTarget > maxExtinguishDistance) {
			StandardEntity locationToGo = this.chooseBestLocationToExtinguish(model, fbTarget, controlledEntity);
			if (locationToGo != null 
					/*&& underlyingAgent.getRouter().isReachable(locationToGo.getID())*/) {
				List<EntityID> path = underlyingAgent.getPath(underlyingAgent.location(), 
						(Area)locationToGo, world.getRouter().getNormalCostFunction());
				if (path != null) {
					if (path.size() > 1) {
						if (AgentConstants.FB) {
							System.out.println(world.getTime() + ", " + world.getControlledEntity() 
									+ " move to best location: " + locationToGo);
							System.out.println(" ------ DirectionBasedExtinguishBehavior: extinguish");
						}
						this.underlyingAgent.moveOnPlan(path);
					} else {
						// lastTryToExtinguish(model, agentId, 1);
						currentLocationExtinguish(1);
					}
				}
			} else {
				// this.lastTryToExtinguish(model, agentId, 2);
				currentLocationExtinguish(2);
			}
			
		} else {
			Set<EntityID> forbiddenLocations = this.getForbiddenLocation(model, fbTarget, agentId);
			if (!forbiddenLocations.isEmpty() && forbiddenLocations.contains( this.underlyingAgent.location().getID())) {
				StandardEntity newLocation = this.chooseBestLocationToExtinguish(model, fbTarget, controlledEntity);
				if (newLocation != null) {
					if (AgentConstants.FB) {
						System.out.println(world.getTime() + ", "
										+ world.getControlledEntity()
										+ " in forbidden location, and move to: " + newLocation);
						System.out.println("------DirectionBasedExtinguishBehavior: extinguish" );
					}
					this.underlyingAgent.newMove((Area)newLocation, world.getRouter().getNormalCostFunction());
				}
			}
			
			if (this.controlledEntity.getWater() != 0) {
				this.waterPower = FbUtilities.calculateWaterPower(model, fbTarget.getCsuBuilding());
				fbTarget.getCsuBuilding().setWaterQuantity(fbTarget.getCsuBuilding().getWaterQuantity() + waterPower);
				
				extinguishTargetAndTimeMap.put(world.getTime(), fbTarget.getCsuBuilding());
				
				if (AgentConstants.FB) {
					System.out.println(world.getTime() + ", " + world.getControlledEntity() 
							+ " is extinguish: " + fbTarget.getCsuBuilding().getSelfBuilding());
					System.out.println("------ DirectionBasedExtinguishBehavior: extinguish");
				}
				this.underlyingAgent.extinguish(fbTarget.getCsuBuilding().getId(), waterPower);
			} else {
				if (AgentConstants.FB) {
					System.out.println(world.getTime() + ", " + controlledEntity + 
							" is out of water, and can not to extinguish");
					System.out.println("------ DirectionBasedExtinguishBehavior: extinguish");
			}
			}
		}
		underlyingAgent.isThinkTimeOver("extinguishTarget");
	}
	
	private void searchDyingCluster(FireCluster cluster) throws ActionCommandException, TimeOutException {
		if (AgentConstants.FB) {
			System.out.println(world.getTime() + ", " + world.getControlledEntity() 
					+ " is searching dying cluster");
			System.out.println("------DirectionBasedExtinguishBehavior: searchDyingCluster");
		}
		cluster.removeFromAllEntities(lookedBuildings());
		Set<EntityID> dyingClusterEntities = cluster.getAllEntities();

		if (dyingClusterEntities.isEmpty())
			return;

		Set<StandardEntity> dest = new FastSet<>();
		for (EntityID id : dyingClusterEntities) {
			dest.add(world.getEntity(id));
		}

		Point selfL = new Point(controlledEntity.getX(), controlledEntity.getY());
		CostFunction costFun = underlyingAgent.getRouter().getNormalCostFunction();
		Area loca = (Area) world.getSelfPosition();

		List<EntityID> path = underlyingAgent.getRouter().getMultiDest(loca, dest, costFun, selfL);
		EntityID destId = path.get(path.size() - 1);
		double cost = underlyingAgent.getRouter().getRouteCost(); ///routeCost get from getMultiDest
		double dis = world.getDistance(loca.getID(), destId);

		if (cost > 10 * dis) {
			cluster.removeFromAllEntities(unreachables(world.getEntity(destId, Area.class)));
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity + 
						" is searching a unreachable target: "+destId);
				System.out.println(" ------DirectionBasedExtinguishBehavior: searchDyingCluster()");
			}
		} else {
			
			if (AgentConstants.FB) {
				String str = null;
				for (EntityID next : path) {
					if (str == null)
						str = next.getValue() + "";
					else
						str = str + ", " + next.getValue();
				}
				
				System.out.println(world.getTime() + ", " + world.getControlledEntity()
						+ " is searching dying cluster. path = [" + str + "]");
				System.out.println("------DirectionBasedExtinguishBehavior: searchDyingCluster");
			}
			
			this.underlyingAgent.move(path);
		}
	}
	
	private List<EntityID> lookedBuildings() {
		List<EntityID> result = new ArrayList<>();

		Human agent;
		for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
			agent = (Human) next;

			if (agent.getID().getValue() == underlyingAgent.getID().getValue()) {
				for (EntityID visible : underlyingAgent.getChanged()) {
					StandardEntity v_entity = world.getEntity(visible);
					if (v_entity instanceof Building) {
						result.add(visible);
					}
				}
			}
			
			if (agent.isPositionDefined())
				result.add(agent.getPosition());

			/*if (agent.isPositionDefined()) {
				StandardEntity position = world.getSelfPosition();
				List<EntityID> visible = null;
				if (position instanceof Building) {
					CSUBuilding csu_b = world.getCsuBuilding(position.getID());
					visible = csu_b.getObservableAreas();
					result.add(agent.getPosition());

				}
				if (position instanceof Road) {
					CSURoad csu_r = world.getCsuRoad(position.getID());
					visible = csu_r.getObservableAreas();
				}

				if (visible != null) {
					for (EntityID visible_id : visible) {
						StandardEntity v_entity = world.getEntity(visible_id);
						if (v_entity instanceof Building) {
							result.add(visible_id);
						}
					}
				}
			}*/
		}

		return result;
	}
	
	private Set<EntityID> unreachables(Area area) {
		Set<EntityID> result = new FastSet<>();
		
		if (area instanceof Building) {
			for (Road entrance : world.getEntrance().getEntrance((Building)area)) {
				for (Building next : world.getEntrance().getBuilding(entrance)) {
					result.add(next.getID());
				}
			}
		} else {
			List<Building> bu_s = world.getEntrance().getBuilding((Road)area);
			if (bu_s != null) {
				for (Building next : bu_s) {
					result.add(next.getID());
				}
			}
		}
		
		return result;
	}
	
	/** 
	 * This method handle the case when fb was stucked. I write this method here for the simplicity
	 * of communication handling.
	 */
	public void extinguishNearbyWhenStuck(CSUBuilding targetBuilding) throws ActionCommandException, TimeOutException {
		underlyingAgent.isThinkTimeOver("extinguishNearbyWhenStuck");
		if (targetBuilding == null)
			return;
		this.target = targetBuilding;
		this.waterPower = FbUtilities.calculateWaterPower(world, target);
		targetBuilding.setWaterQuantity(targetBuilding.getWaterQuantity() + waterPower);
		extinguishTargetAndTimeMap.put(world.getTime(), targetBuilding);
		
		if (AgentConstants.FB) {
			System.out.println(world.getTime() + ", agent: " + world.getControlledEntity() 
					+ " do stucked extinguish: " + target.getSelfBuilding());
			System.out.println(" ------DirectionBasedExtinguishBehavior: extinguishNearbyWhenStuck");
		}
		
		this.underlyingAgent.extinguish(targetBuilding.getId(), waterPower);
	}
	
	private StandardEntity chooseBestLocationToExtinguish(FireBrigadeWorld model, FireBrigadeTarget target, FireBrigade fbAgent) {
		double minDistance = Double.MAX_VALUE;
		double distance;
		Set<EntityID> forbiddenLocations = this.getForbiddenLocation(model, target, fbAgent.getID());
		Set<EntityID> possibleArea = new FastSet<>();
		StandardEntity locationToExtinguish = null;
		
		Collection<EntityID> areaInExtinguishableRange = target.getCsuBuilding().getAreasInExtinguishableRange();
		possibleArea.addAll(areaInExtinguishableRange);
		possibleArea.removeAll(forbiddenLocations);
		
		/*if (count > TIME_LOCK) {
			if (lastLoatLocationToGo != null 
					&& possibleArea.contains(lastLoatLocationToGo.getID())) {
				count ++;
				return lastLoatLocationToGo;
			}
		}*/
		
		
		if (possibleArea.isEmpty())
			possibleArea.addAll(areaInExtinguishableRange);
		
		Set<Building> possibleBuildingsToExtinguish = new FastSet<>();
		for (EntityID next : possibleArea) {
			StandardEntity entity = model.getEntity(next);
			if (entity instanceof Road) {
				// distance = model.getDistance(fbAgent.getPosition(), next);
				distance = model.getDistance(target.getCsuBuilding().getId(), entity.getID());
				if (distance < minDistance) {
					minDistance = distance;
					locationToExtinguish = entity;
				}
			} else if (entity instanceof Building) {
				possibleBuildingsToExtinguish.add((Building) entity);
			}
		}
		if (locationToExtinguish == null) {
			for (Building next : possibleBuildingsToExtinguish) {
				//distance = model.getDistance(fbAgent.getPosition(), next.getID());
				distance = model.getDistance(target.getCsuBuilding().getId(), next.getID());
				boolean flag = next.isFierynessDefined() && next.getFieryness() >= 1 && next.getFieryness() < 4;///
				if (distance < minDistance && (!next.isFierynessDefined() || !flag)) { ///test: || flag
					minDistance = distance;
					locationToExtinguish = next;
				}
			}
		}
		
		/*lastLoatLocationToGo = locationToExtinguish;
		count = 0;*/
		return locationToExtinguish;
	}
	
	public void currentLocationExtinguish(int marker) throws ActionCommandException {
		SortedSet<CSUBuilding> inRanges = new TreeSet<>(new Comparator<CSUBuilding>() {

			@Override
			public int compare(CSUBuilding o1, CSUBuilding o2) {
				if (o1.BUILDING_VALUE > o2.BUILDING_VALUE)
					return 1;
				if (o1.BUILDING_VALUE < o2.BUILDING_VALUE)
					return -1;
				return 0;
			}
		});
		
		inRanges.addAll(FbUtilities.getBuildingInExtinguishableRange(world, controlledEntity.getID()));
		
		if (inRanges.isEmpty())
			return;
		CSUBuilding target = inRanges.first();
		if (target.getEstimatedFieryness() > 0 && target.getEstimatedFieryness() < 4
				/*|| (target.getEstimatedFieryness() != 8 && target.getEstimatedTemperature() > 35)*/) {
			
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity + " is current " +
						"location extinguishing: " + target.getId());
				System.out.println("DirectionBasedExtinguishBehavior: currentLocationExtinguish(" + marker + ")");
						
			}
			
			this.waterPower = FbUtilities.calculateWaterPower(world, target); 
			target.setWaterQuantity(target.getWaterQuantity() + waterPower);
			extinguishTargetAndTimeMap.put(world.getTime(), target);
			underlyingAgent.extinguish(target.getId(), waterPower);
		} else {
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity + 
						" has no target in its extinguishable range");
				System.out.println("DirectionBasedExtinguishBehavior: lastTryToExtinguish()");
			}
		}
	}
	
	/*private void lastTryToExtinguish(FireBrigadeWorld model, EntityID fbAgent, int marker) throws ActionCommandException{
		Set<CSUBuilding> buildingsInRange = FbUtilities.getBuildingInExtinguishableRange(model, fbAgent);
		List<CSUBuilding> firedBuildingsInRange = new ArrayList<>();
		FireBrigade fireBrigade = world.getEntity(fbAgent, FireBrigade.class);
		
		for (CSUBuilding next : buildingsInRange) {
			if (next.getSelfBuilding().isOnFire())
				firedBuildingsInRange.add(next);
		}
		CSUBuilding tempTarget = 
				FbUtilities.getNearest(firedBuildingsInRange, fireBrigade.getLocation(model));
		if (tempTarget != null) {
			
			if (AgentConstants.FB) {
				System.out.println("time = " + world.getTime() + ", " + world.getControlledEntity() 
						+ " last try to extinguish: " + tempTarget.getSelfBuilding() + " ----- class: " 
						+ "DirectionBasedExtinguishBehavior, method: lastTryToExtinguish(" + marker + ")");
			}
			
			this.waterPower = FbUtilities.calculateWaterPower(model, tempTarget); 
			tempTarget.setWaterQuantity(tempTarget.getWaterQuantity() + waterPower);
			extinguishTargetAndTimeMap.put(world.getTime(), tempTarget);
			underlyingAgent.extinguish(tempTarget.getId(), waterPower);
		} else {
			if (AgentConstants.FB) {
				System.out.println("time = " + world.getTime() + ", " + controlledEntity + 
						" ???????????????????????????????????????" +
						"class:DirectionBasedExtinguishBehavior, method: lastTryToExtinguish()");
			}
		}
	}*/

	private Set<EntityID> getForbiddenLocation(FireBrigadeWorld model, FireBrigadeTarget target, EntityID fbAgent) {
		Set<EntityID> forbiddenLocations = new FastSet<>();
		
		if (target.getCluster() != null) {
			forbiddenLocations.addAll(target.getCluster().getAllEntities());
//			 forbiddenLocations.addAll(Util.entityToIds(target.getCluster().getEntities()));
		}
		// forbiddenLocations.addAll(Util.entityToIds(world.getBurningBuildings()));
		forbiddenLocations.addAll(Util.csuBuildingToId(world.getEstimatedBurningBuildings()));
		
		int number = 3;
		if (model.isMapMedium()) number = 10;
		if (model.isMapHuge()) number = 15;
		
		for(EntityID next : model.getFireBrigadeIdList()) {
			// -------------- ?????????????????????????? 
			if (fbAgent.getValue() < next.getValue() 
					&& model.getDistance(fbAgent, next) < model.getConfig().viewDistance
					&& --number < 0) {
				FireBrigade fireBrigade = model.getEntity(next, FireBrigade.class);
				StandardEntity entity = world.getEntity(fireBrigade.getPosition());
				if (entity instanceof Building) {
					CSUBuilding buildingOfNearFb = model.getCsuBuilding(entity.getID());
					forbiddenLocations.addAll(buildingOfNearFb.getObservableAreas());
				} else if (entity instanceof Road) {
					CSURoad roadOfNearFb = model.getCsuRoad(entity.getID());
					forbiddenLocations.addAll(roadOfNearFb.getObservableAreas());
				}
			}
		}
		
		return forbiddenLocations;
	}
	public EntityID getTargetInDBEXT() {
		if(this.target != null)
			return this.target.getId();
		else
			return null;
	}
}
