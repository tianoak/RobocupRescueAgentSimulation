package csu.util;

import java.util.Comparator;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   A comparator that sorts entities by distance to a reference point.
   	一个比较器,各种实体的一个参考点的距离。
*/
public class DistanceSorter implements Comparator<StandardEntity> {
    
	private final StandardEntity reference;
    private final StandardWorldModel world;
	private final OrderBy order;
	
    /**
       Create a DistanceSorter.
                   创建一个DistanceSorter。
       @param reference The reference point to measure distances from.
       	参考参考点来测量距离。
       @param world The world model.
    */
    public DistanceSorter(StandardEntity reference, StandardWorldModel world, OrderBy order) {
    	this.reference = reference;
    	this.world = world;
    	this.order = order;
    }
    //构造器
    public DistanceSorter(StandardEntity reference, StandardWorldModel world) {
        this(reference, world, OrderBy.ASC);
    }
    //比较两个实体的一个参考点的距离。
    @Override
    public int compare(StandardEntity a, StandardEntity b) {
        int d1 = world.getDistance(reference, a);
        int d2 = world.getDistance(reference, b);
        return (d1 - d2) * order.by();
    }
}

