package csu.util;

/**
 * Representation of Sorter's sort benchmark.
 * 
 * @author CSU --- Appreciation
 *
 */
public enum OrderBy {
	/** Ascending order. */
	ASC(1),	
	
	/** Descending order. */
	DESC(-1);
	
	/** Sorter's sort benchmark. */
	private int order;
	
	// constructor
	private OrderBy(int order) {
		this.order = order;
	}
	
	/** Returns Sorter's sort benchmark. */
	public int by() {
		return order;
	}
}
