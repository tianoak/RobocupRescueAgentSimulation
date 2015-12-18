package csu.common.test;

import java.awt.Polygon;
import java.awt.geom.Area;

public class AreaIntersect {

	int[] block_x = {64179, 65132, 57032, 56142, 56042, 56042, 68935};
	int[] block_y = {92042, 93355, 99235, 98009, 99000, 100638, 92042};
	
	int[] human_x = {55977, 63665, 64251, 56563};
	int[] human_y = {96913, 91333, 92141, 97721};
	
	public void intersect() {
		Area block_area = new Area(new Polygon(block_x, block_y, 7));
		Area human_area = new Area(new Polygon(human_x, human_y, 4));
		
		block_area.intersect(human_area);
		
		if (block_area.getPathIterator(null).isDone())
			System.out.println("No intersect");
		else
			System.out.println("Intersect");
	}
	
	public static void main(String[] args) {
		(new AreaIntersect()).intersect();
	}
}
