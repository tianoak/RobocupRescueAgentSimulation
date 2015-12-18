package csu.model.route.pov.graph;

public class RootNode extends AreaNode {
	
	private final int x;
	private final int y;

	public RootNode(AreaNode areaNode, int x, int y) {
		super(-1, new AreaNodeBase(areaNode.getBelong()));
		this.x = x;
		this.y = y;
		for (EdgeNode e : areaNode.getNeighbours()) {
			this.addNeighbour(e);
		}
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}
}
