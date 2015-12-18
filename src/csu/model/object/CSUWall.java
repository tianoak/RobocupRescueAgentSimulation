package csu.model.object;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.Hashtable;
import java.util.Random;

import csu.model.AdvancedWorldModel;

/**
 * The Wall object represent the wall of a building. And this class is borrowed
 * from RCRSS: firesimulator.world.Wall.
 * 
 * Date: Sep 06, 2013 Time: 3:34PM
 * 
 * @author appreciation-csu
 */
public class CSUWall {
	/**
	 * The random seed in resq-fire.cfg is 23.
	 */
	private static Random random = new Random(23);
	/**
	 * Constant from resq-fire.cfg: the number of rays this wall emitted is (wall_length * ray_rate)
	 */
	private static double RAY_RATE = 0.0025;
	/**
	 * Constant from resq-fire.cfg: the maximum distance a energy can reach.
	 * 
	 * In server's simulator, this value is 200000. But in our simulator, it
	 * should be smaller.
	 */ ///why
	private static double MAX_RAY_DISTANCE = 50000;
	
	public static final int MAX_SAMPLE_DISTANCE = 100000;

	/** The X coordinate of this wall's start point.*/
	public int x1;
	/** The Y coordinate of this wall's start point.*/
	public int y1;
	/** The X coordinate of this wall's end point.*/
	public int x2;
	/** The Y coordinate of this wall's end point.*/
	public int y2;
	
	/** The owner building of this wall.*/
	public CSUBuilding owner;
	
	/** The number of energy rays this wall emitted.*/
	public int rays;
	
	/**
	 * The number of wall this wall's energy ray hits. The wall with same owner
	 * of this wall is not included.
	 */
	public int hits;
	/**
	 * The number of wall, which has the same owner of this wall, this wall's
	 * energy ray hits.
	 */
	public int selfHits;
	/**
	 * The number of rays which is not started from this wall.
	 */
	public int strange;
	
	/** The length of this wall.*/
	public double length;
	/** The start point of this wall.*/
	public Point startPoint;
	/** The end point of this wall.*/
	public Point endPoint;
	
	
//	public int distance;
//	public boolean right;
	
	
	public CSUWall(int x1, int y1, int x2, int y2, CSUBuilding owner, AdvancedWorldModel world) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.startPoint = new Point(x1, y1);
		this.endPoint = new Point(x2, y2);
		this.length = startPoint.distance(endPoint);
		
		this.owner = owner;
		// the number of walls this wall's ray hits excluding walls with the same owner of this wall
		this.hits = 0; 
		this.rays = (int)Math.ceil(length * RAY_RATE);
	}
	
	/** This method used to judge whether the two point can form a wall.*/
	public boolean validate() {
		return !(startPoint.x == endPoint.x && startPoint.y == endPoint.y);
	}
	
	public void findHits(AdvancedWorldModel world, CSUBuilding building) {
		this.selfHits = 0; 
		this.strange = 0;  
		for (int emitted = 0; emitted < this.rays; emitted++) {
			Point start = getRandomPoint(startPoint, endPoint);
			if (start == null) {      ///can not be null
				strange++;
				continue;
			}
			Point end = getRandomPoint(startPoint, MAX_RAY_DISTANCE);
			CSUWall closest = null;
			double minDistance = Double.MAX_VALUE;
			for (CSUWall other : building.getAllWalls()) {
				if (other == this)
					continue;
				Point cross = intersect(start, end, other.startPoint, other.endPoint);
				if (cross != null && cross.distance(start) < minDistance) {
					minDistance = cross.distance(start);
					closest = other;
				}
			}
			if (closest == null) {
                continue;    // nothing was hit
            }
            if (closest.owner == this.owner) {
                selfHits++;   // the source building was hit
            }
            if (closest != this && closest != null && closest.owner != owner) {
                hits++;
                Hashtable<CSUBuilding, Integer> hashtable = building.getConnectedBuildingTable();
                Integer value = hashtable.get(closest.owner);
                int temp = 0;
                if (value != null) {
                    temp = value.intValue();
                }
                temp++;
                hashtable.put(closest.owner, new Integer(temp));
            }
		}
	}
	
/* ---------------------------------------- Geometry Functions --------------------------------------------- */

	/**
	 * Returns a random point on a line.
	 * 
	 * @param a
	 *            One point defining the line
	 * @param b
	 *            The other point defining the line
	 * @return A point between a and b
	 */
    public static Point getRandomPoint(Point a, Point b) {
        float[] mb = getAffineFunction((float) a.x, (float) a.y, (float) b.x, (float) b.y);
        float dx = (Math.max((float) a.x, (float) b.x) - Math.min((float) a.x, (float) b.x));
        dx *= random.nextDouble();
        dx += Math.min((float) a.x, (float) b.x);
        if (mb == null) {
            //vertical line
            int p = Math.max(a.y, b.y) - Math.min(a.y, b.y);
            p = (int) (p * Math.random());
            p = p + Math.min(a.y, b.y);
            return new Point(a.x, p);
        }
        float y = mb[0] * dx + mb[1];
        Point rtv = new Point((int) dx, (int) y);
        if (rtv != null) {
            System.currentTimeMillis();
        }
        return rtv;
    }

	/**
	 * Get a random point with the given distance from the given start point a.
	 * 
	 * @param a
	 *            the given start point
	 * @param length
	 *            the distance between the random point and given start point a
	 * @return a random point
	 */
    public static Point getRandomPoint(Point a, double length) {
        double angel = random.nextDouble() * 2d * Math.PI;
        double x = Math.sin(angel) * length;
        double y = Math.cos(angel) * length;
        return new Point((int) x + a.x, (int) y + a.y);
    }

    /**
	 * Get the linear equations of those two points. A linear equation can be
	 * write in this form: y = k * x + b. And we let float_array[0] = k,
	 * float_array[1] = b, and then return this float array.
	 * 
	 * @param x1
	 *            the X coordiante of point_1
	 * @param y1
	 *            the Y coordinate of point_1
	 * @param x2
	 *            the X coordiante of point_2
	 * @param y2
	 *            the Y coordinate of point_2
	 * @return a float array which represents the linear equation
	 */
    public static float[] getAffineFunction(float x1, float y1, float x2, float y2) {
        if (x1 == x2) 
        	return null;
        float m = (y1 - y2) / (x1 - x2);
        float b = y1 - m * x1;
        return new float[]{m, b};
    }

    public static Point intersect(Point a, Point b, Point c, Point d) {
        float[] rv = intersect(new float[]{a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y});
        if (rv == null) return null;
        return new Point((int) rv[0], (int) rv[1]);
    }
    
    public static float[] intersect(float[] points) {
        float[] l1 = getAffineFunction(points[0], points[1], points[2], points[3]);
        float[] l2 = getAffineFunction(points[4], points[5], points[6], points[7]);
        float[] crossing;
        if (l1 == null && l2 == null) {
            return null;
        } else if (l1 == null && l2 != null) {
            crossing = intersect(l2[0], l2[1], points[0]);
        } else if (l1 != null && l2 == null) {
            crossing = intersect(l1[0], l1[1], points[4]);
        } else {
            crossing = intersect(l1[0], l1[1], l2[0], l2[1]);
        }
        if (crossing == null) {
            return null;
        }
        if (!(inBounds(points[0], points[1], points[2], points[3], crossing[0], crossing[1]) &&
                inBounds(points[4], points[5], points[6], points[7], crossing[0], crossing[1]))) return null;
        return crossing;
    }

    public static float[] intersect(float m1, float b1, float x) {
        return new float[]{x, m1 * x + b1};
    }

    public static float[] intersect(float m1, float b1, float m2, float b2) {
        if (m1 == m2) {
            return null;
        }
        float x = (b2 - b1) / (m1 - m2);
        float y = m1 * x + b1;
        return new float[]{x, y};
    }

    public static boolean inBounds(float bx1, float by1, float bx2, float by2, float x, float y) {
        if (bx1 < bx2) {
            if (x < bx1 || x > bx2) return false;
        } else {
            if (x > bx1 || x < bx2) return false;
        }
        if (by1 < by2) {
            if (y < by1 || y > by2) return false;
        } else {
            if (y > by1 || y < by2) return false;
        }
        return true;
    }

    public Line2D getLine() {
        if (startPoint == null || endPoint == null)
            return null;

        return new Line2D.Double(startPoint, endPoint);
    }
}
