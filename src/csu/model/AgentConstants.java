
package csu.model;
import java.util.EnumSet;

import rescuecore2.standard.entities.StandardEntityURN;

/**
 * <pre>
 * This class defines many Agent Constants which divide entities in
 * this simulation system into several groups according to properties of those
 * entities. Because an entity can has more than one properties, so each entity
 * can locate into several groups.
 * All involved entities are list here:
 * 		Ambulance Centre
 * 		Fire Brigade
 * 		Police Force
 * 		Civilian
 * 		Building
 * 		Ambulance Centre
 * 		Fire Station
 * 		Police Office
 *		Road
 * 		Refuge
 * 		Blockade
 * 		GAS_STATION
 * 		HYDRANT
 * </pre>
 * 
 * @author CSU --- Appreciation
 *
 */
public class AgentConstants {

	/**
	 * <pre>
	 * Index of all entities. Those entities are 
	 *  	Ambulance Centre
     *  	Fire Brigade
     *  	Police Force
     *  	Building
     *  	Ambulance Centre
     *  	Fire Station
     *  	Police Office
     *  	Road
     *  	Refuge
     *  	Building
     *  	Blockade
     *  	Civilian</pre>
	 */
	public static final EnumSet<StandardEntityURN> INDEX_CLASS = EnumSet.of(
			StandardEntityURN.AMBULANCE_TEAM,
			StandardEntityURN.FIRE_BRIGADE,
			StandardEntityURN.POLICE_FORCE,
			StandardEntityURN.BUILDING,
			StandardEntityURN.AMBULANCE_CENTRE,
			StandardEntityURN.FIRE_STATION,
			StandardEntityURN.POLICE_OFFICE,
			StandardEntityURN.ROAD,
			StandardEntityURN.REFUGE,
			StandardEntityURN.BLOCKADE,
			StandardEntityURN.CIVILIAN,
			StandardEntityURN.GAS_STATION,
			StandardEntityURN.HYDRANT);

	/** 
	 * <pre>Uniformable entities which are 
	 *  	Ambulance Team
	 *  	Fire Brigade, 
	 *  	Police Force
	 *  	Building
	 *  	Road
	 *  	Refuge
	 *  	GAS_STATION
	 *  	HYDRANT</pre>
	 **/
	public static final EnumSet<StandardEntityURN> UNIFORMABLE_ENTITIES = EnumSet.of(
			StandardEntityURN.AMBULANCE_TEAM,
			StandardEntityURN.FIRE_BRIGADE,
			StandardEntityURN.POLICE_FORCE,
			StandardEntityURN.BUILDING,
			StandardEntityURN.ROAD,
			StandardEntityURN.REFUGE,
			StandardEntityURN.GAS_STATION,
			StandardEntityURN.HYDRANT);

	/**
	 * <pre>Those are entities can be regarded as an Areas object which are 
	 *  	Road
	 *  	Building
	 *  	Refuge
	 *  	Ambulance Centre
	 *  	Police Office
	 *  	Fire Station.
	 *  	GAS_STATION
	 *  	HYDRANT</pre>
	 */
	public static final EnumSet<StandardEntityURN> AREAS = EnumSet.of(
			StandardEntityURN.ROAD,
			StandardEntityURN.BUILDING,
			StandardEntityURN.REFUGE,
			StandardEntityURN.AMBULANCE_CENTRE,
			StandardEntityURN.POLICE_OFFICE,
			StandardEntityURN.FIRE_STATION,
			StandardEntityURN.GAS_STATION,
			StandardEntityURN.HYDRANT);
	
	/**
	 * <pre>Those are entities can be regared as Building which are 
	 *  	Building
	 *  	Refuge
	 *  	Ambulance Centre
	 *  	Police Office
	 *  	Fire Station.
	 *  	GAS_STATION</pre>
	 */
	public static final EnumSet<StandardEntityURN> BUILDINGS = EnumSet.of(
			StandardEntityURN.BUILDING,
			StandardEntityURN.REFUGE,
			StandardEntityURN.AMBULANCE_CENTRE,
			StandardEntityURN.POLICE_OFFICE,
			StandardEntityURN.FIRE_STATION,
			StandardEntityURN.GAS_STATION);
	
	/**
	 * <pre>Those are entities can be regarded as Platoons Agents which are 
	 *  	Ambulance Team
	 *  	Fire Brigade
	 *  	Police Force</pre>
	 */
	public static final EnumSet<StandardEntityURN> PLATOONS = EnumSet.of(
			StandardEntityURN.AMBULANCE_TEAM,
			StandardEntityURN.FIRE_BRIGADE,
			StandardEntityURN.POLICE_FORCE);
	
	/**
	 * <pre>Those are entities can be regared as Centre Agents which are 
	 *  	Ambulance Centre
	 *  	Fire Station
	 *  	Police Office</pre>
	 */
	public static final EnumSet<StandardEntityURN> CENTRES = EnumSet.of(
			StandardEntityURN.AMBULANCE_CENTRE,
			StandardEntityURN.FIRE_STATION,
			StandardEntityURN.POLICE_OFFICE);
	
	/**
	 * <pre>Those are entities can be regared as Humanoids which are 
	 *  	Ambulance Team
	 *  	Fire Brigade
	 *  	Police Force
	 *  	Civilian.</pre>
	 */
	public static final EnumSet<StandardEntityURN> HUMANOIDS = EnumSet.of(
			StandardEntityURN.AMBULANCE_TEAM,
			StandardEntityURN.FIRE_BRIGADE,
			StandardEntityURN.POLICE_FORCE,
			StandardEntityURN.CIVILIAN);
	
	/**
	 * <pre>Those are entities can be regared as Roads which are 
	 *  	Road
	 *  	Hydrant</pre>
	 */
	public static final EnumSet<StandardEntityURN> ROADS = EnumSet.of(
			StandardEntityURN.ROAD, 
			StandardEntityURN.HYDRANT);
	
	/**
	 * only used for hydrants
	 */
	public static final EnumSet<StandardEntityURN> HYDRANTS = EnumSet.of(
			StandardEntityURN.HYDRANT);
	
	/**
	 * Control key to determines whether to use TestViewer or not.
	 */
	public static final boolean LAUNCH_VIEWER = true; 
	
	/**
	 * Control key to determines whether to print the communication contents.
	 */
	public static final boolean PRINT_COMMUNICATION = false;
	
	/**       
	 * Control key to determines whether to print the test data.
	 */
	public static final boolean PRINT_TEST_DATA = false;
	
	public static final boolean PRINT_TEST_DATA_FB = false;
	
	public static final boolean FB = false;
	
	public static final boolean PRINT_SORTED_BUILDINGS_FB = false;
	
	public static final boolean PRINT_TEST_DATA_PF = false;
	
	public static final boolean PRINT_TEST_DATA_AT  = false;
	
	public static final boolean PRINT_BUILDING_INFO = false;
	
	public static boolean IS_DEBUG = false; 
}
