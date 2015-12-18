package csu.common.test.escape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Blockade;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;
import csu.util.Util;

@SuppressWarnings("serial")
public class ClearArea extends JPanel{
	private static final Color PASSABLE_EDGE_COLOR = Color.CYAN;
	private static final Color IMPASSABLE_EDGE_COLOR = Color.GREEN;
	private static final Color BLOCKADE_COLOR = Color.BLACK;
	
	private final int repairDistance = 10000;
	
	private ScreenTransform transform;
	
	private Map<Integer, CSUBlockade> csuBlockades = null;
	private List<CSURoad> csuRoads = null;
	
	private EscapeData escapeData;
	
	public void paintComponent(Graphics g) {
		Graphics2D gra_2D = (Graphics2D) g;
		
		this.escape_6(4);
		
		drawRoad(gra_2D, 350);
		drawBlockade(gra_2D, 350);
		
		//doClear(gra_2D, null, null, null);
	}
	
	public void escape_6(int mark) {
		switch (mark) {
		case 1:
			transform = new ScreenTransform(521851, 220000, 534597, 240000);
			break;
		case 2:
			transform = new ScreenTransform(707988, 211094, 784944, 258343);
			break;
		case 3:
			transform = new ScreenTransform(828162, 143612, 880150, 199012);
			break;
		case 4:
			transform = new ScreenTransform(645663, 720658, 695621, 746927);
			break;
		}
		transform.rescale(1000, 800);
		
		this.escapeData = new EscapeData_6();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		
		this.assignBlockadeToRoad();
	}
	
	private void drawRoad(Graphics2D gra_2D, int offset) {
		for (CSURoad road : this.csuRoads) {
			for (CSUEdge next : road.getCsuEdges()) {
				if (next.isPassable())
					gra_2D.setColor(PASSABLE_EDGE_COLOR);
				else
					gra_2D.setColor(IMPASSABLE_EDGE_COLOR);
				
				int x_1 = transform.xToScreen((int)next.getStart().getX()) - offset;
				int y_1 = transform.yToScreen((int)next.getStart().getY());
				int x_2 = transform.xToScreen((int)next.getEnd().getX()) - offset;
				int y_2 = transform.yToScreen((int)next.getEnd().getY());
				
				gra_2D.drawLine(x_1, y_1, x_2, y_2);
				// DrawingTools.drawArrowHeads(x_1, y_1, x_2, y_2, gra_2D);
			}
		}
	}
	
	public void drawBlockade(Graphics2D gra_2D, int offset) {
		gra_2D.setColor(BLOCKADE_COLOR);
		
		for (CSURoad road : this.csuRoads) {
			for (CSUBlockade next : road.getCsuBlockades()) {
				if (next == null)
					continue;
				
				int vertexCount = next.getPolygon().npoints;
				int[] x_coordinates = new int[vertexCount];
				int[] y_coordinates = new int[vertexCount];
				for (int i = 0; i < vertexCount; i++) {
					x_coordinates[i] = transform.xToScreen((double)next.getPolygon().xpoints[i]) - offset;
					y_coordinates[i] = transform.yToScreen((double)next.getPolygon().ypoints[i]);
				}
				gra_2D.draw(new Polygon(x_coordinates, y_coordinates, vertexCount));
			}
		}
	}
	
	private void assignBlockadeToRoad() {
		for (CSURoad next : csuRoads) {
			int[] blockadeList = escapeData.getBlockadeList(next.getId().getValue());
			List<CSUBlockade> blockades = new ArrayList<>();
			
			for (int i = 0; i < blockadeList.length; i++) {
				blockades.add(csuBlockades.get(new Integer(blockadeList[i])));
			}
			next.setCsuBlockades(blockades);
		}
	}
	
	
	public void doClear(Graphics2D gra_2D, Point2D selfL, CSURoad road, CSUEdge dir) {
		gra_2D.setColor(Color.RED);
		
		rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(selfL, dir.getMiddlePoint());
		rescuecore2.misc.geometry.Line2D[] temp = getParallelLine(line, 500);
		rescuecore2.misc.geometry.Line2D[] lines = {line, temp[0], temp[1]};
		
		Set<CSUBlockade> blockades = getBlockades(road, selfL, lines);
		
		double minDistance = Double.MAX_VALUE, distance;
		CSUBlockade nearest = null;
		for (CSUBlockade next : blockades) {
			//distance = Ruler.getDistance(next.getSelfBlockade().getX(), next.getSelfBlockade().getY(), x, y);
			distance = findDistanceTo(next.getSelfBlockade(), (int)selfL.getX(), (int)selfL.getY());
			if (distance < minDistance) {
				minDistance = distance;
				nearest = next;
			}
		}
		
		if (nearest != null) {
			
			double disToBlo = Ruler.getDistance(selfL, dir.getMiddlePoint());
			Vector2D v = dir.getMiddlePoint().minus(selfL);
			v = v.normalised().scale(Math.min(disToBlo, repairDistance - 50));
			
			rescuecore2.misc.geometry.Line2D t_li = new rescuecore2.misc.geometry.Line2D(selfL, v);
			rescuecore2.misc.geometry.Line2D[] a_li = getParallelLine(t_li, 500);
			
			gra_2D.drawLine(transform.xToScreen(t_li.getOrigin().getX()), 
					transform.yToScreen(t_li.getOrigin().getY()), 
					transform.xToScreen(t_li.getEndPoint().getX()), 
					transform.yToScreen(t_li.getEndPoint().getY()));
			gra_2D.drawLine(transform.xToScreen(a_li[0].getOrigin().getX()), 
					transform.yToScreen(a_li[0].getOrigin().getY()), 
					transform.xToScreen(a_li[0].getEndPoint().getX()), 
					transform.yToScreen(a_li[0].getEndPoint().getY()));
			gra_2D.drawLine(transform.xToScreen(a_li[1].getOrigin().getX()), 
					transform.yToScreen(a_li[1].getOrigin().getY()), 
					transform.xToScreen(a_li[1].getEndPoint().getX()), 
					transform.yToScreen(a_li[1].getEndPoint().getY()));
		}
	}

	private Set<CSUBlockade> getBlockades(CSURoad road, Point2D selfL, rescuecore2.misc.geometry.Line2D... li) {
		Set<CSUBlockade> results = new HashSet<CSUBlockade>();
		for (rescuecore2.misc.geometry.Line2D line : li) {
			for (CSUBlockade blockade : road.getCsuBlockades()) {
				if (hasIntersection(blockade.getPolygon(), line) 
						|| blockade.getPolygon().contains(selfL.getX(), selfL.getY())) {
					results.add(blockade);
				}
			}
		}
		
		return results;
	}
	
	private boolean hasIntersection(Polygon po, rescuecore2.misc.geometry.Line2D line) {
		List<rescuecore2.misc.geometry.Line2D> polyLines = getLines(po);
		for (rescuecore2.misc.geometry.Line2D ln : polyLines) {
			
			if (Util.isIntersect(ln, line))
				return true;
		}
		return false;
	}
	
	private rescuecore2.misc.geometry.Line2D[] getParallelLine(rescuecore2.misc.geometry.Line2D line, int rad) {
		double theta = Math.atan2(line.getEndPoint().getY() - line.getOrigin().getY(), 
				line.getEndPoint().getX() - line.getOrigin().getX());
		theta = theta - Math.PI / 2;
		while (theta > Math.PI || theta < -Math.PI) {
			if (theta > Math.PI)
				theta -= 2 * Math.PI;
			else
				theta += 2 * Math.PI;
		}
		int x = (int)(rad * Math.cos(theta)), y = (int)(rad * Math.sin(theta));
		
		Point2D line_1_s, line_1_e, line_2_s, line_2_e;
		line_1_s = new Point2D(line.getOrigin().getX() + x, line.getOrigin().getY() + y);
		line_1_e = new Point2D(line.getEndPoint().getX() + x, line.getEndPoint().getY() + y);
		line_2_s = new Point2D(line.getOrigin().getX() - x, line.getOrigin().getY() - y);
		line_2_e = new Point2D(line.getEndPoint().getX() - x, line.getEndPoint().getY() - y);
		
		rescuecore2.misc.geometry.Line2D[] result = {
				new rescuecore2.misc.geometry.Line2D(line_1_s, line_1_e),
				new rescuecore2.misc.geometry.Line2D(line_2_s, line_2_e),
		};
		
		return result;
	}
	
	private List<rescuecore2.misc.geometry.Line2D> getLines(Polygon polygon) {
		List<rescuecore2.misc.geometry.Line2D> lines = new ArrayList<>();
		int count = polygon.npoints;
		for (int i = 0; i < count; i++) {
			int j = (i + 1) % count;
			Point2D p1 = new Point2D(polygon.xpoints[i], polygon.ypoints[i]);
			Point2D p2 = new Point2D(polygon.xpoints[j], polygon.ypoints[j]);
			rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(p1, p2);
			lines.add(line);
		}
		return lines;
	}
	
	protected int findDistanceTo(Blockade b, int x, int y) {
		List<rescuecore2.misc.geometry.Line2D> lines = GeometryTools2D.pointsToLines(
						GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
		double best = Double.MAX_VALUE;
		Point2D origin = new Point2D(x, y);
		for (rescuecore2.misc.geometry.Line2D next : lines) {
			Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
			double d = GeometryTools2D.getDistance(origin, closest);
			if (d < best) {
				best = d;
			}
		}
		return (int) best;
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("EscapePoint");
		frame.setSize(1000, 800);
		JPanel panel = new ClearArea();
		frame.add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
