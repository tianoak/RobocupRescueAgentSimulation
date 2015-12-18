package csu.Viewer.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import math.geom2d.conic.Circle2D;

import csu.Viewer.SelectedObject;
import csu.agent.pf.cluster.Cluster;
import csu.geom.PolygonScaler;
import csu.model.object.csuZoneEntity.CsuZone;
import csu.model.object.csuZoneEntity.CsuZones;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.Icons;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;

/**
 * A layer for building zones. In this layer we can show zone ids, zone
 * neighbours and zone's convex hull polygon.
 * 
 * Date: Mar 8, 2014 Time: 8:23pm
 * 
 * @author appreciation-csu
 */
public class CSU_ZonePolygonLayer extends StandardViewLayer {
	private static final Color ZONE_ID_COLOR = Color.red;
	private static final Color ZONE_POLYGON_COLOR = Color.green;
	private static final Color CLUSTER_POLYGON_COLOR = Color.yellow;
	private static final Color ZONE_NEIGHBOUR_COLOR = Color.blue;
	private static final Color ZONE_SURRDOUNDING_ROAD_COLOR = Color.cyan;

	private static final Stroke ZONE_POLYGON_STROKE = new BasicStroke(2.0f, 0,
			0);
	private static final Stroke ZONE_NEIGHBOUR_STROKE = new BasicStroke(1.5f,
			0, 0);
	private static final Stroke SURROUNDING_ROAD_STROKE = new BasicStroke(1.0f,
			0, 0);

	public static CsuZones CSU_ZONES = null;
	public static List<Cluster> CLUSTERS = null;// add in May 25, 2014

	private boolean showZoneIdFlag;
	private RenderZoneIdAction renderZoneIdAction;

	private boolean showZonePolygonFlag;
	private RenderZonePolygonAction renderZonePolygonAction;

	// 20140525
	private boolean showClusterPolygonFlag;
	private RenderClusterPolygonAction renderClusterPolygonAction;

	private boolean showZoneNeighbourFlag;
	private RenderZoneNeighbourAction renderZoneNeighbourAction;

	private boolean showSurroundingRoadFlag;
	private RenderSurroundingRoadAction renderSurroundingRoadAction;

	private boolean showAllEntranceRoadFlag;
	private RenderAllEntranceRoadAction renderAllEntranceRoadAction;

	private ScreenTransform transform;
	private Graphics2D gra_2D;

	// private CompositeConvexHull convexHull = null;

	public CSU_ZonePolygonLayer() {
		showZoneIdFlag = false;
		renderZoneIdAction = new RenderZoneIdAction();

		showZonePolygonFlag = false;
		renderZonePolygonAction = new RenderZonePolygonAction();

		showClusterPolygonFlag = false;
		renderClusterPolygonAction = new RenderClusterPolygonAction();

		showZoneNeighbourFlag = false;
		renderZoneNeighbourAction = new RenderZoneNeighbourAction();

		showSurroundingRoadFlag = false;
		renderSurroundingRoadAction = new RenderSurroundingRoadAction();

		showAllEntranceRoadFlag = false;
		renderAllEntranceRoadAction = new RenderAllEntranceRoadAction();
	}

	@Override
	public List<JMenuItem> getPopupMenuItems() {
		List<JMenuItem> result = new ArrayList<>();
		result.add(new JMenuItem(renderZoneIdAction));
		result.add(new JMenuItem(renderZonePolygonAction));
		result.add(new JMenuItem(renderZoneNeighbourAction));
		result.add(new JMenuItem(renderSurroundingRoadAction));
		result.add(new JMenuItem(renderAllEntranceRoadAction));
		result.add(new JMenuItem(renderClusterPolygonAction));
		return result;
	}

	@Override
	public String getName() {
		return "Zone Polygon";
	}

	@Override
	public Collection<RenderedObject> render(Graphics2D g, ScreenTransform t,
			int width, int height) {
		transform = t;
		gra_2D = g;
		Collection<RenderedObject> resultList = new ArrayList<>();

		if (CSU_ZONES != null && !CSU_ZONES.isEmpty()) {
			if (showZonePolygonFlag) {
				renderZonePolygon(CSU_ZONES, ZONE_POLYGON_COLOR, false);
			}
			if (showClusterPolygonFlag) {
				renderClusterPolygon( CLUSTER_POLYGON_COLOR, false);
			}
			if (showZoneNeighbourFlag) {
				renderZoneNeighbour(CSU_ZONES, ZONE_NEIGHBOUR_COLOR);
			}
			if (showZoneIdFlag) {
				renderZoneId(CSU_ZONES, ZONE_ID_COLOR);
			}
			if (showSurroundingRoadFlag) {
				renderSurroundingRoad(CSU_ZONES, ZONE_SURRDOUNDING_ROAD_COLOR,
						SURROUNDING_ROAD_STROKE);
			}
			if (showAllEntranceRoadFlag) {
				renderAllEntranceRoad(CSU_ZONES, ZONE_SURRDOUNDING_ROAD_COLOR);
			}
		}

		return resultList;
	}

	private void renderZonePolygon(CsuZones all_zones, Color color,
			boolean bigPolygon) {
		gra_2D.setStroke(ZONE_POLYGON_STROKE);
		gra_2D.setColor(color);
		Polygon polygon = null;
		if (!bigPolygon) {
			for (CsuZone zone : all_zones) {
				polygon = zone.getZonePolygon();
				if (polygon != null) {
					int vertexCount = polygon.npoints;
					int[] x_coorfiantes = new int[vertexCount];
					int[] y_coordinates = new int[vertexCount];
					for (int i = 0; i < vertexCount; i++) {
						x_coorfiantes[i] = transform
								.xToScreen(polygon.xpoints[i]);
						y_coordinates[i] = transform
								.yToScreen(polygon.ypoints[i]);
					}

					gra_2D.draw(new Polygon(x_coorfiantes, y_coordinates,
							vertexCount));
				}
			}
		}

		if (bigPolygon) {
			polygon = null;
			gra_2D.setStroke(SURROUNDING_ROAD_STROKE);
			for (CsuZone zone : all_zones) {
				polygon = PolygonScaler
						.scalePolygon(zone.getZonePolygon(), 1.5);
				if (polygon != null) {
					int vertexCount = polygon.npoints;
					int[] x_coorfiantes = new int[vertexCount];
					int[] y_coordinates = new int[vertexCount];
					for (int i = 0; i < vertexCount; i++) {
						x_coorfiantes[i] = transform
								.xToScreen(polygon.xpoints[i]);
						y_coordinates[i] = transform
								.yToScreen(polygon.ypoints[i]);
					}

					gra_2D.draw(new Polygon(x_coorfiantes, y_coordinates,
							vertexCount));
				}
			}

		}
	}

	private void renderClusterPolygon(Color color,
			boolean bigPolygon) {
		gra_2D.setStroke(ZONE_POLYGON_STROKE);
		gra_2D.setColor(color);
		Polygon polygon = null;
		if (!bigPolygon) {

			// 20140525
			for (Cluster next : CLUSTERS) {
				polygon = next.getClusterPolygon();
				if (polygon != null) {
					int vertexCount = polygon.npoints;
					int[] x_coorfiantes = new int[vertexCount];
					int[] y_coordinates = new int[vertexCount];
					for (int i = 0; i < vertexCount; i++) {
						x_coorfiantes[i] = transform
								.xToScreen(polygon.xpoints[i]);
						y_coordinates[i] = transform
								.yToScreen(polygon.ypoints[i]);
					}

					gra_2D.draw(new Polygon(x_coorfiantes, y_coordinates,
							vertexCount));
				}
				Point centerPoint = next.getCentroid();
				int id = CLUSTERS.indexOf(next);
				int x_coordiante = transform.xToScreen(centerPoint.x);
				int y_coordinate = transform.yToScreen(centerPoint.y);

				gra_2D.drawString("zone Id: " + id, x_coordiante - 15,
						y_coordinate + 5);
			}
			

		}
		if (bigPolygon) {
			for (Cluster next : CLUSTERS) {
				polygon = PolygonScaler.scalePolygon(next.getClusterPolygon(),
						1.5);
				if (polygon != null) {
					int vertexCount = polygon.npoints;
					int[] x_coorfiantes = new int[vertexCount];
					int[] y_coordinates = new int[vertexCount];
					for (int i = 0; i < vertexCount; i++) {
						x_coorfiantes[i] = transform
								.xToScreen(polygon.xpoints[i]);
						y_coordinates[i] = transform
								.yToScreen(polygon.ypoints[i]);
					}

					gra_2D.draw(new Polygon(x_coorfiantes, y_coordinates,
							vertexCount));
				}
			}

		}

	}

	private void renderZoneNeighbour(CsuZones all_zones, Color color) {
		gra_2D.setStroke(ZONE_NEIGHBOUR_STROKE);
		gra_2D.setColor(color);
		for (CsuZone zone : all_zones) {
			Point zoneCenter = zone.getZoneCenter();
			int x_1 = transform.xToScreen(zoneCenter.x);
			int y_1 = transform.yToScreen(zoneCenter.y);

			Circle2D circle2D = new Circle2D(x_1, y_1, 3.0, true);
			circle2D.fill(gra_2D);

			for (CsuZone neighbour : zone.getNeighbourZones()) {
				Point neighbourCenter = neighbour.getZoneCenter();
				int x_2 = transform.xToScreen(neighbourCenter.x);
				int y_2 = transform.yToScreen(neighbourCenter.y);

				gra_2D.drawLine(x_1, y_1, x_2, y_2);
			}
		}
	}

	private void renderZoneId(CsuZones all_zones, Color color) {
		gra_2D.setColor(ZONE_ID_COLOR);
		for (CsuZone next : all_zones) {
			Point center = next.getZoneCenter();
			int id = next.getZoneId();

			int x_coordiante = transform.xToScreen(center.x);
			int y_coordinate = transform.yToScreen(center.y);

			gra_2D.drawString("zone Id: " + id, x_coordiante - 15,
					y_coordinate + 5);
		}
	}

	private void renderSurroundingRoad(CsuZones all_zones, Color color,
			Stroke stroke) {
		gra_2D.setColor(color);
		gra_2D.setStroke(stroke);

		for (CsuZone next : all_zones) {
			// convexHull = new CompositeConvexHull();
			// if (next.getSurroundingRoad().size() < 3)
			// continue;
			if (SelectedObject.selectedObject instanceof Building) {
				if (!next.containtBuilding(SelectedObject.selectedObject
						.getID()))
					continue;
			}
			for (EntityID roadId : next.getSurroundingRoad()) {
				Road road = (Road) world.getEntity(roadId);

				List<Edge> edges = road.getEdges();
				if (edges.isEmpty())
					continue;
				int edgeCount = edges.size();
				int[] Xs = new int[edgeCount];
				int[] Ys = new int[edgeCount];
				int i = 0;
				for (Edge nextE : edges) {
					Xs[i] = transform.xToScreen(nextE.getStartX());
					Ys[i] = transform.yToScreen(nextE.getStartY());
					i++;
				}
				Polygon shape = new Polygon(Xs, Ys, edgeCount);
				gra_2D.fill(shape);

				// Point point = new Point(transform.xToScreen(road.getX()),
				// transform.yToScreen(road.getY()));
				// convexHull.addPoint(point);
			}
			// try {
			// if (convexHull.getConvexPolygon() == null)
			// continue;
			//
			// gra_2D.drawPolygon(convexHull.getConvexPolygon());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

		}
	}

	private void renderAllEntranceRoad(CsuZones all_zones, Color color) {
		gra_2D.setColor(color);
		for (CsuZone next : all_zones) {
			for (Road entrance : next.getAllEntranceRoad()) {
				List<Edge> edges = entrance.getEdges();
				if (edges.isEmpty())
					continue;
				int edgeCount = edges.size();
				int[] Xs = new int[edgeCount];
				int[] Ys = new int[edgeCount];
				int i = 0;
				for (Edge nextE : edges) {
					Xs[i] = transform.xToScreen(nextE.getStartX());
					Ys[i] = transform.yToScreen(nextE.getStartY());
					i++;
				}
				Polygon shape = new Polygon(Xs, Ys, edgeCount);
				gra_2D.fill(shape);
			}
		}
	}

	/*
	 * --------------------------------------------------------------------------
	 * ------------------------------
	 */

	@SuppressWarnings("serial")
	private class RenderZoneIdAction extends AbstractAction {
		public RenderZoneIdAction() {
			super("Zone Id");
			putValue(AbstractAction.SELECTED_KEY, showZoneIdFlag);
			putValue(AbstractAction.SMALL_ICON, showZoneIdFlag ? Icons.TICK
					: Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showZoneIdFlag = !showZoneIdFlag;
			putValue(AbstractAction.SELECTED_KEY, showZoneIdFlag);
			putValue(AbstractAction.SMALL_ICON, showZoneIdFlag ? Icons.TICK
					: Icons.CROSS);
			component.repaint();
		}
	}

	@SuppressWarnings("serial")
	private class RenderZonePolygonAction extends AbstractAction {
		public RenderZonePolygonAction() {
			super("Zone Polygon");
			putValue(AbstractAction.SELECTED_KEY, showZonePolygonFlag);
			putValue(AbstractAction.SMALL_ICON,
					showZonePolygonFlag ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showZonePolygonFlag = !showZonePolygonFlag;
			putValue(AbstractAction.SELECTED_KEY, showZonePolygonFlag);
			putValue(AbstractAction.SMALL_ICON,
					showZonePolygonFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}

	@SuppressWarnings("serial")
	private class RenderClusterPolygonAction extends AbstractAction {
		public RenderClusterPolygonAction() {
			super("Cluster Polygon");
			putValue(AbstractAction.SELECTED_KEY, showClusterPolygonFlag);
			putValue(AbstractAction.SMALL_ICON,
					showClusterPolygonFlag ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showClusterPolygonFlag = !showClusterPolygonFlag;
			putValue(AbstractAction.SELECTED_KEY, showClusterPolygonFlag);
			putValue(AbstractAction.SMALL_ICON,
					showClusterPolygonFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}

	@SuppressWarnings("serial")
	private class RenderZoneNeighbourAction extends AbstractAction {
		public RenderZoneNeighbourAction() {
			super("Zone Neighbour");
			putValue(AbstractAction.SELECTED_KEY, showZoneNeighbourFlag);
			putValue(AbstractAction.SMALL_ICON,
					showZoneNeighbourFlag ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showZoneNeighbourFlag = !showZoneNeighbourFlag;
			putValue(AbstractAction.SELECTED_KEY, showZoneNeighbourFlag);
			putValue(AbstractAction.SMALL_ICON,
					showZoneNeighbourFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}

	@SuppressWarnings("serial")
	private class RenderSurroundingRoadAction extends AbstractAction {
		public RenderSurroundingRoadAction() {
			super("Surrounding Road");
			putValue(AbstractAction.SELECTED_KEY, showSurroundingRoadFlag);
			putValue(AbstractAction.SMALL_ICON,
					showSurroundingRoadFlag ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showSurroundingRoadFlag = !showSurroundingRoadFlag;
			putValue(AbstractAction.SELECTED_KEY, showSurroundingRoadFlag);
			putValue(AbstractAction.SMALL_ICON,
					showSurroundingRoadFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}

	@SuppressWarnings("serial")
	private class RenderAllEntranceRoadAction extends AbstractAction {
		public RenderAllEntranceRoadAction() {
			super("Entrance Road");
			putValue(AbstractAction.SELECTED_KEY, showAllEntranceRoadFlag);
			putValue(AbstractAction.SMALL_ICON,
					showAllEntranceRoadFlag ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showAllEntranceRoadFlag = !showAllEntranceRoadFlag;
			putValue(AbstractAction.SELECTED_KEY, showAllEntranceRoadFlag);
			putValue(AbstractAction.SMALL_ICON,
					showAllEntranceRoadFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
}
