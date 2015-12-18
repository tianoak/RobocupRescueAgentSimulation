package csu.Viewer.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.view.StandardViewLayer;
import rescuecore2.view.RenderedObject;

public class CSU_CriticalAreaLayer extends StandardViewLayer{
	private static final Color CRITICAL_AREA_COLOR = Color.ORANGE;
	public static List<StandardEntity> CRITICAL_AREA = 
			Collections.synchronizedList(new ArrayList<StandardEntity>());
	

	public CSU_CriticalAreaLayer() {
		setVisible(true);
	}
	
	@Override
	public String getName() {
		return "critial area";
	}
	
	@Override
	public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
		g.setColor(CRITICAL_AREA_COLOR);
		List<RenderedObject> result = new ArrayList<>();
		
		if (!CRITICAL_AREA.isEmpty()) {
			for (StandardEntity next : CRITICAL_AREA) {
				Area area = (Area) next;
				int[] apexList = area.getApexList();
				int vertexCount = apexList.length / 2;
				
				int[] x_coordinates = new int[vertexCount];
				int[] y_coordinates = new int[vertexCount];
				
				for (int i = 0, j = 0; i < vertexCount; i++, j = j + 2) {
					x_coordinates[i] = transform.xToScreen(apexList[j]);
					y_coordinates[i] = transform.yToScreen(apexList[j + 1]);
				}
				g.fill(new Polygon(x_coordinates, y_coordinates, vertexCount));
			}
		}
		
		return result;
	}

}
