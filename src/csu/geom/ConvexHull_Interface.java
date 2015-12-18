package csu.geom;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Collection;

public interface ConvexHull_Interface {

	/**
	 * Gets or calculates the output convex hull based on internal point data.
	 * 
	 * @return A Polygon object representing the convex hull
	 */
	public Polygon getConvexPolygon();

	/**
	 * Adds a point to the point-list. Early-calculation implementations should
	 * recalculate Convex Hull polygon after addition.
	 * 
	 * @param x
	 *            X Coordinate of the added point
	 * @param y
	 *            Y Coordinate of the added point
	 * @see #addPoint(java.awt.Point)
	 */
	public void addPoint(int x, int y);

	/**
	 * Adds a point to the point-list. Early-calculation implementations should
	 * recalculate Convex Hull polygon after addition.
	 * 
	 * @param point
	 *            Coordinates of the added point
	 * @see #addPoint(int, int)
	 */
	public void addPoint(Point point);

	/**
	 * Removes a point from the point-list. Early-calculation implementations
	 * should recalculate Convex Hull polygon after removal.
	 * 
	 * @param x
	 *            X Coordinate of the removed point
	 * @param y
	 *            Y Coordinate of the removed point
	 * @see #removePoint(java.awt.Point)
	 */
	public void removePoint(int x, int y);

	/**
	 * Removes a point from the point-list. Early-calculation implementations
	 * should recalculate Convex Hull polygon after removal.
	 * 
	 * @param point
	 *            Coordinates of the removed point
	 * @see #removePoint(int, int)
	 */
	public void removePoint(Point point);

	/**
	 * Updates the point-list based on the presented added and removed points.
	 * Early-calculation implementations should recalculate Convex Hull polygon
	 * after update.<br/>
	 * <p>
	 * Basic pre-optimization implementation can delegate to
	 * {@link #removePoint(java.awt.Point)} and
	 * {@link #addPoint(java.awt.Point)}.
	 * 
	 * @param addedPoints
	 *            Coordinates of the added points
	 * @param removedPoints
	 *            Coordinates of the removed points
	 */
	public void updatePoints(Collection<Point> addedPoints, Collection<Point> removedPoints);
}
