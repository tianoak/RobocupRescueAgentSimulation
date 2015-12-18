package csu.common.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.PathIterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.line.Line2D;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.DrawingTools;

@SuppressWarnings("serial")
public class TangentPoint extends JPanel{
	
	private Color MAX_TANGENT_COLOR = Color.GREEN;
	private Color MIN_TANGENT_COLOR = Color.CYAN;
	
	private Stroke stroke = new BasicStroke(1.5f, 0, 2);
	
	private Point location = new Point(800, 500);
	
	private Point location_2 = new Point(50, 250);

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gra_2d = (Graphics2D)g;
		
		gra_2d.setStroke(stroke);
		
		Polygon polygon = makePolygon(6, 500, 300, 150);
		Point[] tangent_1 = getTangentPoint(polygon, location);
		Point[] tangent_2 = getTangentPoint(polygon, location_2);
		
		PathIterator itor = polygon.getPathIterator(null);
		double[] point = new double[6];
		double x = 0.0, y = 0.0, init_x = 0.0, init_y = 0.0;;
		while(!itor.isDone()) {
			switch(itor.currentSegment(point)) {
			case PathIterator.SEG_MOVETO:
				x = point[0];
				y = point[1];
				init_x = point[0];
				init_y = point[1];
				break;
			case PathIterator.SEG_LINETO:
				gra_2d.drawLine((int)x, (int)y, (int)point[0], (int)point[1]);
				DrawingTools.drawArrowHeads((int)x, (int)y, (int)point[0], (int)point[1], gra_2d);
				x = point[0];
				y = point[1]; 
				break;
			default:
				gra_2d.drawLine((int)x, (int)y, (int)init_x, (int)init_y);
				DrawingTools.drawArrowHeads((int)x, (int)y, (int)init_x, (int)init_y, gra_2d);
				break;
			}
			
			itor.next();
		}
		
		gra_2d.setColor(MAX_TANGENT_COLOR);
		gra_2d.drawLine((int)location.getX(), 
				(int)location.getY(), (int)tangent_1[0].getX(), (int)tangent_1[0].getY());
		DrawingTools.drawArrowHeads((int)location.getX(), 
				(int)location.getY(), (int)tangent_1[0].getX(), (int)tangent_1[0].getY(), gra_2d);
		gra_2d.drawLine((int)location_2.getX(), (int)location_2.getY(), 
				(int)tangent_2[0].getX(), (int)tangent_2[0].getY());
		DrawingTools.drawArrowHeads((int)location_2.getX(), (int)location_2.getY(), 
				(int)tangent_2[0].getX(), (int)tangent_2[0].getY(), gra_2d);
		
		gra_2d.setColor(MIN_TANGENT_COLOR);
		gra_2d.drawLine((int)location.getX(), 
				(int)location.getY(), (int)tangent_1[1].getX(), (int)tangent_1[1].getY());
		DrawingTools.drawArrowHeads((int)location.getX(), 
				(int)location.getY(), (int)tangent_1[1].getX(), (int)tangent_1[1].getY(), gra_2d);
		gra_2d.drawLine((int)location_2.getX(), (int)location_2.getY(), 
				(int)tangent_2[1].getX(), (int)tangent_2[1].getY());
		DrawingTools.drawArrowHeads((int)location_2.getX(), (int)location_2.getY(), 
				(int)tangent_2[1].getX(), (int)tangent_2[1].getY(), gra_2d);
	}
	
	private Point[] getTangentPoint(Polygon polygon, Point location) {
		Point centroid = new Point();
		for (int i = 0; i < polygon.npoints; i++) {
			centroid.x += polygon.xpoints[i];
			centroid.y += polygon.ypoints[i];
		}
		centroid.x /= polygon.npoints;
		centroid.y /= polygon.npoints;
		
		Line2D anchor = new Line2D(centroid.x, centroid.y, location.x, location.y);
		double anchorAngle = anchor.getHorizontalAngle();
		
		double minAngle = 2 * Math.PI;
		double maxAngle = -2 * Math.PI;
		Point minPoint = null, maxPoint = null;
		
		for (int i = 0; i < polygon.npoints; i++) {
			Point p = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			Line2D temp = new Line2D(p.getX(), p.getY(), location.getX(), location.getY());
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
				maxPoint = p;
			}
			if (angle < minAngle) {
				minAngle = angle;
				minPoint = p;
			}
		}
		
		Point[] res = {maxPoint, minPoint};
		
		return res;
	}
	
	/**
	 * Create a polygon.
	 * 
	 * @param vertexsCount
	 *            the vertex count of this polygon
	 * @param center_x
	 *            the x coordinate of this polygon's center
	 * @param center_y
	 *            the y coordinate of this polygon's center
	 * @param radius
	 *            the radius length of this polygon's
	 * @return a polygon with given vertex count.
	 */
	private Polygon makePolygon(int vertexsCount, int center_x, int center_y, double radius) {
		double dAngle = Math.PI * 2 / vertexsCount;
		int[] x_coordinates = new int[vertexsCount];
		int[] y_coordinates = new int[vertexsCount];
		
		for (int i = 0; i < vertexsCount; i++) {
			double angle = i * dAngle;
			Vector2D vector = new Vector2D(Math.sin(angle), Math.cos(angle)).scale(radius);
			Point2D centerPoint = new Point2D(center_x, center_y);
			Point2D vertexPoint = centerPoint.translate(vector.getX(), vector.getY());
			
			x_coordinates[i] = (int)vertexPoint.getX();
			y_coordinates[i] = (int)vertexPoint.getY();
		}
		
		return new Polygon(x_coordinates, y_coordinates, vertexsCount);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Tangent Point");
		TangentPoint test = new TangentPoint();
		frame.add(test);
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
