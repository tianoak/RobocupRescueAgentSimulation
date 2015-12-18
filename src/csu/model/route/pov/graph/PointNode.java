package csu.model.route.pov.graph;

import rescuecore2.misc.Pair;
/**
 * 
 */
public class PointNode {
	
	private final boolean endable;
	private final Pair<Integer, Integer> position;
	
	// constructor
	public PointNode(final boolean endable, int x, int y) {
		this.endable = endable;
		position = new Pair<Integer, Integer>(x, y);
	}

	public boolean isEndable() {
		return endable;
	}
	
	public int getX() {
		return position.first();
	}

	public int getY() {
		return position.second();
	}
	
	@Override
	public int hashCode() {
		return position.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof PointNode) {
			return position.equals(((PointNode)o).position);
		}
		return false;
	}

	public double distance(int x, int y) {
		return Math.hypot(this.getX() - x, this.getY() - y);
	}

	public double distance(PointNode next) {
		return distance(next.getX(), next.getY());
	}
	
}
