package csu.model.route.pov;

import java.awt.Point;
import java.util.Map;

import csu.model.AdvancedWorldModel;
import csu.model.route.pov.graph.AreaNode;
import csu.model.route.pov.graph.EdgeNode;
import csu.model.route.pov.graph.PassableDictionary;
import csu.model.route.pov.graph.PointNode;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;

class CostFunctionFactory {
		
	static CostFunction normal(final AdvancedWorldModel world, final PassableDictionary passableDic) {
		return new CostFunction() {
			@Override
			public double cost(final PointNode from, final PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				} else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall()) ///why
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 1000.0;
					} else {
						return distance * 1.5;
					}
				}
				if (area instanceof Road) {
					switch (passableDic.getPassableLevel(areaNode, edgeNode, startPoint)) {
					case SURE_PASSABLE:
					case COMMUNICATION_PASSABLE:
						return distance;
					case PARTLT_PASSABLE:
						return distance * 2.0;
					case LOGICAL_PASSABLE:
						return distance;
					case UNKNOWN:
						return distance;
					case UNPASSABLE: 
					{
						return distance * 100000000.0;
					}
					}
				}
				return distance;
			}
		};
	}
	
	static CostFunction strict(final AdvancedWorldModel world, final PassableDictionary passableDic) {
		return new CostFunction() {
			@Override
			public double cost(final PointNode from, final PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				} else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall())
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 100000.0;
					} else {
						return distance * 1.5;
					}
				}
				if (area instanceof Road) {
					switch (passableDic.getPassableLevel(areaNode, edgeNode, startPoint)) {
					case SURE_PASSABLE:
					case COMMUNICATION_PASSABLE:
						return distance;	
					case PARTLT_PASSABLE:
						return distance * 2.0;
					case LOGICAL_PASSABLE:
						return distance * 3.0;
					case UNKNOWN:
						return distance * 4.0;
					case UNPASSABLE:
						return Double.POSITIVE_INFINITY;
					}
				}
				return distance;
			}
		};
	}
	
	static CostFunction search(final AdvancedWorldModel world, final PassableDictionary passableDic) {
		return new CostFunction() {
			@Override
			public double cost(final PointNode from, final PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				} else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall())
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 1000.0;
					} else {
						return distance * 1.5;
					}
				}
				if (area instanceof Road) {
					switch (passableDic.getPassableLevel(areaNode, edgeNode, startPoint)) {
					case SURE_PASSABLE:
						return distance * 1.5;
					case PARTLT_PASSABLE:
						return distance * 2.0;
					case COMMUNICATION_PASSABLE:
						return distance;		
					case LOGICAL_PASSABLE:
						return distance * 10.0;
					case UNKNOWN:
						return distance * 0.75;
					case UNPASSABLE:
						return distance * 1000000.0;
					}
				}
				return distance;
			}
		};
	}
	
	static CostFunction fb(final AdvancedWorldModel world, 
			final PassableDictionary passableDic, final Building dest) {
		return new CostFunction() {
			@Override
			public double cost(final PointNode from, final PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				} else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall())
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 100.0;
					} else {
						return distance * 1.5;
					}
				}
				if (to.distance(dest.getX(), dest.getY()) < world.getConfig().extinguishableDistance) {
					return distance;
				}
				if (area instanceof Road) {
					switch (passableDic.getPassableLevel(areaNode, edgeNode, startPoint)) {
					case SURE_PASSABLE:
					case COMMUNICATION_PASSABLE:
						return distance;	
					case PARTLT_PASSABLE:
						return distance * 2.0;
					case LOGICAL_PASSABLE:
						return distance;
					case UNKNOWN:
						return distance;
					case UNPASSABLE:
						int extinguishable = world.getConfig().extinguishableDistance;
						double cost = distance * 1000.0 * to.distance(dest.getX(), dest.getY());
						return Math.max(cost / extinguishable, 1);  ///why
					}
				}
				return distance;
			}
		};
	}

	static CostFunction pf(final AdvancedWorldModel world) {
		return new CostFunction() {
			@Override
			public double cost(PointNode from, PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				} else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall())
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 1000.0;
					} else {
						return distance * 1.5;
					}
				}
				return distance;
			}
		};
	}
	
	static CostFunction at(final AdvancedWorldModel world, 
			final PassableDictionary passableDic, final Map<EntityID, Double> minStaticCost) {
		return new CostFunction() {
			@Override
			public double cost(final PointNode from, final PointNode to, Point startPoint) {
				double distance = from.distance(to);
				AreaNode areaNode;
				EdgeNode edgeNode;
				if (from instanceof AreaNode) {
					areaNode = (AreaNode) from;
					edgeNode = (EdgeNode) to;
				}
				else /* if (to instanceof AreaNode) */ {
					edgeNode = (EdgeNode) from;
					areaNode = (AreaNode) to;
				}
				
				if (edgeNode.isTooSmall())
					return distance * 100000000.0;
				
				Area area = areaNode.getBelong();
				if (area instanceof Building) {
					if (((Building) area).isOnFire()) {
						return distance * 100000.0;
					}
				}
				if (area instanceof Road) {
					switch (passableDic.getPassableLevel(areaNode, edgeNode, startPoint)) {
					case SURE_PASSABLE:
					case COMMUNICATION_PASSABLE:
						return distance;
					case PARTLT_PASSABLE:
						return distance * 2.0;
					case LOGICAL_PASSABLE:
						return distance;
					case UNKNOWN:
						return distance;
					case UNPASSABLE:
						return distance * 100000000.0;
					}
				}
				Double staticCost = minStaticCost.get(area.getID());
				if (staticCost != null) {
					distance += staticCost;
				}
				return distance;
			}
		};
	}
}
