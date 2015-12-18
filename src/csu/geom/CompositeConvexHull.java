package csu.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.convhull.JarvisMarch2D;

/**
 * Implementation of Convex Hull using Graham Scan, Jarvis March and On-line
 * Chan's algorithm.
 * 
 * This class is in conformance with late-computation technique. Whenever
 * {@link #getConvexPolygon() getConvexPolygon()} is called, the actual convex
 * hull is created and kept until addition or removal of a point (possibly)
 * invalidates it.
 * 
 * @version 1.0
 */
public class CompositeConvexHull implements ConvexHull_Interface {

	/**
	 * Because the underlying algorithm requires sorting the input, we need to
	 * store points in the order we put them into data structure. So we choose
	 * List to stores all points. And Set will disrupt this order, so you should
	 * not use a Set.</br>
	 * <p>
	 * The stored points are vertices of convex hull polygon and stored in CCW
	 * order because we put them in CCW(counterclockwise) order.
	 */
	private List<Point> points;

	/**
	 * Temporary storage for points which are added but not yet taken into
	 * account for calculation.
	 */
	private Set<Point> addedPoints;

	/**
	 * Temporary storage for points which are removed but not yet taken into
	 * account for calculation.
	 */
	
	private Set<Point> removedPoints;

	/**
	 * Cached convex hull polygon. In this implementation it's updated whenever
	 * {@link #getConvexPolygon()} is called and {@link #isDataUpdated()
	 * isDataUpdated()} returns true.
	 */
	private Polygon convexHullPolygon;

	/**
	 * Delegate algorithm: Jarvis's March which can construct 2D convex hull in O(nh) time.
	 */
	private JarvisMarch2D jarvisMarchCalculator;

	// constructor
	public CompositeConvexHull() {
		this.points = new ArrayList<Point>();
		this.addedPoints = new HashSet<Point>();
		this.removedPoints = new HashSet<Point>();

		this.jarvisMarchCalculator = new JarvisMarch2D();
	}

	/**
	 * When we only needs to add some outside points, we use Chan's algorithm.
	 * <p>
	 * When we needs to add some outside points and remove some vertices from
	 * current convex hull polygon, we use delegate algorithm: Jarvis's March.
	 * <p>
	 * We do this just for time consideration.
	 * 
	 * @return a new convex hull polygon
	 */
	private Polygon updateConvexPolygon() {
		Polygon returnValue;
		if (convexHullPolygon != null) {
			Set<Point> onVerticesRemovedPoints = new HashSet<Point>();
			for (Point removedPoint : removedPoints) {
				for (int i = 0; i < convexHullPolygon.npoints; i++) {
					if (removedPoint.x == convexHullPolygon.xpoints[i]
							&& removedPoint.y == convexHullPolygon.ypoints[i]) {
						onVerticesRemovedPoints.add(removedPoint);
						break;
					}
				}
			}
			removedPoints.removeAll(onVerticesRemovedPoints);
			points.removeAll(removedPoints);
			removedPoints = onVerticesRemovedPoints; ///onVerticesPoints can not be removed

			Set<Point> onVerticesAddedPoints = new HashSet<Point>();
			Set<Point> outerAddedPoints = new HashSet<Point>();
			Set<Point> innerAddedPoints = new HashSet<Point>();

			for (Point addedPoint : addedPoints) {
				for (int i = 0; i < convexHullPolygon.npoints; i++) {
					if (addedPoint.x == convexHullPolygon.xpoints[i]
							&& addedPoint.y == convexHullPolygon.ypoints[i]) {
						onVerticesAddedPoints.add(addedPoint);
					} else if (convexHullPolygon.contains(addedPoint)) {
						innerAddedPoints.add(addedPoint);
					} else {
						outerAddedPoints.add(addedPoint);
					}
				}
			}

			//addedPoints.removeAll(innerAddedPoints);
			//removeAll(onVerticesAddedPoints);

			points.addAll(innerAddedPoints);
			points.addAll(onVerticesAddedPoints);

			addedPoints = outerAddedPoints;
		}

		if (removedPoints.isEmpty() && addedPoints.isEmpty()) {
			returnValue = convexHullPolygon;
		} else if (convexHullPolygon != null && removedPoints.isEmpty() && !addedPoints.isEmpty()) {
			addPointsToConvexHull(addedPoints);
			addedPoints.clear();
			returnValue = convexHullPolygon;
		} else {  ///convexHullPolygon == null || !removedPoints.isEmpty() || addPoints.isEmpty()
			points.removeAll(removedPoints);
			points.addAll(addedPoints);

			addedPoints.clear();
			removedPoints.clear();

			if (points != null && !points.isEmpty()) {
				List<Point2D> point2ds = convertPoints(points); ///normal to 2D
				Polygon2D polygon2d = jarvisMarchCalculator.convexHull(point2ds);
				returnValue = convertPolygon2d(polygon2d);///2D polygon to normal
			} else
				returnValue = convexHullPolygon;
		}

		return returnValue;
	}

	/**
	 * Timothy Chan's algorithm. <br/>
	 * When only needs to add some outside points to current convex hull
	 * polygon, Chan's algorithm is better than Jarvis's March because it runs
	 * in O(n log h) time.
	 * 
	 * @param addedPoints
	 *            a set of outside points will be added
	 */
	private void addPointsToConvexHull(Set<Point> addedPoints) {
		/*
		 * For each added point, we consider it separately. First we handle the
		 * first added point and current cached polygon. After some calculation,
		 * we gain a new cached polygon. Then we handle the second added point
		 * and the new cached polygon. Repeat this process till there is no no
		 * point to add any more. The final cached polygon is what we needed.
		 */
		for (Point addedPoint : addedPoints) {
			Point centroid = new Point();
			for (int i = 0; i < convexHullPolygon.npoints; i++) {
				centroid.x += convexHullPolygon.xpoints[i];
				centroid.y += convexHullPolygon.ypoints[i];
			}
			centroid.x /= convexHullPolygon.npoints;
			centroid.y /= convexHullPolygon.npoints;

			Line2D anchor = new Line2D(centroid.x, centroid.y, addedPoint.x, addedPoint.y);
			double anchorAngle = anchor.getHorizontalAngle();

			double minAngle = 2 * Math.PI;
			double maxAngle = -2 * Math.PI;
			int minAngleVertexIndex = -1;
			int maxAngleVertexIndex = -1;
			for (int i = 0; i < convexHullPolygon.npoints; i++) {
				Line2D tempLine = new Line2D(convexHullPolygon.xpoints[i],
						convexHullPolygon.ypoints[i], addedPoint.x, addedPoint.y);
				double angle = anchorAngle - tempLine.getHorizontalAngle();
				if (angle > 2 * Math.PI)
					angle -= (2 * Math.PI);
				if (angle < 0)
					angle += 2 * Math.PI;

				if (angle > Math.PI / 2 && angle < 1.5 * Math.PI)
					angle = angle - Math.PI;
				else if (angle >= 1.5 * Math.PI && angle <= 2 * Math.PI)
					angle = angle - 2 * Math.PI;

				if (angle > maxAngle) {
					maxAngle = angle;
					maxAngleVertexIndex = i;
				}
				if (angle < minAngle) {
					minAngle = angle;
					minAngleVertexIndex = i;
				}
			}
			/*
			 * All points between maxAngleVertexIndex and
			 * minAngleVertexIndex(CCW order) should be removed. The number of
			 * those points is [Math.abs(maxAngleVertexIndex -
			 * minAngleVertexIndex) - 1]. And the addedPoint should be added in.
			 * So the verticis number of new polygon is
			 * [convexHullPolygon.npoints + 2 - temp].
			 */
			int temp = Math.abs(maxAngleVertexIndex - minAngleVertexIndex);
			int[] xPoints = new int[convexHullPolygon.npoints + 2 - temp];
			int[] yPoints = new int[convexHullPolygon.npoints + 2 - temp];

			int newPointsIndex = 0;
			points.clear();

			/*
			 * All points will be added in CCW order. And those points are
			 * divided into three part. First, point at index 0 to point at
			 * index [Math.min(maxAngleVertexIndex, minAngleVertexIndex] Second,
			 * the addedPoint Third, point at index
			 * [Math.max(maxAngleVertexIndex, minAngleVertexIndex)] to end.
			 */
			for (int i = 0; i <= Math.min(maxAngleVertexIndex, minAngleVertexIndex); i++) {
				xPoints[newPointsIndex] = convexHullPolygon.xpoints[i];
				yPoints[newPointsIndex] = convexHullPolygon.ypoints[i];
				points.add(new Point(xPoints[newPointsIndex], yPoints[newPointsIndex]));
				newPointsIndex++;
			}
			xPoints[newPointsIndex] = addedPoint.x;
			yPoints[newPointsIndex] = addedPoint.y;
			points.add(new Point(xPoints[newPointsIndex], yPoints[newPointsIndex]));
			newPointsIndex++;
			int i = Math.max(maxAngleVertexIndex, minAngleVertexIndex);
			for (; i < convexHullPolygon.npoints; i++) {
				xPoints[newPointsIndex] = convexHullPolygon.xpoints[i];
				yPoints[newPointsIndex] = convexHullPolygon.ypoints[i];
				points.add(new Point(xPoints[newPointsIndex], yPoints[newPointsIndex]));
				newPointsIndex++;
			}
			convexHullPolygon = new Polygon(xPoints, yPoints, newPointsIndex);
		}
	}

	@Override
	public Polygon getConvexPolygon() {
		if (isDataUpdate()) {
			convexHullPolygon = updateConvexPolygon();
		}
		return convexHullPolygon;
	}

	@Override
	public void addPoint(int x, int y) {
		addPoint(new Point(x, y));
	}

	@Override
	public void addPoint(Point point) {
		if (removedPoints.contains(point)) {
			removedPoints.remove(point);
			addedPoints.add(point);
		} else {
			addedPoints.add(point);
		}
	}

	@Override
	public void removePoint(int x, int y) {
		removePoint(new Point(x, y));
	}

	@Override
	public void removePoint(Point point) {
		if (addedPoints.contains(point)) {
			addedPoints.remove(point);
			removedPoints.add(point);
		} else {
			removedPoints.add(point);
		}
	}

	@Override
	public void updatePoints(Collection<Point> addedPoints, Collection<Point> removedPoints) {
		if (addedPoints != null) {
			for (Point addedPoint : addedPoints)
				addPoint(addedPoint);
		}

		if (removedPoints != null) {
			for (Point removedPoint : removedPoints)
				removePoint(removedPoint);
		}
	}

	/**
	 * Flag to determines whether cached convexHullPolygon needs to update.
	 * 
	 * @return true if points data has been changed since last time
	 *         convexHullPolygon is created, false otherwise.
	 */
	private boolean isDataUpdate() {
		return !(addedPoints.isEmpty() && removedPoints.isEmpty());
	}

	/**
	 * Because the delegate algorithm Jarvis's March handles Point2D and
	 * Polygon2D, we needs method to convert normal to 2D and 2D to normal.</br>
	 * This method converts normal points to 2D points.
	 * 
	 * @param points
	 *            a list of normal points needs to convert
	 * @return a list of 2D points
	 */
	private static List<Point2D> convertPoints(List<Point> points) {
		List<Point2D> points_2ds = new ArrayList<Point2D>();

		for (Point point : points) {
			points_2ds.add(new Point2D(point.x, point.y));
		}

		return points_2ds;
	}

	/**
	 * Because the delegate algorithm Jarvis's March handles Point2D and
	 * Polygon2D, we needs method to convert normal to 2D and 2D to normal.</br>
	 * This method converts 2D polygon to normal.
	 * 
	 * @param polygon2d
	 *            target 2D polygon needed to convert
	 * @return the normal polygon
	 */
	private static Polygon convertPolygon2d(Polygon2D polygon2d) {
		Collection<Point2D> vertices = polygon2d.getVertices();
		int[] xPoints = new int[vertices.size()];
		int[] yPoints = new int[vertices.size()];
		int i = 0;

		for (Point2D point2d : vertices) {
			xPoints[i] = (int) point2d.x;
			yPoints[i] = (int) point2d.y;
			i++;
		}
		return new Polygon(xPoints, yPoints, vertices.size());
	}
}
