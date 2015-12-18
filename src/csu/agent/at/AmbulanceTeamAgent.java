package csu.agent.at;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.lang.Boolean;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.agent.at.buriedHumanInfo.BuriedHumanInfoProcesser;
import csu.agent.at.rescueTools.BuriednessComparator;
import csu.agent.at.rescueTools.EntityFreezer;
import csu.agent.at.rescueTools.EntityFreezer.Freeze_Tab;
import csu.agent.at.rescueTools.RescueTargetSelector;
import csu.agent.at.rescueTools.SearchSpaceManager;
import csu.common.TimeOutException;
import csu.standard.EntityIdComparator;


public class AmbulanceTeamAgent extends AbstractAmbulanceTeamAgent {

	/** The target human to rescue or to load */
	protected Human targetHuman ;
	
	//cyw  judge if the targetHuman is not rescued or loaded by the other AT.putAll
	protected static Map<EntityID, EntityID> mapOfTargetHumanLocked = new HashMap<EntityID, EntityID>();
	
	/** The BuriedHumanInfoProcesser*/
	protected BuriedHumanInfoProcesser BHI_processer;
	
	/** The TargetSelector*/
	protected RescueTargetSelector targetSelector;
	
	/** The BuildingManager*/
	protected SearchSpaceManager searchSpaceManager;
	
	/** The EntityFreezer*/
	protected EntityFreezer freezer;
	
	/** The next area AT will pass by*/
	protected EntityID nextPassArea;
	
	/** True if the AT has got a burn injury*/
	protected boolean getBurnInjury;
	
	/**
	 * This method can be overrided. When overriding, you must invoke parent class's initialize() method
	 * by this line of code : super.initialize()
	 */
	@Override
	protected void initialize() {
		super.initialize();
		currentTask=AmbulanceTeamTasks.NULL_TASK;
		targetHuman = null;
		freezer = new EntityFreezer();
		BHI_processer = new BuriedHumanInfoProcesser(world);
		targetSelector = new RescueTargetSelector(world , clusters , assignedClusterIndex , BHI_processer , freezer);
		searchSpaceManager = new SearchSpaceManager(world , clusters , assignedClusterIndex , freezer);
		getBurnInjury = false;
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
//		System.out.println("======================="+me()+"       time : "+time+"=======================");
		super.think(time, changed, heard);
	}

	/**
	 * At the beginning of each circle , the <code>needlessBuildingForSearch</code> 
	 * , <code>HumanForRescue</code> , <code>HumanForRescue</code>
	 * @throws TimeOutException 
	 */
	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
		if (time >= world.getConfig().ignoreUntil){
			updateForFreezer();
			searchSpaceManager.update(changed);
			BHI_processer.update();
			targetSelector.update(changed , searchSpaceManager.getNeedlessBuildingForRescue() , searchSpaceManager.isAssignedClusterNearlyClear());
			nextPassArea = null;
		}
	}
	
	@Override
	protected void act() throws ActionCommandException, TimeOutException{
		super.act();
		updateCurrentTask();
//		System.out.println(me()+"time : "+time+" The size of buried human in cluster is : "+BHI_processer.getHumanForRescueSize());
//		System.out.println(me()+"time : "+time+" The size of building in cluster is :     "+buildingForSearch.size());
//		System.out.println(me()+"time : "+time+" last task :    "+lastTask);
//		System.out.println(me()+"time : "+time+" current task : "+currentTask);
//		if (currentTask.equals(AmbulanceTeamTasks.MOVING_TO_HUMAN))
//			System.out.println(world.me + "  Time : " + world.getTime() + "  Target Human " + targetHuman);
		executeCurrentTask();
	}
	
	@Override
	protected void afterAct(){
		//update task
		lastTask = currentTask;
		targetSelector.afterAct();
		super.afterAct();
	}
	
	/**
	 * To update the <code>currentTask</code> and <code>lastTask</code>
	 */
	protected void updateCurrentTask(){
		
		if (checkForSTUCK()) return ;
		if (checkForBURIED()) return ;
		
		if (checkForDYING()) return ;
		switch (lastTask){
		case LOADING : 
			updateForLOADING();
			break;
		case RESCUING :
			updateForRESCUING();
			break;
		case STUCK:
			updateForSTUCK();
			break;
		case BURIED:
			updateForBURIED();
			break;
		case MOVING_TO_HUMAN :
			updateForMOVING_TO_HUMAN();
			break;
		case SEARCHING :
		case NULL_TASK : 
		case RESCUE_OVER :
			getNewTask();
			break;
		default :
			break;
		}
	}
	
	/**
	 * To execute the corresponding task according to the <code>currentTask</code>
	 * @throws csu.agent.Agent.ActionCommandException 
	 */
	protected void executeCurrentTask() throws ActionCommandException{
		switch (currentTask){
		case LOADING :
			executeForLOADING();
			break;
		case RESCUING :
			executeForRESCUING();
			break;
		case SEARCHING :
			executeForSEARCHING();
			break;
		case MOVING_TO_HUMAN :
			executeForMOVING_TO_HUMAN();
			break;
		case STUCK :
			executeForSTUCK();
			break;
		case BURIED :
			executeForBURIED();
			break;
		case DYING :
			executeForDYING();
			break;
		case RESCUE_OVER :
			executeForRESCUE_OVER();
			break;
		default : break;
		}
	}
	
	/**
	 * To update the <code>currentTask</code> when the <code>lastTask</code> is LOADING.<br>
	 * If the agent has arrived in the refuge , the it will be arranged a new task 
	 * by using the method : <code>updateForNULL_TASK</code>.
	 */
	protected void updateForLOADING(){
//		System.out.println(me() + " time : " + time + "  updateForLOADING");
		Human human = someoneOnBoard(world.me);
		if (human != null){
			currentTask = AmbulanceTeamTasks.LOADING;
			targetHuman = human;
			return;
		}
		else getNewTask();
	}

	/**
	 * cyw
	 * judge whether the human is bured.If yes ,compute if it will died when rescued and 
	 * it will died ,don't be rescued.if no,rescueit.
	 * @param human
	 * @return false rescued
	 * @return true don't rescued.
	 */
	protected boolean willDiedWhenRscued(Human human){
		System.out.println("human.isBuriednessDefined():"+human.isBuriednessDefined());
		if(human.isBuriednessDefined()&&human.getBuriedness() == 0)
			return false;
		
		int deadtime = estimatedDeathTime(human.getHP(), human.getDamage(), time);
		int resuceTime = 10000;
		System.out.println("deadtime:"+deadtime);
		if(deadtime > resuceTime)
			return true;
		
		return false;
	}
	
	public int estimatedDeathTime(int hp, double dmg, int updatetime) {
		int agenttime = 1000;
		int count = agenttime - updatetime;
		if ((count <= 0) || (dmg == 0.0D)) {
			return hp;
		}
		double kbury = 3.5E-05D;
		double kcollapse = 0.00025D;
		double darsadbury = -0.0014D * updatetime + 0.64D;
		double burydamage = dmg * darsadbury;
		double collapsedamage = dmg - burydamage;

		while (count > 0) {
			int time = agenttime - count;

			burydamage += kbury * burydamage * burydamage + 0.11D;
			collapsedamage += kcollapse * collapsedamage * collapsedamage
					+ 0.11D;
			dmg = burydamage + collapsedamage;
			count--;
			hp = (int) (hp - dmg);

			if (hp <= 0)
				return time;
		}
		return 1000;
	}
	//cywEnd
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is RESCUING .<br>
	 * The method contains the following steps : 
	 * 1. Check the buriedness of the target human is more than 0 , if yes ,return.
	 * 2. Check the target human is a civilian , if no , get a new task for the agent.
	 * 3. Check the agent is the one whose ID is the smallest one in the building ,
	 *  if yes , load the civilian
	 * 4. Check there are other buried human in the building , if yes , update the 
	 * 	<code>targetHuman</code> 
	 * 5. Get a new task
	 */
	protected void updateForRESCUING(){




//		System.out.println(me() + " time : " + time + "  updateForRESCUING");
		ArrayList<EntityID> victimInBuilding = new ArrayList<EntityID>();
		ArrayList<EntityID> ATInBuilding = new ArrayList<EntityID>();
		//update victimInBuilding and ATInBuilding from changed.
		for (EntityID id : changed.getChangedEntities()){
			StandardEntity entity = world.getEntity(id);
			if (!(entity instanceof Human))
				continue;
			Human human = (Human)entity;
			if (!human.getPosition().equals(me().getPosition()))
				continue;
			if (human.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()) && human.getHP() > 0 &&
					human.getBuriedness() == 0 && someoneOnBoard(human) == null)
				ATInBuilding.add(id);
			if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()) && BHI_processer.isBuriedHuman(id) ||
					human.getBuriedness() > 0 && targetSelector.isNeedToRescue(id))
				victimInBuilding.add(id);
		}
		
		Collections.sort(victimInBuilding , new BuriednessComparator(world));
		Collections.sort(ATInBuilding, new EntityIdComparator());
		
		while (!victimInBuilding.isEmpty()){
			Human victim = (Human)world.getEntity(victimInBuilding.get(0));
			
			
			 	//cyw TestTestTestTestTest   judge whether will died when rescued.
//				if(willDiedWhenRscued(victim)){
//					System.out.println("truetruetruetruetruetruetruetrueTtrue");
//					victimInBuilding.remove(0);
//					continue;
//				}
			
			//cyw BUG:::::::judge whether it is rescued?if it is already rescued,no business,or rescue.
//			if(mapOfTargetHumanLocked.containsKey(victim.getID())){
//				//in the beginning, will rescue .but when the next time refresh ,continue,not rescue.
////				EntityID loadingAT = ATInBuilding.remove(0);
////				if (victim.getURN().equals(StandardEntityURN.CIVILIAN.toString()) && !freezer.containStuff(Freeze_Tab.LOADINGAT , loadingAT))
////					freezer.addNewfreezingStuff(Freeze_Tab.LOADINGAT , loadingAT , targetSelector.timeOnPathToRefuge(victim.getID()));
//				
//				if(mapOfTargetHumanLocked.get(victim.getID())!=me().getID())
//					continue;
//			}
			//cywEnd
			
			
			
			
			//have rescued,now loading.
			if (victim.getURN().equals(StandardEntityURN.CIVILIAN.toString()) && victim.getBuriedness() == 0){
				if (ATInBuilding.get(0).equals(me().getID())){
					targetHuman = victim;
					currentTask = AmbulanceTeamTasks.LOADING;
					if (targetHuman.getID().equals(targetSelector.getTargetID()))
						targetSelector.clearTargetHuman();
					return;
				}
				else if (targetHuman.getID().equals(victim.getID())){
					targetHuman = null;
					targetSelector.clearTargetHuman();
					getNewTask();
					return ;
				}
				EntityID loadingAT = ATInBuilding.remove(0);
				if (!freezer.containStuff(Freeze_Tab.LOADINGAT , loadingAT))
					freezer.addNewfreezingStuff(Freeze_Tab.LOADINGAT , loadingAT , targetSelector.timeOnPathToRefuge(victim.getID()));
			}
			else if (victim.getBuriedness() < ATInBuilding.size()){
				int removeSize = victim.getBuriedness(); // The number of AT to be removed
				for (int i = 0 ; i<removeSize ; i++){
					//set targetHuman
					if (ATInBuilding.get(0).equals(me().getID())){
						
						
						
						//cyw 
//						mapOfTargetHumanLocked.put(victim.getID(),me().getID());
						//cywEnd
						//cyw if it is already rescued,no business,or rescue.
						if(!mapOfTargetHumanLocked.containsKey(victim.getID())){
							mapOfTargetHumanLocked.put(victim.getID(),me().getID());
							System.out.println("cyw222 no this targetHuman"+victim.getID()+"\tput in .mapOfTargetHumanLocked.size():"+mapOfTargetHumanLocked.size());
							targetHuman = victim;
							targetSelector.setTargetHuman(victim.getID());
							return;
						}else if(mapOfTargetHumanLocked.get(victim.getID())==null){
							mapOfTargetHumanLocked.replace(victim.getID(),me().getID());
							System.out.println("cyw222 have this targetHuman"+victim.getID()+" but false\treplace .mapOfTargetHumanLocked.size():"+mapOfTargetHumanLocked.size());
							targetHuman = victim;
							targetSelector.setTargetHuman(victim.getID());
							return;
						}else if(mapOfTargetHumanLocked.get(victim.getID())!=me().getID()){
//							targetHuman = null;
//							targetSelector.clearTargetHuman();
//							getNewTask();
//							continue;
						}
						
						//cywEnd
						
						
						
						
						
//						targetHuman = victim;
//						targetSelector.setTargetHuman(victim.getID());
//						return ;
					}
					EntityID loadingAT = ATInBuilding.remove(0);
					if (victim.getURN().equals(StandardEntityURN.CIVILIAN.toString()) && !freezer.containStuff(Freeze_Tab.LOADINGAT , loadingAT))
						freezer.addNewfreezingStuff(Freeze_Tab.LOADINGAT , loadingAT , targetSelector.timeOnPathToRefuge(victim.getID()));
				}
				if (victim.getID().equals(targetHuman.getID())){
					targetHuman = null;
					targetSelector.clearTargetHuman();
					getNewTask();
					return ;
				}
				victimInBuilding.remove(0);
			}
			else break;
		}
//		if (!CVInBuilding.contains(targetHuman.getID())){
//			targetHuman = null;
//		}
		EntityID humanID = world.getBuriedHumans().getNewRescuingHumanFromCommunication();
		Human human = (Human)world.getEntity(humanID);
		if (human != null && victimInBuilding.contains(humanID) && 
				targetSelector.isNeedToRescue(humanID)){
			
			
			
			//cyw lock targetHuman
			if(!mapOfTargetHumanLocked.containsKey(humanID)){
				mapOfTargetHumanLocked.put(humanID,me().getID());
				System.out.println("cyw111 no this targetHuman"+humanID+"\tput in .mapOfTargetHumanLocked.size():"+mapOfTargetHumanLocked.size());
//				targetHuman = (Human)world.getEntity(humanID);
//				targetSelector.setTargetHuman(humanID);
			}else if(mapOfTargetHumanLocked.get(humanID)==null){
				mapOfTargetHumanLocked.replace(humanID,me().getID());
				System.out.println("cyw111 have this targetHuman"+humanID+" but false\treplace .mapOfTargetHumanLocked.size():"+mapOfTargetHumanLocked.size());
//				targetHuman = (Human)world.getEntity(humanID);
//				targetSelector.setTargetHuman(humanID);
			}else if(mapOfTargetHumanLocked.get(humanID)!=me().getID()){
				targetHuman = null;
				targetSelector.clearTargetHuman();
				getNewTask();
				return;
			}
			
			//cyw
			
			
			
			
			
			
			targetHuman = (Human)world.getEntity(humanID);
			targetSelector.setTargetHuman(humanID);
//			System.out.println(world.me + " Time : " + world.getTime() + " getRescuingHuman : " + humanID +"  in updateForRESCUING");
		}
		if (!targetSelector.isNeedToRescue(targetHuman.getID())){
			targetHuman = null;
			targetSelector.clearTargetHuman();
//			System.out.println("getNewTask for : !BHI_processer.isNeedToRescue(targetHuman.getID())");
			getNewTask();
			return ;
		}
		if (changed.getChangedEntities().contains(targetHuman.getPosition()) && 
				!changed.getChangedEntities().contains(targetHuman.getID())){
			targetHuman = null;
//			System.out.println("getNewTask for : target human not exists in the building");
			targetSelector.clearTargetHuman();
			getNewTask();
			return ;
		}
		if (world.me.getPosition().equals(targetHuman.getPosition()) && !victimInBuilding.contains(targetHuman.getID())){
			targetHuman = null;
			targetSelector.clearTargetHuman();
			getNewTask();
			return ;
		}
 	}
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is SEARCHING .<br>
	 * If the agent has reached a unsearched building belongs to the cluster ,
	 * and there are buried human , his currentTask will be set as RESCUING.
	 * Otherwise , the agent will go on searching for another buildings.
	 */
	protected void updateForSEARCHING(){
		System.out.println(me() + " time : " + time + "  updateForSEARCHING");
		return ;
	}
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is MOVING_TO_HUMAN .<br>
	 */
	protected void updateForMOVING_TO_HUMAN(){
//		System.out.println(me() + " time : " + time + "  updateForMOVING_TO_HUMAN");
		if (!targetSelector.isNeedToRescue(targetHuman.getID())){
//			System.out.println(me()+ "  Time : " + time + "========1========");
			getNewTask();
			return;
		}
		
		//cyw
		if(mapOfTargetHumanLocked.containsKey(targetHuman.getID())){
			targetHuman = null;
			targetSelector.clearTargetHuman();
			getNewTask();
			return;
		}
		//cywEnd
		
		
		
		
		
		if (!targetHuman.getPosition().equals(me().getPosition())){
			if (!targetSelector.isHumanForRescueEmpty()){
//				targetHuman = targetSelector.getTargetHuman();
				//cyw
				targetHuman = targetSelector.getTargetHuman(mapOfTargetHumanLocked,me().getID());
				System.out.println("me:"+me().getID()+"\ttargetHuman:"+targetHuman);
				//
			}
			else {
				getNewTask();
				return ;
			}
		}
		if (targetHuman.getPosition().equals(me().getPosition())){
			if (changed.getChangedEntities().contains(targetHuman.getID())){
				currentTask = AmbulanceTeamTasks.RESCUING;
				EntityID humanID = world.getBuriedHumans().getNewRescuingHumanFromCommunication();
				Human human = (Human)world.getEntity(humanID);
				if (human != null && human.isPositionDefined() && human.getPosition().equals(world.me.getPosition()) && 
						targetSelector.isNeedToRescue(humanID)){
					targetHuman = (Human)world.getEntity(humanID);
					targetSelector.setTargetHuman(humanID);
					
					
					//cyw
					mapOfTargetHumanLocked.put(humanID, me().getID());
					//
					
//					System.out.println(world.me + " Time : " + world.getTime() + " getRescuingHuman : " + humanID + "  in updateForMOVING_TO_HUMAN");
				}
				updateForRESCUING();
				return ;
			}
			else {
				targetHuman = null;
				targetSelector.clearTargetHuman();
//				System.out.println(me()+ "  Time : " + time + "========3========");
				getNewTask();
				return ;
			}
		}
	}
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is STUCK .<br>
	 */
	protected void updateForSTUCK(){
		Human human = someoneOnBoard(world.me);
		if (human != null){
			currentTask = AmbulanceTeamTasks.LOADING;
			targetHuman = human;
			return;
		}
		getNewTask();
		return ;
	}
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is BURIED .<br>
	 */
	protected void updateForBURIED(){
		Human human = someoneOnBoard(world.me);
		if (human != null){
			currentTask = AmbulanceTeamTasks.LOADING;
			targetHuman = human;
			return;
		}
		getNewTask();
		return ;
	}
	
	/**
	 * To update the <code>currentTask</code> when the lastTask is STUCK .<br>
	 * If the agent is not stuck now , arrange a new task for it.
	 */
	protected boolean checkForSTUCK(){
//		System.out.println(me() + " time : " + time + "  checkForSTUCK");
		if (isStucked(world.me)){
			currentTask = AmbulanceTeamTasks.STUCK;
			return true;
		}
		else return false;
	}
	
	/**
	 * To check if the AT is buried
	 */
	protected boolean checkForBURIED(){
//		System.out.println(me() + " time : " + time + "  checkForBURIED");
		if (me().getBuriedness() > 0){
			currentTask = AmbulanceTeamTasks.BURIED;
			return true;
		}
		else 
			return false;
	}
	
	/**
	 * To check if the AT is dying
	 * @return
	 */
	protected boolean checkForDYING(){
		if (world.me.getDamage() == 0)
			return false;
		if (someoneOnBoard(me()) != null)
			return false;
		checkBurnInjury();
		int deathTime;
		if(getBurnInjury){
			double hp = world.me.getHP();
			double damage = world.me.getDamage();
			deathTime = 0;
			double k = 0.00025;
			double l = 0.03;
			double m = 0.1;
			//count the deathTime.using time when burned.
			while (hp > 0){
				damage += k * damage * damage + l + m;
				hp = hp - damage;
				deathTime++;
			}
		}
		else
			deathTime = (int)Math.ceil(me().getHP() / (me().getDamage() * 1.5));
		int timeToRefuge = targetSelector.timeOnPathToRefuge(me().getID());
		//cyw
//		if (deathTime <= timeToRefuge + 2) {
		if (deathTime >= timeToRefuge + 2 && deathTime <= timeToRefuge + 4) {
//			cywEnd
			currentTask = AmbulanceTeamTasks.DYING;
			return true;
		}
		else return false;
	}
	
	
	protected void checkBurnInjury(){
		Area area = (Area)world.me.getPosition(world);
		if (area instanceof Building){
			Building building = (Building)area;
			if (building.isOnFire())
				getBurnInjury = true;
		}
	}
	
	/**
	 * To get a new task for the agent when it has no task now.
	 * @return false if there is nothing to do
	 */
	protected void getNewTask(){
		
		if (!targetSelector.isHumanForRescueEmpty()){
			currentTask = AmbulanceTeamTasks.MOVING_TO_HUMAN;
//			targetHuman = targetSelector.getTargetHuman();
			targetHuman = targetSelector.getTargetHuman(mapOfTargetHumanLocked,me().getID());
			System.out.println("me:"+me().getID()+"\ttargetHuman:"+targetHuman);
			//
			updateForMOVING_TO_HUMAN();
			return ;
		}
		if (!searchSpaceManager.isBuildingForSearchEmpty()){
			currentTask = AmbulanceTeamTasks.SEARCHING;
			updateForSEARCHING();
			return ;
		}
		currentTask = AmbulanceTeamTasks.RESCUE_OVER;
		return ;
	}
	
	protected void executeForLOADING() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  executeForLOADING");
		if (lastTask != AmbulanceTeamTasks.LOADING){
			load(targetHuman);
			return ;
		}
		//if There are lot of refuse,not zero. 
		if (!world.getEntitiesOfType(StandardEntityURN.REFUGE).isEmpty()){
			//arrive refuse.
			if (location() instanceof Refuge){
	//			System.out.println(targetHuman);
				targetHuman = null;
				unload();
				return ;
			}
		}
		else{
			EntityID areaID = world.me.getPosition();
			StandardEntity se = (Area)world.getEntity(areaID);
			if (se.getURN().equals(StandardEntityURN.ROAD.toString())){
				targetHuman = null;
				unload();
				return ;
			}
		}
		if (targetHuman.getHP() == 0){
			targetHuman = null;
			unload();
			return ;
		}
		if (targetSelector.lifeTime(targetHuman.getID()) <= 2 && 
				!world.me.getPosition(world).getURN().equals(StandardEntityURN.REFUGE.toString())){
//			System.out.println(world.me + "   Time : " + world.getTime() + "   unload#1#");
			targetHuman = null;
			unload();
			return ;
		}
		if (targetSelector.lifeTime(targetHuman.getID()) <= targetSelector.timeOnPathToRefuge(targetHuman.getID())){
//			System.out.println(world.me + "   Time : " + world.getTime() + "   unload#2#");
			targetHuman = null;
			unload();
			return;
		}
//		if (BHI_processer.getBuriedHumanInfo(targetHuman.getID()).isDamageSure() &&
//				BHI_processer.getBuriedHumanInfo(targetHuman.getID()).getDamageType().equals(DamageTypeEnum.none_slight) &&
//						world.me.getPosition(world).getURN().equals(StandardEntityURN.ROAD.toString())){
//			System.out.println(world.me + "   Time : " + world.getTime() + "   unload#3#");
//			targetHuman = null;
//			unload();
//			return;
//		}
//		System.out.println("load target : " + targetHuman);
		
		if (!world.getEntitiesOfType(StandardEntityURN.REFUGE).isEmpty()){
			List<EntityID> path = router.getMultiDest(location(), 
					world.getEntitiesOfType(StandardEntityURN.REFUGE), 
					router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
					new Point(me().getX(), me().getY()));
			if (path.size() > 1)
				nextPassArea = path.get(1);
			move(path);
			return;
		}
		else {
			Set<Road> roads = world.getEntrance().getEntrance((Building)world.me.getPosition(world));
			List<EntityID> path = router.getMultiDest(location(), 
					roads, 
					router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
					new Point(me().getX(), me().getY()));
			if (path.size() > 1)
				nextPassArea = path.get(1);
			move(path);
		}
//		move(refuges , router.getAtCostFunction(searchSpaceManager.getMinStaticCost()));
	}
	
	protected void executeForRESCUING() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  rescue target : " + targetHuman);
		world.getBuriedHumans().addRescuedHuman(targetHuman.getID());
		world.getBuriedHumans().addRescuingHuman(targetHuman.getID());
		
		System.out.println("targetHuman:"+targetHuman.getID()+" now is rescuing.");
		rescue(targetHuman);
	}
	
	protected void executeForSEARCHING() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  executeForSEARCHING");
		
		List<EntityID> path = router.getMultiDest(location(), 
				searchSpaceManager.getSearchingDestination(), 
				router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
				new Point(me().getX(), me().getY()));
		if (path.size() > 1)
			nextPassArea = path.get(1);
		move(path);
		
//		move(searchSpaceManager.getSearchingDestination() , router.getAtCostFunction(searchSpaceManager.getMinStaticCost()));
	}
	
	private void executeForMOVING_TO_HUMAN() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  executeForMOVING_TO_HUMAN");
		
//		System.out.println(world.me + "   Time : " + world.getTime() + "  movingToHuman : " + targetHuman);
		StandardEntity position = targetHuman.getPosition(world);
		List<EntityID> path = router.getAStar(me(), 
				(Area) position, 
				router.getAtCostFunction(searchSpaceManager.getMinStaticCost()));
		if (path.size() > 1)
			nextPassArea = path.get(1);
		move(path , targetHuman.getLocation(world));
		
//		move(targetHuman , router.getAtCostFunction(searchSpaceManager.getMinStaticCost()));
	}
	
	private void executeForSTUCK() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  executeForSTUCK");
//		randomWalk();
		rest();
	}
	
	private void executeForBURIED() throws ActionCommandException{
//		System.out.println(me() + " time : " + time + "  executeForBURIED");
		rest();
	}	
	
	/**
	 * if dying,move to refuse.
	 * @throws csu.agent.Agent.ActionCommandException
	 */
	private void executeForDYING() throws csu.agent.Agent.ActionCommandException {
		if (me().getPosition(world).getURN().equals(StandardEntityURN.REFUGE.toString()))
			rest();
		else {
			List<EntityID> path = router.getMultiDest(location(), 
					world.getEntitiesOfType(StandardEntityURN.REFUGE), 
					router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
					new Point(me().getX(), me().getY()));
			if (path.size() > 1)
				nextPassArea = path.get(1);
			move(path);
		}
	}
	
	private void executeForRESCUE_OVER() throws csu.agent.Agent.ActionCommandException{
		if (!world.getEntitiesOfType(StandardEntityURN.REFUGE).isEmpty()){
			if (me().getPosition(world).getURN().equals(StandardEntityURN.REFUGE.toString()))
				rest();
			else {
				List<EntityID> path = router.getMultiDest(location(), 
						world.getEntitiesOfType(StandardEntityURN.REFUGE), 
						router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
						new Point(me().getX(), me().getY()));
				if (path.size() > 1)
					nextPassArea = path.get(1);
				move(path);
			}
		}
		else{
			if (me().getPosition(world).getURN().equals(StandardEntityURN.ROAD.toString()))
				rest();
			else{
				Set<Road> roads = world.getEntrance().getEntrance((Building)world.me.getPosition(world));
				List<EntityID> path = router.getMultiDest(location(), 
						roads, 
						router.getAtCostFunction(searchSpaceManager.getMinStaticCost()) , 
						new Point(me().getX(), me().getY()));
				if (path.size() > 1)
					nextPassArea = path.get(1);
				move(path);
			}
		}
	}
	
	protected void cantMove() throws csu.agent.Agent.ActionCommandException {
	}
	
	/**
	 * To update the freezer , and add new EntityID into it when the AT is blocked.
	 */
	private void updateForFreezer(){
		freezer.updateFreezingTime();
//		System.out.println(world.me + "   Time : " + world.getTime() + "  Freeze : " + freezer.getFreeze());
		if (isBlocked()){
//			System.out.println(world.me + "   Time : " + world.getTime() + " is blocked");
			switch(lastTask){
			case LOADING : 
			case SEARCHING :
				freezer.addNewfreezingStuff(Freeze_Tab.AREA , nextPassArea, 3);
				break;
			case MOVING_TO_HUMAN :
				freezer.addNewfreezingStuff(Freeze_Tab.HUMAN , targetHuman.getID() , 3);
				break;
			default :
				break;
			}
		}
	}
}
