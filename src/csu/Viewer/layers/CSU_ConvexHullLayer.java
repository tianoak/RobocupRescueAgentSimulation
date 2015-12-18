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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import math.geom2d.conic.Circle2D;

import csu.Viewer.SelectedObject;
import csu.common.clustering.FireCluster;
import csu.common.clustering.FireCluster.FireCondition;
import csu.geom.ConvexObject;
import javolution.util.FastMap;
import javolution.util.FastSet;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.Icons;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;

/**
 * A layer for convex hull.
 * 
 * @author Appreciation - csu
 */
public class CSU_ConvexHullLayer extends StandardViewLayer{
	private static final Color CONVEX_COLOR = Color.CYAN;										
	private static final Color CONVEX_DYING_COLOR = Color.BLACK;								
	private static final Color CONVEX_EXPANDABLE = Color.GREEN;							
	
	private static final Color BORDER_DIRECTION_ENTITY_COLOR = Color.orange;					
	private static final Color BORDER_ENTITY_COLOR = Color.white;								
	private static final Color IGNORE_BORDER_ENTITY_COLOR = Color.cyan;					
	
	private static final Color MAP_BORDER_COLOR = Color.cyan;									
	private static final Color SMALL_BORDER_COLOR = Color.magenta;							
	private static final Color BIG_BORDER_COLOR = Color.yellow;								
	
	private static final Color TARG_COLOR = Color.pink;										

	private static final Stroke STROKE = new BasicStroke(3.0f, 0, 0);			  // BasicStroke.JOIN_MITER = 0	
	
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * combox. And the map's values is a list of {@link FireCondition} related
	 * to its {@link FireCluster}.
	 */
    public static final Map<EntityID, List<Pair<Point2D, String>>> FIRE_CLUSTER_CONDITIONS = 
    		Collections.synchronizedMap(new FastMap<EntityID, List<Pair<Point2D, String>>>());
	
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * combox. And the map's values is a list of its <Strong>ignored border
	 * buildings</strong>.
	 * <p>
	 * <strong>Ignored border buildings</strong> are those buildings located in
	 * the intersection area of two <code>FireCluster</code>.
	 */
	public static Map<EntityID, Set<StandardEntity>> IGNORE_BORDER_BUILDING = 
			Collections.synchronizedMap(new FastMap<EntityID, Set<StandardEntity>>());
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * combox. And the map's value is a list of its all <code>FireCluster</code>'s
	 * border buildings.
	 * <p>
	 * The border buildings of a <code>FireCluster</code> are those located in
	 * the area between {@link #BIG_BORDER_HULLS BIG_BORDER_HULLS} and
	 * {@link #SMALL_BORDER_HULLS SMALL_BORDER_HULLS}.
	 */
	public static Map<EntityID, Set<StandardEntity>> BORDER_BUILDINGS = 
			Collections.synchronizedMap(new FastMap<EntityID, Set<StandardEntity>>());
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * combox. And the map's value is a list of <strong>indirection border buildings</strong> 
	 * of this Agent.
	 * <p>
	 * <strong>Indirection border buildings</strong> are border buildings of all FireCLusters
	 * which located in the main spread direction of fires.
	 */
	public static Map<EntityID, List<StandardEntity>> BORDER_DIRECTION_BUILDINGS = 
			Collections.synchronizedMap(new FastMap<EntityID, List<StandardEntity>>());
	
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in
	 * combox. And the map's value is a list of {@link FireCluster} this Agent
	 * has known.
	 */
    public static final Map<EntityID, List<FireCluster>> CONVEX_HULLS_MAP = 
    		Collections.synchronizedMap(new FastMap<EntityID, List<FireCluster>>());
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in
	 * combox. And the map's value is a list of {@link Polygon} which is the
	 * enlarged representation of its {@link FireCLuster}'s polygon.
	 */
    public static Map<EntityID, List<Polygon>> BIG_BORDER_HULLS = 
    		Collections.synchronizedMap(new FastMap<EntityID, List<Polygon>>());
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in
	 * combox. And the map's value is a list of {@link Polygon} which is the
	 * norrowing of its {@link FireCLuster}'s polygon.
	 */
    public static Map<EntityID, List<Polygon>> SMALL_BORDER_HULLS = 
    		Collections.synchronizedMap(new FastMap<EntityID, List<Polygon>>());
      
	/**
	 * The border buildings of this map. All border buildings are located in the area
	 * between {@link #BIG_MAP_BORDER_CONVEX_HULL BIG_MAP_BORDER_CONVEX_HULL} and
	 * {@link #SMALL_MAP_BORDER_CONVEX_HULL SMALL_MAP_BORDER_CONVEX_HULL}.
	 */
	public static Set<EntityID> MAP_BORDER_BUILDINGS = 
			Collections.synchronizedSet(new FastSet<EntityID>());
	/**
	 * An enlarged polygon of this map's initial polygon. And it is same for all Agents.
	 */
	public static Polygon BIG_MAP_BORDER_CONVEX_HULL;
	/**
	 * An norrowed polygon of this map's initial polygon. And it is same for all Agents.
	 */
    public static Polygon SMALL_MAP_BORDER_CONVEX_HULL;
	
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * combox. And the map's value is a list of target buildings this Agent is
	 * going to extinguish.
	 */
	public static Map<EntityID, List<Building>> TARGET = 
			Collections.synchronizedMap(new FastMap<EntityID, List<Building>>());
	/**
	 * The map's key is the Id of an Agent(always FB) which is selected in the
	 * comox. And the map's value is a set place this Agent can stand to
	 * extinguish its target building.
	 */
    public static Map<EntityID, Set<StandardEntity>> BEST_PLACE_TO_STAND = 
    		Collections.synchronizedMap(new FastMap<EntityID, Set<StandardEntity>>());
    
    public static Map<EntityID, List<Pair<Point, ConvexObject>>> TRIANGLE_CENTER_POINT = 
    		Collections.synchronizedMap(new FastMap<EntityID, List<Pair<Point, ConvexObject>>>());
    
    /**
     * The maximum extinguish range of fire brigade.
     */
    public static int MAX_EXTINGUISH_RANGE;
    
    /**
     * Flags to determines whether to show FireCluster condition or not.
     */
    private boolean fireClusterCondition;
    FireclusterConditionAction fireClusterConditionAction;
    
    /**
     * Flags to determines whether to show the ignored border buildings or not.
     */
    private boolean ignoredBorderEntities;
    IgnoredBorderEntitiesAction ignoredBorderEntitiesAction;
    
    /**
     * Flags to determines whether to show the border buildings of FireCluster or not.
     */
    private boolean borderEntities;
    RenderBorderEntitiesAction renderBorderEntitiesAction;
    
    /**
     * Flags to determines whether to show the indirection border buildings of FireCLuster or not.
     */
    private boolean borderDirectionBuilding;
    BorderDirectionBuildingAction borderDirectionBuildingAction;
    
    /**
     * Flags to determines whether to show FireCluster's border hull or not.
     */
    private boolean borderHulls;
    RenderBorderHullsAction renderBorderHullsAction;
    
    /**
     * Flags to determines whether to show map border buildings or not.
     */
    private boolean mapBorderBuildings;
    MapBorderBuildingAction mapBorderBuildingAction;
    
    /**
     * Flags to determines whether to show Agent's target buildings or not.
     */
    private boolean showTarget;
    showTargetAction showTargetAction;
    
    /**
     * Flags to determines whether to show FireCluster's direction triangle or not.
     */
    private boolean directionTriangle;
    RenderDirectionTriangleAction directionTriangleAction;
    
    /**
     * A ScreenTransform translate world coordinates into screen coordinates. 
     */
    private ScreenTransform transform;
    /**
     * The graphics this layer will paint on.
     */
    private Graphics2D gra_2D;
    
    // constructor
    public CSU_ConvexHullLayer() {
    	fireClusterCondition = false;
    	fireClusterConditionAction = new FireclusterConditionAction();
    	ignoredBorderEntities = false;
    	ignoredBorderEntitiesAction = new IgnoredBorderEntitiesAction();
    	borderEntities = false;
    	renderBorderEntitiesAction = new RenderBorderEntitiesAction();
    	borderDirectionBuilding = false;
    	borderDirectionBuildingAction = new BorderDirectionBuildingAction();
    	borderHulls = false;
    	renderBorderHullsAction = new RenderBorderHullsAction();
    	mapBorderBuildings = false;
    	mapBorderBuildingAction = new MapBorderBuildingAction();
    	showTarget = false;
    	showTargetAction = new showTargetAction();
    	directionTriangle = false;
    	directionTriangleAction = new RenderDirectionTriangleAction();
    }
    
    @Override
	public String getName() {
		return "Convex Hull";
	}
    
    @Override
    public List<JMenuItem> getPopupMenuItems() {
    	List<JMenuItem> result = new ArrayList<>();
    	result.add(new JMenuItem(fireClusterConditionAction));
    	result.add(new JMenuItem(ignoredBorderEntitiesAction));
    	result.add(new JMenuItem(renderBorderEntitiesAction));
    	result.add(new JMenuItem(borderDirectionBuildingAction));
    	result.add(new JMenuItem(renderBorderHullsAction));
    	result.add(new JMenuItem(mapBorderBuildingAction));
    	result.add(new JMenuItem(showTargetAction));
    	result.add(new JMenuItem(directionTriangleAction));
    	return result;
    }
    
    @Override
	public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
		this.gra_2D = g;
		this.transform = transform;
		Collection<RenderedObject> resultList = new ArrayList<>();
		
		List<Pair<Point2D, String>> fireClusterConditions = new ArrayList<>();
		List<FireCluster> fireClusters = new ArrayList<>();
		List<Polygon> smallPolygons = new ArrayList<>();
		List<Polygon> bigPolygons = new ArrayList<>();
		List<StandardEntity> ignoredBorderBuildings = new ArrayList<>();
		List<StandardEntity> borderBuildings = new ArrayList<>();
		List<StandardEntity> borderDirectBuildings = new ArrayList<>();
		Set<EntityID> mapBorderBuildings = new FastSet<>();
		List<Building> targets = new ArrayList<>();
		Set<StandardEntity> standPlace = new FastSet<>();
		List<Pair<Point, ConvexObject>> triangleCenterPoints = new ArrayList<>();
		
		EntityID selectedAgent = SelectedObject.selectedAgent;
		if (FIRE_CLUSTER_CONDITIONS.get(selectedAgent) != null) {
			fireClusterConditions = Collections.synchronizedList(FIRE_CLUSTER_CONDITIONS.get(selectedAgent));
		} 
		if (IGNORE_BORDER_BUILDING.get(selectedAgent) != null) {
			List<StandardEntity> list = new ArrayList<>(IGNORE_BORDER_BUILDING.get(selectedAgent));
			ignoredBorderBuildings = Collections.synchronizedList(list);
		} 
		if (BORDER_BUILDINGS.get(selectedAgent) != null) {
			List<StandardEntity> list = new ArrayList<>(BORDER_BUILDINGS.get(selectedAgent));
			borderBuildings = Collections.synchronizedList(list);
		} 
		if (BORDER_DIRECTION_BUILDINGS.get(selectedAgent) != null) {
			borderDirectBuildings = Collections.synchronizedList(BORDER_DIRECTION_BUILDINGS.get(selectedAgent));
		} 
		if (CONVEX_HULLS_MAP.get(selectedAgent) != null) {
			fireClusters = Collections.synchronizedList(CONVEX_HULLS_MAP.get(selectedAgent));
		} 
		if (SMALL_BORDER_HULLS.get(selectedAgent) != null) {
			smallPolygons = Collections.synchronizedList(SMALL_BORDER_HULLS.get(selectedAgent));
		} 
		if (BIG_BORDER_HULLS.get(selectedAgent) != null) {
			bigPolygons = Collections.synchronizedList(BIG_BORDER_HULLS.get(selectedAgent));
		} 
		if (! MAP_BORDER_BUILDINGS.isEmpty()) {
			mapBorderBuildings.addAll(MAP_BORDER_BUILDINGS);
		} 
		if (TARGET.get(selectedAgent) != null) {
			targets = Collections.synchronizedList(TARGET.get(selectedAgent));
		} 
		if (BEST_PLACE_TO_STAND.get(selectedAgent) != null) {
			standPlace = Collections.synchronizedSet(BEST_PLACE_TO_STAND.get(selectedAgent));
		} 
		if (TRIANGLE_CENTER_POINT.get(selectedAgent) != null) {
			triangleCenterPoints = Collections.synchronizedList(TRIANGLE_CENTER_POINT.get(selectedAgent));
		}
		
		if (fireClusterCondition) {
			renderFireClusterCondition(fireClusterConditions);
		} 
		if (ignoredBorderEntities) {
			renderBuildings(ignoredBorderBuildings, IGNORE_BORDER_ENTITY_COLOR);
		} 
		if (borderEntities) {
			renderBuildings(borderBuildings, BORDER_ENTITY_COLOR);
		} 
		if (borderDirectionBuilding) {
			renderBuildings(borderDirectBuildings, BORDER_DIRECTION_ENTITY_COLOR);
		} 
		if (borderHulls) {
			renderBorderHulls(smallPolygons, bigPolygons);
		} 
		if (this.mapBorderBuildings) {
			renderMapBorderBuildings(mapBorderBuildings);
		} 
		if (showTarget) {
			renderTarget(targets, standPlace);
		} 
		if (directionTriangle) {
			for (Pair<Point, ConvexObject> next : triangleCenterPoints) {
				renderDirectionTriangle(next.first(), next.second());
			}
		}
		
		if (fireClusters != null && !fireClusters.isEmpty()) {
			for (FireCluster next : fireClusters) {
				Polygon convexPolygon = next.getConvexObject().getConvexHullPolygon();
				int vertexCount = convexPolygon.npoints;
				int[] x_coordinates = new int[vertexCount];
				int[] y_coordinates = new int[vertexCount];
				for (int i = 0; i < vertexCount; i++) {
					x_coordinates[i] = transform.xToScreen(convexPolygon.xpoints[i]);
					y_coordinates[i] = transform.yToScreen(convexPolygon.ypoints[i]);
				}
				gra_2D.setColor(CONVEX_COLOR);
				gra_2D.setStroke(STROKE);
				if (next.isDying()) 
					gra_2D.setColor(CONVEX_DYING_COLOR);
				else if(next.isExpandableToMapCenter())
					gra_2D.setColor(CONVEX_EXPANDABLE);
				gra_2D.draw(new Polygon(x_coordinates, y_coordinates, vertexCount));
			}
		}
    	
    	return resultList;
	}
    
    private void renderFireClusterCondition(List<Pair<Point2D, String>> conditions) {
    	gra_2D.setColor(Color.WHITE);
    	int x_coordiante, y_coordinate;
    	for (Pair<Point2D, String> next : conditions) {
    		x_coordiante = transform.xToScreen(next.first().getX());
    		y_coordinate = transform.yToScreen(next.first().getY());
    		gra_2D.drawString(next.second(), x_coordiante, y_coordinate);
    	}
    }
    
    private void renderBuildings(List<StandardEntity> renderedBuildings, Color color) {
    	gra_2D.setColor(color);
    	gra_2D.setStroke(STROKE);
    	for (StandardEntity next : renderedBuildings) {
    		Building building = (Building) next;
    		int vertexCount = building.getApexList().length / 2;
    		int[] x_coordinates = new int[vertexCount];
    		int[] y_coordinates = new int[vertexCount];
    		for (int i = 0, j = 0; i < vertexCount; i++, j += 2) {
				x_coordinates[i] = transform.xToScreen(building.getApexList()[j]);
				y_coordinates[i] = transform.yToScreen(building.getApexList()[j + 1]);
			}
    		Polygon polygon = new Polygon(x_coordinates, y_coordinates, vertexCount);
    		gra_2D.fill(polygon);
    	}
    }
    
	private void renderBorderHulls(List<Polygon> smallBorderHulls, List<Polygon> bigBorderHulls) {
		gra_2D.setStroke(STROKE);
		gra_2D.setColor(BIG_BORDER_COLOR);
		for (Polygon next : bigBorderHulls) {
			int vertexCount = next.npoints;
			int[] x_coordinates = new int[vertexCount];
			int[] y_coordinates = new int[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				x_coordinates[i] = transform.xToScreen(next.xpoints[i]);
				y_coordinates[i] = transform.yToScreen(next.ypoints[i]);
			}
			Polygon polygon = new Polygon(x_coordinates, y_coordinates, vertexCount);
			gra_2D.draw(polygon);
		}
		
		gra_2D.setColor(SMALL_BORDER_COLOR);
		for (Polygon next : smallBorderHulls) {
			int vertexCount = next.npoints;
			int[] x_coordinates = new int[vertexCount];
			int[] y_coordinates = new int[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				x_coordinates[i] = transform.xToScreen(next.xpoints[i]);
				y_coordinates[i] = transform.yToScreen(next.ypoints[i]);
			}
			Polygon polygon = new Polygon(x_coordinates, y_coordinates, vertexCount);
			gra_2D.draw(polygon);
		}
	}
	
	private void renderMapBorderBuildings(Set<EntityID> mapBorderBuildings) {
		gra_2D.setColor(MAP_BORDER_COLOR);
		gra_2D.setStroke(new BasicStroke(1.5f));
		
		int smallBorderVertexCount = SMALL_MAP_BORDER_CONVEX_HULL.npoints;
		int[] x_s_coordinates = new int[smallBorderVertexCount];
		int[] y_s_coordinates = new int[smallBorderVertexCount];
		for (int i = 0; i < smallBorderVertexCount; i++) {
			x_s_coordinates[i] = transform.xToScreen(SMALL_MAP_BORDER_CONVEX_HULL.xpoints[i]);
			y_s_coordinates[i] = transform.yToScreen(SMALL_MAP_BORDER_CONVEX_HULL.ypoints[i]);
		}
		gra_2D.draw(new Polygon(x_s_coordinates, y_s_coordinates, smallBorderVertexCount));
		
		int bigBorderVertexCount = BIG_MAP_BORDER_CONVEX_HULL.npoints;
		int[] x_b_coordinates = new int[bigBorderVertexCount];
		int[] y_b_coordinates = new int[bigBorderVertexCount];
		for (int i = 0; i < bigBorderVertexCount; i++) {
			x_b_coordinates[i] = transform.xToScreen(BIG_MAP_BORDER_CONVEX_HULL.xpoints[i]);
			y_b_coordinates[i] = transform.yToScreen(BIG_MAP_BORDER_CONVEX_HULL.ypoints[i]);
		}
		gra_2D.draw(new Polygon(x_b_coordinates, y_b_coordinates, bigBorderVertexCount));
		
		for (EntityID next : mapBorderBuildings) {
			Building building = (Building)world.getEntity(next);
			int vertexCount = building.getApexList().length / 2;
    		int[] x_coordinates = new int[vertexCount];
    		int[] y_coordinates = new int[vertexCount];
    		for (int i = 0, j = 0; i < vertexCount; i++, j += 2) {
				x_coordinates[i] = transform.xToScreen(building.getApexList()[j]);
				y_coordinates[i] = transform.yToScreen(building.getApexList()[j + 1]);
			}
    		Polygon polygon = new Polygon(x_coordinates, y_coordinates, vertexCount);
    		gra_2D.fill(polygon);
		}
	}
	
	private void renderTarget(List<Building> targetList, Set<StandardEntity> standPosition) {
		gra_2D.setStroke(STROKE);
		gra_2D.setColor(TARG_COLOR);
		if (!targetList.isEmpty()) {
			Building target = targetList.get(targetList.size() - 1);
			int vertexCount = target.getApexList().length / 2;
    		int[] x_coordinates = new int[vertexCount];
    		int[] y_coordinates = new int[vertexCount];
    		for (int i = 0, j = 0; i < vertexCount; i++, j += 2) {
				x_coordinates[i] = transform.xToScreen(target.getApexList()[j]);
				y_coordinates[i] = transform.yToScreen(target.getApexList()[j + 1]);
			}
    		Polygon polygon = new Polygon(x_coordinates, y_coordinates, vertexCount);
    		gra_2D.fill(polygon);
    		
    		int x = target.getX(), y = target.getY();
    		int radius = transform.xToScreen(x + MAX_EXTINGUISH_RANGE) - transform.xToScreen(x);
    		Circle2D circle = new Circle2D(transform.xToScreen(x), transform.yToScreen(y), radius, true);
    		circle.fill(gra_2D);
		}
	}
	
	private void renderDirectionTriangle(Point centerPoint, ConvexObject convexObject) {
		if (convexObject.FIRST_POINT != null) {
//			Set<Line2D> lines = Collections.synchronizedSet(convexObject.CONVEX_INTERSECT_LINES == null ? 
//					new FastSet<Line2D>() : convexObject.CONVEX_INTERSECT_LINES);
//			gra_2D.setColor(Color.RED);
//			gra_2D.setStroke(new BasicStroke(9.0f));
//			for (Line2D next : lines) {
//				int start_x = transform.xToScreen(next.getX1());
//				int start_y = transform.yToScreen(next.getY1());
//				int end_x = transform.xToScreen(next.getX2());
//				int end_y = transform.yToScreen(next.getY2());
//				gra_2D.drawLine(start_x, start_y, end_x, end_y);
//			}
			
			gra_2D.setColor(Color.YELLOW);
			gra_2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			int x_center = transform.xToScreen(centerPoint.x);
			int y_center = transform.yToScreen(centerPoint.y);
			Circle2D circle = new Circle2D(x_center, y_center, 10, true);
			circle.draw(gra_2D);
			int x_convex = transform.xToScreen(convexObject.CONVEX_POINT.x);
			int y_convex = transform.yToScreen(convexObject.CONVEX_POINT.y);
			circle = new Circle2D(x_convex, y_convex, 10, true);
			circle.draw(gra_2D);
			gra_2D.drawLine(x_center, y_center, x_convex, y_convex);
			
			gra_2D.setColor(Color.GREEN);
			gra_2D.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			int x_first = transform.xToScreen(convexObject.FIRST_POINT.x);
			int y_first = transform.yToScreen(convexObject.FIRST_POINT.y);
			int x_second = transform.xToScreen(convexObject.SECOND_POINT.x);
			int y_second = transform.yToScreen(convexObject.SECOND_POINT.y);
			gra_2D.drawLine(x_first, y_first, x_second, y_second);
			
//			if (convexObject.CONVEX_INTERSECT_POINTS != null) {
//				for (Point2D next : convexObject.CONVEX_INTERSECT_POINTS) {
//					int x = transform.xToScreen(next.getX()), y = transform.yToScreen(next.getY());
//					circle = new Circle2D(x, y, 10, true);
//					circle.draw(gra_2D);
//				}
//			}
			
			// BasicStroke.CAP_BUTT = 0        BasicStroke.JOIN_BEVEL = 2
			gra_2D.setStroke(new BasicStroke(1.8f, 0, 2, 0, new float[]{9}, 0));
			int x_other_1 = transform.xToScreen(convexObject.OTHER_POINT_1.x);
			int y_other_1 = transform.yToScreen(convexObject.OTHER_POINT_1.y);
			int x_other_2 = transform.xToScreen(convexObject.OTHER_POINT_2.x);
			int y_other_2 = transform.yToScreen(convexObject.OTHER_POINT_2.y);
			gra_2D.drawLine(x_first, y_first, x_convex, y_convex);
			gra_2D.drawLine(x_second, y_second, y_convex, y_convex);
			gra_2D.drawLine(x_first, y_first, x_other_1, y_other_1);
			gra_2D.drawLine(x_second, y_second, x_other_2, y_other_2);
			gra_2D.drawLine(x_other_1, y_other_1, x_other_2, y_other_2);
		}
//		if (convexObject.DIRECTION_POLYGON != null) {
//			int directionVertexCount = convexObject.DIRECTION_POLYGON.npoints;
//			int[] direction_x = new int[directionVertexCount];
//			int[] direction_y = new int[directionVertexCount];
//			for (int i = 0; i < directionVertexCount; i++) {
//				direction_x[i] = convexObject.DIRECTION_POLYGON.xpoints[i];
//				direction_y[i] = convexObject.DIRECTION_POLYGON.ypoints[i];
//			}
//			gra_2D.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//			gra_2D.setColor(Color.BLACK);
//			gra_2D.fill(new Polygon(direction_x, direction_y, directionVertexCount));
//		}
	}
    
    
/* -------------------------------------------- Action cllass --------------------------------------------- */
    
    @SuppressWarnings("serial")
	private final class FireclusterConditionAction extends AbstractAction {
    	public FireclusterConditionAction() {
    		super("Fire Cluster Condition");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(fireClusterCondition));
    		putValue(Action.SMALL_ICON, fireClusterCondition ? Icons.TICK : Icons.CROSS);
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			fireClusterCondition = ! fireClusterCondition;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(fireClusterCondition));
    		putValue(Action.SMALL_ICON, fireClusterCondition ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class IgnoredBorderEntitiesAction extends AbstractAction {
    	public IgnoredBorderEntitiesAction() {
    		super("Ignore Border Entities");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(ignoredBorderEntities));
    		putValue(Action.SMALL_ICON, ignoredBorderEntities ? Icons.TICK : Icons.CROSS);
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			ignoredBorderEntities = ! ignoredBorderEntities;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(ignoredBorderEntities));
    		putValue(Action.SMALL_ICON, ignoredBorderEntities ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class RenderBorderEntitiesAction extends AbstractAction {
    	public RenderBorderEntitiesAction() {
    		super("Border Entities");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(borderEntities));
    		putValue(Action.SMALL_ICON, borderEntities ? Icons.TICK : Icons.CROSS);
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			borderEntities = ! borderEntities;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(borderEntities));
    		putValue(Action.SMALL_ICON, borderEntities ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class BorderDirectionBuildingAction extends AbstractAction {
    	public BorderDirectionBuildingAction() {
    		super("Border Direction Buildings");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(borderDirectionBuilding));
    		putValue(Action.SMALL_ICON, borderDirectionBuilding ? Icons.TICK : Icons.CROSS);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			borderDirectionBuilding = ! borderDirectionBuilding;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(borderDirectionBuilding));
    		putValue(Action.SMALL_ICON, borderDirectionBuilding ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class RenderBorderHullsAction extends AbstractAction {
    	public RenderBorderHullsAction() {
    		super("Border Hulls");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(borderHulls));
    		putValue(Action.SMALL_ICON, borderHulls ? Icons.TICK : Icons.CROSS);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			borderHulls = ! borderHulls;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(borderHulls));
    		putValue(Action.SMALL_ICON, borderHulls ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class MapBorderBuildingAction extends AbstractAction {
    	public MapBorderBuildingAction() {
    		super("Map Border Buildings");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(mapBorderBuildings));
    		putValue(Action.SMALL_ICON, mapBorderBuildings ? Icons.TICK : Icons.CROSS);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			mapBorderBuildings = ! mapBorderBuildings;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(mapBorderBuildings));
    		putValue(Action.SMALL_ICON, mapBorderBuildings ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class showTargetAction extends AbstractAction {
    	public showTargetAction() {
    		super("Show Target");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(showTarget));
    		putValue(Action.SMALL_ICON, showTarget ? Icons.TICK : Icons.CROSS);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			showTarget = ! showTarget;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(showTarget));
    		putValue(Action.SMALL_ICON, showTarget ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
    
    @SuppressWarnings("serial")
    private final class RenderDirectionTriangleAction extends AbstractAction {
    	public RenderDirectionTriangleAction() {
    		super("Fire Direction");
    		putValue(Action.SELECTED_KEY, Boolean.valueOf(directionTriangle));
    		putValue(Action.SMALL_ICON, directionTriangle ? Icons.TICK : Icons.CROSS);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			directionTriangle = ! directionTriangle;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(directionTriangle));
    		putValue(Action.SMALL_ICON, directionTriangle ? Icons.TICK : Icons.CROSS);
    		component.repaint();
		}
    }
}