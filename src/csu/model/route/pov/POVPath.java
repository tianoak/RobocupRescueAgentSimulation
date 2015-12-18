package csu.model.route.pov;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import csu.model.route.pov.graph.AreaNode;
import csu.model.route.pov.graph.PointNode;

import rescuecore2.worldmodel.EntityID;

/**
 * We use the A* search(pronounced "A-star search"), which is the most widely
 * known form of best-first search, to plan a path from start point to
 * destination. It evaluates nodes by combining <i>g(n)</i>, the cost to reach
 * the node, and <i>h(n)</i>, the cost to get from the node to the goal:
 * 
 * <pre>
 *          <i>f(n) = g(n) + h(n)</i>
 * </pre>
 * 
 * Since <i>g(n)</i> gives the path cost from the start node to node <i>n</i>,
 * and <i>h(n)</i> is the estimated cost of the cheapest path from <i>n</i> to
 * the goal, we have:
 * 
 * <pre>
 *      <i>f(n)</i> = estimated cost of the cheapest solution through <i>n</i>
 * </pre>
 * 
 * And we call <i>f(n)</i> as <b>evaluation function</b>, and <i>h(n)</i> as
 * <b>heuristic function</b>.
 * <p>
 * If you want to know more about A* search, please refer to <a
 * href="http://theory.stanford.edu/~amitp/GameProgramming"> Amit’s A* Pages</a>
 * 
 * @author utisam
 * 
 */
public class POVPath implements Comparable<POVPath> {

	private final PointNode point;
	public PointNode getPoint() {
		return point;
	}

	/**
	 * The <i>g(n)</i>.
	 */
	private final double routeCost;
	/**
	 * The heuristic function <i>h(n)</i>.
	 */
	private final double distance;
	
	private POVPath previous;

	public POVPath(AreaNode from, double distance) {
		this.point = from;
        this.setPrevious(null);
        this.routeCost = 0.0;
		this.distance = distance;
	}

	public POVPath(POVPath previous, PointNode next, CostFunction costFunc, double distance, Point start) {
		this.setPrevious(previous);
		this.point = next;
		this.routeCost = previous.routeCost + costFunc.cost(previous.point, next, start);
		
		this.distance = distance;
	}
	
	void setPrevious(POVPath previous) {
		this.previous = previous;
	}

	/**
	 * Get the real path represent by this POVPath.
	 * 
	 * @return the real path represent by this POVPath
	 */
	public List<EntityID> getRoute() {
		LinkedList<EntityID> route = new LinkedList<EntityID>();
    	for (POVPath rt = this; rt != null; rt = rt.getPrevious()) {
    		if (rt.point instanceof AreaNode) {
    			route.addFirst(((AreaNode)rt.point).getBelong().getID());    			
    		}
    	}
    	return route;
	}
	
	public List<PointNode> getPoints() {
		LinkedList<PointNode> route = new LinkedList<PointNode>();
    	for (POVPath rt = this; rt != null; rt = rt.getPrevious()) {
   			route.addFirst(rt.point);    			
    	}
    	return route;
	}
	
	public List<EntityID> getReverseRoute() {
		List<EntityID> route = getRoute();
		Collections.reverse(route);
    	return route;
	}

	public List<PointNode> getReversePoints() {
		List<PointNode> points = getPoints();
		Collections.reverse(points);
    	return points;
	}
	
	/**
	 * The evaluation function <i>f(n)</i>.
	 */
	public double cost() {
    	return routeCost + distance;
    }
	
	@Override
	public int compareTo(POVPath route) {
		return (int) (this.cost() - route.cost());
	}

	public POVPath getPrevious() {
		return previous;
	}

	/**
	 * Add a POVPath in the end of this POVPath. Because the POVPath can
	 * represent a real path, so we should connect the head of the added POVPath
	 * to the tail of this POVPath.
	 * 
	 * @param newTail
	 *            the added POVPath
	 * @return the added POVPath
	 */
	public POVPath add(final POVPath newTail) {
		POVPath current = newTail;
		for (; current.getPrevious() != null; current = current.getPrevious())
			;;;;;;;;;;;;;;;;;;;;;;;;;;;   ///;
		current.setPrevious(this.previous);
		return newTail;
	}
}
