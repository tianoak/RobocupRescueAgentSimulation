package csu.model.object.csuZoneEntity;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javolution.util.FastMap;

import rescuecore2.worldmodel.EntityID;

@SuppressWarnings("serial")
public class CsuZones extends ArrayList<CsuZone>{
	protected Map<EntityID, CsuZone> buildingZonePair = new FastMap<>();
	protected Random random;
	
	public CsuZones(Random random) {
		this.random = random;
	}
	
	public CsuZone getZone(int zoneId) {
		for (CsuZone next : this) {
			if (next.getZoneId() == zoneId)
				return next;
		}
		return null;
	}
	
	public void findNeighbourZone() {
		for (CsuZone next : this) {
			for (Integer id : next.getNeighbourZoneIds()) {
				next.addNeighbourZone(getZone(id.intValue()));
			}
		}
	}
	
	public void addBuildingZonePair(EntityID buildingId, CsuZone zone) {
		this.buildingZonePair.put(buildingId, zone);
	}
	
	public CsuZone getBuildingZone(EntityID buildingId) {
		return this.buildingZonePair.get(buildingId);
	}
}
