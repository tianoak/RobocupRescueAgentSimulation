//package csu.Viewer.layers;
//
//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.Stroke;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import csu.model.route.pov.graph.EdgeNodeBase;
//
//import rescuecore2.misc.gui.ScreenTransform;
//import rescuecore2.standard.entities.Edge;
//import rescuecore2.standard.view.StandardViewLayer;
//import rescuecore2.view.RenderedObject;
//
//public class CSU_PovLayer extends StandardViewLayer{
//	private static final Color EDGE_POINT_COLOR = Color.GREEN;
//	private static final Stroke EDGE_POINT_STROKE = new BasicStroke(1.5f);
//	
//	public static List<EdgeNodeBase> EDGE_NODES = Collections.synchronizedList(new ArrayList<EdgeNodeBase>());
//	
//	public CSU_PovLayer() {
//		this.setVisible(false);
//	}
//	
//	@Override
//	public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
//		Collection<RenderedObject> resultList = new ArrayList<>();
//		
//		if (!EDGE_NODES.isEmpty()) {
//			g.setColor(EDGE_POINT_COLOR);
//			g.setStroke(EDGE_POINT_STROKE);
//			for (EdgeNodeBase next : EDGE_NODES) {
//				Edge edge = next.getEdge();
//				int start_x = transform.xToScreen(edge.getStartX());
//				int start_y = transform.yToScreen(edge.getStartY());
//				int end_x = transform.xToScreen(edge.getEndX());
//				int end_y = transform.yToScreen(edge.getEndY());
//				g.drawLine(start_x, start_y, end_x, end_y);
//			}
//		}
//		
//		return resultList;
//	}
//
//	@Override
//	public String getName() {
//		return "POV Layer";
//	}
//
//}
