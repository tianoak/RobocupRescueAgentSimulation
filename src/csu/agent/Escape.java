//package csu.agent;
//
//import java.awt.Shape;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import rescuecore2.misc.Pair;
//import rescuecore2.misc.geometry.GeometryTools2D;
//import rescuecore2.misc.geometry.Line2D;
//import rescuecore2.misc.geometry.Point2D;
//import rescuecore2.standard.entities.Area;
//import rescuecore2.standard.entities.Blockade;
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.Edge;
//import rescuecore2.standard.entities.Human;
//import rescuecore2.standard.entities.PoliceForce;
//import rescuecore2.standard.entities.Road;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.worldmodel.EntityID;
//
//import csu.agent.Agent.ActionCommandException;
//import csu.model.AdvancedWorldModel;
//import csu.model.object.CSUEdge;
//import csu.model.object.CSURoad;
//import csu.standard.Ruler;
//import csu.util.Util;
//
//public class Escape {
//
//	private AdvancedWorldModel world;
//	
//	private Human underlyingHuman;
//	
//	private Pair<Integer, Integer> lastPositionCoordinate;
//	
//	private EntityID position;
//	
//	private EntityID nextArea;
//	
//	private int stuckThreshold;
//	
//	private EntityID lastStuckAreaId;
//	
//	private List<Pair<Integer, Integer>> escapeCoordinates;
//	
//	private EscapeState escapeState;
//	
//	private int moveDistance;
//	
//	private List<EntityID> planedPath;
//	
//	public enum EscapeState {
//		FAILED,
//		STUCK,
//		BURIED,
//		MOVE_ON_COORDINATE,
//		DEFAULT,
//		UNREACHABLE
//	}
//	
//	// constructor
//	public Escape(AdvancedWorldModel world) {
//		this.world = world;
//		this.underlyingHuman = (Human)world.getControlledEntity();
//		
//		this.escapeCoordinates = new ArrayList<>();
//		this.stuckThreshold = 2000;
//		this.escapeState = EscapeState.DEFAULT;
//		
//		this.lastPositionCoordinate = world.getSelfLocation();
//		this.lastStuckAreaId = null;
//		
//		this.nextArea = null;
//	}
//	
//	/**
//	 * Determines whether this can move or not.
//	 * 
//	 * @param isThisTimeToMoveToPoint
//	 *            flags to determines whether
//	 * @param locationTargetAreSame
//	 *            flags to determines whether
//	 * @return true when this agent cannot move. Otherwise, false.
//	 */
//	public boolean connotMove(boolean isThisTimeToMoveToPoint, boolean locationTargetAreSame) {
//		if (world.getTime() < world.getConfig().ignoreUntil)
//			return false;
//		
//		this.position = world.getSelfPosition().getID();
//		
//		if (underlyingHuman.isBuriednessDefined() && underlyingHuman.getBuriedness() > 0) {
//			this.escapeState = EscapeState.BURIED;
//			return true;
//		}
//		
//		if (escapeState.equals(EscapeState.MOVE_ON_COORDINATE))
//			return true;
//		
//		if (isThisTimeToMoveToPoint)
//			return false;
//		
//		if (locationTargetAreSame)
//			return false;
//		
//		this.moveDistance = (int)Ruler.getDistance(this.lastPositionCoordinate, world.getSelfLocation());
//		if (moveDistance <= stuckThreshold)
//			return true;
//		
//		this.lastPositionCoordinate = world.getSelfLocation();
//		return false;
//	}
//	
//	public EscapeState escape(Area target, List<EntityID> plan) throws ActionCommandException{
//		this.position = world.getSelfPosition().getID();
//		this.planedPath = plan;
//		
//		if (target == null)
//			return EscapeState.DEFAULT;
//		
//		if (escapeState.equals(EscapeState.BURIED))
//			return escapeState;
//		
//		if (escapeCoordinates.size() > 0) {
//			moveOnCoordiante();
//		}
//		
//		boolean flag_1 = world.getControlledEntity() instanceof PoliceForce;
//		boolean flag_2 = isLockByBlockade();
//		boolean flag_3 = isolated();
//		
//		if (!flag_1 && (flag_2 || flag_3)) {
//			System.out.println("isolated = " + flag_3 + " lock = " + flag_2 +" ----- Time: " 
//						+ world.getTime() + ", agent: " + world.getControlledEntity());
//
//			this.escapeState = EscapeState.STUCK;
//			return escapeState;
//		}
//		
//		if (plan.isEmpty()) {
//			this.escapeState = EscapeState.UNREACHABLE;
//			return escapeState;
//		}
//		
//		this.nextArea = getNextPosition();
//		StandardEntity positionEntity = world.getSelfPosition();
//		if (positionEntity instanceof Road) {
//			escape((Road) positionEntity);
//		} else if (positionEntity instanceof Building) {
//			escape((Building) positionEntity);
//		}
//		
//		if (escapeState.equals(EscapeState.MOVE_ON_COORDINATE)) {
//			moveOnCoordiante();
//		}
//		
//		return EscapeState.FAILED;
//	}
//	
//	private void moveOnCoordiante() throws ActionCommandException{
//		Pair<Integer, Integer> escape = this.escapeCoordinates.remove(0);
//		if (escapeCoordinates.size() == 0) {
//			escapeState = EscapeState.DEFAULT;
//		}
//		
//		((HumanoidAgent<?>)world.getAgent()).moveToPoint(position, escape.first(), escape.second());
//	}
//	
//	private boolean isLockByBlockade() {
//		if (world.getSelfPosition() instanceof Building)
//			return false;
//		
//		Human human = (Human)world.getControlledEntity();
//		
//		if (!human.isPositionDefined())
//			return false;
//
//		StandardEntity entity = world.getEntity(human.getPosition());
//		if (entity instanceof Road) {
//			Road road = (Road) entity;
//			Blockade blockade;
//			Shape shape;
//
//			if (!road.isBlockadesDefined())
//				return false;
//
//			for (EntityID next : road.getBlockades()) {
//				blockade = world.getEntity(next, Blockade.class);
//
//				if (blockade == null || !blockade.isApexesDefined())
//					continue;
//
//				shape = Util.getPolygon(blockade.getApexes());
//				if (shape.contains(human.getX(), human.getY())) {
//					return true;
//				}
//			}
//		}
//
//		return false;
//	}
//	
//	private boolean isolated() {
//		return getReachableNeighbours(world.getEntity(position, Area.class)).size() == 0;
//	}
//	
//	private EntityID getNextPosition() {
//		if (this.planedPath.isEmpty())
//			return null;
//		
//		int posIndex = this.planedPath.indexOf(position);
//		if (posIndex + 1 == this.planedPath.size())
//			return this.planedPath.get(posIndex);
//		
//		
//		return this.planedPath.get(posIndex + 1);
//	}
//	
//	private void escape(Road road) {
//		this.escapeCoordinates.clear();
//		
//		if (this.nextArea != null && road.getNeighbours().contains(nextArea)) {
//			CSURoad csuRoad = world.getCsuRoad(road.getID());
//			Pair<Integer, Integer> openPartPoint = null;
//			
//			for (CSUEdge next : csuRoad.getCsuEdgeTo(nextArea)) {
//				if (next.isBlocked())
//					continue;
//				
//				Point2D escapePoint = getMiddle(next.getOpenPart());
//				Point2D positionPoint = 
//						new Point2D(world.getSelfLocation().first(), world.getSelfLocation().second());
//				
//				// Find the parallel line of current CsuEdge, which through the positionPoint. And get a 
//				// another point in this parallel line. Defualt, anotherPoint.x = 0 or anotherPoint.y = 0
//				double molecular = next.getOpenPartEnd().getY() - next.getOpenPartStart().getY();
//				double denominator = next.getOpenPartEnd().getX() - next.getOpenPartStart().getX();
//				double slope = molecular / denominator;
//				double x = 0, y = 0;
//				if (Double.isInfinite(slope) || slope >= Double.MAX_VALUE / 2.0) {
//					x = positionPoint.getX();
//				} else {
//					y = positionPoint.getY() - slope * positionPoint.getX();
//				}
//				
//				Point2D anotherPoint = new Point2D(x, y);
//				Point2D parallelPoint = GeometryTools2D.getClosestPoint(
//						new Line2D(positionPoint, anotherPoint), escapePoint);
//				
//				Line2D improvedLine = Util.improveLine(new Line2D(parallelPoint, escapePoint), 500);
//				
//				openPartPoint = new Pair<Integer, Integer>(
//						(int)improvedLine.getEndPoint().getX(), (int)improvedLine.getEndPoint().getY());
//				if (openPartPoint != null)
//					break; // exist the for loop
//			}
//			
//			if (openPartPoint != null) {
//				this.escapeCoordinates.add(openPartPoint);
//				this.escapeState = EscapeState.MOVE_ON_COORDINATE;
//			}
//			
////			if (escapeCoordinates.isEmpty()) {
////				Pair<Integer, Integer> escapeBlockadePoint = null;
////				
////				Area positionArea = world.getEntity(position, Area.class);
////				Area nextArea = world.getEntity(this.nextArea, Area.class);
////				
////				if (positionArea instanceof Road && !((Road)positionArea).getBlockades().isEmpty()) {
////					
////				}
////			}
//		}
//	}
//	
//	private void escape(Building building) {
//		escapeCoordinates.clear();
//		
//		if (nextArea != null && world.getEntity(position, Area.class).getNeighbours().contains(nextArea)) {
//			Pair<Integer, Integer> positionAreaPoint = ((Area)world.getSelfPosition()).getLocation(world);
//			Point2D escapePoint = new Point2D(positionAreaPoint.first(), positionAreaPoint.second());
//			
//			Point2D edgeMiddle = null;
//			if (world.getEntity(nextArea, Area.class) instanceof Road) {
//				CSURoad csuRoad = world.getCsuRoad(nextArea);
//				
//				for (CSUEdge next : csuRoad.getCsuEdgeTo(position)) {
//					if (!next.isBlocked()) {
//						edgeMiddle = getMiddle(next.getOpenPart());
//						break;
//					}
//				}
//			}
//			
//			if (edgeMiddle == null) {
//				Edge edge = world.getEntity(position, Area.class).getEdgeTo(nextArea);
//				edgeMiddle = getMiddle(edge.getLine());
//			}
//			
//			Line2D line = Util.improveLine(new Line2D(escapePoint,  edgeMiddle), -1000);
//			escapePoint = line.getEndPoint();
//			this.escapeCoordinates.add(
//					new Pair<Integer, Integer>((int)escapePoint.getX(), (int)escapePoint.getY()));
//			
//			line = Util.improveLine(new Line2D(escapePoint, edgeMiddle), 1000);
//			escapePoint = line.getEndPoint();
//			this.escapeCoordinates.add(
//					new Pair<Integer, Integer>((int)escapePoint.getX(), (int)escapePoint.getY()));
//			
//			this.escapeState = EscapeState.MOVE_ON_COORDINATE;
//		}
//	}
//	
//	private Point2D getMiddle(Line2D line) {
//		double cx = (line.getOrigin().getX() + line.getEndPoint().getX()) / 2.0;
//		double cy = (line.getOrigin().getY() + line.getEndPoint().getY()) / 2.0;
//		
//		return new Point2D(cx, cy);
//	}
//	
//	public Set<EntityID> getReachableNeighbours(Area area) {
//    	Set<EntityID> result = new HashSet<>();
//    	
//    	if (area == null)
//    		return result;
//    	
//    	if (area instanceof Road) {
//    		if (world.getCsuRoad(area.getID()).getPassableEdge().isEmpty())
//    			return result;
//    	}
//    	
//    	for (EntityID next : area.getNeighbours()) {
//    		Area neighbour = world.getEntity(next, Area.class);
//    		if (neighbour instanceof Road) {
//    			for (CSUEdge edge : world.getCsuRoad(next).getCsuEdgeTo(area.getID())) {
//    				if (!edge.isBlocked()) {
//    					result.add(next);
//    					break;
//    				}
//    			}
//    		} else if (area instanceof Road) {
//    			for (CSUEdge edge : world.getCsuRoad(area.getID()).getCsuEdgeTo(next)) {
//    				if (!edge.isBlocked()) {
//    					result.add(next);
//    					break;
//    				}
//    			}
//    		} else {
//    			result.add(next);
//    		}
//    	}
//    	
//    	return result;
//    }
//	
////	private Point2D getPointToEscapeBlockade(CSURoad csuRoad, Line2D line) {
////		Line2D nearestLine = null;
////		List<Line2D> instersectLine
////	}
//}
