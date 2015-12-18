package csu.standard.simplePartition;

import java.util.ArrayList;
import java.util.List;

import csu.model.AdvancedWorldModel;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;

public class EntityRegion {
    //private static final Log LOG = LogFactory.getLog(EntityRegion.class);
	
	/**==========================有什么用===============================*/
    int no;
    
    List<Building> buildings;
    List<Road> roads_;
    
    /**==========================有什么用===============================*/
    int row_;
    
    /**==========================有什么用===============================*/
    int column_;
    
    RegionGroup regionGroup_;
    int assignCost_;

    
    public EntityRegion(int no) {
        this(no,new ArrayList<Building>(), new ArrayList<Road>());
    }
    /**
     * Construct a region.
     * @param no Region number.
     * @param buildings List of buildings.
     * @param nodes List of nodes.
     */

    public EntityRegion(int no, List<Building> buildings, List<Road> roads) {
        this.no = no;
        this.buildings = buildings;
        roads_ = roads;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public List<Road> getRoads() {
        return roads_;
    }

    public void setRoads(List<Road> roads) {
        this.roads_ = roads;
    }


    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }
    /**
     * Adds a StandardEntity to this region.
     * @param entity Given StandardEntity for the region.
     */

    public void add(StandardEntity entity){
        if(entity instanceof Building){
            buildings.add((Building) entity);
        }
        else if(entity instanceof Road){
            roads_.add((Road) entity);
        }else{
            //LOG.warn("Unexpected entity type: " + entity);
        }
    }
    /**
     * Checks whether given entity is in this EntityRegion or not.
     * @param entity Given StandardEntity.
     * @return Returns true if the given entity is in this EntityRegion.
     */

    public boolean contains(StandardEntity entity){
        if(entity instanceof Building){
            return buildings.contains((Building)entity);
        }else if(entity instanceof Road){
            return roads_.contains((Road)entity);
        }else{
            //LOG.warn("Unexpected entity type at contains: " + entity);
            return false;
        }
    }

    /**获取世界模型中的中心点<p>
     * 方法：通过遍历所有的building和road，得到最小的横纵坐标，和最大的横纵坐标，两者去均值就得到中点的坐标
     * */
    public StandardPoint<Integer> getCenter(AdvancedWorldModel model){//标准点
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;
        boolean isFirst = true;
        List<Area> entities;

        entities = new ArrayList<Area>();
        entities.addAll(buildings);
        entities.addAll(roads_);
        for(Area area : entities){
            Pair<Integer,Integer> location = area.getLocation(model);
            int x = location.first();
            int y = location.second();

			if (isFirst) {
				minX = x;
				minY = y;
				maxX = x;
				maxY = y;
				isFirst = false;
			} else {
                minX = Math.min(x, minX);
                maxX = Math.max(x, maxX);
                minY = Math.min(y, minY);
                maxY = Math.max(y, maxY);
            }
        }
        int x = (minX + maxX) / 2;
        int y = (minY + maxY) / 2;
        StandardPoint<Integer> center = new StandardPoint<Integer>(x,y);
        return center;
    }

    /**========================What is the cost===============================*/
    public int calculateAssignCost(GroupingType type){
        int cost;
        cost = 0;
        if(type == GroupingType.BuildingCount){//other agents
            cost = buildings.size();//建筑物数量
        }else if(type == GroupingType.RoadCount){//POLICE_FORCE
            cost = roads_.size();//道路数量
        }else if(type == GroupingType.TotalBuildingArea){//针对FIRE_BRIGADE
            for(Building building : buildings){
                cost += building.getTotalArea();
            }
        }
        assignCost_ = cost;
        return cost;
    }

    @Override
    public String toString() {
        return "Region#" + no;
    }

    public int getColumn() {
        return column_;
    }

    public void setColumn(int column) {
        this.column_ = column;
    }

    public int getRow() {
        return row_;
    }

    public void setRow(int row) {
        this.row_ = row;
    }

    public RegionGroup getRegionGroup() {
        return regionGroup_;
    }

    public void setRegionGroup(RegionGroup regionGroup) {
        this.regionGroup_ = regionGroup;
    }

    public int getAssignCost(){
        return assignCost_;
    }


}
