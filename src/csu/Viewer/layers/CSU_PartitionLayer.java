package csu.Viewer.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.Icons;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;

import csu.agent.at.cluster.Cluster;
import csu.geom.CompositeConvexHull;

/**
 * Date: Mar 30, 2014   Time: 12:27PM
 * 
 * @author appreciation-csu
 *
 */
public class CSU_PartitionLayer extends StandardViewLayer{
	private static final Color AT_PARTITION_COLOR = Color.CYAN;
	private static final Stroke AT_PARTITION_STROKE = new BasicStroke(1.0f);
	
	private static final Color FB_PARTITION_COLOR = Color.green;
	private static final Stroke FB_PARTITION_STROKE = new BasicStroke(1.0f);
	
	private static final Color CIRCLE_COLOR = Color.BLUE;
	private static final Stroke CIRCLE_SROKE = new BasicStroke(1.5f);
	
	public static List<Cluster> AT_CLUSTERS = Collections.synchronizedList(new ArrayList<Cluster>());
	public static int RADIUS_LENGTH = 0;
	
	public static List<csu.agent.fb.cluster.Cluster> FB_CLUSTERS = 
			Collections.synchronizedList(new ArrayList<csu.agent.fb.cluster.Cluster>());
	
	private boolean shouldRenderAtPartition;
	private RenderAtPartitionAction renderAtPartitionAction;
	
	private boolean shouldRenderAtWorkArea;
	private RenderAtWorkAreaAction renderAtWorkAreaAction;
	
	private boolean shouldRenderFbPartition;
	private RenderFbPartitionAction renderFbPartitionAction;
	
	// private static final double MEAN_VELOCITY_OF_MOVING = 31445.392;
	public static int CLUSTER_RANGE_THRESHOLD;
	
	
	public CSU_PartitionLayer () {
		shouldRenderAtPartition = false;
		renderAtPartitionAction = new RenderAtPartitionAction();
		
		shouldRenderAtWorkArea = false;
		renderAtWorkAreaAction = new RenderAtWorkAreaAction();
		
		shouldRenderFbPartition = false;
		renderFbPartitionAction = new RenderFbPartitionAction();
	}
	
	@Override
    public List<JMenuItem> getPopupMenuItems() {
    	List<JMenuItem> result = new ArrayList<>();
    	result.add(new JMenuItem(renderAtPartitionAction));
    	result.add(new JMenuItem(renderFbPartitionAction));
    	result.add(new JMenuItem(renderAtWorkAreaAction));
    	return result;
    }

	@Override
	public String getName() {
		return "partition";
	}
	
	@Override
	public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
		Collection<RenderedObject> resultList = new ArrayList<>();
		
		if (shouldRenderAtPartition && AT_CLUSTERS != null && !AT_CLUSTERS.isEmpty()) {
			renderAtPartition(g, transform, AT_CLUSTERS);
		}
		
		if (shouldRenderFbPartition && FB_CLUSTERS != null && !FB_CLUSTERS.isEmpty()) {
			renderFbPartition(g, transform, FB_CLUSTERS);
		}
		
		if (shouldRenderAtWorkArea) {
			renderAtWorkArea(g, transform);
		}
		
		return resultList;
	}
	
	private void renderAtPartition(Graphics2D g, ScreenTransform t, List<Cluster> partitions) {
		g.setColor(AT_PARTITION_COLOR);
		g.setStroke(AT_PARTITION_STROKE);
		StandardEntity entity;
		Building bui;
		CompositeConvexHull convexHull;
		for (Cluster cluster : partitions) {
			convexHull = new CompositeConvexHull();
			for (EntityID next : cluster.getCluster()) {
				entity = world.getEntity(next);
				if (entity instanceof Building) {
					bui = (Building)entity;
					
					transfer(bui.getApexList(), convexHull, t);
				}
			}
			g.draw(convexHull.getConvexPolygon());
		}
	}
	
	private void renderFbPartition(Graphics2D g, ScreenTransform t, List<csu.agent.fb.cluster.Cluster> partitions) {
		g.setColor(FB_PARTITION_COLOR);
		g.setStroke(FB_PARTITION_STROKE);
		StandardEntity entity;
		Area area;
		CompositeConvexHull convexHull;
		for (csu.agent.fb.cluster.Cluster cluster : partitions) {
			convexHull = new CompositeConvexHull();
			for (EntityID next : cluster.getPoints()) {
				entity = world.getEntity(next);
				if (entity instanceof Area) {
					area = (Area) entity;
					
					transfer(area.getApexList(), convexHull, t);
				}
			}
			g.draw(convexHull.getConvexPolygon());
		}
	}
	
	private void transfer(int[] apexList, CompositeConvexHull polygon, ScreenTransform t) {
		int vertexCount = apexList.length;	
		int x_coordinate, y_coordinate;
		for (int i = 0; i < vertexCount; i += 2) {
			x_coordinate = t.xToScreen(apexList[i]);
			y_coordinate = t.yToScreen(apexList[i +1]);
			polygon.addPoint(x_coordinate, y_coordinate);
		}
	}
	
	private void renderAtWorkArea(Graphics2D g, ScreenTransform transform) {
		g.setColor(CIRCLE_COLOR);
		g.setStroke(CIRCLE_SROKE);
		
		AmbulanceTeam at;
		for (StandardEntity next : world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM)) {
			at = (AmbulanceTeam) next;
			int x_coordinate = 0, y_coordinate = 0;
			if (at.isXDefined() && at.isYDefined()) {
				x_coordinate = at.getX();
				y_coordinate = at.getY();
			}
			if (x_coordinate != 0 && y_coordinate != 0) {
				Polygon polygon = makePolygon(20, x_coordinate, y_coordinate, RADIUS_LENGTH, transform);
				
				g.draw(polygon);
			}
		}
		
		// TODO
		Building building = (Building)world.getEntity(new EntityID(55169));
		Polygon polygon_1 = makePolygon(20, building.getX(), building.getY(), RADIUS_LENGTH, transform);
		g.drawPolygon(polygon_1);
		
	}
	
	/**
	 * Create a polygon.
	 * 
	 * @param vertexsCount
	 *            the vertex count of this polygon
	 * @param center_x
	 *            the x coordinate of this polygon's center
	 * @param center_y
	 *            the y coordinate of this polygon's center
	 * @param radius
	 *            the radius length of this polygon's
	 * @return a polygon with given vertex count.
	 */
	private Polygon makePolygon(int vertexsCount, double center_x, double center_y, double radius, ScreenTransform t) {
		double dAngle = Math.PI * 2 / vertexsCount;
		int[] x_coordinates = new int[vertexsCount];
		int[] y_coordinates = new int[vertexsCount];
		
		for (int i = 0; i < vertexsCount; i++) {
			double angle = i * dAngle;
			Vector2D vector = new Vector2D(Math.sin(angle), Math.cos(angle)).scale(radius);
			Point2D centerPoint = new Point2D(center_x, center_y);
			Point2D vertexPoint = centerPoint.translate(vector.getX(), vector.getY());
			
			x_coordinates[i] = t.xToScreen((int)vertexPoint.getX());
			y_coordinates[i] = t.yToScreen((int)vertexPoint.getY());
		}
		
		return new Polygon(x_coordinates, y_coordinates, vertexsCount);
	}
	
	
	@SuppressWarnings("serial")
	private final class RenderAtPartitionAction extends AbstractAction {

		public RenderAtPartitionAction() {
			super("AT Partition");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderAtPartition));
			putValue(Action.SMALL_ICON, shouldRenderAtPartition ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			shouldRenderAtPartition = ! shouldRenderAtPartition;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderAtPartition));
			putValue(Action.SMALL_ICON, shouldRenderAtPartition ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class RenderAtWorkAreaAction extends AbstractAction  {

		public RenderAtWorkAreaAction() {
			super("AT Work Area");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderAtWorkArea));
			putValue(Action.SMALL_ICON, shouldRenderAtWorkArea ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			shouldRenderAtWorkArea = ! shouldRenderAtWorkArea;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderAtWorkArea));
			putValue(Action.SMALL_ICON, shouldRenderAtWorkArea ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class RenderFbPartitionAction extends AbstractAction {

		public RenderFbPartitionAction() {
			super("FB Partition");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderFbPartition));
			putValue(Action.SMALL_ICON, shouldRenderFbPartition ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			shouldRenderFbPartition = ! shouldRenderFbPartition;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(shouldRenderFbPartition));
			putValue(Action.SMALL_ICON, shouldRenderFbPartition ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
}
