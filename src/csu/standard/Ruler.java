package csu.standard;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import csu.standard.simplePartition.Locator;
import csu.util.Util;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;


/**
 * Calculate distance.
 * 
 * @author nale
 */
public class Ruler {

	public static double getDistance(Polygon polygon1, Polygon polygon2) {
		int count = polygon2.npoints;
		double minDistance = Double.MAX_VALUE;
		int j;
		double distance;
		for (int i = 0; i < count; i++) {
			j = (i + 1) % count;
			rescuecore2.misc.geometry.Point2D startPoint = new rescuecore2.misc.geometry.Point2D(
					polygon2.xpoints[i], polygon2.ypoints[i]);
			rescuecore2.misc.geometry.Point2D endPoint = new rescuecore2.misc.geometry.Point2D(
					polygon2.xpoints[j], polygon2.ypoints[j]);
			
			rescuecore2.misc.geometry.Line2D poly2Line = new rescuecore2.misc.geometry.Line2D(
					startPoint, endPoint);

			distance = getDistance(poly2Line, polygon1);
			minDistance = Math.min(minDistance, distance);
			if (minDistance == 0.0) {
				break;
			}
		}
		return minDistance;
	}

	public static double getDistance(rescuecore2.misc.geometry.Line2D line, Polygon polygon) {
		List<rescuecore2.misc.geometry.Line2D> polyLines = Util.getLine2DOfPolygon(polygon);
		double minDist = Double.MAX_VALUE;
		for (rescuecore2.misc.geometry.Line2D polyLine : polyLines) {
			minDist = Math.min(minDist, getDistance(line, polyLine));
			if (minDist == 0.0) {
				break;
			}
		}
		return minDist;
	}

	public static double getDistance(rescuecore2.misc.geometry.Line2D line1,
			rescuecore2.misc.geometry.Line2D line2) {
		double dist1 = getDistance(line1, line2.getOrigin());
		double dist2 = getDistance(line1, line2.getEndPoint());

		double dist3 = getDistance(line2, line1.getOrigin());
		double dist4 = getDistance(line2, line1.getEndPoint());

		double min = Math.min(dist1, dist2);
		min = Math.min(min, dist3);
		min = Math.min(min, dist4);
		return min;
	}

	public static double getDistance(rescuecore2.misc.geometry.Line2D line,
			rescuecore2.misc.geometry.Point2D point) {
		rescuecore2.misc.geometry.Point2D startPoint = line.getOrigin();
		rescuecore2.misc.geometry.Point2D endPoint = line.getEndPoint();
		
		return Line2D.ptSegDist(startPoint.getX(), startPoint.getY(),
				endPoint.getX(), endPoint.getY(), point.getX(), point.getY());
	}
	
	public static double getDistance(rescuecore2.misc.geometry.Line2D line, math.geom2d.Point2D point) {
		rescuecore2.misc.geometry.Point2D startPoint = line.getOrigin();
		rescuecore2.misc.geometry.Point2D endPoint = line.getEndPoint();
		
		return Line2D.ptSegDist(startPoint.getX(), startPoint.getY(),
				endPoint.getX(), endPoint.getY(), point.getX(), point.getY());
	}
	
	public static double getDistance(Polygon polygon, Pair<Integer, Integer> location) {
		rescuecore2.misc.geometry.Point2D point = new rescuecore2.misc.geometry.Point2D(
				location.first(), location.second());
		return getDistance(polygon, point);
	}

	public static double getDistance(Polygon polygon, math.geom2d.Point2D point) {
		rescuecore2.misc.geometry.Point2D tempPoint = new rescuecore2.misc.geometry.Point2D(
				point.x, point.y);
		return getDistance(polygon, tempPoint);
	}

	public static double getDistance(Polygon polygon, Point point) {
		rescuecore2.misc.geometry.Point2D tempPoint = new rescuecore2.misc.geometry.Point2D(
				point.x, point.y);
		return getDistance(polygon, tempPoint);
	}

	public static double getDistance(Polygon polygon, rescuecore2.misc.geometry.Point2D point) {
		if (polygon.contains(point.getX(), point.getY()))
			return 0.0;

		int count = polygon.npoints;
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < count; i++) {
			int j = (i + 1) % count;
			rescuecore2.misc.geometry.Point2D startPoint = new rescuecore2.misc.geometry.Point2D(
					polygon.xpoints[i], polygon.ypoints[i]);
			
			rescuecore2.misc.geometry.Point2D endPoint = new rescuecore2.misc.geometry.Point2D(
					polygon.xpoints[j], polygon.ypoints[j]);
			
			rescuecore2.misc.geometry.Line2D polygonLine = new rescuecore2.misc.geometry.Line2D(
					startPoint, endPoint);
			
			double distance = getDistance(polygonLine, point);
			minDistance = Math.min(minDistance, distance);
			if (minDistance == 0.0)
				break;
		}
		return minDistance;
	}
	
	public static double getDistance(rescuecore2.misc.geometry.Line2D line, Pair<Integer, Integer> pair) {
		rescuecore2.misc.geometry.Point2D startPoint = line.getOrigin();
		rescuecore2.misc.geometry.Point2D endPoint = line.getEndPoint();
		
		return Line2D.ptSegDist(startPoint.getX(), startPoint.getY(),
				endPoint.getX(), endPoint.getY(), pair.first(), pair.second());
	}

	public static double getDistance(Pair<Integer, Integer> pair, Point point) {
		return Math.hypot(pair.first() - point.x, pair.second() - point.y);
	}
	
	public static double getDistance(rescuecore2.misc.geometry.Point2D p1, rescuecore2.misc.geometry.Point2D p2) {
		
		return Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	public static double getDistance(Point point1, Point point2) {
		return Math.hypot(point1.getX() - point2.getX(), point1.getY() - point2.getY());
	}

	public static int getDistance(int x1, int y1, int x2, int y2) {
		return (int) Math.hypot(x1 - x2, y1 - y2);
	}
	
	public static int getDistance(double x1, double y1, double x2, double y2) {
		return (int) Math.hypot(x1 - x2, y1 - y2);
	}
	
	public static double getDistance(Area area1, Area area2) {
		return Math.hypot(area1.getX() - area2.getX(), area1.getY() - area2.getY());
	}

	public static double getDistance(Pair<Integer, Integer> pair1, Pair<Integer, Integer> pair2) {
		int x1 = pair1.first().intValue();
		int y1 = pair1.second().intValue();
		int x2 = pair2.first().intValue();
		int y2 = pair2.second().intValue();
		return Math.hypot((x2 - x1) * 1.0, (y2 - y1) * 1.0);
	}

	public static double getDistance(int x1, int y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0));
	}

	public static double getCenterDistance(Edge e1, Edge e2) {
		Point p1, p2;
		double d;

		p1 = Locator.getCenter(e1);
		p2 = Locator.getCenter(e2);
		d = getDistance(p1, p2);
		return d;
	}

	public static int getDistance(Edge e1, Edge e2) {
		double d1, d2, d3, d4;
		int d;
		Line2D l1, l2;

		l1 = Locator.getLine(e1);
		l2 = Locator.getLine(e2);
		
		d1 = l1.ptSegDist(l2.getP1());
		d2 = l1.ptSegDist(l2.getP2());
		d3 = l2.ptSegDist(l1.getP1());
		d4 = l2.ptSegDist(l1.getP2());
		d = (int) Math.min(Math.min(d3, d4), Math.min(d1, d2));
		return d;
	}

	public static int getDistance(Point2D p1, Point2D p2) {
		double dx, dy;
		int d;

		dx = p1.getX() - p2.getX();
		dy = p1.getY() - p2.getY();
		d = (int) Math.hypot(dx, dy);
		return d;
	}

	public static int getDistanceToBlock(Blockade b, int x, int y) {
		List<rescuecore2.misc.geometry.Line2D> lines = GeometryTools2D.pointsToLines(
						GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		rescuecore2.misc.geometry.Point2D origin = new rescuecore2.misc.geometry.Point2D(x, y);
		for (rescuecore2.misc.geometry.Line2D next : lines) {
			rescuecore2.misc.geometry.Point2D closest = GeometryTools2D
					.getClosestPointOnSegment(next, origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			// LOG.debug("Next line: " + next + ", closest point: " + closest + ", distance: " + d);
			if (d < best) {
				best = d;
				// LOG.debug("New best distance");
			}

		}
		return (int) best;
	}

	public static int getDistanceToBlock(Blockade block, Point point) {
		int x, y, d;

		x = point.x;
		y = point.y;
		d = getDistanceToBlock(block, x, y);
		return d;
	}

	public static int getLength(Line2D line) {
		Point2D p1, p2;
		int d;

		p1 = line.getP1();
		p2 = line.getP2();
		d = getDistance(p1, p2);
		return d;
	}

	public static int getDistance(Building b1, Building b2) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge edge : b1.getEdges()) {
			int d;

			d = getDistance(b2, edge);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistance(Building building, Edge edge) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge e : building.getEdges()) {
			int d;

			d = getDistance(e, edge);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistanceToEdges(Point point, Building building) {
		int min;
		boolean first;

		first = true;
		min = 0;
		for (Edge e : building.getEdges()) {
			int d;

			d = getDistance(point, e);
			if (first || d < min) {
				min = d;
				first = false;
			}
		}
		return min;
	}

	public static int getDistance(Point point, Edge edge) {
		int d;
		Line2D l;

		l = Locator.getLine(edge);
		d = (int) l.ptSegDist(point);
		return d;
	}
}
