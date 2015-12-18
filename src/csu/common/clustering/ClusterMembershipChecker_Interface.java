package csu.common.clustering;

import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBuilding;

/**
 * This general interface should be used when forming or updating a cluster and membership of an 
 * object in that cluster is in question.
 * <br/><b>Note:</b> Implementations must mention acceptable classes in java doc.
 *
 * @author CSU - Appreciation
 */
public interface ClusterMembershipChecker_Interface {

	 /**
     * Checks if the provided {@code object} should/can be a member of any cluster.
     *
     * @param object Object to be checked for membership.
     * @return true if provided object is considered member of a cluster, false otherwise.
     * @throws IllegalArgumentException Implementations are only allowed to throw unchecked 
     * exceptions in case a wrong class is passed.
     */
	public boolean membershipCheck(AdvancedWorldModel world, CSUBuilding csuBuilding);
}
