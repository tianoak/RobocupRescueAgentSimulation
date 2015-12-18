package csu.model.flow;

import java.util.Collection;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;

public class FlowLine {
	
	private final Building building;
	private final double distance; 
	private final double sin;
	
	public FlowLine(Building target, Collection<StandardEntity> range, Line2D ray) {
		// 线段从中最附近时Building寻求
		Building nearestHit = null;
		Line2D hitLine = null;
		double nearestHitDist = Double.MAX_VALUE;
		
		for (StandardEntity se : range) {
			if (!(se instanceof Building) || target.equals(se))
				continue;
			final Building building = (Building) se;
			for (Edge e : building.getEdges()) {
				if (e.isPassable()) continue;
				final Line2D l = e.getLine();
				final double d1 = ray.getIntersection(l);
				final double d2 = l.getIntersection(ray);
				if (d1 < nearestHitDist
						&& 0.0 <= d2 && d2 <= 1.0
						&& 0.0 < d1 && d1 <= 1.0) {
					nearestHit = building;
					hitLine = l;
					nearestHitDist = d1;
				}
			}
		}
		building = nearestHit;
		if (nearestHit != null) {
			final Point2D rayPt1 = ray.getOrigin(), rayPt2 = ray.getEndPoint();
			final Point2D hitLinePt1 = hitLine.getOrigin(), hitLinePt2 = hitLine.getEndPoint();
			// Outer product
			final double cross = Math.abs(
					(rayPt2.getX() - rayPt1.getX()) * (hitLinePt2.getY() - hitLinePt1.getY())
					- (hitLinePt2.getX() - hitLinePt1.getX()) * (rayPt2.getY() - rayPt1.getY()));
			final double rayLength = GeometryTools2D.getDistance(rayPt1, rayPt2);
			final double hitLineLength = GeometryTools2D.getDistance(hitLinePt1, hitLinePt2);
			sin = cross / (rayLength * hitLineLength);
			// 交点为止的hitLine和垂直的方向的距离
			distance = rayLength * nearestHitDist;
		}
		else {
			sin = 0.0;
			distance = 0.0;
		}
	}

	public Building getBuilding() {
		return building;
	}

	public double getDistance() {
		return distance;
	}

	public double getSin() {
		return sin;
	}
}
