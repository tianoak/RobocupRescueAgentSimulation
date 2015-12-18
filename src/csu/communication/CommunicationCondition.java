package csu.communication;

/**
 * This enum list all possible communication condition of cuurent map.
 * 
 * @author appreciation - csu
 * 
 */
public enum CommunicationCondition {
	/**
	 * When there is no radio channel.
	 */
	Less, 
	/**
	 * When the maximum bandwidth within all radio channel is less than 128.
	 */
	Low, 
	/**
	 * When the maximum bandwidth within all radio channel is between 128 and 1024.
	 */
	Medium,
	/**
	 * When the maximum bandwidth within all radio channel is greater than 1024.
	 */
	High 	
}
