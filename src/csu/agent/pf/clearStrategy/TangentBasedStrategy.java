package csu.agent.pf.clearStrategy;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import math.geom2d.line.Line2D;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.geom.CompositeConvexHull;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;

public class TangentBasedStrategy extends AbstractStrategy{

	public TangentBasedStrategy(AdvancedWorldModel world) {
		super(world);
	}

	@Override
	public void clear() throws ActionCommandException {
		Area location = (Area)world.getSelfPosition();
		if (location instanceof Building)
			return;
		CSURoad road = world.getCsuRoad(location.getID());
		doClear(road.getSelfRoad(), null, null);
	}
	
	@Override
	public void doClear(Road roada, CSUEdge directionEdge, Blockade block) throws ActionCommandException {
		CSURoad road = world.getCsuRoad(roada.getID());
		java.awt.geom.Area pfClearArea = road.getPfClearArea(road);
		if (pfClearArea == null) {
			// clear all blockade of this road
			clearf(false);
			return;
		}
		
		Pair<Integer, Integer> selfLocation = world.getSelfLocation();
		double limit = world.getConfig().repairDistance;
		double minDiscance = Double.MAX_VALUE, distance;
		java.awt.geom.Area target = null;
		
		for (java.awt.geom.Area next : needClearPart(road, pfClearArea)) {
			distance = distanceToArea(next, selfLocation);
			if (distance < minDiscance) {
				minDiscance = distance;
				target = next;
			}
		}
		
		if (target == null) // no blockades, just return
			return;
		
		math.geom2d.Point2D tangent[] = getTargetPoint(road, target, selfLocation);
		math.geom2d.Point2D locP = new math.geom2d.Point2D(selfLocation.first(), selfLocation.second());
		double tanV = world.getConfig().repairRad / (2 * world.getConfig().repairDistance);
		double rotateRadians = Math.atan(tanV);
		
		double distance_1 = Ruler.getDistance(locP.x, locP.y, tangent[0].x, tangent[0].y);
		double distance_2 = Ruler.getDistance(locP.x, locP.y, tangent[1].x, tangent[1].y);
		
		rescuecore2.misc.geometry.Line2D centerLine = road.getRoadCenterLine();
		boolean p1_inters = true;
		if (Ruler.getDistance(centerLine, tangent[0]) < Ruler.getDistance(centerLine, tangent[1]))
			p1_inters = false;
		
		if (distance_1 > limit && distance_2 > limit) {
			List<EntityID> pa = new ArrayList<>();
			pa.add(road.getId());
			if (p1_inters) {
				underlyingAgent.sendMove(time, pa, (int)tangent[0].x, (int)tangent[0].y);
			} else {
				underlyingAgent.sendMove(time, pa, (int)tangent[1].x, (int)tangent[1].y);
			}
		} else {
			math.geom2d.Point2D targetPoint;
			
			if (distance_1 > limit){
				if (p1_inters) {
					targetPoint = tangent[1].rotate(locP, -rotateRadians);
				} else {
					targetPoint = tangent[1].rotate(locP, rotateRadians);
				}
			} else if (distance_2 > limit) {
				if (p1_inters) {
					targetPoint = tangent[0].rotate(locP, rotateRadians);
				} else {
					targetPoint = tangent[0].rotate(locP, -rotateRadians);
				}
			} else {
				if (p1_inters) {
					targetPoint = tangent[0].rotate(locP, rotateRadians);
				} else {
					targetPoint = tangent[1].rotate(locP, rotateRadians);
				}
			}
			
			Vector2D vector = new Vector2D(targetPoint.x - locP.x, targetPoint.x - locP.x);
			vector.normalised().scale(1000000);
			underlyingAgent.sendClear(time, (int) (x + vector.getX()), (int) (y + vector.getY()));
		}
	}
	
	private List<java.awt.geom.Area> needClearPart(CSURoad road, java.awt.geom.Area clearArea) {
		List<java.awt.geom.Area> result = new ArrayList<>();
		java.awt.geom.Area area;
		for (CSUBlockade next : road.getCsuBlockades()) {
			area = new java.awt.geom.Area(next.getPolygon());
			area.intersect(clearArea);
			if (area.getPathIterator(null).isDone())
				continue;
			result.add(area);
		}
		
		return result;
	}
	
	private double distanceToArea(java.awt.geom.Area area, Pair<Integer, Integer> location) {
		Rectangle bound = area.getBounds();
		bound.getCenterX();
		return Ruler.getDistance(location.first(), location.second(), bound.getCenterX(), bound.getCenterY());
	}
	
	private math.geom2d.Point2D[] getTargetPoint(CSURoad road, java.awt.geom.Area area, Pair<Integer, Integer> location) {
		CompositeConvexHull convexHull = new CompositeConvexHull();
		
		PathIterator itor = area.getPathIterator(null);
		double[] point = new double[6];
		while (!itor.isDone()) {
			switch (itor.currentSegment(point)) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
				convexHull.addPoint(new Point((int)point[0], (int)point[1]));
				break;

			default:
				break;
			}
			itor.next();
		}
		
		return getTangentPoint(convexHull.getConvexPolygon(), location);
	}
	
	private math.geom2d.Point2D[] getTangentPoint(Polygon polygon, Pair<Integer, Integer> location) {
		Point centroid = new Point();
		for (int i = 0; i < polygon.npoints; i++) {
			centroid.x += polygon.xpoints[i];
			centroid.y += polygon.ypoints[i];
		}
		centroid.x /= polygon.npoints;
		centroid.y /= polygon.npoints;
		
		Line2D anchor = new Line2D(centroid.x, centroid.y, location.first(), location.second());
		double anchorAngle = anchor.getHorizontalAngle();
		
		double minAngle = 2 * Math.PI;
		double maxAngle = -2 * Math.PI;
		int maxTangentIndex = 0, minTangentIndex = 0;
		
		for (int i = 0; i < polygon.npoints; i++) {
			Point p = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			Line2D temp = new Line2D(p.getX(), p.getY(), location.first(), location.second());
			double angle = anchorAngle - temp.getHorizontalAngle();
			
			if (angle > 2 * Math.PI)
				angle -= (2 * Math.PI);
			if (angle < 0)
				angle += (2 * Math.PI);
			
			if (angle > Math.PI / 2 && angle < 1.5 * Math.PI)
				angle -= Math.PI;
			else if (angle > 1.5 * Math.PI && angle < 2 * Math.PI)
				angle -= (2 * Math.PI);
			
			if (angle > maxAngle) {
				maxAngle = angle;
				maxTangentIndex = i;
			}
			if (angle < minAngle) {
				minAngle = angle;
				minTangentIndex = i;
			}
		}
		
		math.geom2d.Point2D[] res = {
				new math.geom2d.Point2D(polygon.xpoints[maxTangentIndex], polygon.ypoints[maxTangentIndex]), 
				new math.geom2d.Point2D(polygon.xpoints[minTangentIndex], polygon.ypoints[minTangentIndex])};
		
		return res;
	}
}
