package csu.agent.at;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.EntityID;
import csu.LaunchAgents;
import csu.agent.PlatoonAgent;
import csu.agent.at.cluster.Cluster;
import csu.agent.at.cluster.Clustering;
import csu.model.AgentConstants;


public abstract class AbstractAmbulanceTeamAgent extends PlatoonAgent<AmbulanceTeam> {
	
	/** The clusters of map generate by Kmeans-Plus-Plus */
	protected ArrayList<Cluster> clusters ;
	
	/** The index of the cluster which the agent assigned to at the beginning*/
	protected int assignedClusterIndex ;
	
	/** The current task of the AT */
	protected AmbulanceTeamTasks currentTask ;
	
	/** The last task of the AT*/
	protected AmbulanceTeamTasks lastTask;
	
	
	/**
	 * Rescue someone.
	 * @param target  the ID of target Huaman needs rescuing
	 * @throws ActionCommandException
	 */
	protected void rescue(EntityID target) throws ActionCommandException {
		sendRescue(time, target);
		throw new ActionCommandException(StandardMessageURN.AK_RESCUE);
	}
	
	/**
	 * Rescue some one.
	 * @param target  the target Human needs rescuing
	 * @throws ActionCommandException
	 */
	protected void rescue(Human target) throws ActionCommandException {
		rescue(target.getID());
	}

	/**
	 * Load a human to a safety place, generally, is the refuge.
	 * @param target  the ID of the target Human
	 * @throws ActionCommandException
	 */
	protected void load(EntityID target) throws ActionCommandException {
		sendLoad(time, target);
		throw new ActionCommandException(StandardMessageURN.AK_LOAD);
	}

	/**
	 * Load a human to a safety place, generally, is the refuge.
	 * @param target  the human we will load
	 * @throws ActionCommandException
	 */
	protected void load(Human target) throws ActionCommandException {
		load(target.getID());
	}

	/**
	 * Unload a human. When we reach the refuge or the human we load is died, we need unload it.
	 * @throws ActionCommandException
	 */
	protected void unload() throws ActionCommandException {
		sendUnload(time);
		throw new ActionCommandException(StandardMessageURN.AK_UNLOAD);
	}
	
	/**
	 * Get the civilian we are loading.
	 * @return  the civilian we load or null when we load nobody
	 */
	public Human someoneOnBoard(Human at) {
		for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
			if (next == null)
				continue ;
			Human civilian = (Human)next;
			if (civilian.isPositionDefined() && civilian.getPosition().equals(at.getID())) {
				return civilian;
			}
		}
		return null;
	}
	

	/**
	 * This method can be overrided. When overriding, you must invoke parent class's initialize() method
	 * by this line of code:
	 * <pre><pre><pre><pre><pre><pre><pre>super.initialize()</pre></pre></pre></pre></pre></pre></pre>
	 */
	@Override
	protected void initialize() {
		super.initialize();
		getClusters();
		world.getRemainCluster().init(clusters);
	}
	
	protected void getClusters(){
		Collection<StandardEntity> allAT = world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
		if (LaunchAgents.SHOULD_PRECOMPUTE)
		{
			String filename = "precompute/Clusters";
			File file = new File(filename);
			if (file.exists()){
				System.out.println("File exists!");
				clusters = Clustering.readFromFile(filename, world);
			}
			else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("File not exists!");
				clusters = Clustering.KMeansPlusPlus(allAT.size(),
						world.getEntitiesOfType(AgentConstants.BUILDINGS), world);
				Clustering.writeToFile(clusters, filename);
			}
		}
		else{
			clusters = Clustering.KMeansPlusPlus(allAT.size(),
					world.getEntitiesOfType(AgentConstants.BUILDINGS), world);
		}
		
		Clustering.assignAgentsToClusters(allAT, clusters, world);
		assignedClusterIndex = Clustering.getClusterIndexAgentBelong(getID(), clusters);
		System.out.println("My position : "+me().getPosition(world));
		System.out.println("My cluster index is : " + assignedClusterIndex);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}
}
