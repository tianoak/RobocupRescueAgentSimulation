package csu.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import csu.util.IdSorter;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

/**
 * This class uniformed uniformable entity according to their types. And provide a method to get
 * an entity's uniform number according to its ID. ALso a method to get an entity's ID according 
 * to its uniform number and its type. 
 * 
 * @author CSU --- Appreciation
 *
 */
final public class Uniform {
	/** The &nbsp:<code>EntityID</code> and &nbsp;<code>Uniform</code> pairs. */
	private Map<EntityID, Integer> idToUniform = new HashMap<EntityID, Integer>(40);
	
	private Map<StandardEntityURN, Map<Integer, EntityID>> uniformToID = new HashMap<StandardEntityURN, Map<Integer, EntityID>>(40);

	/**
	 * To uniform uniformable entity in <code> world</code> according to their types
	 * @param world
	 */
	public Uniform(AdvancedWorldModel world) {
		for (StandardEntityURN urn : AgentConstants.UNIFORMABLE_ENTITIES) {
			Map<Integer, EntityID> uniform_id = new HashMap<Integer, EntityID>();
			ArrayList<StandardEntity> entities = new ArrayList<StandardEntity>(world.getEntitiesOfType(urn));
			// Ordering EntityID in ASC
			Collections.sort(entities, new IdSorter());
			int i = 0;
			for (StandardEntity entity : entities) {
				idToUniform.put(entity.getID(), i);
				uniform_id.put(i, entity.getID());
				i++;
			}
			uniformToID.put(urn, uniform_id);
		}
	}
	
	/**
	 * Get the &nbsp;<code>Uniform</code> of an &nbsp;<code>Entity</code> according to its ID.
	 * 
	 * @param id &nbsp;&nbsp;the &nbsp;<code>Entity</code> needs to get its &nbsp;<code>Uniform</code>.
	 * @return the &nbsp;<code>Uniform</code> of this &nbsp;<code>Entity</code> 
	 */
	public int toUniform(EntityID id) {
		//
		Integer uniform = idToUniform.get(id);
		if (uniform == null) {
			return -1;
		}
		return uniform.intValue();
	}
	
	/**
	 * Get an &nbsp;<code>Entity</code>'s ID according to its &nbsp;(<code>StandardEntityURN urn</code>)  
	 * and &nbsp;(<code>int uniform</code>).
	 * 
	 * @param urn &nbsp;&nbsp;the &nbsp;<code>StandardEntityURN<code> of this <code>Entity</code>
	 * @param uniform the <code>Uniform</code> number of an <code>Entity</code>
	 * @return an <code>Entity</code>'s ID
	 */
	public EntityID toID(StandardEntityURN urn, int uniform) {
		if (!AgentConstants.UNIFORMABLE_ENTITIES.contains(urn)) {
			return null;
		}
		return uniformToID.get(urn).get(uniform);
	}
}
