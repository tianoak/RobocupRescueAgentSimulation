package csu.agent.at.rescueTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rescuecore2.worldmodel.EntityID;

/**
 * The class is used to manage the entities which should be froze ,
 * in order not to process them temporarily.
 * @author Nale
 * Jun 30, 2014
 */
public class EntityFreezer {

	/** To store the EntityID and the freezing time of those which need to be froze classified with tabs*/
	private HashMap<Freeze_Tab , HashMap<EntityID , Integer>> freezingStuff ;
	
	private ArrayList<EntityID> freezingAreaList;
	
	/** To store the stuff removed from the freezer when their freezing time below zero*/
	private HashSet<EntityID> unfreezingStuff;
	
	/** The capacity of the freezer to store the area stuff*/ 
	private final int FREEZING_AREA_CAPACITY = 2;
	
	public enum Freeze_Tab{
		AREA , 
		HUMAN,
		LOADINGAT,
		WARM_BUILDING;
	}
	
	public EntityFreezer(){
		freezingStuff = new HashMap<Freeze_Tab , HashMap<EntityID , Integer>>();
		freezingAreaList = new ArrayList<EntityID>();
		unfreezingStuff = new HashSet<EntityID>();
	}
	
	/**
	 * To update the freezing time
	 */
	public void updateFreezingTime(){
		unfreezingStuff.clear();
		for (HashMap<EntityID , Integer> stuffWithThisTab : freezingStuff.values()) {
			for (Iterator<EntityID> iterator = stuffWithThisTab.keySet().iterator() ; iterator.hasNext() ; ){
				EntityID entityID = iterator.next();
				int freezingTime = stuffWithThisTab.get(entityID);
				freezingTime--;
				if (freezingTime < 0){
					iterator.remove();
					unfreezingStuff.add(entityID);
				}
				else 
					stuffWithThisTab.put(entityID, freezingTime);
			}
		}
	}
	
	public void addNewfreezingStuff(Freeze_Tab tab , EntityID entityID , int freezingTime){
		//if this tag is not exist.
		if (!freezingStuff.containsKey(tab))
			freezingStuff.put(tab , new HashMap<EntityID , Integer> ());
		freezingStuff.get(tab).put(entityID, freezingTime);
		if (tab.equals(Freeze_Tab.AREA) && !freezingAreaList.contains(entityID)){
			freezingAreaList.add(entityID);
			if (freezingAreaList.size() > FREEZING_AREA_CAPACITY){
				EntityID removeID = freezingAreaList.remove(0);
				freezingStuff.get(tab).remove(removeID);
			}
		}
	}
	
	public void addNewfreezingStuff(Freeze_Tab tab , EntityID entityID){
		addNewfreezingStuff(tab , entityID , 5);
	}
	
	public Set<EntityID> getEntitiesWithTab(Freeze_Tab tab){
		HashMap<EntityID , Integer> stuffWithTab = freezingStuff.get(tab);
		if (stuffWithTab == null)
			return null;
		return freezingStuff.get(tab).keySet();
	}
	
	public void removeEntityWithTab(Freeze_Tab tab){
		freezingStuff.remove(tab);
	}
	
	/**
	 * True if the freezer contains to the stuff with given tab
	 * @param tab
	 * @param stuffID
	 * @return
	 */
	public boolean containStuff(Freeze_Tab tab , EntityID stuffID){
		HashMap<EntityID , Integer> stuffWithTab = freezingStuff.get(tab);
		if (stuffWithTab == null)
			return false;
		return freezingStuff.get(tab).containsKey(stuffID);
	}
	
	public HashMap<Freeze_Tab , HashMap<EntityID , Integer>> getFreeze(){
		return freezingStuff;
	}
}
