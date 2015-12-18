//package csu.model;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import csu.communication.CommunicationUtil;
//import csu.communication.MessageBitSection;
//import csu.communication.Port;
//import csu.communication.MessageConstant.MessageReportedType;
//import csu.io.BitArrayInputStream;
//import csu.util.BitUtil;
//
//import rescuecore2.standard.entities.Building;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityURN;
//import rescuecore2.worldmodel.ChangeSet;
//import rescuecore2.worldmodel.EntityID;
//
//@SuppressWarnings("serial")
//public class UnburntBuilding extends HashSet<Building>{
//	/** A list of unburnt building will reported to others.*/
//	private ArrayList<Building> addRecords = new ArrayList<Building>();
//	/** An instance of world model.*/
//	private AdvancedWorldModel world;
//	
//	// constructor
//	public UnburntBuilding(AdvancedWorldModel world) {
//		this.world = world;
//	}
//	
//	/** Update unburnt building in this world model.*/
//	public void update(AdvancedWorldModel world, ChangeSet changed){
//		addRecords.clear();
//		
//		for (EntityID id : changed.getChangedEntities()) {
//			StandardEntity se = world.getEntity(id);
//			if (se instanceof Building) {
//				Building buiding = (Building)se;
//				if (buiding.isFierynessDefined() && buiding.getFieryness() == 0) {
//					if (add(buiding) && buiding.isTemperatureDefined())
//						addRecords.add(buiding);
//				} else  
//					remove(buiding);
//			}
//		}
//	}
//	
//	/** Maximum number of building can reported each time.*/
//	private final int MAX_NUMBER = 31;
//	private final int NUMBER_BIT_SIZE = BitUtil.needBitSize(MAX_NUMBER);
//	
//	public Port createAddPort(final CommunicationUtil comUtil) {
//		return new Port() {
//			private boolean flag;
//			private ChangeSet changed;
//			
//			
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				final int n = stream.readBit(NUMBER_BIT_SIZE);
//				for (int i = 0; i < n; i++) {
//					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
//					final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
//					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
//					if (id == null)
//						continue;
//					if (changed.getChangedEntities().contains(id))
//						continue;
//					
//					Building building = (Building) world.getEntity(id);
//					if (0 <= fireyness && fireyness < 9)
//						building.setFieryness(fireyness);
//					building.setTemperature(temperature);
//					if (temperature > 0)
//						add(building);
//				}
//			}
//			
//			@Override
//			public MessageBitSection next() {
//				this.flag = true;
//				MessageBitSection sec = new MessageBitSection(50);
//				final int n = Math.min(MAX_NUMBER, addRecords.size());
//				sec.add(n, NUMBER_BIT_SIZE);
//				
//				for (int i = 0; i < n; i++) {
//					Building building = addRecords.get(i);
//					int uniform = world.getUniform().toUniform(building.getID());
//					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
//					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
//				}
//				return sec;
//			}
//			
//			@Override
//			public void init(ChangeSet changed) {
//				this.flag = addRecords.isEmpty();
//				this.changed = changed;
//			}
//			
//			@Override
//			public boolean hasNext() {
//				return !flag;
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
//	public Port createNoComPort(final CommunicationUtil comUtil, final boolean isAggregator, final boolean isMessenger) {
//		if (isAggregator) 
//			return new NoComPort(comUtil, 180);
//		if (isMessenger)
//			return new NoComPort(comUtil, 80);
//		else
//			return new NoComPort(comUtil, 3);
//	}
//	
//	private class NoComPort implements Port {
//		private boolean flag;
//		private java.util.List<EntityID> reportedBuilding = new ArrayList<EntityID>();
//		final private CommunicationUtil comUtil;
//		final private int remain;
//		
//		public NoComPort(CommunicationUtil comUtil, int remain) {
//			this.comUtil = comUtil;
//			this.remain = remain;
//		}
//		
//		@Override
//		public void init(ChangeSet changed) {
//			for (Iterator<EntityID> it = reportedBuilding.iterator(); it.hasNext();) {
//				EntityID id = it.next();
//				if (remain < world.getTime() - world.getTimestamp().getLastChangedTime(id)
//						|| world.getUnburntBuildings().contains(id))
//					it.remove();
//			}
//			
//			for (EntityID id : changed.getChangedEntities()) {
//				StandardEntity se = world.getEntity(id);
//				if (se.getStandardURN() == StandardEntityURN.BUILDING) {
//					Building building = (Building) se;
//					if (world.getUnburntBuildings().contains(building))
//						reportedBuilding.add(building.getID());
//				}
//			}
//			flag = reportedBuilding.isEmpty();
//		}
//
//		@Override
//		public boolean hasNext() {
//			return !flag;
//		}
//
//		@Override
//		public MessageBitSection next() {
//			this.flag = true;
//			MessageBitSection sec = new MessageBitSection(50);
//			final int n = Math.max(MAX_NUMBER, reportedBuilding.size());
//			sec.add(n, NUMBER_BIT_SIZE);
//			for (int i = 0; i < n; i++) {
//				Building building = world.getEntity(reportedBuilding.get(i), Building.class);
//				int uniform = world.getUniform().toUniform(building.getID());
//				sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//				sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
//				sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
//				sec.add(world.getTimestamp().getLastChangedTime(building.getID()), comUtil.TIME_BIT_SIZE);
//			}
//			return sec;
//		}
//
//		@Override
//		public void read(EntityID sender, int time, BitArrayInputStream stream) {
//			final int n = stream.readBit(NUMBER_BIT_SIZE);
//			for (int i = 0; i < n; i++) {
//				final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
//				EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
//				final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
//				final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
//				final int messageTime = stream.readBit(comUtil.TIME_BIT_SIZE);
//				if (id == null)
//					continue;
//				if (messageTime < world.getTimestamp().getLastChangedTime(id))
//					continue;
//				
//				Building building = (Building) world.getEntity(id);
//				if (0 <= fireyness && fireyness < 9)
//					building.setFieryness(fireyness);
//				building.setTemperature(temperature);
//				if (temperature > 0)
//					add(building);
//				world.getTimestamp().setLastChangedTime(id, messageTime);
//			}
//		}
//
//		@Override
//		public MessageReportedType getMessageReportedType() {
//			return MessageReportedType.REPORTRD_TO_ALL;
//		}
//	}
//	
//	public boolean contains(Building building) {
//		return super.contains(building);
//	}
//
//	public boolean contains(EntityID id) {
//		return super.contains(world.getEntity(id, Building.class));
//	}
//	
////	
////	public Port createRemovePort(final CommunicationUtil comUtil) {
////		return new Port() {
////			private boolean flag;
////			
////			@Override
////			public void read(EntityID sender, int time, BitArrayInputStream stream) {
////				final int n = stream.readBit(NUMBER_BIT_SIZE);
////				for (int i = 0; i < n; i++) {
////					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
////					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
////					if (id == null)
////						continue;
////					Building building = (Building)world.getEntity(id);
////					remove(building);
////				}
////			}
////			
////			@Override
////			public MessageBitSection next() {
////				this.flag = true;
////				MessageBitSection sec = new MessageBitSection(50);
////				final int n = Math.max(MAX_NUMBER, removeRecords.size());
////				sec.add(n, NUMBER_BIT_SIZE);
////				for (int i = 0; i < n; i++) {
////					Building building = removeRecords.get(i);
////					int uniform = world.getUniform().toUniform(building.getID());
////					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
////				}
////				
////				return sec;
////			}
////			
////			@Override
////			public void init(ChangeSet changed) {
////				this.flag = removeRecords.isEmpty();
////			}
////			
////			@Override
////			public boolean hasNext() {
////				return !flag;
////			}
////			
////			@Override
////			public MessageReportedType getMessageReportedType() {
////				return MessageReportedType.REPORTRD_TO_ALL;
////			}
////		};
////	}
//}
