package csu.geom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.conic.Circle2D;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

/**
 * A test class for <code>PolygonScaler</code>.
 * 
 * @author Appreciation - csu
 */
@SuppressWarnings("serial")
public class TestForPolygonScaler extends JPanel{
	// BasicStroke.CAP_BUTT = 0          BasicStroke.JOIN_BEVEL = 2
	private Stroke stroke = new BasicStroke(3.0f, 0, 2);
	private Stroke lineStroke = new BasicStroke(1.7f, 0, 2, 0, new float[]{9}, 0);
	
	private Color centerColor = Color.BLUE;
	private Color initialColor = Color.BLACK;
	private Color bigColor = Color.RED;
	private Color smallColor = Color.GREEN;
	
	private int center_X = 400;
	private int center_Y = 400;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gra_2D = (Graphics2D) g;
		gra_2D.setStroke(stroke);
		Polygon initialPolygon = makePolygon(10, center_X, center_Y, 100);
		gra_2D.setColor(initialColor);
		gra_2D.draw(initialPolygon);
		
		Polygon smallPolygon = PolygonScaler.scalePolygon(initialPolygon, 0.5);
		gra_2D.setColor(smallColor);
		gra_2D.draw(smallPolygon);
		
		Polygon bigPolygon = PolygonScaler.scalePolygon(initialPolygon, 1.5);
		gra_2D.setColor(bigColor);
		gra_2D.draw(bigPolygon);
		
		Circle2D circle2D = new Circle2D(center_X, center_Y, 5, true);
		gra_2D.setColor(centerColor);
		circle2D.fill(gra_2D);
		
		gra_2D.setStroke(lineStroke);
		for (int i = 0; i < bigPolygon.npoints; i++) {
			gra_2D.drawLine(center_X, center_Y, bigPolygon.xpoints[i], bigPolygon.ypoints[i]);
		}
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
	private Polygon makePolygon(int vertexsCount, double center_x, double center_y, double radius) {
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
		JFrame frame = new JFrame("Polygon Scaler");
		frame.setSize(new Dimension(800, 800));
		TestForPolygonScaler scaler = new TestForPolygonScaler();
		frame.add(scaler);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
