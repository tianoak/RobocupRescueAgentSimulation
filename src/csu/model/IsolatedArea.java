package csu.model;

import java.util.HashSet;
import java.util.List;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

final public class IsolatedArea extends HashSet<StandardEntity> {
	private static final long serialVersionUID = 1L;
	
	private AdvancedWorldModel world = null;
	
	public IsolatedArea(StandardEntity me, AdvancedWorldModel w) {
		super(w.getEntitiesOfType(AgentConstants.AREAS));
		world = w;
		Area area;
		if (me instanceof Human) {
			area = (Area) ((Human) me).getPosition(world);
		}
		else {
			area = (Building) me;
		}
		setIsolationEntities(area);
	}

	/**
	 * 所有相与area连通的建筑都被移除，留下与area不相连的区域，即IsolationArea
	 * @param area
	 */
	private void setIsolationEntities(Area area) {
		if (!this.contains(area)) {
			return; 
		}
		this.remove(area);
		List<EntityID> neighbors = area.getNeighbours();
		for (EntityID id : neighbors) {
			Area a = (Area) world.getEntity(id);
			setIsolationEntities(a);
		}		
	}
	
	/**
	 * 递归相连Area孤立的Area的Set要除外
	 */
	public boolean isIsolation(EntityID position) {
		try {
			return isIsolation((Area) world.getEntity(position));
		}catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * 孤立着，不能去的地方是否。。
	 * 
	 * @param area
	 *            判定Area
	 * @return 不能去的话true
	 */
	public boolean isIsolation(Area area) {
		return this.contains(area);
	}	
}
