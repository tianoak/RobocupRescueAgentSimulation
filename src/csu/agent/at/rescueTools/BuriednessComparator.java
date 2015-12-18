package csu.agent.at.rescueTools;

import java.util.Comparator;

import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;
import csu.model.AdvancedWorldModel;

/**
 * This class is used to compare the buriedness of human , and order the EntityID in ascend order. 
 * @author Nale
 * Jun 7, 2014
 */
public class BuriednessComparator implements Comparator<EntityID>{
	
	/** the world model*/
	private AdvancedWorldModel world;
	
	
	public BuriednessComparator(AdvancedWorldModel world){
		this.world = world;
	}


	@Override
	public int compare(EntityID o1, EntityID o2) {
		Human human1 = (Human)world.getEntity(o1);
		Human human2 = (Human)world.getEntity(o2);
		int buriedness1 = human1.getBuriedness();
		int buriedness2 = human2.getBuriedness();
		if (buriedness1 != buriedness2)
			return buriedness1 - buriedness2;
		else {
			int ID1 = o1.getValue();
			int ID2 = o2.getValue();
			return ID1 - ID2;
		}
	}

}
