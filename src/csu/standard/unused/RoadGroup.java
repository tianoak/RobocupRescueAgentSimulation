package csu.standard.unused;
//package csu.standard;
//
//import java.util.List;
//
//import rescuecore2.standard.entities.Road;
//import rescuecore2.worldmodel.EntityID;
//
//
//public class RoadGroup {
//    List<Road> roads;
//
//    public RoadGroup(List<Road> roads) {
//        this.roads = roads;
//    }
//
//    public List<Road> getRoads() {
//        return roads;
//    }
//
//    public void setRoads(List<Road> roads) {
//        this.roads = roads;
//    }
//
//    public boolean contains(Road road){
//        return roads.contains(road);
//    }
//    /**
//     *
//     * @param groups
//     * @param road
//     * @return
//     */
//
//    public static boolean contains(List<RoadGroup> groups, Road road){
//        for(RoadGroup group : groups){
//            if(group.contains(road)){
//                return true;
//            }
//        }
//        return false;
//    }
//    /**
//     *
//     * @return
//     */
//
//    public EntityID getHead(){
//        if(roads == null || roads.size() == 0){
//            return null;
//        }
//        if(roads.size() == 1){
//            return roads.get(0).getNeighbours().get(0);
//        }
//        Road first = roads.get(0);
//        Road second = roads.get(1);
//        EntityID fh = first.getNeighbours().get(0);
//        EntityID ft = first.getNeighbours().get(1);
//        EntityID sh = second.getNeighbours().get(0);
//        EntityID st = second.getNeighbours().get(1);
//        if(!fh.equals(sh) && !fh.equals(st)){
//            return fh;
//        }else{
//            return ft;
//        }
//    }
//    /**
//     *
//     * @return
//     */
//    public EntityID getTail(){
//        if(roads == null || roads.size() == 0){
//            return null;
//        }
//        if(roads.size() == 1){
//            return roads.get(0).getNeighbours().get(1);
//        }
//        Road first = roads.get(roads.size()-1);
//        Road second = roads.get(roads.size()-2);
//        EntityID fh = first.getNeighbours().get(0);
//        EntityID ft = first.getNeighbours().get(1);
//        EntityID sh = second.getNeighbours().get(0);
//        EntityID st = second.getNeighbours().get(1);
//        if(!ft.equals(sh) && !ft.equals(st)){
//            return ft;
//        }else{
//            return fh;
//        }
//    }
//    /**
//     *
//     * @return
//     */
//
//    @Override
//    public String toString(){
//        String s = "RoadGroup: ";
//        boolean first = true;
//        for(Road road : roads){
//            if(first){
//                first = false;
//            }else{
//                s+=",";
//            }
//            s+=road.getID().getValue();
//        }
//        return s;
//    }
//}
