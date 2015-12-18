package csu.model.route.pov.graph;

import java.util.ArrayList;
import java.util.Collection;

public class EdgeNode extends PointNode {

	private Collection<AreaNode> neighbours;
	private boolean isTooSmall = false;
	private final int id;
	
	public EdgeNode(int id, EdgeNodeBase edge) {
		super(false, edge.getX(), edge.getY());
		this.id = id;
		neighbours = new ArrayList<AreaNode>();
		if (edge.getLength() < 600)
			isTooSmall = true;
	}
	
	public Collection<AreaNode> getNeighbours() {
		return neighbours;
	}
	
	public void add(AreaNode areaNode) {
		neighbours.add(areaNode);
	}

	@Override
	public int hashCode() {
		return id;
	}
	@Override
	public boolean equals(Object o) {
		return o != null && this.hashCode() == o.hashCode();
	}

	public void addNeighbour(AreaNode areaNode) {
		neighbours.add(areaNode);
	}
	
	public boolean isTooSmall() {
		return isTooSmall;
	}
}
