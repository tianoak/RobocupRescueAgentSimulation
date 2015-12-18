package csu.util;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.*;

import rescuecore2.misc.Pair;

/**
 * java.awt.geom.Area扩张，辅助传球関处理追加了班
 * @author com
 *
 */
public class MyArea extends Area {

	public MyArea() {
		super();
	}
	
	public MyArea(Shape shape) {
		super(shape);
	}
	
	/**
	 * Area分传球的集合转换了的东西还给人家
	 * @return 辅助传球集合
	 */
	public List<Shape> divideArea() {
		if (!isPolygonal()) {
			return null;
		}
		List<Shape> res = new ArrayList<Shape>();
		PathIterator it = getPathIterator(null);
		Path2D path = null;
		do {
			double[] coords = new double[6];
			switch (it.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO :
				path = new Path2D.Double(it.getWindingRule());
				path.moveTo(coords[0], coords[1]);
				path.setWindingRule(it.getWindingRule());
				break;
			case PathIterator.SEG_LINETO :
				path.lineTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_QUADTO :
				path.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			case PathIterator.SEG_CUBICTO :
				path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				break;
			case PathIterator.SEG_CLOSE :
				path.clone();
				res.add(path);
				break;
			default:
				// 無視
				break;
			}
			it.next();
		} while (!it.isDone());
		return res;
	}
	
	/**
	 * 毫了包括得到辅助传球坐标
	 * @param x 指定的X座標
	 * @param y 指定的Y座標
	 * @return 1个分传球
	 */
	public Shape getSubPath(int x, int y) {
		if (!isPolygonal()) {
			return null;
		}
		PathIterator it = getPathIterator(null);
		Polygon polygon = new Polygon();
		while (!it.isDone()) {
			double[] coords = new double[6];
			switch (it.currentSegment(coords)) {
			case PathIterator.SEG_CLOSE :
				if (polygon.contains(x, y)) {
					return polygon;
				}
				break;
			case PathIterator.SEG_MOVETO :
				polygon.reset();
			case PathIterator.SEG_LINETO :
				polygon.addPoint((int)coords[0], (int)coords[1]);
				break;
			case PathIterator.SEG_QUADTO :
			case PathIterator.SEG_CUBICTO :
			default:
				// 無視
				break;
			}
			it.next();
		}
		return null;
	}
	
	public Shape getSubPath(Pair<Integer, Integer> coords) {
		return getSubPath(coords.first(), coords.second());
	}
	
	
	public boolean isSamePath(List<Pair<Integer, Integer>> points) {
		//		Pair<Integer, Integer> origin = points.remove(0);
		//		Shape shape = getSubPath(origin);
		//		for (Pair<Integer, Integer> point : points) {
		//			if (!shape.contains(point.first(), point.second())) {
		//				return false;
		//			}
		//		}
		//		return true;
		for (Pair<Integer, Integer> point : points) {
			if (!contains(point.first(), point.second())) {
				return false;
			}
		}
		return true;
	
	}
}
