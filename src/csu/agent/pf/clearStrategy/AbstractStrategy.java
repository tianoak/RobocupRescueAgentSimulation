package csu.agent.pf.clearStrategy;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.agent.pf.PoliceForceAgent;
import csu.model.AdvancedWorldModel;
import csu.standard.Ruler;
import csu.util.Util;

public abstract class AbstractStrategy implements I_ClearStrategy {

	protected AdvancedWorldModel world;
	protected PoliceForceAgent underlyingAgent;

	protected List<EntityID> lastCyclePath;
	
	protected int x, y, time;
	
	protected double repairDistance;
	
	protected int lastClearDest_x = -1, lastClearDest_y = -1;
	protected int count = 0, lock = 4, reverseLock = 4;
	
	protected AbstractStrategy(AdvancedWorldModel world) {
		this.world = world;
		this.underlyingAgent = (PoliceForceAgent) world.getAgent();

		this.time = world.getTime();
		this.x = world.getSelfLocation().first();
		this.y = world.getSelfLocation().second();
		
		this.repairDistance = world.getConfig().repairDistance;
	}
	
	@Override
	public void updateClearPath(List<EntityID> path) {
		this.lastCyclePath = path;
		
		this.time = world.getTime();
		this.x = world.getSelfLocation().first();
		this.y = world.getSelfLocation().second();
	}
	
	/* (non-Javadoc)
	 * @see csu.agent.pf.clearStrategy.I_ClearStrategy#blockedClear()
	 * 返回最近的blockade
	 */
	@Override
	public Blockade blockedClear() {
		Set<Blockade> blockades = new HashSet<>();
		StandardEntity entity = null;
		for (EntityID next : underlyingAgent.getChanged()) {
			entity = world.getEntity(next);
			
			if (entity instanceof Blockade) {
				Blockade bloc = (Blockade) entity;
				//??
				if (bloc.isApexesDefined() && bloc.getApexes().length < 6)
					continue;
				blockades.add(bloc);
			}
		}
		
		Blockade nearestBlockade = null;
		double minDistance = repairDistance;
		for (Blockade next : blockades) {
			double distance = findDistanceTo(next, x, y);
			if (distance < minDistance) {
				nearestBlockade = next;
				minDistance = distance;
			}
		}
		
		return nearestBlockade;
	}

	protected void clearf(boolean clearNeighbour) throws csu.agent.Agent.ActionCommandException {
		Area pfarea = (Area) world.getSelfPosition();
		List<EntityID> blockades = new ArrayList<EntityID>();
		if (pfarea.getBlockades() != null) {
			blockades.addAll(pfarea.getBlockades());
		}

		List<EntityID> neighbours = pfarea.getNeighbours();
		if (clearNeighbour) {
			for (EntityID next : neighbours) {
				StandardEntity entity = world.getEntity(next);
				if (entity instanceof Road) {
					Road ro = (Road) entity;
					if (ro.isBlockadesDefined())
						blockades.addAll(ro.getBlockades());
				}
			}
		}
		
		int minDistance = (int) (world.getConfig().repairDistance);
		Blockade result = null;
		if (blockades != null) {
			for (EntityID entityID : blockades) {
				Blockade b = (Blockade) world.getEntity(entityID);
				double d = findDistanceTo(b, x, y);
				// double d = Ruler.getDistance(b.getX(), b.getY(), x, y);
				if (d < minDistance - 10) {
					minDistance = (int) d;
					result = b;
				}
			}
		}

		minDistance = (int) (world.getConfig().repairDistance);
		for (EntityID entityID : neighbours) {
			Area neighbourTemp = (Area) world.getEntity(entityID);

			if (neighbourTemp != null && world.getCriticalArea().getAreas().contains(neighbourTemp)) {
				if (neighbourTemp.getBlockades() != null) {
					for (EntityID blockade : neighbourTemp.getBlockades()) {
						Blockade b = (Blockade) world.getEntity(blockade);
						// double d = findDistanceTo(b, x, y);
						double d = Ruler.getDistance(b.getX(), b.getY(), x, y);
						if (d < minDistance - 10) {
							minDistance = (int) d;
							result = b;
						}
					}
				}
			} else if (neighbourTemp instanceof Road) {
				Road neighbour = (Road) neighbourTemp;
				if (world.getEntrance().isEntrance(neighbour)) {
					if (neighbour.getBlockades() != null) {
						for (EntityID blockade : neighbour.getBlockades()) {
							Blockade b = (Blockade) world.getEntity(blockade);
							// double d = findDistanceTo(b, x, y);
							double d = Ruler.getDistance(b.getX(), b.getY(), x, y);
							if (d < minDistance - 10) {
								minDistance = (int) d;
								result = b;
							}
						}
					}
				}
			}
		}

		if (result != null) {
			// doClear(result);
			underlyingAgent.sendClear(world.getTime(), result.getID());
			throw new ActionCommandException(StandardMessageURN.AK_CLEAR);
		}	
	}

	protected void doClear(Blockade result) throws ActionCommandException {

		List<rescuecore2.misc.geometry.Line2D> lines = GeometryTools2D.pointsToLines(
						GeometryTools2D.vertexArrayToPoints(result.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D bestPoint = null;
		Point2D origin = new Point2D(x, y);

		double d = 0;
		for (rescuecore2.misc.geometry.Line2D next : lines) {
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
			d = GeometryTools2D.getDistance(origin, closest);
			if (d < best) {
				best = d;
				bestPoint = closest;
			}
		}
		Vector2D v = bestPoint.minus(new Point2D(x, y));
		v = v.normalised().scale(1000000);// v.dx * 1/length * 1000000

		underlyingAgent.sendClear(world.getTime(), (int) (x + v.getX()), (int) (y + v.getY()));
		throw new ActionCommandException(StandardMessageURN.AK_CLEAR);
	}

	protected double findDistanceTo(Blockade b, int x, int y) {
		/*List<rescuecore2.misc.geometry.Line2D> lines = GeometryTools2D.pointsToLines(
						GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D origin = new Point2D(x, y);
		
		for (rescuecore2.misc.geometry.Line2D next : lines) {
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			if (d < best) {
				best = d;
			}
		}
		return (int) best;*/
		
		Polygon bloc_pol = Util.getPolygon(b.getApexes());
		Point selfL = new Point(x, y);
		
		return Ruler.getDistance(bloc_pol, selfL);
	}
}
