package csu.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * This class defines the entrance of a building. You can use its&nbsp;
 * {@code getEntrance(Building building)} method to get all entrance of a
 * specified building.
 * 
 * @author appreciation-csu
 */

@SuppressWarnings("serial")
final public class Entrance extends HashMap<Building, Set<Road>> {
	/** An instance of world model. */
	protected AdvancedWorldModel world = null;
	/** A set of all entrance of this world. */
	private Set<Road> allEntrance;

	private Map<Road, List<Building>> entranceBuildingMap = new LazyMap<Road, List<Building>>() {

		@Override
		public List<Building> createValue() {
			return new ArrayList<Building>();
		}
	};

	public Entrance(AdvancedWorldModel world) {
		this.world = world;
		this.allEntrance = new HashSet<Road>();
		for (StandardEntity entity : world.getEntitiesOfType(AgentConstants.BUILDINGS)) {
			Building building = (Building) entity;
			Set<Road> entrances = setEntrance(building);
			for (Road road : entrances) {
				this.entranceBuildingMap.get(road).add(building);
			}
			this.allEntrance.addAll(entrances);
			this.put((Building) entity, entrances);
		}
	}

	/**
	 * Set the entrance of a specified building.
	 * <p>
	 * Depth-first search, find out entities directly connected with the
	 * buliding, if the entity is <code>Road</code>, its surely the entrance of
	 * this building, then store it in {@code Set<Road>}.
	 * 
	 * @param building
	 *            the building needs to setting entrance
	 * @return a set of entrance of this building
	 */
	private Set<Road> setEntrance(Building building) {
		Set<Road> roads = new HashSet<Road>();
		Stack<Area> stack = new Stack<Area>();
		Set<Area> visited = new HashSet<Area>();
		stack.add(building);
		do {
			Area entity = stack.pop();
			visited.add(entity);
			if (entity instanceof Road) {
				roads.add((Road) entity);
				world.getCsuRoad(entity.getID()).setEntrance(true);
			} else if (entity instanceof Building) {
				Building bld = (Building) entity;
				for (EntityID id : bld.getNeighbours()) {
					Area e = (Area) world.getEntity(id);
					if (!visited.contains(e)) {
						stack.push(e);
					}
				}
			}
		} while (!stack.isEmpty());
		
		return roads;
	}

	/**
	 * Get the road connected with entrance of this building.
	 * <p>
	 * A building may has more than one entrance, so you need to take this into
	 * consideration.
	 * 
	 * @param building
	 *            the building needs to getting its entrance
	 * @return a set of entrance
	 */
	public Set<Road> getEntrance(Building building) {
		return this.get(building);
	}

	/**
	 * If the target road is an entrance, then return the building it belongs
	 * to. And if the road is not an entrance, null will be returned.
	 * 
	 * @param road
	 *            target road
	 * @return the building this road belongs to or null if this road is not an
	 *         entrance
	 */
	public List<Building> getBuilding(Road road) {
		return this.entranceBuildingMap.get(road);
	}

	/** Determines whether a road is entrance of building. */
	public boolean isEntrance(Road road) {
		return allEntrance.contains(road);
	}

}
