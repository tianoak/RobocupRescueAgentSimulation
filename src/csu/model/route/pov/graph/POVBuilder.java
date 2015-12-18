package csu.model.route.pov.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import csu.Viewer.layers.CSU_PovLayer;
import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.util.IdSorter;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * This class translate Areas and passable Edges into PointNode.
 * 
 * @author utisam
 *
 */
class POVBuilder {
	final private PointNode[] points;
	public PointNode[] getPointsResult() {
		return points;
	}
	
	final private HashMap<EntityID, AreaNode> idToArea;
	public HashMap<EntityID, AreaNode> getIdToAreaResult() {
		return idToArea;
	}
	///why
	private final Comparator<PointNode> baseNodeComparator = new Comparator<PointNode>() {
		@Override
		public int compare(PointNode a, PointNode b) {
			int xResult = a.getX() - b.getX();
			if (xResult == 0) {return a.getY() - b.getY();}
			return xResult;
		}
	};
	
	// constructor
	POVBuilder(final AdvancedWorldModel world) {
		List<PointNode> basePoints = createBasics(world);
		points = new PointNode[basePoints.size()];
		idToArea = new HashMap<EntityID, AreaNode>(world.getEntitiesOfType(AgentConstants.AREAS).size());
		
		for (int i = 0; i < points.length; i++) {
			PointNode p = basePoints.get(i);
			if (p instanceof AreaNodeBase) {
				AreaNode areaNode = new AreaNode(i, (AreaNodeBase) p);
				points[i] = areaNode;
				idToArea.put(areaNode.getBelong().getID(), areaNode);
			} else /*if (p instanceof EdgeNodeBase)*/ {
				points[i] = new EdgeNode(i, (EdgeNodeBase) p);
			}
		}
		
		// Neighbours的设定
		for (int i = 0; i < points.length; i++) {
			PointNode p = basePoints.get(i);
			
			if (p instanceof AreaNodeBase) {
				AreaNodeBase base = (AreaNodeBase) p;
				AreaNode node = (AreaNode) points[i];
				for (EdgeNodeBase e : base.getNeighbours()) {
					int index = Collections.binarySearch(basePoints, e, baseNodeComparator);
					node.addNeighbour((EdgeNode) points[index]);
				}
			} else /*if (p instanceof EdgeNodeBase)*/ {
				EdgeNodeBase base = (EdgeNodeBase) p;
				EdgeNode node = (EdgeNode) points[i];
				for (AreaNodeBase e : base.getNeighbours()) {
					int index = Collections.binarySearch(basePoints, e, baseNodeComparator);
					node.addNeighbour((AreaNode) points[index]);
				}
			}
		}
	}
	
	private List<PointNode> createBasics(final AdvancedWorldModel world) {
		HashMap<EntityID, AreaNodeBase> areaBases = new HashMap<EntityID, AreaNodeBase>();
		Set<Pair<EntityID, EntityID>> fromtoTuples = new HashSet<Pair<EntityID, EntityID>>();
		
		ArrayList<StandardEntity> areas = new ArrayList<>(world.getEntitiesOfType(AgentConstants.AREAS));
		
		Collections.sort(areas, new IdSorter());
		
		for (StandardEntity se : areas) {
			Area area = (Area) se;
			areaBases.put(area.getID(), new AreaNodeBase(area));
			for (EntityID neighbourID : area.getNeighbours()) {
				if (!fromtoTuples.contains(new Pair<EntityID, EntityID>(neighbourID, area.getID()))) {
					fromtoTuples.add(new Pair<EntityID, EntityID>(area.getID(), neighbourID));					
				}
			}
		}
		
		List<PointNode> points = new ArrayList<PointNode>(areaBases.values());
		
		List<EdgeNodeBase> edgeNodeBase = new ArrayList<>();    // only for test
		
		for (Pair<EntityID, EntityID> tuple : fromtoTuples) {
			final Area area1 = (Area) world.getEntity(tuple.first());
			final Area area2 = (Area) world.getEntity(tuple.second());
			if (area1 == null || area2 == null) 
				continue;
			
			final Edge edge1to2 = area1.getEdgeTo(tuple.second());
			final Edge edge2to1 = area2.getEdgeTo(tuple.first());
			
			if (edge1to2 == null || edge2to1 == null) 
				continue;
			if (!edge1to2.isPassable() || !edge2to1.isPassable()) 
				continue;
			
			EdgeNodeBase edgeNode;
			if (getLength(edge1to2) < getLength(edge2to1)) {
				edgeNode = new EdgeNodeBase(edge1to2);
			} else {
				edgeNode = new EdgeNodeBase(edge2to1);
			}
			points.add(edgeNode);
			
			if (AgentConstants.LAUNCH_VIEWER) {
				edgeNodeBase.add(edgeNode);
			}
			final AreaNodeBase areaNode1 = areaBases.get(tuple.first());
			final AreaNodeBase areaNode2 = areaBases.get(tuple.second());
			
			edgeNode.add(areaNode1);
			edgeNode.add(areaNode2);
			areaNode1.add(edgeNode);
			areaNode2.add(edgeNode);
		}
		
		Collections.sort(points, baseNodeComparator);
		
		return points;
	}
	
	/**
	 * Get the length of a <code>Edge</code>
	 * 
	 * @param edge
	 *            the target <code>Edge</code>
	 * @return the length of this <code>Edge</code>
	 */
	private static double getLength(Edge edge) {
		rescuecore2.misc.geometry.Point2D s = edge.getStart();
		rescuecore2.misc.geometry.Point2D e = edge.getEnd();
		return Math.hypot(s.getX() - e.getX(), s.getY() - e.getY());
	}
}
