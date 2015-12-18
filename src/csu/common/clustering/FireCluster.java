package csu.common.clustering;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.Viewer.layers.CSU_ConvexHullLayer;
import csu.agent.fb.tools.WaterCoolingEstimator;
import csu.geom.CompositeConvexHull;
import csu.geom.ConvexObject;
import csu.geom.PolygonScaler;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.standard.Ruler;

public class FireCluster extends Cluster{
	
	private double coefficient;
	// private double neededWater;
	
	///set default value, avoid nullPointer
	private FireCondition condition = FireCondition.unControllable;
	
	/** 
	 * The real fired buildings in this cluster. 
	 */
	private List<Building> realFiredBuildings;
	
	/**
	 * An enum representation of this FireCluster's condition. They are
	 * <i>smallControllable</i>, <i>largeControllable</i>,
	 * <i>edgeControllable</i> and <i>unControllable</i>.
	 */
	public enum FireCondition{
		smallControllable, largeControllable, edgeControllable, unControllable
	}

	// constructor
	public FireCluster(AdvancedWorldModel world) {
		super(world);
		this.realFiredBuildings = new ArrayList<Building>();
	}
	
	/** 
	 * Update fire condition of this fire cluster.
	 */
	public void updateFireCondition() {
		double fireBoundingBoxArea = this.getBoundingBoxArea();
		double mapArea = world.getMapDimension().getHeight() * world.getMapDimension().getWidth() / 1000000d;
		double percent = fireBoundingBoxArea / mapArea;
		if (percent > 0.80) {
			this.setFireCondition(FireCondition.unControllable);
			return;
		}
		if (percent > 0.15) {
			this.setFireCondition(FireCondition.edgeControllable);
			return;
		}
		if (percent > 0.04) {
			this.setFireCondition(FireCondition.largeControllable);
			return;
		}
		if (percent > 0.00) {
			this.setFireCondition(FireCondition.smallControllable);
			return;
		}
	}

//	/**
//	 * In this update model, we add new buildings in and remove useless building. Then we update the
//	 * convex hull polygon according to those added and removed building vertices and return the new
//	 * polygon. 
//	 */
//	@Override
//	public void updateConvexHull() {
//		Building building;
//		for (StandardEntity entity : this.entities) {
//			if (entity instanceof Building) {
//				building = (Building)entity;
//				if (this.isBorder && !world.getBorderBuildings().contains(entity)) {
//					this.setBorder(false);
//				}
//				
//				int[] vertices = building.getApexList();
//				for (int i = 0; i < vertices.length; i += 2) {
//					this.convexHull.addPoint(vertices[i], vertices[i + 1]);
//				}
//			}
//		}
//		
//		for (StandardEntity entity : this.newEntities) {
//			if (entity instanceof Building) {
//				building = (Building)entity;
//				if (!isBorder && world.getBorderBuildings().contains(entity)) {
//					this.setBorder(true);
//				}
//				int[] vertices = building.getApexList();
//				for (int i = 0; i < vertices.length; i += 2) {
//					this.convexHull.addPoint(vertices[i], vertices[i + 1]);
//				}
//			}
//		}
//		
//		for (StandardEntity entity : this.removedEntities) {
//			if (entity instanceof Building) {
//				building = (Building) entity;
//				int[] vertices = building.getApexList();
//				for (int i = 0; i < vertices.length; i += 2) {
//					this.convexHull.removePoint(vertices[i], vertices[i + 1]);
//				}
//			}
//		}
//		
//		newEntities.clear();
//		removedEntities.clear();
//		
//		Set<Building> dangerBuildings = new HashSet<>();
//		this.sizeOfBuilding(dangerBuildings);
//		this.setDying(dangerBuildings.isEmpty());
//		
//		this.convexObject.setConvexHullPolygon(convexHull.getConvexPolygon());
//		this.setBorderEntities();
//		this.setCenter();
//	}
	
	/**
	 * In this update method, we create a new CompositeConvexHull object and add
	 * all entity into this convex hull. Then create a new convex hull polygon
	 * through CompositeConvexHull's getConvexPolygon method.
	 */
	@Override
	public void updateConvexHull() {
		Building building;
		convexHull = new CompositeConvexHull();  // create a new CompositeConvexHull object
		for (StandardEntity entity : this.caredEntities) {
			if (entity instanceof Building) {
				building = (Building)entity;
				if (!isBorder && world.getBorderBuildings().contains(entity)) {
					this.setBorder(true);
				}
				
				int[] vertices = building.getApexList();
				for (int i = 0; i < vertices.length; i += 2) {
					this.convexHull.addPoint(vertices[i], vertices[i + 1]);
				}
			}
		}
		
		Set<Building> dangerBuildings = new HashSet<>();
		this.sizeOfBuilding(dangerBuildings);
		this.setDying(dangerBuildings.isEmpty());
		
		this.convexObject.setConvexHullPolygon(convexHull.getConvexPolygon());
		this.setBorderEntities();
		this.setCenter();
	}
	
	@Override
	public void updateValue() {
		if (isDying)
			dyingTimeLock++;
		else
			dyingTimeLock = 0;
	}
	
	/**
	 * This function calculate the number buildings with real fieryness 1, 2 and 3.
	 * <p>
	 * This function also find danger buildings of this cluster which used to
	 * determines whether thic cluster is dying or not.
	 */
	int count = 0;							
	private void sizeOfBuilding(Set<Building> dangerBuildings) {
		for (StandardEntity entity : caredEntities) {
			if (entity instanceof Building) {
				CSUBuilding estimatedBuilding = world.getCsuBuilding(entity);
				Building building = (Building) entity;
				if (building.isOnFire()) {
					// dangerBuildings.add(building);
					realFiredBuildings.add(building);
					count ++;
				}
				int fieryness = estimatedBuilding.getEstimatedFieryness();
				switch (fieryness) {
				case 1:
				case 2:
					dangerBuildings.add(building);
					break;
				case 3:
					if (estimatedBuilding.getEstimatedTemperature() > 150) {
						dangerBuildings.add(building);
					}
					break;
				default:
					break;
				}
			}
		}
		value = coefficient * count;
	}
	
	/**
	 * Set border buildings of this FireCluster. All buildings except Refuge
	 * within {@link Cluster#smallBorderPolygon smallBorderPolygon} and
	 * {@link Cluster#bigBorderPolygon bigBorderPolygon} are border buildings.
	 */
	private void setBorderEntities() {
		Building building;
		this.borderEntities.clear();
		
		if (convexObject.getConvexHullPolygon().npoints == 0)
			return;
		
		this.smallBorderPolygon = PolygonScaler.scalePolygon(convexObject.getConvexHullPolygon(), 0.9);
		this.bigBorderPolygon = PolygonScaler.scalePolygon(convexObject.getConvexHullPolygon(), 1.1);
		
		for (StandardEntity entity : this.caredEntities) {
			if (entity instanceof Refuge) {
                continue;
            }
            if (!(entity instanceof Building)) {
                continue;
            }
			building = (Building) entity;
			int[] vertices = building.getApexList();
			for (int i = 0; i < vertices.length; i += 2) {
				boolean flag_1 = this.bigBorderPolygon.contains(vertices[i], vertices[i + 1]);
				boolean flag_2 = this.smallBorderPolygon.contains(vertices[i], vertices[i + 1]);
				if (flag_1 && !flag_2) {
					this.borderEntities.add(entity);
					break;
				}
			}
		}
	}
	
	/**
	 * Set the center point of this FireCluster.
	 */
	private void setCenter() {
		int centerX = 0;
		int centerY = 0;
		Polygon convexHullPolygon = convexObject.getConvexHullPolygon();
		
		for (int x : convexHullPolygon.xpoints) {
			centerX += x;
		}
		for (int y : convexHullPolygon.ypoints) {
			centerY += y;
		}
		if (convexHullPolygon.npoints > 0) {
			centerX /= convexHullPolygon.npoints;
			centerY /= convexHullPolygon.npoints;
			this.center = new Point(centerX, centerY);
		} else {
			this.center = new Point(0, 0);
		}
	}

	/**
	 * Calculate the need water to extinguish this FireCluster. Only border
	 * buildings are considered.
	 * 
	 * @return the need water to extinguish this FireCluster's border buildings
	 */
	public int calculateNeedWaterToExtinguish() {
		int neededWater = 0;
		for (StandardEntity entity : getBorderEntities()) {
			neededWater += WaterCoolingEstimator.waterNeededToExtinguish(world.getCsuBuilding(entity));
		}
		return neededWater;
	}
	///why
	public boolean isExpandableToMapCenter() {
		if (isBorder) {
			int mapWidth = (int)world.getMapDimension().getWidth();
			int mapHeight = (int)world.getMapDimension().getHeight();
			Point mapCenter = new Point(mapWidth >> 1, mapHeight >> 1);
			double distanceClusterToCenter = Ruler.getDistance(this.center, mapCenter);
			
			for (StandardEntity entity : world.getBorderBuildings()) {
				Building building = (Building) entity;
				double distanceBuildingToCenter = Ruler.getDistance(building.getLocation(world), mapCenter);
				if (distanceBuildingToCenter <= distanceClusterToCenter) {
					return true;
				}
			}   
		} else {
			return true;
		}
		              
		return false;
	}
	
/* -------------------------------------------------------------------------------------------------------- */
	/**
	 * Determines whether there are candidate buildings in the given direction.
	 * 
	 * @param center
	 *            the point marks the direction
	 * @param limitDirection
	 *            a flag used to determines the size of the direction triangle
	 * @return true if there is building in this direction. Otherwise, false.
	 */
	public boolean haveBuildingInDirectionOf(Point center) {
		if (!isOverCenter)
			this.checkForOverCenter(center);
		this.setTriangle(isOverCenter);
		
		if (this.isDying() || this.getConvexObject() == null)
			return false;
		if (convexObject.CENTER_POINT == null || convexObject.CONVEX_POINT == null) 
			return false;
		
		Building building;
		CSUBuilding csuBuilding;
		Polygon polygon;
		
		if (isOverCenter) {
			polygon = this.convexObject.getDirectionRectangle();
		} else {
			polygon = this.convexObject.getTriangle();
		}
		
		for (StandardEntity entity : this.borderEntities) {
			building = (Building) entity;
			csuBuilding = world.getCsuBuilding(entity);
			if (!isCandidate(csuBuilding))
				continue;
			if (!isOldCandidate(csuBuilding))
				continue;
			int[] vertices = building.getApexList();
			for (int i = 0; i < vertices.length; i += 2) {
				if (polygon.contains(vertices[i], vertices[i + 1]))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Get all candidate buildings of this FireCluster in the given direction.
	 * 
	 * @param center
	 *            the point marks the direction
	 * @param limitDirection
	 *            a flag used to determine the size of the direction triangle
	 * @return
	 */
	public Set<CSUBuilding> findBuildingInDirection(Point center) {
		Set<CSUBuilding> targetBuildins = new FastSet<>();
		
		if (!isOverCenter)
			this.checkForOverCenter(center);
		this.setTriangle(isOverCenter);
		
		if (this.isDying() || this.getConvexObject() == null)
			return targetBuildins;
		if (convexObject.CENTER_POINT == null || convexObject.CONVEX_POINT == null) 
			return targetBuildins;
		
		Building building;
		CSUBuilding csuBuilding;
		Polygon polygon;
		if (isOverCenter) {
			polygon = this.convexObject.getDirectionRectangle();
		} else {
			polygon = this.convexObject.getTriangle();
		}
		
		for (StandardEntity entity : this.borderEntities) {
			building = (Building) entity;
			csuBuilding = world.getCsuBuilding(entity);
			if (!isCandidate(csuBuilding))
				continue;
			if (!isOldCandidate(csuBuilding))
				continue;
			int[] vertices = building.getApexList();
			for (int i = 0; i < vertices.length; i += 2) {
				if (polygon.contains(vertices[i], vertices[i + 1])) {
					targetBuildins.add(csuBuilding);
					break;
				}
			}
		}
		
		if (AgentConstants.LAUNCH_VIEWER) {
			EntityID id = world.getAgent().getID();
			List<StandardEntity> result = new ArrayList<>();
			for (CSUBuilding next : targetBuildins) {
				result.add(next.getSelfBuilding());
			}
			CSU_ConvexHullLayer.BORDER_DIRECTION_BUILDINGS.put(id, result);
		}
		
		return targetBuildins;
	}
	
	/**
	 * We should find the expand direction of fire cluster. And in the expand
	 * direction, there always are buildings not burn or with slight burn rate.
	 */
	private boolean isCandidate(CSUBuilding building) {
		return !(building.getEstimatedFieryness() == 2
				|| building.getEstimatedFieryness() == 3
				|| building.getEstimatedFieryness() == 8);
		
	}

	/**
	 * For those building that is the candidate in last one or two cycle will
	 * have a higher fieryness in current cycle. So we simply think that
	 * buildings with estimate fieryness == 3 and estimate temperature < 150 are
	 * the candidate in last one or two cycle.
	 */
	private boolean isOldCandidate(CSUBuilding building) {
		return !(building.getEstimatedFieryness() == 3 && building.getEstimatedTemperature() < 150);
	}
	
	/**
	 * Set the direction triangle. Please run class {@link TestForSetTriangle}
	 * to see what this method can do.
	 */
	private void setTriangle(boolean isOverCenter) {
		Polygon convexPolygon = this.convexHull.getConvexPolygon();
		Rectangle convexPolygonBound = convexPolygon.getBounds();
		double polygonBoundWidth = convexPolygonBound.getWidth();
		double polygonBoundHeight = convexPolygonBound.getHeight();
		double radiusLength = Math.hypot(polygonBoundWidth, polygonBoundHeight); 
		
		Point targetPoint = this.convexObject.CENTER_POINT;
		Point convexCenterPoint = this.convexObject.CONVEX_POINT;
		
		if (isOverCenter) {
			radiusLength /= 2.0;
		} else {
			rescuecore2.misc.geometry.Point2D point = 
					new rescuecore2.misc.geometry.Point2D(targetPoint.getX(), targetPoint.getY());
			double distance = Ruler.getDistance(convexPolygon, point);
			
			if (distance > radiusLength)
				radiusLength = distance;
			
			/*if (distance < radiusLength / 2.0)
				radiusLength /= 2.0;
			else
				radiusLength = distance;*/
		}
		
		Point[] points = getPerpendicularPoints(targetPoint, convexCenterPoint, radiusLength);
		Point point1 = points[0], point2 = points[1];
		
		this.convexObject.FIRST_POINT = points[0];
		this.convexObject.SECOND_POINT = points[1];
		
		if (AgentConstants.LAUNCH_VIEWER) {
			EntityID id = world.getAgent().getID();
			Pair<Point, ConvexObject> pair = new Pair<Point, ConvexObject>(targetPoint, convexObject);
			List<Pair<Point, ConvexObject>> list = new ArrayList<>();
			list.add(pair);
			CSU_ConvexHullLayer.TRIANGLE_CENTER_POINT.put(id, list);
		}
		
		Polygon trianglePolygon = new Polygon();
		trianglePolygon.addPoint(convexCenterPoint.x, convexCenterPoint.y);
		trianglePolygon.addPoint(point1.x, point1.y);
		trianglePolygon.addPoint(point2.x, point2.y);
		this.convexObject.setTriangle(trianglePolygon);
		
		if (isOverCenter) {
			double distance = point1.distance(point2) / 2.0;
			
			Polygon directionPolygon = new Polygon();
			directionPolygon.addPoint(point1.x, point1.y);
			directionPolygon.addPoint(point2.x, point2.y);
			
			points = getPerpendicularPoints(point1, point2, distance);
			if (convexCenterPoint.distance(points[0]) > convexCenterPoint.distance(points[1])) {
				directionPolygon.addPoint(points[0].x, points[0].y);
				this.convexObject.OTHER_POINT_1 = points[0];
			} else {
				directionPolygon.addPoint(points[1].x, points[1].y);
				this.convexObject.OTHER_POINT_1 = points[1];
			}
			points = getPerpendicularPoints(point2, point1, distance);
			if (convexCenterPoint.distance(points[0]) > convexCenterPoint.distance(points[1])) {
				directionPolygon.addPoint(points[0].x, points[0].y);
				this.convexObject.OTHER_POINT_2 = points[0];
			} else {
				directionPolygon.addPoint(points[1].x, points[1].y);
				this.convexObject.OTHER_POINT_2 = points[1];
			}
		}
	}
	
	/**
	 * We have a test class {@link TestForGetPerpendicularPoints} for this
	 * method. Please run this test class to see what this method can do.
	 */
    public Point[] getPerpendicularPoints(Point2D P_1, Point2D P_2, double radiusLength) {
    	double x1 = P_1.getX();
        double y1 = P_1.getY();
        double x2 = P_2.getX();
        double y2 = P_2.getY();
        
        double x3, x4, y3, y4;
        
        if (y1 == y2) {
        	x3 = x1;
        	x4 = x1;
        	y3 = y1 + radiusLength;
        	y4 = y1 - radiusLength;
        } else {
        	/* a * X^2 + b * X + c = 0 */
            double m1 = (y1 - y2) / (x1 - x2);   ///infinity
            double m2 = (-1 / m1);                    ///0

//            double a = Math.pow(m2, 2) + 1;
//            double b = (-2 * x1) - (2 * Math.pow(m2, 2) * x1);
//            double c = (Math.pow(x1, 2) * (Math.pow(m2, 2) + 1)) - Math.pow(radiusLength, 2);
//            x3 = ((-1 * b) + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
//            x4 = ((-1 * b) - Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);

            double x = Math.sqrt(Math.pow(radiusLength,2) / (Math.pow(m2,2)+1));
            x3 = x1 + x;
            x4 = x1 - x;

            y3 = (m2 * x3) - (m2 * x1) + y1;
            y4 = (m2 * x4) - (m2 * x1) + y1;
        }

        Point perpendicular1 = new Point((int) x3, (int) y3);
        Point perpendicular2 = new Point((int) x4, (int) y4);
        return new Point[]{perpendicular1, perpendicular2};
    }
	
/* -------------------------------------------------------------------------------------------------------- */
	public FireCondition getFireCondition() {
		return this.condition;
	}
	public void setFireCondition(FireCondition condition) {
		this.condition = condition;
	}
}
