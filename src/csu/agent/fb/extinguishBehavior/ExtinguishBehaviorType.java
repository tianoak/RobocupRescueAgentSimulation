package csu.agent.fb.extinguishBehavior;

/**
 * An enmu class for extinguish behavior type.
 * 
 * <pre>
 * CLUSTER_BASED: DirectionBasedExtinguishBehavior
 * 
 * MUTUAL_LOCATION: MutualLocationExtinguishBehavior
 * </pre>
 * 
 * @author appreciation-csu
 * 
 */
public enum ExtinguishBehaviorType {

	CLUSTER_BASED, // DirectionBasedExtinguishBehavior
	CSU_OLD_BASED,
	GREEDY,
	MUTUAL_LOCATION // MutualLocationExtinguishBehavior

}
