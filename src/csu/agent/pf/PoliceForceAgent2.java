package csu.agent.pf;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.pf.PFLastTaskType.PFClusterLastTaskEnum;
import csu.agent.pf.clearStrategy.I_ClearStrategy;
import csu.agent.pf.clearStrategy.POSBasedStrategy;
import csu.agent.pf.cluster.Cluster;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.model.route.pov.CostFunction;

public class PoliceForceAgent2 extends AbstractPoliceForceAgent {

//	/** Count how long this PF Agent has following a specified platoon Agent. */
//	private int counter = 0;
//	/** The platoon Agent this PF Agent followed to help last cycle */
//	private Human oldNearestAgent = null;
//	private HashSet<Human> clearHuman = new HashSet<Human>();
//	private int fbAroundThreshold;

	private I_ClearStrategy clearStrategy;

	@Override
	protected void initialize() {
		super.initialize();
		assignClearStrategy();

//		fbAroundThreshold = world.getConfig().extinguishableDistance * 2;
		currentCluster = clusters.get(currentClusterIndex);
		taskTarget = new PFLastTaskTarget();

		traversalEntranceSet = new HashSet<Road>();
		traversalEntranceSet.addAll(currentCluster.getEntranceList());
		
		traversalCriticalAreas = new HashSet<>();
		traversalCriticalAreas.addAll(currentCluster.getCriticalAreas());

		traversalRefugeSet = new HashSet<Refuge>();
		traversalRefugeSet.addAll(currentCluster.getRefugeList());

		clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
		System.out.println(toString() + " was connected. [id=" + getID()
				+ ", uniform=" + getUniform() + "]");
	}

	@Override
	protected void prepareForAct() throws TimeOutException {
		super.prepareForAct();
		world.getCriticalArea().update(router);
		this.updateTaskList();
	}

	@Override
	protected void act() throws ActionCommandException, TimeOutException {
		cannotClear();
		this.leaveBurningBuilding();
		blockedClear();

		this.clearStrategy.updateClearPath(lastCyclePath);
		this.clearStrategy.clear();
		super.act();
		
		continueLastTask();
		traversalRefuge();
		helpStuckHuman();
		traversalCritical();
		traversalEntrance();
		expandCluster();
		// oldStrategy();
		randomWalk();
	}
	
	private void blockedClear() throws ActionCommandException {
		/*if (isBlocked()) {
			this.lastCyclePath = null;
			this.lastTask = PFLastTaskEnum.NO_TAST;
			Blockade target = this.clearStrategy.blockedClear();
			if (target != null) {
				this.clearStrategy.doClear(null, null, target);
			} else {
				if (this.lastTask == PFLastTaskEnum.HELP_STUCK_HUMAN) {
					world.getStuckedAgents().remove(taskTarget.getHelpStuckHuman().getID());
				} else {
					randomWalk();
				}
			}
		}*/
	}
	
	/*private void clearAndMove () throws csu.agent.Agent.ActionCommandException {
		
		if (lastCyclePath == null) {
			return;
		}
		if (world.getEntity(lastCyclePath.get(lastCyclePath.size() - 1)) instanceof Building) {
			
			this.clearStrategy.updateClearPath(lastCyclePath);
			this.clearStrategy.clear();

			send(new AKMove(getID(), time, lastCyclePath, -1, -1));
			lastCyclePath = null;
			throw new ActionCommandException(StandardMessageURN.AK_MOVE);
			
		} else if (world.getEntity(lastCyclePath.get(lastCyclePath.size() - 1)) instanceof Road) {
			this.clearStrategy.updateClearPath(lastCyclePath);
			this.clearStrategy.clear();

			moveToRoadwithBlockade(lastCyclePath);
			lastCyclePath = null;
			throw new ActionCommandException(StandardMessageURN.AK_MOVE);
		}
	}*/

	private void assignClearStrategy() {
		// clearStrategy = new AroundBasedStrategy();
		// clearStrategy = new CenterAreaBasedStrategy(world);
		// clearStrategy = new CenterLineBasedStrategy(world);
		// clearStrategy = new TangentBasedStrategy(world);
		clearStrategy = new POSBasedStrategy(world);
	}

	private void updateTaskList() {
		StandardEntity entity = null;
		for (EntityID changed : getChanged()) {
			entity = world.getEntity(changed);
			if (entity instanceof Road) {
				Road road = (Road) entity;
				if (!world.getEntrance().isEntrance(road))
					continue;
				if (road.isBlockadesDefined() && road.getBlockades().size() > 0)
					continue;
				/*for (EntityID neighbour : road.getNeighbours()) {
					entity = world.getEntity(neighbour);
					if (entity instanceof Road) {
						Road ne_r = (Road) entity;
						if (ne_r.isBlockadesDefined() && ne_r.getBlockades().size() > 0)
							continue MARK;
					}
				}*/
				this.traversalEntranceSet.remove(road);
			}
		}
	}

	/*private void oldStrategy() throws ActionCommandException, TimeOutException {
		if (world.getPoliceForceList().size() >= 5) {
			int n = world.getPoliceForceList().size() / 5;
			if (getUniform() < n) {
				// let pf clear the blockade near fires
				this.clearBlockadeAroundFire();
				if (!isNoRadio())
					// move
					this.radioExtingish();
				// move
				this.moveToFires();
			}
			if (n <= getUniform() && getUniform() < 2 * n) {
				// let pf clear the blockade near buried human
				// move
				clearBuriedHuman();
			}

			if (2 * n <= getUniform() && getUniform() < 3 * n) {
				helpAgents();
				// eraseUnreachable();
			}
			if (3 * n <= getUniform() && getUniform() < 4 * n) {
				if (time < 60) {
					clearCriticalArea();
				}
			}
		}
		// clearNearLocateInBlocade(); // clear(b)

		careSelf();
		
		if (isAggregator()) {
			// ?
			aggregatorStay();
		} else {
			if (isMessenger()) {
				messengerLoop(25);
			}
			if (time < 70) {
				helpAgents();
			}
			if (time < (world.getConfig().timestep / 2) - 10) {
				clearBuriedHuman();
			}
			if (time < 30) {
				clearCriticalArea();
			}
			if (time > world.getConfig().timestep / 2)
				clearAround();
			searchFires();
			// eraseUnreachable();

			if (world.getTime() < Math.min(60, world.getConfig().timestep)
					|| world.getConfig().timestep - 30 < world.getTime()) {
				lookupSearchBuildings();
			}
			enterSearchBuildings();
		}
	}*/

	private void cannotClear() throws csu.agent.Agent.ActionCommandException {

		if (!me().isHPDefined() || me().getHP() <= 1000) {

			clusterLastTaskType = PFClusterLastTaskEnum.CANNOT_TO_CLEAR;
			Collection<StandardEntity> allReguge = world
					.getEntitiesOfType(StandardEntityURN.REFUGE);

			List<EntityID> pathList = router.getMultiAStar(location(),
					allReguge, router.getNormalCostFunction(), new Point(me().getX(), me().getY()));
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out.println("In time: " + time + " Agent: " + me()
								+ " can't to clear and move to refuge" + clusterLastTaskType
								+ " ----- class: PoliceForceAgent, method: cantToClear()");

				for (EntityID next : pathList) {
					System.out.println(next);
				}
			}
			lastCyclePath = pathList;
			move(pathList);
		}
	}

	private void leaveBurningBuilding() throws ActionCommandException {
		StandardEntity entity;
		if (location() instanceof Building) {
			Building building = (Building) location();
			if ((building.isFierynessDefined() && building.isOnFire())
					|| (building.isTemperatureDefined() && building.getTemperature() > 35)) {

				if (AgentConstants.PRINT_TEST_DATA) {
					System.out.println("Agent "
									+ me()
									+ " in time: "
									+ time
									+ " was in a dangerous Building and trying to go out it."
									+ " ----- class: PoliceForceAgent, method: leaveBurningBuilding()");
				}

				for (EntityID next : building.getNeighbours()) {
					entity = world.getEntity(next);
					if (entity instanceof Building) {
						unlookupedBuildings.remove(entity);
					}
				}
				moveToRefuge();
			}
		}
	}

	private void continueLastTask()
			throws csu.agent.Agent.ActionCommandException {

		EntityID taskEntityID;
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

			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " continue last task: "
								+ taskEntityID.getValue()
								+ "building ----- class: PoliceForceAgent, method: continueLastTast()");
			}

			lastCyclePath = router
					.getAStar(me(), taskTarget.getTraversalRefuge(),
							router.getPfCostFunction());

			move(lastCyclePath);
			// TODO
			// clearAndMove ();

			// lastTask = PFLastTaskEnum.NOTAST;
			// break;
		case TRAVERSAL_CRITICAL_AREA:
			taskEntityID = taskTarget.getTraversalCriticalArea().getID();
			if (location().getID().getValue() == taskEntityID.getValue()) {
				clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
				break;
			}
			CostFunction costFunc = router.getPfCostFunction();
			lastCyclePath = router.getAStar(me(), taskTarget.getTraversalCriticalArea(), costFunc);
			
			if (AgentConstants.PRINT_TEST_DATA) {
				String str = null;
				for (EntityID next : lastCyclePath) {
					if (str == null) {
						str = next.getValue() + "";
					} else {
						str = str + ", " + next.getValue();
					}
				}
				
				System.out.println("time = " + time + ", agent = " + me() + " continue traversal " +
						"critical: " + taskEntityID.getValue() + ", path = [" + str + "],"
						+ " ----- class: PoliceForceAgent, method: continueLastTask()");
			}
			move(lastCyclePath);
			break;
		case TRAVERSAL_ENTRANCE:
			taskEntityID = taskTarget.getTraversalEntrance().getID();

			if (changed.getChangedEntities().contains(taskEntityID)) {
				Road road = (Road) world.getEntity(taskEntityID);
				if (road.isBlockadesDefined()
						&& road.getBlockades().size() == 0) {
					clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
					break;
				}
			}
			
			if (this.location() instanceof Road) {
				if (((Road) this.location()).equals(taskTarget.getTraversalEntrance())) {
					if (((Road) this.location()).isBlockadesDefined()
							&& ((Road) this.location()).getBlockades().size() == 0) {
						clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
						break;

					}
				}
			}
			lastCyclePath = router.getAStar(me(),
					taskTarget.getTraversalEntrance(), router.getPfCostFunction());

			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " continue last tast: "
								+ taskEntityID.getValue()
								+ "building ----- class: PoliceForceAgent, method: continueLastTast()");
				for (EntityID next : lastCyclePath) {
					System.out.println(next);
				}
			}
			move(lastCyclePath);
			// TODO
			// clearAndMove ();
			break;
		case HELP_STUCK_HUMAN:
			/*taskEntityID = taskTarget.getHelpStuckHuman().getID();
			if (world.getStuckedAgents().contains(taskEntityID)) {
				lastTask = PFLastTaskEnum.NO_TAST;
				break;
			}
			StandardEntity hu_posi_e = world.getEntity(taskTarget.getHelpStuckHuman().getPosition());
			if (hu_posi_e instanceof Area) {
				Point selfL = new Point(world.getSelfLocation().first(), world
						.getSelfLocation().second());
				Area stucHum_A = (Area) hu_posi_e;
				if (isVisible(stucHum_A.getID())) {
					lastCyclePath = router.getAStar(location(), stucHum_A, selfL);
				} else {
					lastCyclePath = router.getAStar(location(), 
							stucHum_A, router.getPfCols
							stFunction(), selfL);
				}
				move(lastCyclePath, taskTarget.getHelpStuckHuman().getX(), 
						taskTarget.getHelpStuckHuman().getY());
			}*/
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;
		case NO_TAST:
			break;
		default:
			clusterLastTaskType = PFClusterLastTaskEnum.NO_TAST;
			break;
		}
	}

	private void traversalRefuge() throws ActionCommandException {
		if (traversalRefugeSet.size() == 0) {
			return;
		}

		lastCyclePath = router
				.getMultiAStar(location(), traversalRefugeSet, router
						.getPfCostFunction(), new Point(me().getX(), me().getY()));

		Building destination = (Building) world.getEntity(lastCyclePath
				.get(lastCyclePath.size() - 1));
		traversalRefugeSet.remove(destination);
		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_REFUGE;
		taskTarget.setTraversalRefuge(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA) {
			System.out
					.println("In time: "
							+ time
							+ " Agent: "
							+ me()
							+ " traversalRefuge: "
							+ destination.getID().getValue()
							+ " ----- class: PoliceForceAgent, method: traversalRefuge()");
			System.out.println(destination);
			for (EntityID next : lastCyclePath) {
				System.out.println(next);
			}
		}

		move(lastCyclePath);
		// TODO
		// clearAndMove ();
	}

	private void traversalCritical() throws ActionCommandException {
		if (traversalCriticalAreas.size() == 0)
			return;
		CostFunction costFunc = router.getPfCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router.getMultiAStar(location(), traversalCriticalAreas, costFunc, selfL);
		
		Area destination = world.getEntity(lastCyclePath.get(lastCyclePath.size() - 1), Area.class);
		traversalCriticalAreas.remove(destination);
		
		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_CRITICAL_AREA;
		taskTarget.setTraversalCriticalArea(destination);
		taskTarget.setEntlityList(lastCyclePath);
		
		if (AgentConstants.PRINT_TEST_DATA) {
			String str = null;
			for (EntityID next : lastCyclePath) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}
			
			System.out.println("time = " + time + ", agent = " + me() + " traversal critical: " 
					+ destination.getID().getValue() + ", path = [" + str + "],"
					+ " ----- class: PoliceForceAgent, method: traversalCritical()");
		}
		
		move(lastCyclePath);
	}
	
	private void traversalEntrance()
			throws csu.agent.Agent.ActionCommandException {
		if (traversalEntranceSet.size() == 0) {
			return;
		}

		lastCyclePath = router.getMultiAStar(location(), traversalEntranceSet, router
						.getPfCostFunction(), new Point(me().getX(), me()
						.getY()));
		Road destination = (Road) world.getEntity(lastCyclePath
				.get(lastCyclePath.size() - 1));

		traversalEntranceSet.remove(destination);

		clusterLastTaskType = PFClusterLastTaskEnum.TRAVERSAL_ENTRANCE;
		taskTarget.setTraversalEntrance(destination);
		taskTarget.setEntlityList(lastCyclePath);

		if (AgentConstants.PRINT_TEST_DATA) {
			System.out
					.println("In time: "
							+ time
							+ " Agent: "
							+ me()
							+ " traversalEntrance: "
							+ destination.getID().getValue()
							+ " ----- class: PoliceForceAgent, method: traversalEntrance()");
			System.out.println(destination);
			System.out.println(traversalEntranceSet.size());
			for (EntityID next : lastCyclePath) {
				System.out.println(next);
			}
		}
		
		move(lastCyclePath);
		// TODO
		// moveToRoadwithBlockade(lastCyclePath);
		// clearAndMove ();
	}

	/*private void moveToRoadwithBlockade(List<EntityID> pathList) {
		Road destination = null;
		destination = (Road) world.getEntity(pathList.get(pathList.size() - 1));

		int x = -1;
		int y = -1;
		if (destination.isBlockadesDefined()
				&& destination.getBlockades().size() > 0) {

			EntityID blockadeEntityID = destination.getBlockades().get(0);
			Blockade blockade = (Blockade) world.getEntity(blockadeEntityID);

			if (blockade.isXDefined() && blockade.isYDefined()) {

				x = blockade.getX();
				y = blockade.getY();
			}
		}
		// move(pathList);
		send(new AKMove(getID(), time, lastCyclePath, x, y));
		// lastCyclePath = null;
		// throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}

	private boolean shouldRemove(Refuge refuge) {
		for (Road entrance : world.getEntrance().getEntrance((Building) refuge)) {
			boolean flag_1 = entrance.getBlockades().isEmpty();
			boolean flag_2 = true;
			for (EntityID next : entrance.getNeighbours()) {
				StandardEntity entity = world.getEntity(next);
				if (entity instanceof Building)
					continue;
				Road neig = (Road) entity;
				if (neig.isBlockadesDefined() && neig.getBlockades().size() > 0)
					flag_2 = false;
			}
			if (flag_1 && flag_2)
				return true;
		}
		return false;
	}
	
	private void clearRefuge() throws ActionCommandException {
		if (refugeList.size() == 0) {
			return;
		}
		Refuge refuge = null;
		int n = refugeList.size();
		int start = random.nextInt(n);
		for (int i = 0; i < n; i++) {
			// refuge = (Refuge) Collections.min(refugeList, new DistanceSorter(
			// me(), world));
			refuge = refugeList.get((i + start) % n);
			// if (world.getRouter().isSureReachable(refuge)) {
			// continue;
			// } else {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " move to clear cluster refuge."
								+ " ----- class: PoliceForceAgent, method: clearRefuge()");
				System.out.println(world.getRouter().isSureReachable(refuge));
			}

			lastCyclePath = router.getAStar(location(), refuge, router
					.getPfCostFunction(), new Point(me().getX(), me().getY()));
			move(lastCyclePath);
			// }
		}
	}

	private void clearEntrance() throws csu.agent.Agent.ActionCommandException {

		if (entranceList.size() == 0) {
			return;
		}

		Road closestEntrance = null;
		double minDistance = Double.MAX_VALUE;
		double currentDistance;
		// ArrayList<Road> tempList = new ArrayList<Road>();
		for (Road entrance : entranceList) {

			if (world.getEntrance().isEntrance(entrance)
					&& (entrance.isBlockadesDefined() && entrance
							.getBlockades().size() == 0)) {
				continue;
				// tempList.add(entrance);
			} else {
				currentDistance = distanceToArea(entrance);
				if (currentDistance > 0 && currentDistance < minDistance) {
					closestEntrance = entrance;
					minDistance = currentDistance;
				}
			}
		}
		// entranceList.removeAll(tempList);
		if (closestEntrance != null) {
			// if (location().equals((Area) closestEntrance)) {

			if (closestEntrance.isBlockadesDefined()
					&& closestEntrance.getBlockades().size() > 0) {

				List<EntityID> pathTemp = new ArrayList<EntityID>();
				// pathTemp.add(entityIDTemp);
				pathTemp = router.getAStar(me(), closestEntrance,
						router.getPfCostFunction());
				EntityID blockadeEntityID = closestEntrance.getBlockades().get(
						0);
				Blockade blockade = (Blockade) world
						.getEntity(blockadeEntityID);

				if (!(blockade.isXDefined() && blockade.isYDefined())) {
					System.out.println("x or y of blockade is not defined!");
					return;
				}

				// int destX = blockade.getX();
				// int destY = blockade.getY();
				if (AgentConstants.PRINT_TEST_DATA) {
					System.out.println("In time: "
									+ time
									+ " Agent: "
									+ me()
									+ " clear entrances."
									+ " ----- class: PoliceForceAgent, method: clearEntrance()");
					for (EntityID entityID : pathTemp) {
						System.out.println(entityID);
					}
					System.out.println(closestEntrance);

				}

				lastCyclePath = pathTemp;
				sendMove(time, pathTemp);
				throw new ActionCommandException(StandardMessageURN.AK_MOVE);
			}
			return;
		}
	}*/
	
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

	private void helpStuckHuman() throws csu.agent.Agent.ActionCommandException {
		Collection<StandardEntity> PFEntity = world
				.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);

		List<EntityID> closetHuman = new ArrayList<EntityID>();
		for (EntityID next : world.getStuckedAgents()) {

			if (next.getValue() == me().getID().getValue())
				continue;

			StandardEntity stuckHuman = world.getEntity(next);
			int minInt = Integer.MAX_VALUE;
			StandardEntity closetPFEntity = null;

			for (StandardEntity next1 : PFEntity) {
				int dis = world.getDistance(next1, stuckHuman);
				if (dis > 0 && dis <= minInt ) {
					minInt = dis;
					closetPFEntity = next1;
				}
			}

			if (closetPFEntity != null) {
				PoliceForce PF = (PoliceForce) closetPFEntity;
				if (PF.equals(me())) {
					closetHuman.add(next);
				}
			}
		}

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
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " helpStuckHuman."
								+ " ----- class: PoliceForceAgent, method: helpStuckHuman()");
			}
			 
			StandardEntity hu_posi_e = world.getEntity(targetHuman.getPosition());
			if (hu_posi_e instanceof Area) {
				Point selfL = new Point(world.getSelfLocation().first(), world.getSelfLocation().second());
				Area stucHum_A = (Area) hu_posi_e;
				if (isVisible(stucHum_A.getID())) {
					lastCyclePath = router.getAStar(location(), stucHum_A, selfL);
				} else {
					lastCyclePath = router.getAStar(location(), 
							stucHum_A, router.getPfCostFunction(), selfL);
				}
				move(lastCyclePath, targetHuman.getX(), targetHuman.getY());
			}
			
			// TODO
			// clearAndMove ();
		}
	}

	/**
	 * if the pf has cleared his cluster, then change
	 */
	private void expandCluster() {
		int nextClusterIndex = getNextClusterIndex();
		if (nextClusterIndex == -1) {
		}
		if (0 <= nextClusterIndex
				&& nextClusterIndex < world.getEntitiesOfType(
						StandardEntityURN.POLICE_FORCE).size()) {

			Cluster nextClusterPF = clusters.get(nextClusterIndex);

			traversalRefugeSet.addAll(nextClusterPF.getRefugeList());
			traversalEntranceSet.addAll(nextClusterPF.getEntranceList());

			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " expandCluster."
								+ " ----- class: PoliceForceAgent, method: expandCluster()");
			}
		}
	}

	private int getNextClusterIndex() {
		int nextClusterIndex = -1;
		ArrayList<CsuZone> myZoneList = new ArrayList<CsuZone>();

		for (CsuZone next : myZoneList) {
			for (CsuZone next1 : next.getNeighbourZones()) {
				if (!myZoneList.contains(next1)) {
					for (Cluster next2 : clusters) {
						if (next2.getZoneList().contains(next1)) {
							nextClusterIndex = clusters.indexOf(next2);
						}
					}
				}
			}
		}

		return nextClusterIndex;
	}

	/*private void clearBlockadeAroundFire() throws ActionCommandException {
		FireBrigade targetFirebrigade = null;
		boolean targetFlag = false;
		for (FireBrigade fb : world.getFireBrigadeList()) {
			if (fb.isPositionDefined()) {
				int counter = 0;
				EntityID fbPosition = fb.getPosition();
				Collection<StandardEntity> inRange = world.getObjectsInRange(
						fbPosition, fbAroundThreshold);
				for (StandardEntity next : inRange) {
					if (next instanceof FireBrigade) {
						counter++;
						if (counter > 5) {
							targetFlag = true;
							break;
						}
					}
				}
			}
			if (targetFlag) {
				targetFirebrigade = fb;
				break;
			}
		}
		if (targetFirebrigade != null) {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " move to clear a far refuge."
								+ " ----- class: PoliceForceAgent, method: clearBlockadeAroundFire()");
			}
			move(targetFirebrigade);
		}
	}*/

	/*private void searchFires() throws ActionCommandException {
		List<Building> ranking = world.getEnergyFlow().getInTotalRanking();
		int u = getUniform();
		if (u < ranking.size()) {
			Building myBuilding = ranking.get(u);
			if (location() instanceof Building) {
				return;
			}
			int lastChanged = world.getTimestamp().getLastChangedTime(
					myBuilding.getID());
			if (myBuilding.getTotalArea() < world.getEnergyFlow().getInTotal(
					myBuilding)
					&& (lastChanged == -1 || time / 3 < time - lastChanged)) {
				if (AgentConstants.PRINT_TEST_DATA) {
					System.out
							.println("In time: "
									+ time
									+ " Agent: "
									+ me()
									+ " move to search fires."
									+ " ----- class: PoliceForceAgent, method: searchFires()");
				}
				// clearAround();

				moveFront(myBuilding, router.getPfCostFunction());
			}
		}
	}

	private void clearBuriedHuman() throws ActionCommandException {
		HashSet<Area> humanArea = new HashSet<Area>();
		for (EntityID id : world.getBuriedHumans().getTotalBuriedHuman()) {
			Human hm = (Human) world.getEntity(id);
			if (hm.isPositionDefined() && (hm.getHP() > 100)) {
				StandardEntity pos = hm.getPosition(world);
				if (pos instanceof Area) {
					Area area = (Area) pos;
					if (!area.isBlockadesDefined()
							|| area.getBlockades().size() == 0)
						continue;
					humanArea.add(area);
				}
			}
		}

		if (!humanArea.isEmpty()) {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " is move to clear human"
								+ " ----- class: PoliceForceAgent, method: clearBuriedHuman()");
			}
			move(humanArea, router.getPfCostFunction());
		}
	}*/

	/*private void radioExtingish() throws ActionCommandException {
		// final int t =
		// (world.getStuckHandle().isLocateInBlockade(me().getLocation(world))
		// != null) ? 8 : 3;
		Building minDifficultyBuilding = null;
		double minDifficulty = Integer.MAX_VALUE;
		double minDifficultyAffect = 0.0;

		for (Building building : world.getBurningBuildings()) {
			if (!building.isBlockadesDefined()
					|| building.getBlockades().size() == 0) {
				continue;
			}
			if (world.getTime()
					- world.getTimestamp().getLastChangedTime(building.getID()) >= 3) {
				continue;
			}
			if (building.isFierynessDefined()
					&& building.getFierynessEnum() == Fieryness.INFERNO) {
				continue;
			}
			double area = (building.isTotalAreaDefined()) ? building
					.getTotalArea() : 1.0;
			double affected = world.getEnergyFlow().getIn(building);
			double difficulty = area * affected;
			if (world.getBurningBuildings().contains(building)
					&& difficulty < minDifficulty) {
				minDifficultyBuilding = building;
				minDifficulty = difficulty;
				minDifficultyAffect = world.getEnergyFlow().getOut(building);
			} else if (Math.abs(minDifficulty - difficulty) < 500.0) {
				double affect = world.getEnergyFlow().getOut(building);
				if (minDifficultyAffect < affect) {
					minDifficultyBuilding = building;
					minDifficulty = difficulty;
					minDifficultyAffect = world.getEnergyFlow()
							.getOut(building);
				}
			}
		}
		if (minDifficultyBuilding != null) {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out.println("In time: " + time + " Agent: " + me()
						+ " is radio move. ----- class: "
						+ "PoliceForceAgent, method: radioExtinguish()");
			}
			move(minDifficultyBuilding, router.getPfCostFunction());
		}
	}*/

	/*private void moveToFires() throws ActionCommandException {
		if (world.getBurningBuildings().isEmpty())
			return;
		Building minValueBuilding = null;
		double minValue = Integer.MAX_VALUE;

		for (Building building : world.getBurningBuildings()) {
			if (!building.isBlockadesDefined()
					|| building.getBlockades().size() == 0) {
				continue;
			}
			final double affect = world.getEnergyFlow().getOut(building);
			final double distance = world.getDistance(building, me());
			final double value = affect * distance;
			if (value < minValue) {
				minValueBuilding = building;
				minValue = value;
			}
		}
		if (minValueBuilding != null) {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " is move to target building to clear"
								+ " ----- class: PoliceForceAgent, method: moveToFires()");
			}
			clearAround();
			move(minValueBuilding, router.getPfCostFunction());
		}
	}*/

	/*private void clearAround() throws ActionCommandException {
		ArrayList<Blockade> blocks = new ArrayList<Blockade>();
		for (EntityID id : getVisibleEntities()) {
			StandardEntity entity = world.getEntity(id);
			if (entity instanceof Blockade) {
				Blockade block = (Blockade) entity;
				blocks.add(block);
			}
		}
		Collections.sort(blocks, new DistanceSorter(me(), world));
		// Collections.shuffle(blocks, random);
		for (Blockade block : blocks) {
			if (!isClearable(block)) {
				continue;
			}
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " is clearing around"
								+ " ----- class: PoliceForceAgent, method: clearAround()");
			}
			clear(block);
		}
	}*/

	/*private void clearCriticalArea() throws ActionCommandException {
		final int uniform = getUniform();
		if (uniform < world.getCriticalArea().size()) {
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out
						.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " is move to clear critical area"
								+ " ----- class: PoliceForceAgent, method: clearCriticalArea()");
			}
			move(world.getCriticalArea().get(uniform),
					router.getPfCostFunction());
		}
	}*/

	/**
	 * 20140523 help civilian is abandoned
	 * 
	 * @throws csu.agent.Agent.ActionCommandException
	 */
	// next step: add fb and at, now there is a problem in isSureReachable
	public void helpCivilian() throws csu.agent.Agent.ActionCommandException {
		Collection<StandardEntity> civilian = world
				.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		double helpDistance = world.getConfig().repairDistance * 4;
		double dis;
		for (StandardEntity entity : civilian) {

			dis = world.getDistance(me(), entity);
			if (dis < helpDistance && !router.isSureReachable(entity.getID())) {

				Human human = (Human) entity;
				if (AgentConstants.PRINT_TEST_DATA) {
					System.out
							.println("In time: "
									+ time
									+ " Agent: "
									+ me()
									+ " is move to civilian"
									+ " ----- class: PoliceForceAgent, method: helpCivilian()");
					System.out.println(router.isSureReachable(entity.getID()));
				}

				move(human);
				return;
			}
		}
	}

	/**
	 * What this method can do?
	 */
	/*private void helpAgents() throws ActionCommandException {
		Human nearestAgent = null;
		int nearestDist = 600000;

		for (StandardEntity se : world
				.getEntitiesOfType(AgentConstants.PLATOONS)) {
			Human platoon = (Human) se;
			if (platoon instanceof PoliceForce) {
				continue;
			}
			if (platoon.isPositionDefined()) {
				EntityID platoonPosition = platoon.getPosition();

				if (platoon.getID().equals(me().getID())) {
					clearHuman.add(nearestAgent);
				}

				if (!clearHuman.contains(platoon)) {
					int dist = world.getDistance(me().getPosition(),
							platoonPosition);
					if (!router.isSureReachable(platoonPosition)
							&& dist < nearestDist) {
						// router.isSureReachable(pos)&&
						nearestAgent = platoon;
						nearestDist = dist;
					}
				}
			}
		}
		if (nearestAgent != null) {
			if (nearestAgent.equals(oldNearestAgent)) {
				counter++;
				if (counter > 6) {
					clearHuman.add(nearestAgent);
				}
			} else {
				counter = 0;
			}
			oldNearestAgent = nearestAgent;
			if (AgentConstants.PRINT_TEST_DATA) {
				System.out.println("In time: "
								+ time
								+ " Agent: "
								+ me()
								+ " is move to help other Agent"
								+ " ----- class: PoliceForceAgent, method: helpAgent()");
			}
			move(nearestAgent, router.getPfCostFunction());
		}
	}*/

	@Override
	protected void enterSearchBuildings() throws ActionCommandException {
		if (unenteredBuildings.isEmpty()) {
			initUnentered();
		}
		/* Remove all Buildings that someone has visited. */
		unenteredBuildings.removeAll(someoneVisitedArea);

		for (Iterator<EntityID> it = unenteredBuildings.iterator(); it.hasNext();) {
			EntityID id = it.next();
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if (world.getCollapsedBuildings().contains(building)
						|| world.getBurningBuildings().contains(building)) {
					it.remove();
				} else if (building.isTemperatureDefined()
						&& building.getTemperature() > 25) {
					it.remove();
				}
			}
		}
		if (!unenteredBuildings.isEmpty()) {
			Set<StandardEntity> dest = new HashSet<StandardEntity>(
					unenteredBuildings.size());
			for (EntityID id : unenteredBuildings) {
				dest.add(world.getEntity(id));
			}

			if (me() instanceof PoliceForce) {
				move(dest, router.getPfCostFunction());
			}

			if (AgentConstants.PRINT_TEST_DATA) {
				System.out.println("Agent: " + me()
						+ " is enter search building in time: " + time
						+ " ----- class: FireBrigadeAgent, method: think()");
			}

			move(dest);
		}
	}

	@Override
	public String toString() {
		return "CSU_YUNLU police force agent";
	}
}
