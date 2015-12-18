package csu.agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rescuecore2.log.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.pf.PoliceForceAgent;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.route.pov.CostFunction;
import csu.model.route.pov.POVRouter;

/**
 * In this class, we difined three kind of move. All of them send a
 * {@link AKMove} to the kernel.
 * 
 * <pre>
 * 1.moveFront: when the destination is a building, we remove this building from
 * path, and then move along the new path. By to do this, we can avoid moving
 * into a fired building
 * 
 * 2.move: move to the destination
 * 
 * 3.moveReachable: find the reachable destinations from a collection of
 * destinations and then move to the best one
 * </pre>
 * 
 * @author nale
 * 
 * @param <E>
 */
public abstract class HumanoidAgent<E extends Human> extends CommunicationAgent<E> {

	/** 
	 * Router which do the path search task. 
	 */
	protected POVRouter router;
	
	/** 
	 * The path this Agent will follow. A path is a list of Areas.
	 */
	protected List<EntityID> path;
	
	// TODO new move Date: May 31, 2014 Time: 2:42pm ----- appreciation-csu
	protected Area prevTarget_area = null;
	protected EntityID prevTarget_id;
	
	protected boolean followLastPlan = false;
	
	protected List<EntityID> previousPath = new ArrayList<>();
	protected List<EntityID> lastMovePlan = new ArrayList<>();
	
	protected int tryToReachTargetCount = 0;
	// TODO new move
	
	
	
	@Override
	protected void initialize() {
		super.initialize();
		router = world.getRouter();
	}

	/** 
	 * The location I stayed in last circle. 
	 */
	Pair<Integer, Integer> preLocation = null;
	
	/** 
	 * The average distance of each move. 
	 */
	protected double distAverage = 0.0;
	
	/** The number I moved. */
	private int distCount = 0;

	@Override
	protected void prepareForAct() throws TimeOutException {
		world.update(me(), changed); 
		super.prepareForAct();
		EntityID position = me().getPosition();
		this.router.update(position, getVisibleEntities());
		this.positionHistory.put(new Integer(time), position);
		this.locationHistory.put(time, world.getSelfLocation());
		updateMoveAverage();
	}

	/**
	 * Update the average move distance.
	 */
	private void updateMoveAverage() {
		// get the current position of me
		final Pair<Integer, Integer> location = me().getLocation(world);
		// If command send in last circle is AK_MOVE
		if (getCommandHistory(world.getTime() - 1) == StandardMessageURN.AK_MOVE) {
			if (preLocation != null) {
				// the distance of current location and pre-location
				final double dist = Math.hypot(
						location.first() - preLocation.first(), location.second() - preLocation.second());
				distCount++;
				if (distAverage == 0.0) {
					distAverage = dist;
				} else {
					// the arithmetic mean value
					double t = (double) (distCount - 1) / distCount;
					distAverage = distAverage * t + dist / distCount;
				}
			}
		}
		preLocation = location;
	}
	
	/**
	 * Return this area this agent located in or null when this agent is load by
	 * an AT.
	 * 
	 * @return the area this agent located in or null
	 */
	@Override
	public Area location() {
		
		StandardEntity location = super.location();
		
		if (location instanceof Area)
			return (Area) location;
		
		return null;
	}

	/**
	 * Get the average move distance.
	 * 
	 * @return the average move distance
	 */
	public double getDistAverage() {
		return Math.max(10000.0, distAverage);
	}

	public POVRouter getRouter() {
		return router;
	}
	
	// TODO new move Date: May 31, 2014 Time: 2:42pm ----- appreciation-csu
	
	public boolean newMove(Area target, CostFunction costFunc) throws ActionCommandException {
		if (target == null)
			return false;
		
		if (world.getSelfPosition().getID().equals(target.getID()))
			return false;
		
		List<EntityID> areaPath = getPath((Area)world.getSelfPosition(), target, costFunc);
		
		if (!target.getID().equals(this.prevTarget_id)) {
			this.prevTarget_id = target.getID();
			this.tryToReachTargetCount = 0;
		}
		
		moveOnPlan(areaPath);
		
		return false;
	}
	
	public boolean newMove(Collection<? extends StandardEntity> targets,
			CostFunction costFunc, boolean force) throws ActionCommandException {
		
		moveOnPlan(getPath(targets, costFunc, me() instanceof PoliceForce));
		return false;
	}
	
	public List<EntityID> getPath(Area sourArea, Area destArea, CostFunction costFunc) {
		
		List<EntityID> finalPath = new ArrayList<>();
		
		if (sourArea == null || destArea == null)
			return finalPath;
		
		if (destArea.getID().equals(this.prevTarget_id)) {
			this.prevTarget_id = destArea.getID();
			this.tryToReachTargetCount = 0;
		}
		
		if (sourArea.equals(destArea)) {
			Logger.warn("Already in target! ----- Time: " + world.getTime());
			return finalPath;
		}
		
		boolean shouldReplan;
		if (prevTarget_area != null && prevTarget_area.equals(destArea) && followLastPlan) {
			this.followLastPlan = false;
			shouldReplan = false;
		} else {
			this.followLastPlan = true;
			shouldReplan = true;
		}
		
		if (shouldReplan) {
			this.previousPath.clear();
			
			finalPath = router.getAStar(sourArea, destArea, costFunc, new Point(me().getX(), me().getY()));
			this.prevTarget_area = destArea;
			this.previousPath = finalPath;
		} else {
			List<EntityID> temp = new ArrayList<>();
			
			for (EntityID next : previousPath) {
				if (!sourArea.getID().equals(next)) {
					temp.add(next);
				} else {
					break;
				}
			}
			
			this.previousPath.removeAll(temp);
			finalPath = this.previousPath;
		}
		
		if (!finalPath.isEmpty() && !(this instanceof PoliceForceAgent)) {
			// if the target is blocked by blockades, just return a empty path
		}
		
		return finalPath;
	}
	
	public List<EntityID> getPath(Collection<? extends StandardEntity> targets, CostFunction costFunc, boolean force) {
		List<EntityID> areaPath = new ArrayList<> ();
		Area positionArea = (Area) world.getSelfPosition();
		
		if (targets.isEmpty()) 
			return areaPath;
		
		boolean shouldReplan;
		if (this.prevTarget_area != null && targets.contains(this.prevTarget_area) && followLastPlan) {
			this.followLastPlan = false;
			shouldReplan = false;
		} else {
			this.followLastPlan = true;
			shouldReplan = true;
		}
		
		if (shouldReplan) {
			this.previousPath = router.getMultiAStar(positionArea, targets,
					force ? router.getPfCostFunction() : router.getNormalCostFunction(), 
							new Point(me().getX(), me().getY()));
		} else {
			List<EntityID> temp = new ArrayList<>();

			for (EntityID next : previousPath) {
				if (!positionArea.getID().equals(next)) {
					temp.add(next);
				} else {
					break;
				}
			}

			this.previousPath.removeAll(temp);
		}
		
		areaPath = this.previousPath;
		return areaPath;
	}
	
	public void moveOnPlan(List<EntityID> path) throws ActionCommandException {
		if (path == null || path.isEmpty()) ///the empth() is very important
			return;

		this.lastMovePlan.clear();
		this.lastMovePlan.addAll(path);
		
		if (AgentConstants.FB) {
			String str = null;
			for (EntityID next : path) {
				if (str == null)
					str = next.getValue() + "";
				else
					str = str + ", " + next.getValue();
			}
			System.out.println(time + ", " + me() + ", moving path = [" + str + "]"); 
            System.out.println("------HumanoidAgent: moveOnPlan");
		}

		sendMove(world.getTime(), path);
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}
	
	public void moveToPoint(EntityID area, int destX, int destY) throws ActionCommandException{
		if (location().getID().equals(area)) {
			List<EntityID> list = new ArrayList<>();
			list.add(area);
			sendMove(world.getTime(), list, destX, destY);
			throw new ActionCommandException(StandardMessageURN.AK_MOVE);
		} else {
			move(world.getEntity(area, Area.class), router.getNormalCostFunction());
			throw new ActionCommandException(StandardMessageURN.AK_MOVE);
		}
	}
	
	/**
	 * Random walk.
	 * 
	 * @throws ActionCommandException
	 */
	protected void randomWalk() throws ActionCommandException {
		Collection<StandardEntity> entities = world.getEntitiesOfType(AgentConstants.AREAS);
		ArrayList<StandardEntity> areas = new ArrayList<StandardEntity>(entities);
		areas.removeAll(world.getIsolated());
		Area area = (Area) areas.get(random.nextInt(areas.size()));

		if (AgentConstants.PRINT_TEST_DATA) {
			System.out.println("Agent: " + me() + " is random walking in time: " + time + 
					" ----- class: PlatoonAgent, method: randomWalk()");
		}
		
		move(area);
	}
	
	// TODO new move
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/* --------------------------------------------- Move Front ----------------------------------------------- */

	/**
	 * Move to a destination with the specified CostFunction.
	 * 
	 * @param destination
	 *            the place I want to go to
	 * @param costFunc
	 *            the CostFunction
	 * @throws ActionCommandException
	 */
	public void moveFront(StandardEntity destination, CostFunction costFunc) throws ActionCommandException {
		if (destination instanceof Human) {
			Human human = (Human) destination;
			StandardEntity position = human.getPosition(world);
			if (position instanceof Area) {
				moveFront(router.getAStar(me(), (Area) position, costFunc),
						destination.getLocation(world));
			} else {
				if (position == null) {
					System.err.println("Move error\ttime=" + time + " id="
							+ me().getID() + " my position is null");
				} else {
					// this human was load by an AT
					System.err.println("Move error\ttime=" + time + " id="
							+ me().getID() + " move from " + position);
				}
				return;
			}
		} else if (destination instanceof Blockade) {
			Blockade block = (Blockade) destination;
			EntityID position = block.getPosition();
			moveFront(router.getAStar(me(), (Area) world.getEntity(position), costFunc), 
					destination.getLocation(world));
		}
		
		moveFront(router.getAStar(me(), (Area) destination, costFunc), destination.getLocation(world));
	}

	/**
	 * Move along the given path to the target location.
	 * 
	 * @param path
	 *            the given path to move along
	 * @param location
	 *            target location
	 * @throws ActionCommandException
	 */
	private void moveFront(List<EntityID> path, Pair<Integer, Integer> location) 
			throws ActionCommandException {
		if (location == null) {
			moveFront(path, -1, -1);
		} else {
			moveFront(path, location.first(), location.second());
		}
	}
	
	/**
	 * Move along a path.
	 * 
	 * @param path
	 *            the given path to move along
	 * @throws ActionCommandException
	 */
	public void moveFront(List<EntityID> path) throws ActionCommandException {
		moveFront(path, -1, -1);
	}
	
	/**
	 * Move along the given path to the target location specified by (x, y).
	 * 
	 * @param path
	 *            the given path to move along
	 * @param x
	 *            the x coordinate of the target location
	 * @param y
	 *            the y coordinate of the target location
	 * @throws ActionCommandException
	 */
	public void moveFront(List<EntityID> path, int x, int y) throws ActionCommandException {
		int last = path.size() - 1;
		if (world.getEntity(path.get(last)) instanceof Building) {
			path.remove(last);
			last = path.size() - 1;
		}

		send(new AKMove(getID(), time, path, x, y));
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}
	
	/**
	 * Find the best target location from a collection of destinations and move
	 * to this best target location with the NormalCostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @throws ActionCommandException
	 */
	public void moveFront(Collection<? extends StandardEntity> destinations) throws ActionCommandException {
		moveFront(destinations, router.getStrictCostFunction());
	}

	/**
	 * Find the best target location from a collection of destinations and move
	 * to this best target location with the given CostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @param costFunc
	 *            the given CostFunction
	 * @throws ActionCommandException
	 */
	public void moveFront(Collection<? extends StandardEntity> destinations,
			CostFunction costFunc) throws ActionCommandException {
		moveFront(router.getMultiDest(location(), destinations, costFunc, new Point(me().getX(), me().getY())));
	}
	
	
	
/* ---------------------------------------------- Move -------------------------------------------------- */	

	
	/**
	 * Move to the destination with the NormalCostFunction.
	 * 
	 * @param destination
	 *            the target location
	 * @throws ActionCommandException
	 */
	public void move(EntityID destination) throws ActionCommandException {
		move(world.getEntity(destination));
	}
	
	/**
	 * Move to the destination with the NormalCostFunction.
	 * 
	 * @param destination
	 *            the target location
	 * @throws ActionCommandException
	 */
	public void move(StandardEntity destination) throws ActionCommandException {
		move(destination, router.getNormalCostFunction());
	}

	/**
	 * Move to the destination with given CostFunction.
	 * 
	 * @param destination
	 *            the target location
	 * @param costFunc
	 *            the given CostFunction
	 * @throws ActionCommandException
	 */
	public void move(EntityID destination, CostFunction costFunc) throws ActionCommandException {
		move(world.getEntity(destination), costFunc);
	}

	/**
	 * Move to destination with the given CoatFunction
	 * 
	 * @param destination
	 *            the destination to move
	 * @param costFunc
	 *            the CostFuntion this move will use
	 * @throws ActionCommandException
	 */
	public void move(StandardEntity destination, CostFunction costFunc) throws ActionCommandException {
		if (destination instanceof Human) {
			Human human = (Human) destination;
			StandardEntity position = human.getPosition(world);
			if (position instanceof Area) {
				move(router.getAStar(me(), (Area) position, costFunc), destination.getLocation(world));
			} else {
				if (position == null) {
					System.err.println("Move error\ttime=" + time + " id="
							+ me().getID() + " my position is null");
				} else {
					System.err.println("Move error\ttime=" + time + " id="
							+ me().getID() + " move from " + position);
				}
				return;
			}
		} else if (destination instanceof Blockade) {
			Blockade block = (Blockade) destination;
			EntityID position = block.getPosition();
			move(router.getAStar(me(), (Area) world.getEntity(position), 
					costFunc), destination.getLocation(world));
		}
		move(router.getAStar(me(), (Area) destination, costFunc),
				destination.getLocation(world));
	}
	
	/**
	 * Move along the given path to the target location.
	 * 
	 * @param path
	 *            the given path to move along
	 * @param location
	 *            the target location
	 * @throws ActionCommandException
	 */
	public void move(List<EntityID> path, Pair<Integer, Integer> location) throws ActionCommandException {
		if (location == null) {
			move(path, -1, -1);
		} else {
			move(path, location.first(), location.second());
		}
	}

	/**
	 * The move method. When you only know the desitination, and the coordinate
	 * you need to go, you can invoke this method. It use the
	 * <code>NormalCostFunction</code>
	 * 
	 * @param destination
	 *            the desitination
	 * @param x
	 *            the X coordinate
	 * @param y
	 *            the Y coordinate
	 * @throws ActionCommandException
	 */
	public void move(Area destination, int x, int y) throws ActionCommandException {
		move(router.getAStar(me(), destination, router.getNormalCostFunction()), x, y);
	}

	/**
	 * Move along the given path.
	 * 
	 * @param path
	 *            the given path to move along
	 * @throws ActionCommandException
	 */
	public void move(List<EntityID> path) throws ActionCommandException {
		StandardEntity entity = world.getEntity(path.get(path.size() - 1));
		Pair<Integer, Integer> e_location = entity.getLocation(world);
		if (e_location == null) {
			move(path, -1, -1);
		} else {
			move(path, e_location.first().intValue(), e_location.second().intValue());
		}
	}

	/**
	 * Move along the given path to the target location specified by (x, y)
	 * 
	 * @param path
	 *            the given path to move along
	 * @param x
	 *            the x coordinate of the target location
	 * @param y
	 *            the y coordinate of the target location
	 * @throws ActionCommandException
	 */
	public void move(List<EntityID> path, int x, int y) throws ActionCommandException {
		send(new AKMove(getID(), time, path, x, y));
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}

	/**
	 * Find the best target location from a collection of destinations and move
	 * to this best target location with the NormalCostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @throws ActionCommandException
	 */
	public void move(Collection<? extends StandardEntity> destinations) throws ActionCommandException {
		move(destinations, router.getNormalCostFunction());
	}
	
	/**
	 * Find the best target location from a collection of destinations and move
	 * to this best target location with the given CostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @param costFunc
	 *            the given CostFunction
	 * @throws ActionCommandException
	 */
	public void move(Collection<? extends StandardEntity> destinations,
			CostFunction costFunc) throws ActionCommandException {
		move(router.getMultiDest(location(), destinations, costFunc, new Point(me().getX(), me().getY())));
	}


/* ----------------------------------------- Move Reachable ---------------------------------------------- */	
	
	/**
	 * First find the reachable destinations for a collection of destinations.
	 * Then get the best target location from reachable destinations and move to
	 * this best target location with the NormalCostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @param front
	 *            a flag to control whether to move into a building when the
	 *            best destination is a building. If true, just stop in front of
	 *            the target building. Otherwise, move into this building
	 * @throws ActionCommandException
	 */
	public void moveReachable(Collection<? extends StandardEntity> destinations, boolean front)
			throws ActionCommandException {
		moveReachable(destinations, router.getNormalCostFunction(), front);
	}

	/**
	 * First find the reachable destinations for a collection of destinations.
	 * Then get the best target location from reachable destinations and move to
	 * this best target location with the NormalCostFunction.
	 * 
	 * @param destinations
	 *            a collection of destinations
	 * @param costFunc
	 *            the given CostFunction
	 * @param front
	 *            a flag to control whether to move into a building when the
	 *            best destination is a building. If true, just stop in front of
	 *            the target building. Otherwise, move into this building
	 * @throws ActionCommandException
	 */
	public void moveReachable(Collection<? extends StandardEntity> destinations,
			CostFunction costFunc, boolean front) throws ActionCommandException {
		for (Iterator<? extends StandardEntity> it = destinations.iterator(); it.hasNext();) {
			if (!router.isSureReachable((Area) it.next())) {
				it.remove();
			}
		}
		if (destinations.isEmpty())
			return;
		moveReachable(router.getMultiDest(location(), 
				destinations, costFunc, new Point(me().getX(), me().getY())), front);
	}

	/**
	 * Move along the given path to a reachable destination.
	 * 
	 * @param path
	 *            the path to move along
	 * @param front
	 *            a flag to control whether to move into a building when the
	 *            best destination is a building. If true, just stop in front of
	 *            the target building. Otherwise, move into this building
	 * @throws ActionCommandException
	 */
	public void moveReachable(List<EntityID> path, boolean front) throws ActionCommandException {
		moveReachable(path, -1, -1, front);
	}

	/**
	 * Move along the given path to a reachable destination.
	 * 
	 * @param path
	 *            the path to move along
	 * @param x
	 *            the x coordinate of the destination
	 * @param y
	 *            the y coordinate of the destination
	 * @param front
	 *            a flag to control whether to move into a building when the
	 *            best destination is a building. If true, just stop in front of
	 *            the target building. Otherwise, move into this building
	 * @throws ActionCommandException
	 */
	public void moveReachable(List<EntityID> path, int x, int y, boolean front)
			throws ActionCommandException {
		if (front && world.getEntity(path.get(path.size() - 1)) instanceof Building) {
			path.remove(path.size() - 1);
		}
		send(new AKMove(getID(), time, path, x, y));
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}
}
