package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.Port;
import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import csu.model.object.CSUBuilding;
import csu.util.BitUtil;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * Collapsed buildings are buildings whose fireyness is BURNT_OUT.
 * 
 * @author appreciation-csu
 *
 */
@SuppressWarnings("serial")
public class CollapsedBuildings extends HashSet<Building>{
	/** A list of collapsed building will reported to others this cycle.*/
	private ArrayList<Building> addRecoreds = new ArrayList<Building>();
	
	/** An instance of world model.*/
	private AdvancedWorldModel world;
	
	// constructor
	public CollapsedBuildings(AdvancedWorldModel world){
		this.world = world;
	}
	
	/** Update collapsed building in this world model.*/
	public void update(AdvancedWorldModel world, ChangeSet changed) {
		addRecoreds.clear();
		for (EntityID id : changed.getChangedEntities()) {
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if (building.isFierynessDefined() && building.getFierynessEnum() == Fieryness.BURNT_OUT) {
					if (add(building))
						addRecoreds.add(building);
				}
			}
		}
	}
	
	public void remove() {
		for (Iterator<Building> itor = this.iterator(); itor.hasNext(); ) {
			Building building = itor.next();
			if (building.isFierynessDefined() && building.getFieryness() != 8)
				itor.remove();
		}
	}
	
	/** Maximum number of building can reported each time.*/
	private final int MAX_NUMBER = 31;
	private final int NUMBER_BIT_SIZE = BitUtil.needBitSize(MAX_NUMBER);
	
	public Port createAddPort(final CommunicationUtil comUtil, final int timeToLive, final int priority) {
		return new Port() {
			private ChangeSet changed;
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				this.changed = changed;
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}
			
			private MessageBitSection createNewPacket() {
				if (addRecoreds.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(priority);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(addRecoreds.size(), MAX_NUMBER);
				sec.add(n, NUMBER_BIT_SIZE);
				
				for (int i = 0; i < n; i++) {
					Building building = addRecoreds.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
				}
				addRecoreds.clear();
				return sec;
			}
			
			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// next.decrementTTL();
				counter++;
				return next;
				
			}
			
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(NUMBER_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				CSUBuilding csuBuilding;
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					readString = readString + "uniform = " + uniform + "\n";
					final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					readString = readString + "fireyness = " + fireyness + "\n";
					
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					if (id == null)
						continue;
					TimeStamp timestamp = world.getTimestamp();
					if (timestamp.getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS) >= time)
						continue;
					if (changed.getChangedEntities().contains(id))
						continue;
					Building building = world.getEntity(id, Building.class);
					if (0 <= fireyness && fireyness < 9) {
						building.setFieryness(fireyness);
						csuBuilding = world.getCsuBuilding(id);
						csuBuilding.setFuel(0);
					}
					add(building);
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createAddPort, COllapsedBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("uniform = " + dataSizePair.get(i * 2 + 1).first().intValue() + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 2 + 2).first().intValue() + "\n");
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" read in createAddPort, COllapsedBuildings\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	public Port createAddPortNoRadio(final CommunicationUtil comUtil, final int timeToLive, final int priority) {
		return new Port() {
			private ChangeSet changed;
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				this.changed = changed;
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}
			
			private MessageBitSection createNewPacket() {
				if (addRecoreds.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(priority);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(addRecoreds.size(), MAX_NUMBER);
				sec.add(n, NUMBER_BIT_SIZE);
				
				for (int i = 0; i < n; i++) {
					Building building = addRecoreds.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(world.getTime(), comUtil.TIME_BIT_SIZE);
				}
				addRecoreds.clear();
				return sec;
			}
			
			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// next.decrementTTL();
				counter++;
				return next;
				
			}
			
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(NUMBER_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				CSUBuilding csuBuilding;
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					readString = readString + "uniform = " + uniform + "\n";
					final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					readString = readString + "fireyness = " + fireyness + "\n";
					final int findTime = stream.readBit(comUtil.TIME_BIT_SIZE);
					readString = readString + "findTime = " + findTime + "\n";
					
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					if (id == null)
						continue;
					TimeStamp timestamp = world.getTimestamp();
					if (timestamp.getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS) >= findTime)
						continue;
					if (changed.getChangedEntities().contains(id))
						continue;
					Building building = world.getEntity(id, Building.class);
					if (0 <= fireyness && fireyness < 9) {
						building.setFieryness(fireyness);
						csuBuilding = world.getCsuBuilding(id);
						csuBuilding.setFuel(0);
					}
					add(building);
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int numberToSend = dataSizePair.get(0).first().intValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createAddPort, COllapsedBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("uniform = " + dataSizePair.get(i * 3 + 1).first().intValue() + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 3 + 2).first().intValue() + "\n");
						writer.write("findTime = " + dataSizePair.get(i * 3 + 3).first().intValue() + "\n");
					}
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void printRead(int channel) {
				try {
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/read-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" read in createAddPort, COllapsedBuildings\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
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
//				if (remain < world.getTime() - world.getTimestamp().getLastChangedTime(id))
////						|| world.getUnburntBuildings().contains(id))
//					it.remove();
//			}
//			
//			for (EntityID id : changed.getChangedEntities()) {
//				StandardEntity se = world.getEntity(id);
//				if (se.getStandardURN() == StandardEntityURN.BUILDING) {
//					Building building = (Building) se;
////					if (world.getUnburntBuildings().contains(building))
////						reportedBuilding.add(building.getID());
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
//			
//			for (int i = 0; i < n; i++) {
//				Building building = world.getEntity(reportedBuilding.get(i), Building.class);
//				int uniform = world.getUniform().toUniform(building.getID());
//				sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//				sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
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
//				if (id == null)
//					continue;
//				Building building = world.getEntity(id, Building.class);
//				if (0 <= fireyness && fireyness < 9) 
//					building.setFieryness(fireyness);
//				add(building);
//			}
//		}
//
//		@Override
//		public MessageReportedType getMessageReportedType() {
//			return MessageReportedType.REPORTRD_TO_ALL;
//		}
//	}
	
	public boolean contains(Building building) {
		return super.contains(building);
	}
	
	public boolean contains(EntityID id) {
		return super.contains(world.getEntity(id, Building.class));
	}
}
