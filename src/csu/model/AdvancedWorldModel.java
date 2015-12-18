package csu.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import javolution.util.FastSet;

import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.Viewer.layers.CSU_BuildingLayer;
import csu.Viewer.layers.CSU_RoadLayer;
import csu.agent.Agent;
import csu.agent.CentreAgent;
import csu.agent.HumanoidAgent;
import csu.common.TimeOutException;
import csu.communication.CivilianVoiceListener;
import csu.geom.CompositeConvexHull;
import csu.geom.PolygonScaler;
import csu.model.flow.EnergyFlow;
import csu.model.object.CSUBuilding;
import csu.model.object.CSUHydrant;
import csu.model.object.CSUWall;
import csu.model.object.CSURoad;
import csu.model.object.csuZoneEntity.CsuZoneFactory;
import csu.model.object.csuZoneEntity.CsuZones;
import csu.model.route.pov.POVRouter;
import csu.standard.Ruler;
import csu.standard.simplePartition.GroupingType;
import csu.standard.simplePartition.Line;

public class AdvancedWorldModel extends StandardWorldModel {
	public static final double MEAN_VELOCITY_OF_MOVING = 31445.392;
	public int REFILL_REFUGE_RATE = 2000;
	protected long thinkStartTime;
	protected String exceptionMessage = "";
	
	// owner agent part
	public Human me = null;					// for platoon agent
	public HumanoidAgent<Human> selfHuman;
	public Building building;				// for center agent
	public CentreAgent<Area> centre;
	public EntityID selfId;
	
	// message handlers
	protected BurningBuildings burningBuildings = null;
	protected ExtinguishedBuilding extinguishBuildings = null;
	protected CollapsedBuildings collapsedBuildings = null;
//	protected StuckHandler stuckHandler = null;
	protected BuriedHumans buriedHumans = null;
	protected RemainCluster remainCluster = null;
	protected SearchedBuildings searchedBuildings = null;
	protected CriticalArea criticalArea = null;
	protected WaterPort waterPort = null;
	
	// map informs
	protected Set<StandardEntity> mapBorderBuildings;
	protected Dimension mapDimension = null;
	protected double mapWidth;
	protected double mapHeight;
	protected double mapDiameter;
	protected boolean isMapHuge = false;
	protected boolean isMapMedium = false;
	protected boolean isMapSmall = false;
	protected Area mapCenter;
	
	// communication conditions
	protected boolean communicationLess = true;
	protected boolean communicationLow = false;
	protected boolean communicationMedium = false;
	protected boolean communicationHigh = false;
	
	protected boolean isNoRadio = false;
	
	// agent informs
	protected List<FireBrigade> fireBrigadeList = new ArrayList<>();
	protected List<PoliceForce> policeForceList = new ArrayList<>();
	protected List<AmbulanceTeam> ambulanceTeamList = new ArrayList<>();
	protected List<FireStation> fireStationList = new ArrayList<>();
	protected List<PoliceOffice> policeOfficeList = new ArrayList<>();
	protected List<AmbulanceCentre> ambulanceCenterList = new ArrayList<>();
	protected List<EntityID> fireBrigadeIdList = new ArrayList<>();
	protected List<EntityID> policeForceIdList = new ArrayList<>();
	protected List<EntityID> ambulanceTeamIdList = new ArrayList<>();
	protected List<EntityID> fireStationIdList = new ArrayList<>();
	protected List<EntityID> policeOfficeIdList = new ArrayList<>();
	protected List<EntityID> ambulanceCenterIdList = new ArrayList<>();
	///
	protected Set<EntityID> stuckedAgentList = new HashSet<>();
	
	// CSUBuilding, and CSURoad part
	protected Map<EntityID, CSUBuilding> csuBuildingMap;
	protected Map<EntityID, CSURoad> csuRoadMap;
	protected Set<CSURoad> csuRoads;
	protected Map<String, Building> buildingXYMap = new FastMap<>();
	
	///Hydrant part
	protected Map<Hydrant, Boolean> hydrantsMap = new FastMap<>();
	protected Map<EntityID, CSUHydrant> csuHydrantMap;
	// others
	// protected RegionModel regionModel;
	// protected Exploration<Building> buildingExploration;
	// protected Set<BuildingInfo> buildingInfos;
	protected int time;
	protected ConfigConstants config = null;
	protected POVRouter router = null;
	protected Entrance entrance = null;
	protected Uniform uniform = null;
	protected TimeStamp timestamp = null;
	protected EnergyFlow energyFlow = null;
	protected IsolatedArea isolated = null;
	protected Set<EntityID> visibleEntity = null;
	protected CsuZones zones = null;
	
	///
		public void markOccupiedHydrant(Hydrant hy) {
				Set<Hydrant> hydrants = hydrantsMap.keySet();
				for(Hydrant next : hydrants) {
					if(next.equals(hy)) {
						hydrantsMap.put(hy, true);
						break;
					}
				}
		}
   ///
		public void markAvailableHydrant(Hydrant hy) {
			Set<Hydrant> hydrants = hydrantsMap.keySet();
			for(Hydrant next : hydrants) {
				if(next.equals(hy)) {
					hydrantsMap.put(hy, false);
					break;
				}
			}
		}
		
	///
		public boolean isHydrantAvailable(Hydrant hy) {
			if(hydrantsMap.get(hy) == false)
				return true;
			else 
				return false;
		}
	
	
/* ----------------------------------- constructor and initialize ----------------------------------------- */
	public AdvancedWorldModel() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public void initialize(Agent<? extends StandardEntity> selfAgent, Config conf, GroupingType type) {
		// others
		this.config = new ConfigConstants(conf, this);
		this.uniform = new Uniform(this);
		this.timestamp = new TimeStamp(this);
		this.router = new POVRouter(this);
		this.energyFlow = new EnergyFlow(this);
        // this.regionModel = new RegionModel(this, type);
        // this.buildingExploration = new Exploration<Building>(this);
		
        // handle owner agent informs
		this.selfId = selfAgent.getID();
		
		// create CSUBuildings and CSURoads
		this.createCsuBuildings();
		this.createCsuRoads();
		this.createCsuHrants();
		
		if (selfAgent instanceof CentreAgent) {
			this.centre = (CentreAgent<Area>)selfAgent;
			this.building = (Building) getEntity(selfAgent.getID());
		} else if (selfAgent instanceof HumanoidAgent) {
			this.selfHuman = (HumanoidAgent<Human>)selfAgent;
			this.me = (Human)getEntity(selfAgent.getID());
			this.isolated = new IsolatedArea(this.me, this);
			this.entrance = new Entrance(this);
		}
		
		
		
		// message handlers
		this.burningBuildings = new BurningBuildings(this);
		this.collapsedBuildings = new CollapsedBuildings(this);
		this.extinguishBuildings = new ExtinguishedBuilding(this);
		this.buriedHumans = new BuriedHumans(this);
		this.waterPort = new WaterPort(this);
//		this.stuckHandler = new StuckHandler(this, 80, 6);
		this.criticalArea = new CriticalArea(this);
		this.searchedBuildings = new SearchedBuildings(this);
		this.remainCluster = new RemainCluster(this);
		
		// handle map informs
		this.mapBorderBuildings = this.findBorderBuilding(0.9, this);
		this.mapDimension = this.calculateMapDimension();
		this.verifyMap();
        
		// get all agents
		for (StandardEntity next : getEntitiesOfType(AgentConstants.PLATOONS)) {
			if (next instanceof FireBrigade) {
				fireBrigadeList.add((FireBrigade) next);
				fireBrigadeIdList.add(next.getID());
			} else if (next instanceof PoliceForce) {
				policeForceList.add((PoliceForce) next);
				policeForceIdList.add(next.getID());
			} else if (next instanceof AmbulanceTeam) {
				ambulanceTeamList.add((AmbulanceTeam) next);
				ambulanceTeamIdList.add(next.getID());
			}
		}
		for (StandardEntity next : getEntitiesOfType(AgentConstants.CENTRES)) {
			if (next instanceof FireStation) {
				fireStationList.add((FireStation) next);
				fireStationIdList.add(next.getID());
			} else if (next instanceof PoliceOffice) {
				policeOfficeList.add((PoliceOffice) next);
				policeOfficeIdList.add(next.getID());
			} else if (next instanceof AmbulanceCentre) {
				ambulanceCenterList.add((AmbulanceCentre) next);
				ambulanceCenterIdList.add(next.getID());
			}
		}
		
		///
		for(StandardEntity next : getEntitiesOfType(AgentConstants.INDEX_CLASS)) {
			if(next instanceof Hydrant)
				hydrantsMap.put((Hydrant) next, false);
		}
		
        // CSUZone part
		if (me instanceof Human) {
			CsuZoneFactory zoneFactory = new CsuZoneFactory(this);
			zones = zoneFactory.createZones();
		}
	}
	
	///oak
		/**
		 * Returns a set of all hydrants' EntityID
		 */
		public List<EntityID> getHydrantSet() {
			List<EntityID> hyID = new ArrayList<>();
			Collection<StandardEntity> hydrants = this.getEntitiesOfType(StandardEntityURN.HYDRANT);
			for(StandardEntity hydrant : hydrants) {
				hyID.add(hydrant.getID());
			}
			return hyID;
		}
		
	
/* ----------------------------------------- update and merge --------------------------------------------- */
	/** Update world model, and be invoked in Agent's prepareForAct() method.*/
	public void update(Human me, ChangeSet changed) throws TimeOutException{
		this.visibleEntity = changed.getChangedEntities();
		
		for (StandardEntity se : getEntitiesOfType(AgentConstants.HUMANOIDS)) {
			Human hm = (Human) se;
			if (me.getPosition(this).equals(hm.getPosition(this))
					&& !changed.getChangedEntities().contains(hm.getID())) {
				hm.undefinePosition();
			}
		}
		
		for (StandardEntity se : getEntitiesOfType(StandardEntityURN.BLOCKADE)) {
			Blockade block = (Blockade) se;
			if (me.getPosition().equals(block.getPosition())
					&& !changed.getChangedEntities().contains(block.getID())) {
				block.undefinePosition();
			}
		}

		this.criticalArea.update(router);
		this.burningBuildings.update(this, changed);
		this.extinguishBuildings.update(this, changed);
		this.waterPort.update(this);
		this.collapsedBuildings.update(this, changed);
		this.buriedHumans.update(changed);
		this.energyFlow.update(changed);
		this.searchedBuildings.update(this, me);
		
		this.burningBuildings.remove();
		this.extinguishBuildings.remove();
		this.collapsedBuildings.remove();
		
		for (EntityID next : changed.getChangedEntities()) {
			StandardEntity entity = getEntity(next);
			if (entity instanceof Road) {
				CSURoad road = getCsuRoad(next);
				road.update();
			}
			
			if(entity instanceof Hydrant) {
				CSUHydrant csuHydrant = getCsuHydrant(next);
				csuHydrant.update();
			}
			
			if (entity instanceof AmbulanceTeam || entity instanceof FireBrigade) {
				if (getAgent().isStucked((Human)entity))
					stuckedAgentList.add(next);
				else
					stuckedAgentList.remove(next);
			}
		}
	}
	
	/**
	 * The merge() method do the following things:
	 * 
	 * <pre>
	 * First, get all entities I can see from ChangeSet. For each visible entity, if it's
	 * not in the collection of entities I can see last cycle, add it. If it is in, update its 
	 * property.
	 * 
	 * Second, delete those entities I can see this cycle from the collection of entities
	 * I can see last cycle.
	 * 
	 * Third, update human rectangles.
	 * </pre>
	 */
	@Override
	public void merge(ChangeSet changeSet) {
		
		List<EntityID> changedEntityID = new ArrayList<EntityID>();
		for (EntityID id : changeSet.getChangedEntities()) {
			if (getEntity(id) == null) {
				changedEntityID.add(id);
			}
		}
		
		super.merge(changeSet);
		if (timestamp != null) {
			timestamp.merge(changeSet);
		}
		for (EntityID id : changedEntityID) {
			StandardEntity se = getEntity(id);
			if (se instanceof Civilian) {
				timestamp.addStateChangeListener(se);
			}
		}
		
		for (EntityID entityId : changeSet.getChangedEntities()) {
			try {
				String entityUrn = changeSet.getEntityURN(entityId);
				if (isBuilding(entityUrn)) {
					Building building = getEntity(entityId, Building.class);
					if (building == null) {
						building = (Building) Registry.getCurrentRegistry().createEntity(entityUrn, entityId);
						this.addEntity(building);
					}
					if (this.me != null && this.me instanceof FireBrigade) {
						updateBuildingFuelForFireBrigade(building);
					}
				}
			} catch (NullPointerException e) {
				System.out.println("NullPointException in AdvancedWorldModel's merge " +
						"method in time: " + time);
				e.printStackTrace();
			} catch (ClassCastException ex) {
				System.out.println("CLassCastException in AdvancedWorldModel's merge method " +
						"in time: " + time);
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Determines whether an entity is a building.
	 * 
	 * @param entityUrn
	 *            the URN of this entity
	 * @return true when this entity is a building. Otherwise, false.
	 */
	private boolean isBuilding(String entityUrn) {
		return entityUrn.equals(StandardEntityURN.BUILDING.toString())
				|| entityUrn.equals(StandardEntityURN.REFUGE.toString())
				|| entityUrn.equals(StandardEntityURN.AMBULANCE_CENTRE.toString())
				|| entityUrn.equals(StandardEntityURN.FIRE_STATION.toString())
				|| entityUrn.equals(StandardEntityURN.POLICE_OFFICE.toString())
				|| entityUrn.equals(StandardEntityURN.GAS_STATION.toString());
	}
	
	/**
	 * Update the building fuel and energy for the given building for
	 * FireBrigade Agent.
	 * 
	 * @param building
	 *            the target building currently within the eye shot of this
	 *            FireBrigade Agent
	 */
	private void updateBuildingFuelForFireBrigade(Building building) {
		CSUBuilding csuBuilding = this.getCsuBuilding(building.getID());
		csuBuilding.setVisible(true);
		if (building.isFierynessDefined() && building.isTemperatureDefined()) {
			int temperature = building.getTemperature();
			csuBuilding.setEnergy(temperature * csuBuilding.getCapacity(), "update changeSet");
			switch (building.getFieryness()) {
			case 0:
				csuBuilding.setFuel(csuBuilding.getInitialFuel());
				if (csuBuilding.getEstimatedTemperature() >= csuBuilding.getIgnitionPoint()) {
					csuBuilding.setEnergy(csuBuilding.getIgnitionPoint() / 2, "update changeSet case 0");
				}
				break;
			case 1:
				if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.66) {
					csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.75));
				} else if (csuBuilding.getFuel() == csuBuilding.getInitialFuel()) {
					csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.90));
				}
				break;
			case 2:
				if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.33
						|| csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.66) {
					csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.50));
				}
				break;
			case 3:
				if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.01
						|| csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.33) {
					csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.15));
				}
				break;
			case 8:
				csuBuilding.setFuel(0);
				break;
			default:
				break;
			}
		}
	}
	
	
/* ----------------------------------- getEntitiesOfType and getEntity ------------------------------------- */
	/**
	 * Returns a collection of entity specified by <b>StandardEntityURN urn<b>.
	 * ANd then cast those entity to the specified type <b>&lt;T extends
	 * StandardEntity&gt;</b>
	 */
	public <T extends StandardEntity> List<T> getEntitiesOfType(Class<T> c, StandardEntityURN urn) {
		Collection<StandardEntity> entities = getEntitiesOfType(urn);
		List<T> list = new ArrayList<T>();
		for (StandardEntity entity : entities) {
			if (c.isInstance(entity)) {
				list.add(c.cast(entity));
			}
		}
		return list;
	}

	/** Get all entities of specified types stores in this world model. */
	public Collection<StandardEntity> getEntitiesOfType(EnumSet<StandardEntityURN> urns) {
		Collection<StandardEntity> res = new HashSet<StandardEntity>();
		for (StandardEntityURN urn : urns) {
			res.addAll(getEntitiesOfType(urn));
		}
		return res;
	}

	/**
	 * Get an object of Entity according to its ID and cast this object to
	 * <b>&lt;T extends StandardEntity&gt;</b>.
	 */
	public <T extends StandardEntity> T getEntity(EntityID id, Class<T> c) {
		StandardEntity entity;
		entity = getEntity(id);
		if (c.isInstance(entity)) {
			T castedEntity = c.cast(entity);

			return castedEntity;
		} else {
			return null;
		}
	}

	public CSUBuilding getCsuBuilding(StandardEntity entity) {
		return this.csuBuildingMap.get(entity.getID());
	}

	public CSUBuilding getCsuBuilding(EntityID entityId) {
		return this.csuBuildingMap.get(entityId);
	}

//	///
//	public EntityID getID(CSUBuilding csuBuilding) {
//		Set<EntityID> ids = this.csuBuildingMap.keySet();
//		for(EntityID id : ids) {
//			if(this.csuBuildingMap.get(id).hashCode() == csuBuilding.hashCode())
//				return id;
//		}
//		return null;
//	}
	public CSURoad getCsuRoad(StandardEntity entity) {
		return this.csuRoadMap.get(entity.getID());
	}

	public CSURoad getCsuRoad(EntityID entityId) {
		return this.csuRoadMap.get(entityId);
	}

	public Collection<CSUBuilding> getCsuBuildings() {
		return this.csuBuildingMap.values();
	}
	
	public Map<EntityID, CSUBuilding> getCsuBuildingMap() {
		return csuBuildingMap;
	}
	
    public Set<CSURoad> getCsuRoads() {
    	return this.csuRoads;
    }
    
    public Map<EntityID, CSURoad> getCSURoadMap() {
    	return csuRoadMap;
    }
    
    ///oak
    public CSUHydrant getCsuHydrant(EntityID entityid) {
    	return this.csuHydrantMap.get(entityid);
    }
    
/* ---------------------------------------------------------------------------------------------------------- */
    /**
     * This method used to find all border building of this world.
     * 
     * @return a set of border building of this world
     */
    private Set<StandardEntity> findBorderBuilding (double scale, AdvancedWorldModel world) {
    	CompositeConvexHull convexHull = new CompositeConvexHull();
    	Set<StandardEntity> allEntities = new FastSet<StandardEntity>();
    	for (StandardEntity entity : world) {
			if (entity instanceof Building) {
				allEntities.add(entity);
				Pair<Integer, Integer> location = entity.getLocation(world);
				convexHull.addPoint(location.first(), location.second());
			}
    	}
  
    	Set<StandardEntity> borderBuilding = 
    			PolygonScaler.getMapBorderBuildings(convexHull, allEntities, scale, world);
    	return borderBuilding;
    }
    
//    /**
//     * Remove the map border buildings from all building set to save the computing time.
//     */
//    private void removeBorderBuilding() {
//    	for (StandardEntity next : borderBuildings) {
//    		if (next instanceof Building) {
//    			csuBuildingsCopy.remove(this.getCsuBuilding(next.getID()));
//    		}
//    	}
//    }
    
	/**
	 * Calculate the dimension of current map. The result dimension is slightly
	 * smaller than the real dimension. But it can save lots of computing time.
	 */ ///why
    private Dimension calculateMapDimension() {
    	int minX = Integer.MAX_VALUE;
    	int maxX = Integer.MIN_VALUE;
    	int minY = Integer.MAX_VALUE;
    	int maxY = Integer.MIN_VALUE;
        Pair<Integer, Integer> pos;
        for (StandardEntity standardEntity : this.getAllEntities()) {
            pos = standardEntity.getLocation(this);
            if (pos.first() < minX)
                minX = pos.first();
            if (pos.second() < minY)
                minY = pos.second();
            if (pos.first() > maxX)
                maxX = pos.first();
            if (pos.second() > maxY)
                maxY = pos.second();
        }
        Dimension mapDimension = new Dimension(maxX - minX, maxY - minY);
        this.mapWidth = mapDimension.getWidth();
        this.mapHeight = mapDimension.getHeight();
        
        return mapDimension;
    }
    
    /**
     * Thie method determines whether current map is huge, medium or small.
     */
    private void verifyMap() {
    	double mapWidth = this.getMapDimension().getWidth();
    	double mapHeight = this.getMapDimension().getHeight();
    	double mapDiagonalLength = Math.hypot(mapWidth, mapHeight);
    	double rate = mapDiagonalLength / MEAN_VELOCITY_OF_MOVING;
    	if (rate > 60) {
    		this.isMapHuge = true;
    	} else if (rate > 30) {
    		this.isMapMedium = true;
    	} else {
    		this.isMapSmall = true;
    	}
    }
    
    private void calculateMapDiameter() {
    	this.mapDiameter = Math.sqrt(Math.pow(this.mapWidth, 2.0) + Math.pow(this.mapHeight, 2.0));
    }
    public double getMapDiameter() {
    	if (mapDiameter == 0)
    		this.calculateMapDiameter();
    	return this.mapDiameter;
    }
    
    public boolean isMapHuge() {
    	return this.isMapHuge;
    }
    public boolean isMapMedium() {
    	return this.isMapMedium;
    }
    public boolean isMapSmall() {
    	return this.isMapSmall;
    }
    
    /** Find the center area of current map.*/
    public Area getMapCenter() {
    	if (this.mapCenter != null) {
    		return this.mapCenter;
    	}
    	
    	double ret;
        int min_x = Integer.MAX_VALUE;
        int max_x = Integer.MIN_VALUE;
        int min_y = Integer.MAX_VALUE;
        int max_y = Integer.MIN_VALUE;

        Collection<StandardEntity> areas = getEntitiesOfType(AgentConstants.AREAS);

        long x = 0, y = 0;
        Area result;

        for (StandardEntity entity : areas) {
            Area area1 = (Area) entity;
            x += area1.getX();
            y += area1.getY();
        }

        x /= areas.size();
        y /= areas.size();
        result = (Area) areas.iterator().next();
        for (StandardEntity entity : areas) {
            Area temp = (Area) entity;
            double a = Ruler.getDistance((int) x, (int) y, result.getX(), result.getY());
            double b = Ruler.getDistance((int) x, (int) y, temp.getX(), temp.getY());
            if (a > b) { ///result is the nearest actual area to calculated center
                result = temp;
            }

            if (temp.getX() < min_x) {
                min_x = temp.getX();
            } else if (temp.getX() > max_x)
                max_x = temp.getX();

            if (temp.getY() < min_y) {
                min_y = temp.getY();
            } else if (temp.getY() > max_y)
                max_y = temp.getY();
        }
        ret = (Math.pow((min_x - max_x), 2) + Math.pow((min_y - max_y), 2));
        ret = Math.sqrt(ret);
        this.mapCenter = result;

        return result;
    }
    ///got from entities' locations
    public Point getMapCenterPoint() {    ///should be >> 1
    	return new Point((int)mapWidth >> 2, (int)mapHeight >> 2);
    }
    
    public List<Building> findNearBuildings(Building centerbuilding, int distance) {
        List<Building> result;
        Collection<StandardEntity> allObjects;
        int radius;

        Rectangle rect = centerbuilding.getShape().getBounds();
        radius = (int) (distance + rect.getWidth() + rect.getHeight());

        allObjects = getObjectsInRange(centerbuilding, radius);
        result = new ArrayList<Building>();
        for (StandardEntity next : allObjects) {
            if (next instanceof Building) {
                Building building;

                building = (Building) next;
                if (!building.equals(centerbuilding)) {
                    if (Ruler.getDistance(centerbuilding, building) < distance) {
                        result.add(building);
                    }
                }
            }
        }
        return result;
    }
    
/* -------------------------------------------------------------------------------------------------------- */
    ///what occurs using Point
    public int distance(java.awt.Point p1, java.awt.Point p2) {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    public int distance(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (int) Math.hypot(dx, dy);
    }
   
    public Line getLine(Area node1, Area node2) {
        int x1 = node1.getX();
        int y1 = node1.getY();
        int x2 = node2.getX();
        int y2 = node2.getY();
        return new Line(x1, y1, x2, y2);
    }
    
    public Area getPositionFromCoordinates(int x, int y) {
		for (StandardEntity entity : getObjectsInRange(x, y, 1000)) {
			if (entity instanceof Area) {
				Area area = (Area) entity;
				if (area.getShape().contains(x, y)) {
					return area;
				}
			}
		}
		return null;
	}
    
    public StandardEntity getPositionFromCoordinates(Pair<Integer, Integer> coordinate) {
		return getPositionFromCoordinates(coordinate.first(), coordinate.second());
	}
    
    public Area getNearestRoad(int x, int y) {
		Area result = getPositionFromCoordinates(x, y);
		if (result != null) 
			return result;
		
		int l = 1;
		int diameter = (int) Math.hypot(
				Math.max(config.MAX_X, x) - Math.min(config.MIN_X, x),
				Math.max(config.MAX_Y, y) - Math.min(config.MIN_Y, y));
		Collection<StandardEntity> minRange = null;
		while (diameter - l > 1000) {
			int m = (diameter + l) / 2;   ///why, but m is smaller, more impossible to have objects in range
			Collection<StandardEntity> range = getObjectsInRange(x, y, m);
			if (0 < range.size()) {
				diameter = m;
				if (minRange == null || minRange.size() < range.size()) {
					minRange = range;
				}
			} else {
				l = m;
			}
		}
		if (minRange == null) 
			return null;
		double minDist = Double.MAX_VALUE;
		for (StandardEntity se : minRange) {
			if (se instanceof Area) {
				Area area = (Area) se;
				double dist = Math.hypot(area.getX() - x, area.getY() - y);
				if (dist < minDist) {
					minDist = dist;
					result = area;
				}
			}
		}
		return result;
	}
    
/* -------------------------------------------------------------------------------------------------------- */
    
    public int getDeathTime(Human human) {
		if (human.isDamageDefined()) {
			if (human.getDamage() == 0) {
				return getConfig().timestep + 1;
			}
			else {
				int lastChangedTime = timestamp.getLastChangedTime(human.getID());
				if (lastChangedTime < 0) {
					lastChangedTime = time;
				}
				if (human.isHPDefined()) {   ///where to get the formula
					return lastChangedTime + human.getHP() / human.getDamage() - 1;
				}
				else {
					return getConfig().timestep + 1;
				}
			}
		}
		return getConfig().timestep + 1;
	}
    
	/**
	 * Get the Agent this world model belongs to.
	 * 
	 * @return the Agent who own this world model
	 */
    public Agent<? extends StandardEntity> getAgent() {
    	if (this.selfHuman != null)
    		return this.selfHuman;
    	if (this.centre != null)
    		return this.centre;
    	return null;
    }
    
	/**
	 * Get the controlled entity of this world model's owner Agent
	 * 
	 * @return the entity controlled by this world model's owner Agent
	 */
    public StandardEntity getControlledEntity() {
    	if (this.me != null)
    		return this.me;
    	if (this.building != null)
    		return this.building;
    	return null;
    }

//    public BuildingInfo getBuildingInfo(EntityID id) {
//		BuildingInfo info;
//
//		info = buildingInfoMap.get(id);
//		if (info == null) {
//			Building building;
//			building = getEntity(id, Building.class);
//			if (building != null) {
//				info = new BuildingInfo(building, null, time);
//				buildingInfoMap.put(id, info);
//			}
//		}
//		return info;
//	}
    
	public Set<EntityID> getVisibleEntitiesID() {
		return visibleEntity;
	}
	
	public boolean isVisible(EntityID id) {
		return getVisibleEntitiesID().contains(id);
	}
	
	protected boolean isVisible(StandardEntity entity) {
		return isVisible(entity.getID());
	}
	
   
	/* -------------------------------------------------------------------------------------------------------- */
	public Set<StandardEntity> getNeighbours(StandardEntity e, int Ext){
		Set<StandardEntity> Neighbours = new HashSet<StandardEntity>();
		if (e instanceof Building){
			for(EntityID tmp2:((Building) e).getNeighbours()){
				Neighbours.add(getEntity(tmp2));
			}
			if(((Building) e).isEdgesDefined()){
				Ext*=1000;
				List<Edge> Edges = new ArrayList<Edge>();
				Edges=((Building) e).getEdges();
				Polygon ExtArea =new Polygon();
				Polygon baseArea = new Polygon();
				int n=1;
				Point2D.Double tmp1=new Point2D.Double();
				Point2D.Double tmp2=new Point2D.Double();
				Point2D.Double tmp3=new Point2D.Double();
				Point2D.Double tmp4=new Point2D.Double();
				Point2D.Double tmp5=new Point2D.Double();
				for(Edge Ee : Edges){baseArea.addPoint(Ee.getStartX(),Ee.getStartY());}
				for(Edge Ee : Edges)
				{
					for(Edge Ee1:Edges){
						if(Ee.getStart().equals(Ee1.getEnd())){
							tmp1.setLocation
							(
									Ee.getStartX()+(n*(Ee.getEndX()-Ee.getStartX()))/Math.hypot((double)Ee.getEndX()-(double)Ee.getStartX() , (double)Ee.getEndY()-(double)Ee.getStartY() )
									,
									Ee.getStartY()+(n*(Ee.getEndY()-Ee.getStartY()))/Math.hypot((double)Ee.getEndX()-(double)Ee.getStartX() , (double)Ee.getEndY()-(double)Ee.getStartY() )
							);
							tmp2.setLocation
							(
									Ee1.getEndX()+(n*(Ee1.getStartX()-Ee1.getEndX()))/Math.hypot((double)Ee1.getStartX()-(double)Ee1.getEndX() , (double)Ee1.getStartY()-(double)Ee1.getEndY() )
									,
									Ee1.getEndY()+(n*(Ee1.getStartY()-Ee1.getEndY()))/Math.hypot((double)Ee1.getStartX()-(double)Ee1.getEndX() , (double)Ee1.getStartY()-(double)Ee1.getEndY() )
							);

							tmp3.setLocation
							(
									(tmp1.x+tmp2.x)/2
									,
									(tmp1.y+tmp2.y)/2
							);
							tmp4.setLocation(Ee.getStartX(), Ee.getStartY());
							if(tmp3.x==tmp4.x&&tmp3.y==tmp4.y){continue;}
							else if(baseArea.contains(tmp3)){
								tmp5.setLocation(
										tmp4.x+(Ext*(tmp4.x-tmp3.x)/Math.hypot(tmp4.x-tmp3.x,tmp4.y-tmp3.y))
										,
										tmp4.y+(Ext*(tmp4.y-tmp3.y)/Math.hypot(tmp4.x-tmp3.x, tmp4.y-tmp3.y)));

							}
							else if (!baseArea.contains(tmp3)){
								tmp5.setLocation(
										tmp4.x+(Ext*(tmp3.x-tmp4.x)/Math.hypot(tmp3.x-tmp4.x,tmp3.y-tmp4.y))
										,
										tmp4.y+(Ext*(tmp3.y-tmp4.y)/Math.hypot(tmp3.x-tmp4.x, tmp3.y-tmp4.y)));
							}
							ExtArea.addPoint((int)tmp5.x, (int)tmp5.y);
						}
					}
				}
				for(StandardEntity checker:getObjectsInRange(e, 100*1000)){
					if(checker instanceof Building){
						if(((Building)checker).isEdgesDefined()){
							for(Edge Edger:((Building)checker).getEdges()){
								if(ExtArea.contains(Edger.getStartX(),Edger.getStartY())){
									Neighbours.add(checker);
								}
								else	if(ExtArea.contains((Edger.getStartX()+Edger.getEndX())/2,(Edger.getStartY()+Edger.getEndY())/2)){
									Neighbours.add(checker);
								}
							}
						}
					}
				}
			}
		}
		else if (e instanceof Road){
			for(EntityID Neighs:((Road) e).getNeighbours()){
				if(getEntity(Neighs) instanceof Road){Neighbours.add(getEntity(Neighs));}
				else if (getEntity(Neighs) instanceof Building){Neighbours.add(getEntity(Neighs));}
			}
		}
		return Neighbours;
	}
	
	
/* ------------------------------------------- handle civilians ------------------------------------------- */
	public CivilianVoiceListener createNewCivilianListener() {
		return new CivilianVoiceListener() {
			@Override
			public void hear(AKSpeak message) {
				final EntityID id = message.getAgentID();
				addNewCivilian(id);
			}
		};
	}
	
	public Civilian addNewCivilian(EntityID id) {
		StandardEntity existing = getEntity(id);
		if (existing == null) {
			Civilian civilian = new Civilian(id);
			timestamp.addStateChangeListener(civilian);
			addEntity(civilian);
			return civilian;
		}
		return null;
	}
    
	public Building getBuildingInPoint(int x, int y) {
		String xy = x + "," + y;
		return buildingXYMap.get(xy);
	}
   
	
	
	
/* ------------------------------------------- CSUBuilding part -------------------------------------------- */
	
	/**
	 * Create {@link CSUBuilding} object for this world model.
	 */
	private void createCsuBuildings() {
		this.csuBuildingMap = new FastMap<EntityID, CSUBuilding>();
		
		for (StandardEntity entity : this.getEntitiesOfType(AgentConstants.BUILDINGS)) {
			CSUBuilding csuBuilding;
			Building building = (Building) entity;
			String xy = building.getX() + "," + building.getY();
			buildingXYMap.put(xy, building);
			
			csuBuilding = new CSUBuilding(entity, this);
			
			if (entity instanceof Refuge || entity instanceof PoliceOffice
					|| entity instanceof FireStation || entity instanceof AmbulanceCentre)
				csuBuilding.setInflammable(false);
			this.csuBuildingMap.put(building.getID(), csuBuilding);
		}
		
		for (CSUBuilding next : csuBuildingMap.values()) {
			Collection<StandardEntity> neighbour = getObjectsInRange(next.getId(), CSUWall.MAX_SAMPLE_DISTANCE);
			
			for (StandardEntity entity : neighbour) {
				if (entity instanceof Building) {
					next.addNeighbourBuilding(this.csuBuildingMap.get(entity.getID()));
				}
			}
		}
		
		if (AgentConstants.LAUNCH_VIEWER && CSU_BuildingLayer.CSU_BUILDING_MAP.isEmpty()) {
			CSUBuilding.VIEWER_BUILDING_MAP.put(selfId, csuBuildingMap);
			CSU_BuildingLayer.CSU_BUILDING_MAP.putAll(csuBuildingMap);
		}
	}
	
	/**
	 * Create {@link CSURoad} object for this world model.
	 */
	private void createCsuRoads() {
		this.csuRoadMap = new FastMap<>();
		this.csuRoads = new FastSet<>();
		
		CSURoad csuRoad;
		Road road;
		
		for (StandardEntity entity : this.getEntitiesOfType(AgentConstants.ROADS)){
			road = (Road)entity;
			csuRoad = new CSURoad(road, this);
			this.csuRoadMap.put(entity.getID(), csuRoad);
			this.csuRoads.add(csuRoad);
		}
		
		if (AgentConstants.LAUNCH_VIEWER) {
			if (this.selfId.getValue() == 485278126)  ///
		 		CSU_RoadLayer.CSU_ROAD_MAP.putAll(csuRoadMap);
		}
	}
	
	/**
	 * Create {@link CSUHydrant} object for this world model.
	 * oak
	 */
	private void createCsuHrants() {
		this.csuHydrantMap = new FastMap<>();
		CSUHydrant csuHydrant;
		for (StandardEntity entity : this.getEntitiesOfType(AgentConstants.HYDRANTS)){
			csuHydrant = new CSUHydrant(entity.getID(), this);
			this.csuHydrantMap.put(entity.getID(), csuHydrant);
		}
	}
	
/* ----------------------------------------- getters and setters ------------------------------------------- */

//	public void addBuildingInfo(BuildingInfo info) {
//		this.buildingInfos.add(info);
//	}
//	
//	public RegionModel getRegionModel() {
//        return regionModel;
//    }
//	public void setRegionModel(RegionModel regionModel) {
//        this.regionModel = regionModel;
//    }
//   
//    public Exploration<Building> getBuildingExploration() {
//        return this.buildingExploration;
//    }
   
	public ConfigConstants getConfig() {
		return config;
	}
	
	public POVRouter getRouter() {
		return router;
	}

	public Entrance getEntrance() {
		return entrance;
	}
	
	public int getTime() {
		return time;
	}

	public Uniform getUniform() {
		return uniform;
	}
	
	public BurningBuildings getBurningBuildings() {
		return this.burningBuildings;
	}
	
	public CollapsedBuildings getCollapsedBuildings() {
		return this.collapsedBuildings;
	}
	
	public ExtinguishedBuilding getExtinguishedBuildings() {
		return this.extinguishBuildings;
	}
	
	/**
	 * Get all border building of this world.
	 * 
	 * @return all border building of this world
	 */
	public Set<StandardEntity> getBorderBuildings() {
		return this.mapBorderBuildings;
	}

//	public StuckHandler getStuckHandle() {
//		return stuckHandler;
//	}
	
	public BuriedHumans getBuriedHumans() {
		return buriedHumans;
	}
	
	public SearchedBuildings getSearchedBuildings() {
		return searchedBuildings;
	}
	public void setSearchBuildings(SearchedBuildings searchBuildings) {
		this.searchedBuildings = searchBuildings;
	}
	
	public CriticalArea getCriticalArea() {
		return criticalArea;
	}
	
	public EnergyFlow getEnergyFlow() {
		return energyFlow;
	}
	
	public IsolatedArea getIsolated() {
		return isolated;
	}

	public TimeStamp getTimestamp() {
		return timestamp;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public Dimension getMapDimension() {
		return this.mapDimension;
	}
	public double getMapWidth() {
		return this.mapWidth;
	}
	public double getMapHeight() {
		return this.mapHeight;
	}
	
	public boolean isCommunicationLess() {
		return this.communicationLess;
	}
	public void setCommunicationLess(boolean communicationLess) {
		this.communicationLess = communicationLess;
	}
	
	public boolean isCommunicationLow() {
		return this.communicationLow;
	}
	public void setCommunicationLow(boolean communicationLow) {
		this.communicationLow = communicationLow;
	}
	
	public boolean isCommunicationMedium() {
		return this.communicationMedium;
	}
	public void setCommunicationMedium(boolean communicationMedium) {
		this.communicationMedium = communicationMedium;
	}
	
	public boolean isCommunicationHigh() {
		return this.communicationHigh;
	}
	public void setCommunicationHigh(boolean communicationHigh) {
		this.communicationHigh = communicationHigh;
	}
	
	public WaterPort getWaterPort() {
		return this.waterPort;
	}

	public List<FireBrigade> getFireBrigadeList() {
		return fireBrigadeList;
	}

	public List<PoliceForce> getPoliceForceList() {
		return policeForceList;
	}
	
	public List<AmbulanceTeam> getAmbulanceTeamList() {
		return ambulanceTeamList;
	}

	public List<FireStation> getFireStationList() {
		return fireStationList;
	}

	public List<PoliceOffice> getPoliceOfficeList() {
		return policeOfficeList;
	}

	public List<AmbulanceCentre> getAmbulanceCenterList() {
		return ambulanceCenterList;
	}
	
	public List<EntityID> getFireBrigadeIdList() {
		return fireBrigadeIdList;
	}
	
	public List<EntityID> getPoliceForceIdList() {
		return policeForceIdList;
	}

	public List<EntityID> getAmbulanceTeamIdList() {
		return ambulanceTeamIdList;
	}

	public List<EntityID> getFireStationIdList() {
		return fireStationIdList;
	}

	public List<EntityID> getPoliceOfficeIdList() {
		return policeOfficeIdList;
	}
	
	public List<EntityID> getAmbulanceCenterIdList() {
		return ambulanceCenterIdList;
	}
	
	public Set<EntityID> getStuckedAgents() {
		return this.stuckedAgentList;
	}

	public StandardEntity getSelfPosition() {
		if (building != null)
			return building;
		else {
			return me.getPosition(this);
		}
	}
	
	public Pair<Integer, Integer> getSelfLocation() {
        if (me != null) {
            return me.getLocation(this);
        } else {
            return building.getLocation(this);
        }
    }
	
	public void setThinkStartTime(long thinkStartTime) {
		this.thinkStartTime = thinkStartTime;
	}
	
	public long getThinkStartTime() {
		return thinkStartTime;
	}
	
	public void setExceptionMessage(String message) {
		this.exceptionMessage = message;
	}
	public String getExceptionMessage() {
		return this.exceptionMessage;
	}
	
	public RemainCluster getRemainCluster(){
		return this.remainCluster;
	}
	
	public CsuZones getZones() {
		return zones;
	}
	
	public boolean isNoRadio() {
		return this.isNoRadio;
	}
	
	
	
	public void setNoRadio(boolean noRadio) {
		this.isNoRadio = noRadio;
	}
}
