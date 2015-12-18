package csu.model.route.pov.graph;

import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.standard.entities.Area;

/**
 * 
 */
class AreaNodeBase extends PointNode {

	protected Area belong;
	
	private Collection<EdgeNodeBase> neighbours;
	
	AreaNodeBase(Area area) {
		super(true, area.getX(), area.getY());
		belong = area;
		neighbours = new ArrayList<EdgeNodeBase>();
	}

	public Area getBelong() {
		return belong;
	}
	
	public Collection<EdgeNodeBase> getNeighbours() {
		return neighbours;
	}

	public void add(EdgeNodeBase edgeNode) {
		neighbours.add(edgeNode);
	}
}
