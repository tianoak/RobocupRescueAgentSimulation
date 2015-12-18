package csu.agent.fb.extinguishBehavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javolution.util.FastSet;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.FireBrigadeAgent;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.targetPart.FireBrigadeTarget;
import csu.agent.fb.tools.FbUtilities;
import csu.common.TimeOutException;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
/**
 * name it greedy owning to selecting neighbor fires as targets,
 * but we should exclude those could not be extinguished.
 * also including extinguish fire entirely in the fire area.
 *@author oak
 *@version Oct 4, 2015 10:31:46 AM
 *
 */
public class GreedyExtinguishBehavior implements ExtinguishBehavior_Interface {
    
	private AdvancedWorldModel world;
	private FireBrigadeAgent underlyingAgent;
	private FireBrigade controlledEntity;
	private EntityID agentId;
	
	private EntityID lastFire;
	
	public GreedyExtinguishBehavior(AdvancedWorldModel world) {
			this.world = world;
			this.underlyingAgent = (FireBrigadeAgent)world.getAgent();
			this.controlledEntity = (FireBrigade)world.getControlledEntity();
			this.agentId = world.getAgent().getID();
	}

	
	public void circleEXT() throws ActionCommandException {

		if ((this.controlledEntity).getWater() == 0) 
			return;

		if(this.underlyingAgent.getArcTargetList() == null) 
			initArcTargetList();
				
		updateArcTargetList();
		List<EntityID> arcTargets = this.underlyingAgent.getArcTargetList(); 
		
		if(arcTargets == null || arcTargets.isEmpty())
			return;
		
		EntityID id = arcTargets.get(0);
		this.underlyingAgent.getArcTargetList().remove(0);
		
		if(AgentConstants.FB)
	    	System.out.println(world.getTime() + ", " + world.me + ", using greedy to select target");
//		this.underlyingAgent.extinguish(id); 
		EXT(id);
//		CSUBuilding arcTarget = world.getCsuBuilding(id);
//		int water = FbUtilities.calculateWaterPower(world, arcTarget);
//	    arcTarget.setWaterQuantity(arcTarget.getWaterQuantity() + water);  
//	    this.underlyingAgent.extinguish(id, water);
	}
	
	/**
	 * select the neighbor fires first, we put the fires within the extinguishable distance in the 
	 * agent's arcTargetList according to the clockwise direction, the beginning angle may be important, 
	 * remaining to be tested.
	 * Generally, the view distance is larger than the extinguish distance
	 */
	public void initArcTargetList() {
		if(AgentConstants.FB)
			System.out.println(world.getTime() + ", " + world.me + ", initArcTargetList()");
		int x = world.getEntity(agentId).getLocation(world).first();
		int y = world.getEntity(agentId).getLocation(world).second();
		double deg = 22.5;
		double degree = 0;
		double radian = 0;
		int r = (int) (world.getConfig().extinguishableDistance *0.9); ///test
	    for(int i = 0; i  < 16; i++) {
	    	degree = i*deg;
	    	radian = Math.toRadians(degree);
	    	Collection<StandardEntity> inCircleR = world.getObjectsInRectangle(x, y, x + (int)(r*Math.cos(radian)), y + (int)(r*Math.sin(radian))) ;
	    	for(StandardEntity se : inCircleR) {
	    		if(se instanceof Building && isCandiate((Building)se)) {
	    			System.out.println(this.underlyingAgent.location() +", degree:" + degree + ", arc: " +  se);
	    			this.underlyingAgent.getArcTargetList().add(se.getID());
	    		}
	    	}
	    
	    }		
	}
	
	/**
	 * Some building has been extinguished by other agents,
	 * so we need update the list, avoiding selecting unnecesssary target.
	 */
	public void updateArcTargetList() {

		for (Iterator<EntityID> it = this.underlyingAgent.getArcTargetList().iterator(); it.hasNext();) {
			EntityID id = it.next();
			if(! isCandiate((Building)world.getEntity(id)))
				it.remove();
		}
	}

	boolean isCandiate(Building build) {
		if(build.isOnFire())
			  if(this.underlyingAgent.isEXTQuick(controlledEntity, build))
				  return true;
//		else if(build.isTemperatureDefined() && build.getTemperature() >= 25 && csubuild.getEstimatedTemperature() >=25)
//			return true;
		return false;
	}

	public void entireEXT() throws ActionCommandException {
		if(this.lastFire == null)
			return;
		CSUBuilding lastBuilding = world.getCsuBuilding(this.lastFire);
		 Set<CSUBuilding> neighbourDangerBuildings = lastBuilding.getNeighbourDangerBuildings();
		 if(AgentConstants.FB)
			 System.out.println(world.getTime() + ", " + world.me + ", neighbor danger building: " + neighbourDangerBuildings);
		 for(Iterator<CSUBuilding> it = lastBuilding.getNeighbourDangerBuildings().iterator(); it.hasNext();) {
				CSUBuilding build = it.next();
				it.remove();
				if(isCandiate(build.getSelfBuilding()))
					EXT(build.getId()); 
			}
	}
	/**
	 * Choose the buildings in the direction returned by getDir();
	 * @throws ActionCommandException
	 */
	public void directionEXT() throws ActionCommandException {
		int dir = getDir(this.agentId, this.lastFire);
		Set<EntityID> buildingsInDir = ((FireBrigadeWorld)world).getBuildingsInNESW(dir, this.underlyingAgent.location().getID());
		for(Iterator<EntityID> it = buildingsInDir.iterator(); it.hasNext();) {
			CSUBuilding build = world.getCsuBuilding(it.next());
			it.remove();
			if(isCandiate(build.getSelfBuilding())) {/// { && world.getDistance(this.agentId, this.lastFircd e) < 30000) {
				System.out.println("directionEXT(): " + world.getConfig().extinguishableDistance + ",  " + world.getDistance(this.agentId, this.lastFire));
				EXT(build.getId());
			}
		}
	}
	/**
	 * Get the direction NESW the other is for own marked as 0123, otherwise return 4 for handling.
	 * @param own
	 * @param other
	 * @return
	 */
	public int getDir(EntityID own, EntityID other) {
		if(own == null || other == null)
			return 4;
		int x1= world.getEntity(own).getLocation(world).first();
		int y1 = world.getEntity(own).getLocation(world).second();
		int x2 = world.getEntity(other).getLocation(world).first();
		int y2 = world.getEntity(other).getLocation(world).second();
		int dir;
		int flag = Math.abs(x2-x1) <= Math.abs(y2-y1) ? 1 : 0;
		if(y1 <= y2 && flag == 1)
			dir = 0;
		else if(x1 < x2 && flag == 0)
			dir = 1;
		else if(y1 >= y2 && flag == 1) 
			dir = 2;
		else if(x1 > x2 && flag == 0)
			dir = 3;
		else dir = 4;
		return dir;
	}
	/**
	 * increase water for damaged building when there are no fire
	 * @throws ActionCommandException 
	 */
	public void plusWater() throws ActionCommandException { 
		if(world.getTime() > Math.max(150, world.getConfig().timestep*0.9)) {
			for(StandardEntity build : world.getEntitiesOfType(StandardEntityURN.BUILDING)) {
				if(((Building)build).isFierynessDefined() && ((Building)build).getFieryness() > 3 && ((Building)build).getFieryness() < 7)
					if(world.getDistance(world.me.getID(), build.getID()) < 30000)
						EXT(build.getID());
			}
		}
	} 
	
	public void EXT(EntityID id) throws ActionCommandException {
		if(! world.getCsuBuilding(id).isInflammable())
			return;
		this.lastFire = id;
		CSUBuilding extTarget = world.getCsuBuilding(id);
		int water = FbUtilities.calculateWaterPower(world, extTarget);
	    extTarget.setWaterQuantity(extTarget.getWaterQuantity() + water);
//	    extinguishTargetAndTimeMap.put(world.getTime(), id);
	    this.underlyingAgent.extinguish(id, water);
	}
	
	
	@Override
	public void extinguish(FireBrigadeWorld world, FireBrigadeTarget target)
			throws ActionCommandException, TimeOutException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extinguishNearbyWhenStuck(CSUBuilding target)
			throws ActionCommandException, TimeOutException {
		// TODO Auto-generated method stub
		
	}

}
