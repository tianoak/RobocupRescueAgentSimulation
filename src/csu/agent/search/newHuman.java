package csu.agent.search;

import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.EntityRefProperty;

public class newHuman extends Human {

	private EntityRefProperty position;
	
	protected newHuman(EntityID id) {
		super(id);
		position = new EntityRefProperty(StandardPropertyURN.POSITION);
	}
	
	/**
    Get the position of this human.
    @return The position.
  */
	public EntityID getPosition() {
		return position.getValue();
	}
	@Override
	public StandardEntityURN getStandardURN() {
		return null;
	}

	@Override
	protected Entity copyImpl() {
		return null;
	}

}
