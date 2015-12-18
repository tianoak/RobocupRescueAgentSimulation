package csu.common.test.escape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import csu.geom.ExpandApexes;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSUEscapePoint;
import csu.model.object.CSURoad;
import csu.standard.Ruler;
import csu.util.Util;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ScreenTransform;

@SuppressWarnings("serial")
public class EscapePoint extends JPanel{
	private static final Color PASSABLE_EDGE_COLOR = Color.CYAN;
	private static final Color IMPASSABLE_EDGE_COLOR = Color.GREEN;
	private static final Color BLOCKADE_COLOR = Color.BLACK;
	private static final Color ESCAPE_POINT_COLOR = Color.RED;
	private static final Stroke STROKE = new BasicStroke(1.5f);
	
	private static double CLEAR_WIDTH = 3000d; // 3m = 3000mm
	
	private ScreenTransform transform;
	
	private Map<Integer, CSUBlockade> csuBlockades = null;
	private List<CSURoad> csuRoads = null;
	
	private EscapeData escapeData;
	
	@Override
	public void paintComponent(Graphics graph) {
		Graphics2D gra_2D = (Graphics2D) graph;
		this.init(gra_2D);
		
		System.out.println();
		
		this.drawRoad(gra_2D, 350);
		this.drawBlockade(gra_2D, 350);
		this.drawEscapePoint(gra_2D, 350);
	}
	
	private void init(Graphics2D gra_2D) {
		gra_2D.setStroke(STROKE);
		gra_2D.setFont(new Font("", Font.PLAIN, 24));
		
		// TODO
		this.escape_3(gra_2D);
	}
	
	public void escape_1(Graphics2D gra_2D) {
		transform = new ScreenTransform(100000, 10000, 150000, 100000);
		transform.rescale(1000, 800);
		
		escapeData = new EscapeData_1();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		this.assignBlockadeToRoad();
	}
	
	public void escape_2(Graphics2D gra_2D) {
		transform = new ScreenTransform(635000, 1200000, 735000, 1270000);
		transform.rescale(1000, 800);
		
		escapeData = new EscapeData_2();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		this.assignBlockadeToRoad();
	}
	
	public void escape_3(Graphics2D gra_2D) {
		transform = new ScreenTransform(1090000, 518000, 1190000, 630000);
		transform.rescale(1000, 800);
		
		escapeData = new EscapeData_3();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		this.assignBlockadeToRoad();
	}
	
	public void escape_4(Graphics2D gra_2D) {
		transform = new ScreenTransform(383150, 540000, 615214, 543000);
		transform.rescale(1000, 800);
		
		escapeData = new EscapeData_4();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		this.assignBlockadeToRoad();
	}
	
	public void escape_5(Graphics2D gra_2D) {
		transform = new ScreenTransform(601254, 571846, 804398, 688535);
		transform.rescale(1000, 800);
		
		escapeData = new EscapeData_5();
		this.csuBlockades = escapeData.blockadeList();
		this.csuRoads = escapeData.roadList();
		this.assignBlockadeToRoad();
	}
	
	private void drawRoad(Graphics2D gra_2D, int offset) {
		for (CSURoad road : this.csuRoads) {
			// , 320, 15682, 5181, 15691, 4342, 15736, 281, 14044, 7559, 4053,
			String str = isRoadPassable(road) ? "true" : "false";
			System.out.println(road.getId().getValue() + " " + str + "");
//			if (road.getId().getValue() != 17014)
//				continue;
			
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
			
			gra_2D.setColor(Color.red);
			
			Area area = getPfClearArea(road);
			if (area == null)
				continue;
			PathIterator itor = area.getPathIterator(null);
			double[] point = new double[6];
			double x = 0.0, y = 0.0;
			while (!itor.isDone()) {
				switch (itor.currentSegment(point)) {
				case PathIterator.SEG_MOVETO:
					x = point[0];
					y = point[1];
					break;
				case PathIterator.SEG_LINETO:
					gra_2D.drawLine(transform.xToScreen((int)x) - offset, transform.yToScreen((int)y), 
							transform.xToScreen((int)point[0]) - offset, transform.yToScreen((int)point[1]));
					x = point[0];
					y = point[1];
					break;
	
				default:
					break;
				}
				itor.next();
			}
			
			
//			Pair<Line2D, Line2D> lines = getPfClearLine(road);
//			if (lines == null)
//				continue;
//			int x_1 = transform.xToScreen((int)lines.first().getOrigin().getX()) - offset;
//			int y_1 = transform.yToScreen((int)lines.first().getOrigin().getY());
//			int x_2 = transform.xToScreen((int)lines.first().getEndPoint().getX()) - offset;
//			int y_2 = transform.yToScreen((int)lines.first().getEndPoint().getY());
//			
//			gra_2D.drawLine(x_1, y_1, x_2, y_2);
//			System.out.println(getLength(lines.first())/1000 + "m");
//			
//			x_1 = transform.xToScreen((int)lines.second().getOrigin().getX()) - offset;
//			y_1 = transform.yToScreen((int)lines.second().getOrigin().getY());
//			x_2 = transform.xToScreen((int)lines.second().getEndPoint().getX()) - offset;
//			y_2 = transform.yToScreen((int)lines.second().getEndPoint().getY());
//			
//			gra_2D.drawLine(x_1, y_1, x_2, y_2);
//			System.out.println(getLength(lines.second()) / 1000 + "m");
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
	
	public void drawEscapePoint(Graphics2D gra_2D, int offset) {
		for (CSURoad road : this.csuRoads) {
			gra_2D.setColor(ESCAPE_POINT_COLOR);
			List<CSUEscapePoint> escape = getEscapePoint(road);
			
			for(CSUEscapePoint next : escape) {
				int x = transform.xToScreen(next.getUnderlyingPoint().getX()) - offset;
				int y = transform.yToScreen(next.getUnderlyingPoint().getY());

				gra_2D.setColor(ESCAPE_POINT_COLOR);
				gra_2D.fillOval(x - 2, y - 2, 4, 4);
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
	
	public Area getPfClearArea(CSURoad road) {
		if (road.getCsuEdges().size() != 4)
			return null;
		if (road.isAllEdgePassable())
			return null;
		
		CSUEdge edge_1 = road.getCsuEdges().get(0);
		CSUEdge edge_2 = road.getCsuEdges().get(1);
		CSUEdge edge_3 = road.getCsuEdges().get(2);
		CSUEdge edge_4 = road.getCsuEdges().get(3);
		
		Polygon area = new Polygon();
		
		Line2D line_1 = null, line_2 = null;
		
		if (edge_1.isPassable() && edge_3.isPassable()) {
			Point2D perpendicular_1, perpendicular_2;
			
			Pair<Double, Boolean> dis = ptSegDistSq(edge_2.getLine(), edge_1.getStart());
			if (!dis.second().booleanValue()) { // the point is out the range of this line
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_4.getLine(), edge_1.getEnd());
				line_1 = new Line2D(perpendicular_1, edge_1.getEnd());
			} else { // the point is within the range of this line
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_2.getLine(), edge_1.getStart());
				line_1 = new Line2D(edge_1.getStart(), perpendicular_1);
			}
			
			dis = ptSegDistSq(edge_4.getLine(), edge_3.getStart());
			if (!dis.second().booleanValue()) {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_2.getLine(), edge_3.getEnd());
				line_2 = new Line2D(edge_3.getEnd(), perpendicular_2);
			} else {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_4.getLine(), edge_3.getStart());
				line_2 = new Line2D(perpendicular_2, edge_3.getStart());
			}
		} else if (edge_2.isPassable() && edge_4.isPassable()) {
			Point2D perpendicular_1, perpendicular_2;
			
			Pair<Double, Boolean> dis = ptSegDistSq(edge_3.getLine(), edge_2.getStart());
			if (!dis.second().booleanValue()) {
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_1.getLine(), edge_2.getEnd());
				line_1 = new Line2D(perpendicular_1, edge_2.getEnd());
			} else {
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_3.getLine(), edge_2.getStart());
				line_1 = new Line2D(edge_2.getStart(), perpendicular_1);
			}
			
			dis = ptSegDistSq(edge_1.getLine(), edge_4.getStart());
			if (!dis.second().booleanValue()) {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_3.getLine(), edge_4.getEnd());
				line_2 = new Line2D(edge_4.getEnd(), perpendicular_2);
			} else {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_1.getLine(), edge_4.getStart());
				line_2 = new Line2D(perpendicular_2, edge_4.getStart());
			}
		}
		
		double rate_1 = CLEAR_WIDTH / getLength(line_1);
		double rate_2 = CLEAR_WIDTH / getLength(line_2);
		Point2D mid_1 = getMiddle(line_1), mid_2 = getMiddle(line_2);
		
		Point2D end_1 = (new Line2D(mid_1, line_1.getOrigin())).getPoint(rate_1);
		Point2D end_2 = (new Line2D(mid_2, line_2.getOrigin())).getPoint(rate_2);
		area.addPoint((int)end_1.getX(), (int)end_1.getY());
		area.addPoint((int)end_2.getX(), (int)end_2.getY());
		
		end_1 = (new Line2D(mid_1, line_1.getEndPoint())).getPoint(rate_1);
		end_2 = (new Line2D(mid_2, line_2.getEndPoint())).getPoint(rate_2);
		
		area.addPoint((int)end_2.getX(), (int)end_2.getY());
		area.addPoint((int)end_1.getX(), (int)end_1.getY());
		
		return new Area(area);
	}
	
	public Pair<Line2D, Line2D> getPfClearLine(CSURoad road) {
		
		if (road.getCsuEdges().size() != 4)
			return null;
		if (road.isAllEdgePassable())
			return null;
		
		CSUEdge edge_1 = road.getCsuEdges().get(0);
		CSUEdge edge_2 = road.getCsuEdges().get(1);
		CSUEdge edge_3 = road.getCsuEdges().get(2);
		CSUEdge edge_4 = road.getCsuEdges().get(3);
		
		Line2D line_1 = null, line_2 = null, line_3 = null, line_4 = null;
		
		if (edge_1.isPassable() && edge_3.isPassable()) {
			Point2D perpendicular_1, perpendicular_2;
			
			Pair<Double, Boolean> dis = ptSegDistSq(edge_2.getLine(), edge_1.getStart());
			if (dis.second().booleanValue()) { // the point is out the range of this line
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_4.getLine(), edge_1.getEnd());
				line_1 = new Line2D(perpendicular_1, edge_1.getEnd());
			} else { // the point is within the range of this line
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_2.getLine(), edge_1.getStart());
				line_1 = new Line2D(edge_1.getStart(), perpendicular_1);
			}
			
			dis = ptSegDistSq(edge_4.getLine(), edge_3.getStart());
			if (dis.second().booleanValue()) {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_2.getLine(), edge_3.getEnd());
				line_2 = new Line2D(edge_3.getEnd(), perpendicular_2);
			} else {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_4.getLine(), edge_3.getStart());
				line_2 = new Line2D(perpendicular_2, edge_3.getStart());
			}
		} else if (edge_2.isPassable() && edge_4.isPassable()) {
			Point2D perpendicular_1, perpendicular_2;
			
			Pair<Double, Boolean> dis = ptSegDistSq(edge_3.getLine(), edge_2.getStart());
			if (dis.second().booleanValue()) {
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_1.getLine(), edge_2.getEnd());
				line_1 = new Line2D(perpendicular_1, edge_2.getEnd());
			} else {
				perpendicular_1 = GeometryTools2D.getClosestPoint(edge_3.getLine(), edge_2.getStart());
				line_1 = new Line2D(edge_2.getStart(), perpendicular_1);
			}
			
			dis = ptSegDistSq(edge_1.getLine(), edge_4.getStart());
			if (dis.second().booleanValue()) {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_3.getLine(), edge_4.getEnd());
				line_2 = new Line2D(edge_4.getEnd(), perpendicular_2);
			} else {
				perpendicular_2 = GeometryTools2D.getClosestPoint(edge_1.getLine(), edge_4.getStart());
				line_2 = new Line2D(perpendicular_2, edge_4.getStart());
			}
		}
		
		double rate_1 = CLEAR_WIDTH / getLength(line_1);
		double rate_2 = CLEAR_WIDTH / getLength(line_2);
		Point2D mid_1 = getMiddle(line_1), mid_2 = getMiddle(line_2);
		
		Point2D end_1 = (new Line2D(mid_1, line_1.getOrigin())).getPoint(rate_1);
		Point2D end_2 = (new Line2D(mid_2, line_2.getOrigin())).getPoint(rate_2);
		line_3 = new Line2D(end_1, end_2);
		
		end_1 = (new Line2D(mid_1, line_1.getEndPoint())).getPoint(rate_1);
		end_2 = (new Line2D(mid_2, line_2.getEndPoint())).getPoint(rate_2);
		line_4 = new Line2D(end_1, end_2);
		
		return new Pair<Line2D, Line2D>(line_3, line_4);
	}
	
	
	public boolean isRoadPassable(CSURoad road) {
		
		if (road.isAllEdgePassable())
			return true;
		
		List<CSUBlockade> blockades = new LinkedList<>(road.getCsuBlockades());
		
		for (CSUEscapePoint next : getEscapePoint(road)) {
			blockades.removeAll(next.getRelateBlockade());
		}
		
		if (blockades.isEmpty())
			return true;
		
		return false;
	}
	
	private List<CSUEscapePoint> getEscapePoint(CSURoad road) {
		List<CSUEscapePoint> m_p_points = new ArrayList<>();
		
		for (CSUBlockade next : road.getCsuBlockades()) {
			if (next == null)
				continue;
			Polygon expan = next.getPolygon();
			
			for(CSUEdge csuEdge : road.getCsuEdges()) {
				CSUEscapePoint p = findPoints(csuEdge, expan, next);
				if (p == null) {
					continue;
				} else {
					m_p_points.add(p);
				}
			}
		}
		
		filter(road, m_p_points);
		return m_p_points;
	}
	
	private CSUEscapePoint findPoints(CSUEdge csuEdge, Polygon expan, CSUBlockade next) {
		if (csuEdge.isPassable()) {
			// do nothing
		} else {
			if (hasIntersection(expan, csuEdge.getLine())) {
				return null;
			}
			double minDistance = Double.MAX_VALUE, distance;
			Pair<Integer, Integer> minDistanceVertex = null;
			
			for (Pair<Integer, Integer> vertex : next.getVertexesList()) {
				
				Pair<Double, Boolean> dis = ptSegDistSq(csuEdge.getStart().getX(), 
						csuEdge.getStart().getY(), csuEdge.getEnd().getX(), 
						csuEdge.getEnd().getY(), vertex.first(), vertex.second());
				
				if (dis.second().booleanValue())
					continue;
				distance = dis.first().doubleValue();
				
				if (distance < minDistance) {
					minDistance = distance;
					minDistanceVertex = vertex;
				}
			}
			
			if (minDistanceVertex == null)
				return null;
			
			Point2D perpendicular = GeometryTools2D.getClosestPoint(csuEdge.getLine(), 
					new Point2D(minDistanceVertex.first(), minDistanceVertex.second()));
			
			Point middlePoint = getMiddle(minDistanceVertex, perpendicular);
			
			Point2D vertex = new Point2D(minDistanceVertex.first(), minDistanceVertex.second());
			Point2D perpenPoint = new Point2D(perpendicular.getX(), perpendicular.getY());
			
			Line2D lin = new Line2D(vertex, perpenPoint);
			
			return new CSUEscapePoint(middlePoint, lin, next);
		}
		
		return null;
	}
	
	private void filter(CSURoad road, List<CSUEscapePoint> m_p_points) {
		Mark:for (Iterator<CSUEscapePoint> itor = m_p_points.iterator(); itor.hasNext(); ) {
			
			CSUEscapePoint m_p = itor.next();
			// TODO
			for (CSUEdge edge : road.getCsuEdges()) {
				if (edge.isPassable())
					continue;
				if (contains(edge.getLine(), m_p.getUnderlyingPoint(), 100)) {
					itor.remove();
					continue Mark;
				}
			}
			
			for (CSUBlockade blockade : road.getCsuBlockades()) {
				if (blockade == null)
					continue;
				Polygon polygon = blockade.getPolygon();
				Polygon po = ExpandApexes.expandApexes(blockade.getSelfBlockade(), 200);
				
				
				if (po.contains(m_p.getLine().getEndPoint().getX(), m_p.getLine().getEndPoint().getY())) {
					
					Set<Point2D> intersections = Util.getIntersections(polygon, m_p.getLine());
					
					double minDistance = Double.MAX_VALUE, distance;
					Point2D closest = null;
					boolean shouldRemove = false;
					for (Point2D inter : intersections) {
						distance = Ruler.getDistance(m_p.getLine().getOrigin(), inter);
						
						if (distance > 450 && distance < minDistance) {
							minDistance = distance;
							closest = inter;
						}
						shouldRemove = true;
					}
					
					if (closest != null) {
						Point p = getMiddle(m_p.getLine().getOrigin(), closest);
						m_p.getUnderlyingPoint().setLocation(p);
						m_p.addCsuBlockade(blockade);
					} else if (shouldRemove){
						itor.remove();
						continue Mark;
					}
				}
				
				if (po.contains(m_p.getUnderlyingPoint())) {
					itor.remove();
					continue Mark;
				}
			}
		}
	}
	
	private boolean contains(Line2D line, Point point, double threshold) {

		double pos = java.awt.geom.Line2D.ptSegDist(line.getOrigin().getX(),
				line.getOrigin().getY(), line.getEndPoint().getX(), line
						.getEndPoint().getY(), point.getX(), point.getY());
		if (pos <= threshold)
			return true;

		return false;
	}
	
	private Pair<Double, Boolean> ptSegDistSq(Line2D line,Point2D point) {
		return ptSegDistSq((int)line.getOrigin().getX(), (int)line.getOrigin().getY(), 
				(int)line.getEndPoint().getX(), (int)line.getEndPoint().getY(), 
				(int)point.getX(), (int)point.getY());
	}
	
	private Pair<Double, Boolean> ptSegDistSq(double x1, double y1, double x2,
			double y2, double px, double py) {

		x2 -= x1;
		y2 -= y1;

		px -= x1;
		py -= y1;

		double dotprod = px * x2 + py * y2;

		double projlenSq;

		if (dotprod <= 0) {
			projlenSq = 0;
		} else {
			px = x2 - px;
			py = y2 - py;
			dotprod = px * x2 + py * y2;

			if (dotprod <= 0.0) {
				projlenSq = 0.0;
			} else {
				projlenSq = dotprod * dotprod / (x2 * x2 + y2 * y2);
			}
		}
		
		double lenSq = px * px + py * py - projlenSq;

		if (lenSq < 0)
			lenSq = 0;
		
		if (projlenSq == 0) {
			// the target point out of this line
			return new Pair<Double, Boolean>(Math.sqrt(lenSq), true);
		} else {
			// the target point within this line
			return new Pair<Double, Boolean>(Math.sqrt(lenSq), false);
		}
	}
	
//	public boolean hasIntersection(Polygon polygon, rescuecore2.misc.geometry.Line2D line) {
//		List<rescuecore2.misc.geometry.Line2D> polyLines = getLines(polygon);
//		for (rescuecore2.misc.geometry.Line2D ln : polyLines) {
//			Point2D p = GeometryTools2D.getSegmentIntersectionPoint(ln, line);
//			if (p != null)
//				return true;
//		}
//		return false;
//	}
	
	public boolean hasIntersection(Polygon polygon, rescuecore2.misc.geometry.Line2D line) {
		List<rescuecore2.misc.geometry.Line2D> polyLines = getLines(polygon);
		for (rescuecore2.misc.geometry.Line2D ln : polyLines) {

			math.geom2d.line.Line2D line_1 = new math.geom2d.line.Line2D(
					line.getOrigin().getX(), line.getOrigin().getY(), 
					line.getEndPoint().getX(), line.getEndPoint().getY());

			math.geom2d.line.Line2D line_2 = new math.geom2d.line.Line2D(
					ln.getOrigin().getX(), ln.getOrigin().getY(),
					ln.getOrigin().getX(), ln.getOrigin().getY());

			if (math.geom2d.line.Line2D.intersects(line_1, line_2)) {

				return true;
			}
		}
		return false;
	}
	
	private List<rescuecore2.misc.geometry.Line2D> getLines(Polygon polygon) {
		List<rescuecore2.misc.geometry.Line2D> lines = new ArrayList<>();
		int count = polygon.npoints;
		for (int i = 0; i < count; i++) {
			int j = (i + 1) % count;
			rescuecore2.misc.geometry.Point2D p1 = new rescuecore2.misc.geometry.Point2D(polygon.xpoints[i], polygon.ypoints[i]);
			rescuecore2.misc.geometry.Point2D p2 = new rescuecore2.misc.geometry.Point2D(polygon.xpoints[j], polygon.ypoints[j]);
			rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(p1, p2);
			lines.add(line);
		}
		return lines;
	}
	
	private Point getMiddle(Pair<Integer, Integer> first, Point2D second) {
		int x = first.first() + (int)second.getX();
		int y = first.second() + (int)second.getY();
		
		return new Point(x / 2, y / 2);
	}
	
	private Point getMiddle(Point2D first, Point2D second) {
		int x = (int)(first.getX() + second.getX());
		int y = (int)(first.getY() + second.getY());
		
		return new Point(x / 2, y / 2);
	}
	
	private Point2D getMiddle(Line2D line) {
		double x = line.getOrigin().getX() + line.getEndPoint().getX();
		double y = line.getOrigin().getY() + line.getEndPoint().getY();
		
		return new Point2D(x / 2, y / 2);
	}
	
	private int getLength(Line2D line) {
		return (int)Ruler.getDistance(line.getOrigin(), line.getEndPoint());
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("EscapePoint");
		frame.setSize(1000, 800);
		JPanel panel = new EscapePoint();
		frame.add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
