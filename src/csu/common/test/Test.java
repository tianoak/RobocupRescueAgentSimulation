package csu.common.test;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Test {

	private static Point[] getPerpendicularPoints(Point2D point1, Point2D point2, double radiusLength) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();
        
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
        System.out.println("Point_1: x3 = " + x3 + " y3 = " + y3);
        Point perpendicular2 = new Point((int) x4, (int) y4);
        System.out.println("Point_2: x3 = " + x4 + " y3 = " + y4);
        return new Point[]{perpendicular1, perpendicular2};
    }

	public static void main(String[] args) {
		Point point1 = new Point(-3, 0);
		Point point2 = new Point(3, 0);
		Point[] points = getPerpendicularPoints(point1, point2, 4);
		System.out.println("-----------------------------------");
		System.out.println("Point_1 is: (" + points[0].getX() + ", " + points[0].getY() + ")");
		System.out.println("Point_2 is: (" + points[1].getX() + ", " + points[1].getY() + ")");
	}
}
