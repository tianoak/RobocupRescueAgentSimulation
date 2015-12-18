package csu.standard.simplePartition;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import csu.agent.Agent;
import csu.model.AdvancedWorldModel;
import csu.standard.Ruler;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author murat
 */
public class Locator {
	private static int AGENT_RADIUS = 500;
	
	
    public static Point getCenter(Edge edge){
        Point p;
        int x, y;

        x = (edge.getStartX() + edge.getEndX()) / 2;
        y = (edge.getStartY() + edge.getEndY()) / 2;
        p = new Point(x, y);
        return p;
    }

    public static Point getCenter(Area area){
        Point p;
        int x, y;

        x = area.getX();
        y = area.getY();
        p = new Point(x, y);
        return p;
    }

    public static Line2D getLine(Edge edge){
        Point p1, p2;
        int x, y;
        Line2D line;

        x = edge.getStartX();
        y = edge.getStartY();
        p1 = new Point(x, y);
        x = edge.getEndX();
        y = edge.getEndY();
        p2 = new Point(x, y);
        line = new Line2D.Double(p1, p2);
        return line;
    }

    public static Point getExtensionPoint(Point reference, Point target, int extension){
        int d, x, y, dx, dy;
        double r;
        Point p;

        d = (int)Ruler.getDistance(reference, target);
        r = (d+extension)/(double)d;
        dx = target.x - reference.x;
        dy = target.y - reference.y;
        x = (int) (reference.x + r * dx);
        y = (int) (reference.y + r * dy);
        p = new Point(x, y);
        return p;
    }


    public static Point getPosition(StandardEntity entity, AdvancedWorldModel model) {
        Point p;
        Pair<Integer, Integer> pair;

        pair = entity.getLocation(model);
        if (pair == null) {
            return null;
        } else {
            p = new Point(pair.first(), pair.second());
            return p;
        }
    }

    public static Point getPosition(EntityID id, AdvancedWorldModel model) {
        Point p;
        StandardEntity entity;
        
        entity = model.getEntity(id);
        p = getPosition(entity, model);
        return p;
    }

    public static Rectangle getBounds(Human human, AdvancedWorldModel model) {
        Point p;
        double s;
        Rectangle2D rect;
        
        s = AGENT_RADIUS * 2;
        p = getPosition(human, model);
        rect = new Rectangle2D.Double(p.x-s, p.y-s, s*2, s*2);  ///why double 

        return rect.getBounds();
    }
}
