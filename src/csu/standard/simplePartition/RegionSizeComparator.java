package csu.standard.simplePartition;

import java.util.Comparator;

public class RegionSizeComparator implements Comparator<EntityRegion> {

    public RegionSizeComparator() {
    }
    
    @Override
    public int compare(EntityRegion a, EntityRegion b) {
        int s1 = a.getAssignCost();
        int s2 = b.getAssignCost();
        return s1 - s2;
    }
}
