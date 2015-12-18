package csu.agent;

import csu.common.TimeOutException;
import rescuecore2.standard.entities.StandardEntity;

public abstract class CentreAgent<E extends StandardEntity> extends CommunicationAgent<E> {

	/** This method invoked in connection time.*/
	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
//		if (world.isCommunicationMedium() || world.isCommunicationHigh()) {
//			EntityID centreId = world.getTeam().get(0);
//			if (getID().getValue() == centreId.getValue()) {
//				world.update(changed);
//				world.getEnergyFlow().update(changed);
//				world.getBuriedHumans().update(changed);
//			}
//		}
	}

	@Override
	protected void afterAct() {
		super.afterAct();
	}
}
