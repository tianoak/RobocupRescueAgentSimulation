package csu.agent.at.buriedHumanInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import csu.agent.at.buriedHumanInfo.DamageType.DamageTypeEnum;

import rescuecore2.standard.entities.Human;

/**
 * This class mainly describe the damage type of buried humans
 * @author nale
 *
 */
public class BuriedHumanDamageInfo {

	/** the EntityID of the buriedHuman*/
	private Human human;
	
	/** If the human's damage type is confirmed , 
	 * 	it points to the damge type of the human<p>
	 *  If the human's damage type is not confirmed , 
	 *  it points to the most possible damage type of the human<p>
	 */
	private int damageTypeIndex;
	
	/** indicate whether the damage type of this human is sure*/
	private boolean isDamageSure;
	
	/** to store the possible damage types for this human*/
	private Set<DamageType> possibleDamage;
	
	public BuriedHumanDamageInfo(Human human , Collection<DamageType> damageTypes){
		this.human = human;
		possibleDamage = new HashSet<DamageType>();
		possibleDamage.addAll(damageTypes);
		isDamageSure = false;
	}
	
	public boolean isDamageSure(){
		return isDamageSure;
	}
	
	public Set<DamageType> getPossibleDamage(){
		return possibleDamage;
	}
	
	public void setDamageType(int damageTypeIndex){
		this.damageTypeIndex = damageTypeIndex;
	}
	
	public int getDamageTypeIndex(){
		return damageTypeIndex;
	}
	
	public DamageTypeEnum getDamageType(){
		return DamageTypeEnum.values()[damageTypeIndex];
	}
	
	public void damageTypeSure(){
		isDamageSure = true;
	}
	
	public Human getHuman(){
		return human;
	}
	
	
}
