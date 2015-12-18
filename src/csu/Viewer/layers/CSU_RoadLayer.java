package csu.Viewer.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import javolution.util.FastMap;

import csu.Viewer.SelectedObject;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;
import csu.util.Util;

import rescuecore2.config.Config;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.view.Icons;
import rescuecore2.worldmodel.EntityID;

public class CSU_RoadLayer extends CSU_AreaLayer<Road> {
	private static final Color ROAD_EDGE_COLOUR = Color.GRAY.darker();
	private static final Color ROAD_SHAPE_COLOUR = new Color(185, 185, 185);
	private static final Color OPEN_PART_COLOR = Color.green;

	private static final Stroke WALL_STROKE = new BasicStroke(2,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	private static final Stroke ENTRANCE_STROKE = new BasicStroke(1,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	
	public static Map<EntityID, CSURoad> CSU_ROAD_MAP = 
			Collections.synchronizedMap(new FastMap<EntityID, CSURoad>());
	
	public static Point2D selfL = null;
	public static CSURoad road = null;
	public static CSUEdge dir = null;
	public static double repairDistance = 0;
	
	public static List<CSUEdge> openParts = Collections.synchronizedList(new ArrayList<CSUEdge>());
	
	private boolean showRoadIdsFlag;
	private Action showIdsAction;
	
	private boolean showOpenPartFlag;
	private Action showOpenPartAction;
	
	private boolean showBlockadeFlag;
	private Action showBlockadeAction;

	public CSU_RoadLayer() {
		super(Road.class);
	}

	@Override
	public String getName() {
		return "Roads";
	}

	@Override
	public Shape render(Road road, Graphics2D g, ScreenTransform t) {
		Shape shape = super.render(road, g, t);
		
		if (showRoadIdsFlag)
			drawInfo(road, g, String.valueOf(road.getID().getValue()), road.getClass());
		
		if (showOpenPartFlag)
			drawOpenPart(g, OPEN_PART_COLOR, t);
		
		if (showBlockadeFlag)
			drawBlockade(g);
		
		return shape;
	}
	
	@Override
	protected void paintEdge(Edge edge, Graphics2D g, ScreenTransform t) {
		g.setColor(ROAD_EDGE_COLOUR);
		g.setStroke(edge.isPassable() ? ENTRANCE_STROKE : WALL_STROKE);
		int x1 = t.xToScreen(edge.getStartX()), y1 = t.yToScreen(edge.getStartY());
		int x2 = t.xToScreen(edge.getEndX()), y2 = t.yToScreen(edge.getEndY());
		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	protected void paintShape(Road road, Polygon shape, Graphics2D g) {
		if (road == SelectedObject.selectedObject) {
			g.setColor(Color.MAGENTA);
		} else {
			g.setColor(ROAD_SHAPE_COLOUR);
		}
		g.fill(shape);
	}
	
	@Override
	public void initialise(Config config) {
		showRoadIdsFlag = false;
		showIdsAction = new showIdsAction();
		
		showOpenPartFlag = true;
		showOpenPartAction = new showOpenPartAction();
		
		showBlockadeFlag = false;
		showBlockadeAction = new showBlockadeAction();
	}
	
	@Override
    public java.util.List<JMenuItem> getPopupMenuItems() {
        java.util.List<JMenuItem> result = new ArrayList<JMenuItem>();
        result.add(new JMenuItem(showIdsAction));
        result.add(new JMenuItem(showOpenPartAction));
        result.add(new JMenuItem(showBlockadeAction));
        
        return result;
    }
	
	private void drawOpenPart(Graphics2D gra_2D, Color color, ScreenTransform t) {
		gra_2D.setColor(color);
		for (CSUEdge next : openParts) {
			int x1 = t.xToScreen(next.getOpenPartStart().getX());
			int y1 = t.yToScreen(next.getOpenPartStart().getY());
			int x2 = t.xToScreen(next.getOpenPartEnd().getX());
			int y2 = t.yToScreen(next.getOpenPartEnd().getY());
			gra_2D.drawLine(x1, y1, x2, y2);
		}
	}
	
	private void drawBlockade(Graphics2D gra_2D) {
		gra_2D.setColor(Color.black);
		gra_2D.setStroke(ENTRANCE_STROKE);
		for (CSURoad road : CSU_ROAD_MAP.values()) {
			for (CSUBlockade next : road.getCsuBlockades()) {
				int vertexCount = next.getPolygon().npoints;
				int[] x_coordinates = new int[vertexCount];
				int[] y_coordinates = new int[vertexCount];
				for (int i = 0; i < vertexCount; i++) {
					x_coordinates[i] = transform.xToScreen((double)next.getPolygon().xpoints[i]);
					y_coordinates[i] = transform.yToScreen((double)next.getPolygon().ypoints[i]);
				}
				gra_2D.draw(new Polygon(x_coordinates, y_coordinates, vertexCount));
			}
		}
		
		if (selfL != null && road != null && dir != null) {
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
	
	@SuppressWarnings("serial")
	private final class showIdsAction extends AbstractAction {
		public showIdsAction() {
			super("Show Ids");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showRoadIdsFlag));
			putValue(Action.SMALL_ICON, showRoadIdsFlag ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showRoadIdsFlag = ! showRoadIdsFlag;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showRoadIdsFlag));
			putValue(Action.SMALL_ICON, showRoadIdsFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class showOpenPartAction extends AbstractAction {
		public showOpenPartAction() {
			super("Show Open Part");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showOpenPartFlag));
			putValue(Action.SMALL_ICON, showOpenPartFlag ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showOpenPartFlag = ! showOpenPartFlag;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showOpenPartFlag));
			putValue(Action.SMALL_ICON, showOpenPartFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class showBlockadeAction extends AbstractAction {
		public showBlockadeAction() {
			super("Show Blockade");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showBlockadeFlag));
			putValue(Action.SMALL_ICON, showBlockadeFlag ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showBlockadeFlag = ! showBlockadeFlag;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showBlockadeFlag));
			putValue(Action.SMALL_ICON, showBlockadeFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
}
