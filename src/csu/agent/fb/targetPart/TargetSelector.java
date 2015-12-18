package csu.agent.fb.targetPart;

import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.worldmodel.EntityID;
import csu.agent.fb.FireBrigadeAgent;
import csu.agent.fb.FireBrigadeWorld;
import csu.agent.fb.tools.DirectionManager;
import csu.agent.fb.tools.FbUtilities;
import csu.model.object.CSUBuilding;

public abstract class TargetSelector implements FireBrigadeTargetSelector_Interface{

	protected FireBrigadeWorld world;
	protected FbUtilities  fbUtilities;
	protected DirectionManager directionManager; //used to find the expand direction of fire.
	
	protected FireBrigadeAgent underlyingAgent;
	protected FireBrigade controlledEntity;
	protected EntityID agentId;
	
	/** 
	 * Target for single platoon Agent when each platoon Agent select target separately.
	 */
	protected CSUBuilding target;
	/** 
	 * Last cycle's target for single platoon Agent when each platoon select target separately.
	 */
	protected CSUBuilding lastTarget;
	
	protected boolean checkFires = false;
	
// The fire cluster current target building belongs to.*/
//	protected FireCluster targetCluster;
//	protected FireCluster lastTargetCluster;
	
	public TargetSelector(FireBrigadeWorld world) {
		this.world = world;
		this.fbUtilities = new FbUtilities(world); 
		this.directionManager = new DirectionManager(world);
		this.agentId = world.getControlledEntity().getID();
		
		if (world.getAgent() instanceof FireBrigadeAgent) {
			this.underlyingAgent = (FireBrigadeAgent) world.getAgent();
			this.controlledEntity = (FireBrigade) world.getControlledEntity();
			this.lastTarget = this.underlyingAgent.getLastFire();///oak
			this.target = this.underlyingAgent.getThisFire();
		}
	}
	
}
