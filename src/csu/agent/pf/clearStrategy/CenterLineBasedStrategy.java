package csu.agent.pf.clearStrategy;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;
import csu.agent.Agent.ActionCommandException;
import csu.model.AdvancedWorldModel;
import csu.model.object.CSUBlockade;
import csu.model.object.CSUEdge;
import csu.model.object.CSURoad;
import csu.standard.Ruler;

public class CenterLineBasedStrategy extends AbstractStrategy{
	public CenterLineBasedStrategy(AdvancedWorldModel world) {
		super(world);
	}

	@Override
	public void clear() throws ActionCommandException {
		Area location = (Area)world.getSelfPosition();
		if (location instanceof Building)
			return;
		
		if (lastCyclePath == null)
			return;
		
		int index = 0 ;
		boolean found = false;
		for (EntityID next : lastCyclePath) {
			if (location.getID().getValue() == next.getValue()) {
				found = true;
				break;
			}
			
			index++;
		}
		
		if (!found)
			return;
		
		if (index == lastCyclePath.size() - 1)
			return;
		
		EntityID nextArea = lastCyclePath.get(index + 1);
		CSURoad road = world.getCsuRoad(location.getID());
		CSUEdge targetEdge = null;
		for (CSUEdge next : road.getCsuEdges()) {
			if (!next.getUnderlyingEdge().isPassable())
				continue;
			if (next.getUnderlyingEdge().getNeighbour().getValue() == nextArea.getValue())
				targetEdge = next;
		}
		if (targetEdge == null) {
			for (CSUEdge next : road.getCsuEdges()) {
				if  (next.getUnderlyingEdge().isPassable()) {
					targetEdge = next;
					break;
				}
			}
		}
		
		doClear(road.getSelfRoad(), targetEdge, null);
	}
	
	@Override
	public void doClear(Road roada, CSUEdge dir, Blockade targetB) throws ActionCommandException {
		CSURoad road = world.getCsuRoad(roada.getID());
		if (road.isEntrance()) {
			clearf(false);
			return;
		}
		
		PriorityQueue<Point2D> priori = new PriorityQueue<>(10, new Comparator<Point2D>() {

			@Override
			public int compare(Point2D o1, Point2D o2) {
				double dis_1 = Ruler.getDistance(x, y, o1.getX(), o1.getY());
				double dis_2 = Ruler.getDistance(x, y, o2.getX(), o2.getY());
				if (dis_1 > dis_2)
					return 1; 
				if (dis_1 < dis_2)
					return -1;
				return 0;
			}
		});
		Point2D selfL = new Point2D(x, y);
		rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(selfL, dir.getMiddlePoint());
		
		for (CSUBlockade next : road.getCsuBlockades()) {
			priori.addAll(getIntersections(next.getPolygon(), line));
		}
		
		if (priori.isEmpty())
			return;
		Point2D target = priori.remove();
		if (Ruler.getDistance(selfL, target) < world.getConfig().repairDistance) {
			Vector2D vector = new Vector2D(
					dir.getMiddlePoint().getX() - selfL.getX(), dir.getMiddlePoint().getY() - selfL.getY());
			vector.normalised().scale(1000000);
			underlyingAgent.sendClear(time, (int) (x + vector.getX()), (int) (y + vector.getY()));
		} else {
			List<EntityID> pa = new ArrayList<>();
			pa.add(road.getId());
			underlyingAgent.sendMove(time, pa, (int)target.getX(), (int)target.getY());
		}
	}
	
	private List<Point2D> getIntersections(Polygon polygon, rescuecore2.misc.geometry.Line2D line) {
		
		List<Point2D> result = new ArrayList<>();
		List<rescuecore2.misc.geometry.Line2D> polyLines = getLines(polygon);
		for (rescuecore2.misc.geometry.Line2D ln : polyLines) {
			Point2D p = GeometryTools2D.getSegmentIntersectionPoint(ln, line);
			if (p != null)
				result.add(p);
		}
		return result;
	}
	
	private List<rescuecore2.misc.geometry.Line2D> getLines(Polygon polygon) {
		List<rescuecore2.misc.geometry.Line2D> lines = new ArrayList<>();
		int count = polygon.npoints;
		for (int i = 0; i < count; i++) {
			int j = (i + 1) % count;
			Point2D p1 = new Point2D(polygon.xpoints[i], polygon.ypoints[i]);
			Point2D p2 = new Point2D(polygon.xpoints[j], polygon.ypoints[j]);
			rescuecore2.misc.geometry.Line2D line = new rescuecore2.misc.geometry.Line2D(p1, p2);
			lines.add(line);
		}
		return lines;
	}
}
