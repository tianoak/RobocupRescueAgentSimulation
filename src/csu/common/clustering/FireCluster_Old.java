//package csu.common.clustering;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import csu.model.AdvancedWorldModel;
//import csu.standard.BuildingInfo;
//import csu.standard.BuildingInfo.BUILDING_CONDITION;
//import csu.standard.Ruler;
//import csu.util.AddAndRemove;
//
//import rescuecore2.standard.entities.Area;
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.Human;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityConstants;
//import rescuecore2.standard.entities.StandardWorldModel;
//import rescuecore2.worldmodel.EntityID;
//
//public class FireCluster_Old {
//	
//	/** The X and Y coordinate of this cluster's center.*/
//	public double[] centroid;
//	/** The target whose center is the closest to  the cluster's centroid.*/
//	public Area center;
//	/** The radius of this FireCluster.*/
//	public double radius;
//	/** All Buildings in this fire cluster.*/
//	private ArrayList<BuildingInfo> cluster = new ArrayList<BuildingInfo>();
//	
//	/** A list of Building with Fireyness UNBURNT.*/
//	private ArrayList<Building> buildingsUnburnt = new ArrayList<Building>();
//	/** A list of warm Building.*/
//	private ArrayList<Building> buildingsWarm = new ArrayList<Building>();
//	/** A list of Buildings with Fireyness HEATING.*/
//	private ArrayList<Building> buildingsHeating = new ArrayList<Building>();
//	/** A list of Buildings with Fireyness BURNING.*/
//	private ArrayList<Building> buildingsBurning = new ArrayList<Building>();
//	/** A list of Buildings with Fireyness INFERNO.*/
//	private ArrayList<Building> buildingsInferno = new ArrayList<Building>();
//	/** A list of extinguished Buildings.*/
//	private ArrayList<Building> buildingsExtinguished = new ArrayList<Building>();
//	/** A list of collapsed Buildings.*/
//	private ArrayList<Building> buildingCollapsed = new ArrayList<Building>();
//	/** A list of burning expected to extinguish.*/
//	private ArrayList<Building> buildingsExpectedToExtinguish = new ArrayList<Building>();
//	
//	private AdvancedWorldModel world;
//	
//	// constructor
//	public FireCluster_Old(double[] centroid, StandardEntity center, AdvancedWorldModel world){
//		this.world = world;
//		
//		this.centroid = centroid;
//		this.center = (Area)center;
//		if (centroid == null && center != null){
//			this.centroid = new double[2];
//			this.centroid[0] = this.center.getX();
//			this.centroid[1] = this.center.getY();
//		}
////		System.out.println("New FireCluster created which center is: " + this.center);
//	}
//	
//	/**
//	 * Add a new Building to this FireCluster.
//	 * 
//	 * @param b
//	 *            target Building
//	 * @param time
//	 *            the time target was added
//	 * @param condition
//	 *            the condition of this Building
//	 * @return true when add a new Building to this FireCluster, false when not
//	 */
//	public boolean addNewTarget(Building b, int time, BUILDING_CONDITION condition){
//		BuildingInfo bi = this.getBuildingInfoFromList(b.getID(), cluster);
//		if (bi != null && bi.time > time)
//			return false;
//		switch (condition) {
//		case BUILDING_UNBURNT:
//			return addUnburntBuilding(null, b, time);
//		case BUILDING_WARM:
//			return addWarmBuilding(null, b, time);
//		case BUILDING_ON_FIRE:
//			return addFireBuilding(null, b, time);
//		case BUILDING_EXTINGUISHED:
//			return addExtinguishedBuilding(null, b, time);
//		case BUILDING_COLLAPSED:
//			return addCollapsedBuilding(null, b, time);
//		default:
//			return false;
//		}
//	}
//	
//	public boolean addUnburntBuilding(BuildingInfo bi, Building b, int time){
//		boolean flag = true;
//		if (bi != null){
//			switch (bi.getBuildingCondition()) {
//			case BUILDING_WARM: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsUnburnt);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsWarm);
//			};
//			    break;
//			case BUILDING_UNBURNT:
//			case BUILDING_ON_FIRE:
//			case BUILDING_EXTINGUISHED:
//			case BUILDING_COLLAPSED:
//				flag = false;
//				break;
//			default:
//				break;
//			}
//			bi.updateProperties(BUILDING_CONDITION.BUILDING_UNBURNT, time);
//		} else {
//			flag = true;
//			bi = new BuildingInfo(b, BUILDING_CONDITION.BUILDING_UNBURNT, time);
//			world.addBuildingInfo(bi);
//			AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsUnburnt);
//			AddAndRemove.addIfNotExists(bi, cluster);
//		}
//		return flag;
//	}
//	
//	public boolean addWarmBuilding(BuildingInfo bi, Building b, int time){
//		boolean flag = true;
//		if (bi != null){
//			switch (bi.getBuildingCondition()) {
//			case BUILDING_UNBURNT: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsWarm);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsUnburnt);
//			};			
//				break;
//			case BUILDING_WARM :
//				flag = false;
//				break;
//			case BUILDING_ON_FIRE: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsWarm);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//			};
//			    break;
//			case BUILDING_EXTINGUISHED: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsWarm);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsExtinguished);
//			}
//			case BUILDING_COLLAPSED:
//				return false;
//			default:
//				break;
//			}
//			bi.updateProperties(BUILDING_CONDITION.BUILDING_WARM, time);
//		} else {
//			flag = true;
//			bi = new BuildingInfo(b, BUILDING_CONDITION.BUILDING_WARM, time);
//			world.addBuildingInfo(bi);
//			AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsWarm);
//			AddAndRemove.addIfNotExists(bi, cluster);
//		}
//		return flag;
//	}
//	
//	public boolean addFireBuilding(BuildingInfo bi, Building b, int time){
//		boolean flag = true;
//		if (bi != null) {
//			switch (bi.getBuildingCondition()) {
//			case BUILDING_UNBURNT: {
//				switch (bi.getBuilding().getFierynessEnum()) {
//				case HEATING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//					break;
//				case BURNING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//				    break;
//				case INFERNO: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsInferno);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				};
//				break;
//				default:
//					break;
//				}
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsUnburnt);
//			};
//			    break;
//			case BUILDING_WARM: {
//				switch (bi.getBuilding().getFierynessEnum()) {
//				case HEATING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//					break;
//				case BURNING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//				    break;
//				case INFERNO: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsInferno);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				};
//				break;
//				default:
//					break;
//				}
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsWarm);
//			};	
//				break;
//			case BUILDING_ON_FIRE: {
//				flag = false;
//				switch (bi.getBuilding().getFierynessEnum()) {
//				case HEATING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//					break;
//				case BURNING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//				    break;
//				case INFERNO: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsInferno);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				};
//				break;
//				default:
//					break;
//				}
//			};
//			    break;
//			case BUILDING_EXTINGUISHED: {
//				switch (bi.getBuilding().getFierynessEnum()) {
//				case HEATING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//					break;
//				case BURNING: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsBurning);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//				};
//				    break;
//				case INFERNO: {
//					AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsInferno);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//					AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				};
//				break;
//				default:
//					break;
//				}
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsExtinguished);
//			};
//			    break;
//			case BUILDING_COLLAPSED: 
//				return false;
//			default:
//				break;
//			}
//			bi.updateProperties(BUILDING_CONDITION.BUILDING_ON_FIRE, time);
//		} else {
//			flag = true;
//			bi = new BuildingInfo(b, BUILDING_CONDITION.BUILDING_ON_FIRE, time);
//			switch (bi.getBuilding().getFierynessEnum()) {
//			case HEATING:
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsHeating);
//				break;
//			case BURNING:
//			    AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsBurning);
//			case INFERNO:
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsInferno);
//			default:
//				break;
//			}
//			world.addBuildingInfo(bi);
//			AddAndRemove.addIfNotExists(bi, cluster);
//		}
//		return flag;
//	}
//
//	public boolean addExtinguishedBuilding(BuildingInfo bi, Building b, int time){
//		boolean flag = true;
//		if (bi != null) {
//			switch (bi.getBuildingCondition()) {
//			case BUILDING_UNBURNT: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsExtinguished);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsUnburnt);
//			};
//				break;
//			case BUILDING_WARM: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsExtinguished);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsWarm);
//			};
//			    break;
//			case BUILDING_ON_FIRE: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsExtinguished);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//			};
//			    break;
//			case BUILDING_EXTINGUISHED: {
//				flag = false;
//			};
//			    break;
//			case BUILDING_COLLAPSED: {
//				flag = false;
//			}
//			default:
//				break;
//			}
//			bi.updateProperties(BUILDING_CONDITION.BUILDING_EXTINGUISHED, time);
//		} else {
//			flag = true;
//			bi = new BuildingInfo(b, BUILDING_CONDITION.BUILDING_EXTINGUISHED, time);
//			world.addBuildingInfo(bi);
//			AddAndRemove.addIfNotExists(bi.getBuilding(), buildingsExtinguished);
//			AddAndRemove.addIfNotExists(bi, cluster);
//		}
//		return flag;
//	}
//
//	public boolean addCollapsedBuilding(BuildingInfo bi, Building b, int time){
//		boolean flag = true;
//		if (bi != null) {
//			switch (bi.getBuildingCondition()) {
//			case BUILDING_UNBURNT: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingCollapsed);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsUnburnt);
//			};
//				break;
//			case BUILDING_WARM: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingCollapsed);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsWarm);
//			};
//			    break;
//			case BUILDING_ON_FIRE: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingCollapsed);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsHeating);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsBurning);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsInferno);
//			};
//			    break;
//			case BUILDING_EXTINGUISHED: {
//				AddAndRemove.addIfNotExists(bi.getBuilding(), buildingCollapsed);
//				AddAndRemove.removeIfExists(bi.getBuilding(), buildingsExtinguished);
//			};
//			    break;
//			case BUILDING_COLLAPSED: {
//				flag = false;
//			};
//			    break;
//			default:
//				break;
//			}
//			bi.updateProperties(BUILDING_CONDITION.BUILDING_COLLAPSED, time);
//		} else {
//			flag = true;
//			bi = new BuildingInfo(b, BUILDING_CONDITION.BUILDING_COLLAPSED, time);
//			world.addBuildingInfo(bi);
//			AddAndRemove.addIfNotExists(bi.getBuilding(), buildingCollapsed);
//			AddAndRemove.addIfNotExists(bi, cluster);
//		}
//		return flag;
//	}
//	
//	public void addExpectedExtinguish(AdvancedWorldModel world, Human human, Building target) {
//		
//	}
//	
//	/**
//	 * Update radius of this FireCluster. The radius is the longest distance
//	 * between centroid and farest Building.
//	 */
//	public void updateRadius(int time) {
//		double max = 0;
//		for (BuildingInfo bi : cluster) {
//			if (bi.getBuildingCondition() == BUILDING_CONDITION.BUILDING_UNBURNT)
//				continue;
//			if (bi.getBuildingCondition() == BUILDING_CONDITION.BUILDING_COLLAPSED)
//				continue;
//			int buildingX = bi.getBuilding().getX();
//			int buildingY = bi.getBuilding().getY();
//			if (Ruler.getDistance(buildingX, buildingY, centroid[0], centroid[1]) > max){
//				max = Ruler.getDistance(buildingX, buildingY, centroid[0], centroid[1]);
//			}
//		}
////		System.out.println("In timestep " + time + ", update FireCluster's radius, new radius is " + radius);
//		radius = max;
//	}
//	
//	/** Update centroid of this FireCluster, using K-Means.*/
//	public void updateCentroid(AdvancedWorldModel model, int time){
//		double x = 0, y = 0;
//		for (BuildingInfo bi : cluster) {
//			if (bi.getBuildingCondition() == BUILDING_CONDITION.BUILDING_UNBURNT)
//				continue;
//			if (bi.getBuildingCondition() == BUILDING_CONDITION.BUILDING_COLLAPSED)
//				continue;
//			x += bi.getBuilding().getX();
//			y += bi.getBuilding().getY();
//		}
//		int size = cluster.size() - buildingCollapsed.size() - buildingsUnburnt.size();
//		double[] center = {x / size, y / size};
//		if (center[0] == Double.NaN)
//			center[0] = -1000;
//		if (center[1] == Double.NaN)
//			center[1] = -1000;
//		centroid = center;
//		Area area = (Area)this.getClosestEntity(cluster, centroid[0], centroid[1]);
//		if (area != null) {
//			this.center = area;
////			System.out.println("In timestep " + time + ", update FireCluster's center, new center is " 
////			+ area.getID() + ", x: " + area.getX() + ", y:" + area.getY());
//		}
//	}
//	
//	/** Get closest Entity of point(x, y).*/
//	private StandardEntity getClosestEntity(Collection<BuildingInfo> targets, double x, double y){
//		StandardEntity result = null;
//		double minDistance = Double.POSITIVE_INFINITY, currentDistance = 0;
//		for (BuildingInfo bi : targets) {
//			double buildingX = bi.getBuilding().getX();
//			double buildingY = bi.getBuilding().getY();
//			currentDistance = Math.sqrt(Math.pow(x - buildingX, 2.0) + Math.pow(y - buildingY, 2.0));
//			if (currentDistance < minDistance) {
//				minDistance = currentDistance;
//				result = bi.getBuilding();
//			}
//		}
//		return result;
//	}
//	
//	/**
//	 * Get target Building's BuildingInfo from a list of BuildingInfo. If target
//	 * Building does not exist in this list, null will be returned.
//	 * 
//	 * @param target
//	 *            target Building
//	 * @param list
//	 *            a list of BulidingInfo
//	 * @return target Building's BuildingInfo or null if target does not exist
//	 *         in this list
//	 */
//	private BuildingInfo getBuildingInfoFromList(EntityID target, ArrayList<BuildingInfo> list){
//		for (BuildingInfo bi : list)
//			if (bi.getBuilding().getID().getValue() == target.getValue())
//				return bi;
//		return null;
//	}
//	
//	/**
//	 * Determines whether target Building belongs to this FireCluster. True when
//	 * belongs to false when not.
//	 */
//	public boolean belongsToCluster(EntityID terget) {
//		for (BuildingInfo bi : cluster) {
//			if (bi.getBuilding().getID().getValue() == terget.getValue())
//				return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Determines whether the target Building is close to this cluster. This
//	 * means that the distance between target and this cluster's center is less
//	 * than a certain value which is (radius + 100000)mm.
//	 */
//	public boolean isClosToCluster(EntityID target, AdvancedWorldModel model) {
//		return model.getDistance(center.getID(), target) < (radius + 100000);
//	}
//	
//	/** Get the nearest fire cluster for this target Building.*/
//	public static FireCluster_Old getClosestCluster(EntityID target, ArrayList<FireCluster_Old> lists, StandardWorldModel model) {
//		double smallestDistance = Double.POSITIVE_INFINITY;
//		FireCluster_Old closest = null;
//		// If the terget belongs to a cluster, return this cluster. Otherwise, return the closest one.
//		for (FireCluster_Old list : lists) {
//			if (list.belongsToCluster(target)) {
//				closest = list;
//				break;
//			}
//			if (model.getDistance(list.center.getID(), target) < smallestDistance) {
//				closest = list;
//				smallestDistance = model.getDistance(list.center.getID(), target);
//			}
//		}
//		return closest;
//	}
//
//	public static FireCluster_Old mergeClusters(FireCluster_Old fc1, FireCluster_Old fc2) {
//		for (BuildingInfo buildInfo : fc1.cluster) {
//			// add fc1 to fc2
//			if (AddAndRemove.addIfNotExists(buildInfo, fc2.cluster)) {
//				AddAndRemove.removeIfExists(buildInfo.getBuilding(), fc2.buildingsUnburnt);
//				switch (buildInfo.getBuildingCondition()) {
//				case BUILDING_ON_FIRE: {
//					switch (StandardEntityConstants.Fieryness.values()[buildInfo.getFireyness()]) {
//					case BURNING: {
//						AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingBurning());
//					}
//						;
//						break;
//					case HEATING: {
//						AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingHeating());
//					}
//						break;
//					case INFERNO: {
//						AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingInferno());
//					}
//						break;
//					default:
//						break;
//					}
//				}
//					break;
//				case BUILDING_COLLAPSED: {
//					AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingCollapsed());
//				}
//					break;
//				case BUILDING_WARM: {
//					AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingsWarm());
//				}
//					break;
//				case BUILDING_EXTINGUISHED: {
//					AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingExtinguished());
//				}
//					break;
//				case BUILDING_UNBURNT: {
//					AddAndRemove.addIfNotExists(buildInfo.getBuilding(), fc2.getBuildingsUnburnt());
//				}
//					break;
//				}
//
//			}
//		}
//		return fc2;
//	}
//	
//	@Override
//	public String toString(){
//		return "" + this.getClass().getName();
//	}
//	
//	public ArrayList<BuildingInfo> getAllBuildings() {
//		return cluster;
//	}
//
//	public ArrayList<Building> getBuildingsUnburnt() {
//		return buildingsUnburnt;
//	}
//	
//	public ArrayList<Building> getBuildingsWarm() {
//		return buildingsWarm;
//	}
//	
//	public ArrayList<Building> getBuildingHeating() {
//		return buildingsHeating;
//	}
//	
//	public ArrayList<Building> getBuildingBurning() {
//		return buildingsBurning;
//	}
//	
//	public ArrayList<Building> getBuildingInferno() {
//		return buildingsInferno;
//	}
//	
//	public ArrayList<Building> getBuildingExtinguished() {
//		return buildingsExtinguished;
//	}
//	
//	public ArrayList<Building> getBuildingCollapsed() {
//		return buildingCollapsed;
//	}
//	
//	public ArrayList<Building> getBuildingExpectedToExtinguish() {
//		return buildingsExpectedToExtinguish;
//	}
//}