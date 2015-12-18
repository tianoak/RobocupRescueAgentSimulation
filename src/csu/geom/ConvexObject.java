package csu.geom;


import java.awt.Point;
import java.awt.Polygon;
import csu.common.clustering.TestForSetTriangle;

/**
 * Please run class {@link TestForSetTriangle} to see what those points are.
 */
public class ConvexObject {

	/** 
	 * The underlying convex hull polygon of this ConvexObject.
	 */
	private Polygon convexHullPolygon;
	
	/** 
	 * A triangle represent a direction.
	 */
	private Polygon triangle;
	
	private Polygon directionRectangle;
	
	/** 
	 * This point represent the main expand direction of this fire cluster.
	 */
	public Point CENTER_POINT;
	
	/** 
	 * This point is the center point of Cluster this ConvexObject belongs to.
	 */
	public Point CONVEX_POINT;
	
	/**
	 * 
	 */
	public Point FIRST_POINT;
	
	/**
	 * 
	 */
	public Point SECOND_POINT;
	
	/**
	 * 
	 */
	public Point OTHER_POINT_1;
	
	/***/
	public Point OTHER_POINT_2;
	
	public ConvexObject(Polygon convexHullPolygon) {
		this.convexHullPolygon = convexHullPolygon;
	}
	public ConvexObject() {
		// empty constructor, do nothing
	}
	
	public Polygon getConvexHullPolygon() {
		return this.convexHullPolygon;
	}
	public void setConvexHullPolygon(Polygon convexHullPolygon) {
		this.convexHullPolygon = convexHullPolygon;
	}
	
	public Polygon getTriangle() {
		return this.triangle;
	}
	
	public void setTriangle(Polygon triangle) {
		int vertexCount = triangle.npoints;
		
		int[] xs = new int[vertexCount];
		int[] ys = new int[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			xs[i] = triangle.xpoints[i];
			ys[i] = triangle.ypoints[i];
		}
		
		this.triangle = new Polygon(xs, ys, vertexCount);
	}
	
	public Polygon getDirectionRectangle() {
		return this.directionRectangle;
	}
	
	public void setDirectionRectangle(Polygon rectangle) {
		int vertexCount = rectangle.npoints;
		
		int[] x_coordinates = new int[vertexCount];
		int[] y_coordinates = new int[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			x_coordinates[i] = rectangle.xpoints[i];
			y_coordinates[i] = rectangle.ypoints[i];
		}
		
		this.directionRectangle = new Polygon(x_coordinates, y_coordinates, vertexCount);
	}
}
