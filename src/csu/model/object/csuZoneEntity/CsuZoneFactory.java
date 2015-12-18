package csu.model.object.csuZoneEntity;

import java.awt.Point;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import csu.LaunchAgents;
import csu.Viewer.layers.CSU_ZonePolygonLayer;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.standard.Ruler;

/**
 * Zone factory. A zone is a cluster of buildings and those buildings must meet
 * two requirements:
 * <p>
 * 1. There is no road between any two buildings.
 * <p>
 * 2. The minimum distance between two buildings should within a threshold. If
 * the distance is longer than threshold, those two building are two different
 * zones. And the threshold is adjust according to the size of map.
 * 
 * Date: Mar 8, 2014 Time: 0:20pm
 * 
 * @author Appreciation - csu
 */
public class CsuZoneFactory {
	/** A refer of world model.*/
	private AdvancedWorldModel world;
	/** Zone Id generator.*/
	private int idGenerator;
	/***/
	private int neighbourThreshold;
	
	public CsuZoneFactory(AdvancedWorldModel world) {
		this.world = world;
		neighbourThreshold = calculateNeighbourThreshold(world.getMapWidth(), world.getMapHeight());
		idGenerator = 0;
	}
	
	public CsuZones createZones() {
		CsuZones all_zones;
		if (LaunchAgents.SHOULD_PRECOMPUTE) {
			String fileName = "precompute/mapZone.zone";
			File zoneFile = new File(fileName);
			if (zoneFile.exists()) {
				all_zones = readFromFile(fileName);
			} else {
				all_zones = dynamicZoning();
				findZoneNeighbours(all_zones);
				writeZoneFile(all_zones, fileName);
			}
		} else {
			all_zones = dynamicZoning();
			findZoneNeighbours(all_zones);
		}
		
		all_zones.findNeighbourZone();
		
		if (AgentConstants.LAUNCH_VIEWER) {
			//if (CSU_ZonePolygonLayer.CSU_ZONES == null)
				CSU_ZonePolygonLayer.CSU_ZONES = all_zones;
		}
		
		return all_zones;
	}
	
	/**
	 * Zone buildings must satisfy two conditions:
	 * <p>
	 * 1. There is not road between any two buildings.
	 * <p>
	 * 2. The minimum distance between two buildings must within a threshold. If
	 * the distance between two building is longer than this threshold, the two
	 * building are two different zones. And the threshold is adjust according
	 * to the size of map.
	 * 
	 * @return all zones
	 */
	private CsuZones dynamicZoning() {
		List<CSUBuilding> unzonedBuildings = new ArrayList<>(world.getCsuBuildings());
		CsuZone zone;
		CsuZones all_zones = new CsuZones(world.getAgent().getRandom());
		
		while(!unzonedBuildings.isEmpty()) {
			zone = new CsuZone(world, idGenerator++);
			zone.addBuilding(unzonedBuildings.get(0));
			all_zones.addBuildingZonePair(unzonedBuildings.get(0).getId(), zone);
			
			int i = 0;
			while (i < zone.size()) { ///zone.size() is always 1, only add one building
				CSUBuilding centerBuilding = zone.get(i);
				addAroBuild(centerBuilding, unzonedBuildings, zone, all_zones);
				unzonedBuildings.removeAll(zone);
				i++;
			}
			zone.initialiseZone();
			all_zones.add(zone);
		}
		
		/*
		 * Getting all surrounding roads of a zone is quite difficult, and it is
		 * unnecessary to get all surrounding roads, too.
		 */
		Area area = null;
		Area n_area = null;
		for (CsuZone next : all_zones) {
			for (Road entrance : next.getAllEntranceRoad()) {
				for (EntityID neighbour : entrance.getNeighbours()) {
					area = (Area) world.getEntity(neighbour);
					if (area instanceof Building)
						continue;
					next.addSurroundingRoad(neighbour);
					
					for (EntityID n_n_id : area.getNeighbours()) {
						n_area = (Area) world.getEntity(n_n_id);
						if (n_area instanceof Building)
							continue;
						
						if (world.getEntrance().isEntrance((Road)n_area))
							continue;
						
						next.addSurroundingRoad(n_n_id);
					}
				}
			}
		}
		
		return all_zones;
	}
	
	/**
	 * Find neighbour zones for each zone.
	 * 
	 * @param allZoens
	 *            all zones
	 */
	private void findZoneNeighbours(CsuZones allZoens) {
		int size = allZoens.size();
		Polygon polygon, otherPolygon;
		double distance;
		
		for (int i = 0; i < size; i++) {
			polygon = allZoens.get(i).getZonePolygon();
			for (int j = i + 1; j < size; j++) {
				otherPolygon = allZoens.get(j).getZonePolygon();
				distance = Ruler.getDistance(polygon, otherPolygon);
				if (distance <= neighbourThreshold) {
					allZoens.get(i).addNeighbourZoneIds(allZoens.get(j).getZoneId());
					allZoens.get(j).addNeighbourZoneIds(allZoens.get(i).getZoneId());
				}
			}
		}
	}
	
	private CsuZones readFromFile(String fileName) {
		Random random;
		if (world.getAgent() != null)
			random = world.getAgent().getRandom();
		else 
			random = new Random();
		
		CsuZones all_Zones = new CsuZones(random);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String str = reader.readLine();
			int zoneId;
			double radiusLength;
			CsuZone zone = null;
			while(str != null) {
				if (!str.isEmpty()) {
					if (str.startsWith("id: ")) {
						if (zone != null) {
							zone.initialiseZone();
							all_Zones.add(zone);
						}
						
						String[] string = str.split(" ");
						zoneId = Integer.parseInt(string[1]);
						zone = new CsuZone(world, zoneId);
					} else if (str.startsWith("radiusLength: ") && zone != null) {
						String[] string = str.split(" ");
						radiusLength = Double.parseDouble(string[1]);
						zone.setRadiusLength(radiusLength);
					} else if (str.startsWith("neighbours: ") && zone != null) {
						String[] neighbourIds = str.split(" ");
						for (int i = 1; i < neighbourIds.length; i++) {
							if (neighbourIds[i].isEmpty())
								continue;
							zone.addNeighbourZoneIds(new Integer(Integer.parseInt(neighbourIds[i])));
						}
					} else if (str.startsWith("surroundRoads: ") && zone != null) {
						String[] surroundRoads = str.split(" ");
						for (int i = 1; i < surroundRoads.length; i++) {
							if (surroundRoads[i].isEmpty())
								continue;
							zone.addSurroundingRoad(new EntityID(Integer.parseInt(surroundRoads[i])));
						}
					} else if (zone != null) {
						int id = Integer.parseInt(str);
						CSUBuilding building = null;
						
						StandardEntity entity = world.getEntity(new EntityID(id));
						if (entity != null && entity instanceof Building) {
							building = world.getCsuBuilding(entity.getID());
						}
						
						if (building != null) {
							zone.addBuilding(building);
							all_Zones.addBuildingZonePair(building.getId(), zone);
							building.setZoneId(new Integer(zone.getZoneId()));
						}
					}
				}
				str = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return all_Zones;
	}
	
	private void writeZoneFile(CsuZones zones, String fileName) {
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			PrintWriter printWriter = new PrintWriter(file);
			for (CsuZone next : zones) {
				printWriter.println();
				printWriter.println("id: " + next.getZoneId());
				
				printWriter.println("radiusLength: " + next.getRadiusLength());
				
				printWriter.print("neighbours: ");
				for (Integer id : next.getNeighbourZoneIds()) {
					printWriter.print(id.intValue() + " ");
				}
				printWriter.println();
				
				printWriter.print("surroundRoads: ");
				for (EntityID id : next.getSurroundingRoad()) {
					printWriter.print(id.getValue() + " ");
				}
				printWriter.println();
				
				Building building;
				for (CSUBuilding csuBuilding : next) {
					building = csuBuilding.getSelfBuilding();
					printWriter.println(building.getID().getValue());
				}
			}
			printWriter.flush();
			printWriter.close();
		} catch (Exception e) {
			System.err.println("error in write file");
		}
	}
	
	private void addAroBuild(CSUBuilding build, List<CSUBuilding> unzoned, CsuZone zone, CsuZones zones) {
		Integer mapWidth = new Integer((int)world.getMapWidth());
		Integer mapHeight = new Integer((int)world.getMapHeight());
		int threshold = calculateThreshold(mapWidth, mapHeight);
		
		for (CSUBuilding next : unzoned) {
			if (!next.getId().equals(build.getId()) && minBuildingDistance(build, next) < threshold) {
				if (!isAnyRoadBetween(build, next, zone)) {
					next.setZoneId(new Integer(zone.getZoneId()));
					zone.add(next);
					zones.addBuildingZonePair(next.getId(), zone);
				}
			}
		}
	}
	
	private int calculateThreshold(Integer mapWidth, Integer mapHeight) {
		BigInteger width = new BigInteger(mapWidth.toString());
		BigInteger height = new BigInteger(mapHeight.toString());
		BigInteger multiply = width.multiply(height);
		
		BigInteger threshould = new BigInteger("3000000000000");	// three million square meters
		if (multiply.compareTo(threshould) >= 0) {
			return 25000;
		}
		threshould = new BigInteger("500000000000");				// five hundred thousand square meters
		if (multiply.compareTo(threshould) >= 0) {
			return 20000;
		}
		return 8000;
	}
	
	private int calculateNeighbourThreshold(double mapWidth, double mapHeight) {
		Integer width = new Integer((int) mapWidth);
		Integer height = new Integer((int)mapHeight);
		
		BigInteger map_width = new BigInteger(width.toString());
		BigInteger map_height = new BigInteger(height.toString());
		BigInteger multiply = map_width.multiply(map_height);
		
		BigInteger threshold = new BigInteger("3000000000000");		// three million square meters
		if (multiply.compareTo(threshold) >= 0) {
			return 35000;
		}
		threshold = new BigInteger("500000000000");					// five hundred thousand square meters
		if (multiply.compareTo(threshold) >= 0) {
			return 25000;
		}
		
		return 15000;
	}
	
	private int minBuildingDistance(CSUBuilding first, CSUBuilding second) {
		int minDistance = Integer.MAX_VALUE;
		int distance;
		int x_1, y_1, x_2, y_2;
		int[] first_apexList = first.getSelfBuilding().getApexList();
		int[] second_apexList = second.getSelfBuilding().getApexList();
		for (int i = 0; i < first_apexList.length; i++) {
			x_1 = first_apexList[i];
			y_1 = first_apexList[++i];
			for (int j = 0; j < second_apexList.length; j++) {
				x_2 = second_apexList[j];
				y_2 = second_apexList[++j];
				distance = Ruler.getDistance(x_1, y_1, x_2, y_2);
				if (distance < minDistance)
					minDistance = distance;
			}
		}
		return minDistance;
	}
	
	private boolean isAnyRoadBetween(CSUBuilding first, CSUBuilding second, CsuZone zone) {
		int distance = world.getDistance(first.getId(), second.getId());
		Road road;
		for (StandardEntity next : world.getObjectsInRange(first.getId(), distance)) {
			if (next instanceof Road) {
				road = (Road) next;
				Point point_1 = new Point(first.getSelfBuilding().getX(), first.getSelfBuilding().getY());
				Point point_2 = new Point(second.getSelfBuilding().getX(), second.getSelfBuilding().getY());
				if (hasCollision(road, point_1, point_2)) {

					if (!world.getEntrance().isEntrance(road))
						zone.addSurroundingRoad(road);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean hasCollision(Road road, Point p1, Point p2) {
        for (Edge edge : road.getEdges()) {
        	Point2D point_1 = new Point2D(p1.getX(), p1.getY());
        	Point2D point_2 = new Point2D(p2.getX(), p2.getY());
            if (edgeIntersection(edge.getStart(), edge.getEnd(), point_1, point_2)) {
                return true;
            }
        }

        return false;
    }

    private boolean edgeIntersection(Point2D a, Point2D b, Point2D c, Point2D d) {

        double det = determinant(b.minus(a), c.minus(d));
        double t = determinant(c.minus(a), c.minus(d)) / det;
        double u = determinant(b.minus(a), c.minus(a)) / det;

        if ((t < 0) || (u < 0) || (t > 1) || (u > 1))  /// 0<=t<=1 && 0<=u<=1
            return false;
        return true;
    }

    private double determinant(Vector2D p1, Vector2D p2) {
        return p1.getX() * p2.getY() - p1.getY() * p2.getX();
    }
}
