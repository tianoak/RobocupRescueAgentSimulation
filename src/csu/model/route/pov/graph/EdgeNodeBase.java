package csu.model.route.pov.graph;

import java.util.ArrayList;
import java.util.Collection;

import rescuecore2.standard.entities.Edge;

public class EdgeNodeBase extends PointNode {

	private Collection<AreaNodeBase> neighbours;
	private Edge edge;
	
	EdgeNodeBase(Edge edge) {
		super(false, (edge.getStartX() + edge.getEndX()) / 2, (edge.getStartY() + edge.getEndY()) / 2);
		this.edge = edge;
		this.neighbours = new ArrayList<AreaNodeBase>();
	}
	
	public Collection<AreaNodeBase> getNeighbours() {
		return this.neighbours;
	}
	
	public void add(AreaNodeBase areaNode) {
		this.neighbours.add(areaNode);
	}
	
	public Edge getEdge() {
		return this.edge;
	}
	
	public int getLength() {
		double dis = Math.hypot(edge.getStartX() - edge.getEndX(), edge.getStartY() - edge.getEndY());
		return (int)dis;
	}
}
