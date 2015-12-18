package csu.agent.fb;

import java.util.EnumSet;
import csu.agent.CentreAgent;
import csu.common.TimeOutException;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;

public class FireStationAgent extends CentreAgent<FireStation> {

//	private ExtinguishBehaviorType extinguishBehaviorType = ExtinguishBehaviorType.CLUSTER_BASED;
//	private ExtinguishBehavior_Interface extinguishBehavior;
//	
//	private TargetSelectorType targetSelectorType = TargetSelectorType.DIRECTION_BASED;
//	private FireBrigadeTargetSelector_Interface targetSelector;
//	
//	private Map<EntityID, FireBrigadeTarget> agentTargetMap = new FastMap<>();
//	private Map<EntityID, EntityID> locationToGoMap;
//	private Map<EntityID, Pair<EntityID, Integer>> buildingToExtinguishMap;
	
	@Override
	protected StandardWorldModel createWorldModel() {
		return new FireBrigadeWorld();
	}
	
	@Override
	protected void initialize() {
		super.initialize();
//		this.setTargetSelector();
//		this.setExtinguishBehavior();
		System.out.println(toString()+" is connected. [id="+getID()+"]");
	}

	@Override
	protected void prepareForAct() throws TimeOutException{
		super.prepareForAct();
	}

	@Override
	protected void act() throws ActionCommandException, TimeOutException {
//		agentTargetMap.clear();
//		if (isTimeToRefreshEstimator()) {
//			System.out.println("In time: " + world.getTime() + " Fire Station: " + world.getControlledEntity() 
//					+ " refreshed estimator");
//			FbUtilities.refreshFireEstimator(world);
//		}
//		
//		for (FireBrigade next : getFreeFireBrigade()) {
//			agentTargetMap.put(next.getID(), targetSelector.selectTarget(next));
//		}
//		System.out.println("In time: " + world.getTime() 
//				+ " Agent: " + world.getControlledEntity() + " ----- agentTargetMap: " + agentTargetMap);
//		 
//		extinguishBehavior.extinguish((FireBrigadeWorld)world, agentTargetMap);
//		this.locationToGoMap = extinguishBehavior.getLocationToGoMap();
//		this.buildingToExtinguishMap = extinguishBehavior.getBuildingToExtinguishMap();
//		System.out.println("In time: " + world.getTime() + " Agent: " + world.getControlledEntity() 
//				+ " ----- buildingToExtinguishMap: "+ buildingToExtinguishMap);
	}

	@Override
	protected void afterAct() {
		super.afterAct();
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.FIRE_STATION);
	}

	@Override
	public String toString() {
		return "CSU_YUNLU fire stasion agent";
	}
	
//	private boolean isTimeToRefreshEstimator() {
//		if (world.getTime() > 120 && world.getTime() % 30 == 0)
//			return true;
//		return false;
//	}
//	
//	private void setTargetSelector() {
//		switch (this.targetSelectorType) {
//		case DIRECTION_BASED:
//			targetSelector = new DirectionBasedTargetSelector((FireBrigadeWorld)world);
//			break;
//
//		default:
//			targetSelector = new DirectionBasedTargetSelector((FireBrigadeWorld)world);
//			break;
//		}
//	}
//	
//	private void setExtinguishBehavior() {
//		switch (this.extinguishBehaviorType) {
//		case CLUSTER_BASED:
//			extinguishBehavior = new DirectionBasedExtinguishBehavior((FireBrigadeWorld)world);
//			break;
//		case MUTUAL_LOCATION:
//			extinguishBehavior = new MutualLocationExtinguishBehavior();
//			break;
//		default:
//			extinguishBehavior = new DirectionBasedExtinguishBehavior((FireBrigadeWorld)world);
//			break;
//		}
//	}
	
//	private List<FireBrigade> getFreeFireBrigade() {
//		List<FireBrigade> result = new ArrayList<>();
//		System.out.println(world.getAgentStateMap());
//		for (EntityID next : world.getFireBrigadeIdList()) {
//			AgentState state = world.getAgentStateMap().get(next);
//			switch (state) {
//			case NORMAL:
//			case SEARCH:
//				result.add(world.getEntity(next, FireBrigade.class));
//				break;
//			case RESTING:
//			case BURIED:
//			case DEAD:
//			case STUCK:
//			default:
//				break;
//			}
//		}
//		System.out.println("In time: " + world.getTime() + " Fire Station: "
//				+ world.getControlledEntity() + " get free FB: " + result);
//		return result;
//	}
	
//	private final int fbNumberBitSize = BitUtil.needBitSize(50);
//	@Override
//	public Port createMoveTaskPort(final CommunicationUtil comUtil, final int timeToLive) {
//		return new Port() {
//			
//			private List<MessageBitSection> packetList = new ArrayList<>();
//			private int counter = 0; 
//			
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				stream.readBit(comUtil.AGENT_UNIFORM_BIT_SIZE);
////				EntityID fbId = world.getUniform().toID(StandardEntityURN.FIRE_BRIGADE, fbUniform);
//				final int mark = stream.readBit(1);
////				final int targetUniform;
////				EntityID targetId;
//				if (mark == 0) {
//					stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
////					targetId = world.getUniform().toID(StandardEntityURN.BUILDING, targetUniform);
//				} else {
//					stream.readBit(comUtil.ROAD_UNIFORM_BIT_SIZE);
////					targetId = world.getUniform().toID(StandardEntityURN.ROAD, targetUniform);
//				}
//			}
//			
//			@Override
//			public MessageBitSection next() {
//				MessageBitSection next = packetList.get(counter);
//				next.decrementTTL();
//				counter++;
//				return next;
//			}
//			
//			@Override
//			public void init(ChangeSet changed) {
//				MessageBitSection newPacket = createNewPacket();
//				if (newPacket != null)
//					packetList.add(newPacket);
//				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
//					if (it.next().getTimeToLive() <= 0)
//						it.remove();
//			}
//			
//			private MessageBitSection createNewPacket() {
//				if (locationToGoMap == null || locationToGoMap.isEmpty())
//					return null;
//				MessageBitSection sec = new MessageBitSection(1);
//				sec.setTimeToLive(timeToLive);
//				StandardEntity location;
//				EntityID locationId;
//				
//				final int n = locationToGoMap.size();
//				sec.add(n, fbNumberBitSize);
//				for (EntityID next : locationToGoMap.keySet()) {
//					int fbUniform = world.getUniform().toUniform(next);
//					sec.add(fbUniform, comUtil.AGENT_UNIFORM_BIT_SIZE);
//					
//					locationId = locationToGoMap.get(next);
//					location = world.getEntity(locationId);
//					int targetUniform = world.getUniform().toUniform(locationToGoMap.get(next));
//					// let 0 to mark Building and 1 to mark Road
//					if (location instanceof Building) {
//						sec.add(0, 1);
//						sec.add(targetUniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					} else if (location instanceof Road){
//						sec.add(1, 1);
//						sec.add(targetUniform, comUtil.ROAD_UNIFORM_BIT_SIZE);
//					}
//				}
//				return sec;
//			}
//			
//			@Override
//			public boolean hasNext() {
//				if (counter < packetList.size())
//					return true;
//				else
//					return false;
//			}
//			
//			@Override
//			public MessageReportedType getMessageReportedType() {
//				return MessageReportedType.REPORTED_TO_FB;
//			}
//		};
//	}
//	
//	@Override
//	public Port createExtinguishTaskPort(final CommunicationUtil comUtil, final int timeToLive) {
//		return new Port() {
//			
//			private List<MessageBitSection> packetList = new ArrayList<>();
//			private int counter = 0; 
//			
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				final int n = stream.readBit(fbNumberBitSize);
//				for (int i = 0; i < n; i++) {
//					stream.readBit(comUtil.AGENT_UNIFORM_BIT_SIZE);
////					EntityID fbId = world.getUniform().toID(StandardEntityURN.FIRE_BRIGADE, fbUniform);
//					stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
////					EntityID targetId = world.getUniform().toID(StandardEntityURN.BUILDING, targetUniform);
//					stream.readBit(comUtil.WATER_POWER_BIT_SIZE);
//				}
//			}
//			
//			@Override
//			public MessageBitSection next() {
//				MessageBitSection next = packetList.get(counter);
//				next.decrementTTL();
//				counter++;
//				return next;
//			}
//			
//			@Override
//			public void init(ChangeSet changed) {
//				MessageBitSection newPacket = createNewPacket();
//				if (newPacket != null)
//					packetList.add(newPacket);
//				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
//					if (it.next().getTimeToLive() <= 0)
//						it.remove();
//			}
//			
//			private MessageBitSection createNewPacket() {
//				if (buildingToExtinguishMap == null 
//						|| buildingToExtinguishMap.isEmpty())
//					return null;
//				MessageBitSection sec = new MessageBitSection(1);
//				sec.setTimeToLive(timeToLive);
//				final int n = buildingToExtinguishMap.size();
//				sec.add(n, fbNumberBitSize);
//				for (EntityID next : buildingToExtinguishMap.keySet()) {
//					int fbUniform = world.getUniform().toUniform(next);
//					int targetUniform = world.getUniform().toUniform(buildingToExtinguishMap.get(next).first());
//					int water = buildingToExtinguishMap.get(next).second();
//					sec.add(fbUniform, comUtil.AGENT_UNIFORM_BIT_SIZE);
//					sec.add(targetUniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					sec.add(water, comUtil.WATER_POWER_BIT_SIZE);
//				}
//				return sec;
//			}
//			
//			@Override
//			public boolean hasNext() {
//				if (counter < packetList.size())
//					return true;
//				else
//					return false;
//			}
//			
//			@Override
//			public MessageReportedType getMessageReportedType() {
//				return MessageReportedType.REPORTED_TO_FB;
//			}
//		};
//	}
}
