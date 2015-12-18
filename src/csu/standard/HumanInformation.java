package csu.standard;

import rescuecore2.worldmodel.EntityID;

/**
 * This class is used to store the buried human's EntityID , buriedness , damage and hp
 * @author nale
 *
 */
public class HumanInformation {

	private EntityID humanID;
	
	private EntityID posID;
	
	private int buriedness;
	
	private int damage;
	
	private int hp;
	
	/** The time step this information accords to */
	private int timeStep;
	
	public HumanInformation(EntityID humanID , EntityID posID , int buriedness , int damage , int hp , int timeStep){
		this.humanID = humanID;
		this.posID = posID;
		this.buriedness = buriedness;
		this.damage = damage;
		this.hp = hp;
		this.timeStep = timeStep;
	}
	
	public EntityID getHumanID(){
		return this.humanID;
	}
	
	public EntityID getPositionID(){
		return this.posID;
	}
	
	public int getBuriedness(){
		return this.buriedness;
	}
	
	public int getDamage(){
		return this.damage;
	}
	
	public int getHP(){
		return this.hp;
	}
	
	public int getTimeStep(){
		return this.timeStep;
	}
}
