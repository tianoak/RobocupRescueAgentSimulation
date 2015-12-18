
package csu.model.object.csuZoneEntity;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javolution.util.FastSet;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;

import csu.geom.CompositeConvexHull;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBuilding;
import csu.standard.Ruler;

@SuppressWarnings("serial")
public class CsuZone extends ArrayList<CSUBuilding> implements
		Comparable<CsuZone> {
	/** The Id of this zone. */
	private int id;
	/** A reference of world model. */ 
	private AdvancedWorldModel world;

	/** The polygon of this zone. */
	private Polygon zonePolygon;
	/** The center point of this polygon. */
	private Point centerPoint;
	private double distanceToMapCenter;
	private double radiusLength;
	/**
	 * The number of buildings this zone has.
	 */
	private int buildingCount = 0;

	private List<CsuZone> neighbourZones = new ArrayList<>();
	private List<Integer> neighbourZoneIds = new ArrayList<>();

	private Set<EntityID> unvisitBuildings = new HashSet<>();

	private Set<EntityID> unclearedEntrances = new HashSet<>();

	private Set<EntityID> buriedHumans = new FastSet<>();

	private double totalInitialFuel = 0.0d;
	private double totalGroundArea = 0.0d;
	private double totalArea = 0.0d;

	// private double totalRemainFuel;
	// private List<CSUBuilding> burningBuildings = new ArrayList<>();
	// private List<CSUBuilding> unburnedBuildings = new ArrayList<>();
	//
	// private boolean isOnFire;
	// private boolean isBurned;

	private double localValue;
	private double globalValue;
	private double zoneValue;
	// private double searchValue;
	//
	// private int neededAgentToExtinguish;
	//
	// private Map<EntityID, Integer> agentDistanceMap = new FastMap<EntityID,
	// Integer>();
	// private int fireBrigadeSize;
	// private int maxPower;

	private Set<EntityID> surroundingRoad = new FastSet<>();
	private Set<Road> allEntranceRoad = null;

	int belongPfClusterIndex = -1;
	private Set<Area> criticalAreaList;

	public CsuZone(AdvancedWorldModel world, int id) {
		this.world = world;
		this.id = id;
		this.criticalAreaList = new HashSet<Area>();

		// this.fireBrigadeSize = world.getFireBrigadeIdList().size();
		// this.maxPower = world.getConfig().maxPower;
	}

	public void initialiseZone() {
		calculateCenterPoint();
		createZonePolygon();

		for (CSUBuilding next : this) {
			unvisitBuildings.add(next.getId());
		}

		for (Road next : getAllEntranceRoad()) {
			unclearedEntrances.add(next.getID());
		}
	}

	public Set<Area> getCriticalAreaOfZone() {
		if (this.criticalAreaList == null || this.criticalAreaList.size() == 0) {

			for (EntityID road : this.getSurroundingRoad()) {
				Area area = world.getEntity(road, Area.class);
				if (world.getCriticalArea().isCriticalArea(area)) {
					this.criticalAreaList.add(area);
				}
			}
		} 

		return this.criticalAreaList;
	}

	private void calculateCenterPoint() {
		centerPoint = new Point();
		for (CSUBuilding next : this) {
			centerPoint.x += next.getSelfBuilding().getX();
			centerPoint.y += next.getSelfBuilding().getY();
		}

		int count = this.size();
		if (count != 0) {
			centerPoint.x /= count;
			centerPoint.y /= count;

			Point mapCenter = world.getMapCenterPoint();
			distanceToMapCenter = Ruler.getDistance(centerPoint, mapCenter);
		}
	}

	private void createZonePolygon() {
		try {
			CompositeConvexHull convexHull = new CompositeConvexHull();
			for (CSUBuilding next : this) {
				int[] apexList = next.getSelfBuilding().getApexList();
				for (int i = 0; i < apexList.length; i = i + 2) {
					convexHull.addPoint(apexList[i], apexList[i + 1]);
				}
			}
			zonePolygon = convexHull.getConvexPolygon();

			radiusLength = Math.hypot(zonePolygon.getBounds().getWidth(),
					zonePolygon.getBounds().getHeight());
		} catch (Exception e) {
			e.printStackTrace();
		}

		radiusLength /= 2.0;
	}
   ///the value to priority
	@Override
	public int compareTo(CsuZone o) {
		if (this.zoneValue > o.getZoneValue())
			return -1;

		if (this.zoneValue < o.getZoneValue())
			return 1;

		return 0;
	}

	public boolean addBuilding(CSUBuilding building) {
		totalArea += building.getSelfBuilding().getTotalArea();
		totalGroundArea += building.getSelfBuilding().getGroundArea();
		totalInitialFuel += building.getInitialFuel();
		buildingCount++;

		return super.add(building);
	}

	public Polygon getZonePolygon() {
		return zonePolygon;
	}

	public Point getZoneCenter() {
		return centerPoint;
	}

	public double getTotalInitialFuel() {
		return totalInitialFuel;
	}

	public double getTotaoGroundArea() {
		return totalGroundArea;
	}

	public double getTotalArea() {
		return totalArea;
	}

	public int getZoneId() {
		return this.id;
	}

	public List<CsuZone> getNeighbourZones() {
		return neighbourZones;
	}

	public void addNeighbourZoneIds(Integer zoneId) {
		if (!neighbourZoneIds.contains(zoneId))
			neighbourZoneIds.add(zoneId);
	}

	public void addNeighbourZone(CsuZone zone) {
		if (zone == null)
			return;
		if (!neighbourZones.contains(zone))
			neighbourZones.add(zone);
	}

	public List<Integer> getNeighbourZoneIds() {
		return neighbourZoneIds;
	}

	public double getZoneValue() {
		return this.zoneValue;
	}

	public double getLocalValue() {
		return this.localValue;
	}

	public double getGlobalValue() {
		return this.globalValue;
	}

	public int getBuildingCount() {
		return this.buildingCount;
	}

	public void setRadiusLength(double radiusLength) {
		this.radiusLength = radiusLength;
	}

	public double getRadiusLength() {
		return this.radiusLength;
	}

	public void addSurroundingRoad(Road road) {
		surroundingRoad.add(road.getID());
	}

	public void addSurroundingRoad(EntityID road) {
		surroundingRoad.add(road);
	}

	public Set<EntityID> getSurroundingRoad() {
		return surroundingRoad;
	}

	public double getDistanceToMapCenter() {
		return this.distanceToMapCenter;
	}

	public Set<Road> getAllEntranceRoad() {
		if (allEntranceRoad == null) {
			allEntranceRoad = new FastSet<>();
			for (CSUBuilding next : this) {
				allEntranceRoad.addAll(world.getEntrance().getEntrance(
						next.getSelfBuilding()));
			}
		}

		return allEntranceRoad;
	}

	public Set<EntityID> getUnvisitBuildings() {
		return unvisitBuildings;
	}

	public void removeVisitedFromUnvisit(Collection<EntityID> visited) {
		this.unvisitBuildings.removeAll(visited);
	}

	public Set<EntityID> getBuriedHumans() {
		return this.buriedHumans;
	}

	public void addBuriedHuman(EntityID... humans) {
		for (EntityID next : humans) {
			this.buriedHumans.add(next);
		}
	}

	public boolean isAllEntranceCleared() {
		return this.unclearedEntrances.isEmpty();
	}

	public boolean containtBuilding(EntityID id) {
		return this.contains(world.getCsuBuilding(id));
	}

	public AdvancedWorldModel getWorld() {
		return world;
	}

	public void setWorld(AdvancedWorldModel world) {
		this.world = world;
	}

	public void setBelongPfClusterIndex(int pfClusterIndex) {
		this.belongPfClusterIndex = pfClusterIndex;
	}

	public int getBelongPfClusterIndex() {
		return this.belongPfClusterIndex;
	}
}