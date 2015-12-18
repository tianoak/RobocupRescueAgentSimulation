package csu.model.route.pov;

import java.awt.Point;

import csu.model.route.pov.graph.PointNode;

/**
 * The cost from current node to its successor node.
 */
public interface CostFunction {

	double cost(PointNode from, PointNode to, Point startPoint);
}
