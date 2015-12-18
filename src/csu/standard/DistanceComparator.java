package csu.standard;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * A comparator that sorts entities by distance to a reference point.
 */
public class DistanceComparator implements Comparator<StandardEntity> {
	private StandardEntity reference;
	private StandardWorldModel world;

	/**
	 * Create a DistanceSorter.
	 * 
	 * @param reference
	 *            The reference point to measure distances from.
	 * @param world
	 *            The world model.
	 */
	public DistanceComparator(StandardEntity reference, StandardWorldModel world) {
		this.reference = reference;
		this.world = world;
	}

	/**
	 * Compares the standard entities according to distance.
	 * 
	 * @param a
	 *            First StandardEntity to compare
	 * @param b
	 *            Second StandardEntity to compare
	 * @return The difference between distances.
	 */

	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		int d1 = world.getDistance(reference, a);
		int d2 = world.getDistance(reference, b);
		return d1 - d2;
	}
}
