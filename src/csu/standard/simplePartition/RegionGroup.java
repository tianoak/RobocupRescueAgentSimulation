package csu.standard.simplePartition;

import java.util.ArrayList;
import java.util.List;

import rescuecore2.standard.entities.StandardEntity;


public class RegionGroup {
    List<EntityRegion> regions_;
    
    /**==========================有什么用===============================*/
    int totalSize_;
    
    /**==========================有什么用===============================*/
    int no_;

    public RegionGroup(int no){
        regions_ = new ArrayList<EntityRegion>();
        no_ = no;
    }

    public List<EntityRegion> getRegions() {
    	//System.out.println("regions_"+regions_);
        return regions_;
    	}

    public void setRegions(List<EntityRegion> regions) {
        this.regions_ = regions;
    }

    public int getTotalSize() {
        return totalSize_;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize_ = totalSize;
    }

    public int getNo(){
        return no_;
    }

    void addRegion(EntityRegion region) {
        regions_.add(region);
        region.setRegionGroup(this);
    }

    public boolean contains(StandardEntity entity){
        for(EntityRegion region : regions_){
            if(region.contains(entity)){
                return true;
            }
        }
        return false;
    }


}
