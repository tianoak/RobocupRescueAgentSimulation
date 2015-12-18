package csu.agent.at;
/**
 * The enum of AmbulanceTeam tasks
 * @author nale
 *
 */
public enum AmbulanceTeamTasks {
	/**Represent the AT has no task*/
	NULL_TASK,
	/**Represent the AT is loading a civilian to refuge*/
	LOADING,
	/**Represent the AT is rescuing a human*/
	RESCUING,
	/**Represent the AT is searching for buried human*/
	SEARCHING,
	/**Represent tht AT is moving to a human which need to be rescued*/
	MOVING_TO_HUMAN,
	/**Represent the AT is stuck in a blockade*/
	STUCK,
	/**Represent the AT is buried in the building*/
	BURIED,
	/**Represent the AT is dying*/
	DYING,
	/**Represent the AT has nothing to do*/
	RESCUE_OVER;
}
