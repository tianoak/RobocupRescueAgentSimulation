package csu.agent.fb;

import java.awt.Point;
import java.awt.Shape;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javolution.util.FastSet;

import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.LaunchAgents;
import csu.agent.Agent;
import csu.agent.fb.tools.ProcessFile;
import csu.agent.fb.tools.Simulator;
// import csu.common.ProcessAdvantageRatio;
import csu.common.TimeOutException;
import csu.common.clustering.ClusterManager;
import csu.common.clustering.FireCluster;
import csu.common.clustering.FireClusterManager;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.object.CSUBuilding;
import csu.standard.simplePartition.GroupingType;

/**
 * A world model for FB only.
 * 
 * @author appreciation-csu
 *
 */
public class FireBrigadeWorld extends AdvancedWorldModel{
	
	private ClusterManager<FireCluster> fireClusterManager;
	private Simulator simulator;
	private float rayRate = 0.0025f;
	private CurrentState currentState = CurrentState.notExplored;
	
	private Set<CSUBuilding> estimatedBurningBuildings = new FastSet<CSUBuilding>();
	
	// private ProcessAdvantageRatio processAdvantageRatio;
	
	public FireBrigadeWorld() {
		super();
	}
	
	@Override
	public void initialize(Agent<? extends StandardEntity> selfAgent, Config conf, GroupingType type) {
		super.initialize(selfAgent, conf, type);
		
		// this.processAdvantageRatio = new ProcessAdvantageRatio(this);
        // this.processAdvantageRatio.process();
		
		if (LaunchAgents.SHOULD_PRECOMPUTE) {
			initConnectionValues();
		} else {
			for (CSUBuilding next : getCsuBuildings()) {
				if (!next.isInflammable())
					continue;
				next.initWallValue(this);
			}
		}
		
		this.simulator = new Simulator(this);
		this.fireClusterManager = new FireClusterManager(this);
		
//		if (AgentConstants.LAUNCH_VIEWER) {
//			if (CSU_BuildingLayer.CSU_BUILDING_MAP.isEmpty())
//				CSU_BuildingLayer.CSU_BUILDING_MAP = Collections.synchronizedMap(getCsuBuildingMap());
//		}
	}
	
	@Override
	public void update(Human me, ChangeSet changed) throws TimeOutException{
		super.update(me, changed);
		this.updateFireCluster(me, changed);
		
		// TODO add in Mar 30, 2014
		if (AgentConstants.PRINT_BUILDING_INFO) {
			if (this.getTime() == 249) {
				System.out.println("***************************************");
				FireClusterManager manager = (FireClusterManager) fireClusterManager;

				ProcessFile processFile = new ProcessFile(getAgent().getID().getValue());

				processFile.setTimeEstimatedTemperature(manager.getTimeEstimatedTemperature());
				processFile.setTimeEstimatedFieryness(manager.getTimeEstimatedFieryness());

				Thread thread = new Thread(processFile);
				thread.start();
			}
		}
		// TODO add in Mar 30, 2014
	}
	
	private void updateFireCluster(Human me, ChangeSet changed) throws TimeOutException {
		this.simulator.update();
		fireClusterManager.updateClusters();
	}
	
	private void initConnectionValues() {
		String fileName = "precompute/connect.ray";
		
		try {
			readConnectedValues(fileName);
		} catch (Exception e) {
			try {
				writeConnectedValues(fileName);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private void writeConnectedValues(String fileName) throws IOException {
		File f = new File(fileName);
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(rayRate + "\n");

		for (CSUBuilding csuBuilding : getCsuBuildings()) {

			csuBuilding.initWallValue(this);

			bw.write(csuBuilding.getSelfBuilding().getX() + "\n");
			bw.write(csuBuilding.getSelfBuilding().getY() + "\n");
			bw.write(csuBuilding.getConnectedBuildings().size() + "\n");

			for (int c = 0; c < csuBuilding.getConnectedBuildings().size(); c++) {
				CSUBuilding building = csuBuilding.getConnectedBuildings().get(c);
				Float val = csuBuilding.getConnectedValues().get(c);
				bw.write(building.getSelfBuilding().getX() + "\n");
				bw.write(building.getSelfBuilding().getY() + "\n");
				bw.write(val + "\n");
			}
		}
		bw.close();
	}

	private void readConnectedValues(String fileName) throws IOException {
		File f = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(f));
		Float.parseFloat(br.readLine());
		String nl;
		while (null != (nl = br.readLine())) {
			int x = Integer.parseInt(nl);
			int y = Integer.parseInt(br.readLine());
			int quantity = Integer.parseInt(br.readLine());
			List<CSUBuilding> bl = new ArrayList<CSUBuilding>();
			List<EntityID> bIDs = new ArrayList<EntityID>();
			List<Float> weight = new ArrayList<Float>();
			for (int c = 0; c < quantity; c++) {
				int ox = Integer.parseInt(br.readLine());
				int oy = Integer.parseInt(br.readLine());
				Building building = getBuildingInPoint(ox, oy);
				if (building == null) {
					System.err.println("building not found: " + ox + "," + oy);
					br.readLine();
				} else {
					bl.add(getCsuBuilding(building.getID()));
					bIDs.add(building.getID());
					weight.add(Float.parseFloat(br.readLine()));
				}

			}
			Building b = getBuildingInPoint(x, y);
			getCsuBuilding(b.getID()).setConnectedBuildins(bl);
			getCsuBuilding(b.getID()).setConnectedValues(weight);
		}
		br.close();
	}
	
	public ClusterManager<FireCluster> getFireClusterManager() {
		return this.fireClusterManager;
	}
	
	public Set<EntityID> getAreaInShape(Shape shape) {
		Set<EntityID> result = new FastSet<>();
		for (StandardEntity next : this.getEntitiesOfType(AgentConstants.BUILDINGS)) {
			Area area = (Area) next;
			if (!(area.isXDefined() && area.isYDefined()))
				continue;
			Point p = new Point(area.getX(), area.getY());
			if (shape.contains(p))
				result.add(area.getID());
		}
		return result;
	}
	
	public Simulator getSimulator() {
		return this.simulator;
	}
	
//	public Map<EntityID, CSUBuilding> getCsuBuildingMap() {
//		return csuBuildingMap;
//	}
//	
//	public List<EntityID> getFreeFireBrigades() {
//		List<EntityID> freeFireBrigade = new ArrayList<>();
//		List<EntityID> atRefuge = new ArrayList<>();
//		FireBrigade fireBrigade;
//		
//		freeFireBrigade.addAll(getFireBrigadeIdList());
//		freeFireBrigade.removeAll(this.getStuckHandle().getStuckedAgent());
//		freeFireBrigade.removeAll(this.getBuriedHumans().getTotalBuriedHuman());
//		for (EntityID next : freeFireBrigade) {
//			fireBrigade = getEntity(next, FireBrigade.class);
//			if (!fireBrigade.isPositionDefined() || getEntity(fireBrigade.getPosition()) instanceof Refuge) {
//				atRefuge.add(next);
//			}
//		}
//		freeFireBrigade.removeAll(atRefuge);
//		return freeFireBrigade;
//	}
	
	public Set<CSUBuilding> getEstimatedBurningBuildings() {
		return this.estimatedBurningBuildings;
	}
//	public void setEstimatedBurningBuildings(Set<CSUBuilding> estimatedBurningBuildings) {
//	this.estimatedBurningBuildings = estimatedBurningBuildings;
//}
	
	/**
	 * We define the four directions N, E, S, W as 0, 1, 2, 3,
	 * and get the buildings in a rectangle defined by two points in the same 
	 * direction of the fbAgent at the diagonal.
	 * When the fbAgent is in a building, we use the desktop directions.
	 * When in a road, we use the road as the separator, defining L and R as 0,1.
	 * @param dir
	 * @return the buildings in particular direction of the fbAgent
	 */
	public Set<EntityID> getBuildingsInNESW(int dir, EntityID position) {
		Set<EntityID> buildingsInDir = new FastSet<>();
		Pair<Integer, Integer> location = this.getEntity(position).getLocation(this);
		int x = location.first();
		int y = location.second();
		int range = (int)(this.getConfig().extinguishableDistance*0.9);
		Point pointLT = new Point(x-range, y+range);
		Point pointT = new Point(x, y+range);
		Point pointR = new Point(x+range, y);
		Point pointRB = new Point(x+range, y-range);
		Point pointB = new Point(x, y-range);
		Point pointL = new Point(x-range, y);
		Collection<StandardEntity> entities;
		switch(dir) {
		case 0:
			entities = this.getObjectsInRectangle(pointLT.x, pointLT.y, pointR.x, pointR.y);
			break;
		case 1:
			entities = this.getObjectsInRectangle(pointT.x, pointT.y, pointRB.x, pointRB.y);
			break;
		case 2:
			entities = this.getObjectsInRectangle(pointL.x, pointL.y, pointRB.x, pointRB.y);
			break;
		case 3:
			entities = this.getObjectsInRectangle(pointLT.x, pointLT.y, pointB.x, pointB.y);
			break;
		default:
			entities = this.getObjectsInRectangle(x-range, y-range, x+range, y+range);
		}
		
		for(StandardEntity se : entities) {
			if(se instanceof Building) {
				EntityID id = se.getID();
				buildingsInDir.add(id);
			}
		}
		System.out.println(this.getTime() + ", " + this.me + ", buildingsInDir" + dir + ":   " + buildingsInDir);
		return buildingsInDir;
	}
//	/**
//	 * When in a road, we use the road as the separator, defining L and R as 0,1.
//	 * @param dir
//	 * @return
//	 */
//	public Set<EntityID> getBuilldingsInLR(int dir, EntityID position) {
//		Set<EntityID> buildingsInDir = new FastSet<>();
//		Road road = (Road)this.getEntity(position);
//		List<Edge> roadEdge = road.getEdges();
//		int x1, y1, x2, y2;
//		double theta;
//		for(Edge edge : roadEdge) {
//			if(!edge.isPassable()) {
//				x1 = edge.getStartX(); y1 = edge.getStartY();
//				x2 = edge.getEndX(); y2 = edge.getEndY();
//				theta = Math.atan((double)(y2-y1)/(x2-x1));
//				break;
//			}
//		}
//		
//		System.out.println(position + ", the edges: " + roadEdge);
//		return null;
//	}
//		Pair<Integer, Integer> location = this.getEntity(position).getLocation(this);
//		int x = location.first();
//		int y = location.second();
//		int range = this.getConfig().extinguishableDistance;
//		Collection<StandardEntity> entities;
//		switch(dir) {
//		case 0:
//			entities = this.getObjectsInRectangle(pointLT.x, pointLT.y, pointR.x, pointR.y);
//			break;
//		case 1:
//			entities = this.getObjectsInRectangle(pointT.x, pointT.y, pointRB.x, pointRB.y);
//			break;
//		default:
//			entities = this.getObjectsInRectangle(x-range, y-range, x+range, y+range);
//	}
//	
	//local cluster and global map, fire condition, search globally. 
	/**
	 * used to set the global map's state for selecting particular extinguish strategy
	 */
	public enum CurrentState {
		notExplored,
		searched,
		burning,
		extiniguished,
		needResearched
	} 
	
	public void updateCurrentState() {
		
	}
		
}