package csu.model.route.pov;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import csu.model.route.pov.graph.AreaNode;
import csu.model.route.pov.graph.EdgeNode;
import csu.model.route.pov.graph.PointNode;
import csu.model.route.pov.graph.PointOfVisivility;
import csu.model.route.pov.graph.RootNode;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;

public class POVSearch {
	
	final int routeThinkTime;
	final private PointOfVisivility pov;
	
	public POVSearch(final int thinkTime, PointOfVisivility pov) {
		this.routeThinkTime = thinkTime;
		this.pov = pov;
	}
	
	public POVPath getAStarPath(Human me, Area destination, CostFunction costFunc) {
		return getAStarPath(new RootNode(pov.get(me.getPosition()), me.getX(), me.getY()),
				destination, costFunc, new Point(me.getX(), me.getY()));
	}
	
	public POVPath getAStarPath(Area origin, Area destination, CostFunction costFunc, Point start) {
		return getAStarPath(new RootNode(pov.get(origin.getID()), origin.getX(), origin.getY()),
				destination, costFunc, start);
	}
	
	private POVPath getAStarPath(AreaNode from, Area destination, CostFunction costFunc, Point start) {
		if (destination == null) 
			return null;
		final long timeLimit = routeThinkTime + System.currentTimeMillis();
		
		PriorityQueue<POVPath> queue = new PriorityQueue<POVPath>();
		Set<PointNode> visited = new HashSet<PointNode>();
		
		queue.offer(new POVPath(from, from.distance(destination.getX(), destination.getY())));
		int deep = 0;
		
		do {
			POVPath currentRoute = queue.poll(); 
			
			if (visited.contains(currentRoute.getPoint())) 
				continue;
			visited.add(currentRoute.getPoint());
			
			if (currentRoute.getPoint() instanceof AreaNode) {
				AreaNode currentArea = (AreaNode) currentRoute.getPoint();
				if (currentArea.getBelong().equals(destination)) { 
					return currentRoute;
				}
				
				for (EdgeNode next : currentArea.getNeighbours()) {
					if (visited.contains(next)) 
						continue;
					double estimatedCost = currentArea.distance(destination.getX(), destination.getY());
					
					if (deep == 0) {
						queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, start));
					} else {
						queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, null));
					}
					
				}
				
				if (System.currentTimeMillis() > timeLimit) {
					//(new AssertionError("A* POV-Route plan create time over.")).printStackTrace();
					System.err.println("A* POV-Route plan create time over.");
					return currentRoute;
				}
				deep++;
				
			} else if (currentRoute.getPoint() instanceof EdgeNode) {
				EdgeNode currentEdge = (EdgeNode) currentRoute.getPoint();
				for (AreaNode next : currentEdge.getNeighbours()) {
					if (visited.contains(next)) 
						continue;
					double estimatedCost = currentEdge.distance(destination.getX(), destination.getY());
					queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, null));
				}
				deep++;
			}
		} while (!queue.isEmpty());
		return null;
	}
	
	public POVPath getDijkstraPath(Area origin, Set<StandardEntity> destinations, CostFunction costFunc, Point start) {
		if (destinations.size() == 0) 
			return null;
		final long timeLimit = routeThinkTime + System.currentTimeMillis();
		
		PriorityQueue<POVPath> queue = new PriorityQueue<POVPath>();
		Set<PointNode> visited = new HashSet<PointNode>();
		final AreaNode originNode = pov.get(origin.getID());
		queue.offer(new POVPath(originNode, 0.0));
		do {
			POVPath currentRoute = queue.poll(); 
			if (visited.contains(currentRoute.getPoint())) 
				continue;
			visited.add(currentRoute.getPoint());
			if (currentRoute.getPoint() instanceof AreaNode) {
				AreaNode currentArea = (AreaNode) currentRoute.getPoint();
				if (destinations.contains(currentArea.getBelong())) { 
					return currentRoute;
				}
				for (EdgeNode next : currentArea.getNeighbours()) {
					if (visited.contains(next)) 
						continue;
					queue.offer(new POVPath(currentRoute, next, costFunc, 0.0, null));
				}
				if (System.currentTimeMillis() > timeLimit) {
					System.err.println("Dijkstra POV-Route plan create time over.");
					return currentRoute;
				}
			} else if (currentRoute.getPoint() instanceof EdgeNode) {
				EdgeNode currentEdge = (EdgeNode) currentRoute.getPoint();
				for (AreaNode next : currentEdge.getNeighbours()) {

					if (visited.contains(next)) 
						continue;
					if (next.getBelong().equals(origin)) {
						queue.offer(new POVPath(currentRoute, next, costFunc, 0.0, start));
					} else {
						queue.offer(new POVPath(currentRoute, next, costFunc, 0.0, null));
					}
					//queue.offer(new POVPath(currentRoute, next, costFunc, 0.0, null));
				}
			}
		} while (!queue.isEmpty());
		return null;
	}
	
	public POVPath getMultiAStarPath(Area origin, 
			Collection<? extends StandardEntity> destinations, CostFunction costFunc, Point startP) {
		if (origin == null || destinations == null) 
			return null;
		if (destinations.isEmpty())
			return null;
		final long timeLimit = routeThinkTime + System.currentTimeMillis();
		
		PriorityQueue<POVPath> queue = new PriorityQueue<POVPath>();
		Set<PointNode> visited = new HashSet<PointNode>();
		for (StandardEntity se : destinations) {
			AreaNode start = pov.get(se.getID());
			queue.offer(new POVPath(start, start.distance(origin.getX(), origin.getY())));
		}
		do {
			POVPath currentRoute = queue.poll(); 
			if (visited.contains(currentRoute.getPoint())) 
				continue;
			visited.add(currentRoute.getPoint());
			if (currentRoute.getPoint() instanceof AreaNode) {
				AreaNode currentArea = (AreaNode) currentRoute.getPoint();
				if (currentArea.getBelong().equals(origin)) { 
					return currentRoute;
				}
				for (EdgeNode next : currentArea.getNeighbours()) {
					if (visited.contains(next))
						continue;
					double estimatedCost = currentArea.distance(origin.getX(), origin.getY());
					queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, null));
				}
				if (System.currentTimeMillis() > timeLimit) {
					System.err.println("A* POV-Route plan (MultiDest) create time over.");
					return currentRoute;
				}
			} else if (currentRoute.getPoint() instanceof EdgeNode) {
				EdgeNode currentEdge = (EdgeNode) currentRoute.getPoint();
				for (AreaNode next : currentEdge.getNeighbours()) {
					if (visited.contains(next)) 
						continue;
					double estimatedCost = currentEdge.distance(origin.getX(), origin.getY());
					if (next.getBelong().equals(origin)) {
						queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, startP));
					} else {
						queue.offer(new POVPath(currentRoute, next, costFunc, estimatedCost, null));
					}
					
				}
			}
		} while (!queue.isEmpty());
		return null;
	}
}
