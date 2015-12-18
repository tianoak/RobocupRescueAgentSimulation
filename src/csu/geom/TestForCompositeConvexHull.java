package csu.geom;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.conic.Circle2D;

@SuppressWarnings("serial")
public class TestForCompositeConvexHull extends JPanel{
	private static Color POINT_COLOR = Color.GREEN;
	private static Color CURRENT_CONVEX_HULL_COLOR = Color.RED;
	private static Color OLD_CONVEX_HULL_COLOR = Color.BLUE;
	
	private static Stroke stroke = new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	
	private List<Point> points;
	private boolean paintConvexHull = false;
	
	private Polygon currentConvexHull;
	private Polygon oldConvexHull;
	
	public TestForCompositeConvexHull() {
		super();
		points = new ArrayList<>();
		this.addMouseListener(new MyMouseListener());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gra_2D = (Graphics2D) g;
		if (points != null && !points.isEmpty()) {
			for (Point next : points) {
				drawPoint(next, gra_2D);
			}
		}
		
		if (currentConvexHull != null && !paintConvexHull) {
			gra_2D.setColor(OLD_CONVEX_HULL_COLOR);
			oldConvexHull = currentConvexHull;
			drawConvexHull(oldConvexHull, gra_2D);
		}
		
		if (paintConvexHull) {
			gra_2D.setColor(CURRENT_CONVEX_HULL_COLOR);
			gra_2D.setStroke(stroke);
			
			if (currentConvexHull != null) {
				gra_2D.setColor(OLD_CONVEX_HULL_COLOR);
				oldConvexHull = currentConvexHull;
				drawConvexHull(oldConvexHull, gra_2D);
			}
			
			currentConvexHull = createConvexHull();
			gra_2D.setColor(CURRENT_CONVEX_HULL_COLOR);
			drawConvexHull(currentConvexHull, gra_2D);
		}
		paintConvexHull = false;
	}
	
	private void drawConvexHull(Polygon polygon, Graphics2D gra_2D) {
		int vertexCount = polygon.npoints;
		Point[] vertices = new Point[vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			vertices[i] = new Point(polygon.xpoints[i], polygon.ypoints[i]);
		}
		
		for (int i = 0; i < vertexCount; i++) {
			Point start_point = vertices[i];
			Point end_point = vertices[(i + 1) % vertexCount];
			gra_2D.drawLine(start_point.x, start_point.y, end_point.x, end_point.y);
		}
	}
	
	private Polygon createConvexHull() {
		CompositeConvexHull convexHull = new CompositeConvexHull();
		convexHull.updatePoints(points, null);
		return convexHull.getConvexPolygon();
	}
	
	public JPanel createControllPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JButton clearButton = new JButton("Clear");
		clearButton.setName("clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton sourceButton = (JButton)e.getSource();
				String sourceName = sourceButton.getName();
				if (sourceName == "clear") {
					currentConvexHull = null;
					oldConvexHull = null;
					points.clear();
					paintConvexHull = false;
					repaint();
				}
			}
		});
		
		JButton repaintButton = new JButton("Repaint");
		repaintButton.setName("repaint");
		repaintButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton sourceButton = (JButton)e.getSource();
				String sourceName = sourceButton.getName();
				if (sourceName == "repaint") {
					paintConvexHull = true;
					repaint();
				}
			}
		});
		panel.add(clearButton, BorderLayout.WEST);
		panel.add(repaintButton, BorderLayout.EAST);
		return panel;
	}
	
	private void drawPoint(Point point, Graphics2D gra_2D) {
		gra_2D.setColor(POINT_COLOR);
		gra_2D.setStroke(stroke);
		Circle2D circle2D = new Circle2D(point.getX(), point.getY(), 2, true);
		circle2D.fill(gra_2D);
		System.out.println("Draw a point at: (" + point.getX() + ", " + point.getY() + ")");
	}

	private class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			int clickCount = e.getClickCount();
			Point clickPoint = e.getPoint();
			Component clickComponent = e.getComponent();
			
			if (clickComponent.equals(TestForCompositeConvexHull.this) && clickCount == 1) {
				points.add(clickPoint);
				TestForCompositeConvexHull.this.repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Composite Convex Hull");
		frame.setSize(1000, 800);
		frame.setLayout(new BorderLayout());
		
		TestForCompositeConvexHull test = new TestForCompositeConvexHull();
		JPanel control = test.createControllPanel();
		
		frame.add(test, BorderLayout.CENTER);
		frame.add(control, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
