package csu.model.object;

import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;

import csu.model.AdvancedWorldModel;
import csu.util.Util;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.worldmodel.EntityID;

public class CSUEdge {
	
	private Edge underlyingEdge;
	
	private Pair<EntityID, EntityID> neighbours = null;
	
	private Point2D start, end, openPartStart, openPartEnd, middlePoint;
	
	private Line2D line, openPartLine;
	
	private boolean isBlocked = false, isPassable;
	
	private AdvancedWorldModel world;
	
	// only for test
	public CSUEdge(Point2D start, Point2D end, boolean passable) {
		this.underlyingEdge = null;
		
		this.start = start;
		this.end = end;
		
		this.isPassable = passable;
		
		this.middlePoint = new Point2D((start.getX() + end.getX()) / 2.0, (start.getY() + end.getY()) / 2.0);
		
		this.openPartStart = start;
		this.openPartEnd = end;
		this.line = new Line2D(start, end);
		this.openPartLine = new Line2D(start, end);
	}
	
	public CSUEdge(AdvancedWorldModel world, Edge underlyingEdge, EntityID owner) {
		this.underlyingEdge = underlyingEdge;
		this.neighbours = new Pair<EntityID, EntityID>(underlyingEdge.getNeighbour(), owner);
		
		this.isPassable = underlyingEdge.isPassable();
		
		this.start = underlyingEdge.getStart();
		this.end = underlyingEdge.getEnd();
		
		this.middlePoint = new Point2D((start.getX() + end.getX()) / 2.0, (start.getY() + end.getY()) / 2.0);
		
		this.openPartStart = underlyingEdge.getStart();
		this.openPartEnd = underlyingEdge.getEnd();
		
		this.line = underlyingEdge.getLine();
		this.openPartLine = underlyingEdge.getLine();
		
		this.world = world;
	}
	
	public boolean isPassable() {
		return this.isPassable;
	}
	
	public boolean isBlocked() {
		return this.isBlocked;
	}
	
	public void setBlocked(boolean blocked) {
		this.isBlocked = blocked;
	}
	
	public Line2D getLine() {
		return this.line;
	}
	
	/**
	 * Get the open part of this edge.
	 * 
	 * @return null when this edge is totally blocked.
	 */
	public Line2D getOpenPart() {
		return this.openPartLine;
	}
	
	public void setOpenPart(Line2D openPart) {
		if (openPart == null) {
			this.openPartLine = null;
			this.openPartStart = null;
			this.openPartEnd = null;///
		} else {
			this.openPartLine = openPart;
			this.openPartStart = openPart.getOrigin();
			this.openPartEnd = openPart.getEndPoint();
		}
	}
	
	public void setOpenPart(Point2D start, Point2D end) {
		if (start == null || end == null) {
			this.openPartLine = null;
			this.openPartStart = null;
			this.openPartEnd = null;
		} else {
			this.openPartStart = start;
			this.openPartEnd = end;
			this.openPartLine = new Line2D(start, end);
		}
	}
	
	public boolean isNeedToClear() {
		Area area_1 = world.getEntity(neighbours.first(), Area.class);
		Area area_2 = world.getEntity(neighbours.second(), Area.class);
		Set<EntityID> blockadeIds = new HashSet<>();
		if (area_1 != null && area_1.isBlockadesDefined())
			blockadeIds.addAll(area_1.getBlockades());
		if (area_2 != null && area_2.isBlockadesDefined())
			blockadeIds.addAll(area_2.getBlockades());
		
		java.awt.geom.Area edgeArea = edgeArea(line, getLength(line));
		
		for (EntityID next : blockadeIds) {
			Blockade blockade = (Blockade) world.getEntity(next);
			Polygon po = Util.getPolygon(blockade.getApexes());
			java.awt.geom.Area blocArea = new java.awt.geom.Area(po);
			blocArea.intersect(edgeArea);
			if (!blocArea.getPathIterator(null).isDone())
				return true;
		}
		return false;
	}
	
	private java.awt.geom.Area edgeArea(Line2D line, double rad) {
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
		
		return new java.awt.geom.Area(polygon);
	}
	
	public Point2D getStart() {
		return this.start;
	}
	
	public Point2D getEnd() {
		return this.end;
	}
	
	public Point2D getMiddlePoint() {
		return this.middlePoint;
	}
	
	public Point2D getOpenPartStart() {
		return this.openPartStart;
	}
	
	public Point2D getOpenPartEnd() {
		return this.openPartEnd;
	}
	
	public Edge getUnderlyingEdge() {
		return this.underlyingEdge;
	}
	
	public Pair<EntityID, EntityID> getNeighbours() {
		return this.neighbours;
	}
	
	private double getLength(Line2D line) {
		return Math.hypot(line.getOrigin().getX() - line.getEndPoint().getX(), 
				line.getOrigin().getY() - line.getEndPoint().getY());
	}
}
