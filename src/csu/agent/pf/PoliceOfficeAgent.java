package csu.agent.pf;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import csu.agent.CentreAgent;
import csu.common.TimeOutException;
import csu.model.route.pov.POVRouter;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardEntityURN;

public class PoliceOfficeAgent extends CentreAgent<PoliceOffice> {

	protected POVRouter router;

	// protected PoliceForceTaskManager taskManager;

	@Override
	protected void initialize() {
		super.initialize();
		System.out.println(toString() + " is connected. [id=" + getID() + "]");
		
		//router = new POVRouter(me(), world);

		// taskManager = new PoliceForceTaskManager(world);
	}
	
	protected Set<Area> crossses = new HashSet<Area>();
	
	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
//		for (StandardEntity se : world.getEntitiesOfType(AgentConstants.AREAS)) {
//			Area area = (Area) se;
//			if (area.getNeighbours().size() >= 3) {
//				crossses.add(area);
//			}
//		}
		
	}

	@Override
	protected void act() throws ActionCommandException, TimeOutException {
		
	}

	@Override
	protected void afterAct() {
		super.afterAct();

	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
	}

	@Override
	public String toString() {
		return "CSU_YUNLU police office agent";
	}

}
