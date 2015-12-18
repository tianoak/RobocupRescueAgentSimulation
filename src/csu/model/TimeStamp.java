package csu.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;

/**
 * A timestamp is a sequence of characters or encoded information identifying when a certain event 
 * occurred, usually giving date and time of day. In modern times usage of this term has expanded 
 * to refer to digital date and time information attached to digital data. 
 * 
 * @author CSU --- Appreciation
 *
 */
final public class TimeStamp {
	private Map<EntityID, Integer> lastChangedTime = new HashMap<EntityID, Integer>();

	private Map<EntityID, Integer> lastSeenTime = new HashMap<EntityID, Integer>();
	
	private Map<EntityID, Map<StandardPropertyURN, Integer>> updatedPropertyTimeStamp = 
			new LazyMap<EntityID, Map<StandardPropertyURN, Integer>>() {
		@Override
		public Map<StandardPropertyURN, Integer> createValue() {
			return new HashMap<StandardPropertyURN, Integer>();
		}
	};
	
	private AdvancedWorldModel world = null;
		
	/**
	 * Add <code>AreaBlockadeListener</code> for the <code>rode</code> in <code>world</code>
	 * @param world
	 */
	public TimeStamp(AdvancedWorldModel world) {
		this.world = world;
		for (StandardEntity entity : this.world) {
			entity.addEntityListener(new StateChangeListener());
			if (entity instanceof Road) {
				entity.addEntityListener(new AreaBlockadeListener());
			}
		}
	}

	public void setLastSeenTime(EntityID id, int timeStamp) {
		lastSeenTime.put(id, timeStamp);
	}

	public int getLastSeenTime(EntityID id) {
		Integer time = lastSeenTime.get(id);
		return (time != null) ? time : -1;
	}

	public int getLastChangedTime(EntityID id) {
		Integer t = lastChangedTime.get(id);
		return t != null ? t.intValue() : -1;
	}
	
	public void setLastChangedTime(EntityID id, int time) {
		lastChangedTime.put(id, time);
	}
	
	public Map<StandardPropertyURN, Integer> getPropertyTimeStampMap(EntityID id) {
		return updatedPropertyTimeStamp.get(id);
	}

	public int getPropertyTimeStamp(EntityID id, StandardPropertyURN propertyUrn) {
		Integer time = updatedPropertyTimeStamp.get(id).get(propertyUrn);
		return time != null ? time : -1;
	}

	private void setProtertyTimestamp(EntityID id, StandardPropertyURN propertyUrn, int time) {
		Map<StandardPropertyURN, Integer> map = updatedPropertyTimeStamp.get(id);
		map.put(propertyUrn, time);
	}
	
	public boolean haveSeen(EntityID id) {
		return this.getLastSeenTime(id) != -1;
	}

	public void addStateChangeListener(Entity entity) {
		entity.addEntityListener(new StateChangeListener());
	}
	
	public class StateChangeListener implements EntityListener {
		@Override
		public void propertyChanged(Entity entity, Property property, Object oldValue, Object newValue) {
			final EntityID id = entity.getID();
			setLastChangedTime(id, world.getTime());
			try {
				setProtertyTimestamp(id, StandardPropertyURN.fromString(property.getURN()), world.getTime());
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class AreaBlockadeListener implements EntityListener {

		@SuppressWarnings("unchecked")
		@Override
		public void propertyChanged(Entity entity, Property property, Object oldValue, Object newValue) {
			if (property.getURN().equals(StandardPropertyURN.BLOCKADES.toString())) {
				if (oldValue == null || newValue == null) {
					return;
				}
				List<EntityID> before = (List<EntityID>) oldValue;
				List<EntityID> after = (List<EntityID>) newValue;
				before.removeAll(after);
				for (EntityID id : before) {
					world.removeEntity(id);
				}
			}
		}
	}

	public void merge(ChangeSet changeSet) {
		for (EntityID e : changeSet.getChangedEntities()) {
			StandardEntity existingEntity = world.getEntity(e);
			Map<StandardPropertyURN, Integer> propertyTimestampMap = 
					this.getPropertyTimeStampMap(existingEntity.getID());
			if (propertyTimestampMap == null) {
				propertyTimestampMap = new HashMap<StandardPropertyURN, Integer>();
			}
			for (Property p : changeSet.getChangedProperties(e)) {
				Property existingProperty = existingEntity.getProperty(p.getURN());
				existingProperty.takeValue(p);
				propertyTimestampMap.put(StandardPropertyURN.fromString(p.getURN()), world.getTime());
			}
			this.setLastSeenTime(existingEntity.getID(), world.getTime());
		}
	}
}
