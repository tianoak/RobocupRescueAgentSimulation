package csu.standard;

import java.util.Comparator;

import rescuecore2.worldmodel.EntityID;

/**
 * EntityID的比较器，用来比较两个ID的大小
 * @author nale
 *
 */
public class EntityIdComparator implements Comparator<EntityID> {

    public EntityIdComparator() {
    }
    
    @Override
    public int compare(EntityID a, EntityID b) {
        int s1 = a.getValue();
        int s2 = b.getValue();
        return s1 - s2;
    }
}
