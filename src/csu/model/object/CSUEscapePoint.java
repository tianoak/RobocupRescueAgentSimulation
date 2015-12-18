package csu.model.object;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import rescuecore2.misc.geometry.Line2D;

/**
 * Date: June 12, 2014 Time: 6:29pm
 * 
 * @author appreciation-csu
 * 
 */
public class CSUEscapePoint {
	private List<CSUBlockade> realteBlockades = new ArrayList<>();;
	
	private Point underlyingPoint;
	
	private Line2D line;
	
	public CSUEscapePoint(Point point, Line2D line, CSUBlockade... blockade) {
		this.setUnderlyingPoint(point);
		this.setLine(line);
		
		for (CSUBlockade next : blockade) {
			this.realteBlockades.add(next);
		}
	}

	public List<CSUBlockade> getRelateBlockade() {
		return this.realteBlockades;
	}
	
	public void addCsuBlockade(CSUBlockade blockade) {
		this.realteBlockades.add(blockade);
	}
	
	public boolean removeCsuBLockade(CSUBlockade blockade) {
		return this.realteBlockades.remove(blockade);
	}

	public Point getUnderlyingPoint() {
		return underlyingPoint;
	}

	public void setUnderlyingPoint(Point underlyingPoint) {
		this.underlyingPoint = underlyingPoint;
	}

	public Line2D getLine() {
		return line;
	}

	public void setLine(Line2D line) {
		this.line = line;
	}
}
