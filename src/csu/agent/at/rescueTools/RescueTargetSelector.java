package csu.agent.at.rescueTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import java.util.Map;
import csu.agent.at.buriedHumanInfo.BuriedHumanDamageInfo;
import csu.agent.at.buriedHumanInfo.BuriedHumanInfoProcesser;
import csu.agent.at.buriedHumanInfo.DamageType;
import csu.agent.at.cluster.Cluster;
import csu.agent.at.cluster.Clustering;
import csu.agent.at.rescueTools.EntityFreezer.Freeze_Tab;
import csu.agent.at.sortTools.ElementWithDistance;
import csu.agent.at.sortTools.GeneralSorter;
import csu.model.AdvancedWorldModel;
import csu.standard.EntityIdComparator;
import csu.standard.Ruler;

public class RescueTargetSelector {
	
	/** the world model*/
	private AdvancedWorldModel world;
	
	//cyw  judge if the targetHuman is not rescued or loaded by the other AT.putAll
	protected static Map<EntityID, EntityID> mapOfTargetHumanLocked = new HashMap<EntityID, EntityID>();
	
	/** The index of the cluster which the agent assigned to at the beginning*/
	protected int assignedClusterIndex ;

	/** the current cluster the agent belongs to */
	private Area currentClusterCenter;
	
	/** The index of the cluster which the agent currently locate*/
	private int currentClusterIndex;
	
	/** The clusters of map generate by KMeans-Plus-Plus */
	private final ArrayList<Cluster> clusters;
	
	/** To store the number of buried human in each cluster*/
	protected HashMap<Integer , Integer> numberOfBuriedHumanInCluster;
	
	/** To store all the died human*/
	private Collection<EntityID> allDiedHuman;
	
	/** To store all the rescued human*/
	private Collection<EntityID> allRescuedHuman;
	
	/** To store all the civilian in refuge*/
	private Set<EntityID> needlessHumanForRescue;
	
	/** To store the buried humans in the cluster the agent currently located in and its neighbor clusters*/
	private Set<EntityID> currentHumanForRescue;
	
	/** To store the buried human who can be rescued safely*/
	private Set<EntityID> humanForEasyRescue;
	
	/** To store all the human who need to be rescued*/
	private Set<EntityID> allHumanForRescue;
	
	/** To store all the agent who need to be rescued*/
	private ArrayList<EntityID> allAgentForRescue;
	
	/** To store all the human who are removed from currentHumanForRescue*/
	private Set<EntityID> removedHuman;
	
//	/** To stroe all the human who are in danger for closing to fire*/
//	private Set<EntityID> inDangerHuman;	
	
	/** the target id for rescue*/
	private EntityID targetID;
	
	/** To store all the refuges*/
	private Collection<StandardEntity> allRefuges;
	
	/** The BuriedHumanInfoProcesser*/
	private BuriedHumanInfoProcesser BHI_processer;
	
	/** The EntityFreezer*/
	private EntityFreezer freezer;
	
	/** True if all the buildings in the assigned cluster have been searched */
	private boolean isAssignedClusterNearlyClear;
	
	/** True if the number of victim needed to be rescued is larger than the number of AT*/
	private boolean manyVictim;
	
	/** the mean distance of the agent travel per time step*/
	private final int VELOCITY = 15700;

	/** The minimum number of AT to rescue an agent*/
	private final int NUMBER_OF_AT_FOR_AGENT = 4;
	
	/** The time step over which a large number of victim will die */
	private final int DEATH_PERIOD = 170;
	
//	/** The dangerous temperature upon which a building will soon be on fire*/
//	private final int DANGEROUS_TEMPERATURE = 35;
	
//	/** The range to the victim within which the victim is dangerous*/
//	private final int DANGEROUS_RANGE = 100000;
	
	/** The time precision*/
	private final int TIME_PRECISION = 5;
	
	public RescueTargetSelector(AdvancedWorldModel world , ArrayList<Cluster> clusters , 
			int assignedClusterIndex , BuriedHumanInfoProcesser BHI_processer , EntityFreezer freezer){
		this.world = world;
		this.currentClusterCenter = clusters.get(currentClusterIndex).getCenterEntity();
		this.assignedClusterIndex = assignedClusterIndex;
		this.currentClusterIndex = assignedClusterIndex;
		this.clusters = clusters;
		this.BHI_processer = BHI_processer;
		this.freezer = freezer;
		targetID = null;
		currentHumanForRescue = new HashSet<EntityID>();
		humanForEasyRescue = new HashSet<EntityID>();
		allHumanForRescue = new HashSet<EntityID>();
		needlessHumanForRescue = world.getBuriedHumans().getNeedlessHuman();
		allDiedHuman = world.getBuriedHumans().getAllDiedHuman();
		allRescuedHuman = world.getBuriedHumans().getAllRescuedHuman();
		allRefuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
		removedHuman = new HashSet<EntityID>();
		allAgentForRescue = new ArrayList<EntityID>();
//		inDangerHuman = new HashSet<EntityID>();
	}
	
	public void update(ChangeSet changed , Set<EntityID> needlessBuildingForRescue , boolean isAssignedClusterNearlyClear){
		this.isAssignedClusterNearlyClear = isAssignedClusterNearlyClear;
		findLoadingAT(changed);
		updateAllHumanForRescue(needlessBuildingForRescue);
		if (allHumanForRescue.isEmpty())
			return ;
//		getInDangerHuman();
		checkIfManyVictim();
		updateHumanForEasyRescue();
		updateHumanForRescue();
	}
	
	/**
	 * To find the AT who are loading civilian to refuge .
	 * If found , throw it into the freezer.
	 * @param changed
	 */
	public void findLoadingAT(ChangeSet changed){
		for (EntityID entityID : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(entityID);
			if (!se.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()))
				continue ;
			Human at = (Human)se;
			Human civilian = someoneOnBoard(at);
			if (civilian != null && !freezer.containStuff(Freeze_Tab.LOADINGAT , entityID)){
				freezer.addNewfreezingStuff(Freeze_Tab.LOADINGAT , entityID , (int)timeOnPathToRefuge(civilian.getID()));
			}
		}
	}
	
	/**
	 * To filter the buried human by checking whether they are not died or rescued or their position are dangerous
	 */
	public void updateAllHumanForRescue(Set<EntityID> needlessBuildingForRescue){
		allHumanForRescue.addAll(BHI_processer.getAllBuriedHuman());
		for (Iterator<EntityID> iterator = allHumanForRescue.iterator() ; iterator.hasNext() ;){
			EntityID humanID = iterator.next();
			Human human = (Human) world.getEntity(humanID);
			if (allDiedHuman.contains(humanID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#1#" + world.getEntity(humanID));
				continue;
			}
			if (allRescuedHuman.contains(humanID) ){
				if (targetID == null){
					iterator.remove();
//					System.out.println(world.me + "   Time : " + world.getTime() + "  remove#2#" + world.getEntity(humanID));
					continue;
				}
				else if (!targetID.equals(humanID)){
					iterator.remove();
//					System.out.println(world.me + "   Time : " + world.getTime() + "  remove#3#" + world.getEntity(humanID));
					continue;
				}
			}
			if (needlessHumanForRescue.contains(humanID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#4#" + world.getEntity(humanID));
				continue;
			}
			if (removedHuman.contains(humanID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#5#" + world.getEntity(humanID));
				continue;
			}
			if (!human.isPositionDefined() || human.getPosition() == null){
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#6#" + world.getEntity(humanID));
				iterator.remove();
				if (targetID != null && targetID.equals(humanID))
					targetID = null;
				continue;
			}
			EntityID posID = human.getPosition();
			if (!(human.getPosition(world) instanceof Building)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#7#" + world.getEntity(humanID));
				continue;
			}
			if (human.getPosition(world).getURN().equals(StandardEntityURN.REFUGE.toString())){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#8#" + world.getEntity(humanID));
				continue ; 
			}
			if (needlessBuildingForRescue.contains(posID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#9#" + world.getEntity(humanID));
				continue;
			}
			Set<EntityID> warmBuildings = freezer.getEntitiesWithTab(Freeze_Tab.WARM_BUILDING);
			if (warmBuildings != null && warmBuildings.contains(posID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#10#" + world.getEntity(humanID));
				continue;
			}
			if (lifeTime(humanID) <= 3){
				iterator.remove();
//				System.out.println(world.me + "  Time : " + world.getTime() + "  " + human + "  damage Type : " + 
//				BHI_processer.getBuriedHumanInfo(humanID).getDamageTypeIndex());
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#11#" + world.getEntity(humanID));
				continue ;
			}
			if (lifeTime(humanID) <= timeOnPathToRefuge(humanID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#12#" + world.getEntity(humanID));
				continue ;
			}
//			BuriedHumanDamageInfo bhi = BHI_processer.getBuriedHumanInfo(humanID);
//			if (isInSafeBuilding(humanID) && world.getTime() < 220 && bhi.isDamageSure()){
//				if (bhi.getDamageType().equals(DamageTypeEnum.slight_none) ||
//						bhi.getDamageType().equals(DamageTypeEnum.none_slight) ||
//						bhi.getDamageType().equals(DamageTypeEnum.none_serious)){
//					iterator.remove();
//					System.out.println(world.me + "   Time : " + world.getTime() + "  remove#13#" + world.getEntity(humanID));
//					continue ;
//				}
//			}
		}
	}
	
//	public void getInDangerHuman(){
//		for (EntityID humanID : allHumanForRescue){
//			if (!isInSafeBuilding(humanID)){
//				inDangerHuman.add(humanID);
//			}
//		}
//	}
	
	public void updateHumanForEasyRescue(){
		humanForEasyRescue.addAll(allHumanForRescue);
		for (Iterator<EntityID> iterator = humanForEasyRescue.iterator() ; iterator.hasNext() ;){
			EntityID humanID = iterator.next();
			if (!canBeRescued(humanID)){
				iterator.remove();
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#14#" + world.getEntity(humanID));
				continue;
			}
			if (shouldRemoveIfEnough(humanID)){
//				System.out.println(world.me + "   Time : " + world.getTime() + "  remove#15#" + world.getEntity(humanID));
				iterator.remove();
				continue ;
			}
		}
	}
	
	public void updateHumanForRescue(){
		if (humanForEasyRescue.isEmpty()){
			if (isAssignedClusterNearlyClear || manyVictim || world.getTime() > DEATH_PERIOD)
				humanForEasyRescue.addAll(allHumanForRescue);
			else 
				return ;
		}
		updateNumberOfBuriedHumanInCluster();
		updateCurrentHumanForRescue();
		if (currentHumanForRescue.isEmpty()){
			if (isAssignedClusterNearlyClear || manyVictim || world.getTime() > DEATH_PERIOD){
				updateRescueSpace();
				updateCurrentHumanForRescue();
			}
			else 
				return ;
		}
	}
	
	public void checkIfManyVictim(){
		int numberOfVictim = allHumanForRescue.size();
		int numberOfAT = world.getAmbulanceTeamIdList().size();
		if (numberOfVictim > 0.57 * numberOfAT)
			manyVictim = true;
		else 
			manyVictim = false;
	}
	
	/**
	 * To update human for rescue
	 */
	public void updateCurrentHumanForRescue(){//TODO for agent
		allAgentForRescue.clear();
		if (currentClusterIndex == -1){
//			System.out.println(world.me + "   Time : " + world.getTime() + "  currentClusterIndex == -1");
			return ;
		}
		for (EntityID humanID : humanForEasyRescue){
			Human human = (Human)world.getEntity(humanID);
			Building pos = (Building)human.getPosition(world);
			if (!human.getURN().equals(StandardEntityURN.CIVILIAN.toString())){
				allAgentForRescue.add(humanID);
				continue;
			}
			if (targetID != null && humanForEasyRescue.contains(targetID)){
				currentHumanForRescue.add(targetID);
				continue;
			}
			int distanceFromCenterToTarget = world.getDistance(pos , currentClusterCenter);
			int distanceFromMeToTarget = world.getDistance(pos , world.me);
			if (distanceFromCenterToTarget <= 1.0 * Clustering.MAX_RADIUS){
				currentHumanForRescue.add(humanID);
				continue ;
			}
			if (distanceFromCenterToTarget <= 2.5 * Clustering.MAX_RADIUS &&  distanceFromMeToTarget <= 1.5 * Clustering.MAX_RADIUS){
				currentHumanForRescue.add(humanID);
				continue ;
			}
			if (distanceFromMeToTarget <= 1.0 * Clustering.MAX_RADIUS){
				currentHumanForRescue.add(humanID);
				continue ;
			}
		}
		if (!allAgentForRescue.isEmpty())
			assignAgentToAT();
		Set<EntityID> freezingHuman = freezer.getEntitiesWithTab(Freeze_Tab.HUMAN);
		if (freezingHuman == null)
			return ;
		currentHumanForRescue.removeAll(freezingHuman);
		if (currentHumanForRescue.isEmpty()){
			for (EntityID humanID : freezingHuman){
				if (humanForEasyRescue.contains(humanID))
					currentHumanForRescue.add(humanID);
			}
			freezer.removeEntityWithTab(Freeze_Tab.HUMAN);
		}
	}
	
	/**
	 * To assign the buried agents to AT
	 */
	protected void assignAgentToAT(){
		Collections.sort(allAgentForRescue , new EntityIdComparator());
		HashSet<EntityID> availableAT = new HashSet<EntityID>();
		ArrayList<ElementWithDistance> distancesFromAgentToAT = new ArrayList<ElementWithDistance>();
		for (StandardEntity se : world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)){
			Human at = (Human)se;
			if (!allAgentForRescue.contains(at.getID()) && at.isPositionDefined())
				availableAT.add(at.getID());
		}
//		System.out.println(world.me + "  Time : " + world.getTime() + " availableAT : " + availableAT);
		for (EntityID agentID : allAgentForRescue){
			Human agent = (Human)world.getEntity(agentID);
			int distance;
			distancesFromAgentToAT.clear();
			for (EntityID atID : availableAT){
				Human at = (Human)world.getEntity(atID);
				distance = world.getDistance(agent.getPosition() , at.getPosition());
				distancesFromAgentToAT.add(new ElementWithDistance(atID , distance));
			}
			Collections.sort(distancesFromAgentToAT , new GeneralSorter());
			for (int i = 0 ; i < Math.min(NUMBER_OF_AT_FOR_AGENT , distancesFromAgentToAT.size()); i++){
				EntityID atID = distancesFromAgentToAT.get(i).getEntityID();
				if (world.me.getID().equals(atID)){
//					System.out.println(world.me + "   Time : " + world.getTime() + " I shoule rescue the agent " + agent);
					currentHumanForRescue.add(agentID);
					return ;
				}
			}
		}
	}
	
	
	/**
	 * To update the set : numberOfBuriedHumanInCluster
	 */
	public void updateNumberOfBuriedHumanInCluster(){
		numberOfBuriedHumanInCluster = new HashMap<Integer , Integer>();
		for (EntityID humanID : humanForEasyRescue){
			int index = Clustering.getCloestClusterForHuman(humanID, clusters, world);
			if (!numberOfBuriedHumanInCluster.containsKey(index))
				numberOfBuriedHumanInCluster.put(index , 0);
			numberOfBuriedHumanInCluster.put(index , numberOfBuriedHumanInCluster.get(index).intValue() + 1);
		}
	}
	
	private void updateRescueSpace(){
		currentClusterIndex = getNewRescueCluster();
		if (currentClusterIndex == -1)
			return ;
		currentClusterCenter = clusters.get(currentClusterIndex).getCenterEntity();
	}
	
	private int getNewRescueCluster(){
		int newClusterIndex ;
		ArrayList<Cluster> rescueClusters = new ArrayList<Cluster>();
		for (Integer index : numberOfBuriedHumanInCluster.keySet())
			rescueClusters.add(clusters.get(index));
		int index = Clustering.getCloestClusterForHuman(world.me.getID() , rescueClusters , world);
		if (index == -1)
			return -1;
		newClusterIndex = clusters.indexOf(rescueClusters.get(index));
		return newClusterIndex;
	}
	
	/**
	 * To get the number of ATs close to the given human
	 * @param human
	 * @return
	 */
	protected int getNearAT(Human human){
		float theNumberOfATNearby = 0;
		Set<EntityID> loadingATs = freezer.getEntitiesWithTab(Freeze_Tab.LOADINGAT);
		label : for (StandardEntity at : world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)){
			Human humanAT = (Human)at;
			if (humanAT.getID().equals(world.me.getID())){
				theNumberOfATNearby += 1;
				continue ;
			}
			if (!humanAT.isPositionDefined())
				continue ;
			if (humanAT.getPosition().equals(world.me.getPosition())){
				theNumberOfATNearby += 1;
				continue ;
			}
		   	else if (world.getDistance(humanAT.getPosition() , human.getPosition()) < Clustering.MAX_RADIUS){
				if (humanAT.isHPDefined() && humanAT.getHP() == 0)
					continue ;
				if (loadingATs != null && loadingATs.contains(at.getID()))
					continue ;
				if (allHumanForRescue.contains(humanAT.getID()))
					continue ;
				if (human.getPosition().equals(humanAT.getPosition())){
					theNumberOfATNearby += 1;
					continue ;
				}
				for (EntityID humanID : allHumanForRescue){
					Human humanBeingRescued = (Human)world.getEntity(humanID);
					if (humanBeingRescued.getPosition().equals(humanAT.getPosition())){
						theNumberOfATNearby += 0.3;
						continue label;	
					}
				}
				theNumberOfATNearby += 0.7 ;
			}
		}
		return (int)theNumberOfATNearby;
	}
	
	/**
	 * To judge whether the given human can be rescued
	 * @param human
	 * @return
	 */
	protected boolean canBeRescued(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		int minAT = getMinATToRescue(humanID);
		int theNumberOfATNearby = getNearAT(human);
//		System.out.println(world.me + "  Time : " + world.getTime() + "  for human : " + human + 
//				"  minAT : " + minAT + "  theNumberOfATNearby : " + theNumberOfATNearby);
		if (theNumberOfATNearby >= minAT)
			return true;
		else 
			return false;
	}
	
	protected boolean shouldRemoveIfEnough(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		HashSet<EntityID> ATAroundVictim = new HashSet<EntityID>();
		HashSet<EntityID> areaAroundVictim = new HashSet<EntityID>();
		int minATToRescue;
		if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()))
			minATToRescue = getMinATToRescue(humanID);
		else 
			minATToRescue = this.NUMBER_OF_AT_FOR_AGENT;
		areaAroundVictim.add(human.getPosition());
		for (EntityID negID : world.getCsuBuilding(human.getPosition()).getObservableAreas()){
			areaAroundVictim.add(negID);
			Area neg = (Area)world.getEntity(negID);
			if (neg instanceof Building)
				areaAroundVictim.addAll(world.getCsuBuilding(negID).getObservableAreas());
			else
				areaAroundVictim.addAll(world.getCsuRoad(negID).getObservableAreas());
		}
		for (StandardEntity se : world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)){
			Human at = (Human)se;
			if (allDiedHuman.contains(at))
				continue ;
			if (at.isBuriednessDefined() && at.getBuriedness() > 0)
				continue ;
			if (at.isPositionDefined() && areaAroundVictim.contains(at.getPosition()))
				ATAroundVictim.add(at.getID());
		}
		if (ATAroundVictim.size() == minATToRescue && !ATAroundVictim.contains(world.me.getID())){
//			System.out.println(world.me + "   Time : " + world.getTime() + "  true #1#  " + human);
			return true;
		}
//		System.out.println(world.me + "  Time : " + world.getTime() + "  For human : " + human + "  ATAroundVictim.size = " + ATAroundVictim.size() +
//				"  minATToRescue : " + minATToRescue);
		if (ATAroundVictim.size() > minATToRescue){
			if (!ATAroundVictim.contains(world.me.getID())){
//				System.out.println(world.me + "   Time : " + world.getTime() + "  true #2#  " + human);
				return true;
			}
			if (targetID != null && targetID.equals(humanID) && human.getPosition().equals(world.me.getPosition()))
				return false;
//			ArrayList<ElementWithDistance> distanceFromVictimToAT = new ArrayList<ElementWithDistance>();
//			for (EntityID atID : ATAroundVictim){
//				Human at = (Human)world.getEntity(atID);
//				int distance = world.getDistance(at.getPosition(), human.getPosition());
//				distanceFromVictimToAT.add(new ElementWithDistance(atID , distance));
//			}
//			Collections.sort(distanceFromVictimToAT , new GeneralSorter());
//			do {
//				EntityID removeID = distanceFromVictimToAT.remove(distanceFromVictimToAT.size() - 1).getEntityID();
//				if (removeID.equals(world.me.getID())){
////					System.out.println(world.me + "   Time : " + world.getTime() + "  true #3#  " + human);
//					return true;
//				}
//			}while (distanceFromVictimToAT.size() > minATToRescue);
			return false;
		}
		else 
			return false;
	}
	
	
//	public boolean isInSafeBuilding(EntityID humanID){
//		Human human = (Human)world.getEntity(humanID);
//		for (StandardEntity se : world.getObjectsInRange(human.getPosition() , DANGEROUS_RANGE)){
//			if (!(se instanceof Building))
//				continue ;
//			Building building = (Building)se;
//			if (building.isFierynessDefined() && building.getFierynessEnum().equals(Fieryness.HEATING))
//				return false;
//		}
//		return true;
//	}
	
	/**
	 * To know whether there are somebody need to be rescued
	 * @return
	 */
	public boolean isHumanForRescueEmpty(){
		if (currentHumanForRescue.isEmpty())
			updateHumanForRescue();
		return currentHumanForRescue.isEmpty();
	}
	
	public boolean isNeedToRescue(EntityID humanID){
		return currentHumanForRescue.contains(humanID);
	}
	
	public void clearTargetHuman(){
		removedHuman.add(targetID);
		allHumanForRescue.remove(targetID);
		humanForEasyRescue.remove(targetID);
		currentHumanForRescue.remove(targetID);
		targetID = null;
	}
	
	public EntityID getTargetID(){
		return targetID;
	}
	
	/**
	 * To get the minimum number of AT to rescue the given human completely
	 * @param human
	 * @return
	 */
	protected int getMinATToRescue(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		int lifeTime = lifeTime(humanID);
		int timeOnPath = timeOnPath(humanID);
		if (lifeTime <= timeOnPath) 
			return Integer.MAX_VALUE;
		int minATInDouble = (int)Math.ceil(human.getBuriedness()*1.0/(lifeTime - timeOnPath)) ;
//		System.out.println(world.me + "  Time : " + world.getTime() + "  " + human + "   minATInDouble : " + minATInDouble);
//		if (inDangerHuman.contains(humanID))
//			minATInDouble *= 2;
		return minATInDouble;
	}
	
	/**
	 * To get the life time of the special buried human .<br>
	 * @param BHInfo
	 * @return
	 */
	public int lifeTime(EntityID humanID){
		BuriedHumanDamageInfo BHI = BHI_processer.getBuriedHumanInfo(humanID);
		int damageTypeIndex = BHI.getDamageTypeIndex();
		DamageType damageType = BHI_processer.getDamageType(damageTypeIndex);
		int lifeTime = damageType.getDeathTime() - world.getTime();
		return lifeTime ;
	}
	

	/**
	 * before use this function,you must ensure the Buriedness
	 * property of the human has been defined 
	 * @param human
	 * @return
	 */
	public  int rescueTime(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		int cost;
		int numberOfAT ;
		int atInBuilding = 0;
		int minNeededAT ;
		for (StandardEntity se : world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)){
			Human at = (Human)se;
			if (at.isPositionDefined() && at.getPosition().equals(world.me.getPosition()) && at.getPosition(world) instanceof Building)
				atInBuilding ++;
		}
		minNeededAT = getMinATToRescue(humanID);
		numberOfAT = Math.max(minNeededAT, atInBuilding);
		cost = (int)Math.ceil(human.getBuriedness() * 1.0f / numberOfAT) + timeOnPath(humanID);
		return (int)cost;
	}

	/**
	 * get the time the agent costed from it is location 
	 * to target human
	 * @param human
	 * @return
	 */
	public int timeOnPath(EntityID humanID)
	{
		return timeOnPathToTarget(humanID) + timeOnPathToRefuge(humanID);
	}
	
	/**
	 * To get the time cost on the path to target human
	 * @param human
	 * @return
	 */
	public int timeOnPathToTarget(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		for (EntityID atID : world.getAmbulanceTeamIdList()){
			Human at = (Human)world.getEntity(atID);
			if(at.isPositionDefined() && at.getPosition().equals(human.getPosition()))
				return 0;
		}
		double distanceToHuman = 0.0;
		int time ; 
		if (!world.me.getPosition().equals(human.getPosition()) && !human.getPosition().equals(world.me.getID()))
			distanceToHuman = Ruler.getDistance(world.me.getLocation(world),human.getLocation(world));
		time = (int)Math.ceil(distanceToHuman / VELOCITY); 
		return time;
	}
	
	/**
	 * To get the time cost on the path to closest refuge
	 * @param humanID : the ID of target human
	 * @return
	 */
	public int timeOnPathToRefuge(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		int distanceToRefuge = 0;
		int time;
		if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString())){
//			if (BHI_processer.getBuriedHumanInfo(humanID).isDamageSure() && 
//					BHI_processer.getBuriedHumanInfo(humanID).getDamageType().equals(DamageTypeEnum.none_slight))
//				return 3;
			if (human.getPosition(world) instanceof Area)
				distanceToRefuge = getMinDistanceToRefuge(human.getID());
			else 
				distanceToRefuge = getMinDistanceToRefuge(world.me.getID());
		}
		time = (int)Math.ceil(distanceToRefuge * 1.0f / VELOCITY);
		return time + 2; //LOAD and UNLOAD each cost one time step
	}
	
	/**
	 * 
	 * free time of a human defined as follow:
	 * deathtime - recuetime - timeonpath
	 * @param human
	 * @return
	 */
	public  int freeTime(EntityID humanID)
	{
		int freeTime = lifeTime(humanID) - rescueTime(humanID);
		return freeTime;
	}
	
	/**
	 * get the human with the shortest rescue time  
	 * @param sortedBuriedHuman
	 * @return
	 */
	public Human getTargetHuman(){
		//if the same posotion?
		for (EntityID humanID : currentHumanForRescue){
			Human human = (Human)world.getEntity(humanID);
			if (human.getPosition().equals(world.me.getPosition())){
				targetID = humanID;
				return human;
			}
		}
		ArrayList<EntityID> buriedHuman = new ArrayList<EntityID>(currentHumanForRescue);
		BuriedHumanSorter sorter = new BuriedHumanSorter();
		//sort for buriedHuman
		Collections.sort(buriedHuman, sorter);
		EntityID rescueID = buriedHuman.get(0);
		if (sorter.getLabel(rescueID) > 1)
			return returnTargetHuman(rescueID) ;
		ArrayList<EntityID> easyRescueHuman = new ArrayList<EntityID>();
		easyRescueHuman.add(rescueID);
		int shortestRescueTime = rescueTime(buriedHuman.get(0));
		for(int index = 1 ; index < buriedHuman.size() ; index++){
			Human nextHuman =  (Human)world.getEntity(buriedHuman.get(index));
			if (round(freeTime(nextHuman.getID()) , TIME_PRECISION) > shortestRescueTime){
				return returnTargetHuman(rescueID) ;
			}
			int rescueTime = round(rescueTime(nextHuman.getID()) , TIME_PRECISION);
			if(rescueTime < shortestRescueTime){
				shortestRescueTime = rescueTime;
				rescueID = nextHuman.getID();
				easyRescueHuman.clear();
				easyRescueHuman.add(rescueID);
			}
			else if (rescueTime == shortestRescueTime)
				easyRescueHuman.add(nextHuman.getID());
				
		}
		if (easyRescueHuman.size() > 1)
			System.out.println(world.me + "  Time : " + world.getTime() + "   easyRescueHuman.size : " + easyRescueHuman.size());
		Random random = new Random(world.me.getID().getValue());
		int rescueIndex = random.nextInt(easyRescueHuman.size());
		rescueID = easyRescueHuman.get(rescueIndex);
		return returnTargetHuman(rescueID);
	}
	
	//cyw
	/**
	 * 
	 * @param map
	 * @return
	 */
	public Human getTargetHuman(Map<EntityID, EntityID> map,EntityID me){
		
		mapOfTargetHumanLocked.putAll(map);
		
		//if the same posotion?
		for (EntityID humanID : currentHumanForRescue){
			//cyw
			if(mapOfTargetHumanLocked.containsKey(humanID)){
				continue;
			}
			//cywEnd
			Human human = (Human)world.getEntity(humanID);
			if (human.getPosition().equals(world.me.getPosition())){
				targetID = humanID;
				mapOfTargetHumanLocked.put(humanID,me);
				return human;
			}
		}
		ArrayList<EntityID> buriedHuman = new ArrayList<EntityID>(currentHumanForRescue);
		BuriedHumanSorter sorter = new BuriedHumanSorter();
		//sort for buriedHuman
		Collections.sort(buriedHuman, sorter);
		EntityID rescueID = buriedHuman.get(0);
		if (sorter.getLabel(rescueID) > 1&&!mapOfTargetHumanLocked.containsKey(rescueID)){
			mapOfTargetHumanLocked.put(rescueID,me);
			return returnTargetHuman(rescueID) ;
		}
//		if (sorter.getLabel(rescueID) > 1)
//			return returnTargetHuman(rescueID) ;
		ArrayList<EntityID> easyRescueHuman = new ArrayList<EntityID>();
		easyRescueHuman.add(rescueID);
		int shortestRescueTime = rescueTime(buriedHuman.get(0));
		for(int index = 1 ; index < buriedHuman.size() ; index++){
			Human nextHuman =  (Human)world.getEntity(buriedHuman.get(index));
			if (round(freeTime(nextHuman.getID()) , TIME_PRECISION) > shortestRescueTime
					&&!mapOfTargetHumanLocked.containsKey(rescueID)){
				mapOfTargetHumanLocked.put(rescueID,me);
				return returnTargetHuman(rescueID) ;
			}
//			if (round(freeTime(nextHuman.getID()) , TIME_PRECISION) > shortestRescueTime){
//				return returnTargetHuman(rescueID) ;
//			}
			int rescueTime = round(rescueTime(nextHuman.getID()) , TIME_PRECISION);
			if(rescueTime < shortestRescueTime){
				shortestRescueTime = rescueTime;
				rescueID = nextHuman.getID();
				easyRescueHuman.clear();
				easyRescueHuman.add(rescueID);
			}
			else if (rescueTime == shortestRescueTime)
				easyRescueHuman.add(nextHuman.getID());
				
		}
		if (easyRescueHuman.size() > 1)
			System.out.println(world.me + "  Time : " + world.getTime() + "   easyRescueHuman.size : " + easyRescueHuman.size());
		Random random = new Random(world.me.getID().getValue());
		int rescueIndex = random.nextInt(easyRescueHuman.size());
		rescueID = easyRescueHuman.get(rescueIndex);
		//cyw
		while(mapOfTargetHumanLocked.containsKey(rescueID)){
			rescueIndex = random.nextInt(easyRescueHuman.size());
			rescueID = easyRescueHuman.get(rescueIndex);
			if(easyRescueHuman.size()==1)
				break;
		}
		mapOfTargetHumanLocked.put(rescueID,me);
		//
		return returnTargetHuman(rescueID);
	}
	//cywEnd
	
	public Human returnTargetHuman(EntityID rescueID){
		Human targetHuman = (Human)world.getEntity(rescueID);
		if (targetID != rescueID && currentHumanForRescue.contains(targetID)){
			freezer.addNewfreezingStuff(Freeze_Tab.HUMAN , targetID , timeOnPathToTarget(rescueID) + 3);
//			System.out.println(world.me + "   Time : " + world.getTime() + "  add " + world.getEntity(targetID) + " to freezer in getTargetHuman");
		}
		targetID = rescueID;
		return targetHuman;
	}
	
	/**
	 * To set the target human forcibly
	 * @param humanID
	 */
	public void setTargetHuman(EntityID humanID){
		targetID = humanID;
	}
	
	/**
	 * To get the minimum distance to refuge from the position of the buried civilian
	 * @param human
	 * @return
	 */
	public int getMinDistanceToRefuge(EntityID humanID){
		if (world.getEntitiesOfType(StandardEntityURN.REFUGE).isEmpty())
			return 3;
		Human human = (Human)world.getEntity(humanID);
		int minDistance = Integer.MAX_VALUE;
//		allRefuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
		for (StandardEntity refuge : allRefuges){
			int distance = world.getDistance((Area)refuge , (Area)human.getPosition(world));
			if (distance < minDistance)
				minDistance = distance;
		}
		return minDistance;
	}
	
	/**
	 * To get the civilian on the given AT
	 * @param at
	 * @return
	 */
	public Civilian someoneOnBoard(Human at) {
		for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
			Civilian civilian = (Civilian) next;
			if (civilian.isPositionDefined() && civilian.getPosition().equals(at.getID())) {
				return civilian;
			}
		}
		return null;
	}
	
	public void afterAct(){
//		System.out.println(world.me + "  Time : " + world.getTime() + "  allHumanForRescue : " + allHumanForRescue);
//		System.out.println(world.me + "  Time : " + world.getTime() + "  humanForEasyRescue : " + humanForEasyRescue);
//		System.out.println(world.me + "  Time : " + world.getTime() + "  currentHumanForRescue : " + currentHumanForRescue);
		allHumanForRescue.clear();
		humanForEasyRescue.clear();
		currentHumanForRescue.clear();
		allAgentForRescue.clear();
		if (!isAssignedClusterNearlyClear)
			currentClusterIndex = assignedClusterIndex;
	}
	
	/**
	 * The class is to create a descending order of the buried human to rescue.
	 * @author nale
	 *
	 */
	private class BuriedHumanSorter implements Comparator<EntityID>{

		@Override
		public int compare(EntityID humanID1, EntityID humanID2) {
			final int label1 = getLabel(humanID1);
			final int label2 = getLabel(humanID2);
			final int freeTime1 = round(freeTime(humanID1) , TIME_PRECISION);
			final int freeTime2 = round(freeTime(humanID1) , TIME_PRECISION); 
			final int rescueTime1 = round(rescueTime(humanID1) , TIME_PRECISION);
			final int rescueTime2 = round(rescueTime(humanID2) , TIME_PRECISION);
			
			if(label1 > label1)
				return -1;
			else if (label1 < label2)
				return 1;
			else if (freeTime1 < freeTime2)
				return -1;
			else if (freeTime1 > freeTime2)
				return 1;
			else if (rescueTime1 < rescueTime2)
				return -1;
			else if (rescueTime1 > rescueTime2)
				return 1;
			return 0;
		}
		
		/**
		 * set the label of the agent 
		 * the label used to 
		 * @param entity
		 * @return
		 */
		public int getLabel(EntityID humanID){
			Human human = (Human)world.getEntity(humanID);
//			if (inDangerHuman.contains(humanID))
//				return 5;
//			else 
				if(human.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()))
				 return 4;
			else if(human.getURN().equals(StandardEntityURN.FIRE_BRIGADE.toString()))
				 return 3;
			else if(human.getURN().equals(StandardEntityURN.POLICE_FORCE.toString()))
				 return 2;
			else 
				 return 1;
		}
	}
	private int round(int value , int precision){
		int remainder = value % precision;
		value -= remainder;
		if (remainder >= precision * 1.0f / 2)
			value += precision;
		return value;
	}
}
