package csu.geom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import csu.util.Util;

import math.geom2d.conic.Circle2D;

import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.worldmodel.EntityID;

/**
 * A test class for <code>ExpandApexes</code>
 * 
 * @author Appreciation - csu
 */
@SuppressWarnings("serial")
public class TestForExpandApexes extends JPanel{
	private Color initialColor = Color.black;
	private Color expandColor = Color.green;
	private Color centerColor = Color.cyan;
	
	// BasicStroke.CAP_BUTT = 0          BasicStroke.JOIN_BEVEL = 2
	private Stroke stroke = new BasicStroke(3.0f, 0, 2); 
	private Stroke lineStroke = new BasicStroke(1.7f, 0, 2, 0, new float[]{9}, 0);
	
	private int center_X = 350;
	private int center_y = 350;
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gra_2D = (Graphics2D)  g;
		gra_2D.setStroke(stroke);
		
		Polygon initialPolygon = makePolygon(6, center_X, center_y, 100);
		
		
		Line2D line = new Line2D(new Point2D(100, 100), new Point2D(500, 400));
		gra_2D.drawLine(100, 100, 500, 400);
		Set<Point2D> inters = Util.getIntersections(initialPolygon, line);
		
		gra_2D.setColor(initialColor);
		gra_2D.draw(initialPolygon);
		
		Blockade blockade = makeBlockade(initialPolygon, center_X, center_y);
		Polygon expandBlockade = (Polygon)ExpandApexes.expandApexes(blockade, 10);
		gra_2D.setColor(expandColor);
		gra_2D.draw(expandBlockade);
		
		Circle2D circle2D = new Circle2D(center_X, center_y, 5, true);
		gra_2D.setColor(centerColor);
		circle2D.fill(gra_2D);
		
		gra_2D.setStroke(lineStroke);
		for (int i = 0; i < expandBlockade.npoints; i++) {
			gra_2D.drawLine(center_X, center_y, expandBlockade.xpoints[i], expandBlockade.ypoints[i]);
		}
		
		gra_2D.setColor(Color.red);
		for (Point2D next : inters) {
			gra_2D.fillOval((int)next.getX() - 5, (int)next.getY() - 5, 10, 10);
		}
	}
	
	/**
	 * Create a blockade with given shape and center point.
	 * 
	 * @param polygon
	 *            the shape of this polygon
	 * @param center_x
	 *            the x coordinate of center point
	 * @param center_y
	 *            the y coordinate of center point
	 * @return a blockade
	 */
	private Blockade makeBlockade(Polygon polygon, int center_x, int center_y) {
		Blockade blockade = new Blockade(new EntityID(1));
		int count = polygon.npoints;
		int[] apexes = new int[count * 2];
		
		for (int i = 0; i < count; i++) {
			apexes[i * 2] = polygon.xpoints[i];
			apexes[i * 2 + 1] = polygon.ypoints[i];
		}
		blockade.setApexes(apexes);
		blockade.setX(center_x);
		blockade.setY(center_y);
		
		return blockade;
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
		JFrame frame = new JFrame("Expand Apexes");
		frame.setSize(new Dimension(1000, 800));
		TestForExpandApexes test = new TestForExpandApexes();
		frame.add(test);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
