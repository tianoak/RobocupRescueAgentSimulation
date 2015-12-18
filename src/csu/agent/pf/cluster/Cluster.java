package csu.agent.pf.cluster;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import csu.geom.CompositeConvexHull;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBuilding;
import csu.model.object.csuZoneEntity.CsuZone;

/**
 * The class represents the cluster generate using KmeansPlusPlus
 * 
 * @author nale
 * 
 */
public class Cluster {

	/** The list of CsuZone belongs to this cluster */
	private List<CsuZone> zoneList;

	/** A reference of world model. */
	private AdvancedWorldModel world;

	/** The coordinate of the centroid of this cluster. */
	private Point centroid;

	/** list of agents assigned to this cluster */
	private List<EntityID> agents;

	/** The list of road belongs to this cluster */
	private List<Road> roadList;

	/** The list of entrance belongs to this cluster, the entrance has blockade */
	private List<Road> entranceList;

	/** The list of refuge belongs to this cluster */
	private List<Refuge> refugeList;

	/** The polygon of this cluster. */
	private Polygon clusterPolygon;
	
	private Set<Integer> neighbourCluster;

	/** The surroungding road of this cluster */
	private Set<Road> surroundingRoadList;
	
	private Set<Area> criticalAreaList;
	
	public Cluster(ArrayList<CsuZone> zoneList, Point centroid) {
		if (zoneList == null)
			this.zoneList = new ArrayList<CsuZone>();
		else
			this.zoneList = zoneList;
		this.centroid = centroid;
		agents = new ArrayList<EntityID>();

		entranceList = new ArrayList<Road>();
		refugeList = new ArrayList<Refuge>();
		setRoadList(new ArrayList<Road>());
		surroundingRoadList = new HashSet<Road>();
		criticalAreaList = new HashSet<Area>();
		
		this.neighbourCluster = new TreeSet<>();
	}

	public void initialize() {
		initEntranceList();			
		initRefuge();	
		initSurroundingRoad();
		initCriticalArea();
		createClusterPolygon();
	}

	public void testDate() {
		System.out.println("entranceList:");
		for (Road next : entranceList) {
			System.out.println(next);
		}
		System.out.println("refugeList:");
		for (Refuge next : refugeList) {
			System.out.println(next);
		}
	}

	public void initEntranceList() {
		for (CsuZone zone : zoneList) {
			entranceList.addAll(zone.getAllEntranceRoad());
		}
	}

	/**
	 * @param EntityID
	 * @return 判断建筑是属于cluster
	 */
	public boolean containtBuilding(EntityID id) {
		for (CsuZone zone : zoneList) {
			if (zone.contains(world.getCsuBuilding(id))) {
				return true;
			}
		}
		return false;
	}

	public void initRefuge() {
		Collection<StandardEntity> allRefugeList = world
				.getEntitiesOfType(StandardEntityURN.REFUGE);

		for (StandardEntity standardEntity : allRefugeList) {
			EntityID entityID = standardEntity.getID();
			if (containtBuilding(entityID)) {
				
				refugeList.add((Refuge)standardEntity);
			}
		}
		
		// System.out.println("test if there are refuges " + refugeList.size() );
	}

	public void initSurroundingRoad() {
		surroundingRoadList.addAll(entranceList);

		Set<Road> neighborsOfEntrances = new HashSet<Road>();
		for (Road next1 : entranceList) {
			// neighbors of the entrance
			for (EntityID next2 : next1.getNeighbours()) {
				StandardEntity entity = world.getEntity(next2);

				if (entity instanceof Road) {
					Road road = (Road) entity;
					neighborsOfEntrances.add(road);
					// neighbors of neighbors
					for (EntityID next3 : road.getNeighbours()) {
						StandardEntity entity2 = world.getEntity(next3);
						if (entity2 instanceof Road) {
							neighborsOfEntrances.add((Road) entity2);
						}
					}
				}
			}
		}

		surroundingRoadList.addAll(neighborsOfEntrances);
	}
	
	private void initCriticalArea() {
		for (CsuZone next : zoneList) {
			for (EntityID road : next.getSurroundingRoad()) {
				Area area = world.getEntity(road, Area.class);
				if (world.getCriticalArea().isCriticalArea(area)) {
					this.criticalAreaList.add(area);
				}
			}
		}
	}

	
	private void createClusterPolygon() {
		try {
			CompositeConvexHull convexHull = new CompositeConvexHull();

			for (Road next : surroundingRoadList) {
				int[] apexList = next.getApexList();
				for (int i = 0; i < apexList.length; i = i + 2) {
					convexHull.addPoint(apexList[i], apexList[i + 1]);
				}
			}
			for (CsuZone next : this.getZoneList()) {
				for (CSUBuilding next1 : next) {
					int[] apexList = next1.getSelfBuilding().getApexList();
					for (int i = 0; i < apexList.length; i = i + 2) {
						convexHull.addPoint(apexList[i], apexList[i + 1]);
					}
				}
			}

			clusterPolygon = convexHull.getConvexPolygon();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public List<Road> getEntranceList() {
		return entranceList;
	}

	public void setEntranceList(ArrayList<Road> entranceList) {
		this.entranceList = entranceList;
	}

	public List<CsuZone> getZoneList() {
		return this.zoneList;
	}

	public Point getCentroid() {
		return this.centroid;
	}

	public List<EntityID> getAgents() {
		return this.agents;
	}

	public void setCentroid(Point centroid) {
		this.centroid = centroid;
	}

	public AdvancedWorldModel getWorld() {
		return world;
	}

	public void setWorldModel(AdvancedWorldModel world) {
		this.world = world;
	}

	public List<Refuge> getRefugeList() {
		return refugeList;
	}

	public void setRefugeList(ArrayList<Refuge> refugeList) {
		this.refugeList = refugeList;
	}

	public Polygon getClusterPolygon() {
		return clusterPolygon;
	}

	public void setClusterPolygon(Polygon clusterPolygon) {
		this.clusterPolygon = clusterPolygon;
	}
	
	public Set<Area> getCriticalAreas() {
		return criticalAreaList;
	}

	public List<Road> getRoadList() {
		return roadList;
	}

	public void setRoadList(List<Road> roadList) {
		this.roadList = roadList;
	}
	
	public void addNeighbours(Integer neig_index) {
		this.neighbourCluster.add(neig_index);
	}
	
	public Set<Integer> getNeighbours() {
		return this.neighbourCluster;
	}
}
