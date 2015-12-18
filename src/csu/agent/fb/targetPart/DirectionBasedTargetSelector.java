package csu.agent.fb.targetPart;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
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

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.tools.FbUtilities;
import csu.common.TimeOutException;
import csu.common.clustering.FireCluster;
import csu.geom.CompositeConvexHull;
import csu.geom.ConvexObject;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.model.object.CSURoad;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.model.route.pov.CostFunction;
import csu.standard.Ruler;
import csu.util.Util;
    
/**
 * First we choose the closest fire cluster from this Agent. Then we find the
 * expand direction of this fire cluster. We get buildings in that direction,
 * and with the border buildings of this fire cluster, we calculate their
 * buildingValues. For inDirectionBuildings and borderBuildings, we choose
 * differnet standard to calculate buildingValues. Very obvious,
 * inDirectionBuildings are more important. Finally, we sorted
 * inDirectionBuildings and borderBuildings together according to their
 * buildingValues and the first in this sorted set is the target building to
 * extinguish.
 * 
 * @author appreciation-csu
 * 
 */
/// the closest fire cluster
/// expand direction 
/// calculate buildingValues of inDirectionBuildings and borderBuildings
/// sort and select the best one

public class DirectionBasedTargetSelector extends TargetSelector {
	private static final double MEAN_VELOCITY_DISTANCE = 31445.392; ///15700
	private PrioritySetter prioritySetter;///
	
	public DirectionBasedTargetSelector(FireBrigadeWorld world) {
		super(world);
		this.prioritySetter = new PrioritySetter(world);/// 
	}

	@Override
	public FireBrigadeTarget selectTarget() throws ActionCommandException, TimeOutException{
		Pair<Integer, Integer> agentLocation = this.controlledEntity.getLocation(this.world);
		FireCluster targetCluster = this.world.getFireClusterManager().findNearestCluster(agentLocation);
		FireBrigadeTarget targetBuilding = null;
		this.lastTarget = this.target;

		if (targetCluster != null) {
			
			if (targetCluster.isDying()) {
				targetBuilding = new FireBrigadeTarget(targetCluster, null);
			} else {
				/*if (lastTarget != null
						&& targetCluster.getBorderEntities().contains(lastTarget.getSelfBuilding())) {
					targetBuilding = new FireBrigadeTarget(targetCluster, this.lastTarget);
					return targetBuilding;
				}*/
				
				targetBuilding = gasStationHander(targetCluster);
				if (targetBuilding != null) {
					return targetBuilding;
				}

				SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings;
				sortedBuildings = this.calculateValue(targetCluster);
				sortedBuildings = fbUtilities.reRankBuildings(sortedBuildings, controlledEntity);

				if (sortedBuildings != null && !sortedBuildings.isEmpty()) {
					if (AgentConstants.PRINT_SORTED_BUILDINGS_FB) {
						printSortedBuilding(sortedBuildings);
					}
					
					this.target = world.getCsuBuilding(sortedBuildings.first().first().first());
					targetBuilding = new FireBrigadeTarget(targetCluster, this.target);
				}
			}
		} else {
	    	
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity + " has no " +
						"target to handle(targer cluster is null))");
				System.out.println("------DirectionBasedTargetSelector: selectTarget");
			}
			
			if (lastTarget != null) {  
				for (CsuZone next : world.getZones()) {
					if (next.contains(lastTarget)) {
						searchZones(next);
						break;
					}
				}
			} else {
				if (AgentConstants.FB) {
					System.out.println(world.getTime() + ", " + controlledEntity 
							+ " last target is null");
					System.out.println("------DirectionBasedTargetSelector: selectTarget");
				}
			}
		}
		
		return targetBuilding;
	}
	/**
	 * choose gas stations whose fieriness == 0 but temperature >= 25 and water quantity < 3000
	 * and distance to the fire cluster <= 5000
	 * if including lastTarget, return it;
	 * otherwise, return the closet(Euler) to the fire brigade
	 * @param cluster
	 * @return
	 */
	private FireBrigadeTarget gasStationHander(FireCluster cluster) {
		Collection<StandardEntity> gasStas = world.getEntitiesOfType(StandardEntityURN.GAS_STATION);
		Set<CSUBuilding> targetGasStas = new HashSet<>();
		
		Polygon cluster_po = cluster.getConvexObject().getConvexHullPolygon();
		
		for (StandardEntity next : gasStas) {
			GasStation sta = (GasStation) next;
			
			if (sta.isFierynessDefined() && sta.getFieryness() >= 1)
				continue;
			
			if  (sta.isTemperatureDefined() && sta.getTemperature() < 25)      ///temperature
				continue;
			
			CSUBuilding csuBu = world.getCsuBuilding(next.getID());
			if (csuBu.getWaterQuantity() >= 3000)  // two cycles   ///water
				continue;
			
			int[] apexs = sta.getApexList();
			CompositeConvexHull convexHull = new CompositeConvexHull();
			
			for (int i = 0; i < apexs.length; i += 2) {
				convexHull.addPoint(apexs[i], apexs[i + 1]);
			}
			
			Polygon po = convexHull.getConvexPolygon();
			
			int dis = (int) Ruler.getDistance(cluster_po, po);
			if (dis <= 5000)     ///distance
				targetGasStas.add(world.getCsuBuilding(next.getID()));
		}
		
		if (targetGasStas.contains(lastTarget)) 
			return new FireBrigadeTarget(cluster, lastTarget);
		
		CSUBuilding tar = null;
		double minDistance = Double.MAX_VALUE;
		for (CSUBuilding next : targetGasStas) {
			double dis = world.getDistance(next.getId(), controlledEntity.getID());
			if (dis < minDistance) {
				minDistance = dis;
				tar = next;
			}
		}
		
		if (tar != null) {
			this.target = tar;
			return new FireBrigadeTarget(cluster, tar);
		} else
			return null;
	}
	/**
	 * search for the zone last target in
	 */
	private void searchZones(CsuZone zone) throws ActionCommandException, TimeOutException {
		zone.removeVisitedFromUnvisit(lookedBuildings());
		Set<EntityID> dests = zone.getUnvisitBuildings();
		if (dests.isEmpty())
			return;
		
		Set<StandardEntity> dest = new HashSet<>(dests.size() * 2); ///why
		for (EntityID id : dests) {
			dest.add(world.getEntity(id));
		}

		Point selfL = new Point(controlledEntity.getX(), controlledEntity.getY());
		CostFunction costFun = underlyingAgent.getRouter().getNormalCostFunction();
		Area loca = (Area) world.getSelfPosition();

		List<EntityID> path = underlyingAgent.getRouter().getMultiDest(loca, dest, costFun, selfL);
		EntityID destId = path.get(path.size() - 1);
		double cost = underlyingAgent.getRouter().getRouteCost();
		double dis = world.getDistance(loca.getID(), destId);  ///distance

		if (cost > 20 * dis) {  ///constant
			zone.removeVisitedFromUnvisit(unreachables(world.getEntity(destId, Area.class)));
			if (AgentConstants.FB) {
				System.out.println(world.getTime() + ", " + controlledEntity 
						+ " the RouteCost of building of zone last target in is much");
				System.out.println("------DirectionBasedTargetSelector: searchZones()");
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
						+ " is searching last target's zone. path = [" + str + "] " );
				System.out.println("------DirectionBasedTargetSelector, method: searchZones()");
			}
			
			this.underlyingAgent.move(path);
		}
	}
	
	/**
	 * Remove all buildings that this agent has seen. And also, all buildings
	 * other agent might see.
	 * first, the underlyingAgent, add buildings from getChanged()
	 * second, other agents, if position is defined, if in building add it, then add observableAreas
	 * @return a list of looked buildings
	 */
	private List<EntityID> lookedBuildings() {
		List<EntityID> result = new ArrayList<>();

		Human agent;
		for (StandardEntity next : world.getEntitiesOfType(AgentConstants.PLATOONS)) {
			agent = (Human)next;
			
			if (agent.getID().getValue() == underlyingAgent.getID().getValue()) {
				for (EntityID visible : underlyingAgent.getChanged()) {
					StandardEntity v_entity = world.getEntity(visible);
					if (v_entity instanceof Building) {
						result.add(visible);
					}
				}
			}
			
			if (agent.isPositionDefined()) {              ///world.getSelfPosition() && agent.getPosition()
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
			}
		}
		
		return result;
	}
	///list, arraylist; set, hashset/fastset
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
	
	private void printSortedBuilding(SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings) {
		String str = null;
		for(Pair<Pair<EntityID, Double>, Double> next : sortedBuildings) {
			if (str == null) {
				str = "time = " + world.getTime() + ", " + controlledEntity +", sortedBuilding: [";
			} else {
				str = str + ", ";
			}
			str = str + "(" + next.first().first().getValue() + ", " + next.second() + ")";
		}
		str = str + "]";
		System.out.println(str);
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
		SortedSet<Pair<EntityID, Double>> sortedSet = new TreeSet<>(FbUtilities.pairComparator);
		Building building;
 
		// the border entities of dying fire cluster
		Set<CSUBuilding> lessValueBuildings = new HashSet<>();
		// map border buildings that also contains in fire cluster
		Set<CSUBuilding> mapBorderBuildings = new HashSet<>();
		// all buildings in the main expand direction of each fire cluster
		Set<CSUBuilding> highValueBuildings = this.buildingClassifier(lessValueBuildings, mapBorderBuildings);

		for (CSUBuilding next : burnBuildings) {
			building = next.getSelfBuilding();
			///int distance = (int)world.getDistance(underlyingAgent.location(),building) / 1000;
			prioritySetter.setPriority(next);
			// if this building is map border building
			if (mapBorderBuildings.contains(next)) {///forbidden multiply, for + -
				next.BUILDING_VALUE -= 500;			
				sortedSet.add(new Pair<EntityID, Double>(building.getID(),
						next.BUILDING_VALUE));
				continue;
			} 
			// if this building is less value building
			if (lessValueBuildings.contains(next)) {
				next.BUILDING_VALUE -= 250;		
				sortedSet.add(new Pair<EntityID, Double>(building.getID(),
						next.BUILDING_VALUE));
				continue; 
			}
			// if this building is high value building
			if (highValueBuildings.contains(next)) {
				if(next.getEstimatedFieryness() != 8)
					next.BUILDING_VALUE += 250;
				else
					next.BUILDING_VALUE += 50;
				sortedSet.add(new Pair<EntityID, Double>(building.getID(),
						next.BUILDING_VALUE));
				continue;
			}
			///if this is not those three kind of building, do not change the value
			///next.BUILDING_VALUE += 150;
			sortedSet.add(new Pair<EntityID, Double>(building.getID(),
					next.BUILDING_VALUE));
			continue;
		}

		if (sortedSet.size() > 0)
			return world.getCsuBuilding(sortedSet.first().first());
		else 
			return null;
	}

	private SortedSet<Pair<Pair<EntityID, Double>, Double>> calculateValue(FireCluster fireCluster) {
		SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings = 
				new TreeSet<Pair<Pair<EntityID, Double>, Double>>(FbUtilities.pairComparator_new);
		
		Set<StandardEntity> borderBuildings = new FastSet<>(fireCluster.getBorderEntities());
		///System.out.println(world.getTime() + ", "+ world.me + ",fireCluster: " + fireCluster.getFireCondition());
        ///System.out.println(world.getTime() + ", "+ world.me + ",borderBuildings  "+borderBuildings);///
		Set<CSUBuilding> inDirectionBuildings;
		Point directionPoint = directionManager.findFarthestPointOfMap(fireCluster, controlledEntity);
		inDirectionBuildings = fireCluster.findBuildingInDirection(directionPoint);
	    ///System.out.println(world.getTime() + ", "+world.me +", inDirectionBuildings  " + inDirectionBuildings);///
		borderBuildings.removeAll(csuBuildingToEntity(inDirectionBuildings));

		this.calculateValueOfInDirectionBuildings(inDirectionBuildings, sortedBuildings);
		this.calculateValueOfBorderBuildings(borderBuildings, sortedBuildings);
		
		return sortedBuildings;
	}

	private void calculateValueOfInDirectionBuildings(Set<CSUBuilding> inDirectionBuildings,
			SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings) {
		for (CSUBuilding next : inDirectionBuildings) {
			
			prioritySetter.setPriority(next);
			
			next.BUILDING_VALUE += 500;///
			
			if (lastTarget != null && next.getId() == lastTarget.getId()) {
				next.BUILDING_VALUE += 250;///
			}
			
			Pair<Integer, Integer> selfLocation = world.getControlledEntity().getLocation(world);
			Pair<Integer, Integer> buildingLocation = next.getSelfBuilding().getLocation(world);
			double distance = Ruler.getDistance(selfLocation, buildingLocation);
			
			Pair<EntityID, Double> pair = new Pair<>(next.getSelfBuilding().getID(), distance);
			sortedBuildings.add(new Pair<Pair<EntityID, Double>, Double>(pair, next.BUILDING_VALUE));
		}
	}

	private void calculateValueOfBorderBuildings(Set<StandardEntity> borderBuildings,
			SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings) {
		CSUBuilding csuBuilding;
		for (StandardEntity next : borderBuildings) {
			csuBuilding = world.getCsuBuilding(next.getID());
			prioritySetter.setPriority(csuBuilding);
			csuBuilding.BUILDING_VALUE -= 250;///
		
			if (lastTarget != null) {
				double distance = world.getDistance(lastTarget.getId(), csuBuilding.getId());
				int temp = (int)(distance / MEAN_VELOCITY_DISTANCE);
				csuBuilding.BUILDING_VALUE -= temp*50;
				
			}
			 
			if (lastTarget != null && csuBuilding.getId() == lastTarget.getId()) {
				csuBuilding.BUILDING_VALUE += 250;///
				
			}
			
			
			Pair<Integer, Integer> selfLocation = world.getControlledEntity().getLocation(world);
			Pair<Integer, Integer> buildingLocation = next.getLocation(world);
			double distance = Ruler.getDistance(selfLocation, buildingLocation);
			
			Pair<EntityID, Double> pair = new Pair<>(next.getID(), distance);
			sortedBuildings.add(new Pair<Pair<EntityID, Double>, Double>(pair, csuBuilding.BUILDING_VALUE));
		}
	}

	/**
	 * Classify buildings.
	 * 
	 * <pre>
	 * 1.lessValueBuildings: the border buildings of dying FireCLuster
	 * 
	 * 2.mapBorderBuildings: the border buildings of map border FireCLuster
	 * 
	 * 3.highValueBuildings: buildings in the expand direction of its FireCluster
	 * 
	 * 4.otherBuildings: the remaining border buildings of FireCluster
	 * </pre>
	 * 
	 * @param lessValue
	 *            a set to store less value buildings
	 * @param mapBorder
	 *            a set to store map border buildings
	 * @return a set of high value buildings
	 */
	private Set<CSUBuilding> buildingClassifier(Set<CSUBuilding> lessValue, Set<CSUBuilding> mapBorder) {
		Set<CSUBuilding> highValueBuildings = new HashSet<>();
		List<FireCluster> fireClusters = world.getFireClusterManager().getClusters();
		
		ConvexObject convexObject;
		Polygon polygon;
		CSUBuilding csuBuilding;

		for (FireCluster cluster : fireClusters) {
			if (cluster == null)
				continue;
			if (cluster.isDying()) {
				for (StandardEntity next : cluster.getBorderEntities())
					lessValue.add(this.world.getCsuBuilding(next.getID()));
				continue;
			}
			if (cluster.isBorder()) {
				for (StandardEntity next : cluster.getBorderEntities())
					mapBorder.add(this.world.getCsuBuilding(next.getID()));
				continue;
			}
			
			directionManager.findFarthestPointOfMap(cluster, controlledEntity);
			convexObject = cluster.getConvexObject();
			if (convexObject == null || convexObject.CENTER_POINT == null
					|| convexObject.CONVEX_POINT == null || convexObject.getConvexHullPolygon() == null)
				continue;
			
			if (cluster.isOverCenter()) {
				polygon = convexObject.getDirectionRectangle();
			} else {
				polygon = convexObject.getTriangle();
			}
			
			for (StandardEntity next : cluster.getBorderEntities()) {
				csuBuilding = world.getCsuBuilding(next.getID());
				int[] vertices = csuBuilding.getSelfBuilding().getApexList();
				for (int i = 0; i < vertices.length; i += 2) {
					if (polygon.contains(vertices[i], vertices[i + 1])) {
						highValueBuildings.add(csuBuilding);
						break;
					}
				}
			}
		}
	
		return highValueBuildings;
	}

	/**
	 * Translate a collection of CSUBuilding into a collection of
	 * StandardEntity.
	 * 
	 * @param csuBuildings
	 *            a collection of CSUBuilding will be translated
	 * @return a collection of StandardEntity
	 */
	private Collection<StandardEntity> csuBuildingToEntity(Collection<CSUBuilding> csuBuildings) {
		Collection<StandardEntity> result = new FastSet<>();
		for (CSUBuilding next : csuBuildings)
			result.add(next.getSelfBuilding());
		return result;
	}
}
