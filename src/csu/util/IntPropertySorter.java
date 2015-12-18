package csu.util;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.properties.IntProperty;

public class IntPropertySorter<E extends StandardEntity> implements Comparator<E> {

	private final OrderBy order;
	private final StandardPropertyURN urn;
	
	public IntPropertySorter(StandardPropertyURN urn, OrderBy order) {
		this.urn = urn;
		this.order = order;
	}
	
	public IntPropertySorter(StandardPropertyURN urn) {
		this(urn, OrderBy.ASC);
	}
	
	@Override
	public int compare(StandardEntity a, StandardEntity b) {
		IntProperty property_a = (IntProperty)a.getProperty(urn.toString());
		IntProperty property_b = (IntProperty)b.getProperty(urn.toString());
		if (!property_a.isDefined()) {
			if (!property_b.isDefined()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (!property_b.isDefined()) {
				return -1;
			}
		}
		return (property_a.getValue() - property_b.getValue()) * order.by();
	}
}
