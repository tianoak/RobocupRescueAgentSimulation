package csu.agent;

import java.awt.Point;
import java.util.*;

import csu.agent.at.AmbulanceTeamAgent;
import csu.common.TimeOutException;
import csu.model.AgentConstants;
import csu.model.object.CSURoad;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.worldmodel.EntityID;

public abstract class PlatoonAgent<E extends Human> extends HumanoidAgent<E> {
	StandardEntity oldLocation = null;
	///int randomCount = 0;

	/** A set of areas that some other agent has been explored. */
	protected Set<EntityID> someoneVisitedArea = null; ///
	
	/**
	 * A set of buildings that has not been entered. So for a enteredBuilding,
	 * it must also be a lookupedBuilding.
	 */
	protected Set<EntityID> unenteredBuildings = null;
	
	/**
	 * A set of buildings that has not been looked up. A lookupedBuilding just
	 * represents those Building has been looked up out side it. So a
	 * lookupedBuilding must not a enteredBuilding
	 */
	protected Set<EntityID> unlookupedBuildings = null;
	
	/** This method was invoked in connection time.*/
	@Override
	protected void initialize() {
		super.initialize();
		
		/*initUnentered();
		initUnlookuped();*/
	}
	
	/** Initializing the unlookuped Buildings.*/
	protected void initUnlookuped() {
		Collection<StandardEntity> initedBuildings = world.getEntitiesOfType(StandardEntityURN.BUILDING);
		unlookupedBuildings = new HashSet<EntityID>(initedBuildings.size());
		for (StandardEntity se : initedBuildings) {
			unlookupedBuildings.add(se.getID());
		}
	}
	
	/** Initializing untered Buildings.*/
	protected void initUnentered() {
		StandardEntityURN[] initedURN;
		if (this instanceof AmbulanceTeamAgent) {
			initedURN = new StandardEntityURN[] {
					StandardEntityURN.BUILDING,
					StandardEntityURN.AMBULANCE_CENTRE,
					StandardEntityURN.FIRE_STATION,
					StandardEntityURN.POLICE_OFFICE,	
					StandardEntityURN.GAS_STATION
			};
		} else {
			initedURN = new StandardEntityURN[] {
					StandardEntityURN.BUILDING,
			};
		}
		Collection<StandardEntity> initedBuildings = world.getEntitiesOfType(initedURN);
		unenteredBuildings = new HashSet<EntityID>(initedBuildings.size());
		for (StandardEntity se : initedBuildings) {
			unenteredBuildings.add(se.getID());
		}
	}

	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
		
		someoneVisitedArea = world.getSearchedBuildings();
	}

	/**
	 * The necessary common behavior of all <code>PlatoonAgent</code>.
	 * <p>
	 * And this action is about how to go out of a Building for all <code>RCR Agent</code>
	 */
	@Override
	protected void act() throws ActionCommandException , TimeOutException{
		 
	}

	/**
	 * Move to building and look up outside it to see if this building is on fire or not,
	 * whether has Human stucked in it and etc.
	 * <p>
	 * This method is mainly used to collecte informations, so there is no need to enter 
	 * those building. Beside, it will take a certain risk to enter a building because if
	 * a building is on fire and Agent enter it, Agent will get a certain damage.  
	 * 
	 * @throws ActionCommandException
	 */
	protected void lookupSearchBuildings() throws ActionCommandException {
		if (unlookupedBuildings == null || unlookupedBuildings.isEmpty()) {
			initUnlookuped();   // think all building in world are unlookuped
		}
		/* Remove all Buildings that someone has visited.*/
		unlookupedBuildings.removeAll(someoneVisitedArea);
		unlookupedBuildings.removeAll(changed.getChangedEntities());
		
		/* Remove all Buildings that is on fire or burnt out.*/
		for (Iterator<EntityID> it = unlookupedBuildings.iterator(); it.hasNext();) {
			EntityID id = it.next();
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if ((building.isFierynessDefined() && building.getFierynessEnum() == Fieryness.BURNT_OUT)
						|| building.isOnFire()) {
					it.remove();
				}
			}
		}
		
		// if a entrance is blocked, then unsearch all buildings related to this entrance
		for (EntityID next : getChanged()) {
			StandardEntity entity = world.getEntity(next);
			if (!(entity instanceof Road))
				continue;
			CSURoad road = world.getCsuRoad(next);
			
			if (road.isEntrance()) {
				for (Building build : world.getEntrance().getBuilding(road.getSelfRoad())) {
					unlookupedBuildings.remove(build.getID());
				}
			}
		}
		
		/* If the unlookupedBuildings is not empty, then I need go to one of them and do something.*/
		if (!unlookupedBuildings.isEmpty()) {
			Set<StandardEntity> dest = new HashSet<StandardEntity>(unlookupedBuildings.size());
			for (EntityID id : unlookupedBuildings) {
				dest.add(world.getEntity(id));
			}
			if (me() instanceof PoliceForce) {
				moveFront(dest, router.getPfCostFunction());
			}

			if (AgentConstants.PRINT_TEST_DATA || AgentConstants.FB) {
				System.out.println(time + ", " + me() + " is lookup search building ");
                System.out.println("------PlatoonAgent: lookupSearchBuilding");
			}
			
			moveFront(dest);
		}
	}
	
	/**
	 * Enter a Building and search. This method mainly used to find buried Human
	 * and reported to AT.
	 * <p>
	 * For the safety case, Agents will not enter fired buildings or buildings
	 * not on fire but with temperature greater than a certian value. And there
	 * is no need to enter burnt out building any more, so Agents will not enter
	 * burnt out building, too.
	 * 
	 * @throws ActionCommandException
	 */
	protected void enterSearchBuildings() throws ActionCommandException {
		if (unenteredBuildings == null || unenteredBuildings.isEmpty()) {
			initUnentered();
		}
		unenteredBuildings.removeAll(someoneVisitedArea);
		for (Iterator<EntityID> it = unenteredBuildings.iterator(); it.hasNext();) {
			EntityID id = it.next();
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if (world.getCollapsedBuildings().contains(building) 
						|| world.getBurningBuildings().contains(building)) {
					it.remove();
				} else if (building.isTemperatureDefined() && building.getTemperature() > 25) {
					it.remove();
				}
			}
		}
		
		for (EntityID next : getChanged()) {
			StandardEntity entity = world.getEntity(next);
			if (!(entity instanceof Road))
				continue;
			CSURoad road = world.getCsuRoad(next);

			if (road.isEntrance()) {
				for (Building build : world.getEntrance().getBuilding(road.getSelfRoad())) {
					unenteredBuildings.remove(build.getID());
				}
			}
		}
		
		if (!unenteredBuildings.isEmpty()) {
			Set<StandardEntity> dest = new HashSet<StandardEntity>(unenteredBuildings.size());
			for (EntityID id : unenteredBuildings) {
				dest.add(world.getEntity(id));
			}
			
			dest.remove(location().getID());
			
			if (me() instanceof PoliceForce) {
				move(dest, router.getPfCostFunction());
			}
			
			if (AgentConstants.PRINT_TEST_DATA || AgentConstants.FB) {
				System.out.println(time + ", " + me() + " is enter search building"); 
				System.out.println("------FireBrigadeAgent: think");
			}
			
			move(dest);
		}
	}

	protected void randomWalk() throws ActionCommandException { ///
	/*	if((me().getID().getValue()==1182500613 ||me().getID().getValue()== 136956027)&& time >= 25) {
			System.out.println(time + ", " + me() + ", starts moving horizentally");
			List<EntityID> path = new ArrayList<>();
			path.add(me().getID());
			path.add(me().getID());
			send(new AKMove(getID(), time, path, me().getLocation(world).first()-300000, me().getLocation(world).second()));
		}*/
		Collection<StandardEntity> inRnage = world.getObjectsInRange(me().getID(), 50000);
		List<Area> target = new ArrayList<>();
		Collection<Area> blockadeDefinedArea = new ArrayList<>();
		
		for (StandardEntity entity : inRnage) {
			if (!(entity instanceof Area))
				continue;
			if (entity.getID().getValue() == location().getID().getValue())
				continue;
			Area nearArea = (Area) entity;
			///now the blockadeDefinedArea is empty, exclude the blocked at first
			if (nearArea.isBlockadesDefined() && nearArea.getBlockades().size() > 0) {
				blockadeDefinedArea.add(nearArea);
				continue;
			}
			///would not go to the fire building
			if(entity instanceof Building && ((Building) entity).isOnFire()) 
				continue;
			
			target.add(nearArea);
		}
		
		if (target.isEmpty())
			target.addAll(blockadeDefinedArea);
		
		///randomCount = (randomCount < target.size()) ? randomCount++ : 0;///walk not randomly
		///Area randomTarget = target.get(randomCount);
		Area randomTarget = target.get(random.nextInt(target.size()));
		
		///System.out.println(time + ", " + me() + ", " + target + ",,,,,,,," + randomTarget);
		List<EntityID> path = router.getAStar(location(), 
				randomTarget, router.getNormalCostFunction(), new Point(me().getX(), me().getY()));
		
		if (AgentConstants.PRINT_TEST_DATA 
				|| AgentConstants.PRINT_TEST_DATA_PF || AgentConstants.FB) {
			String str = null;
			for (EntityID next : path) {
				if (str == null) {
					str = next.getValue() + "";
				} else {
					str = str + ", " + next.getValue();
				}
			}
			System.out.println(time + ", " + me() + " randomWalk path: [" + str + "]");
		}		
		move(path);
	}

	/** Move to refuge.*/
	protected void moveToRefuge() throws ActionCommandException {
		Collection<StandardEntity> refuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
		
		if (refuges.isEmpty())
			return;
		
		if (AgentConstants.PRINT_TEST_DATA 
				|| AgentConstants.PRINT_TEST_DATA_PF || AgentConstants.FB) {
			System.out.println(time + ", " + me() + " move to refuges"); 
			System.out.println("------PlatoonAgent: moveToRefuge");		 
		}
		
		move(refuges);
	}

	/*protected void aggregatorStay() throws ActionCommandException {
		EntityID agPos = getAggregatorPosition(getID());
		int dist = world.getDistance(me().getPosition(), agPos);
		
		 * if the distance to the aggragator position is shorter than the minimum of the voiceChannels' range,
		 * the agent is unnecessary to go to there.
		 * ====================================================================================
		 * But I think maybe while the agent random walking , the distane to the aggragator position was
		 * changed to be longer than the minimum
		 * ====================================================================================
		 
		if (dist < Collections.min(world.getConfig().voiceChannels.values(),
				new Comparator<ConfigConstants.VoiceChannel>() {
					@Override
					public int compare(VoiceChannel c1, VoiceChannel c2) {
						return c1.range - c2.range;
					}
				}).range) {
			randomWalk();
		}
		move(agPos);
	}
	
	protected void messengerLoop(int interval) throws ActionCommandException {
		int lastTime = -1;
		EntityID lastSeenID = null;
		for (EntityID id : getAggregators()) {
			int t = world.getTimestamp().getLastSeenTime(getAggregatorPosition(id));
			if (lastTime < t) {
				lastTime = t;
				lastSeenID = id;
			}
		}
		if (lastTime == -1) {
			Set<Area> aggregatorsArea = new HashSet<Area>(getAggregators().size());
			for (EntityID id : getAggregators()) {
				aggregatorsArea.add((Area) world.getEntity(getAggregatorPosition(id)));
			}
			move(aggregatorsArea);
		}
		if (!world.getBurningBuildings().isEmpty() || lastTime - time > interval) {
			move(lastSeenID);
		}
	}*/
	
	/**
	 * When the HP point of mine is less than a certain point, I need to go to refuge and rest.
	 * 
	 * @throws ActionCommandException
	 */
	protected void careSelf() throws ActionCommandException {
		if (me().getHP() - me().getDamage() * (timestep - time) < 16) {
			if (AgentConstants.PRINT_TEST_DATA_PF) {
				System.out.println("time = " + time + me() + " careSelf");
			}
			moveToRefuge();
		}
	}
}
