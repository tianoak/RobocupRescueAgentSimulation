package csu.agent.fb.targetPart;

import java.util.Collection;

import csu.agent.Agent.ActionCommandException;
import csu.common.TimeOutException;
import csu.model.object.CSUBuilding;

/**
 * Interface for FB target selector. When you want to write a new target
 * selector, you should extends this interface.
 * 
 * @author appreciation-csu
 * 
 */
public interface FireBrigadeTargetSelector_Interface {

	/**
	 * Selected target for not stucked FB.
	 * 
	 * @return a FireBrigadeTarget represent the target building
	 */
	public FireBrigadeTarget selectTarget() throws ActionCommandException, TimeOutException;

	/**
	 * Selected target for stucked FB.
	 * 
	 * @param burnBuildings
	 *            a collection of burned buildings within extinguishable range
	 * @return the target building
	 */
	public CSUBuilding selectTargetWhenStuck(Collection<CSUBuilding> burnBuildings);

	/**
	 * Get the most valuable building from all burned buildings.
	 * 
	 * @param burnBuildings a collection of burned buildings this Agent known
	 * @return the most valuable building
	 */
	public CSUBuilding getOverallBestBuilding(Collection<CSUBuilding> burnBuildings);
}
