package csu.model.route.pov.reachable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import csu.model.AdvancedWorldModel;
import csu.model.AgentConstants;
import csu.model.route.pov.graph.AreaNode;
import csu.model.route.pov.graph.EdgeNode;
import csu.model.route.pov.graph.PassableDictionary;
import csu.util.UnionFindTree;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Huge map only.
 * 
 * @author utisam
 * 
 */
public class UFTReachableArea {
	private UnionFindTree<EntityID> sureReachableTree;
	
	public UFTReachableArea(AdvancedWorldModel world) {
		Collection<StandardEntity> areas = world.getEntitiesOfType(AgentConstants.AREAS);
		ArrayList<EntityID> ids = new ArrayList<EntityID>(areas.size());
		for (StandardEntity se : areas) {
			ids.add(se.getID());
		}
		sureReachableTree = new UnionFindTree<EntityID>(ids);
	}
	
	public void update(final AdvancedWorldModel world, final Set<EdgeNode> newPassables) {
		if (world.getTime() > world.getConfig().ignoreUntil) {
			updateSureReachable(world, newPassables);
		}
	}
	
	private void updateSureReachable(final AdvancedWorldModel world, Set<EdgeNode> newPassables) {
		//sureReachableTree.resetAll();
		final PassableDictionary passableDic = world.getRouter().getPassableDic();
		for (EdgeNode edge : newPassables) {
			AreaNode first = null;
			for (AreaNode area : edge.getNeighbours()) {
				if (passableDic.getPassableLevel(area, edge, null).isPassable()) {
					if (first == null) {
						first = area;
					} else {
						sureReachableTree.unite(first.getBelong().getID(), area.getBelong().getID());
					}
				}
			}
		}
	}

	public boolean isSureReachable(EntityID id, EntityID id2) {
		return sureReachableTree.same(id, id2);
	}
}
