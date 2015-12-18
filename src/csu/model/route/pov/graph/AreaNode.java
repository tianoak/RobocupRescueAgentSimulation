package csu.model.route.pov.graph;

import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.standard.entities.Area;

/**
 * 
 */
public class AreaNode extends PointNode {

	protected Area belong;
	
	private Collection<EdgeNode> neighbours;

	private final int id;
	
	public AreaNode(int id, AreaNodeBase area) {
		super(true, area.getX(), area.getY());
		this.id = id;
		belong = area.getBelong();
		neighbours = new ArrayList<EdgeNode>();
	}

	public Area getBelong() {
		return belong;
	}
	
	public Collection<EdgeNode> getNeighbours() {
		return neighbours;
	}

	@Override
	public int hashCode() {
		return id;
	}
	@Override
	public boolean equals(Object o) {
		return o != null && this.hashCode() == o.hashCode();
	}

	public void addNeighbour(EdgeNode edgeNode) {
		neighbours.add(edgeNode);
	}
}
