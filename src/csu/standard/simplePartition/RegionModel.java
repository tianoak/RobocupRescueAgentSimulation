package csu.standard.simplePartition;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csu.model.AdvancedWorldModel;

import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

public class RegionModel {

    //private static final Logger LOG = Logger.getLogger(RegionModel.class);
    public static final int DEFAULT_LINE_COUNT  = 1;
    public static final int DEFAULT_REGION_COLUMN_COUNT  = 6;
    public static final int DEFAULT_TEAM  = 6;
    AdvancedWorldModel model;
    EntityRegion[][] regions_;
    List<RegionGroup> regionGroups_;
    Map<RegionGroup, EntityID> regionAssignements_;
    GroupingType groupingType_;

    public RegionModel(AdvancedWorldModel model, GroupingType type) {
        int teamSize;

        this.model = model;
        groupingType_ = type;
        teamSize = 25;
        this.regions_ = calculateRegions(teamSize);
        regionAssignements_ = new HashMap<RegionGroup, EntityID>();
        //LOG.debug("Regions are found: " + regions_);
    }
    
    public int getExpectedRegionCount(int teamSize){
        int n;

        n = (int) Math.ceil(Math.sqrt(teamSize) + 1);
        return n;
    }

    public EntityRegion[][] calculateRegions(int teamSize){
        int n;
        EntityRegion[][] regions;
        n = getExpectedRegionCount(teamSize);
        regions = calculateRegions(n, n);
        return regions;
    }

    //calculate regions;return the groups;
    public List<RegionGroup> calculateRegionGroups(int teamSize, GroupingType type){
       List<RegionGroup> groups;
       int c, totalCost, averageCost;

       groups = new ArrayList<RegionGroup>();
       for(int i = 0; i < teamSize; i++){
           groups.add(new RegionGroup(i));
       }
       c = 0;
       totalCost = 0;
       for(EntityRegion[] regionRow : regions_){ 
           for(EntityRegion region : regionRow){ 
               int cost;
               cost = region.calculateAssignCost(type); 
               totalCost += cost;
           }
       }
       averageCost = totalCost / teamSize; 
       totalCost = 0;
       
       for(EntityRegion[] regionRow : regions_){
           for(EntityRegion region : regionRow){
               RegionGroup group;
               group = groups.get(c);
               group.addRegion(region);
               totalCost += region.getAssignCost();
               if(totalCost >= averageCost * (c + 1)){ 
                   if(c < teamSize - 1){
                        c++;
                   }
               }
           }
       }
       return groups;
    }

    //calculate regions;get regions info:row,column,entities(building,refuge,all center)
    public EntityRegion[][] calculateRegions(int row, int column){
        EntityRegion[][] regions;
        Collection<StandardEntity> entities;
        int n;

        regions = new EntityRegion[row][column];
        n = 0;
        for(int r = 0; r < row; r++){
            for(int c = 0; c < column; c++){
                EntityRegion region;

                region = new EntityRegion(n);
                region.setRow(r);
                region.setColumn(c);
                regions[r][c] = region;
                n++;
            }
        }
        entities = model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.ROAD, StandardEntityURN.REFUGE, StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE, StandardEntityURN.FIRE_STATION,
    			StandardEntityURN.GAS_STATION,StandardEntityURN.HYDRANT);
        for(StandardEntity entity : entities){
            Point point;
            int c, r;

            point = Locator.getPosition(entity, model);
            c = getColumn((int)model.getBounds().getMinX(), (int)model.getBounds().getWidth(), column, point.x); //to determine which column the point is.
            r = getColumn((int)model.getBounds().getMinY(), (int)model.getBounds().getHeight(), row, point.y);   //to determine which row the point is.
            if(r >= 0 && c >= 0 && r < regions.length && c < regions[r].length){
                regions[r][c].add(entity);
            }
        }
        return regions;//regions info:row,column,entities
    }

    protected int getColumn(int left, int width, int columnCount, int value){
        return (value - left) * columnCount/ width;
    }

    public List<EntityRegion> calculateRegionsOld(){

        List<EntityRegion> regionList = new ArrayList<EntityRegion>();
        Collection<StandardEntity> temp = model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.ROAD);
        List<StandardEntity> entities = new ArrayList<StandardEntity>(temp);
        List<Line> lines = getGridLines(DEFAULT_LINE_COUNT);
        //List<Line> lines = model.getRoadModel().getMainRoadLines();
        List<String> lineStrings = getLineStrings(entities, lines);
        List<String> ordered = new ArrayList<String>();
        for(int i = 0; i < entities.size(); i++){
            String s = lineStrings.get(i);
            if(!ordered.contains(s)){
                int n = ordered.size();
                ordered.add(s);
                regionList.add(new EntityRegion(n));
            }
            int region = ordered.indexOf(s);
            StandardEntity entity = entities.get(i);
            regionList.get(region).add(entity);
        }
        Collections.sort(regionList, Collections.reverseOrder(new RegionSizeComparator()));
        return regionList;
    }
    
    public EntityRegion[][] getRegions() {
        return regions_;
    }

    public void setRegions(EntityRegion[][] regions) {
        this.regions_ = regions;
    }

    public List<EntityRegion> getAllTeamRegions(List<EntityID> entities){
    	if(entities == null){
    		return null;
    	}
    	List<EntityRegion> teamRegions = new ArrayList<EntityRegion>();
    	for(EntityID entity : entities){
    		RegionGroup regionGroup = getAssignedRegions(entity);
    		List<EntityRegion> entityRegions = regionGroup.getRegions();
    		for(EntityRegion entityRegion : entityRegions){
    			if(entityRegion != null){
    				teamRegions.add(entityRegion);
    			}
    		}
    	}
    	return teamRegions;
    }

    protected List<Line> getDiagonalLines(){
        Rectangle2D bounds = model.getBounds();
        List<Line> lines = new ArrayList<Line>();
        lines.add(new Line(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()));
        lines.add(new Line(bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMinY()));
        return lines;
    }

    protected List<Line> getGridLines(int n){
        Rectangle2D bounds = model.getBounds();
        List<Line> lines = new ArrayList<Line>();
        double h = bounds.getHeight();
        double w = bounds.getWidth();
        double dx = w / (n+1);
        double dy = h / (n+1);
        for(int i = 0; i < n; i++){
            lines.add(new Line(bounds.getMinX(), bounds.getMinY() + (i+1)*dy , bounds.getMaxX(), bounds.getMinY() + (i+1)*dy));
            lines.add(new Line(bounds.getMinX()  + (i+1)*dx, bounds.getMinY(), bounds.getMinX()  + (i+1)*dx, bounds.getMaxY()));
        }
        return lines;
    }

    protected <T extends StandardEntity> List<String> getLineStrings(List<T> entities, List<Line> lines){
        List<String> strings = new ArrayList<String>();
        for(StandardEntity entity : entities){
            strings.add(getLineString(entity, lines));
        }
        return strings;
    }

     protected String getLineString(StandardEntity entity, List<Line> lines){
        String s = "";
        int x,y;
        if(entity instanceof Area){
            Area building = (Area) entity;
            x = building.getX();
            y = building.getY();
        }else{
            return "";
        }
        for(Line line : lines){
            if(line.isOnLeft(x, y)){
                s+=Direction.West.toChar();
            }else{
                s+=Direction.East.toChar();
            }
        }
        return s;
    }

    protected int getTotalBuildingArea(){
        List<Building> buildings = model.getEntitiesOfType(Building.class, StandardEntityURN.BUILDING);
        int totalArea = 0;
        for(Building building : buildings){
            totalArea += building.getTotalArea();
        }
        return totalArea;
    }

    protected List<Building> getBuildings(){
        return model.getEntitiesOfType(Building.class, StandardEntityURN.BUILDING);
    }

    public EntityRegion getRegion(StandardEntity entity){

        for(EntityRegion[] regionRow : regions_){
            for(EntityRegion region : regionRow){
                if(region.contains(entity)){
                    return region;
                }
            }
        }
        return null;
    }

    public RegionGroup getRegionGroup(StandardEntity entity){
        EntityRegion region;
        RegionGroup group;

        region = getRegion(entity);
        if(region == null){
            return null;
        }
        group = region.getRegionGroup();
        return group;
    }

    public int getRegionNo(StandardEntity entity){
        EntityRegion region = getRegion(entity);
        if(region == null){
            return -1;
        }else{
            return region.getNo();
        }
    }

    public EntityRegion getSmallest(List<EntityRegion> regions, int limit){
        List<EntityRegion> temp = new ArrayList<EntityRegion>();
        for(int i = 0; i < limit; i++){
            if(i < regions.size()){
                temp.add(regions.get(i));
            }
        }
        Comparator<EntityRegion> comparator = Collections.reverseOrder(new RegionSizeComparator());
        Collections.sort(temp, comparator);
        return temp.get(0);
    }

    public void assignRegionsToTeam(){
    	if (model.me instanceof PoliceForce)
    		assignRegionsToEntities(model.getPoliceForceIdList());
    	else if (model.me instanceof FireBrigade)
    		assignRegionsToEntities(model.getFireBrigadeIdList());
    	else if (model.me instanceof AmbulanceTeam)
    		assignRegionsToEntities(model.getAmbulanceTeamIdList());
    }

    public void assignRegionsToEntities(List<EntityID> entities){
        int i=0;
        if(regionGroups_ == null){
            regionGroups_ = calculateRegionGroups(entities.size(), groupingType_);
        }

        for(RegionGroup group  : regionGroups_){
                if(i < entities.size()){
                    EntityID id = entities.get(i%entities.size());
                    regionAssignements_.put(group, id);
                    i++;
                }
        }
    }

    public RegionGroup getAssignedRegions(EntityID agentId){
        for(RegionGroup regionGroup: regionAssignements_.keySet()){
            EntityID id = regionAssignements_.get(regionGroup);
            if(id.equals(agentId)){
                return regionGroup;
            }
        }
        return null;
    }

    public List<EntityRegion> getNeighbors(EntityRegion region, int level){
        int column, row, minC, minR, maxC, maxR;
        List<EntityRegion> regions;

        column = region.getColumn();
        row = region.getRow();
        minC = Math.max(0, column - level);
        minR = Math.max(0, row - level);
        maxC = Math.min(regions_[0].length-1, column + level);
        maxR = Math.min(regions_.length-1, row + level);
        regions = new ArrayList<EntityRegion>();
        for(int r = minR; r <= maxR; r++){
            for(int c = minC; c <= maxC; c++){
                if(r != row || c != column){
                    regions.add(regions_[r][c]);
                }
            }
        }
        return regions;
    }

    public Set<RegionGroup> getNeighbors(RegionGroup group, int level){
        Set<RegionGroup> groups;

        groups = new HashSet<RegionGroup>();
        for(EntityRegion region : group.getRegions()){
            List<EntityRegion> neighborRegions;

            neighborRegions = getNeighbors(region, level);
            for(EntityRegion neighborRegion : neighborRegions){
                RegionGroup neighborGroup;

                neighborGroup = neighborRegion.getRegionGroup();
                if(!neighborGroup.equals(group)){
                    groups.add(neighborGroup);
                }
            }
        }
        return groups;
    }

}
