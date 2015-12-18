package csu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import csu.standard.BuildingInfo;

import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.EntityID;

public class AddAndRemove {

	private static int index = 0;
	
	public static<T> boolean addIfNotExists(T current, List<T> list){
		if (!exists(current, list)){
			list.add(current);
			return true;
		}
		return false;
	}
	
	public static<T> boolean addIfNotExists(T current, Set<T> set) {
		List<T> list = new ArrayList<T>(set);
		if (! exists(current, list)) {
			set.add(current);
			return true;
		}
		return false;
	}
	
	public static<T> boolean removeIfExists(T current, List<T> list){
		if (exists(current, list)){
			list.remove(index);
			return true;
		}
		return false;
	}
	
	public static<T> boolean removeIfExists(T current, Set<T> set){
		List<T> list = new ArrayList<T>(set);
		if (exists(current, list)){
			set.remove(index);
			return true;
		}
		return false;
	}
	
	public static<T> int getIndexIfExists(T current, List<T> list){
		if (exists(current, list))
			return index;
		return list.size();
	}
	
	public static<T> boolean exists(T current, List<T> list){
		if (current instanceof BuildingInfo){
			BuildingInfo cur = (BuildingInfo)current;
			for (int i = 0; i < list.size(); i++) {
				BuildingInfo list_i = (BuildingInfo)list.get(i);
				if (list_i.getBuilding().getID().getValue() == cur.getBuilding().getID().getValue()){
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof Road){
			Road cur = (Road)current;
			for (int i = 0; i < list.size(); i++) {
				Road list_i = (Road)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()){
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof Hydrant){
			Hydrant cur = (Hydrant)current;
			for (int i = 0; i < list.size(); i++) {
				Hydrant list_i = (Hydrant)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()){
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof Blockade){
			Blockade cur = (Blockade)current;
			for (int i = 0; i < list.size(); i++) {
				Blockade list_i = (Blockade)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()){
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof EntityID){
			EntityID cur = (EntityID)current;
			for (int i = 0; i < list.size(); i++) {
				EntityID list_i = (EntityID)list.get(i);
				if (list_i.getValue() == cur.getValue()) {
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof StandardEntity){
			StandardEntity cur = (StandardEntity)current;
			for (int i = 0; i < list.size(); i++) {
				StandardEntity list_i = (StandardEntity)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()) {
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof Building){
			Building cur = (Building)current;
			for (int i = 0; i < list.size(); i++) {
				Building list_i = (Building)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()) {
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof Human){
			Human cur = (Human)current;
			for (int i = 0; i < list.size(); i++) {
				Human list_i = (Human)list.get(i);
				if (list_i.getID().getValue() == cur.getID().getValue()) {
					index = i;
					return true;
				}
			}
			return false;
		}
		if (current instanceof AKSpeak) {
			AKSpeak cur = (AKSpeak) current;
			String content = new String(cur.getContent());
			for (int i = 0; i < list.size(); i++) {
				AKSpeak list_i = (AKSpeak) list.get(i);
				String content2 = new String(list_i.getContent());
				if (content.equals(content2)) {
					index = i;
					return true;
				}
			}
			return false;
		}
		return false;
	}
}































//if (current instanceof EntityID){
//EntityID cur = (EntityID)current;
//for (int i = 0; i < list.size(); i++) {
//	EntityID list_i = (EntityID)list.get(i);
//	if (list_i.getValue() == cur.getValue()) {
//		list.remove(i);
//		return true;
//	}
//}
//return false;
//}
//if (current instanceof AKSpeak){
//AKSpeak cur = (AKSpeak)current;
//String content = new String(cur.getContent());
//for (int i = 0; i < list.size(); i++) {
//	AKSpeak list_i = (AKSpeak)list.get(i);
//	String content2 = new String(list_i.getContent());
//	if (content.equals(content2)) {
//		list.remove(i);
//		return true;
//	}
//}
//return false;
//}
//if (current instanceof StandardEntity){
//StandardEntity cur = (StandardEntity)current;
//for (int i = 0; i < list.size(); i++) {
//	StandardEntity list_i = (StandardEntity)list.get(i);
//	if (list_i.getID().getValue() == cur.getID().getValue()) {
//		list.remove(i);
//		return true;
//	}
//}
//return false;
//}
//if (current instanceof Building){
//Building cur = (Building)current;
//for (int i = 0; i < list.size(); i++) {
//	Building list_i = (Building)list.get(i);
//	if (list_i.getID().getValue() == cur.getID().getValue()) {
//		list.remove(i);
//		return true;
//	}
//}
//return false;
//}
//if (current instanceof Human){
//Human cur = (Human)current;
//for (int i = 0; i < list.size(); i++) {
//	Human list_i = (Human)list.get(i);
//	if (list_i.getID().getValue() == cur.getID().getValue()) {
//		list.remove(i);
//		return true;
//	}
//}
//return false;
//}
//return false;
