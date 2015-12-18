package csu.model.route.pov.graph;

import java.util.Collection;
import java.util.HashMap;

import csu.model.AdvancedWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * We need to abstract the transportation network into a graph, and then do all
 * search works in this abstract graph. We abstract Area into AreaNode, and
 * passable Edge into EdgeNode. AreaNodes can only connect to EdgeNodes, and
 * EdgeNodes can only connect to AreaNodes, too. EdgeNode and AreaNode are
 * PointNode.
 * <p>
 * And this class encapsulates all PointNode of current map.
 * 
 * @author utisam
 */
public class PointOfVisivility {
	final private PointNode[] points;
	final private HashMap<EntityID, AreaNode> idToArea;
	
	public PointOfVisivility(final AdvancedWorldModel world) {
		POVBuilder builder = new POVBuilder(world);
		points = builder.getPointsResult();
		idToArea = builder.getIdToAreaResult();
	}

	public AreaNode get(EntityID id) {
		return idToArea.get(id);
	}

	public Collection<AreaNode> getAreaNodes() {
		return idToArea.values();
	}
	
	// unused
	public PointNode getPointFromHash(int nodeHash) {
		return points[nodeHash];
	}
	
	public int size() {
		return points.length;
	}
}
