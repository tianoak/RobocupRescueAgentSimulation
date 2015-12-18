package csu.agent.fb.actionStrategy;

/**
 * This enum define all possible action strategy type. And you should expand this 
 * when you write a new kind of action strategy.
 * 
 * @author CSU - Appreciation
 *
 */
public enum ActionStrategyType {

	DEFAULT,             // DefaultFbActionStrategy
	STUCK_SITUATION,     // StuckFbActionStrategy
	CSU_OLD_BASED        // CsuOldBasedActionStrategy
}
