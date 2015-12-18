package csu.Viewer.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import javolution.util.FastMap;

import csu.Viewer.SelectedObject;
import csu.model.object.CSUBuilding;

import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.DrawingTools;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.view.Icons;
import rescuecore2.worldmodel.EntityID;

public class CSU_BuildingLayer extends CSU_AreaLayer<Building>{
	private static final Color HEATING = new Color(176, 176, 56, 128);
	private static final Color BURNING = new Color(204, 122, 50, 128);
	private static final Color INFERNO = new Color(160, 52, 52, 128);
	private static final Color WATER_DAMAGE = new Color(50, 120, 130, 128);
	private static final Color MINOR_DAMAGE = new Color(100, 140, 210, 128);
	private static final Color MODERATE_DAMAGE = new Color(100, 70, 190, 128);
	private static final Color SEVERE_DAMAGE = new Color(80, 60, 140, 128);
	private static final Color BURNT_OUT = new Color(0, 0, 0, 255);
	
	private static final Color OUTLINE_COLOUR = Color.GRAY.darker();
	private static final Color ENTRANCE = new Color(120, 120, 120);
	
	private static final Stroke WALL_STROKE = new BasicStroke(2, 0, 0);		// BasicStroke.CAP_BUTT = 0
	private static final Stroke ENTRANCE_STROKE = new BasicStroke(1, 0, 0);	// BasicStroke.JOIN_MITER = 0
	
	private static final Color CONNECTE_BUILDINGS_ARROW_COLOR = new Color(42, 0, 255);
	private static final Stroke CONNECT_BUILDINGS_ARROW_STROKE = new BasicStroke(2.0f);
	private static final Stroke BASIC_STROKE = new BasicStroke(1.0f);
	
	public static Map<EntityID, CSUBuilding> CSU_BUILDING_MAP = 
			Collections.synchronizedMap(new FastMap<EntityID, CSUBuilding>());
	
	private boolean showBuildingIdsFlag;
	private Action showIdsAction;
	
	private boolean showBuildingValueFlag;
	private Action buildingValueAction;
	
	private boolean showConnectedBuildingFlag;
	private Action connectedBuildingsAction;
	
	/**
	 * Construct a building layer.
	 */
	public CSU_BuildingLayer() {
		super(Building.class);
	}
	
	@Override
	public String getName() {
		return "Building shapes";
	}
	
	@Override
	public void initialise(Config config) {
		showBuildingIdsFlag = false;
		showIdsAction = new RenderIdsAction();
		
		showConnectedBuildingFlag = true;
		connectedBuildingsAction = new RenderConnectedBuildingAction();
		
		showBuildingValueFlag = false;
		buildingValueAction = new RenderBuildingValueAction();
	}
	
	@Override
    public java.util.List<JMenuItem> getPopupMenuItems() {
        java.util.List<JMenuItem> result = new ArrayList<JMenuItem>();
        
        result.add(new JMenuItem(showIdsAction));
        result.add(new JMenuItem(connectedBuildingsAction));
        result.add(new JMenuItem(buildingValueAction));

        return result;
    }
	
	@Override
	public Shape render(Building building, Graphics2D g, ScreenTransform t) {
		Shape shape = super.render(building, g, t);
		if (showBuildingIdsFlag)
			drawInfo(building, g, String.valueOf(building.getID().getValue()), building.getClass());
		
		boolean flag = SelectedObject.selectedObject != null;
		if (flag && SelectedObject.selectedObject.getID().equals(building.getID())) {
			if (showConnectedBuildingFlag)
				renderConnectedBuildings(building, g, t);
		}
		
		if (showBuildingValueFlag && SelectedObject.renderBuildingValueKey) {
			renderBuildingValue(g, t);
		}
		
		return shape;
	}
	
	@Override
	protected void paintShape(Building area, Polygon p, Graphics2D g) {
		drawBrokenness(area, p, g);
		drawFieryness(area, p, g);
	}
	
	@Override
	protected void paintEdge(Edge edge, Graphics2D g, ScreenTransform t) {
		g.setColor(edge.isPassable() ? ENTRANCE : OUTLINE_COLOUR);
		g.setStroke(edge.isPassable() ? ENTRANCE_STROKE : WALL_STROKE);
		int x1 = t.xToScreen(edge.getStartX()), y1 = t.yToScreen(edge.getStartY());
		int x2 = t.xToScreen(edge.getEndX()), y2 = t.yToScreen(edge.getEndY());
		g.drawLine(x1, y1, x2, y2);
	}
	
	private void drawBrokenness(Building b, Shape shape, Graphics2D g) {
		int brokenness = b.getBrokenness();
		int colour = Math.max(0, 135 - brokenness / 2);
		g.setColor(new Color(colour, colour, colour));
		g.fill(shape);
	}
	
	private void drawFieryness(Building b, Polygon shape, Graphics2D g) {
		if (b == SelectedObject.selectedObject) {
			g.setColor(Color.MAGENTA);
			g.fill(shape);
			return;
		}
		if (!b.isFierynessDefined())
			return;
		switch (b.getFierynessEnum()) {
		case UNBURNT:
			return;
		case HEATING:
			g.setColor(HEATING);
			break;
		case BURNING:
			g.setColor(BURNING);
			break;
		case INFERNO:
			g.setColor(INFERNO);
			break;
		case WATER_DAMAGE:
			g.setColor(WATER_DAMAGE);
			break;
		case MINOR_DAMAGE:
			g.setColor(MINOR_DAMAGE);
			break;
		case MODERATE_DAMAGE:
			g.setColor(MODERATE_DAMAGE);
			break;
		case SEVERE_DAMAGE:
			g.setColor(SEVERE_DAMAGE);
			break;
		case BURNT_OUT:
			g.setColor(BURNT_OUT);
			break;
		default:
			throw new IllegalArgumentException(
					"Don't know how to render fieryness " + b.getFierynessEnum());
		}
		g.fill(shape);
	}
	
	private void renderConnectedBuildings(Building building, Graphics2D g, ScreenTransform t) {
		if (!CSU_BUILDING_MAP.isEmpty() && CSU_BUILDING_MAP.get(building.getID()) != null) {
			g.setColor(CONNECTE_BUILDINGS_ARROW_COLOR);
			g.setStroke(CONNECT_BUILDINGS_ARROW_STROKE);
			
			CSUBuilding csuBuilding = 
					CSU_BUILDING_MAP.get(building.getID());
			List<CSUBuilding> connectedBuildings = csuBuilding.getConnectedBuildings();
			
			int startX = t.xToScreen(csuBuilding.getSelfBuilding().getX());
			int startY = t.yToScreen(csuBuilding.getSelfBuilding().getY());
			for (CSUBuilding next : connectedBuildings) {
				int endX = t.xToScreen(next.getSelfBuilding().getX());
				int endY = t.yToScreen(next.getSelfBuilding().getY());
				
				g.drawLine(startX, startY, endX, endY);
				DrawingTools.drawArrowHeads(startX, startY, endX, endY, g);
			}
		}
	}
	
	private void renderBuildingValue(Graphics2D g, ScreenTransform t) {
		if (!CSU_BUILDING_MAP.isEmpty()) {
			g.setColor(Color.red);
			g.setStroke(BASIC_STROKE);
			double value;
			
			for (CSUBuilding next : CSU_BUILDING_MAP.values()) {
				Pair<Integer, Integer> location = getLocation(next.getSelfBuilding());
				int x = transform.xToScreen(location.first());
				int y = transform.yToScreen(location.second());
		
				value = next.BUILDING_VALUE;
				if (value == Double.MIN_VALUE)///
					value = -1;
				if(value != -1)
					g.drawString((long)value + "", x - 10, y + 5);
			}
		}
	}

/* ------------------------------------------------------------------------------------------------------- */
	
	@SuppressWarnings("serial")
	private final class RenderIdsAction extends AbstractAction {
		public RenderIdsAction() {
			super("Show Ids");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showBuildingIdsFlag));
			putValue(Action.SMALL_ICON, showBuildingIdsFlag ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showBuildingIdsFlag = ! showBuildingIdsFlag;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showBuildingIdsFlag));
			putValue(Action.SMALL_ICON, showBuildingIdsFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class RenderConnectedBuildingAction extends AbstractAction {
		public RenderConnectedBuildingAction() {
			super("Connected Buildings");
			putValue(AbstractAction.SELECTED_KEY, Boolean.valueOf(showConnectedBuildingFlag));
			putValue(AbstractAction.SMALL_ICON, showConnectedBuildingFlag ? Icons.TICK : Icons.CROSS);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showConnectedBuildingFlag = ! showConnectedBuildingFlag;
			putValue(AbstractAction.SELECTED_KEY, Boolean.valueOf(showConnectedBuildingFlag));
			putValue(AbstractAction.SMALL_ICON, showConnectedBuildingFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	private final class RenderBuildingValueAction extends AbstractAction {
		public RenderBuildingValueAction() {
			super("building value");
			putValue(AbstractAction.SELECTED_KEY, Boolean.valueOf(showBuildingValueFlag));
			putValue(AbstractAction.SMALL_ICON, showBuildingValueFlag ? Icons.TICK : Icons.CROSS);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showBuildingValueFlag = !showBuildingValueFlag;
			putValue(AbstractAction.SELECTED_KEY, Boolean.valueOf(showBuildingValueFlag));
			putValue(AbstractAction.SMALL_ICON, showBuildingValueFlag ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}
}