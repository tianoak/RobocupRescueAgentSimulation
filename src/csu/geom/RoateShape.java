package csu.geom;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

@SuppressWarnings("serial")
public class RoateShape extends JPanel{
	
	private Stroke stroke = new BasicStroke(3.0f, 0, 2); 
	private Stroke lineStroke = new BasicStroke(1.7f, 0, 2, 0, new float[]{9}, 0);
//	private Stroke lineStroke = new BasicStroke(1.7f, 0, 2);
	private static final int gridSize = 30;
	
	private List<Grid> grids = new ArrayList<>();
	private int idGenerator = 0;
	
	private int center_X = 350;
	private int center_y = 350;
	
	@Override
	public void paintComponent(Graphics g) {
		grids.clear();
		Graphics2D gra_2d = (Graphics2D) g;
		gra_2d.setStroke(stroke);
		gra_2d.setColor(Color.GREEN);
		
		Polygon polygon = makePolygon(6, center_X, center_y, 100);
		gra_2d.drawPolygon(polygon);
		
		gra_2d.setColor(Color.CYAN);
		Shape shape = createGrid(polygon);
		gra_2d.draw(shape);
//		gra_2d.draw(shape.getBounds2D());
		
		gra_2d.setStroke(lineStroke);
		
		for (Grid grid : grids) {
//			grid.getVertices();
			
			List<Pair<Integer, Integer>> vertex = grid.getVertices();
			int count = vertex.size();
			for (int i = 0; i < count; i++) {
				gra_2d.drawLine(vertex.get(i).first().intValue(), 
						vertex.get(i).second().intValue(), 
						vertex.get((i + 1) % count).first().intValue(), 
						vertex.get((i + 1) % count).second().intValue());
			}
		}
	}
	
	/**
	 * Create a polygon.
	 * 
	 * @param vertexsCount
	 *            the vertex count of this polygon
	 * @param center_x
	 *            the x coordinate of this polygon's center
	 * @param center_y
	 *            the y coordinate of this polygon's center
	 * @param radius
	 *            the radius length of this polygon's
	 * @return a polygon with given vertex count.
	 */
	private Polygon makePolygon(int vertexsCount, int center_x, int center_y, double radius) {
		double dAngle = Math.PI * 2 / vertexsCount;
		int[] x_coordinates = new int[vertexsCount];
		int[] y_coordinates = new int[vertexsCount];
		
		for (int i = 0; i < vertexsCount; i++) {
			double angle = i * dAngle;
			Vector2D vector = new Vector2D(Math.sin(angle), Math.cos(angle)).scale(radius);
			Point2D centerPoint = new Point2D(center_x, center_y);
			Point2D vertexPoint = centerPoint.translate(vector.getX(), vector.getY());
			
			x_coordinates[i] = (int)vertexPoint.getX();
			y_coordinates[i] = (int)vertexPoint.getY();
		}
		
		return new Polygon(x_coordinates, y_coordinates, vertexsCount);
	}
	
	private Shape createGrid(Polygon shape) { 
        double cx = shape.getBounds().getCenterX();   // center X
        double cy = shape.getBounds().getCenterY();   // center Y

        double alpha = 0;
        // get the longest edge of this area
        Edge longestEdge = getLongestEdge(shape);
        double xStart = longestEdge.getStartX();
        double yStart = longestEdge.getStartY();
        double xEnd = longestEdge.getEndX();
        double yEnd = longestEdge.getEndY();

        double numerator = yEnd - yStart;		// 分子
        double denominator = xEnd - xStart;		// 分母
        double mRoad = 0;

        AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
        Shape rotatedShape;

        if (denominator != 0) {

            mRoad = numerator / denominator;
            alpha = Math.toDegrees(Math.atan(mRoad));

            if (alpha > 0 && alpha < 45) {
                // nothing to do
            } else if (alpha > 45) {
                alpha = alpha - 90;
            } else if (alpha < 0 && alpha > (-45)) {
                // nothing to do
            } else if (alpha < (-45)) {
                alpha = 90 + alpha;  // because alpha is negative.
            }
            alpha = (-1) * alpha;
        }

        if (mRoad == 0 || alpha == -90) {
            alpha = 0;
        }
        alpha = Math.toRadians(alpha);

        at.rotate(alpha, cx, cy);
        rotatedShape = at.createTransformedShape(shape);
        
        alpha = (-1) * alpha;

        createGrids(shape, rotatedShape, at, alpha, cx, cy);
        
        return rotatedShape;
	}
	
	private Edge getLongestEdge(Polygon shape) {
		int vertexCount = shape.npoints / 2;
		int x1, y1, x2, y2;
		
		int e_x1 = 0, e_y1 = 0, e_x2 = 0, e_y2 = 0;
		
		double longestDistance = Double.MIN_VALUE, distance;
		
		for (int i = 0; i < vertexCount; i ++) {
			x1 = shape.xpoints[i];
			y1 = shape.ypoints[i];
			
			x2 = shape.xpoints[(i + 1) % vertexCount];
			y2 = shape.ypoints[(i + 1) % vertexCount];
			
			distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
			
			if (distance > longestDistance) {
				longestDistance = distance;
				e_x1 = x1;
				e_y1 = y1;
				e_x2 = x2;
				e_y2 = y2;
			}
		}
		
		return new Edge(e_x1, e_y1, e_x2, e_y2);
	}

	public void createGrids(Shape area, Shape areaShape, AffineTransform at, double alpha, double cx, double cy) {
        Rectangle2D areaBound = areaShape.getBounds2D();
        int startX = (int) areaBound.getX(); // shape x
        int startY = (int) areaBound.getY(); // shape y
        double width = areaBound.getWidth(); // shape width
        double height = areaBound.getHeight(); // shape height
        double finalX = startX + width; // shape final x
        double finalY = startY + height; // shape final y

        int widthNumber = (int) (width / gridSize); 
        int heightNumber = (int) (height / gridSize);

        if (width < height) {
            if (widthNumber > 10) {
                widthNumber = (int) (width / (gridSize * 2));
                if (widthNumber < 10) {
                    widthNumber = 10;
                }
            }
        } else {
            if (heightNumber > 10) {
                heightNumber = (int) (height / (gridSize * 2));
                if (heightNumber < 10) {
                    heightNumber = 10;
                }
            }
        }
        
        if (widthNumber < 1) {
            widthNumber = 1;
        }
        if (heightNumber < 1) {
            heightNumber = 1;
        }

        int widthDist = (int) (width / widthNumber);
        int heightDist =(int) (height / heightNumber);

        int widthCounter;
        int heightCounter = 0;
        int thisGridFinalY;
        int thisGridFinalX;

        for (int y = startY; y < finalY; y += heightDist) {
            heightCounter++;
            widthCounter = 0;
            thisGridFinalY = y + heightDist;

            for (int x = startX; x < finalX; x += widthDist) {
                widthCounter++;

                int id = idGenerator;

                
                if (!areaShape.contains((x + (widthDist / 2)), (y + (heightDist / 2)))) {
                    continue;
                }

                Pair<Integer, Integer> position = new Pair<Integer, Integer>((x + (widthDist / 2)), (y + (heightDist / 2)));

                Grid grid = new Grid(id, position);

                thisGridFinalX = x + widthDist;

                if (widthCounter == widthNumber) {
                    thisGridFinalX = (int) Math.round(finalX + 0.5);
                }
                if (heightCounter == heightNumber) {
                    thisGridFinalY = (int) Math.round(finalY + 0.5);
                }

                grid.addVertex(new Pair<Integer, Integer>(x, y));// 0 - southwest vertex
                grid.addVertex(new Pair<Integer, Integer>(thisGridFinalX, y));// 1 - southeast vertex
                grid.addVertex(new Pair<Integer, Integer>(thisGridFinalX, thisGridFinalY));// 2 - northeast vertex
                grid.addVertex(new Pair<Integer, Integer>(x, thisGridFinalY));// 3 - northwest vertex

				this.grids.add(grid);
				idGenerator++;
            }
        }

        rotateGrids(at, alpha, cx, cy);

    }
	
	private void rotateGrids(AffineTransform at, double alpha, double cx, double cy) {
        for (Grid grid : grids) {
            List<Pair<Double, Double>> points = new ArrayList<Pair<Double, Double>>();
            List<Pair<Double, Double>> rPoints;

            points.add(new Pair<Double, Double>((double) grid.getPosition().first(), (double) grid.getPosition().second()));

            for (Pair<Integer, Integer> pair : grid.getVertices()) {
                points.add(new Pair<Double, Double>((double) pair.first(), (double) pair.second()));
            }

            rPoints = rotatePoints(points, at, alpha, cx, cy);

            grid.setPosition(new Pair<Integer, Integer>((int) Math.round(rPoints.get(0).first()), (int) Math.round((rPoints.get(0).second()))));
            rPoints.remove(0);

            grid.getVertices().clear();
            for (Pair<Double, Double> pair : rPoints) {
                grid.addVertex(new Pair<Integer, Integer>((int) Math.round(pair.first()), (int) Math.round(pair.second())));
            }
        }
    }
	
	private List<Pair<Double, Double>> rotatePoints(List<Pair<Double, Double>> points, AffineTransform at, double alpha, double cx, double cy) {
        List<Pair<Double, Double>> rotatedPoints = new ArrayList<Pair<Double, Double>>();
        java.awt.geom.Point2D point = new Point(), rotatedPoint = new Point();

        for (Pair<Double, Double> pair : points) {

            point.setLocation(pair.first(), pair.second());
            at = AffineTransform.getRotateInstance(alpha, cx, cy);
            rotatedPoint = at.transform(point, rotatedPoint);

            rotatedPoints.add(new Pair<Double, Double>(rotatedPoint.getX(), rotatedPoint.getY()));
        }
        return rotatedPoints;
    }
	
	private class Edge {
		double startX, startY, endX, endY;
		
		public Edge(double startX, double startY, double endX, double endY) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
		
		public double getStartX() {
			return this.startX;
		}
		
		public double getStartY() {
			return this.startY;
		}
		
		public double getEndX() {
			return this.endX;
		}
		
		public double getEndY() {
			return this.endY;
		}
	}
	
	private class Grid {
		@SuppressWarnings("unused")
		private int id;
	    // grid center
	    private Pair<Integer, Integer> position;
	    
	    // 4 vertex grid. ke az paeen smate chap shoroo va pad sa@t gard edame dare.
	    private ArrayList<Pair<Integer, Integer>> vertices = new ArrayList<Pair<Integer, Integer>>();

	    public Grid(int id, Pair<Integer, Integer> position) {
	        this.id = id;
	        this.position = position;
	    }

	    public void setPosition(Pair<Integer, Integer> position) {
	        this.position = position;
	    }

	    public void addVertex(Pair<Integer, Integer> vertex) {
	        this.vertices.add(vertex);
	    }

	    public Pair<Integer, Integer> getPosition() {
	        return position;
	    }

	    public ArrayList<Pair<Integer, Integer>> getVertices() {
	        return vertices;
	    }
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Expand Apexes");
		frame.setSize(new Dimension(1000, 800));
		RoateShape test = new RoateShape();
		frame.add(test);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
