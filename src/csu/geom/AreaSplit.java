package csu.geom;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import csu.model.AdvancedWorldModel;

/**
 * Ri-One
 */
public class AreaSplit {
	private AdvancedWorldModel world;
	final public List<Integer> splitsList;
	public final int LEFT, TOP, WIDTH, HEIGHT;
	private Rectangle[] rects;

	public AreaSplit(AdvancedWorldModel w, final int n) {
		world = w;
		
		int minX = world.getConfig().MIN_X;  // the X coordinate of the top left corner
		int maxX = world.getConfig().MAX_X;  // the X coordinate of the bottom right corner
		int minY = world.getConfig().MIN_Y;  // the Y coordinate of the top left corner
		int maxY = world.getConfig().MAX_Y;  // the Y coordinate of the bottom right corner
		LEFT = minX;
		TOP = minY;
		WIDTH = maxX - minX;
		HEIGHT = maxY - minY;

		splitsList = getAreaSplit(n);

		rects = new Rectangle[n];
		int index = 0;

		int boxH = HEIGHT / getVerticalCount();
		int y = TOP;
		for (Integer c : splitsList) {
			int boxW = WIDTH / c;
			int x = LEFT;
			for (int i = 0; i < c; i++) {
				rects[index] = new Rectangle(x, y, boxW, boxH);
				index++;
				x += boxW;
			}
			y += boxH;
		}
	}

	final public Rectangle[] getRects() {
		return rects;
	}

	List<Integer> getAreaSplit(int n) {
		assert (n > 0);
		ArrayList<Integer> field = new ArrayList<Integer>();
		field.add(1);
		for (int i = 2; i <= n; i++) {
			int w = Collections.max(field);
			int h = field.size();
			int m = Collections.min(field);
			if (h < m) {
				field = new ArrayList<Integer>();
				for (int j = 0; j < w; j++) {
					field.add(h);
				}
			}
			int index = field.indexOf(Collections.min(field));
			field.set(index, field.get(index) + 1);
		}
		return field;
	}

	final public int getVerticalCount() {
		return splitsList.size();
	}

	final public int getHorizontalCount(int n) {
		return splitsList.get(n);
	}
}
