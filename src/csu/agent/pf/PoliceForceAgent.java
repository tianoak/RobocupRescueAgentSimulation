package csu.agent.pf;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.agent.pf.PFLastTaskType.PFClusterLastTaskEnum;
import csu.agent.pf.clearStrategy.AroundBasedStrategy;
import csu.agent.pf.clearStrategy.CenterAreaBasedStrategy;
import csu.agent.pf.clearStrategy.CenterLineBasedStrategy;
import csu.agent.pf.clearStrategy.POSBasedStrategy;
import csu.agent.pf.clearStrategy.TangentBasedStrategy;
import csu.agent.pf.cluster.Cluster;
import csu.agent.pf.cluster.Clustering;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.BuriedHumans;
import csu.model.object.CSUBuilding;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.model.route.pov.CostFunction;
import csu.standard.Ruler;

/**
 * @author aiyouwei
 *
 */
public class PoliceForceAgent extends AbstractPoliceForceAgent {
	private Map<EntityID, Pair<EntityID, Integer>> pfMap;
	private Set<Cluster> cantWorkClusters;
	private Set<EntityID> buriedEntrances;

	@Override
	protected void initialize() {
		super.initialize();
		// 设置clear方式
		assignClearStrategy();

		// 分区的处理
		this.needToExpandClusters = new ArrayList<>(clusters);
		this.expandedClusters = new ArrayList<>();

		currentCluster = clusters.get(currentClusterIndex);
		expandedClusters.add(currentCluster);
		needToExpandClusters.remove(currentCluster);

		taskTarget = new PFLastTaskTarget();

		// 当前分区的
		traversalEntranceSet = new HashSet<Road>(
				currentCluster.getEntranceList());
		traversalCriticalAreas = new HashSet<>(
				currentCluster.getCriticalAreas());
		traversalRefugeSet = new HashSet<Refuge>(currentCluster.getRefugeList());

		clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;

		// 着火建筑
		nearBurningBuildings = new HashSet<Building>();
		hadSearchBuringBuildings = new HashSet<Building>();

		entrancesOfBurningZone = new HashSet<Road>();

		criticalOfBurningZone = new HashSet<Area>();

		searchBurningFlag = true;

		// 建筑数量
		buildingCount = 0;
		for (CsuZone zone : currentCluster.getZoneList()) {
			buildingCount += zone.size();
		}

		// 不能工作警察的分区
		cantWorkClusters = new HashSet<Cluster>();
		// 警察位置
		pfMap = new HashMap<EntityID, Pair<EntityID, Integer>>();

		buriedEntrances = new HashSet<EntityID>();

		System.out.println(toString() + " was connected. [id=" + getID()
				+ ", uniform=" + getUniform() + "]");
	}

	@Override
	protected void prepareForAct() throws TimeOutException {
		super.prepareForAct();
		world.getCriticalArea().update(router);
		this.updateTaskList();

		updatePfCannotToWork();

		if (AgentConstants.PRINT_TEST_DATA_PF) {
			System.out.println("time = " + time + me()
					+ " world.getStuckedAgents()");
			for (EntityID next : world.getStuckedAgents()) {
				Human human = (Human) world.getEntity(next);
				System.out.println("stuckedHumanid: " + next
						+ "-----position: " + human.getPosition().getValue());

			}
		}
	}

	@Override
	protected void act() throws ActionCommandException, TimeOutException {

		this.cannotClear();
		this.careSelf();

		// 自己被困
		// 连续发送move，但是位置变化小。包括了困在building中
		if (isBlocked()) {

			Blockade target = this.clearStrategy.blockedClear();
			if (target != null) {
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out.println("time = " + time + me() + " isBlocked");
				}
				this.clearStrategy.updateClearPath(lastMovePlan);
				this.sendClear(time, target.getX(), target.getY());
			}

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out.println("time = " + time + me()
						+ " is stucked and ramdom walk");
			}
			randomWalk();
		}
		// 空的
		// 由于上面的代码，所以下面就永远不会用到
		this.leaveBurningBuilding();

		// 自己locate in blockade
		// 应该提前
		this.stuckedClear();

		// 优先考虑周围情况
		this.coincidentWork();

		this.clearStrategy.updateClearPath(lastCyclePath);
		this.clearStrategy.clear();
		// 继续上次任务
		this.continueLastTask();
		this.traversalRefuge();

		this.helpBuriedAgent();
		this.helpStuckAgent();
		// 更替
		// this.searchingBurningBuilding();
		this.searchingBurningBuildingCritical();
		// this.traversalCritical();
		this.helpBuriedHumans();
		this.traversalEntrance();
		this.expandCluster();
		this.randomWalk();
	}

	/**
	 * @throws ActionCommandException
	 *             pf自己locate in blockade
	 */
	private void stuckedClear() throws ActionCommandException {
		if (isStucked(me())) {
			Blockade blockade = isLocateInBlockade(me());
			if (blockade != null) {

				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out.println("time = " + time + ", agent = " + me()
							+ " is stucked clear, " + "target = " + blockade
							+ " ----- PoliceForceAgent, stuckedClear()");
				}
				this.sendClear(time, blockade.getID());
//				this.clearStrategy.updateClearPath(lastCyclePath);
//				this.sendClear(time, blockade.getX(), blockade.getY());
				throw new ActionCommandException(StandardMessageURN.AK_CLEAR);
			}
		}
	}

	private void assignClearStrategy() {
		switch (clearStrategyType) {
		case AROUND_BASED_STRATEGY:
			clearStrategy = new AroundBasedStrategy(world);
			break;
		case CENTER_AREA_BASED_STRATEGY:
			clearStrategy = new CenterAreaBasedStrategy(world);
			break;
		case CENTER_LINE_BASED_STRATEGY:
			clearStrategy = new CenterLineBasedStrategy(world);
			break;
		case POS_BASED_STRATEGY:
			clearStrategy = new POSBasedStrategy(world);
			break;
		case TANGENT_BASED_STRATEGY:
			clearStrategy = new TangentBasedStrategy(world);
			break;
		}
	}

	/**
	 * 更新任务列表 更新 coincidentRefuge，visitedBuriedAgent，traversalEntranceSet
	 */
	private void updateTaskList() {
		// coincidentRefuge
		if (location() instanceof Refuge) {
			visitedRefuges.add(location().getID());
			coincidentRefuge.remove((Refuge) location());
		}

		// visitedBuriedAgent
		String string = null;
		FOR: for (Iterator<Human> itor = getCoincidentBuriedAgent().iterator(); itor
				.hasNext();) {
			Human human = itor.next();

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				if (string == null) {
					string = human.getID().getValue() + "";
				} else {
					string = string + ", " + human.getID().getValue();
				}
			}

			if (!human.isPositionDefined()) {
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
							.println("time = "
									+ time
									+ ", "
									+ me()
									+ " human: "
									+ human.getID()
									+ "'s position is not defined "
									+ "----- class: PoliceForceAgent, method: updateTaskList()");
				}

				visitedBuriedAgent.add(human.getID());
				itor.remove();
				continue;
			}

			StandardEntity loca = human.getPosition(world);
			if (!(loca instanceof Building)) {
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
							.println("time = "
									+ time
									+ ", "
									+ me()
									+ " human: "
									+ human.getID()
									+ "'s position is not building "
									+ "----- class: PoliceForceAgent, method: updateTaskList()");
				}

				visitedBuriedAgent.add(human.getID());
				itor.remove();
				continue;
			}

			Building building = (Building) loca;

			for (Road next : world.getEntrance().getEntrance(building)) {
				if (!isVisible(next.getID()))
					continue;
				CSURoad road = world.getCsuRoad(next.getID());
				if (road.isNeedlessToClear()) {
					if (AgentConstants.PRINT_TEST_DATA_PF) {
						System.out
								.println("time = "
										+ time
										+ ", "
										+ me()
										+ " human: "
										+ human.getID()
										+ "'s position's entrance is needless to clear "
										+ "----- class: PoliceForceAgent, method: updateTaskList()");
					}

					visitedBuriedAgent.add(human.getID());
					itor.remove();
					continue FOR;
				}
			}
		}

		if (AgentConstants.PRINT_TEST_DATA_PF) {
			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " coincident buried agents = ["
							+ string
							+ "] ----- class: PoliceForceAgent, method: updateTaskList()");
		}

		StandardEntity entity = null;
		for (EntityID changed : getChanged()) {
			entity = world.getEntity(changed);
			if (entity instanceof Road) {
				CSURoad road = world.getCsuRoad(changed);
				if (!road.isEntrance())
					continue;
				boolean needToClear = false;
				for (EntityID neighbourID : ((Road) entity).getNeighbours()) {
					StandardEntity neighbour = world.getEntity(neighbourID);
					if (neighbour instanceof Building) {
						Building neighbour_Building = (Building) neighbour;
						if (neighbour_Building.isFierynessDefined()
								&& neighbour_Building.getFieryness() < 7) {
							needToClear = true;
						} else if (!neighbour_Building.isFierynessDefined()) {
							needToClear = true;
						}
					}
				}

				if (needToClear == false) {
					this.traversalEntranceSet.remove(changed);
					continue;
				}

				if (road.getSelfRoad().isBlockadesDefined()
						&& road.getSelfRoad().getBlockades().size() > 0)
					continue;

				if (road.isNeedlessToClear())
					this.traversalEntranceSet.remove(road.getSelfRoad());
			}
		}
	}

	/**
	 * @throws csu.agent.Agent.ActionCommandException
	 *             不能够clear的pf，其实什么都作不了
	 */
	private void cannotClear() throws csu.agent.Agent.ActionCommandException {
		if (!me().isHPDefined() || me().getHP() <= 1000) {

			clusterLastTaskType = PFClusterLastTaskEnum.CANNOT_TO_CLEAR;
			Collection<StandardEntity> allReguge = world
					.getEntitiesOfType(StandardEntityURN.REFUGE);

			CostFunction costFunc = router.getNormalCostFunction();
			Point selfL = new Point(me().getX(), me().getY());
			lastCyclePath = router.getMultiAStar(location(), allReguge,
					costFunc, selfL);

			if (AgentConstants.PRINT_TEST_DATA_PF) {

				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null) {
						str = next.getValue() + "";
					} else {
						str = str + "," + next.getValue();
					}
				}

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " can't to clear and move to refuge. "
								+ "path = ["
								+ str
								+ "] ----- class: PoliceForceAgent, method: cantToClear()");
			}

			move(lastCyclePath);
		}
	}

	// 空的
	private void leaveBurningBuilding() throws ActionCommandException {
		/*
		 * StandardEntity entity; if (location() instanceof Building &&
		 * !(location() instanceof Refuge)) { Building building = (Building)
		 * location(); if ((building.isFierynessDefined() &&
		 * building.isOnFire()) || (building.isFierynessDefined() &&
		 * building.getFieryness() != 8 && building.isTemperatureDefined() &&
		 * building.getTemperature() > 35)) {
		 * 
		 * if (AgentConstants.PRINT_TEST_DATA_PF) { System.out.println("Agent "
		 * + me() + " in time: " + time +
		 * " was in a dangerous Building and trying to go out it." +
		 * " ----- class: PoliceForceAgent, method: leaveBurningBuilding()"); }
		 * 
		 * for (EntityID next : building.getNeighbours()) { entity =
		 * world.getEntity(next); if (entity instanceof Building) {
		 * unenteredBuildings.remove(building); } } clusterLastTaskType =
		 * PFClusterLastTaskEnum.NO_TAST; moveToRefuge(); } }
		 */
	}

	/**
	 * @throws ActionCommandException
	 *             优先考虑周围偶然情况
	 */
	private void coincidentWork() throws ActionCommandException {

		coincidentTaskUpdate();
		//201410
		clearEntranceForCivilian();
		//
		coincidentCheckRefuge();
		coincidentHelpStuckedAgent();
		coincidentHelpBuriedAgent();
		
		
	}

	/**
	 * 偶然任务更新 refuge and coincidentBuriedAgent agent in building
	 */
	private void coincidentTaskUpdate() {
		// 周围
		for (StandardEntity next : world.getObjectsInRange(me().getID(), 50000)) {
			if (next instanceof Refuge) {
				if (visitedRefuges.contains(next.getID())) {
					coincidentRefuge.remove((Refuge) next);
					continue;
				}
				coincidentRefuge.add((Refuge) next);
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
							.println("time = "
									+ time
									+ ", "
									+ me()
									+ "coincidentRefuge add refuge : "
									+ next
									+ "----- class: PoliceForceAgent, method: coincidentTaskUpdate()");
				}
				// buried agent in building
			} else if (next instanceof Human && !(next instanceof Civilian)) {
				if (visitedBuriedAgent.contains(next.getID())) {
					getCoincidentBuriedAgent().remove((Human) next);
					continue;
				}

				Human human = (Human) next;
				if (human.isBuriednessDefined() && human.getBuriedness() > 0
						&& human.isPositionDefined()) {
					StandardEntity loca = human.getPosition(world);
					if (loca instanceof Building) {
						getCoincidentBuriedAgent().add(human);

						if (AgentConstants.PRINT_TEST_DATA_PF) {
							System.out
									.println("time = "
											+ time
											+ ", "
											+ me()
											+ "coincidentBuriedAgent add BuriedAgent : "
											+ next
											+ "----- class: PoliceForceAgent, method: coincidentTaskUpdate()");
						}

					}
				}
			}
		}
	}

	/**
	 * @throws ActionCommandException
	 *             move to coincidentCheckRefuge
	 */
	private void coincidentCheckRefuge() throws ActionCommandException {
		if (coincidentRefuge.size() > 0) {
			CostFunction costFunc = router.getPfCostFunction();
			Point selfL = new Point(me().getX(), me().getY());
			lastCyclePath = router.getMultiDest(location(), coincidentRefuge,
					costFunc, selfL);

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null)
						str = next.getValue() + "";
					else
						str = str + ", " + next.getValue();
				}

				EntityID destination = lastCyclePath
						.get(lastCyclePath.size() - 1);

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " moving to clear coincident "
								+ "refuge = "
								+ destination.getValue()
								+ ", path = ["
								+ str
								+ "] ----- "
								+ "class: PoliceForceAgent, method: coincidentCheckRefuge()");
			}

			move(lastCyclePath);
		}
	}

	/**
	 * @throws ActionCommandException
	 *             move to buiried agent ‘s entrance
	 */
	private void coincidentHelpBuriedAgent() throws ActionCommandException {
		if (getCoincidentBuriedAgent().size() > 0) {
			CostFunction costFunc = router.getPfCostFunction();
			Point selfL = new Point(me().getX(), me().getY());

			List<StandardEntity> dest = new ArrayList<>();

			for (Human next : getCoincidentBuriedAgent()) {
				StandardEntity loca = next.getPosition(world);
				if (loca instanceof Building) {
					dest.addAll(world.getEntrance()
							.getEntrance((Building) loca));
				}
			}

			lastCyclePath = router.getMultiDest(location(), dest, costFunc,
					selfL);

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null)
						str = next.getValue() + "";
					else
						str = str + ", " + next.getValue();
				}

				EntityID destination = lastCyclePath
						.get(lastCyclePath.size() - 1);

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " moving to clear coincident "
								+ "buried agent = "
								+ destination.getValue()
								+ ", path = ["
								+ str
								+ "] ----- class: PoliceForceAgent, method: coincidentHelpBuriedAgent()");
			}

			move(lastCyclePath);
		}
	}

	public List<EntityID> getAgentOfCoincidentHelpStuckedAfent() {
		List<EntityID> needClearAgent = new ArrayList<>();
		// changeset 中的at pf，
		List<EntityID> inChangeSetAT_FB = new ArrayList<>();
		// blockade
		List<EntityID> inChangeSetBlockades = new ArrayList<>();

		for (EntityID next : getChanged()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof AmbulanceTeam
					|| entity instanceof FireBrigade) {
				inChangeSetAT_FB.add(next);
			} else if (entity instanceof Blockade) {
				inChangeSetBlockades.add(next);
			}
		}
		
		for (EntityID agent_id : inChangeSetAT_FB) {
			Human agent = (Human) world.getEntity(agent_id);
			for (EntityID blockade_id : inChangeSetBlockades) {
				Blockade blockade = (Blockade) world.getEntity(blockade_id);
				double dis = Ruler.getDistanceToBlock(blockade,
						new Point(agent.getX(), agent.getY()));
				if (dis < 500) {
					needClearAgent.add(agent_id);
				}
			}

		}

		needClearAgent.addAll(world.getStuckedAgents());
		return needClearAgent;
	}

	public List<Blockade> getBlockadeOfCoincidentHelpStuckedAgent() {
		// at fb blockade
		List<EntityID> inChangeSetAT_FB = new ArrayList<>();
		List<EntityID> inChangeSetBlockades = new ArrayList<>();

		for (EntityID next : getChanged()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof AmbulanceTeam
					|| entity instanceof FireBrigade) {
				inChangeSetAT_FB.add(next);
			} else if (entity instanceof Blockade) {
				inChangeSetBlockades.add(next);
			}
		}

		// 旁边有at fb 的 blockade
		List<Blockade> needClearBlockades = new ArrayList<>();
		for (EntityID next : inChangeSetBlockades) {
			Blockade blockade = (Blockade) world.getEntity(next);
			for (EntityID at_pf : inChangeSetAT_FB) {
				Human agent = (Human) world.getEntity(at_pf);
				double dis = Ruler.getDistanceToBlock(blockade,
						new Point(agent.getX(), agent.getY()));
				if (dis < 2000) {
					needClearBlockades.add(blockade);

					if (AgentConstants.PRINT_TEST_DATA_PF) {
						System.out
								.println("time = "
										+ time
										+ ", "
										+ me()
										+ "needClearBlockades add blockade : "
										+ blockade
										+ "----- class: PoliceForceAgent, method: coincidentHelpStuckedAgent()");
					}

					break;
				}
			}
		}

		return needClearBlockades;
	}

	private void coincidentHelpStuckedAgent() throws ActionCommandException {
		List<EntityID> needClearAgentIDList = getAgentOfCoincidentHelpStuckedAfent();
		Human closetAgent = null;
		double minDistance = Double.MAX_VALUE;
		for (EntityID entityID : needClearAgentIDList) {
			if (!getChanged().contains(entityID)) {
				return;
			}
			Human agent = (Human) world.getEntity(entityID);
			StandardEntity agentPosition = world.getEntity(agent.getPosition());
			if (agentPosition instanceof Building && (agent.isBuriednessDefined() && agent.getBuriedness() > 0)) {
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ "agentPosition instanceof Building && (agent.isBuriednessDefined() && agent.getBuriedness() > 0) "
							+ "target agent = "
							+ agent.getID()
							+ " ----- class: PoliceForceAgent, method: coincidentHelpStuckedAgent()");
				}
				continue;
			}
//			agent.getLocation(world);
			double dis = Ruler.getDistance(agent.getLocation(world), me()
					.getLocation(world));
			if (dis < minDistance) {
				minDistance = dis;
				closetAgent = agent;
			}
		}

		if (closetAgent != null) {
			
			StandardEntity entity = world.getEntity(closetAgent.getPosition());
			if (!(entity instanceof Area))
				return;
			Area agentLocation = (Area) entity;
			
			if (agentLocation instanceof Building) {
				Building building = (Building) agentLocation;
				Set<Road> entrancesOfStuckAgent = world.getEntrance().getEntrance(building);
				traversalRoads(entrancesOfStuckAgent);
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " agentLocation instanceof Building and---dont need to traversalRoads(entrancesOfStuckAgent);"
							+ " ----- class: PoliceForceAgent, method: coincidentHelpStuckedAgent()");
				}
				return;
			}

			CostFunction costFunc = router.getPfCostFunction();
			Point selfL = new Point(me().getX(), me().getY());
			lastCyclePath = router.getAStar(location(), agentLocation,
					costFunc, selfL);

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null)
						str = next.getValue() + "";
					else
						str = str + ", " + next.getValue();
				}

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " moving to help agents, "
								+ "target agent = "
								+ closetAgent.getID()
								+ ", path = ["
								+ lastCyclePath
								+ " ----- class: PoliceForceAgent, method: coincidentHelpStuckedAgent()");
			}

			move(lastCyclePath, closetAgent.getX(), closetAgent.getY());
		}
	}

	
	/**
	 * @throws ActionCommandException
	 *             at pf 旁边的 blockade
	 */
	// private void coincidentHelpStuckedAgent() throws ActionCommandException {
	//
	// List<Blockade> needClearBlockades =
	// getBlockadeOfCoincidentHelpStuckedAgent();
	//
	// // 最近的blockade
	// Blockade targetBlockade = null;
	// double minDistance = Double.MAX_VALUE;
	// for (Blockade next : needClearBlockades) {
	// double dis = Ruler.getDistanceToBlock(next, me().getX(), me()
	// .getY());
	// if (dis < minDistance) {
	// minDistance = dis;
	// targetBlockade = next;
	// }
	// }
	// // move
	// if (targetBlockade != null) {
	// StandardEntity entity = world.getEntity(targetBlockade
	// .getPosition());
	// if (!(entity instanceof Area))
	// return;
	// Area blockadeLocation = (Area) entity;
	//
	// CostFunction costFunc = router.getPfCostFunction();
	// Point selfL = new Point(me().getX(), me().getY());
	// lastCyclePath = router.getAStar(location(), blockadeLocation,
	// costFunc, selfL);
	//
	// if (AgentConstants.PRINT_TEST_DATA_PF) {
	// String str = null;
	// for (EntityID next : lastCyclePath) {
	// if (str == null)
	// str = next.getValue() + "";
	// else
	// str = str + ", " + next.getValue();
	// }
	//
	// System.out
	// .println("time = "
	// + time
	// + ", "
	// + me()
	// + " moving to help agents, "
	// + "target blockade = "
	// + targetBlockade.getID()
	// + ", path = ["
	// + lastCyclePath
	// +
	// " ----- class: PoliceForceAgent, method: coincidentHelpStuckedAgent()");
	// }
	//
	//
	// move(lastCyclePath, targetBlockade.getX(), targetBlockade.getY());
	// }
	// }

	private void continueLastTask() throws ActionCommandException {

		EntityID taskEntityID;

		if (AgentConstants.PRINT_TEST_DATA_PF) {
			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ clusterLastTaskType
							+ "----- class: PoliceForceAgent, method: continueLastTask()");
		}

		switch (clusterLastTaskType) {
		case CANNOT_TO_CLEAR:

			if (this.location() instanceof Refuge) {
				clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
				break;
			}
			moveToRefuge();
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;

		case TRAVERSAL_REFUGE:
			taskEntityID = taskTarget.getTraversalRefuge().getID();
			if (changed.getChangedEntities().contains(taskEntityID)) {
				// isSureReachable 准确？
				if (router.isSureReachable(taskEntityID)) {

					clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
					break;
				}
			}
			if (this.location() instanceof Building) {
				if (((Building) this.location()).equals(taskTarget
						.getTraversalRefuge())) {
					clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
					break;
				}
			}

			CostFunction costFunc_1 = router.getPfCostFunction();
			lastCyclePath = router.getAStar(me(),
					taskTarget.getTraversalRefuge(), costFunc_1);

			if (AgentConstants.PRINT_TEST_DATA_PF) {

				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null) {
						str = next.getValue() + "";
					} else {
						str = str + ", " + next.getValue();
					}
				}

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " continue traversal refuge = "
								+ taskEntityID.getValue()
								+ ", the path = ["
								+ str
								+ "] "
								+ "----- class: PoliceForceAgent, method: continueLastTask()");
			}

			move(lastCyclePath);
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;

		case TRAVERSAL_CRITICAL_AREA:
			taskEntityID = taskTarget.getTraversalCriticalArea().getID();
			if (location().getID().getValue() == taskEntityID.getValue()) {
				clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
				break;
			}
			CostFunction costFunc = router.getPfCostFunction();
			lastCyclePath = router.getAStar(me(),
					taskTarget.getTraversalCriticalArea(), costFunc);

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null) {
						str = next.getValue() + "";
					} else {
						str = str + ", " + next.getValue();
					}
				}

				System.out
						.println("time = "
								+ time
								+ ", agent = "
								+ me()
								+ " continue traversal "
								+ "critical: "
								+ taskEntityID.getValue()
								+ ", path = ["
								+ str
								+ "],"
								+ " ----- class: PoliceForceAgent, method: continueLastTask()");
			}
			move(lastCyclePath);
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;

		case TRAVERSAL_ENTRANCE:
			taskEntityID = taskTarget.getTraversalEntrance().getID();
			// 201409
			if (location().getID().getValue() == taskEntityID.getValue()) {
				clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			}
			if (changed.getChangedEntities().contains(taskEntityID)) {
				CSURoad road = world.getCsuRoad(taskEntityID);
				if (road.isNeedlessToClear()) {
					clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
					break;
				}
			}

			if (this.location() instanceof Road) {
				if (location().getID().getValue() == taskEntityID.getValue()) {

					boolean shouldBreak = nearTargetBehavior(taskEntityID,
							false);
					if (shouldBreak) {
						clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
						break;
					}

				}
			}

			if (location() instanceof Building) {
				for (Road next : world.getEntrance().getEntrance(
						(Building) location())) {
					if (next.getID().getValue() == taskEntityID.getValue()) {
						boolean shouldBreak = nearTargetBehavior(taskEntityID,
								true);
						if (shouldBreak) {
							clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
							break;
						}
					}
				}
			}

			CostFunction costFunc_2 = router.getPfCostFunction();
			lastCyclePath = router.getAStar(me(),
					taskTarget.getTraversalEntrance(), costFunc_2);

			if (AgentConstants.PRINT_TEST_DATA_PF) {

				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null) {
						str = next.getValue() + "";
					} else {
						str = str + ", " + next.getValue();
					}
				}

				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " continue traversal entrance = "
								+ taskEntityID.getValue()
								+ ", the path = ["
								+ str
								+ "] "
								+ "----- class: PoliceForceAgent, method: continueLastTask()");
			}
			move(lastCyclePath);
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;

		case HELP_STUCK_HUMAN:
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;
		case NO_TAST:
			break;
		default:
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;
		}
	}

	private boolean nearTargetBehavior(EntityID taskEntityID, boolean inBuilding)
			throws ActionCommandException {

		CSURoad road = world.getCsuRoad(taskEntityID);
		if (road.isNeedlessToClear()) {
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			return true;
		} else if (road.getCsuBlockades().isEmpty()) {
			for (EntityID neighbour : road.getSelfRoad().getNeighbours()) {
				StandardEntity entity = world.getEntity(neighbour);
				if (entity instanceof Building)
					continue;
				for (CSUEdge edge : road.getCsuEdgeTo(neighbour)) {
					if (!edge.getUnderlyingEdge().isPassable())
						continue;
					if (!edge.isNeedToClear())
						continue;
					List<EntityID> path = new ArrayList<>();
					if (inBuilding) {
						path.add(location().getID());
					}
					// 顺序？？
					path.add(edge.getNeighbours().second());
					path.add(edge.getNeighbours().first());

					lastCyclePath = path;

					if (AgentConstants.PRINT_TEST_DATA_PF) {

						String str = null;
						for (EntityID next : lastCyclePath) {
							if (str == null) {
								str = next.getValue() + "";
							} else {
								str = str + ", " + next.getValue();
							}
						}

						System.out
								.println("time = "
										+ time
										+ ", "
										+ me()
										+ ", the path = ["
										+ str
										+ "] "
										+ "----- class: PoliceForceAgent, method: nearTargetBehavior()");
					}

					move(lastCyclePath);
				}
			}
		}
		return false;
	}

	private void traversalRefuge() throws ActionCommandException {
		if (traversalRefugeSet.size() == 0) {
			return;
		}

		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router.getMultiAStar(location(), traversalRefugeSet,
				costFunc, selfL);

		Building destination = (Building) world.getEntity(lastCyclePath
				.get(lastCyclePath.size() - 1));
		traversalRefugeSet.remove(destination);
		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_REFUGE;
		taskTarget.setTraversalRefuge(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA_PF) {

			String str = null;
			for (EntityID next : lastCyclePath) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}

			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " traversal refuge = "
							+ destination.getID().getValue()
							+ ", the path = ["
							+ str
							+ "] "
							+ "----- class: PoliceForceAgent, method: traversalRefuge()");
		}

		move(lastCyclePath);
	}

	private void traversalCriticalOfZone(Set<Area> criticalArea)
			throws ActionCommandException {
		if (criticalArea.size() == 0)
			return;
		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router.getMultiAStar(location(), criticalArea,
				costFunc, selfL);

		Area destination = world.getEntity(
				lastCyclePath.get(lastCyclePath.size() - 1), Area.class);
		criticalArea.remove(destination);

		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_CRITICAL_AREA;
		taskTarget.setTraversalCriticalArea(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA_PF) {
			String str = null;
			for (EntityID next : lastCyclePath) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}

			System.out
					.println("time = "
							+ time
							+ ", agent = "
							+ me()
							+ " traversal critical: "
							+ destination.getID().getValue()
							+ ", path = ["
							+ str
							+ "],"
							+ " ----- class: PoliceForceAgent, method: traversalCriticalOfZone()");
		}

		move(lastCyclePath);
	}

	private void traversalEntrance() throws ActionCommandException {
		if (traversalEntranceSet.size() == 0) {
			return;
		}

		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router.getMultiAStar(location(), traversalEntranceSet,
				costFunc, selfL);
		Road destination = (Road) world.getEntity(lastCyclePath
				.get(lastCyclePath.size() - 1));

		traversalEntranceSet.remove(destination);

		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_ENTRANCE;
		taskTarget.setTraversalEntrance(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA_PF) {

			String str = null;
			for (EntityID next : lastCyclePath) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}

			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " traversal Entrance = "
							+ destination.getID().getValue()
							+ ", the path = ["
							+ str
							+ "] "
							+ "----- class: PoliceForceAgent, method: traversalEntrance()");
		}

		move(lastCyclePath);
	}

	private void traversalRoads(Set<Road> roads) throws ActionCommandException {

		if (roads.size() == 0) {
			return;
		}

		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router
				.getMultiAStar(location(), roads, costFunc, selfL);
		Road destination = (Road) world.getEntity(lastCyclePath
				.get(lastCyclePath.size() - 1));

		roads.remove(destination);
		traversalEntranceSet.remove(destination);
		this.traversalEntranceSet.remove(destination);

		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_ENTRANCE;
		taskTarget.setTraversalEntrance(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA_PF) {

			String str = null;
			for (EntityID next : lastCyclePath) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}

			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " traversal Entrance = "
							+ destination.getID().getValue()
							+ ", the path = ["
							+ str
							+ "] "
							+ "----- class: PoliceForceAgent, method: traversalRoads()");
		}

		move(lastCyclePath);
	}

	/**
	 * @throws ActionCommandException
	 *             帮助 stuck agent
	 */
	private void helpStuckAgent() throws ActionCommandException {
		/*
		 * Set<EntityID> changeSetStuckAgents = new HashSet<>();
		 * 
		 * for (EntityID next : getChanged()) { StandardEntity entity =
		 * world.getEntity(next);
		 * 
		 * if (entity instanceof AmbulanceTeam || entity instanceof FireBrigade)
		 * { if (isStucked((Human)entity)) changeSetStuckAgents.add(next); } }
		 * 
		 * if (changeSetStuckAgents.size() > 0) {
		 * 
		 * EntityID target = null; double minDistance = Double.MAX_VALUE;
		 * 
		 * for (EntityID next : changeSetStuckAgents) { double distance =
		 * world.getDistance(me().getID(), next); if (distance < minDistance) {
		 * minDistance = distance; target = next; } }
		 * 
		 * if (target != null) { Human targetHuman = world.getEntity(target,
		 * Human.class);
		 * 
		 * StandardEntity hu_posi_e =
		 * world.getEntity(targetHuman.getPosition()); if (hu_posi_e instanceof
		 * Road) { CostFunction costFunc = router.getPfCostFunction(); Point
		 * selfL = new Point(world.getSelfLocation().first(),
		 * world.getSelfLocation().second()); Area stucHum_A = (Area) hu_posi_e;
		 * lastCyclePath = router.getAStar(location(), stucHum_A, costFunc,
		 * selfL); move(lastCyclePath, targetHuman.getX(), targetHuman.getY());
		 * } } }
		 */

		Collection<StandardEntity> PFEntity = world
				.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);

		Set<Human> pfs = new HashSet<>();
		for (StandardEntity next : PFEntity) {
			pfs.add((Human) next);
		}

		// 如果是最近的三个警察之一 closetHuman.add
		List<EntityID> closetHuman = new ArrayList<EntityID>();
		for (EntityID next : world.getStuckedAgents()) {
			if (next.getValue() == me().getID().getValue())
				continue;

			if (world.getPoliceForceIdList().contains(next))
				continue;

			final Human stuckHuman = (Human) world.getEntity(next);

			SortedSet<Human> sort = new TreeSet<>(new Comparator<Human>() {
				@Override
				public int compare(Human o1, Human o2) {
					int dis_1 = world.getDistance(o1.getPosition(),
							stuckHuman.getPosition());
					int dis_2 = world.getDistance(o2.getPosition(),
							stuckHuman.getPosition());

					if (dis_1 > dis_2)
						return 1;
					if (dis_1 < dis_2)
						return -1;

					if (dis_1 == dis_2) {
						if (o1.getID().getValue() > o2.getID().getValue())
							return 1;
						if (o1.getID().getValue() < o2.getID().getValue())
							return -1;
					}
					return 0;
				}
			});

			sort.addAll(pfs);
			//System.out.println("sort"+sort.first()+","+sort.size());
			StandardEntity first = sort.first();
			sort.remove(first);
			StandardEntity second=null,third=null;/////////////
			if(sort.size()>=1)
				second=sort.first();
			sort.remove(second);
			if(sort.size()>=1)
				third=sort.first();
			sort.remove(third);
			//StandardEntity second = sort.first();/////
			//sort.remove(second);
			//StandardEntity third = sort.first();
			//StandardEntity second=null,third=null;

			if (first.equals(me()) || second.equals(me()) || third.equals(me())) {
				closetHuman.add(next);
			}
		}

		// 最近的StuckedAgents
		int minInt = Integer.MAX_VALUE;
		StandardEntity closetEntity = null;
		for (EntityID next : closetHuman) {
			StandardEntity stuckHuman = world.getEntity(next);
			int dis = world.getDistance(me(), stuckHuman);
			if (dis <= minInt) {
				minInt = dis;
				closetEntity = stuckHuman;
			}
		}
		if (closetEntity != null) {
			Human targetHuman = (Human) closetEntity;
			//----------------------------------------------------------------------------------
			//CSURoad currentroad=world.getCsuRoad(targetHuman.getPosition());
			/*
			if (visitedBuriedAgent.contains(targetHuman.getID()))////////////////
			{
				StandardEntity hu_posi_e = world.getEntity(targetHuman
						.getPosition());
				if (hu_posi_e instanceof Road) {
					Point selfL = new Point(world.getSelfLocation().first(), world
							.getSelfLocation().second());
					Area stucHum_A = (Area) hu_posi_e;
					lastCyclePath = router.getAStar(location(), stucHum_A,
							router.getPfCostFunction(), selfL);
					Area stuckedarea=(Area) hu_posi_e;
					Edge diredge=null;
					for(Edge nextedge:stuckedarea.getEdges()){
						if(!nextedge.isPassable())
							continue;
						//CSURoad nextroad = world.getCsuRoad(nextedge.getNeighbour());
						if(lastCyclePath.contains(nextedge.getNeighbour()))
						{
							diredge=nextedge;
							break;
						}
					}
					Point2D dirpoint=new Point2D((diredge.getStartX()+diredge.getEndX())/2,
							(diredge.getStartY()+diredge.getEndY())/2);
					move(lastCyclePath, (int)dirpoint.getX(),(int)dirpoint.getY());
				}
			}
			*/
			//-----------------------------------------------------------------
			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " helpStuckHuman."
								+ " ----- class: PoliceForceAgent, method: helpStuckHuman()");
			}

			StandardEntity hu_posi_e = world.getEntity(targetHuman
					.getPosition());
			if (hu_posi_e instanceof Road) {
				Point selfL = new Point(world.getSelfLocation().first(), world
						.getSelfLocation().second());
				Area stucHum_A = (Area) hu_posi_e;
				lastCyclePath = router.getAStar(location(), stucHum_A,
						router.getPfCostFunction(), selfL);
				move(lastCyclePath, targetHuman.getX(), targetHuman.getY());
			}
		}
	}

	private void helpBuriedAgent() throws ActionCommandException {
		//获取所有pf
		Collection<StandardEntity> PFEntity = world
				.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
		Set<Human> pfs = new HashSet<>();
		for (StandardEntity next : PFEntity) {
			pfs.add((Human) next);
		}

		
		BuriedHumans buriedHumanFactory = world.getBuriedHumans();
		Set<Human> buriedAgents = new HashSet<>();
		Set<Road> buriedAgentEntrance = new HashSet<>();

		// agent buried 遍历 
		//是否分配到本pf救助
		for (EntityID next : buriedHumanFactory.getTotalBuriedHuman()) {
			StandardEntity entity = world.getEntity(next);
			//过滤，剩下非市民的human
			if (!(entity instanceof Human))
				continue;
			if (entity instanceof Civilian)
				continue;

			final Human human = (Human) entity;
			SortedSet<Human> sort = new TreeSet<>(new Comparator<Human>() {
				@Override
				public int compare(Human o1, Human o2) {

					int dis_1 = world.getDistance(o1.getPosition(),
							human.getPosition());
					int dis_2 = world.getDistance(o2.getPosition(),
							human.getPosition());

					if (dis_1 > dis_2)
						return 1;
					if (dis_1 < dis_2)
						return -1;

					if (dis_1 == dis_2) {
						if (o1.getID().getValue() > o2.getID().getValue())
							return 1;
						if (o1.getID().getValue() < o2.getID().getValue())
							return -1;
					}
					return 0;
				}
			});
			sort.addAll(pfs);

			StandardEntity first = sort.first();
			sort.remove(first);
			//StandardEntity second = sort.first();
			//sort.remove(second);
			//StandardEntity third = sort.first();
			StandardEntity second=null,third=null;/////////////
			if(sort.size()>=1)
				second=sort.first();
			sort.remove(second);
			if(sort.size()>=1)
				third=sort.first();
			sort.remove(third);

			if (first.equals(me()) || second.equals(me()) || third.equals(me())) {
				buriedAgents.add(human);
			}
		}

		//对需要救助的agent遍历
		//
		FOR: for (Iterator<Human> itor = buriedAgents.iterator(); itor
				.hasNext();) {
			Human human = itor.next();
			//排除已经救助过的
			if (visitedBuriedAgent.contains(human.getID())) {
				itor.remove();
				continue;
			}
			StandardEntity location = world.getEntity(human.getPosition());
			if (!(location instanceof Building))
				continue;
			Building loca_bu = (Building) location;

			//agent所在的建筑入口是存在通的则排除
			for (Road road : world.getEntrance().getEntrance(loca_bu)) {
				if (!isVisible(road))
					continue;
				CSURoad csuRoad = world.getCsuRoad(road.getID());
				if (csuRoad.isNeedlessToClear()) {
					visitedBuriedAgent.add(human.getID());
					itor.remove();
					continue FOR;
				}
			}

			for (Road road : world.getEntrance().getEntrance(loca_bu)) {
				buriedAgentEntrance.add(road);
			}
		}

		if (buriedAgentEntrance.isEmpty())
			return;

		if (AgentConstants.PRINT_TEST_DATA_PF) {
			System.out
					.println("time = "
							+ time
							+ ", "
							+ me()
							+ " go to help buried agents"
							+ " ----- class: PoliceForceAgent, method: helpBuriedAgent()");
		}
		//没有 visitedBuriedAgent.add 但是估计能够在coincidentHelpBuriedAgent()加入，但是其忽略了pf
		traversalRoads(buriedAgentEntrance);
	}

	private void helpBuriedHumans()
			throws csu.agent.Agent.ActionCommandException {
		updateBuriedEntrances();
		if (buriedEntrances.size() == 0) {
			return;
		}
		Set<Road> temp = new HashSet<Road>();
		for (EntityID entityID : buriedEntrances) {
			temp.add((Road) world.getEntity(entityID));
		}
		traversalRoads(temp);
	}

	private void updateBuriedEntrances() {
		BuriedHumans buriedHumanFactory = world.getBuriedHumans();
		
		//System.out.println("buriedhumans:"+world.getBuriedHumans());
		
		Set<EntityID> buriedHumans = buriedHumanFactory.getTotalBuriedHuman();

		buriedEntrances.clear();
		for (EntityID entityID : buriedHumans) {
			StandardEntity SE = world.getEntity(entityID);
			if (!(SE instanceof Human))
				continue;
			Human human = (Human) SE;
			if(human.getHP()==0)//////////////////////
				continue;
			EntityID positionID = human.getPosition();
			StandardEntity positionSE = world.getEntity(positionID);

			if (!(positionSE instanceof Building))
				continue;
			Building building = (Building) positionSE;

			if (currentCluster.containtBuilding(entityID)) {
				for (Road road : world.getEntrance().getEntrance(building)) {
					if (traversalEntranceSet.contains(road))
						buriedEntrances.add(road.getID());
				}
			}
		}
	}

	/**
	 * 201410
	 * @throws ActionCommandException 
	 */
	private void clearEntranceForCivilian() throws ActionCommandException {
		List<EntityID> buildingList = new ArrayList<>();
		List<EntityID> humanList = new ArrayList<>();
		for (EntityID next : getChanged()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof Building) {
				buildingList.add(next);
			}else if (entity instanceof Human) {
				humanList.add(next);
			}
		}
		for (EntityID buildingID : buildingList ) {
			Building building =(Building) world.getEntity(buildingID);
			for (EntityID humanID : humanList) {
				Human human = (Human) world.getEntity(humanID);
				EntityID humanPositionID = human.getPosition();
				if (humanPositionID.getValue() == building.getID().getValue()) {
					Set<Road> entrances = world.getEntrance().getEntrance(building);
					for (Road entrance : entrances) {
						if (entrance.isBlockadesDefined() && entrance.getBlockades().size() > 0) {
							double dis = Ruler.getDistance(entrance.getLocation(world), me().getLocation(world));
							if (dis < 10000) {
								path = router.getAStar(me(), (Area)entrance, router.getPfCostFunction());
								move(path);
							}
						}
					}
				}
			}
		}
	}
	/**
	 * If a pf finished its current cluster, then move to next one.
	 */
	private void expandCluster() {
		Cluster nextClusterPF = getNextCluster();
		if (nextClusterPF == null) {
			//System.out.println("cant to find next cluster");
		} else {
			expandedClusters.add(nextClusterPF);
			needToExpandClusters.remove(nextClusterPF);
			currentCluster = nextClusterPF;
			currentClusterIndex = clusters.indexOf(currentCluster);

			traversalRefugeSet.addAll(nextClusterPF.getRefugeList());
			traversalEntranceSet.addAll(nextClusterPF.getEntranceList());

			for (CsuZone zone : nextClusterPF.getZoneList()) {
				buildingCount += zone.size();
			}

			searchBurningFlag = true;

			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out.println("time = " + time + ", " + me()
						+ " expand cluster");
			}
		}
	}

	private Cluster getNextCluster() {

		ArrayList<Cluster> canToWorkClusters = new ArrayList<Cluster>();
		canToWorkClusters.addAll(clusters);
		canToWorkClusters.removeAll(cantWorkClusters);
		// ClusterPF targetCluster;
		for (Cluster next : cantWorkClusters) {
			if (expandedClusters.contains(next)) {
				continue;
			}
			Cluster closestCluster = Clustering.getClosestCluster(
					canToWorkClusters, next);
			if (closestCluster.equals(currentCluster)) {
				return closestCluster;
			}
		}
		return Clustering.getClosestCluster(needToExpandClusters,
				currentCluster);

		/*
		 * this.cantWorkClusters.removeAll(expandedClusters);
		 * 
		 * Cluster closestCluster =
		 * Clustering.getClosestCluster(cantWorkClusters, currentCluster);
		 * 
		 * if (closestCluster != null) { return closestCluster; } else { return
		 * Clustering.getClosestCluster(needToExpandClusters, currentCluster); }
		 */
	}

	/**
	 * 更新不能work的
	 */
	private void updatePfCannotToWork() {
		this.cantWorkClusters.clear();

		// if buried
		Set<EntityID> buriedID = world.getBuriedHumans().getTotalBuriedHuman();
		for (EntityID entityID : buriedID) {
			StandardEntity SE = world.getEntity(entityID);
			if (SE instanceof PoliceForce) {
				int indexOfCluster = Clustering.getClusterIndexAgentBelongTo(
						entityID, clusters);
				cantWorkClusters.add(clusters.get(indexOfCluster));
				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
							.println("time = "
									+ time
									+ ", "
									+ me()
									+ "cantWorkCluster add "
									+ indexOfCluster
									+ " "
									+ entityID
									+ "----- class: PoliceForceAgent, method: updatePfCannotToWork()");
				}
			}
		}

		Collection<StandardEntity> pfs = world
				.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);

		for (StandardEntity next : pfs) {
			Human human = (Human) next;
			StandardEntity position = world.getEntity(human.getPosition());
			// position为human
			if (position instanceof Human) {
				int indexOfCluster = Clustering.getClusterIndexAgentBelongTo(
						next.getID(), clusters);
				cantWorkClusters.add(clusters.get(indexOfCluster));

				if (AgentConstants.PRINT_TEST_DATA_PF) {
					System.out
							.println("time = "
									+ time
									+ ", "
									+ me()
									+ "cantWorkCluster add "
									+ indexOfCluster
									+ " "
									+ next
									+ "----- class: PoliceForceAgent, method: updatePfCannotToWork()");
				}

			} else if (position instanceof Area) {
				// <position 周期数>
				Pair<EntityID, Integer> positionPair = pfMap.get(next);

				if (positionPair == null) {
					positionPair = new Pair<EntityID, Integer>(
							position.getID(), new Integer(1));
				} else {
					// 与上个周期相同位置
					if (position.getID().getValue() == positionPair.first()
							.getValue()) {
						positionPair = new Pair<EntityID, Integer>(
								positionPair.first(), positionPair.second() + 1);

						// building中停留5个周期
						if (position instanceof Building) {// include refuge
							if (positionPair.second() >= 5) {
								int indexOfCluster = Clustering
										.getClusterIndexAgentBelongTo(
												next.getID(), clusters);
								cantWorkClusters.add(clusters
										.get(indexOfCluster));

								if (AgentConstants.PRINT_TEST_DATA_PF) {
									System.out
											.println("time = "
													+ time
													+ ", "
													+ me()
													+ "cantWorkCluster add "
													+ indexOfCluster
													+ " "
													+ next
													+ "----- class: PoliceForceAgent, method: updatePfCannotToWork()");
								}

							}
							// 某个位置停留 35个周期
						} else if (positionPair.second() >= 35) {// 35 should be
																	// change
							int indexOfCluster = Clustering
									.getClusterIndexAgentBelongTo(next.getID(),
											clusters);
							cantWorkClusters.add(clusters.get(indexOfCluster));

							if (AgentConstants.PRINT_TEST_DATA_PF) {
								System.out
										.println("time = "
												+ time
												+ ", "
												+ me()
												+ "cantWorkCluster add "
												+ indexOfCluster
												+ " "
												+ next
												+ "----- class: PoliceForceAgent, method: updatePfCannotToWork()");
							}

						}
					} else {
						positionPair = new Pair<EntityID, Integer>(
								position.getID(), new Integer(1));
					}
				}
				pfMap.put(next.getID(), positionPair);
			}
		}
	}

	private void searchingBurningBuildingCritical()
			throws ActionCommandException {

		if (!searchBurningFlag) {
			return;
		}

		if (criticalOfBurningZone.size() == 0) {
			CsuZone searchZone = getBurningZone();
			if (searchZone == null) {
				return;
			}

			if (hadSearchBuringBuildings.size() >= buildingCount) {
				searchBurningFlag = false;
				return;
			}
			criticalOfBurningZone = searchZone.getCriticalAreaOfZone();

			for (CSUBuilding csuBuilding : searchZone) {
				hadSearchBuringBuildings.add(csuBuilding.getSelfBuilding());
			}
		}

		if (criticalOfBurningZone.size() == 0) {
			return;
		} else {
			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " traversal burning zones"
								+ " ----- class: PoliceForceAgent, method: traversalRoads()");
			}

			traversalCriticalOfZone(criticalOfBurningZone);
		}

	}

	private void searchingBurningBuilding() throws ActionCommandException {
		if (!searchBurningFlag) {
			return;
		}

		if (entrancesOfBurningZone.size() == 0) {
			CsuZone searchZone = getBurningZone();
			if (searchZone == null) {
				return;
			}

			if (hadSearchBuringBuildings.size() >= buildingCount) {
				searchBurningFlag = false;
				return;
			}

			entrancesOfBurningZone = getEntranceOfZone(searchZone);

			for (CSUBuilding csuBuilding : searchZone) {
				hadSearchBuringBuildings.add(csuBuilding.getSelfBuilding());
				Building bu = csuBuilding.getSelfBuilding();
				if (!bu.isFierynessDefined())
					continue;
				// ??
				if (bu.getFieryness() == 8 || bu.getFieryness() == 7
						|| bu.getFieryness() == 3) {
					entrancesOfBurningZone.removeAll(world.getEntrance()
							.getEntrance(bu));
				}
			}
			traversalEntranceSet.removeAll(entrancesOfBurningZone);
		}

		if (entrancesOfBurningZone.size() == 0) {
			return;
		} else {
			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out
						.println("time = "
								+ time
								+ ", "
								+ me()
								+ " traversal burning zones"
								+ " ----- class: PoliceForceAgent, method: traversalRoads()");
			}

			traversalRoads(entrancesOfBurningZone);
		}
	}

	private CsuZone getBurningZone() {
		if (hadSearchBuringBuildings.size() >= buildingCount) {
			// had done the burning building
			return null;
		}

		nearBurningBuildings.clear();
		nearBurningBuildings.addAll(world.getBurningBuildings());
		nearBurningBuildings.removeAll(hadSearchBuringBuildings);

		// 应该为当前分区
		// Cluster initiCluster = clusters.get(assignedClusterIndex);
		Cluster initiCluster = clusters.get(currentClusterIndex);

		FOR: for (Iterator<Building> itor = nearBurningBuildings.iterator(); itor
				.hasNext();) {
			Building building = itor.next();

			if (initiCluster.containtBuilding(building.getID())) {
				if (building.isFierynessDefined()) {
					continue FOR;
				}
				if (building.getFieryness() != 8
						&& building.getFieryness() != 7) {
					continue FOR;
				}
			}

			for (Integer next : initiCluster.getNeighbours()) {
				Cluster cluster = clusters.get(next.intValue());
				if (cluster.containtBuilding(building.getID())) {
					if (building.isFierynessDefined()) {
						continue FOR;
					}
					if (building.getFieryness() != 8
							&& building.getFieryness() != 7) {
						continue FOR;
					}
				}
			}

			itor.remove();
		}

		if (nearBurningBuildings.isEmpty())
			return null;

		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		List<EntityID> pathList = router.getMultiAStar(location(),
				nearBurningBuildings, costFunc, selfL);

		EntityID destination = pathList.get(pathList.size() - 1);
		CsuZone searchZone = world.getZones().getBuildingZone(destination);

		return searchZone;
	}

	/**
	 * Get all entrance of a zone, excluding those entrances whose all
	 * beighbours are buildings.
	 * 
	 * @param zone
	 *            the target zone
	 * @return the entrance of target zone
	 */
	private Set<Road> getEntranceOfZone(CsuZone zone) {
		Set<Road> entrancesOfZone = zone.getAllEntranceRoad();
		List<EntityID> neighbours;
		for (Road road : entrancesOfZone) {
			neighbours = road.getNeighbours();
			int flag = 0;
			for (EntityID entityID : neighbours) {
				if (world.getEntity(entityID) instanceof Road) {
					flag = 1;
					break;
				}
			}
			if (flag == 0) {
				entrancesOfZone.remove(road);
			}
		}
		return entrancesOfZone;
	}

	public double distanceToArea(Area entrance) {
		if (me().isXDefined() && me().isYDefined() && entrance.isXDefined()
				&& entrance.isYDefined()) {
			Point2D firstPoint = new Point2D(me().getX(), me().getY());
			Point2D secondPoint = new Point2D(entrance.getX(), entrance.getY());
			double d = GeometryTools2D.getDistance(firstPoint, secondPoint);
			return d;
		}
		return -1;
	}

	@Override
	public void move(List<EntityID> path) throws ActionCommandException {
		if (path.size() > 2) {
			EntityID id_1 = path.get(0);
			EntityID id_2 = path.get(1);
			if (id_1.getValue() == id_2.getValue())
				path.remove(0);
		}
		this.clearStrategy.updateClearPath(path);
		this.clearStrategy.clear();
		sendMove(time, path);
		this.lastCyclePath = null;
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}

	@Override
	public void move(List<EntityID> path, int destX, int destY)
			throws ActionCommandException {
		if (path.size() > 2) {
			EntityID id_1 = path.get(0);
			EntityID id_2 = path.get(1);
			if (id_1.getValue() == id_2.getValue())
				path.remove(0);
		}

		this.clearStrategy.updateClearPath(path);
		this.clearStrategy.clear();
		sendMove(time, path, destX, destY);
		this.lastCyclePath = null;
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}

	@Override
	public String toString() {
		return "CSU_YUNLU police force agent";
	}
}
