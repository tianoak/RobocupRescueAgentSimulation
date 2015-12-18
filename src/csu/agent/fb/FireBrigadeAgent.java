package csu.agent.fb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import csu.agent.Agent.ActionCommandException;
import csu.agent.fb.actionStrategy.CsuOldBasedActionStrategy;
import csu.agent.fb.actionStrategy.DefaultFbActionStrategy;
import csu.agent.fb.actionStrategy.StuckFbActionStrategy;
import csu.agent.fb.actionStrategy.ActionStrategyType;
import csu.agent.fb.actionStrategy.fbActionStrategy;
import csu.agent.fb.actionStrategy.fbActionStrategy_Interface;
import csu.agent.fb.extinguishBehavior.ExtinguishBehaviorType;
import csu.agent.fb.extinguishBehavior.ExtinguishBehavior_Interface;
import csu.agent.fb.extinguishBehavior.GreedyExtinguishBehavior;
import csu.agent.fb.tools.DirectionManager;
import csu.agent.fb.tools.FbUtilities;
import csu.agent.fb.tools.WaterCoolingEstimator;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.model.object.CSUHydrant;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;

public class FireBrigadeAgent extends AbstractFireBrigadeAgent {
	private fbActionStrategy_Interface actionStrategy;
	private ActionStrategyType actionType;
	
	private DirectionManager directionManager;
	private FbUtilities fbUtil;
	private List<EntityID> arcTargetList;
	private GreedyExtinguishBehavior greedyEB;
	private List<EntityID> myHydrantList;
	
	@Override
	protected void initialize() {
		super.initialize();
		getSearchClusters();
		this.directionManager = new DirectionManager(world);
		this.fbUtil = new FbUtilities(world);
		this.waterHistory = new TreeMap<Integer, Integer>();
		this.arcTargetList = new ArrayList<>();
		///.myHydrantSet = Collections.synchronizedSet(new HashSet<EntityID>());///no need synchronized for this one thread
		this.myHydrantList = new ArrayList<>();
		this.myHydrantList.addAll(world.getHydrantSet());
		this.setGreedyEB(new GreedyExtinguishBehavior(world));
	}
	///oak, so the world can be converted to FireBrigadeWorld
	@Override
	protected StandardWorldModel createWorldModel() {
		return new FireBrigadeWorld();
	}

	@Override
	protected void prepareForAct() throws TimeOutException {
		super.prepareForAct();
		this.waterHistory.put(time, me().getWater()); ///int & Integer
	}
	
	@Override
	protected void act() throws ActionCommandException, TimeOutException {

		this.chooseActionStrategy();
		this.leaveBurningBuilding(); ///test: still need to add some constraints
	
		if (isBlocked()) { 
			if(AgentConstants.FB)
				System.out.println(time + ", " + me() + ", is blocked");
			this.actionStrategy.extinguishNearbyWhenStuck();
			randomWalk();
		}

		this.careSelf();
		this.supplyWater();
		
		this.actionStrategy.execute();///in the fire cluster
		isThinkTimeOver("after excute"); 
		this.actionStrategy.moveToFires();///overAllBest

//		if(world.getTime() < Math.min(80, world.getConfig().timestep / 3) 
//           	|| world.getConfig().timestep - 45 < world.getTime())
		lookupSearchBuildings(); ///easy to hover
		enterSearchBuildings();
		
		randomWalk();
	}
 
/*-----------------make some changes to leaveBurningBuilding() and supplyWater() by oak---------------------------*/

	private void leaveBurningBuilding() throws ActionCommandException{

		StandardEntity entity;
		if (location() instanceof Building && !(location() instanceof Refuge)) {  /// refuge will never get fired
			Building building = (Building) location();
			if(world.getCsuBuilding(building).wasEverWatered())
				moveToRefuge();
			if(building.isFierynessDefined() && building.getFieryness() != 8 && 
						building.isTemperatureDefined() && building.getTemperature() > 130) {
								
				List<EntityID> neighbours = building.getNeighbours();
				for (EntityID next : neighbours) {
					entity = world.getEntity(next);
					if (entity instanceof Building) {
						if(unlookupedBuildings != null)   ///cause used before initialize, throws NullPointerException
							unlookupedBuildings.remove(entity);
						if(unenteredBuildings != null)
							unenteredBuildings.remove(entity);
					}
				}  
				if(AgentConstants.FB)
					System.out.println(time + ", " + me()  + ", " + building + ", move to refuge in LBB");
				moveToRefuge();
			}
			else if(me().getWater() > 0 && (me().getHP() - (300-time) * 50) > 0 
						&& building.isFierynessDefined() && building.getFieryness() != 8 
						&& building.isTemperatureDefined() && building.getTemperature() > 25){

				int totalWater = me().getWater();
				if(totalWater > WaterCoolingEstimator.waterNeededToExtinguish(world.getCsuBuilding(building))
						&& building.getGroundArea() < 2300) { 
					if (AgentConstants.FB) 
						System.out.println(time + ", " + me()  + ", " + building + "  try to extinguish in LBB." );
					extinguish(building);
				}
			}
				/** if the building next to refuge is fires, then stayed in the refuge, extinguishing the fire
			 * however, the refuge may not be the best location to extinguish, it can not prevent the fire spread*/
		}else if (location() instanceof Refuge) { //does impassable mean wall existing, then invisible
			for(EntityID next : location().getNeighbours()) { 
				StandardEntity neig = world.getEntity(next);
				if(neig.getID().equals(location().getID()))
					continue;
				if (neig instanceof Building) {
					Building n_bu = (Building) neig;
					if (n_bu.isFierynessDefined() && n_bu.isTemperatureDefined())
						if (n_bu.getFieryness() != 8 && n_bu.getTemperature() > 35) 
							extinguish(n_bu);
				}
			}
		}
	}

	
	@Override
	protected void careSelf() throws ActionCommandException {
		if (me().getHP() - me().getDamage() * (timestep - time) < 0 || ((me().getHP() < 1000) && me().getDamage() != 0)) {
//			 setAgentState(AgentState.RESTING);
			
			if(location() instanceof Refuge) {
				if(AgentConstants.FB)
					System.out.println(time + ", " + me() + ", in refuge in careSelf");				
				rest();
			}
			else {
				if(AgentConstants.FB)
					System.out.println(time + ", " + me() + ", move to refuge in careSelf");				
				moveToRefuge();
			}
		}
	}

	///oak
	private void supplyWater() throws ActionCommandException, TimeOutException {
        ///already in the water supplier
	    refillingWater(); 
	    ///no need to get water
	    if(me().getWater() > 0) 
	    	return ; 
	    
	    boolean rhok = (world.getConfig().tankRefillHydrantRate >= 3000); ///3000
	    boolean hasRefuge = ! world.getEntitiesOfType(StandardEntityURN.REFUGE).isEmpty();
	    boolean hasHydrant = ! world.getEntitiesOfType(StandardEntityURN.HYDRANT).isEmpty();
		Area near = getNearWaterSupplier();
	    boolean hasNear = (near != null);
	    /**may be choose for all H&R using near strategy, then must achieve reset refill rate methods, if 2000, supply in refuge
	     * for update the value as fast as possible, add the cost of distance for go-and-back spend*/
	    if(hasRefuge && (!rhok || !hasHydrant || !hasNear))
	    	supplyWaterInRefuge();
	    if(hasHydrant && hasNear)
	    	supplyWaterNearby(near);
	    if(hasHydrant && !hasNear && !hasRefuge)
	    	supplyWaterInHydrant();
	}
	
/*	private int resetRefillRate() {
		if(location() instanceof Refuge) {
			if(commandHistory.get(time - 1).equals(StandardMessageURN.AK_EXTINGUISH))
				world.REFILL_REFUGE_RATE = this.currentWater-this.preWater + world.getConfig().maxPower;
			else
				if(commandHistory.get(time - 1).equals(StandardMessageURN.AK_REST))
					world.REFILL_REFUGE_RATE = this.currentWater - this.preWater;		
		}
		return world.REFILL_REFUGE_RATE;
	}*/
	
	private void refillingWater() throws ActionCommandException {
		
		if (time < world.getConfig().ignoreUntil + 2)
			return ;
		if(! (this.positionHistory.get(time-1)).equals(location().getID()) && me().getWater() > 0)
			return;
         
		final int refillRateH = world.getConfig().tankRefillHydrantRate;
		final int refillRateR = world.getConfig().tankRefillRate;
		final int capacity = world.getConfig().maxTankCapacity;
		
		if(location() instanceof Refuge) {
			if(me().getWater() + refillRateR > capacity)
				return;
		    else
				rest(); 
		} 
		if(location() instanceof Hydrant) {  
			/**too slow to refill*/
			if(me().getWater() + refillRateH > capacity) /// || refillRateH < 3000)
				return;
			/**not refill water, remove and will never reconsider it*/
			if(time > world.getConfig().ignoreUntil + 2 && 
				(this.commandHistory.get(time-1).equals(StandardMessageURN.AK_REST)) &&
					(this.waterHistory.get(time-1).intValue()==this.waterHistory.get(time).intValue())) {
			///	System.out.println(time + ", " + me() + location() + ", capa: " + capacity + " reR: " +refillRateR+"reH:" +
			///			+refillRateH + "rest : water: " + this.waterHistory.get(time-1).intValue()+", " + this.waterHistory.get(time).intValue() );
				myHydrantList.remove(location().getID());
				return ;
			}
			///adjust by maps, former 15
			if (me().getWater() < refillRateH * 10) 
				rest();
		} 
	}
	 
	/**
	 * considering the distance and current water
	 * @throws TimeOutException */
	private Area getNearWaterSupplier() throws TimeOutException {
		selectHydrants();
		Area nearWaterSupplier = null;
		Refuge betterR = null;
		Hydrant betterH = null;
		///double distanceBack = 0;
		double minDistanceToRefuge = Double.MAX_VALUE;
    	double minDistanceToHydrant = Double.MAX_VALUE;
		Collection<StandardEntity> objectsInRange = world.getObjectsInRange(me(), 30000); ///distance
		for(StandardEntity se : objectsInRange) {
			if(se instanceof Area) ///avoid going to blockaded area
				if(((Area)se).isBlockadesDefined() && ((Area)se).getBlockades().size() > 0)
					continue;
			if(se instanceof Refuge) {
				///if(lastFire != null)  ///consider the cost go back
				///	distanceBack = world.getDistance(se.getID(), lastFire.getId());
				double distance = world.getDistance(me(), se);/// + distanceBack;
				if(distance < minDistanceToRefuge) {
					minDistanceToRefuge = distance;
					betterR = (Refuge)se;
				} 
			} //this set has a self-adapting process
			if(se instanceof Hydrant && myHydrantList.contains(se.getID())) {
			///	if(lastFire != null)  ///consider the cost go back
				///	distanceBack = world.getDistance(se.getID(), lastFire.getId());
			    double distance = world.getDistance(me(), se); /// + distanceBack;
				if(distance < minDistanceToHydrant) {
					minDistanceToHydrant = distance;
					betterH = (Hydrant)se;
				}
			}
		} 

		if(minDistanceToRefuge == Double.MAX_VALUE)
			return betterH;
		if(minDistanceToHydrant == Double.MAX_VALUE)
			return betterR;
		/**
		 * normalize the time to refill and move by the formula: (x-avg) / (max-min),
		 * -0.5 and 0.5 is calculated from 8 and 15
		 * */ 
		double dR = minDistanceToRefuge / 31445.392;
		double dH = minDistanceToHydrant / 31445.392;
		double avg = (dR+dH)/2;
		double r = (dR-avg) / Math.abs(dR-dH) - 0.5;
		double h = (dH-avg) / Math.abs(dR-dH) + 0.5;
		nearWaterSupplier = (r < h) ? betterR : betterH;
		if(AgentConstants.FB) 
			System.out.println(time + ", " + me() + ",r= " + r + ",h= " + h + ", near supplier:  " + nearWaterSupplier);
		return nearWaterSupplier;
	}
	
	/**if me want to refill water, meaning other fbs are more likely to refill, too*/
	public void selectHydrants() { ///sos
		Set<EntityID> visibleEntities =  world.getAgent().getVisibleEntities();
		ArrayList<EntityID> visiblefirebrigade = new ArrayList<>();
		for(EntityID id : visibleEntities) {
			if(world.getEntity(id) instanceof FireBrigade)
				visiblefirebrigade.add(id);
		}
		if (visiblefirebrigade.size() == 0)
			return ;
		HYRANT: for (int i = 0; i < myHydrantList.size(); i++) {
			for (int j = 0; j < visiblefirebrigade.size(); j++) {
				if ((world.getEntity(visiblefirebrigade.get(j), FireBrigade.class)).getPosition(world) instanceof Hydrant 
						&& world.getDistance(visiblefirebrigade.get(j), myHydrantList.get(i)) < 20000) {
					myHydrantList.remove(i);
					visiblefirebrigade.remove(j);
					i--;
					j--;
					continue HYRANT;
				}
			}
		}	
}
	private void supplyWaterNearby(Area near) throws ActionCommandException {
	
			if(AgentConstants.FB)
				System.out.println(time + ", " + me() + ", "  + near + ", move near to supply water");
			move(near);
	}
	
	private void supplyWaterInRefuge() throws ActionCommandException {
			
			if(AgentConstants.FB)
				System.out.println(time + ", " + me() +  ", move to refuge to supply water");
			moveToRefuge();
	}
	
	private void supplyWaterInHydrant() throws ActionCommandException {
		
			if(AgentConstants.FB)
				System.out.println(time + ", " + me() + ", move to hydrant to supply water");
			moveToHydrant();
	}
	
	private void moveToHydrant() throws ActionCommandException {
		Collection<StandardEntity> hydrants = new HashSet<StandardEntity>();
		for(EntityID id : myHydrantList)
			hydrants.add(world.getEntity(id));
		if(! hydrants.isEmpty())
			move(hydrants);
	}
	
/*	private Collection<StandardEntity>  getAvailableHydrants() {
		Collection<StandardEntity> hydrants = world.getEntitiesOfType(StandardEntityURN.HYDRANT);
		if(hydrants == null || hydrants.isEmpty())
			return null;
		for (FireBrigade fb : world.getFireBrigadeList()) {
			if (fb.getID().getValue() == me().getID().getValue())
				continue;
			if (!fb.isPositionDefined())
				continue;
			StandardEntity fbPosition = fb.getPosition(world);
			if (fbPosition instanceof Hydrant)
				hydrants.remove((Hydrant)fbPosition);
		}
		return hydrants;	
	}*/
	
	
/*	private Collection<StandardEntity> getHydrants() {
		Collection<StandardEntity> hydrants = world.getEntitiesOfType(StandardEntityURN.HYDRANT);
		Collection<StandardEntity> hys = new HashSet<StandardEntity>();
 		if(hydrants == null || hydrants.isEmpty())
			return null;
		for(StandardEntity next : hydrants) ///test: relax the constraint
			if(next.getID().getValue() % 3 == me().getID().getValue() % 3)
				hys.add(next);
		if(hys.isEmpty() && AgentConstants.FB)
			System.out.println(time + ", " + me() + ", getHydrants() is empty");
		return hys;
	}*/

	/// implements an interface to achieve a new strategy
	private void chooseActionStrategy() {
		this.actionType = ActionStrategyType.DEFAULT;
		switch (actionType) {
		case DEFAULT:
			if (this.shouldChangeActionType(ActionStrategyType.DEFAULT)) {
				actionStrategy = new DefaultFbActionStrategy((FireBrigadeWorld)world, directionManager, fbUtil);
			}
			break;
		case STUCK_SITUATION:
			if (this.shouldChangeActionType(ActionStrategyType.STUCK_SITUATION)) {
				actionStrategy = new StuckFbActionStrategy((FireBrigadeWorld)world, directionManager, fbUtil);
			}
			break;
		case CSU_OLD_BASED:
			if (this.shouldChangeActionType(ActionStrategyType.CSU_OLD_BASED)) {
				actionStrategy = new CsuOldBasedActionStrategy((FireBrigadeWorld)world, directionManager, fbUtil);
			}
		default:
			break;
		}
	}
	/// hardly used
	private boolean shouldChangeActionType(ActionStrategyType actionType) {
		return this.actionStrategy == null || !this.actionType.equals(actionType);
	}
	
	public List<EntityID> getArcTargetList() {
		return this.arcTargetList;
	}
//	
//	public void locationEXT() throws ActionCommandException, TimeOutException  {
//		if(world.getTime() < Math.max(180, world.getConfig().timestep*0.9)) 
//			return;
//		this.getGreedyEB().circleEXT();		
//    	this.getGreedyEB().entireEXT();
//    	this.getGreedyEB().directionEXT();
//    	this.getGreedyEB().plusWater();
//	}
	public GreedyExtinguishBehavior getGreedyEB() {
		return greedyEB;
	}
	public void setGreedyEB(GreedyExtinguishBehavior greedyEB) {
		this.greedyEB = greedyEB;
	}
}
	