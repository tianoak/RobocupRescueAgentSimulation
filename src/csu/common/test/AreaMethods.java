package csu.common.test;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

@SuppressWarnings("serial")
public class AreaMethods extends JPanel{
	
	private Color expandColor = Color.green;
	
	// BasicStroke.CAP_BUTT = 0          BasicStroke.JOIN_BEVEL = 2
	private Stroke stroke = new BasicStroke(3.0f, 0, 2);
	
	private int center_x = 350;
	private int center_y = 350;
	
	private int center_x_2 = 350;
	private int center_y_2 = 350;
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gra_2D = (Graphics2D)  g;
		gra_2D.setStroke(stroke);
		
		Polygon initialPolygon = makePolygon(6, center_x, center_y, 100);
		gra_2D.draw(initialPolygon);
		
		Polygon polygon_2 = makePolygon(6, center_x_2, center_y_2, 70);
		gra_2D.draw(polygon_2);
		
		Polygon polygon_3 = makePolygon(6, center_x_2, center_y, 50);
		gra_2D.draw(polygon_3);
		
		gra_2D.setColor(expandColor);
		
		Area area_1 = new Area(initialPolygon);
		Area area_2 = new Area(polygon_2);
		Area area_3 = new Area(polygon_3);
//		area_2.exclusiveOr(area_3);
//		area_1.intersect(area_2);
//		area_1.exclusiveOr(area_2);
		
		area_3.intersect(area_2);
		
		if (area_3.getPathIterator(null).isDone())
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAA");
		else
			System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOO");
		
		boolean flag = area_1.isPolygonal();
		String str = flag ? "true" : "false";
		System.out.println(str);
		
		gra_2D.fill(area_3);
		
//		PathIterator itor = area_1.getPathIterator(null);
//		double[] point = new double[6];
//		double x = 0.0, y = 0.0;
//		while (!itor.isDone()) {
//			switch (itor.currentSegment(point)) {
//			case PathIterator.SEG_MOVETO:
//				x = point[0];
//				y = point[1];
//				break;
//			case PathIterator.SEG_LINETO:
//				gra_2D.drawLine((int)x, (int)y, (int)point[0], (int)point[1]);
//				x = point[0];
//				y = point[1];
//				break;
//
//			default:
//				break;
//			}
//			itor.next();
//		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Expand Apexes");
		frame.setSize(new Dimension(1000, 800));
		AreaMethods test = new AreaMethods();
		frame.add(test);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
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
}
