package csu.agent.pf.clearStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;
import csu.util.Util;

public class CenterAreaBasedStrategy extends AbstractStrategy {

	public CenterAreaBasedStrategy(AdvancedWorldModel world) {
		super(world);
	}

	public void clear() throws ActionCommandException {
		Area location = (Area)world.getSelfPosition();
		if (location instanceof Building)
			return;

		if (lastCyclePath == null)
			return;

		int index = 0;
		boolean found = false;
		for (EntityID next : lastCyclePath) {
			if (location.getID().getValue() == next.getValue()) {
				found = true;
				break;
			}

			index++;
		}

		if (!found)
			return;

		if (index == lastCyclePath.size() - 1)
			return;

		EntityID nextArea = lastCyclePath.get(index + 1);
		CSURoad road = world.getCsuRoad(location.getID());
		CSUEdge targetEdge = null;
		for (CSUEdge next : road.getCsuEdges()) {
			if (!next.getUnderlyingEdge().isPassable())
				continue;
			if (next.getUnderlyingEdge().getNeighbour().getValue() == nextArea
					.getValue())
				targetEdge = next;
		}
		if (targetEdge == null) {
			for (CSUEdge next : road.getCsuEdges()) {
				if (next.getUnderlyingEdge().isPassable()) {
					targetEdge = next;
					break;
				}
			}
		}

		doClear(road.getSelfRoad(), targetEdge, null);
	}

	/**
	 * Clear the blockade of the given road. When this road is a entrance or
	 * connection road(all edges are passable) road, then clear all blockade of
	 * this road. Otherwise, clear the center part of this road.
	 * <p>
	 * Add by appreciation-csu Date: June 18, 2014 Time: 4:06pm
	 * 
	 * @param road
	 *            the road this agent will clear
	 * @param directionEdge
	 *            the direction this agent want to go
	 * @throws ActionCommandException
	 */
	@Override
	public void doClear(Road roada, CSUEdge directionEdge, Blockade targetB) throws ActionCommandException {
		
		CSURoad road = world.getCsuRoad(roada.getID());
		
		Set<Point2D> intersectionPoints = new FastSet<>();
		Pair<rescuecore2.misc.geometry.Line2D, rescuecore2.misc.geometry.Line2D> pfClearLine;
		pfClearLine = road.getPfClearLine(road);
		Point2D roadCenter = new Point2D(road.getSelfRoad().getX(), road
				.getSelfRoad().getY());
		Point2D dir = directionEdge.getMiddlePoint();

		if (pfClearLine == null) {
			// clear all blockades of this road
			clearf(false);
			return;
		}

		for (CSUBlockade next : road.getCsuBlockades()) {
			intersectionPoints.addAll(Util.getIntersections(next.getPolygon(),
					pfClearLine.first()));
			intersectionPoints.addAll(Util.getIntersections(next.getPolygon(),
					pfClearLine.second()));
		}

		Point2D selfPoint = new Point2D(x, y);
		Vector2D vector_1 = dir.minus(selfPoint), vector_2;
		Point2D vertex, minDistanceP = null;
		double minDistance = Double.MAX_VALUE, distance;

		for (Iterator<Point2D> itor = intersectionPoints.iterator(); itor
				.hasNext();) {
			vertex = itor.next();
			vector_2 = vertex.minus(selfPoint);

			if (vector_1.dot(vector_2) < 0) {
				itor.remove();
				continue;
			}
			distance = Ruler.getDistance(vertex, selfPoint);
			if (distance < minDistance) {
				minDistance = distance;
				minDistanceP = vertex;
			}
		}

		if (minDistanceP == null)
			return;

		Vector2D clearDirec, blockadeDirec, moveDirec;
		blockadeDirec = minDistanceP.minus(selfPoint);
		moveDirec = dir.minus(roadCenter);

		if (minDistance < world.getConfig().repairDistance) {
			clearDirec = moveDirec.add(blockadeDirec);
			clearDirec.normalised().scale(1000000);

			underlyingAgent.sendClear(time, (int) (x + clearDirec.getX()),
					(int) (y + clearDirec.getY()));
		} else {
			Point2D p = GeometryTools2D.getClosestPoint(
					road.getRoadCenterLine(), selfPoint);
			rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(
					p, dir);
			double len = moveDirec.normalised().dot(blockadeDirec);
			double rate = len / getLength(line);

			p = line.getPoint(rate);
			List<EntityID> pa = new ArrayList<>();
			pa.add(road.getId());
			underlyingAgent.sendMove(time, pa, (int) p.getX(), (int) p.getY());
		}
	}
	
	private int getLength(rescuecore2.misc.geometry.Line2D line) {
		return (int)Ruler.getDistance(line.getOrigin(), line.getEndPoint());
	}
}
