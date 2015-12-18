package csu.agent.pf;

import java.awt.Point;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;
import csu.Viewer.layers.CSU_ZonePolygonLayer;
import csu.agent.PlatoonAgent;
import csu.agent.pf.PFLastTaskType.PFClusterLastTaskEnum;
import csu.agent.pf.clearStrategy.I_ClearStrategy;
import csu.agent.pf.clearStrategy.PfClearStrategyEnum;
import csu.agent.pf.cluster.Cluster;
import csu.agent.pf.cluster.Clustering;
import csu.model.AgentConstants;
import csu.model.object.csuZoneEntity.CsuZones;
import csu.model.route.pov.CostFunction;

public abstract class AbstractPoliceForceAgent extends PlatoonAgent<PoliceForce> {

	/**
	 * A list of availiable clusters of current map.
	 */
	protected List<Cluster> clusters;
	
	/**
	 * Index of the cluster assigned to this agent at initialization.
	 */
	protected int assignedClusterIndex;
	
	/**
	 * Index of the cluster this agent currently work on.
	 */
	protected int currentClusterIndex;
	
	/**
	 * The cluster this agent currently work on.
	 */
	protected Cluster currentCluster;
	
	/**
	 * All zones of current map. And we will clustering those zones into clusters.
	 */
	private CsuZones zones;
	
	/**
	 * A set of unvisited entrances of current cluster.
	 */
	protected Set<Road> traversalEntranceSet;
	
	/**
	 * A set of unvisited critical areas of current cluster.
	 */
	protected Set<Area> traversalCriticalAreas;
	
	/**
	 * A set of unvisited refuges of current cluster.
	 */
	protected Set<Refuge> traversalRefugeSet;

	/**
	 * The task target of this agent in last cycle.
	 */
	protected PFLastTaskTarget taskTarget;
	
	/**
	 * The last task type of this agent's cluster's tasks.
	 */
	protected PFClusterLastTaskEnum clusterLastTaskType;
	
	/**
	 * The path this agent will following when clearing.
	 */
	protected List<EntityID> lastCyclePath = null;
	
	/**
	 * The clear strategy this agent currently using.
	 */
	protected I_ClearStrategy clearStrategy;
	
	/**
	 * The type of clear strategy this agent currently using.
	 */
	protected PfClearStrategyEnum clearStrategyType = PfClearStrategyEnum.POS_BASED_STRATEGY;
	
	/**
	 * A list of clusters this agent needed to expand to.
	 */
	protected List<Cluster> needToExpandClusters;
	
	/**
	 * A list of clusters this agent has searched or is searching now.
	 */
	protected List<Cluster> expandedClusters;
	
	/**
	 * A set of burning buildings in this agent's current and neighbour clusters.
	 */
	protected Set<Building> nearBurningBuildings;
	
	/**
	 * A set of burning buildings this agent has searched.
	 */
	protected Set<Building> hadSearchBuringBuildings;
	
	/**
	 * A set of entrances of the burning zone that this agent is searching now.
	 */
	protected Set<Road> entrancesOfBurningZone;
	
	/**
	 * A set of critical of the burning zone that this agent is searching now. 
	 */
	protected Set<Area> criticalOfBurningZone;
	/**
	 * The number of buildings of this agent's expaned clusters.
	 */
	protected int buildingCount;
	
	/**
	 * Flags to determines whether needs to search burning building or not.
	 */
	protected boolean searchBurningFlag;
	
	protected Set<Refuge> coincidentRefuge = new HashSet<>();
	
	protected Set<EntityID> visitedRefuges = new HashSet<>();
	
	private Set<Human> coincidentBuriedAgent = new HashSet<>();
	
	protected Set<EntityID> visitedBuriedAgent = new HashSet<>();
	
	@Override
	protected void initialize() {
		super.initialize();

		zones = world.getZones();
		getClusters();
	}
	
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
	}

	protected void getClusters() {
		Collection<StandardEntity> allPF = world.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
		clusters = Clustering.KMeansPlusPlus(allPF.size(), zones);
		Clustering.assignAgentsToClusters(allPF, clusters, world);
		assignedClusterIndex = Clustering.getClusterIndexAgentBelongTo(getID(), clusters);
		currentClusterIndex = assignedClusterIndex;
		CSU_ZonePolygonLayer.CLUSTERS = clusters;
		
		//test 
		System.out.println("pf: " + me());
		System.out.println(currentClusterIndex);
	}
	
	@Override
	public void sendMove(int time, List<EntityID> path, int destX, int destY) {
		send(new AKMove(getID(), time, path, destX, destY));
	}
	
	@Override
	public void sendClear(int time, int destX, int destY){
		send(new AKClearArea(getID(), time, destX, destY));
	}
	
	@Override
	public void sendClear(int time, EntityID target) {
		send(new AKClear(getID(), time, target));
	}

	@Override
	protected void moveToRefuge() throws ActionCommandException {
		Collection<StandardEntity> refuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
		if (refuges.isEmpty()) 
			return;
		
		CostFunction costFunc = router.getNormalCostFunction();
		Point selfL = new Point(me().getX(), me().getY());
		lastCyclePath = router.getMultiAStar(location(), refuges, costFunc, selfL);
		
		if (AgentConstants.PRINT_TEST_DATA) {
			System.out.println("In time: " + time + ", agent: " + me() + " move to refuges " +
					"----- moveToRefuge, AbstractPoliceForceAgent.java");
		}
		
		sendMove(time, lastCyclePath);
		throw new ActionCommandException(StandardMessageURN.AK_MOVE);
	}

	public Set<Human> getCoincidentBuriedAgent() {
		return coincidentBuriedAgent;
	}

	public void setCoincidentBuriedAgent(Set<Human> coincidentBuriedAgent) {
		this.coincidentBuriedAgent = coincidentBuriedAgent;
	}
}