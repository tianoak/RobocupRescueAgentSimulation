package csu.model.object;

import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;
import csu.agent.fb.FireBrigadeAgent;
import csu.model.AdvancedWorldModel;

public class CSUHydrant {
	
	private AdvancedWorldModel world;
	
	private EntityID selfID;
	private boolean occupied;

	public CSUHydrant(EntityID id, AdvancedWorldModel world) {
		this.selfID = id;
		this.world = world;
		this.occupied  = false;
	}
    /**
     * must  be overwrite if used
     */
	public void update() {
		this.occupied = ! this.occupied;
	}
	
	public boolean isOccuped() {
		return this.occupied;
	}
	
	public String toString() {
		return "CSUH[" + this.selfID +"]";
	}

}
