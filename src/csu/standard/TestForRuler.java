package csu.standard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

/**
 * Date: Mar 19, 2014  Time: 11:46pm
 * 
 * @author appreciation-csu
 *
 */
@SuppressWarnings("serial")
public class TestForRuler extends JPanel{
	private static final Color POLYGON_COLOR = Color.green;
	private static final Stroke POLYGON_STROKE = new BasicStroke(1.5f);
	
	public TestForRuler() {
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D gra_2D = (Graphics2D) g;
		gra_2D.setColor(POLYGON_COLOR);
		gra_2D.setStroke(POLYGON_STROKE);
		
		Polygon polygon_1 = makePolygon(4, 150, 150, 100);
		Polygon polygon_2 = makePolygon(4, 700, 150, 100);
		
		gra_2D.drawPolygon(polygon_1);
		gra_2D.drawPolygon(polygon_2);
		
		Double distance = Ruler.getDistance(polygon_1, polygon_2);
		gra_2D.setFont(new Font("", Font.PLAIN, 24));
		gra_2D.drawString(distance.toString(), 600, 600);
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
		JFrame frame = new JFrame("Test For Ruler");
		frame.setSize(1200, 800);
		frame.add(new TestForRuler());
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
