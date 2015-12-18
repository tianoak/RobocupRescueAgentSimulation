package csu.Viewer.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.view.StandardEntityViewLayer;

public abstract class CSU_AreaLayer<E extends Area> extends StandardEntityViewLayer<E> {
	/**
	 * The transform will translate the world coordinates to screen coordinates.
	 */
	protected ScreenTransform transform;
	
	/**
	 * Construct a <code>Area</code> viewer layer.
	 * @param clazz the subclass of <code>Area</code> this can render
	 */
	protected CSU_AreaLayer(Class<E> clazz) {
		super(clazz);
	}
	
	@Override
	public Shape render(E area, Graphics2D g, ScreenTransform t) {
		transform = t;
		List<Edge> edges = area.getEdges();
		if (edges.isEmpty())
			return null;
		int edgeCount = edges.size();
		int[] Xs = new int[edgeCount];
		int[] Ys = new int[edgeCount];
		int i = 0;
		for (Edge next : edges) {
			Xs[i] = t.xToScreen(next.getStartX());
			Ys[i] = t.yToScreen(next.getStartY());
			i++;
		}
		Polygon shape = new Polygon(Xs, Ys, edgeCount);
		paintShape(area, shape, g);
		
		for(Edge next : edges)
			paintEdge(next, g, t);
		
		return shape;
	}
	
	/**
	 * Paint the shape of an area in the screen.
	 * 
	 * @param area
	 *            the area will be painted
	 * @param p
	 *            the shape of this area
	 * @param g
	 *            the graphics to paint on
	 */
	protected void paintShape(E area, Polygon p, Graphics2D g) {
		
	}
	
	/**
	 * Paint an indivial edge on the screen.
	 * 
	 * @param edge
	 *            the edge will be paint
	 * @param g
	 *            the graphics to paint on
	 * @param t
	 *            the ScreenTransform will translate world coordinates to screen coordinates
	 */
	protected void paintEdge(Edge edge, Graphics2D g, ScreenTransform t) {
		
	}
	
	/**
	 * Draw the string info of this area. Generally, we draw its Id.
	 * 
	 * @param area
	 *            the area which info will be drawn
	 * @param g
	 *            the graphics to paint on
	 * @param info
	 *            the info of this area
	 * @param clazz
	 *            the <code>Class</code> object of this area
	 */
	protected void drawInfo(Area area, Graphics2D g, String info, Class<?> clazz) {
		if (info == null)
			return;
		Pair<Integer, Integer> location = getLocation(area);
		int x = transform.xToScreen(location.first());
		int y = transform.yToScreen(location.second());
		if (clazz.equals(Road.class))
			g.setColor(Color.CYAN.darker().darker());
		else
			g.setColor(Color.MAGENTA.brighter().brighter());
		g.drawString(info, x - 10, y + 5);
	}
	
	/**
	 * Get the location of this area.
	 * 
	 * @param area
	 *            the area object
	 * @return a <code>Pair</code> represent the area's X and Y coordinates
	 */
	protected Pair<Integer, Integer> getLocation(Area area) {
		return area.getLocation(world);
	}
}
