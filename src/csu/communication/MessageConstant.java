package csu.communication;

import java.util.EnumSet;

public class MessageConstant {
	
	/**
	 * unused
	 * 
	 * @author appreciation - csu
	 *
	 */
	public enum MessageType {
		/** This type of message will report a burning Building.*/
		BUILDING_ON_FIRE_MESSAGE,                 // building on fire message
		/** This type of message will report an unburnt Building.*/
		UNBURNT_BUILDING,                            // unburnt building
		/** This type of message will report a collapsed Building.*/
		COLLAPSED_BUILDING_MESSAGE,               // collapsed building message
		/** This type of message will report a warm Building.*/
		WARM_BUILDING,                               // warm building
		/** This type of message will report a cleared Building.*/
		CLEARED_BUILDING,                            // cleared building
		/** This type of message will report an extinguished Building.*/
		EXTINGUISHED_FIRE_MESSAGE,                // extinguished fire message
		
		/** This type of message will report the location of an Agent stuck inside a blockade.*/
		STUCK_INSIDE_BLOCKADE,                       // stuck inside blockade
		/** This type of message will report the location of a buried Agent(AT,PF,FB).*/
		BURIED_AGENT_LOCATION,                       // buried agent location
		/** This type of message will report the location of a buried civilian.*/
		CIVILIAN_LOCATION_BURIED,                    // civilian location buried
		
		/** This type of message will report a Road that is blocked.*/
		BLOCKED_ROAD_MESSAGE,                     // blocked road message
		/** This type of message will report a cleared Road.*/
		CLEARED_ROAD_MESSAGE,                     // cleared road message
		/** This type of message will report a Road that is blocked with its priority.*/
		BLOCKED_ROAD_PRIORITIZED,                    // blocked road prioritized
		
		/** This type of message will report a finished police cluster.*/
		FINISHED_CLUSTER_POLICE_MESSAGE,          // finished cluster police message
		/** This type of message will report a finished fire brigade cluster.*/
		FINISHED_CLUSTER_FIRE_MESSAGE,            // finished cluster fire message
		/** This type of message will report a finished ambulance cluster*/
		FINISHED_CLUSTER_AMBULANCE_MESSAGE,       // finished cluster ambulance message
		
		/** This type of message will report a ocuppied hydrant.*/
		OCUPPIED_HYDRANT,                            // ocuppied hydrant 
		/** This type of message will report an available hydrant.*/
		AVAILABLE_HYDRANT,                           // available hydrant  
			
//		CIVILIAN_LOCATION_NOT_BURIED,                // civilian location not buried 
//		AGENT_LOCATION_HEARD_CIVILIAN,               // agent location heard civilian 
//		ROAD_OCCUPIED,                               // road occupied       
//		CLUSTER_STATUS,                              // cluser status       
//		ONE_CLUSTER_STATUS,                          // one cluster status  
	};
	
	/**
	 * Enums to determines which Agent should recevie this message.
	 * 
	 * @author appreciation - csu
	 *
	 */
	public enum MessageReportedType{
		/** This message will be send to FB only.*/
		REPORTED_TO_FB,
		/** This message will be send to PF only.*/
		REPROTED_TO_PF,
		/** This message will be send to AT only.*/
		REPORTED_TO_AT,
		/** This message will be send to FB and PF.*/
		REPORTED_TO_FB_AND_PF,
		/** This message will be send to PF and AT.*/
		REPROTED_TO_PF_AND_AT,
		/** This message will be send to AT and FB.*/
		REPORTED_TO_AT_AND_FB,
		/** This message will be send to all RCR Agent..*/
		REPORTRD_TO_ALL
	}
	
	public static final EnumSet<MessageReportedType> MESSAGE_FOR_ALL = EnumSet.of(
			MessageReportedType.REPORTED_TO_FB,
			MessageReportedType.REPROTED_TO_PF,
			MessageReportedType.REPORTED_TO_AT,
			MessageReportedType.REPORTED_TO_FB_AND_PF,
			MessageReportedType.REPROTED_TO_PF_AND_AT,
			MessageReportedType.REPORTED_TO_AT_AND_FB,
			MessageReportedType.REPORTRD_TO_ALL);
	
	public static final EnumSet<MessageReportedType> MESSAGE_FOR_AT_PF = EnumSet.of(
			MessageReportedType.REPORTED_TO_AT, 
			MessageReportedType.REPORTED_TO_AT_AND_FB, 
			MessageReportedType.REPROTED_TO_PF_AND_AT, 
			MessageReportedType.REPROTED_TO_PF, 
			MessageReportedType.REPORTED_TO_FB_AND_PF, 
			MessageReportedType.REPORTRD_TO_ALL);
	
	/** Messages FB will received.*/
	public static final EnumSet<MessageReportedType> MESSAGE_FOR_FB = EnumSet.of(
			MessageReportedType.REPORTED_TO_FB, 
			MessageReportedType.REPORTED_TO_FB_AND_PF, 
			MessageReportedType.REPORTED_TO_AT_AND_FB, 
			MessageReportedType.REPORTRD_TO_ALL);
	
	/** Messages PF will received.*/
	public static final EnumSet<MessageReportedType> MESSAGE_FOR_PF = EnumSet.of(
			MessageReportedType.REPROTED_TO_PF,
			MessageReportedType.REPORTED_TO_FB_AND_PF,
			MessageReportedType.REPROTED_TO_PF_AND_AT,
			MessageReportedType.REPORTRD_TO_ALL);
	
	/** Messages AT will received.*/
	public static final EnumSet<MessageReportedType> MESSAGE_FOR_AT = EnumSet.of(
			MessageReportedType.REPORTED_TO_AT,
			MessageReportedType.REPORTED_TO_AT_AND_FB,
			MessageReportedType.REPROTED_TO_PF_AND_AT,
			MessageReportedType.REPORTRD_TO_ALL);
}
