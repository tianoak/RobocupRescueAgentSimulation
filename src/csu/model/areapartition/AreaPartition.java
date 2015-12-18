//package csu.model.areapartition;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import csu.communication.CommunicationUtil;
//import csu.communication.MessageBitSection;
//import csu.communication.MessageConstant.MessageReportedType;
//import csu.communication.Port;
//import csu.io.BitArrayInputStream;
//import csu.model.AdvancedWorldModel;
//import csu.model.areapartition.PartedArea.AreaStatus;
//import csu.util.BitUtil;
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
//import rescuecore2.standard.entities.StandardEntityURN;
//import rescuecore2.worldmodel.ChangeSet;
//import rescuecore2.worldmodel.EntityID;
//
//@SuppressWarnings("serial")
//public class AreaPartition extends ArrayList<PartedArea> {
//
//	public  AreaPartition(AreaPartitionBuilder apb) {
//		this.addAll(apb.getAreaPartitionResult());
//	}
//	private ArrayList<PartedArea> needSendPartition = new ArrayList<PartedArea>();
//	//AreaPartition谁又近了更新，接近的话True
//	public void update(AdvancedWorldModel world, ChangeSet change) {
//		needSendPartition.clear();		HashSet<EntityID> BurnedoutBuildings = new HashSet<EntityID>();
//		for(StandardEntity se : world.getEntitiesOfType(StandardEntityURN.BUILDING)){
//			Building bld = (Building) se;
//			if(bld.isFierynessDefined()){
//				if(bld.getFierynessEnum().equals(Fieryness.BURNT_OUT)){
//					BurnedoutBuildings.add(se.getID());
//				}
//			}
//		}
//		for(PartedArea pa : this){
//			if(pa.isReached()){}
//			else{
//				for(EntityID id : pa.getContentBuildingsID()){
//					if(!pa.isReached() && change.getChangedEntities().contains(id)) {
//						pa.reached();
//						pa.approached();
//						break;
//					}
//					if(!pa.isApproached() ){//&& world.getLastChangedTime(id)!=-1){
//						pa.approached();
//					}
//				}
//			}
//			//TODO status的更新 调整
//			//看不见的话，通
////			if (!pa.containAnyID(world.getVisibleEntitiesID())) {
////				continue;
////			}
//			ArrayList<Building> burings = new ArrayList<Building>(world.getBurningBuildings());
//			AreaStatus as = pa.getStatus();
//			if (pa.containAny(burings)) {
//				as = AreaStatus.burning;
//			}
//			else if (!pa.getStatus().equals(AreaStatus.unburn)) {
//				as = AreaStatus.extinguished;
//			}
//			if (BurnedoutBuildings.containsAll(pa.getContentBuildingsID())) {
//				as = AreaStatus.burnedout;
//			}
//			// +unburn, ignition, catching, +burning, inferno, +extinguished,
//			// +burnedout;
//			if (!as.equals(pa.getStatus())) {
//				pa.setStatus(as);
//			}
//
//			if (pa.isStateChanged()) {
//				needSendPartition.add(pa);
//			}
//		}
//	}
//
//	public Port createPort(CommunicationUtil comUtil) {
//		return new Port() {
//
//			@Override
//			public void init(ChangeSet changed) {
//
//			}
//
//			@Override
//			public MessageBitSection next() {
//				MessageBitSection sec = new MessageBitSection(100);
//				PartedArea pa = needSendPartition.get(0);
//				sec.add((pa.getId()), BitUtil.needBitSize(size()));
//				sec.add(pa.getStatus().ordinal(), BitUtil.needBitSize(AreaStatus.values().length));
//				needSendPartition.remove(0);
//				return sec;
//			}
//
//
//			@Override
//			public boolean hasNext() {
//				return !needSendPartition.isEmpty();
//			}
//
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				int id =stream.readBit(BitUtil.needBitSize(size()));
//				int state = stream.readBit(BitUtil.needBitSize(AreaStatus.values().length));
//				//get(id).setStatus(AreaStatus.values()[state]);
//				System.out.println("bitlength :"+BitUtil.needBitSize(size())+" "+AreaStatus.values().length +"time :"+time +" read id:"+ id +" state "+ AreaStatus.values()[state]);
//			}
//
//			@Override
//			public MessageReportedType getMessageReportedType() {
//				return MessageReportedType.REPORTRD_TO_ALL;
//			}
//		};
//	}
//
//
//
//}
