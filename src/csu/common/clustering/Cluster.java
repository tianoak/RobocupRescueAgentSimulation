package csu.common.clustering;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import csu.geom.CompositeConvexHull;
import csu.geom.ConvexHull_Interface;
import csu.geom.ConvexObject;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBuilding;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

public abstract class Cluster {
	/**
	 * The Id of this Cluster.
	 */
	protected int id;
	
	protected int dyingTimeLock = 0;
	
	/**
	 * The value of this Cluster.
	 */
	protected double value;
	
	/**
	 * The world model.
	 */
	protected AdvancedWorldModel world;
	
	/**
	 * Entities that this Cluster cares about. For example, FireCluster will
	 * care about fired buildings.
	 */
	protected Set<StandardEntity> caredEntities;
	
	protected Set<EntityID> caredEntityIds;
	
	/** 
	 * All entities within the shape of this Cluster.
	 */
	protected Set<EntityID> allEntities;

	// /** Entities that newly added to this cluster this cycle.*/
	// protected Set<StandardEntity> newEntities;
	// /** Entities that removed from this cluster this cycle.*/
	// protected Set<StandardEntity> removedEntities;

	/**
	 * Border entities of this cluster, and those border entities are the
	 * entities this Cluster cares about.
	 */
	protected Set<StandardEntity> borderEntities;
	
	/**
	 * Ignored border entities of this Cluster.
	 * <p>
	 * Border entities that share by two or more clusters should be ignored.
	 */
	protected Set<StandardEntity> singleIgnoreBorderEntities;

	/** 
	 * The center point of this cluster. 
	 */
	protected Point center;
	
	/** 
	 * The underlying {@link ConvexObject}. 
	 */
	protected ConvexObject convexObject;
	
	/**
	 * The convex hull of this cluster. 
	 */
	protected ConvexHull_Interface convexHull;

	/** 
	 * Enlarged polygon of this cluster's convex hull polygon(scale is 1.1). 
	 */
	protected Polygon bigBorderPolygon;
	
	/** 
	 * Narrowed polygon of this cluster's convex hull polygon(scale is 0.9). 
	 */
	protected Polygon smallBorderPolygon;

	/** 
	 * Flag used to determines whether this cluster should be removed. 
	 */
	protected boolean isDying;
	
	/**
	 * Flag used to determines whether this cluster is a border cluster of current map.
	 */
	protected boolean isBorder;
	
	/**
	 * Flag used to determines whether this cluster's convex hull polygon is
	 * over ConverObject's CENTER_POINT.
	 */
	protected boolean isOverCenter;
	
	
	// constructor
	protected Cluster(AdvancedWorldModel world) {
		this.caredEntities = new FastSet<StandardEntity>();
		this.caredEntityIds = new FastSet<EntityID>();
		this.allEntities = new FastSet<EntityID>();

		this.borderEntities = new FastSet<StandardEntity>();
		this.singleIgnoreBorderEntities = new FastSet<StandardEntity>();

		// this.newEntities = new HashSet<>();
		// this.removedEntities = new HashSet<>();

		this.convexHull = new CompositeConvexHull();
		this.convexObject = new ConvexObject();

		this.smallBorderPolygon = new Polygon();
		this.bigBorderPolygon = new Polygon();
		this.isBorder = false;
		this.isDying = false;
		this.isOverCenter = false;

		this.world = world;
	}

	/** 
	 * Update the convex hull polygon.
	 */
	public abstract void updateConvexHull();
	
	/** 
	 * Update the value of this cluster.
	 */
	public abstract void updateValue();
	
	/**
	 * Get the total area of this cluster. And this area is determined by the
	 * bounding box of this cluster's convex hull polygon.
	 * 
	 * @return the bounding box area of this cluster
	 */
	public double getBoundingBoxArea() {
		Dimension clusterDimension = convexHull.getConvexPolygon().getBounds().getSize();
		return clusterDimension.getHeight() * clusterDimension.getWidth() / 1000000d;
	}
	
	/**
	 * Merger the target cluster into this one.
	 * 
	 * @param cluster
	 *            the merged cluster
	 */
	public void merge(Cluster cluster) {
		/* if the eaten cluster is not dying, the new cluster will not dying too*/
		if (!cluster.isDying()) {
			this.isDying = cluster.isDying();
		}
		/* if the eaten cluster is a border cluster, then the new cluster is a border cluster.*/
		if (cluster.isBorder()) {
			this.isBorder = cluster.isBorder;
		}

		// this.newEntities.addAll(cluster.getEntities());
		this.caredEntities.addAll(cluster.getEntities());
	}

/* ----------------------------- Add and remove entities from this cluster --------------------------------- */
	public void addAll(List<StandardEntity> entities) {
		// this.newEntities.addAll(entities);
		this.caredEntities.addAll(entities);
	}

	public void add(StandardEntity entity) {
		// this.newEntities.add(entity);
		this.caredEntities.add(entity);
	}

	public void removeAll(List<StandardEntity> entities) {
		// this.removedEntities.addAll(entities);
		this.caredEntities.removeAll(entities);
	}

	public void remove(StandardEntity entity) {
		// this.removedEntities.add(entity);
		this.caredEntities.remove(entity);
	}
	
/* --------------------------------------- handle boolean flags ------------------------------------------ */
	
	public boolean isDying() {
		return this.isDying;
	}
	public void setDying(boolean dying) {
		this.isDying = dying;
	}
	
	public boolean isBorder() {
		return this.isBorder;
	}
	public void setBorder(boolean border) {
		this.isBorder = border;
	}
	
	public boolean isOverCenter() {
		return this.isOverCenter;
	}
	
	/**
	 * Check whether this cluster's convex hull polygon contains the
	 * ConvexObject's CENTER_POINT.
	 */
	public void checkForOverCenter(Point targetPoint) {
		Polygon convexPolygon = this.convexHull.getConvexPolygon();
		Rectangle convexPolygonBound = convexPolygon.getBounds();
		int convexCenterPoint_x = (int)convexPolygonBound.getCenterX();
		int convexCenterPoint_y = (int)convexPolygonBound.getCenterY();
		Point convexCenterPoint = new Point(convexCenterPoint_x, convexCenterPoint_y);
		
		this.convexObject.CENTER_POINT = targetPoint;
		this.convexObject.CONVEX_POINT = convexCenterPoint;
		
		int[] xs = this.convexHull.getConvexPolygon().xpoints;
		int[] ys = this.convexHull.getConvexPolygon().ypoints;
		
		double x1, y1, x2, y2, total_1, total_2;
		
		for (int i = 0; i < ys.length; i++) {
			Point point = new Point(xs[i], ys[i]);
			x1 = (convexCenterPoint.getX() - targetPoint.getX()) / 1000;
			y1 = (convexCenterPoint.getY() - targetPoint.getY()) / 1000;
			
			x2 = (point.getX() - targetPoint.getX()) / 1000;
			y2 = (point.getY() - targetPoint.getY()) / 1000;
			
			total_1 = x1 * x2;
			total_2 = y1 * y2;
			if (total_1 <= 0 && total_2 <= 0 /*or total_1 + total_2 <= 0*/) {
				this.isOverCenter = true;
				break;
			}
		}
	}
	
/* ------------------------------------------- getter and setter ------------------------------------------*/
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public double getValue() {
		return this.value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	public Set<EntityID> getAllEntities() {
		return allEntities;
	}
	
	public void setAllEntities(Set<EntityID> allEntities) {
		this.allEntities = allEntities;
    }
	
	public void setDyingClusterAllEntities(Set<EntityID> allEntities) {
		this.allEntities.clear();
		
		this.allEntities.addAll(allEntities);
		for (EntityID next : allEntities) {
			StandardEntity entity = world.getEntity(next);
			
			if (!(entity instanceof Building))
				continue;
			CSUBuilding csu_b = world.getCsuBuilding(next);
			
			for (CSUBuilding next_b : csu_b.getConnectedBuildings()) {
				this.allEntities.add(next_b.getId());
			}
		}
	}
	
	public void removeFromAllEntities(Collection<EntityID> removedEntities) {
		this.allEntities.removeAll(removedEntities);
	}
	
	public int getDyingTimeLock() {
		return this.dyingTimeLock;
	}
	
	public void increaseDyingTimeLock() {
		this.dyingTimeLock ++;
	}

	public Set<StandardEntity> getSingleIgnoreBorderEntities() {
		return singleIgnoreBorderEntities;
	}
	
//	public void setSingleIgnoredBorderEntities(Set<StandardEntity> ignoredBorderEntities) {
//        this.singleIgnoreBorderEntities = ignoredBorderEntities;
//  }
	
	public Set<StandardEntity> getBorderEntities() {
		return borderEntities;
	}
	
	public Set<StandardEntity> getEntities() {
		return caredEntities;
	}
	
//	public Set<StandardEntity> getNewEntities() {
//		return newEntities;
//	}


	public ConvexHull_Interface getConvexHull() {
		return convexHull;
	}
	
	public Polygon getBigBorderPolygon() {
		return bigBorderPolygon;
	}
	
	public Polygon getSmallBorderPolygon() {
		return smallBorderPolygon;
	}
	
	public Point getCenter() {
		return center;
	}
	
	public AdvancedWorldModel getWorld() {
		return world;
	}
	
	public ConvexObject getConvexObject() {
		return this.convexObject;
	}
}
