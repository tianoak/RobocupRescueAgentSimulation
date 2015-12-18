package csu.agent.at;

import java.util.EnumSet;

import csu.agent.CentreAgent;
import csu.common.TimeOutException;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.StandardEntityURN;


public class AmbulanceCentreAgent extends CentreAgent<AmbulanceCentre> {

	@Override
	protected void initialize() {
		super.initialize();
		System.out.println(toString()+" is connected. [id="+getID()+"]");
	}

	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
		
	}

	@Override
	protected void act() throws ActionCommandException {
		
	}

	@Override
	protected void afterAct() {
		super.afterAct();
		
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {		
		return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
	}

	@Override
	public String toString() {
		return "CSU_YUNLU ambulance center agent";
	}
}
