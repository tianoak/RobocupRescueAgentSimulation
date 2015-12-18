package csu.common.clustering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.conic.Circle2D;

/**
 * A test class for
 * {@link FireCluster#getPerpendicularPoints(Point2D, Point2D, double)
 * getPerpendicularPoints(Point2D, Point2D, double)} method.
 * <p>
 * Date: Mar 16, 2014  Time: 2:05pm
 * 
 * @author appreciation-csu
 * 
 */
@SuppressWarnings("serial")
public class TestForGetPerpendicularPoints extends JPanel{
	
	private static final Color FIRST_POINT_COLOR = Color.GREEN;
	private static final Color SECOND_POINT_COLOR = Color.BLUE;
	private static final Color PERPENDICULAR_POINT_COLOR = Color.RED;
	private static final Color LINE_COLOR = Color.CYAN;
	private static final Stroke LINE_STROKE = new BasicStroke(1.5f);
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D gra_2d = (Graphics2D) g;
		Point2D firstPoint = new Point2D.Double(300, 300);
		Point2D secondPoint = new Point2D.Double(600, 600);
		
		Point [] perpendicularPoints = getPerpendicularPoints(firstPoint, secondPoint, 300);
		
		Circle2D circle = new Circle2D(firstPoint.getX(), firstPoint.getY(), 10d, true);
		gra_2d.setColor(FIRST_POINT_COLOR);
		circle.fill(gra_2d);
		gra_2d.drawString("first point", (int)firstPoint.getX() + 15, (int)firstPoint.getY());
		
		circle = new Circle2D(secondPoint.getX(), secondPoint.getY(), 10d, true);
		gra_2d.setColor(SECOND_POINT_COLOR);
		circle.fill(gra_2d);
		gra_2d.drawString("second point", (int)secondPoint.getX() + 15, (int)secondPoint.getY());
		
		gra_2d.setColor(PERPENDICULAR_POINT_COLOR);
		circle = new Circle2D(perpendicularPoints[0].getX(), perpendicularPoints[0].getY(), 10, true);
		circle.fill(gra_2d);
		gra_2d.drawString("first", perpendicularPoints[0].x + 15, perpendicularPoints[0].y);
		
		circle = new Circle2D(perpendicularPoints[1].getX(), perpendicularPoints[1].getY(), 10, true);
		circle.fill(gra_2d);
		gra_2d.drawString("second", perpendicularPoints[1].x + 15, perpendicularPoints[1].y);
		
		gra_2d.setColor(LINE_COLOR);
		gra_2d.setStroke(LINE_STROKE);
		gra_2d.drawLine((int)firstPoint.getX(), 
				(int)firstPoint.getY(), (int)secondPoint.getX(), (int)secondPoint.getY());
		gra_2d.drawLine(perpendicularPoints[0].x, 
				perpendicularPoints[0].y, perpendicularPoints[1].x, perpendicularPoints[1].y);
	}

	private Point[] getPerpendicularPoints(Point2D firstPoint, Point2D secondPoint, double radiusLength) {
    	///System.out.println("being used...................");
		double x1 = firstPoint.getX();
        double y1 = firstPoint.getY();
        double x2 = secondPoint.getX();
        double y2 = secondPoint.getY();
        
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
            
           ///
//            double x5, x6, y5, y6;
//            x5 = x1 - Math.sqrt(Math.pow(radiusLength,2) / (Math.pow(m2,2)+1));
//            x6 = x1 + Math.sqrt(Math.pow(radiusLength,2) / (Math.pow(m2,2)+1));
//            y5 = (m2 * x5) - (m2 * x1) + y1;
//            y6 = (m2 * x6) - (m2 * x1) + y1;
//            System.out.println("3, 4:          " + x3 + "   "+ y3 + "   " + x4 +"   "+y4);
//            System.out.println("5, 6:          " + x5 + "   "+y5 +"   "+ x6 +"   "+ y6);
        }

        Point perpendicular1 = new Point((int) x3, (int) y3);
        Point perpendicular2 = new Point((int) x4, (int) y4);
        return new Point[]{perpendicular1, perpendicular2};
    }
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Perpendicular Point");
		frame.setSize(1000, 800);
		frame.add(new TestForGetPerpendicularPoints());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
