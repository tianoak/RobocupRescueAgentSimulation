package csu.model.object;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import csu.Viewer.layers.CSU_RoadLayer;
import csu.geom.ExpandApexes;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.standard.Ruler;
import csu.util.Util;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Mainly for blockades. 
 * 
 * Date: May 31, 2014  Time: 7:50pm
 * 
 * @author appreciation-csu
 *
 */
public class CSURoad {
	private double CLEAR_WIDTH; // 3m = 3000mm

	private Road selfRoad;
	private EntityID selfId; 
	private AdvancedWorldModel world;
	
	private CsuLineOfSightPerception lineOfSightPerception;
	private List<EntityID> observableAreas;
	
	private List<CSUEdge> csuEdges;
	private List<CSUBlockade> csuBlockades = new ArrayList<>();
	
	private Pair<Line2D, Line2D> pfClearLines = null;
	private Area pfClearArea = null;
	
	/**
	 * When {@link CSURoad#pfClearLines} is null, the roadCenterLine is null, too.
	 */
	private Line2D roadCenterLine = null;
	
	private boolean isEntrance = false;
	private boolean isRoadCenterBlocked = false;
	
	// constructor
	public CSURoad(Road road, AdvancedWorldModel world) {
		this.world = world;
		this.selfRoad = road;
		this.selfId = road.getID();
		this.lineOfSightPerception = new CsuLineOfSightPerception(world);
		this.csuEdges = createCsuEdges();
		
		this.CLEAR_WIDTH = world.getConfig().repairRad;
	}
	
	// constructor, only for test
	public CSURoad(EntityID roadId, List<CSUEdge> edges) {
		this.selfId = roadId;
		this.csuEdges = edges;
	}
	
	/**
	 * Update the blockade inform.
	 */
	public void update() {
		for (CSUEdge next : csuEdges) {
			next.setOpenPart(next.getLine());
			next.setBlocked(false);
		}
		
		this.csuBlockades.clear();
		
		if (selfRoad.isBlockadesDefined()) {
			for (CSUEdge next : csuEdges) {
				if (!next.isPassable())
					continue;
				// TODO July 9, 2014  Time: 2:57pm
				setCsuEdgeOpenPart(next);
			}
			
			this.csuBlockades = createCsuBlockade();
		}
	}
	
	/**
	 * Create the CSUEdge objects for this road
	 * 
	 * @return a list of CSUEdges
	 */
	private List<CSUEdge> createCsuEdges() {
		List<CSUEdge> result = new ArrayList<>();

		for (Edge next : selfRoad.getEdges()) {
			result.add(new CSUEdge(world, next, selfRoad.getID()));
		}

		return result;
	}

	/**
	 * Create the CSUBlockade objects for this road.
	 * 
	 * @return a list of CSUBlockades
	 */
	private List<CSUBlockade> createCsuBlockade() {
		List<CSUBlockade> result = new ArrayList<>();
		if (!selfRoad.isBlockadesDefined())
			return result;
		for (EntityID next : selfRoad.getBlockades()) {
			StandardEntity entity = world.getEntity(next);
			if (entity == null)
				continue;
			if (!(entity instanceof Blockade))
				continue;
			Blockade bloc = (Blockade) entity;
			if (!bloc.isApexesDefined())
				continue;
			if (bloc.getApexes().length < 6)
				continue;
			result.add(new CSUBlockade(next, world));
		}

		return result;
	}
	
//	TODO July 9, 2014  Time: 2:56pm
	/**
	 * Find out the open part of passable CSUEdges
	 * 
	 * @param edge
	 *            the target passable CSUEdge
	 */
	private void setCsuEdgeOpenPart(CSUEdge edge) {
		Polygon expand = null;
		boolean isStartBlocked = false, isEndBlocked = false;
		
		Point2D openPartStart = null, openPartEnd = null;
		
		for (CSUBlockade next : csuBlockades) {
			if (next.getPolygon().contains(selfRoad.getX(), selfRoad.getY()))
				isRoadCenterBlocked = true;
			
			expand = ExpandApexes.expandApexes(next.getSelfBlockade(), 10);
			
			if (expand.contains(edge.getStart().getX(), edge.getStart().getY())) {
				isStartBlocked = true;
			} else if (expand.contains(edge.getEnd().getX(), edge.getEnd().getY())) {
				isEndBlocked = true;
			}
			
			if (isStartBlocked && isEndBlocked)
				continue;

			Set<Point2D> intersections = Util.getIntersections(expand, edge.getLine());

			if (isStartBlocked) {
				double minDistance = Double.MAX_VALUE, distance;
				openPartEnd = edge.getEnd();
				for (Point2D point : intersections) {
					distance = distance(point, openPartEnd);
					if (distance < minDistance) {
						minDistance = distance;
						openPartStart = point;
					}
				}
			} else if (isEndBlocked) {
				double minDistance = Double.MAX_VALUE, distance;
				openPartStart = edge.getStart();
				for (Point2D point : intersections) {
					distance = distance(point, openPartStart);
					if (distance < minDistance) {
						minDistance = distance;
						openPartEnd = point;
					}
				}
			}

			if (openPartStart == null || openPartEnd == null || distance(openPartStart, openPartEnd) < 200) {
				edge.setBlocked(true);
				edge.setOpenPart(null);
				break;
			} else {
				edge.setOpenPart(openPartStart, openPartEnd);
				
				if (AgentConstants.LAUNCH_VIEWER) {
					CSU_RoadLayer.openParts.add(edge);
				}
			}
		}
	}

	public Road getSelfRoad() {
		return selfRoad;
	}
	
	public EntityID getId() {
		return this.selfId;
	}

	public List<EntityID> getObservableAreas() {
		if (observableAreas == null || observableAreas.isEmpty()) {
			observableAreas = lineOfSightPerception.getVisibleAreas(getId());
		}
		return observableAreas;
	}
	
	public CSUEdge getCsuEdgeInPoint(Point2D middlePoint) {
		for (CSUEdge next : csuEdges) {
			if (contains(next.getLine(), middlePoint, 1.0))
				return next;
		}
		
		return null;
	}
	
	/**
	 * For entrance only.
	 * 
	 * @return true when this entrance is need to clear.
	 * 140824 true -- dont need ,false -- need
	 */
	public boolean isNeedlessToClear() {
		double buildingEntranceLength = 0.0;
		double maxUnpassableEdgeLength = Double.MIN_VALUE;
		double length;
		
		Edge buildingEntrance = null;
		
		//building的边
		for (Edge next : selfRoad.getEdges()) {
			//可以通过的边
			if (next.isPassable()) {
				StandardEntity entity = world.getEntity(next.getNeighbour());
				if (entity instanceof Building) {
					buildingEntranceLength = distance(next.getStart(), next.getEnd());
					buildingEntrance = next;
				}
			} else {
				length = distance(next.getStart(), next.getEnd());
				if (length > maxUnpassableEdgeLength) {
					maxUnpassableEdgeLength = length;
				}
			}
		}
		
		if (buildingEntrance == null)
			return true;
		double rad = buildingEntranceLength + maxUnpassableEdgeLength;
		Area entranceArea = entranceArea(buildingEntrance.getLine(), rad);
		
		Set<EntityID> blockadeIds = new HashSet<>();
		
		if (selfRoad.isBlockadesDefined()) {
			blockadeIds.addAll(selfRoad.getBlockades());
		}
		
		for (EntityID next : selfRoad.getNeighbours()) {
			StandardEntity entity = world.getEntity(next);
			if (entity instanceof Road) {
				Road road = (Road) entity;
				if (road.isBlockadesDefined()) 
					blockadeIds.addAll(road.getBlockades());
			}
		}
		
		for (EntityID next : blockadeIds) {
			StandardEntity entity = world.getEntity(next);
			if (entity == null)
				continue;
			if (!(entity instanceof Blockade))
				continue;
			Blockade blockade = (Blockade)entity;
			if (!blockade.isApexesDefined())
				continue;
			//？？
			if (blockade.getApexes().length < 6)
				continue;
			Polygon po = Util.getPolygon(blockade.getApexes());
			Area blocArea = new Area(po);
			blocArea.intersect(entranceArea);
			//??
			if (!blocArea.getPathIterator(null).isDone())
				return false;
		}
		return true;
	}
	
	private Area entranceArea(Line2D line, double rad) {
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
		
		Polygon polygon = new Polygon();
		polygon.addPoint((int)(line.getOrigin().getX() + x), (int)(line.getOrigin().getY() + y));
		polygon.addPoint((int)(line.getEndPoint().getX() + x), (int)(line.getEndPoint().getY() + y));
		polygon.addPoint((int)(line.getEndPoint().getX() - x), (int)(line.getEndPoint().getY() - y));
		polygon.addPoint((int)(line.getOrigin().getX() - x), (int)(line.getOrigin().getY() - y));
		
		return new Area(polygon);
	}
	
	public List<CSUEdge> getCsuEdgeTo(EntityID neighbourId) {
		List<CSUEdge> result = new ArrayList<>();
		
		for (CSUEdge next : csuEdges) {
			if (next.isPassable() && next.getNeighbours().first().equals(neighbourId)) {
				result.add(next);
			}
		}
		
		return result;
	}
	
	// TODO July 9, 2014  Time: 2:59pm
	/**
	 * Get all passable edge od this road. If all edge are impassable, then you
	 * are stucked.
	 * 
	 * @return a set of passable edge.
	 */
	public Set<CSUEdge> getPassableEdge() {
		Set<CSUEdge> result = new HashSet<>();
		
		for (CSUEdge next : csuEdges) {
			if (next.isPassable() && !next.isBlocked()) {
				result.add(next);
			}
		}
		
		return result;
	}
	
	/**
	 * Determines whether this road's center point is covered by blockades.
	 * 
	 * @return true when this road's center point is covered by blockade.
	 *         Otherwise, false.
	 */
	public boolean isRoadCenterBlocked() {
		return this.isRoadCenterBlocked;
	}
	
	/**
	 * Determines whether this road is passable or not. When this road is
	 * totally blocked by a blockade, then this road is impassable.
	 * 
	 * @return true when this road is passable. Otherwise, false.
	 */
	public boolean isPassable() {
		if (isAllEdgePassable() || isOneEdgeUnpassable()) {
			
			/*for (CSUBlockade next : getCsuBlockades()) {
				if (next.getPolygon().contains(selfRoad.getX(), selfRoad.getY()))
					return false;
			}*/
			// return true;
			// TODO July 9, 2014  Time: 2:58pm
			return getPassableEdge().size() > 1;      ///why > 
		} else {
			List<CSUBlockade> blockades = new LinkedList<>(getCsuBlockades());
			
			for (CSUEscapePoint next : getEscapePoint(this, 500)) {
				blockades.removeAll(next.getRelateBlockade());
			}
			
			if (blockades.isEmpty())
				return true;
			return false;
		}
	}
	
//	public boolean isPassableForPF() {
//		if (isAllEdgePassable()) {
//			return getPassableEdge().size() > 1;
//		} else {
//			List<CSUBlockade> blockades = new LinkedList<>(getCsuBlockades());
//			
//			for (CSUEscapePoint next : getEscapePoint(this, 1000)) {
//				blockades.removeAll(next.getRelateBlockade());
//			}
//			
//			if (blockades.isEmpty())
//				return true;
//			return false;
//		}
//	}
	
	/**
	 * Determines the passability of entrances.
	 * @return true when entrance is passable. Otherwise, false.
	 */
	public boolean isEntrancePassable() {
		return false;
	}
	
	public boolean isAllEdgePassable() {
		for (CSUEdge next : csuEdges) {
			if (!next.isPassable())
				return false;
		}
		return true;
	}
	
	public boolean isOneEdgeUnpassable() {
		int count = 0;
		for (CSUEdge next : csuEdges) {
			if (!next.isPassable()) 
				count++;
		}
		
		if (count == 1)
			return true;
		else
			return false;
	}
	
	public boolean isEntrance() {
		return this.isEntrance;
	}
	
	public boolean isEntranceNeighbour() {
		for (EntityID next : selfRoad.getNeighbours()) {
			StandardEntity neig = world.getEntity(next);
			if (neig instanceof Road && world.getEntrance().containsKey((Road)neig))
				return true;
		}
		return false;
	}
	
	public void setEntrance(boolean entrance) {
		this.isEntrance = entrance;
	}
	
	/**
	 * We only consider the case when there are four edges, excluding entrances.
	 * <p>
	 * Anti-clockwise of verters.
	 */
	public Pair<Line2D, Line2D> getPfClearLine(CSURoad road) {
		
		if (this.pfClearLines != null)
			return this.pfClearLines;
		
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
			roadCenterLine = new Line2D(edge_1.getMiddlePoint(), edge_3.getMiddlePoint());
			
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
			roadCenterLine = new Line2D(edge_2.getMiddlePoint(), edge_4.getMiddlePoint());
			
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
		
		this.pfClearLines = new Pair<Line2D, Line2D>(line_3, line_4);
		return this.pfClearLines;
	}
	
	public Area getPfClearArea(CSURoad road) {
		
		if (this.pfClearArea != null)
			return pfClearArea;
		
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
			roadCenterLine = new Line2D(edge_1.getMiddlePoint(), edge_3.getMiddlePoint());
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
			roadCenterLine = new Line2D(edge_2.getMiddlePoint(), edge_4.getMiddlePoint());
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
		
		// the order of the following two lines should not be change
		area.addPoint((int)end_2.getX(), (int)end_2.getY());
		area.addPoint((int)end_1.getX(), (int)end_1.getY());
		
		this.pfClearArea = new Area(area);
		return this.pfClearArea;
	}
	
	/**
	 * The method {@link CSURoad#getPfClearLine(CSURoad)} should be invoked
	 * somewhere before using of this method.
	 * 
	 * @return the center line of this road. Null when
	 *         {@link CSURoad#getPfClearLine(CSURoad)} has not been invoked
	 *         somewhere before this metthod or the return value of
	 *         {@link CSURoad#getPfClearLine(CSURoad)} is null.
	 */
	public Line2D getRoadCenterLine() {
		return this.roadCenterLine;
	}
	
	private boolean contains(Line2D line, Point2D point, double threshold) {
		
		double pos = java.awt.geom.Line2D.ptSegDist(line.getOrigin().getX(), line.getOrigin().getY(), 
				line.getEndPoint().getX(), line.getEndPoint().getY(), point.getX(), point.getY());
		if (pos <= threshold)
			return true;
		
		return false;
	}
	
	private double distance(Point2D first, Point2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}
	
	public List<CSUEscapePoint> getEscapePoint(CSURoad road, int threshold) {
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
		
		filter(road, m_p_points, threshold);
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
	
	private void filter(CSURoad road, List<CSUEscapePoint> m_p_points, int threshold) {
		Mark:for (Iterator<CSUEscapePoint> itor = m_p_points.iterator(); itor.hasNext(); ) {
			
			CSUEscapePoint m_p = itor.next();
			for (CSUEdge edge : road.getCsuEdges()) {
				if (edge.isPassable())
					continue;
				if (contains(edge.getLine(), m_p.getUnderlyingPoint(), threshold / 2)) {
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
						
						if (distance > threshold && distance < minDistance) {
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
	
	/*public boolean hasIntersection(Polygon polygon, rescuecore2.misc.geometry.Line2D line) {
		List<rescuecore2.misc.geometry.Line2D> polyLines = getLines(polygon);
		for (rescuecore2.misc.geometry.Line2D ln : polyLines) {
			Point2D p = GeometryTools2D.getSegmentIntersectionPoint(ln, line);
			if (p != null)
				return true;
		}
		return false;
	}*/
	
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
	
/* --------------------------------- the following method is only for test --------------------------------- */
	
	public void setCsuBlockades(List<CSUBlockade> blockades) {
		this.csuBlockades.clear();
		this.csuBlockades.addAll(blockades);
	}
	
	public List<CSUEdge> getCsuEdges() {
		return this.csuEdges;
	}
	
	public List<CSUBlockade> getCsuBlockades() {
		return this.csuBlockades;
	}
}
