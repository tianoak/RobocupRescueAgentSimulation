//package csu.model.areapartition;
//
//import java.awt.geom.Point2D;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//import csu.model.AdvancedWorldModel;
//
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.worldmodel.EntityID;
//
//public class PartedArea {
//
//	static public enum AreaStatus {
//		unburn, ignition, catching, burning, inferno, extinguished, burnedout;
//	}
//
//
//	private HashSet<EntityID> buildings = new HashSet<EntityID>();
//	// anyone reached
//	private boolean approached;
//	// I'd reached
//	private boolean reached;
//	// 重心
//	private Point2D points;
//	//面积 单位mm^2
//	private int volume;
//	// 固有ID
//	private final int id;
//	//status
//	private AreaStatus stat = AreaStatus.unburn;
//	private AreaStatus lastStat = AreaStatus.unburn;
//	private boolean changedflag = false;
//	
//	PartedArea(AdvancedWorldModel world, int index, HashSet<EntityID> buildings) {
//		this.approached = false;//靠近
//		this.reached = false;//达到
//		this.id = index;
//		this.buildings = buildings;
//		//重心和总面积计算
//		volume = 0;
//		double x = 0.0, y = 0.0;
//		for (EntityID id : buildings) {
//			Building bld = (Building) world.getEntity(id);
//			x += bld.getX();
//			y += bld.getY();
//			volume += bld.getTotalArea();
//		}
//		this.points = new Point2D.Double(x / buildings.size(), y / buildings.size());
//	}
//
//
//	/**
//	 * EntityID是否含有
//	 */
//	public boolean contains(EntityID id) {
//		if (getContentBuildingsID().contains(id)) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	/**
//	 * collection内在的一个的话True
//	 */
//	public boolean containAny(Collection<? extends StandardEntity> collect) {
//		for (StandardEntity obj : collect) {
//			if (getContentBuildingsID().contains(obj.getID())) {
//				return true;
//			}
//			if (obj instanceof Building) {
//				if (getContentBuildingsID().contains(((Building) obj).getID())) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * collection内在的一个的话True
//	 */
//	public boolean containAtLeastOne(Collection<? extends EntityID> collect) {
//		for (EntityID id : collect) {
//			if (getContentBuildingsID().contains(id)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	public int size() {
//		return getContentBuildingsID().size();
//	}
//
//	// getter setter的海
//	public Set<EntityID> getContentBuildingsID() {
//		return Collections.unmodifiableSet(buildings);
//	}
//
//	public boolean isApproached() {
//		return approached;
//	}
//
//	public void approached() {
//		this.approached = true;
//	}
//
//	public void reached() {
//		this.reached = true;
//	}
//
//	public boolean isReached() {
//		return reached;
//	}
//
//	public Point2D getPoints() {
//		return points;
//	}
//
//	public int getId() {
//		return id;
//	}
//
//	public int getVolume() {
//		return volume;
//	}
//
//	public void setStatus(AreaStatus status) {
//		this.stat = status;
//		if(!this.stat.equals(this.lastStat)){changedflag=true;}
//		else{changedflag=false;}
//	}
//
//	public AreaStatus getStatus() {
//		return stat;
//	}
//
//	public boolean isStateChanged() {
//		return changedflag;
//	}
//
//
//}
