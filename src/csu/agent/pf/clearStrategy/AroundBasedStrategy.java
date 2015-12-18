package csu.agent.pf.clearStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;

public class AroundBasedStrategy extends AbstractStrategy{
	public AroundBasedStrategy(AdvancedWorldModel world) {
		super(world);
	}

	@Override
	public void clear() throws ActionCommandException {
		Area location = (Area)world.getSelfPosition();
		
		if (location instanceof Building) {
			clearInBuilding((Building)location);
			return;
		}
		
		CSURoad road = world.getCsuRoad(location.getID());
		if (road.isEntrance()) {
			clearEntrance(road);
			return;
		}
		
		if (road.isAllEdgePassable()) {
			clearInCriticalArea(road);
			return;
		}
		
		clearInHighWay(road);
		
		clearf(false);
	}

	@Override
	public void doClear(Road road, CSUEdge dir, Blockade targetB) throws ActionCommandException {
		// do nothing
	}
	
	private void clearInBuilding(Building location) {
		Set<Road> relatedEntrance = world.getEntrance().getEntrance(location);
		List<EntityID> blockades = new ArrayList<>();
		
		for (Road next : relatedEntrance) {
			if (!underlyingAgent.isVisible(next))
				continue;
			if (!next.isBlockadesDefined())
				continue;
			CSURoad road = world.getCsuRoad(next.getID());
			if (isTooLargetEntrance(road) && road.isPassable())
				continue;
			
			blockades.addAll(next.getBlockades());
		}
		
		EntityID target = findTargetBlockade(blockades);
		if (target != null) {
			underlyingAgent.sendClear(time, target);
		}
	}
	
	private void clearEntrance(CSURoad road) {
		List<EntityID> blockades = new ArrayList<>();
		
		if (isTooLargetEntrance(road) && road.isPassable()) {
			// need not to clear anymore
		} else {
			blockades.addAll(road.getSelfRoad().getBlockades());
		}
		
		for (EntityID next : road.getSelfRoad().getNeighbours()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof Road) {
				CSURoad neighbour = world.getCsuRoad(next);
				if (neighbour.isAllEdgePassable() && neighbour.getSelfRoad().isBlockadesDefined()) {
					blockades.addAll(neighbour.getSelfRoad().getBlockades());
				}
			}
		}
		
		EntityID target = findTargetBlockade(blockades);
		if (target != null) {
			underlyingAgent.sendClear(time, target);
		}
	}
	
	private void clearInCriticalArea(CSURoad road) {
		List<EntityID> blockades = new ArrayList<>();
		
		blockades.addAll(road.getSelfRoad().getBlockades());
		
		for (EntityID next : road.getSelfRoad().getNeighbours()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof Road) {
				CSURoad csuRoad = world.getCsuRoad(next);
				if (csuRoad.isEntrance()) {
					if (isTooLargetEntrance(csuRoad) && csuRoad.isPassable()) {
						// do nothing
					} else {
						blockades.addAll(csuRoad.getSelfRoad().getBlockades());
					}
				} else {
					if (!csuRoad.isPassable()) {
						blockades.addAll(csuRoad.getSelfRoad().getBlockades());
					}
				}
			}
		}
		
		EntityID target = findTargetBlockade(blockades);
		if (target != null) {
			underlyingAgent.sendClear(time, target);
		}
	}
	
	private void clearInHighWay(CSURoad road) {
		List<EntityID> criBlockades = new ArrayList<>();
		List<EntityID> highWayBlockades = new ArrayList<>();
		
		for (EntityID next : underlyingAgent.getChanged()) {
			StandardEntity entity  = world.getEntity(next);
			if (!(entity instanceof Road))
				continue;
			
			CSURoad csuRoad = world.getCsuRoad(next);
			if (csuRoad.isEntrance() || road.isAllEdgePassable()) {
				if (!csuRoad.getSelfRoad().isBlockadesDefined())
					continue;
				if (csuRoad.isEntrance() && isTooLargetEntrance(csuRoad) && csuRoad.isPassable()) {
					// do nothing
				} else {
					criBlockades.addAll(csuRoad.getSelfRoad().getBlockades());
				}
			}
			
			if (!csuRoad.getSelfRoad().isBlockadesDefined())
				continue;
			highWayBlockades.addAll(csuRoad.getSelfRoad().getBlockades());
		}
		
		EntityID target = findTargetBlockade(criBlockades);
		if (target != null) {
			underlyingAgent.sendClear(time, target);
		}
		
		target = findTargetBlockade(highWayBlockades);
		if (target != null) {
			underlyingAgent.sendClear(time, target);
		}
	}
	
	private EntityID findTargetBlockade(List<EntityID> blockades) {
		double minDistance = repairDistance;
		Blockade target = null;
		for (EntityID next : blockades) {
			Blockade blockade = (Blockade) world.getEntity(next);
			// double distance = Ruler.getDistance(blockade.getX(), blockade.getY(), x, y);
			double distance = findDistanceTo(blockade, x, y);
			if (distance < minDistance) {
				minDistance = distance;
				target = blockade;
			}
		}
		
		if (target != null)
			return target.getID();
		return null;
	}
	 
	private boolean isTooLargetEntrance(CSURoad road) {
		boolean tooLargetEntrance = false;
		double minLength = Double.MAX_VALUE;
		for (CSUEdge edge : road.getCsuEdges()) {
			if (!edge.isPassable())
				continue;
			double length = Ruler.getDistance(edge.getStart(), edge.getEnd());
			if (minLength > length) {
				minLength = length;
			}
		}
		if (minLength > 5000)
			tooLargetEntrance = true;
		
		return tooLargetEntrance;
	}
}
