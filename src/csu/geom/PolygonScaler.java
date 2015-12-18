package csu.geom;

import java.awt.Polygon;
import java.util.Set;

import csu.Viewer.layers.CSU_ConvexHullLayer;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;

import javolution.util.FastSet;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import math.geom2d.Point2D;

/**
 * Tools for scaling polygon.
 * 
 * @author Appreciation - csu
 */
public class PolygonScaler {

	/**
	 * This function scales a polygon by the scale coefficient
	 * 
	 * @param sourcePolygon
	 *            the Polygon we want to scale
	 * @param scale
	 *            the scale coefficient, It actually multiplies to the
	 *            points and makes the new shape
	 * @return returns the scaled polygon which, its center is on the center
	 *         of the last polygon
	 */
	public static Polygon scalePolygon(Polygon sourcePolygon, double scale) {
		Polygon scaledPolygon;
		Point2D p1, p2;
		int[] xs = new int[sourcePolygon.npoints];
		int[] ys = new int[sourcePolygon.npoints];

		for (int i = 0; i < sourcePolygon.npoints; i++) {
			p1 = new Point2D(sourcePolygon.xpoints[i], sourcePolygon.ypoints[i]);
			p2 = p1.scale(scale);
			xs[i] = (int) p2.getX();
			ys[i] = (int) p2.getY();
			p1.clone();
		}

		Polygon preScaledPolygon = new Polygon(xs, ys, sourcePolygon.npoints);
		scaledPolygon = reAllocatePolygon(preScaledPolygon, sourcePolygon);///place at the center position
		if (scaledPolygon == null)
			scaledPolygon = preScaledPolygon;

		return scaledPolygon;
//		return preScaledPolygon;
	}

	/**
	 * This function changes the position of the polygon which is scaled by the
	 * {@link #scalePolygon(Polygon, double) scalePolygon(Polygon, double)}
	 * function. If we don't use this function the scaled polygon does not
	 * appear in the right place.
	 * 
	 * @param scaled
	 *            is the scaled polygon of our source (notice that it is not in
	 *            the right place)
	 * @param source
	 *            is the source polygon, (that is not scaled) we want it to
	 *            determine the exact position of our scaled polygon
	 * @return returns the new polygon that is in the right place (its center is
	 *         exactly on the old center)
	 */
	private static Polygon reAllocatePolygon(Polygon scaled, Polygon source) {
		if (source == null || scaled == null || source.npoints == 0 || scaled.npoints == 0)
			return null;

		Polygon reAllocated;
		int[] xs = new int[scaled.npoints];
		int[] ys = new int[scaled.npoints];

		int sourceCenterX = 0;
		int sourceCenterY = 0;

		int scaledCenterX = 0;
		int scaledCenterY = 0;

		for (int i = 0; i < scaled.npoints; i++) {
			sourceCenterX += source.xpoints[i];
			sourceCenterY += source.ypoints[i];

			scaledCenterX += scaled.xpoints[i];
			scaledCenterY += scaled.ypoints[i];
		}

		sourceCenterX /= source.npoints;
		sourceCenterY /= source.npoints;

		scaledCenterX /= scaled.npoints;
		scaledCenterY /= scaled.npoints;

		int xDistance = sourceCenterX - scaledCenterX;
		int yDistance = sourceCenterY - scaledCenterY;
        ///then the center of the scaled will be at the same position with the source
		for (int i = 0; i < scaled.npoints; i++) {
			xs[i] = scaled.xpoints[i] + xDistance;
			ys[i] = scaled.ypoints[i] + yDistance;
		} 

		reAllocated = new Polygon(xs, ys, scaled.npoints);
		return reAllocated;
	}

	/**
	 * Get all border buildings of this world.
	 * 
	 * @return all border buildings of this world
	 */
	public static Set<StandardEntity> getMapBorderBuildings(CompositeConvexHull convexHull, 
			Set<StandardEntity> entities, double scale, AdvancedWorldModel world) {
		Building building;
		Polygon convexHullPolygon = convexHull.getConvexPolygon();
		Set<StandardEntity> borderEntities = new FastSet<StandardEntity>();
       ///when will occur ?
		if (convexHullPolygon.npoints == 0) {
			System.out.println("Something gone wrong in setting border entities for Firebrigade!!!");
			return null;
		}

		Polygon smallBorderPolygon = scalePolygon(convexHullPolygon, scale);    ///0.9 when get border entities in FireCluster
		Polygon bigBorderPolygon = scalePolygon(convexHullPolygon, 2 - scale);  ///1.1

		for (StandardEntity entity : entities) {
			if (entity instanceof Refuge)
				continue;
			if (!(entity instanceof Building))
				continue;
			building = (Building) entity;
			int[] vertices = building.getApexList();
			for (int i = 0; i < vertices.length; i += 2) {
				if ((bigBorderPolygon.contains(vertices[i], vertices[i + 1]))
						&& !(smallBorderPolygon.contains(vertices[i], vertices[i + 1]))) {
					borderEntities.add(entity);
					break;
				}
			}
		}
		
		if (AgentConstants.LAUNCH_VIEWER) {
			if (CSU_ConvexHullLayer.BIG_MAP_BORDER_CONVEX_HULL == null) {
				CSU_ConvexHullLayer.BIG_MAP_BORDER_CONVEX_HULL = bigBorderPolygon;
			}
			
			if (CSU_ConvexHullLayer.SMALL_MAP_BORDER_CONVEX_HULL == null) {
				CSU_ConvexHullLayer.SMALL_MAP_BORDER_CONVEX_HULL = smallBorderPolygon;
			}
			
			if (CSU_ConvexHullLayer.MAP_BORDER_BUILDINGS.isEmpty()) {
				for (StandardEntity next : borderEntities) {
					if (next instanceof Building) {
						CSU_ConvexHullLayer.MAP_BORDER_BUILDINGS.add(next.getID());
					}
				}
			}
		}

		return borderEntities;
	}
}
