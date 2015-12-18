package csu.common.test;

import math.geom2d.Point2D;

public class Test4 {

	public static void main(String[] args) {
		int theta = 30;
		double degree = 0;
		Point2D point = new Point2D(0.0, 0.0);
		Point2D targetPoint = new Point2D(1.0, 1.0);
		while(degree <360) {
			System.out.println("(" + targetPoint.x + "," + targetPoint.y + ")");
			targetPoint = targetPoint.rotate(point, Math.toRadians(45));
			System.out.println("(" + targetPoint.x + "," + targetPoint.y + ")");
			System.out.println("---------------------------");
			degree += theta;
		}
	}
}
