package csu.util;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;

public class IdSorter implements Comparator<StandardEntity> {
	
	/** Determines whether ordered by &nbsp;<code>ASC</code> or &nbsp;<code>DESC</code> */
	private final OrderBy order;

	/**
	 * Standard Constructor.
	 * 
	 * @param order &nbsp;&nbsp;ordered by &nbsp;<code>ASC</code> or &nbsp;<code>DESC</code>.
	 */
	public IdSorter(OrderBy order) {
		this.order = order;
	}
	
	/**
	 * Default constructor. And the order is &nbsp;<code>ASC</code>.
	 */
	public IdSorter() {
		this.order = OrderBy.ASC;
	}

	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		return (a.getID().getValue() - b.getID().getValue()) * order.by();
	}
}
