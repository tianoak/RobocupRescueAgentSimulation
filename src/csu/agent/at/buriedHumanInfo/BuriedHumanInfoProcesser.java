package csu.agent.at.buriedHumanInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;
import csu.model.AdvancedWorldModel;
import csu.model.BuriedHumans;
import csu.model.ConfigConstants;
import csu.standard.HumanInformation;

/**
 * This class used to manage all the BuriedHumanInfo and DamageType , 
 * and to offer the interface for the Ambulance Team to decide the target
 * @author nale
 *
 */
public class BuriedHumanInfoProcesser {

	/** the injury values for collapse damage*/
	private int[] collapseDamageValues;
	
	/** the injury values for bury damage*/
	private int[] buryDamageValues;
	
	/** the injury rates for collapse damage in three kinds of building*/
	private double[][] collapseDamageRates;
	
	/** the injury rates for bury damage in three kinds of buildings*/
	private double[][] buryDamageRates;
	
	/** the array of damage type*/
	private static ArrayList<DamageType> damageTypes;

	/** the set of buried human who should be rescued*/
	private Map<EntityID , BuriedHumanDamageInfo> buriedHumanInfos;
	
	/** the configConstants used to get parameters*/
	private ConfigConstants config;
	
	/** the world model*/
	private AdvancedWorldModel world;
	
	/** the BuriedHuman*/
	private BuriedHumans buriedHumans;

	/** the AmbulanceTeam the agent controls*/
	private AmbulanceTeam me;
	
	public BuriedHumanInfoProcesser(AdvancedWorldModel world){
		this.world = world;
		this.buriedHumans = world.getBuriedHumans();
		this.config = world.getConfig();
		this.me = (AmbulanceTeam) world.me;
		initial();
//		print();
	}
	
	/**
	 * To initial this BuriedHumanInfoProcesser , 
	 * mainly to obtain the parameters from the config
	 */
	public void initial(){
		
		collapseDamageValues = new int[4];
		collapseDamageValues[0] = 0;
		collapseDamageValues[1] = config.collapse_slight;
		collapseDamageValues[2] = config.collapse_serious;
		collapseDamageValues[3] = config.collapse_critical;
		
		buryDamageValues = new int[4];
		buryDamageValues[0] = 0; 
		buryDamageValues[1] = config.bury_slight;
		buryDamageValues[2] = config.bury_serious;
		buryDamageValues[3] = config.bury_critical;
		
		collapseDamageRates = new double[3][4];
		collapseDamageRates[0][1] = config.collapse_wood_slight;
		collapseDamageRates[0][2] = config.collapse_wood_serious;
		collapseDamageRates[0][3] = config.collapse_wood_critical;
		collapseDamageRates[0][0] = 1 - collapseDamageRates[0][1] - collapseDamageRates[0][2] - collapseDamageRates[0][3];
		collapseDamageRates[1][1] = config.collapse_steel_slight;
		collapseDamageRates[1][2] = config.collapse_steel_serious;
		collapseDamageRates[1][3] = config.collapse_steel_critical;
		collapseDamageRates[1][0] = 1 - collapseDamageRates[1][1] - collapseDamageRates[1][2] - collapseDamageRates[1][3];
		collapseDamageRates[2][1] = config.collapse_concrete_slight;
		collapseDamageRates[2][2] = config.collapse_concrete_serious;
		collapseDamageRates[2][3] = config.collapse_concrete_critical;
		collapseDamageRates[2][0] = 1 - collapseDamageRates[2][1] - collapseDamageRates[2][2] - collapseDamageRates[2][3];
		
		buryDamageRates = new double[3][4];
		buryDamageRates[0][1] = config.bury_wood_slight;
		buryDamageRates[0][2] = config.bury_wood_serious;
		buryDamageRates[0][3] = config.bury_wood_critical;
		buryDamageRates[0][0] = 1 - buryDamageRates[0][1] - buryDamageRates[0][2] - buryDamageRates[0][3];
		buryDamageRates[1][1] = config.bury_steel_slight;
		buryDamageRates[1][2] = config.bury_steel_serious;
		buryDamageRates[1][3] = config.bury_steel_critical;
		buryDamageRates[1][0] = 1 - buryDamageRates[1][1] - buryDamageRates[1][2] - buryDamageRates[1][3];
		buryDamageRates[2][1] = config.bury_concrete_slight;
		buryDamageRates[2][2] = config.bury_concrete_serious;
		buryDamageRates[2][3] = config.bury_concrete_critical;
		buryDamageRates[2][0] = 1 - buryDamageRates[2][1] - buryDamageRates[2][2] - buryDamageRates[2][3];
		
		damageTypes = new ArrayList<DamageType>(12);
		for (int index = 0 ; index < 12 ; index ++){
			DamageType newType = new DamageType(index , collapseDamageValues[index/4] , buryDamageValues[index%4] , config);
			damageTypes.add(index , newType);
		}
		buriedHumanInfos = new HashMap<EntityID , BuriedHumanDamageInfo>();
	}

	/**
	 * To get the information of buried human from world model，
	 * and add new buried human from world model and communication
	 */
	public void update(){
		//read new BHI
		for (Pair<EntityID , Integer> BHIPair :buriedHumans.getNewBuriedHumanInfos()){
			EntityID humanID = BHIPair.first();
			if (!buriedHumanInfos.containsKey(humanID)){
				createNewBuriedHumanInfo(humanID);
			}
			BuriedHumanDamageInfo BHI = buriedHumanInfos.get(humanID);
			if (!BHI.isDamageSure()){
				BHI.setDamageType(BHIPair.second());
				BHI.damageTypeSure();
			}
			else if (BHI.getDamageTypeIndex() != BHIPair.second()){
//				System.out.println("Damage type from communication error!!!");
			}
		}
		//read buried human from changSet
		for (EntityID humanID : buriedHumans.getBuriedHumanFromChangeSet()){
			Human human = (Human)world.getEntity(humanID);
			if (!human.isPositionDefined())
				continue ;
			if (!(human.getPosition(world) instanceof Building))
				continue ;
			if (!buriedHumanInfos.containsKey(humanID)){
				createNewBuriedHumanInfo(humanID);
			}
			updateBuriedHumanInfo(humanID , world.getTime()-1);
			//TODO TEST
//			System.out.println(humanID + " from buriedHumanFromChangSet");
		}
		//read buried human from voice of civilian
		for (EntityID humanID : buriedHumans.getBuriedHumanFromVoiceOfCivilian()){
			Human human = (Human)world.getEntity(humanID);
			if (!human.isPositionDefined())
				continue ;
			if (!(human.getPosition(world) instanceof Building))
				continue ;
			if (!buriedHumanInfos.containsKey(humanID)){
				createNewBuriedHumanInfo(humanID);
			}
			updateBuriedHumanInfo(humanID , world.getTime()-1);
			//TODO TEST
//			System.out.println(humanID + " from buriedHumanFromVoiceOfCivilian");
		}
		//read buried human from communication
		for (EntityID humanID : buriedHumans.getBuriedHumanFromCommunication().keySet()){
			Human human = (Human)world.getEntity(humanID);
			if (!human.isPositionDefined())
				continue ;
			if (!(human.getPosition(world) instanceof Building))
				continue ;
			if (!buriedHumanInfos.containsKey(humanID)){
				createNewBuriedHumanInfo(humanID);
			}
			Map<Integer , HumanInformation> humanInfos = buriedHumans.getBuriedHumanFromCommunication().get(humanID);
			for (Integer timeStep : humanInfos.keySet()){
				HumanInformation humanInfo = humanInfos.get(timeStep);
				int hp = humanInfo.getHP();
				int damage = humanInfo.getDamage();
				updateBuriedHumanInfo(humanID , hp , damage , timeStep);
			}
			//TODO TEST
//			System.out.println(humanID + " from buriedHumanFromCommunication");
		}
	}

	
	/**
	 * create a BuriedHuman for the new buried human 
	 * @param human
	 */
	public void createNewBuriedHumanInfo(EntityID humanID){
		Human human = (Human)world.getEntity(humanID);
		BuriedHumanDamageInfo newBHInfo = new BuriedHumanDamageInfo(human , damageTypes);
		buriedHumanInfos.put(human.getID() , newBHInfo);
	}
	
	/**
	 * To get the set: buriedHumanInfos.keySet()
	 * @return
	 */
	public Set<EntityID> getAllBuriedHuman(){
		return buriedHumanInfos.keySet();
	}
	
	/**
	 * To get the BuriedHumanInfo with special humanID
	 * @param huamnID : the specical humanID
	 * @return
	 */
	public BuriedHumanDamageInfo getBuriedHumanInfo(EntityID huamnID){
		return buriedHumanInfos.get(huamnID);
	}
	
	/**
	 * To get DamageType with special index
	 * @param DamageTypeIndex : the special index
	 * @return
	 */
	public DamageType getDamageType(int damageTypeIndex){
		return damageTypes.get(damageTypeIndex);
	}
	
	/**
	 * To update the special BuriedHumanInfo
	 * @param BHInfo
	 * @param timeStep
	 */
	public void updateBuriedHumanInfo(EntityID humanID , int timeStep){
		BuriedHumanDamageInfo BHI = buriedHumanInfos.get(humanID);
		Human human = BHI.getHuman();
		if (!BHI.isDamageSure()){
//			System.out.println(human+"      hp : " + human.getHP() + "     damage : " + human.getDamage());
			if (human.getID().equals(me.getID())){
				int roundHP = round(human.getHP() , world.getConfig().hpPrecision);
				int roundDamage = round(human.getDamage() , world.getConfig().damagePrecision);
				matchDamageType(BHI , roundHP , roundDamage , timeStep);
			}
			else matchDamageType(BHI , human.getHP() , human.getDamage() , timeStep);
			//TODO TEST
//			System.out.println(humanID + " possible damageType : ");
//			for (DamageType damageType : BHI.getPossibleDamage())
//				System.out.println(damageType);
		}
		else{
//			System.out.println(humanID + "  damageType : " + damageTypes.get(BHI.getDamageTypeIndex()));
			return ;
		}
	}
	
	/**
	 * To update the special BuriedHumanInfo
	 * @param humanID
	 * @param hp the hp in the special time step
	 * @param damage the damage in the special time step
	 * @param timeStep the special time step
	 */
	public void updateBuriedHumanInfo(EntityID humanID , int hp ,int damage , int timeStep){
		BuriedHumanDamageInfo BHI = buriedHumanInfos.get(humanID);
		if (!BHI.isDamageSure()){
			matchDamageType(BHI , hp , damage , timeStep);
		}
		else return ;
	}
	
	
	/**
	 * To get the most possible damage type or the exact damage type of the buried human <br>
	 * @param BHI
	 * @param hp the hp in special time step
	 * @param damage the damage in special time step
	 * @param timeStep the special time step
	 */
	 
	public void matchDamageType(BuriedHumanDamageInfo BHI , int hp , int damage ,int timeStep){
		Set<DamageType> possibleDamage = BHI.getPossibleDamage();
		Set<DamageType> removeDamageTypes = new HashSet<DamageType>();
		Human human = BHI.getHuman();
		for (int step = 0 ; step < 2 ; step ++){
			for (Iterator<DamageType> it = possibleDamage.iterator() ; it.hasNext() ; ){
				DamageType next = it.next();
				double rate = getDamageRate(human.getPosition() , next);
				if (rate == 0.0){
					it.remove();
					continue ;
				}
				if (next.getDeathTime() + 5 < world.getTime()){
					it.remove();
					continue ;
				}
				if (next.matchToState(hp , damage , timeStep) == MatchResult.UNMATCH){
					if (step < 1)
						removeDamageTypes.add(next);
					it.remove();
				}
			}
			if (possibleDamage.size() > 1){
				Iterator<DamageType> it = possibleDamage.iterator();
				DamageType mostPossible = null;
				double maxRate = 0.0;
				while (it.hasNext()){
					DamageType next = it.next();
					double nextRate = getDamageRate(human.getPosition() , next);
					if (maxRate <= nextRate){
						mostPossible = next;
						maxRate = nextRate;
					}
				}
				BHI.setDamageType(mostPossible.getDamageTypeIndex());
//				System.out.println(human + "Temporary damage type is : " + mostPossible);
				break;
			}
			else if (possibleDamage.size() == 1){
				BHI.damageTypeSure();
				BHI.setDamageType(possibleDamage.iterator().next().getDamageTypeIndex());
				buriedHumans.addBHI(human.getID() , BHI.getDamageTypeIndex());
//				System.out.println(human + "Real damage type is : " + possibleDamage.iterator().next());
				break;
			}
			else {
				if (step < 1){
//					System.out.println(human + "DamageType Match Error in first step!");
					possibleDamage.addAll(damageTypes);
					possibleDamage.remove(removeDamageTypes);
					continue;
				}
				else {
//					System.out.println(human + "DamageType Match Error in second step!");
					possibleDamage.addAll(damageTypes);
					Iterator<DamageType> it = possibleDamage.iterator();
					DamageType mostPossible = null;
					double maxRate = 0.0;
					while (it.hasNext()){
						DamageType next = it.next();
						double nextRate = getDamageRate(human.getPosition() , next);
						if (maxRate <= nextRate){
							mostPossible = next;
							maxRate = nextRate;
						}
					}
					BHI.setDamageType(mostPossible.getDamageTypeIndex());
//					System.out.println(human + "Temperary damage type is : " + mostPossible);
				}
			}
		}
	}
	
	/**
	 * To return the rate of the damage type which the human probably get
	 * @param human
	 * @param damageType
	 * @return
	 */
	public double getDamageRate(EntityID posID , DamageType damageType){
		
		Building position = (Building)world.getEntity(posID);
		int buildingCode = position.getBuildingCode();
		int index = damageTypes.indexOf(damageType);//TODO test
		double collapseDamageRate = collapseDamageRates[buildingCode][index/4];
		double buryDamageRate = collapseDamageRates[buildingCode][index%4];
		return collapseDamageRate * buryDamageRate;
	}
	

	/**
	 * To print the information of all damage type
	 */
	public void print(){
		System.out.println("==================================================Collapse    Bury==================================================");
    	System.out.println("==================================================HP        Damage==================================================");
    	System.out.println("time     none_none     none_slight     none_serious     none_critical");
    	printDamageType(0 , 4);
    	System.out.println("time     slight_none     slight_slight     slight_serious     slight_critical");
    	printDamageType(4 , 8);
    	System.out.println("time     serious_none     serious_slight     serious_serious     serious_critical");
    	printDamageType(8 , 12);
	}
	
	public void printDamageType(int from , int to){
		for (int timeStep = 1 ; timeStep <= 300 ; timeStep ++){
			System.out.print(timeStep+"   ");
			for (int i = from ; i < to ; i ++){
				DamageType damageType = damageTypes.get(i);
				System.out.print(damageType.getHP(timeStep)+"  "+damageType.getMinHP(timeStep)+"  "+
				damageType.getDamage(timeStep)+"  "+damageType.getMaxDamage(timeStep)+"    ");
			}
			System.out.println();
		}
	}
	
	public int round(int value , int precision){
		int remainder = value % precision;
		value -= remainder;
		if (remainder >= precision / 2)
			value += precision;
		return value;
	}
	
	
	public boolean isBuriedHuman(EntityID humanID){
		return buriedHumanInfos.containsKey(humanID);
	}

	public static boolean isStateOfDamageType(int hp , int damage , int timeStep){
		for (DamageType damageType : damageTypes){
			if (damageType.matchToState(hp, damage, timeStep) == MatchResult.MATCH)
				return true;
		}
		return false;
	}
}
