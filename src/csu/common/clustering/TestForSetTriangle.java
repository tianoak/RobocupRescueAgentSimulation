package csu.common.clustering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.conic.Circle2D;

import csu.geom.CompositeConvexHull;
import csu.geom.ConvexObject;
import csu.standard.Ruler;

/**
 * A test class for {@link FireCluster#setTriangle(Point, boolean)
 * setTriangle(Point, boolean)} method.
 * <p>
 * Date: Mar,16, 2014  Time: 3:24pm
 * 
 * @author appreciation-csu
 * 
 */
@SuppressWarnings("serial")
public class TestForSetTriangle extends JPanel{
	private static final Color FIRST_POINT_COLOR = Color.GREEN;
	private static final Color SECOND_POINT_COLOR = Color.BLUE;
	private static final Color PERPENDICULAR_POINT_COLOR = Color.RED;
	private static final Color OTHER_POINT_COLOR = Color.MAGENTA;
	private static final Color LINE_COLOR = Color.CYAN;
	private static final Stroke LINE_STROKE = new BasicStroke(1.5f);
	
	private CompositeConvexHull convexHull;
	private ConvexObject convexObject;
	private Polygon convexHullPolygon;
	private boolean isOverCenter;
	
	public TestForSetTriangle() {
		convexHull = new CompositeConvexHull();
		convexObject = new ConvexObject();
		isOverCenter = false;
		
		Random random = new Random();
		int x_coordinate, y_coordinate;
		
		for (int i = 0; i < 100; i++) {
			x_coordinate = random.nextInt(500) + 10;
			y_coordinate = random.nextInt(500) + 10;
			convexHull.addPoint(new Point(x_coordinate, y_coordinate));
		}
		
		convexHullPolygon = convexHull.getConvexPolygon();
		
		if (!isOverCenter)
			checkForOverCenter(new Point(650, 650));
		setTriangle(isOverCenter);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gra_2d = (Graphics2D) g;
		
		Circle2D circle = new Circle2D(convexObject.CENTER_POINT.x, convexObject.CENTER_POINT.y, 10d, true);
		gra_2d.setColor(FIRST_POINT_COLOR);
		circle.fill(gra_2d);
		gra_2d.drawString("center point", convexObject.CENTER_POINT.x + 15, convexObject.CENTER_POINT.y);
		
		circle = new Circle2D(convexObject.CONVEX_POINT.x, convexObject.CONVEX_POINT.y, 10d, true);
		gra_2d.setColor(SECOND_POINT_COLOR);
		circle.fill(gra_2d);
		gra_2d.drawString("convex point", convexObject.CONVEX_POINT.x + 15, convexObject.CONVEX_POINT.y);
		
		gra_2d.setColor(PERPENDICULAR_POINT_COLOR);
		circle = new Circle2D(convexObject.FIRST_POINT.x, convexObject.FIRST_POINT.y, 10, true);
		circle.fill(gra_2d);
		gra_2d.drawString("first point", convexObject.FIRST_POINT.x + 15, convexObject.FIRST_POINT.y);
		
		circle = new Circle2D(convexObject.SECOND_POINT.x, convexObject.SECOND_POINT.y, 10, true);
		circle.fill(gra_2d);
		gra_2d.drawString("second point", convexObject.SECOND_POINT.x + 15, convexObject.SECOND_POINT.y);
		
		if (isOverCenter) {
			gra_2d.setColor(OTHER_POINT_COLOR);
			circle = new Circle2D(convexObject.OTHER_POINT_1.x, convexObject.OTHER_POINT_1.y, 10, true);
			circle.fill(gra_2d);
			gra_2d.drawString("other point_1", convexObject.OTHER_POINT_1.x + 15, convexObject.OTHER_POINT_1.y);
			
			circle = new Circle2D(convexObject.OTHER_POINT_2.x, convexObject.OTHER_POINT_2.y, 10, true);
			circle.fill(gra_2d);
			gra_2d.drawString("other point_2", convexObject.OTHER_POINT_2.x + 15, convexObject.OTHER_POINT_2.y);
		}
		
		gra_2d.setColor(LINE_COLOR);
		gra_2d.setStroke(LINE_STROKE);
		gra_2d.drawLine(convexObject.CENTER_POINT.x, 
				convexObject.CENTER_POINT.y, convexObject.CONVEX_POINT.x, convexObject.CONVEX_POINT.y);
		gra_2d.drawLine(convexObject.FIRST_POINT.x, 
				convexObject.FIRST_POINT.y, convexObject.SECOND_POINT.x, convexObject.SECOND_POINT.y);
		
		for (int i = 0; i < convexHullPolygon.npoints; i++) {
			int x1 = convexHullPolygon.xpoints[i];
			int y1 = convexHullPolygon.ypoints[i];
			int x2 = convexHullPolygon.xpoints[(i + 1) % convexHullPolygon.npoints];
			int y2 = convexHullPolygon.ypoints[(i + 1) % convexHullPolygon.npoints];
			gra_2d.drawLine(x1, y1, x2, y2);
		}
		
		gra_2d.setStroke(new BasicStroke(1.8f, 0, 2, 0, new float[]{9}, 0));
		gra_2d.drawLine(convexObject.FIRST_POINT.x, 
				convexObject.FIRST_POINT.y, convexObject.CONVEX_POINT.x, convexObject.CONVEX_POINT.y);
		gra_2d.drawLine(convexObject.SECOND_POINT.x, 
				convexObject.SECOND_POINT.y, convexObject.CONVEX_POINT.x, convexObject.CONVEX_POINT.y);
		
		if (isOverCenter) {
			gra_2d.drawLine(convexObject.FIRST_POINT.x, 
					convexObject.FIRST_POINT.y, convexObject.OTHER_POINT_1.x, convexObject.OTHER_POINT_1.y);
			gra_2d.drawLine(convexObject.SECOND_POINT.x, 
					convexObject.SECOND_POINT.y, convexObject.OTHER_POINT_2.x, convexObject.OTHER_POINT_2.y);
			gra_2d.drawLine(convexObject.OTHER_POINT_1.x, 
					convexObject.OTHER_POINT_1.y, convexObject.OTHER_POINT_2.x, convexObject.OTHER_POINT_2.y);
		}
		
		gra_2d.setColor(Color.red);
		gra_2d.setFont(new Font("", Font.PLAIN, 20));
		gra_2d.drawString("is over center: " + Boolean.valueOf(isOverCenter).toString(), 450, 80);
	}
	
	private void checkForOverCenter(Point targetPoint) {
		Polygon convexPolygon = this.convexHull.getConvexPolygon();
		Rectangle convexPolygonBound = convexPolygon.getBounds();
		int convexCenterPoint_x = (int)convexPolygonBound.getCenterX();
		int convexCenterPoint_y = (int)convexPolygonBound.getCenterY();
		Point convexCenterPoint = new Point(convexCenterPoint_x, convexCenterPoint_y);
		
		this.convexObject.CENTER_POINT = targetPoint;
		this.convexObject.CONVEX_POINT = convexCenterPoint;
		
		
		Point centerPoint = this.convexObject.CENTER_POINT;
		Point convexPoint = this.convexObject.CONVEX_POINT;
		
		int[] xs = this.convexHull.getConvexPolygon().xpoints;
		int[] ys = this.convexHull.getConvexPolygon().ypoints;
		
		double x1, y1, x2, y2, total_1, total_2;
		
		for (int i = 0; i < ys.length; i++) {
			Point point = new Point(xs[i], ys[i]);
			x1 = (convexPoint.getX() - centerPoint.getX()) / 1000;
			y1 = (convexPoint.getY() - centerPoint.getY()) / 1000;
			
			x2 = (point.getX() - centerPoint.getX()) / 1000;
			y2 = (point.getY() - centerPoint.getY()) / 1000;
			
			total_1 = x1 * x2;
			total_2 = y1 * y2;
			if (total_1 <= 0 && total_2 <= 0 /*or total_1 + total_2 <= 0*/) {
				this.isOverCenter = true;
				break;
			}
		}
	}
	
	private void setTriangle(boolean isOverCenter) {
		Polygon convexPolygon = this.convexHull.getConvexPolygon();
		Rectangle convexPolygonBound = convexPolygon.getBounds();
		double polygonBoundWidth = convexPolygonBound.getWidth();
		double polygonBoundHeight = convexPolygonBound.getHeight();
		double radiusLength = Math.hypot(polygonBoundWidth, polygonBoundHeight); 
		
		Point targetPoint = this.convexObject.CENTER_POINT;
		Point convexPoint = this.convexObject.CONVEX_POINT;
		
		if (isOverCenter) {
			radiusLength /= 2.0;
		} else {
			rescuecore2.misc.geometry.Point2D point = 
					new rescuecore2.misc.geometry.Point2D(targetPoint.getX(), targetPoint.getY());
			double distance = Ruler.getDistance(convexPolygon, point);
			
			if (distance > radiusLength)
				radiusLength = distance;
///keep consistent with this method in fireCluster			
//			if (distance < radiusLength / 4.0)
//				radiusLength /= 4.0;
//			else
//				radiusLength = distance;
		}
		
		Point[] points = getPerpendicularPoints(targetPoint, convexPoint, radiusLength);
		Point point1 = points[0], point2 = points[1];
		
		this.convexObject.FIRST_POINT = points[0];
		this.convexObject.SECOND_POINT = points[1];
		 
		Polygon trianglePolygon = new Polygon();
		trianglePolygon.addPoint(convexPoint.x, convexPoint.y);
		trianglePolygon.addPoint(point1.x, point1.y);
		trianglePolygon.addPoint(point2.x, point2.y);
		this.convexObject.setTriangle(trianglePolygon);
		
		if (isOverCenter) {
			double distance;
			if (isOverCenter) {
				distance = point1.distance(point2) / 2.0;
			} else {
				rescuecore2.misc.geometry.Point2D point = 
						new rescuecore2.misc.geometry.Point2D(targetPoint.getX(), targetPoint.getY());
				distance = Ruler.getDistance(convexPolygon, point);
			}
			points = getPerpendicularPoints(point1, point2, distance);
			if (convexPoint.distance(points[0]) > convexPoint.distance(points[1])) {
				trianglePolygon.addPoint(points[0].x, points[0].y);
				this.convexObject.OTHER_POINT_1 = points[0];
			} else {
				trianglePolygon.addPoint(points[1].x, points[1].y);
				this.convexObject.OTHER_POINT_1 = points[1];
			}
			points = getPerpendicularPoints(point2, point1, distance);
			if (convexPoint.distance(points[0]) > convexPoint.distance(points[1])) {
				trianglePolygon.addPoint(points[0].x, points[0].y);
				this.convexObject.OTHER_POINT_2 = points[0];
			} else {
				trianglePolygon.addPoint(points[1].x, points[1].y);
				this.convexObject.OTHER_POINT_2 = points[1];
			}
		}
	}
	
	/**
	 * We have a test class {@link TestForGetPerpendicularPoints} for this
	 * method. Please run this test class to see what this method can do.
	 */
    public Point[] getPerpendicularPoints(Point2D P_1, Point2D P_2, double radiusLength) {
    	double x1 = P_1.getX();
        double y1 = P_1.getY();
        double x2 = P_2.getX();
        double y2 = P_2.getY();
        
        double x3, x4, y3, y4;
        
        if (y1 == y2) {
        	x3 = x1;
        	x4 = x1;
        	y3 = y1 + radiusLength;
        	y4 = y1 - radiusLength;
        } else {
        	/* a * X^2 + b * X + c = 0 */
            double m1 = (y1 - y2) / (x1 - x2);
            double m2 = (-1 / m1);
            double a = Math.pow(m2, 2) + 1;
            double b = (-2 * x1) - (2 * Math.pow(m2, 2) * x1);
            double c = (Math.pow(x1, 2) * (Math.pow(m2, 2) + 1)) - Math.pow(radiusLength, 2);

            x3 = ((-1 * b) + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
            x4 = ((-1 * b) - Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
            y3 = (m2 * x3) - (m2 * x1) + y1;
            y4 = (m2 * x4) - (m2 * x1) + y1;
        }

        Point perpendicular1 = new Point((int) x3, (int) y3);
        Point perpendicular2 = new Point((int) x4, (int) y4);
        return new Point[]{perpendicular1, perpendicular2};
    }
    
    public static void main(String[] args) {
    	JFrame frame = new JFrame("Set Triangle");
    	frame.add(new TestForSetTriangle());
    	frame.setSize(1000, 800);
    	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	frame.setVisible(true);
    }
}
