package csu.util;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import csu.model.AdvancedWorldModel;

public class RoadState {

	/** Determines whether a Road is smooth or is blocked.*/
	public static boolean isSmoothRoad(Road road, AdvancedWorldModel world){
		if (!road.isBlockadesDefined())
			return true;
		if (road.getBlockades().size() == 0)
			return true;
		boolean isBlocked = false;
		
		for (EntityID id : road.getBlockades()) {
			StandardEntity entity = world.getEntity(id);
			if (entity != null && RoadState.isBlockingRoad((Blockade)entity, world)){
				isBlocked = true;
				break;
			}
		}
		return (!isBlocked);
	}
	
	/** Determines whether a blockade is blocking a Road.*/
	public static boolean isBlockingRoad(Blockade blockade, AdvancedWorldModel world) {
		Road road = (Road) world.getEntity(blockade.getPosition());
		int[] blockadeVertices = blockade.getApexes();
		for (Edge roadEdge : road.getEdges()) {
			if (roadEdge.isPassable()) {
				for (int i = 0; i < blockadeVertices.length; i += 2) {
					if (!openRoadEdge(blockade.getID(), world, road, roadEdge,
							blockadeVertices)) {
						return true;
					}
				}
			}
		}
		List<EntityID> otherBlockades = road.getBlockades();
		if (otherBlockades != null && otherBlockades.size() > 1) {
			for (EntityID otherBlockadeID : otherBlockades) {
				if (otherBlockadeID.getValue() != blockade.getID().getValue()) {
					StandardEntity entity = world.getEntity(otherBlockadeID);
					if (entity == null)
						continue;
					int[] otherBlockadeVertices = ((Blockade) entity)
							.getApexes();
					for (int j = 0; j < otherBlockadeVertices.length; j += 2) {
						for (int i = 0; i < blockadeVertices.length; i += 2) {
							Line2D blockadeLine = new Line2D(new Point2D(
									blockadeVertices[i],
									blockadeVertices[i + 1]), new Point2D(
									blockadeVertices[(i + 2)
											% blockadeVertices.length],
									blockadeVertices[(i + 3)
											% blockadeVertices.length]));
							Point2D closestPt = GeometryTools2D
									.getClosestPointOnSegment(
											blockadeLine,
											new Point2D(
													otherBlockadeVertices[j],
													otherBlockadeVertices[j + 1]));
							double distance = GeometryTools2D.getDistance(
									closestPt, new Point2D(
											otherBlockadeVertices[j],
											otherBlockadeVertices[j + 1]));
							if (distance <= 2000) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean openRoadEdge(EntityID blockadeID, StandardWorldModel model, Road road, Edge roadEdge,
			int[] blockadeVertices) {
		double minDistanceStart = Double.POSITIVE_INFINITY;
		double minDistanceEnd = Double.POSITIVE_INFINITY;
		Line2D roadEdgeLine = roadEdge.getLine();
		ArrayList<Point2D> projectedPoints = new ArrayList<Point2D>();

		for (int i = 0; i < blockadeVertices.length; i += 2) {
			Point2D point = GeometryTools2D.getClosestPointOnSegment(
					roadEdgeLine, new Point2D(blockadeVertices[i],
							blockadeVertices[i + 1]));
			if (!projectedPoints.contains(point)) {
				projectedPoints.add(point);
			}
		}
		if (projectedPoints.size() < 2)
			return true;
		for (Point2D projectedPoint : projectedPoints) {
			double dStart = GeometryTools2D.getDistance(projectedPoint,
					roadEdge.getStart());
			if (dStart < minDistanceStart) {
				minDistanceStart = dStart;
			}
			double dEnd = GeometryTools2D.getDistance(projectedPoint,
					roadEdge.getEnd());
			if (dEnd < minDistanceEnd) {
				minDistanceEnd = dEnd;
			}
		}
		if (minDistanceEnd > 2000 || minDistanceStart > 2000) {
			Edge neighbourEdge = getNeighbourEdge(model, road, roadEdge);
			Line2D neighbourEdgeLine = neighbourEdge.getLine();
			projectedPoints = new ArrayList<Point2D>();
			for (int i = 0; i < blockadeVertices.length; i += 2) {
				Point2D point = GeometryTools2D.getClosestPointOnSegment(
						neighbourEdgeLine, new Point2D(blockadeVertices[i],
								blockadeVertices[i + 1]));
				if (!projectedPoints.contains(point)) {
					projectedPoints.add(point);
				}
			}
			if (projectedPoints.size() < 2)
				return true;

			minDistanceStart = Double.POSITIVE_INFINITY;
			minDistanceEnd = Double.POSITIVE_INFINITY;
			for (Point2D projectedPoint : projectedPoints) {
				double dStart = GeometryTools2D.getDistance(projectedPoint,
						neighbourEdge.getStart());
				if (dStart < minDistanceStart) {
					minDistanceStart = dStart;
				}

				double dEnd = GeometryTools2D.getDistance(projectedPoint,
						neighbourEdge.getEnd());
				if (dEnd < minDistanceEnd) {
					minDistanceEnd = dEnd;
				}
			}
			if (minDistanceEnd > 2000 || minDistanceStart > 2000) {
				return true;
			}
		}
		return false;
	}
	
	public static Edge getNeighbourEdge(StandardWorldModel model, Road road, Edge roadEdge) {
		Area neighbour = (Area) model.getEntity(roadEdge.getNeighbour());
		for (Edge neighbourEdge : neighbour.getEdges()) {
			if (neighbourEdge.isPassable() && neighbourEdge.getNeighbour().getValue() == road.getID().getValue())
				return neighbourEdge;
		}
		return null;
	}
}
