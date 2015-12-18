package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.communication.CivilianVoiceListener;
import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.MessageConstant.MessageReportedType;
import csu.communication.Port;
import csu.io.BitArrayInputStream;
import csu.standard.HumanInformation;
import csu.util.BitUtil;
import csu.util.IntPropertySorter;

/**
 * This class used to send and read the newest information of the buried human 
 * @author nale
 *
 */
final public class BuriedHumans {
	
	/**To store the ChangeSet*/
	private ChangeSet changed;

	/** To store the newest buried human added only from other AT*/
	private Map<EntityID , Map<Integer , HumanInformation>> fromCommunication;
	
	/** To store the newest buried human added only from the voice of Civilian*/
	private Set<EntityID> fromVoiceOfCivilian;
	
	/** To store the newest buried human added only from the changeSet*/
	private Set<EntityID> fromChangeSet;
	
	/** To store the buried human who are needless to resuce*/
	private List<EntityID> allDiedHuman;
	
	/** To store the new died human ChangeSet*/
	private Set<EntityID> newDiedHumanFromChangeSet;
	
	/** To store the new died human from Communication*/
	private Set<EntityID> newDiedHumanFromCommunication;
	
	/** To store the total buriedHuman from the beginning of the simulation*/
	private Set<EntityID> totalBuriedHuman ;
	
	
	
	
	/** To store all the index of damage type for each buried human*/
	private List<Pair<EntityID , Integer>> allBuriedHumanInfos;
	
	/** To store the new buriedHumanInfo from communication*/
	private Set<Pair<EntityID , Integer>> buriedHumanInfosFromCommunication;
	
	
	
	
	/** To store all the rescued human*/
	private List<EntityID> allRescuedHuman ;
	
	/** To store the new RescuedHuman from me*/
	private EntityID newRescuedHumanFromMe;
	
	/** To store the new RescuedHuman from communication*/
	//TODO IT HAVE NOT BEEN USED
	private Set<EntityID> newRescuedHumanFromCommunication;
	
	
	
	/** To store the needless human ，whose position are not Building , or whose buriedness are zero*/
	private Set<EntityID> needlessHuman;
	
	
	/** To store the human who are rescuing from me*/
	private EntityID newRescuingHumanFromMe;
	
	/** To store the human who are rescuing from communication*/
	private EntityID newRescuingHumanFromCommunication;
	
	/** To store the AT who is responsible for sending the target to rescue*/
	private EntityID headToSend;
	
	/** the world model*/
	private final AdvancedWorldModel world;

	private static final int MAX_N = 31;
	private static final int N_BIT_SIZE = BitUtil.needBitSize(MAX_N + 1);
	private static final int BURIEDNESS_SEND_MAX = 255;
	private static final int BURIEDNESS_BIT_SIZE = BitUtil.needBitSize(BURIEDNESS_SEND_MAX + 1);
	private final int MAX_HP;
	private final int MAX_HP_BIT;
	private final int MAX_DAMAGE;
	private final int MAX_DAMAGE_BIT;
	
	public BuriedHumans(AdvancedWorldModel w) {
		world = w;
		MAX_HP = world.getConfig().maxRoundHP / world.getConfig().hpPrecision;
		MAX_HP_BIT = BitUtil.needBitSize(MAX_HP);
		MAX_DAMAGE = world.getConfig().timestep / world.getConfig().damagePrecision;
		MAX_DAMAGE_BIT = BitUtil.needBitSize(MAX_DAMAGE + 1);
		
//		MAX_BUILDING_BIT = BitUtil.needBitSize(world.getConfig().buildingIDMax) + 1;
		
		fromCommunication = new HashMap<EntityID , Map<Integer , HumanInformation>>();
		fromVoiceOfCivilian = new HashSet<EntityID>();
		fromChangeSet = new HashSet<EntityID>();
		
		allDiedHuman = new ArrayList<EntityID>();
		newDiedHumanFromChangeSet = new HashSet<EntityID>();
		newDiedHumanFromCommunication = new HashSet<EntityID>();
		
		allBuriedHumanInfos = new ArrayList<Pair<EntityID , Integer>>();
		buriedHumanInfosFromCommunication = new HashSet<Pair<EntityID , Integer>>();
		
		allRescuedHuman = new ArrayList<EntityID>();
		newRescuedHumanFromMe = null;
		newRescuedHumanFromCommunication = new HashSet<EntityID>();
		
		needlessHuman = new HashSet<EntityID>();
		
		newRescuingHumanFromMe = null;
		newRescuingHumanFromCommunication = null;
		headToSend = null;
		
		totalBuriedHuman = new HashSet<EntityID>();
		
	}

	/**
	 * To add new buried human.
	 * @param changed
	 */
	public void update(ChangeSet changed) {
		this.changed = changed;
		fromChangeSet.clear();
		newDiedHumanFromChangeSet.clear();
		if (world.getTime() < world.getConfig().ignoreUntil) {
			return;
		}
		updateFromChangeset(changed);
		filterForFromVoiceOfCivilian();
		filterForFromCommunication();
		distinctNewBuriedHuman();
		updateBuiredHumanInformation();
		
	}
	
	/**
	 * To update the set:fromChangeset
	 * @param changed
	 */
	public void updateFromChangeset(ChangeSet changed){
		for (EntityID id : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(id);
			if (se instanceof Human){
				Human human = (Human)se;
				if (shouldIn(human)){
					fromChangeSet.add(id);
					//TODO TEST
//					System.out.println(human + " in buriedHumanFromChangSet");
				}
			}
		}
	}
	
	/**
	 * To distinct the buriedhumans in these three set: fromChangeset, 
	 * fromVoiceOfCivilian, fromCommunication
	 */
	public void distinctNewBuriedHuman(){
		for (EntityID id : fromChangeSet){
			if (fromCommunication.containsKey(id))
				fromCommunication.remove(id);
			if (fromVoiceOfCivilian.contains(id))
				fromVoiceOfCivilian.remove(id);
		}
		for (EntityID id : fromVoiceOfCivilian){
			if (fromCommunication.containsKey(id))
				fromCommunication.remove(id);
		}
	}
	
	/**
	 * To filter the wrong message or the needless message in fromCommunication
	 */
	public void filterForFromCommunication(){
		for (Iterator<EntityID> it = fromCommunication.keySet().iterator() ; it.hasNext() ;){
			EntityID humanID = it.next();
			if (needlessHuman.contains(humanID)){
				it.remove();
				continue;
			}
			if (allDiedHuman.contains(humanID)){
				it.remove();
				continue;
			}
		}
	}
	
	/**
	 * To filter the needless message in fromVoiceOfCivilian
	 */
	public void filterForFromVoiceOfCivilian(){
		for (Iterator<EntityID> it = fromVoiceOfCivilian.iterator() ; it.hasNext() ; ){
			EntityID humanID = it.next();
			if (needlessHuman.contains(humanID)){
				it.remove();
				continue;
			}
		}
	}
	
	public void updateBuiredHumanInformation(){
		for (Iterator<EntityID> it = fromCommunication.keySet().iterator() ; it.hasNext() ;){
			boolean newest = true;
			EntityID humanID = it.next();
			HumanInformation nearestHumanInfo = null;
			try{
				Human human = (Human) world.getEntity(humanID);
				if (human == null) {
					human = world.addNewCivilian(humanID);
					newest = false;
				}
				int lastSeenTime = world.getTimestamp().getLastSeenTime(humanID);
				int nearestTime = 0;
				Map<Integer, HumanInformation> humanInfos = fromCommunication.get(humanID);
				for (Integer timeStep : humanInfos.keySet()) {
					if (nearestTime < timeStep.intValue()) {
						nearestTime = timeStep.intValue();
						nearestHumanInfo = humanInfos.get(timeStep);
					}
				}
				if (nearestHumanInfo == null)
					continue ;
				if (nearestTime > lastSeenTime)
					newest = false;
				if (!newest){
					human.setBuriedness(nearestHumanInfo.getBuriedness());
					human.setDamage(nearestHumanInfo.getDamage());
					human.setHP(nearestHumanInfo.getHP());
					human.setPosition(nearestHumanInfo.getPositionID());
					if (!human.isXDefined() || !human.isYDefined()){
						Building position = (Building)human.getPosition(world);
						human.setX(position.getX());
						human.setY(position.getY());
					}
//					System.out.println("Time : " + world.time + "    " + world.me + "    "  + human + "   buriedness : " + human.getBuriedness());
				}
			}catch(ClassCastException e){
				fromCommunication.remove(humanID);
				e.printStackTrace(System.out);
				continue;
			}
		}
	}

	public CivilianVoiceListener createCivilianVoiceListener() {
		return new CivilianVoiceListener() {
			@Override
			public void hear(AKSpeak message) {
				if (!allDiedHuman.contains(message.getAgentID())){
					Human human = (Human)world.getEntity(message.getAgentID());
					if (human.isBuriednessDefined() && 
							human.isHPDefined() &&
							human.isDamageDefined() &&
							human.isPositionDefined() &&
							human.getPosition(world) instanceof Building)
						fromVoiceOfCivilian.add(message.getAgentID());
					//TODO TEST
//					System.out.println(message.getAgentID() + " in buriedHumanFromVoicOfCivilian");
				}
			}
		};
	}

	/**
	 * To justify if the human is a undied buried human 
	 * @param human
	 * @return
	 */
	public boolean shouldIn(Human human) {
		if (allDiedHuman.contains(human.getID()))
			return false;
		if (!(human.getPosition(world) instanceof Building)){
			needlessHuman.add(human.getID());
			return false;
		}
		if (allRescuedHuman.contains(human.getID()))
			return false;
		Building position = (Building)human.getPosition(world);
		if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString())){
			if (position.getURN().equals(StandardEntityURN.REFUGE.toString())){
				needlessHuman.add(human.getID());
				return false;
			}
			if (human.getHP() == 0) {
				if (!allDiedHuman.contains(human.getID())){
					newDiedHumanFromChangeSet.add(human.getID());
					allDiedHuman.add(human.getID());
					//TODO TEST
//					System.out.println(human + " in newDiedHumanFromChangeSet");
					return false;
				}
			}
			if (human.getBuriedness() > 0)
				return true;
			else if (human.getDamage() > 0)
				return true;
			else 
				needlessHuman.add(human.getID());
		}
		else {
			if (human.getHP() == 0) {
				if (!allDiedHuman.contains(human.getID())){
					newDiedHumanFromChangeSet.add(human.getID());
					allDiedHuman.add(human.getID());
					return false;
				}
			}
			if (human.getBuriedness() > 0)
				return true;
			else 
				needlessHuman.add(human.getID());
		}
		return false;
	}
	
	public void addBHI(EntityID humanID, int index) {
		
		Pair<EntityID , Integer> newBHI = new Pair<EntityID , Integer>(humanID , new Integer(index));
		allBuriedHumanInfos.add(newBHI);
		//TODO TEST
//		System.out.println(humanID + "[" + index + "]" + "in newBHIFromMe");
	}
	
	public void addRescuedHuman(EntityID humanID) {
		Human human = (Human) world.getEntity(humanID);
		int numberOfATInBuilding = 0 ;
		for (EntityID entityID : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(entityID);
			if (se.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()))
				numberOfATInBuilding ++;
		}
		if (world.getConfig().radioChannels.size() == 0){
			if (human.getBuriedness() / numberOfATInBuilding <= 3){
				allRescuedHuman.add(humanID);
				//TODO TEST
				//System.out.println(human + " in newRescuedHumanFromMe in no radio");
			}
		}
		else {
			if (human.getBuriedness() / numberOfATInBuilding <= 3){
				newRescuedHumanFromMe = humanID;
				//TODO TEST
				//System.out.println(human + " in newRescuedHumanFromMe in radio");
			}
		}
	}
	
	/**
	 * This method is called only by the AT whose ID is the highest in the building
	 * @param humanID
	 */
	public void addRescuingHuman(EntityID humanID){
		if (headToSend != null){
			if (headToSend.equals(world.me.getID())){
				newRescuingHumanFromMe = humanID;
			}
			return;
		}
		else {
			EntityID maxID = null;
			for (EntityID entityID : changed.getChangedEntities()){
				StandardEntity entity = world.getEntity(entityID);
				if (entity.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()) && 
						((Human)entity).getPosition().equals(world.me.getPosition())){
					if (maxID == null) 
						maxID = entityID;
					else if (maxID.getValue() < entityID.getValue())
						maxID = entityID;
				}
			}
			if (maxID.equals(world.me.getID()))
				newRescuingHumanFromMe = humanID;
			return ;
		}
	}
	
	public EntityID getNewRescuingHumanFromCommunication(){
		EntityID humanID = null;
		if (newRescuingHumanFromCommunication != null){
			humanID = new EntityID(newRescuingHumanFromCommunication.getValue());
		}
		return humanID;
	}
	
	public EntityID getHeadToSend(){
		return headToSend;
	}

	public Set<EntityID> getBuriedHumanFromChangeSet(){
		return fromChangeSet;
	}
	
	public Set<EntityID> getBuriedHumanFromVoiceOfCivilian(){
		return fromVoiceOfCivilian;
	}
	
	public Map<EntityID , Map<Integer , HumanInformation>> getBuriedHumanFromCommunication(){
		return fromCommunication;
	}
	
	public Set<EntityID> getTotalBuriedHuman(){
		totalBuriedHuman.addAll(fromChangeSet);
		totalBuriedHuman.addAll(fromVoiceOfCivilian);
		totalBuriedHuman.addAll(fromCommunication.keySet());
		return totalBuriedHuman;
	}
	
	public Collection<EntityID> getAllDiedHuman(){
		return allDiedHuman;
	}
	
	public Set<Pair<EntityID , Integer>> getNewBuriedHumanInfos(){
		return buriedHumanInfosFromCommunication;
	}
	
	public Collection<EntityID> getAllRescuedHuman() {
		return allRescuedHuman;
	}
	
	public Set<EntityID> getNeedlessHuman(){
		return needlessHuman;
	}
	
	

	
	/**
	 * To send and read the damage type index of the buried human<p>
	 * Note : It is for the situation with no radio
	 * @param comUtil
	 * @return
	 */
	public Port createBHIPortForNoRadio(final CommunicationUtil comUtil){
		return new Port(){

			private final int MAX_BHI = 5;
			private boolean BHISent;
			private boolean firstRead ;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(final ChangeSet changed) {
				//TODO TEST
//				System.out.println("createBHIPortForNoRadio");
				for (Iterator<Pair<EntityID , Integer>> it = allBuriedHumanInfos.iterator() ; it.hasNext();){
					EntityID humanID = it.next().first();
					StandardEntity se = world.getEntity(humanID);
					if (se == null){
						it.remove();
						continue ;
					}
					Human human = (Human)se;
					if (!human.isPositionDefined() || !(human.getPosition(world) instanceof Building)){
						it.remove();
						continue ;
					}
					if (!human.isBuriednessDefined() || human.getBuriedness() == 0){
						it.remove();
						continue ;
					}
				}
				BHISent = allBuriedHumanInfos.isEmpty();
				firstRead = true;
			}

			@Override
			public boolean hasNext() {
				return !BHISent;
			}

			@Override
			public MessageBitSection next() {
				BHISent = true;
				MessageBitSection sec = new MessageBitSection(20);
				final int n = Math.min(allBuriedHumanInfos.size() , MAX_BHI);
				sec.add(n, N_BIT_SIZE);
				int len = allBuriedHumanInfos.size();
				for (int i = len - n; i <= len - 1; i++) {
					Pair<EntityID , Integer> BHI = allBuriedHumanInfos.get(i);
					final int humanID = BHI.first().getValue();
					final int DTIndex = BHI.second().intValue();
					Human human = (Human)world.getEntity(BHI.first());
					//TODO TEST
					if (human == null){
						//System.out.println("the null human id : " + BHI.first());
					}
					//TODO TEST
					int uniform = world.getUniform().toUniform(human.getPosition());
					final int buriedness = human.getBuriedness();
					sec.add(humanID, comUtil.UINT_BIT_SIZE);
					sec.add(DTIndex, BitUtil.needBitSize(12));
					sec.add(buriedness, BURIEDNESS_BIT_SIZE);
					sec.add(uniform , comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					comUtil.writeArea(sec , (Area)human.getPosition(world), world.getUniform());
					//System.out.println("-------------sent buried human damage type infomation : ");
					//System.out.println("humanID : " + humanID);
					//System.out.println("PosID : " + positionID);
					//System.out.println("buriedness : " + buriedness);
					//System.out.println("DTIndex : " + DTIndex);
				}
				return sec;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				try {
					if (firstRead){
						firstRead = false;
						buriedHumanInfosFromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					readString = "sender = " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
					lable : for (int i = 0; i < n; i++) {
						
						final EntityID newID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						final int DTIndex = stream.readBit(BitUtil.needBitSize(12));
						final int buriedness = stream.readBit(BURIEDNESS_BIT_SIZE);
						final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
						EntityID positionID = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
//						final EntityID positionID = comUtil.readArea(stream, world.getUniform());
						if (AgentConstants.PRINT_COMMUNICATION){
							readString = readString + "newID = " + newID.getValue() + "\n";
							readString = readString + "DTIndex = " + DTIndex + "\n";
							readString = readString + "buriedness = " + buriedness + "\n";
							readString = readString + "positionID = " + positionID.getValue() + "\n";
						}
						if (needlessHuman.contains(newID))
							continue;
						if (positionID == null)
							continue;
						if (DTIndex >= 12 || DTIndex <= 0)
							continue;
						if (buriedness > 80 || buriedness <= 0)
							continue;
						//check whether the human has been added
						for (Pair<EntityID , Integer> BHI : allBuriedHumanInfos){
							EntityID humanID = BHI.first();
							if (humanID.equals(newID))
								continue lable;
						}
						
						//the human has not been added
						StandardEntity se = world.getEntity(newID);
						if (se != null && !(se instanceof Human))
							continue;
						Human human = (Human)se;
						if (human == null) {
							human = world.addNewCivilian(newID);
							human.setBuriedness(buriedness);
							human.setPosition(positionID);
						}
						if (world.getTimestamp().getLastChangedTime(newID) < time)
							human.setBuriedness(buriedness);
						Pair<EntityID , Integer> newBHI = new Pair<EntityID , Integer>(newID , new Integer(DTIndex));
						buriedHumanInfosFromCommunication.add(newBHI);
						allBuriedHumanInfos.add(newBHI);
						//TODO TEST
//						System.out.println(newID + " in newBHIFromCommunication[no radio]");
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}

			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override 
			public String toString(){
				String type = "createBHIPortForNoRadio";
				return type;
			}

			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createBHIPortForNoRadio, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i * 5 + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						writer.write("DTIndex = " + dataSizePair.get(i * 5 + 2).first().intValue() + "\n");
						writer.write("buriedness = " + dataSizePair.get(i * 5 + 3).first().intValue() + "\n");
						
						int marker = dataSizePair.get(i * 5 + 4).first().intValue();
						int uniform = dataSizePair.get(i * 5 + 5).first().intValue();
						
						int areaId = marker == 0 ?
								world.getUniform().toID(StandardEntityURN.BUILDING, uniform).getValue() :
								world.getUniform().toID(StandardEntityURN.ROAD, uniform).getValue();
						
						writer.write("maeker(0 for building, 1 for raod) = " + marker + "\n");
						writer.write("uniform = " + uniform + ", areaId = " + areaId + "\n\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}

			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createBHIPortForNoRadio, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the information of the buried human perceive
	 * from line of sight and voice of civilian.
	 * @param comUtil
	 * @return
	 */
	public Port createAddPort(final CommunicationUtil comUtil) {
		return new Port() {
			private List<Human> addCivilianList;
			private List<Human> addAgentList;
			private boolean civilianSent;
			private boolean agentSent;
			private boolean firstRead = true;
			private boolean firstSent = true;
			
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(final ChangeSet changed) {
				//System.out.println("firstSent : " + firstSent);
				if (firstSent){
					addCivilianList = new ArrayList<Human>();
					addAgentList = new ArrayList<Human>();
					for (EntityID id : fromChangeSet){
						Human human = (Human)world.getEntity(id);
						if (!(human.getPosition(world) instanceof Building)){
//							System.out.println(world.getControlledEntity() + "   Time : " + world.getTime() + "   wrong #1#");
							continue;
						}
						if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()))
							addCivilianList.add(human);
						else 
							addAgentList.add(human);
					}
					for (EntityID id : fromVoiceOfCivilian){
						Human human = (Human)world.getEntity(id);
						if (!(human.getPosition(world) instanceof Building)){
//							System.out.println(world.getControlledEntity() + "   Time : " + world.getTime() + "   wrong #2#");
							continue;
						}
						if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()))
							addCivilianList.add(human);
						else 
							addAgentList.add(human);
					}
					Collections.sort(addCivilianList, new IntPropertySorter<StandardEntity>(StandardPropertyURN.BURIEDNESS));
					Collections.sort(addAgentList, new IntPropertySorter<StandardEntity>(StandardPropertyURN.BURIEDNESS));
					fromVoiceOfCivilian.clear();
				}
				civilianSent = addCivilianList.isEmpty();
				agentSent = addAgentList.isEmpty();
			}
			
			@Override
			public boolean hasNext() {
				if (!civilianSent || !agentSent){
					firstSent = false;
					firstRead = true;
				}
				return !civilianSent || !agentSent;
			}
			
			@Override
			public MessageBitSection next() {
				if (!agentSent){
					agentSent = true;
					return createNewSection(addAgentList , 25);
				}
				else {
					civilianSent = true;
					return createNewSection(addCivilianList , 25);
				}
			}
			
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				try {
					if (firstRead){
						firstRead = false;
						firstSent = true;
						fromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					readString = "sender = " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
					for (int i = 0; i < n; i++) {
						final EntityID humanID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
						EntityID positionID = world.getUniform().toID(StandardEntityURN.BUILDING, uniform); 
//						final EntityID positionID = comUtil.readArea(stream, world.getUniform());
						final int buriedness = stream.readBit(BURIEDNESS_BIT_SIZE);
						final int reciveHP = stream.readBit(MAX_HP_BIT);
						final int damage = stream.readBit(MAX_DAMAGE_BIT) * world.getConfig().damagePrecision;
						final int timeStep = stream.readBit(BitUtil.needBitSize(400));
						if (AgentConstants.PRINT_COMMUNICATION){
							readString = readString + "humanId = " + humanID.getValue() + "\n";
							readString = readString + "positionId = " + positionID.getValue() + "\n";
							readString = readString + "buriedness = " + buriedness;
							readString = readString + "reciveHP = " + reciveHP;
							readString = readString + "damage = " + damage;
							readString = readString + "timestep = " + timeStep;
						}
						
						if (needlessHuman.contains(humanID))
							continue;
						if (positionID == null)
							continue;
						if (buriedness > 80 || buriedness <= 0)
							continue;
						if (reciveHP > MAX_HP || reciveHP <= 0)
							continue;
						if (damage > 300 || damage <0)
							continue;
						
						StandardEntity se = world.getEntity(humanID);
						if (se != null && !(se instanceof Human))
							continue;
						
						final int hp = reciveHP * world.getConfig().hpPrecision;
						
						
						if (!fromCommunication.containsKey(humanID)){
							Map<Integer , HumanInformation> humanInfos = new HashMap<Integer , HumanInformation>();
							fromCommunication.put(humanID , humanInfos);
							//TODO TEST
						}
						if (!fromCommunication.get(humanID).containsKey(new Integer(timeStep))){
							HumanInformation humanInfo = new HumanInformation(humanID , positionID , buriedness , damage , hp , timeStep);
							fromCommunication.get(humanID).put(humanInfo.getTimeStep() , humanInfo);
//							System.out.println("-------------read buried human information : ");
//							System.out.println("humanID : " + humanID);
//							System.out.println("PosID : " + positionID);
//							System.out.println("hp : " + hp);
//							System.out.println("damage : " + damage);
						}
					}
				//System.out.println(" read data in add port : " + n );
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}
			
			
			private MessageBitSection createNewSection(List<Human> addList , int priority){
				MessageBitSection sec = new MessageBitSection(priority);
				final int n = Math.min(addList.size(), MAX_N);
				sec.add(n, N_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Human human = addList.get(i);
					final int humanID = human.getID().getValue();
					int uniform = world.getUniform().toUniform(human.getPosition());
					sec.add(humanID , comUtil.UINT_BIT_SIZE);
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					comUtil.writeArea(sec , (Area)human.getPosition(world), world.getUniform());

					if (human.isBuriednessDefined()) {
						sec.add(Math.min(human.getBuriedness(), BURIEDNESS_SEND_MAX), BURIEDNESS_BIT_SIZE);
					} else {
						sec.add(BURIEDNESS_SEND_MAX, BURIEDNESS_BIT_SIZE);
					}
					final int hp;
					final int damage;
					if (human.isHPDefined()){
						int roundHP ;
						int roundDamage;
						if (human.getID().equals(world.me.getID())){
							roundHP = round(human.getHP() , world.getConfig().hpPrecision);
							roundDamage = round(human.getDamage() , world.getConfig().damagePrecision);
						} else {
							roundHP = human.getHP();
							roundDamage = human.getDamage();
						}
						hp = roundHP / world.getConfig().hpPrecision;
						damage = roundDamage / world.getConfig().damagePrecision;
					} else {
						hp = MAX_HP;
						damage = 0;
					}
					
					sec.add(hp, MAX_HP_BIT);
					sec.add(damage, MAX_DAMAGE_BIT);
					sec.add(world.getTime() - 1 , BitUtil.needBitSize(400));
					
					//System.out.println("-------------sent buried human infomation : ");
					//System.out.println("humanID : " + humanID);
					//System.out.println("PosID : " + posID);
					//System.out.println("hp : " + hp);
					//System.out.println("damage : " + damage);
					
				}
				return sec;
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPROTED_TO_PF_AND_AT;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createAddPort, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i * 7 + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						
						int marker = dataSizePair.get(i * 5 + 2).first().intValue();
						int uniform = dataSizePair.get(i * 5 + 3).first().intValue();
						
						int areaId = marker == 0 ?
								world.getUniform().toID(StandardEntityURN.BUILDING, uniform).getValue() :
								world.getUniform().toID(StandardEntityURN.ROAD, uniform).getValue();
						
						writer.write("maeker(0 for building, 1 for raod) = " + marker + "\n");
						writer.write("uniform = " + uniform + ", areaId = " + areaId + "\n\n");
						
						writer.write("buriedness = " + dataSizePair.get(i * 7 + 4).first().intValue() + "\n");
						writer.write("reciveHP = " + dataSizePair.get(i * 7 + 5).first().intValue() + "\n");
						writer.write("damage = " + dataSizePair.get(i * 7 + 6).first().intValue() + "\n");
						writer.write("timestep = " + dataSizePair.get(i * 7 + 7).first().intValue() + "\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}

			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createAddPort, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the information of the buried human  
	 * from line of sight and voice of civilian.<p>
	 * Note : It is for the situation with no radio
	 * @param comUtil
	 * @return
	 */
	public Port createAddPortForNoRadio(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			private List<MessageBitSection> secList = new ArrayList<MessageBitSection>();
			private List<Human> addCivilianList;
			private List<Human> addAgentList;
			private int sentCount;
			private boolean firstRead = true;
			private boolean firstSent = true;
			
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				if (firstSent){
					addCivilianList = new ArrayList<Human>();
					addAgentList = new ArrayList<Human>();
					for (EntityID id : fromChangeSet){
						Human human = (Human)world.getEntity(id);
						if (!(human.getPosition(world) instanceof Building)){
//							System.out.println(world.getControlledEntity() + "   Time : " + world.getTime() + "   wrong #3#");
							continue;
						}
						if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()))
							addCivilianList.add(human);
						else addAgentList.add(human);
					}
					for (EntityID id : fromVoiceOfCivilian){
						Human human = (Human)world.getEntity(id);
						if (!(human.getPosition(world) instanceof Building)){
//							System.out.println(world.getControlledEntity() + "   Time : " + world.getTime() + "   wrong #4#");
							continue;
						}
						if (human.getURN().equals(StandardEntityURN.CIVILIAN.toString()))
							addCivilianList.add(human);
						else addAgentList.add(human);
					}
					Collections.sort(addCivilianList, 
							new IntPropertySorter<StandardEntity>(
									StandardPropertyURN.BURIEDNESS));
					Collections.sort(addAgentList, 
							new IntPropertySorter<StandardEntity>(
									StandardPropertyURN.BURIEDNESS));
					
					for (Iterator<MessageBitSection> iterator = secList.iterator() ; iterator.hasNext() ; ){
						MessageBitSection sec = iterator.next();
						if (sec.getTimeToLive() <= 0)
							iterator.remove();
					}
					if (!addCivilianList.isEmpty()){
						MessageBitSection newSec = createNewSection(addCivilianList , 25);
						newSec.setTimeToLive(timeToLive);
						secList.add(newSec);
					}
					else if (!addAgentList.isEmpty()){
						MessageBitSection newSec = createNewSection(addAgentList , 25);
						newSec.setTimeToLive(timeToLive);
						secList.add(newSec);
					}
					fromVoiceOfCivilian.clear();
				}
				sentCount = secList.size();
			}
			
			
			@Override
			public boolean hasNext() {
				if (sentCount > 0){
					firstSent = false;
					firstRead = true;
				}
				return sentCount > 0;
			}
			@Override
			public MessageBitSection next() {
				sentCount--;
				return secList.get(sentCount);
			}
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				try {
					if (firstRead){
						firstSent = true;
						firstRead = false;
						fromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					readString = "sender = " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
					for (int i = 0; i < n; i++) {
						final EntityID humanID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
						EntityID positionID = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
//						final EntityID positionID = comUtil.readArea(stream, world.getUniform());
						final int buriedness = stream.readBit(BURIEDNESS_BIT_SIZE);
						final int reciveHP = stream.readBit(MAX_HP_BIT);
						final int damage = stream.readBit(MAX_DAMAGE_BIT) * world.getConfig().damagePrecision;
						final int timeStep = stream.readBit(BitUtil.needBitSize(400));
						if (AgentConstants.PRINT_COMMUNICATION){
							readString = readString + "humanId = " + humanID.getValue() + "\n";
							readString = readString + "uniform = " + uniform + "\n";
							readString = readString + "buriedness = " + buriedness;
							readString = readString + "reciveHP = " + reciveHP;
							readString = readString + "damage = " + damage;
							readString = readString + "timestep = " + timeStep;
						}
						if (needlessHuman.contains(humanID))
							continue;
						if (positionID == null)
							continue;
						if (buriedness > 80 || buriedness <= 0)
							continue;
						if (reciveHP > MAX_HP || reciveHP <= 0)
							continue;
						if (damage > 300 || damage < 0)
							continue;
						
						StandardEntity se = world.getEntity(humanID);
						if (se != null && !(se instanceof Human))
							continue;
						
						final int hp = reciveHP * world.getConfig().hpPrecision;
						
						if (!fromCommunication.containsKey(humanID)){
							Map<Integer , HumanInformation> humanInfos = new HashMap<Integer , HumanInformation>();
							fromCommunication.put(humanID , humanInfos);
							//TODO TEST
//								System.out.println(humanID + " in buriedHumanFromCommunication[no radio]");
						}
						if (!fromCommunication.get(humanID).containsKey(new Integer(timeStep))){
							HumanInformation humanInfo = new HumanInformation(humanID , positionID , buriedness , damage , hp , timeStep);
							fromCommunication.get(humanID).put(humanInfo.getTimeStep() , humanInfo);
//							System.out.println("-------------read buried human infomation : ");
//							System.out.println("humanID : " + humanID);
//							System.out.println("PosID : " + positionID);
//							System.out.println("hp : " + hp);
//							System.out.println("damage : " + damage);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}
			
			
			private MessageBitSection createNewSection(List<Human> addList , int priority){
				MessageBitSection sec = new MessageBitSection(priority);
				final int n = Math.min(addList.size(), MAX_N);
				sec.add(n, N_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Human human = addList.get(i);
					final int humanID = human.getID().getValue();
					int uniform = world.getUniform().toUniform(human.getPosition());
					sec.add(humanID , comUtil.UINT_BIT_SIZE);
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					comUtil.writeArea(sec , (Area)human.getPosition(world), world.getUniform());
					if (human.isBuriednessDefined()) {
						sec.add(Math.min(human.getBuriedness(), BURIEDNESS_SEND_MAX), BURIEDNESS_BIT_SIZE);
					}
					else {
						sec.add(BURIEDNESS_SEND_MAX, BURIEDNESS_BIT_SIZE);
					}
					final int hp;
					final int damage;
					if (human.isHPDefined()){
						int roundHP ;
						int roundDamage;
						if (human.getID().equals(world.me.getID())){
							roundHP = round(human.getHP() , world.getConfig().hpPrecision);
							roundDamage = round(human.getDamage() , world.getConfig().damagePrecision);
						}
						else {
							roundHP = human.getHP();
							roundDamage = human.getDamage();
						}
						hp = roundHP / world.getConfig().hpPrecision;
						damage = roundDamage / world.getConfig().damagePrecision;
					}
					else {
						hp = MAX_HP;
						damage = 0;
					}
					sec.add(hp, MAX_HP_BIT);
					sec.add(damage, MAX_DAMAGE_BIT);
					sec.add(world.getTime() - 1 , BitUtil.needBitSize(400));
					
//					System.out.println("-------------sent buried human infomation : ");
//					System.out.println("humanID : " + humanID);
//					System.out.println("PosID : " + posID);
//					System.out.println("hp : " + hp);
//					System.out.println("damage : " + damage);
				}
				return sec;
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override 
			public String toString(){
				String type = "createAddPortForNoRadio";
				return type;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createAddPortForNoRadio, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i * 7 + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						
						int marker = dataSizePair.get(i * 5 + 2).first().intValue();
						int uniform = dataSizePair.get(i * 5 + 3).first().intValue();
						
						int areaId = marker == 0 ?
								world.getUniform().toID(StandardEntityURN.BUILDING, uniform).getValue() :
								world.getUniform().toID(StandardEntityURN.ROAD, uniform).getValue();
						
						writer.write("maeker(0 for building, 1 for raod) = " + marker + "\n");
						writer.write("uniform = " + uniform + ", areaId = " + areaId + "\n\n");
						
						writer.write("buriedness = " + dataSizePair.get(i * 7 + 4).first().intValue() + "\n");
						writer.write("reciveHP = " + dataSizePair.get(i * 7 + 5).first().intValue() + "\n");
						writer.write("damage = " + dataSizePair.get(i * 7 + 6).first().intValue() + "\n");
						writer.write("timestep = " + dataSizePair.get(i * 7 + 7).first().intValue() + "\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}

			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createAddPortForNoRadio, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			
		};
	}
	
	/**
	 * To send and read the EntityID of the died human
	 * @param comUtil
	 * @return
	 */
	public Port createRemovePort(final CommunicationUtil comUtil) {
		return new Port() {
			private List<EntityID> removeList;
			private boolean removeSent;
			private boolean firstRead = true;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				removeList = new ArrayList<EntityID>();
				removeList.addAll(newDiedHumanFromChangeSet);
				removeSent = removeList.isEmpty();
				firstRead = true;
				//TODO TEST
//				System.out.println(removeList + " sent from newDiedHumanFromChangeSet");
			}
			@Override
			public boolean hasNext() {
				return !removeSent;
			}
			@Override
			public MessageBitSection next() {
//				System.out.println("*******************");
				removeSent = true;
				MessageBitSection sec = new MessageBitSection(50);
				final int n = Math.min(removeList.size(), MAX_N);
				sec.add(n, N_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					sec.add(removeList.get(i).getValue() , comUtil.UINT_BIT_SIZE);
				}
				return sec;
			}
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				
				try {
					if (firstRead){
						firstRead = false;
						newDiedHumanFromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					readString = "sender = " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
					for (int i = 0; i < n; i++) {
						final EntityID humanID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						readString = readString + "humanId = " + humanID + "\n";
						//TODO TEST
//						System.out.println(humanID + " in newDiedHumanFromCommunication");
						StandardEntity se = world.getEntity(humanID);
						if (se != null && !(se instanceof Human))
							continue;
						Human human = (Human)se;
						if (human == null) {
							human = world.addNewCivilian(humanID);
						}
						if (!allDiedHuman.contains(humanID)){
							newDiedHumanFromCommunication.add(humanID);
							allDiedHuman.add(humanID);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}

			}
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTED_TO_AT;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePortFor, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createRemovePort, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the EntityID of the died human<p>
	 * Note : It is for the situation with no radio
	 * @param comUtil
	 * @return
	 */
	public Port createRemovePortForNoRadio(final CommunicationUtil comUtil) {
		return new Port() {
			private List<EntityID> removeList = allDiedHuman;
			private boolean removeSent;
			private int MAX_REMOVE = 3;
			private boolean firstRead = true;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				removeSent = removeList.isEmpty();
				firstRead = true;
			}
			@Override
			public boolean hasNext() {
				return !removeSent;
			}
			@Override
			public MessageBitSection next() {
				final int len = removeList.size();
				removeSent = true;
				MessageBitSection sec = new MessageBitSection(50);
				final int n = Math.min(len , MAX_REMOVE);
				sec.add(n, N_BIT_SIZE);
				for (int i = len - n ; i <= len - 1; i++) {
					sec.add(removeList.get(i).getValue(), comUtil.UINT_BIT_SIZE);
					//TODO TEST
//					System.out.println(removeList.get(i) + " sent from newDiedHumanFromChangeSet[no radio]");
				}
				return sec;
			}
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				try {
					if (firstRead){
						firstRead = false;
						newDiedHumanFromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					readString = "sender = " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
					for (int i = 0; i < n; i++) {
						final EntityID humanID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						if (AgentConstants.PRINT_COMMUNICATION){
							readString = readString + "humanId = " + humanID + "\n";
						}
						StandardEntity se = world.getEntity(humanID);
						if (se != null && !(se instanceof Human))
							continue;
						Human human = (Human)se;
						if (human == null) {
							human = world.addNewCivilian(humanID);
						}
						if (!allDiedHuman.contains(humanID)){
							newDiedHumanFromCommunication.add(humanID);
							allDiedHuman.add(humanID);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override 
			public String toString(){
				String type = "createRemovePortForNoRadio";
				return type;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePortForNoRadio, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createRemovePortForNoRadio, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the EntityID of the rescued human <p>
	 * @param comUtil
	 * @return
	 */
	public Port createRescuedPort(final CommunicationUtil comUtil) {
		return new Port() {
			boolean rescuedSent = false;
			int rescuedID;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}

			@Override
			public void init(ChangeSet changed) {
				//TODO TEST
				//System.out.println("sent information in rescuedPort");
				if (newRescuedHumanFromMe == null)
					rescuedSent = false;
				else if (!allRescuedHuman.contains(newRescuedHumanFromMe)){
					rescuedSent = true;
					rescuedID = newRescuedHumanFromMe.getValue();
					allRescuedHuman.add(newRescuedHumanFromMe);
					newRescuedHumanFromMe = null;
				}
				else rescuedSent = false;
			}
			
			@Override
			public boolean hasNext() {
				return rescuedSent ;
			}

			@Override
			public MessageBitSection next() {
				rescuedSent = false;
				MessageBitSection sec = new MessageBitSection(50);
				System.out.println(world.me + "  Time : " + world.getTime() + "  add rescued human : " + rescuedID);
				sec.add(rescuedID, comUtil.UINT_BIT_SIZE);
				return sec;
			}

			@Override
			public void read(final EntityID senderID, final int time, final BitArrayInputStream stream) {
				try {
					final int id = stream.readBit(comUtil.UINT_BIT_SIZE);
					if (AgentConstants.PRINT_COMMUNICATION){
						readString = "sender = " + senderID + ", sendTime = " + time + "\n";
						readString = readString + "humanId = " + id + "\n";
					}
					EntityID humanID = new EntityID(id);
					StandardEntity se = world.getEntity(humanID);
					if (se != null && !(se instanceof Human))
						return;
					Human human = (Human)se;
					if (human == null) {
						human = world.addNewCivilian(humanID);
					}
					if (!allRescuedHuman.contains(humanID))
						allRescuedHuman.add(humanID);
					//TODO TEST
//					System.out.println(world.me + "   Time : " + world.getTime() + "  get rescued human : " + human);
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTED_TO_AT;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int humanId = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRescuePort, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("humanId = " + humanId + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createRescuePort, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the EntityID of the rescued human <p>
	 * Note : It is for the situation with no radio                                                      
	 * @param comUtil
	 * @return
	 */
	public Port createRescuedPortForNorRadio(final CommunicationUtil comUtil) {
		return new Port(){
			private boolean rescuedSent;
			private List<EntityID> rescuedList = allRescuedHuman;
			private int MAX_RESCUED = 3;
			private boolean firstRead = true;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				rescuedSent = rescuedList.isEmpty();
				firstRead = true;
			}

			@Override
			public boolean hasNext() {
				return !rescuedSent;
			}

			@Override
			public MessageBitSection next() {
				final int len = rescuedList.size();
				rescuedSent = true;
				MessageBitSection sec = new MessageBitSection(50);
				final int n = Math.min(len , MAX_RESCUED);
				sec.add(n, N_BIT_SIZE);
				for (int i = len - n ; i <= len - 1; i++) {
					sec.add(rescuedList.get(i).getValue(), comUtil.UINT_BIT_SIZE);
				}
				return sec;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				try {
					if (firstRead){
						firstRead = false;
						newRescuedHumanFromCommunication.clear();
					}
					final int n = stream.readBit(N_BIT_SIZE);
					if (AgentConstants.PRINT_COMMUNICATION){
						readString = "sender = " + sender + ", sendTime = " + time + ", numberToSend = " + n + "\n";
					}
					for (int i = 0; i < n; i++) {
						final EntityID humanID = new EntityID(stream.readBit(comUtil.UINT_BIT_SIZE));
						
						readString = readString + "humanId = " + humanID + "\n";
						//TODO TEST
//						System.out.println(humanID + " in newRescuedHumanFromCommunication[no radio]");
						StandardEntity se = world.getEntity(humanID);
						if (se != null && !(se instanceof Human))
							return;
						Human human = (Human)se;
						if (human == null) {
							human = world.addNewCivilian(humanID);
						}
						if (!allRescuedHuman.contains(humanID)){
							newRescuedHumanFromCommunication.add(humanID);
							allRescuedHuman.add(humanID);
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
				
			}

			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override 
			public String toString(){
				String type = "createRescuePortForNorRadio";
				return type;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePortForNoRadio, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int humanId = dataSizePair.get(i + 1).first().intValue();
						writer.write("humanId = " + humanId + "\n");
						writer.close();
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createRescuedPortForNoRadio, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	/**
	 * To send and read the EntityID of the rescueing human <p>
	 * Note : It is for the situation with no radio                                                      
	 * @param comUtil
	 * @return
	 */
	public Port createRescuingPortForNorRadio(final CommunicationUtil comUtil) {
		return new Port(){
			private boolean rescuingSent;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				newRescuingHumanFromCommunication = null;
				headToSend = null;
				if (newRescuingHumanFromMe == null)
					rescuingSent = false;
				else rescuingSent = true;
			}

			@Override
			public boolean hasNext() {
				return rescuingSent;
			}

			@Override
			public MessageBitSection next() {
				rescuingSent = false;
				MessageBitSection sec = new MessageBitSection(25);
				sec.add(newRescuingHumanFromMe.getValue(), comUtil.UINT_BIT_SIZE);
//				System.out.println(world.me + " Time : " + world.getTime() + " addRescuingHuman : " + newRescuingHumanFromMe);
				newRescuingHumanFromMe = null;
				return sec;
			}

			@Override
			public void read(final EntityID senderID, final int time, final BitArrayInputStream stream) {
				try {
					final int id = stream.readBit(comUtil.UINT_BIT_SIZE);
					if (AgentConstants.PRINT_COMMUNICATION){
						readString = "sender = " + senderID + ", sendTime = " + time + "\n";
						readString = readString + "humanId = " + id + "\n";
					}
					EntityID humanID = new EntityID(id);
					StandardEntity se = world.getEntity(humanID);
					if (!(se instanceof Human))
						return;
					Human human = (Human)world.getEntity(humanID);
					if (human == null || 
							!changed.getChangedEntities().contains(humanID) ||
							!human.getPosition().equals(world.me.getPosition()))
						return;
					newRescuingHumanFromCommunication = humanID;
					headToSend = senderID;
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace(System.out);
				}
			}

			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTED_TO_AT;
			}
			
			@Override 
			public String toString(){
				String type = "createRescuingPortForNorRadio";
				return type;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int humanID = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePortForNoRadio, BuriedHumans\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("humanID = " + humanID + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOut/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + ", agent: " + world.getControlledEntity());
					writer.write(" read in createRescuedPortForNoRadio, BuriedHumans\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				} catch (Exception e2) {
					e2.printStackTrace(System.out);
				}
			}
		};
	}
	
	public int round(int value , int precision){
		int remainder = value % precision;
		value -= remainder;
		if (remainder >= precision / 2)
			value += precision;
		return value;
	}
}
