package csu.geom;

import java.awt.Polygon;
import rescuecore2.standard.entities.Blockade;

/**
 * Expand the shape of a blockade. First, increase the distance of each vertex
 * to blockade's center. Then get the center point of each edge and increase its
 * distance to blockade's center. So finally, we gain an enlarged shape of this
 * blockade with vertexes number doubled.
 * 
 * @author Wang Shang - csu
 */
public class ExpandApexes {
	/**
	 * Expand the shape of a blockade.
	 * 
	 * @param blockade
	 *            the blockade whose apexes will be expanded
	 * @param size
	 *            the distance a vertex will expand 
	 * @return the expanded shape of this blockade
	 */
	public static Polygon expandApexes(Blockade blockade, int size) {
		/*
		 * The simplest shape is the triangle which need three vertices. So it
		 * is impossible for a blockade with apexes length less than 6.
		 */
		if (!blockade.isApexesDefined() || blockade.getApexes().length < 6 
				|| !blockade.isXDefined() || !blockade.isYDefined()) {
			return null;
		}
		
		int cx = blockade.getX();
		int cy = blockade.getY();
		Polygon polygon = new Polygon();
		int[] apexes = blockade.getApexes();
	
///		for (int i = 0; i < blockade.getApexes().length; i += 2) {
		for (int i = 0; i  < apexes.length; i += 2) {
			int x = apexes[i];
			int y = apexes[i + 1];
			int dx = x - cx;
			int dy = y - cy;
			double distance = getDistance(cx, cy, x, y);
			double coefficient = (distance + size) / distance;

			double newX = cx + (dx * coefficient);
			double newY = cy + (dy * coefficient);
			polygon.addPoint((int) newX, (int) newY);

			// get the center point of the blockade's edge which start from point (apexes[i], apexes[i + 1])
			x = (apexes[i] + apexes[(i + 2) % apexes.length]) / 2;
			y = (apexes[i + 1] + apexes[(i + 3) % apexes.length]) / 2;
			dx = x - cx;
			dy = y - cy;
			distance = getDistance(cx, cy, x, y);
			coefficient = (distance + size) / distance;

			newX = cx + (dx * coefficient);
			newY = cy + (dy * coefficient);
			polygon.addPoint((int) newX, (int) newY);
		}
		return polygon;
	}
	
	private static double getDistance(int x1, int y1, int x2, int y2) {
		return Math.hypot((x1 - x2), (y1 - y2));
	}
}