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
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

@SuppressWarnings("serial")
public class ExtinguishedBuilding extends HashSet<Building>{
	/** A list of extinguished building will reported to others this cycle.*/
	private List<Building> addRecords = new ArrayList<>();
//	/** A list of buildings should removed from extinguished buildings this cycle.*/
//	private List<Building> removeRecords = new ArrayList<>();
	
	/** An instance of world model.*/
	private AdvancedWorldModel world;
	
	// constructor
	public ExtinguishedBuilding(AdvancedWorldModel world) {
		this.world = world;
	}
	
	/** Update extinguished building in world model.*/
	public void update(AdvancedWorldModel world, ChangeSet changed){
		addRecords.clear();
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity se = world.getEntity(id);
			if (se instanceof Building) {
				Building building = (Building) se;
				if (building.isFierynessDefined() && building.getFieryness() > 3 && building.getFieryness() < 8){
					if (add(building) && building.isTemperatureDefined())
						addRecords.add(building);
				} else
					remove(building);
			}
		}
	}
	
	public void remove() {
		for (Iterator<Building> itor = this.iterator(); itor.hasNext(); ) {
			Building building = itor.next();
			if (building.isOnFire() || (building.isFierynessDefined() && building.getFieryness() == 8)) {
				itor.remove();
			}
		}
	}
	
	/** Maximum number of building can reported each time.*/
	private final int MAX_NUMBER = 31;
	private final int NUMBER_BIT_SIZE = BitUtil.needBitSize(MAX_NUMBER);
	
	public Port createAddPort(final CommunicationUtil comUtil, final int timeToLive) {
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
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(NUMBER_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				CSUBuilding csuBuilding;
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					readString = readString + "fieryness = " + fireyness + "\n";
					readString = readString + "temperature = " + temperature + "\n";
					
					if (id == null)
						continue;
					if (changed.getChangedEntities().contains(id))
						continue;
					TimeStamp timestamp = world.getTimestamp();
					if (timestamp.getPropertyTimeStamp(id, StandardPropertyURN.TEMPERATURE) >= time
							|| timestamp.getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS) >= time)
						continue;
					Building building = world.getEntity(id, Building.class);
					csuBuilding = world.getCsuBuilding(id);
					if (0 <= fireyness && fireyness < 9)
						building.setFieryness(fireyness);
					else
						building.setFieryness(StandardEntityConstants.Fieryness.WATER_DAMAGE.ordinal());
					if (temperature > 0 && temperature < csuBuilding.getIgnitionPoint())
						building.setTemperature(temperature);
					else
						building.setTemperature((int)csuBuilding.getIgnitionPoint());
					csuBuilding.setEnergy(0, "extinguishedBuildings");
					csuBuilding.setWasEverWatered(true);
					
					add(building);
				}
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// next.decrementTTL();
				counter++;
				return next;
			}
			
			/** This method used to create this cycle's packet.*/
			private MessageBitSection createNewPacket() {
				if (addRecords.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(9); 
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(MAX_NUMBER, addRecords.size());
				sec.add(n, NUMBER_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Building building = addRecords.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
				}
				addRecords.clear();
				return sec;
			}
			
			@Override
			public void init(ChangeSet changed) {
				this.changed = changed;
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
				    this.packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
					if (it.next().getTimeToLive() <= 0)
						it.remove();
				}
			}
			
			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
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
					writer.write(" write in createAddPort, ExtinguishedBuilding\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("uniform = " + dataSizePair.get(i * 3 + 1).first().intValue() + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 3 + 2).first().intValue() + "\n");
						writer.write("temperature = " + dataSizePair.get(i * 3+ 3).first().intValue() + "\n");
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
					writer.write(" read in createAddPort, ExtinguishedBuilding\n");
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
	
	public Port createAddPortNoRadio(final CommunicationUtil comUtil, final int timeToLive) {
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
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(NUMBER_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				CSUBuilding csuBuilding;
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					final int fireyness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
					final int findTime = stream.readBit(comUtil.TIME_BIT_SIZE);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					readString = readString + "fieryness = " + fireyness + "\n";
					readString = readString + "temperature = " + temperature + "\n";
					readString = readString + "findTime = " + findTime + "\n";
					
					if (id == null)
						continue;
					if (changed.getChangedEntities().contains(id))
						continue;
					TimeStamp timestamp = world.getTimestamp();
					if (timestamp.getPropertyTimeStamp(id, StandardPropertyURN.TEMPERATURE) >= findTime
							|| timestamp.getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS) >= findTime)
						continue;
					Building building = world.getEntity(id, Building.class);
					csuBuilding = world.getCsuBuilding(id);
					if (0 <= fireyness && fireyness < 9)
						building.setFieryness(fireyness);
					else
						building.setFieryness(StandardEntityConstants.Fieryness.WATER_DAMAGE.ordinal());
					if (temperature > 0 && temperature < csuBuilding.getIgnitionPoint())
						building.setTemperature(temperature);
					else
						building.setTemperature((int)csuBuilding.getIgnitionPoint());
					csuBuilding.setEnergy(0, "extinguishedBuildings");
					csuBuilding.setWasEverWatered(true);
					
					add(building);
				}
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				// next.decrementTTL();
				counter++;
				return next;
			}
			
			/** This method used to create this cycle's packet.*/
			private MessageBitSection createNewPacket() {
				if (addRecords.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(9); 
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(MAX_NUMBER, addRecords.size());
				sec.add(n, NUMBER_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Building building = addRecords.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
					sec.add(world.getTime(), comUtil.TIME_BIT_SIZE);
				}
				addRecords.clear();
				return sec;
			}
			
			@Override
			public void init(ChangeSet changed) {
				this.changed = changed;
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
				    this.packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
					if (it.next().getTimeToLive() <= 0)
						it.remove();
				}
			}
			
			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				else
					return false;
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
					writer.write(" write in createAddPort, ExtinguishedBuilding\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("uniform = " + dataSizePair.get(i * 4 + 1).first().intValue() + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 4 + 2).first().intValue() + "\n");
						writer.write("temperature = " + dataSizePair.get(i * 4+ 3).first().intValue() + "\n");
						writer.write("findTime = " + dataSizePair.get(i * 4 + 4).first().intValue() + "\n");
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
					writer.write(" read in createAddPort, ExtinguishedBuilding\n");
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
	
	/*public Port cerateRemovePort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";
			
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				next.decrementTTL();
				counter++;
				return next;
			}
			
			@Override
			public void init(ChangeSet changed) {
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
				    this.packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
					if (it.next().getTimeToLive() <= 0)
						it.remove();
				}
			}
			
			private MessageBitSection createNewPacket() {
				if (removeRecords.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(100);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(MAX_NUMBER, removeRecords.size());
				sec.add(n, NUMBER_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Building building = addRecords.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
				}
				
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
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public void printWrite(MessageBitSection packet) {
				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
				System.out.println(" write in cerateRemovePort, ExtinguishedBuilding");
				System.out.println("priority = " + packet.getPriority());
				List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
				int numberToSend = dataSizePair.get(0).first().intValue();
				System.out.println("numberToSend = " + numberToSend);
				for (int i = 0; i < numberToSend; i++) {
					System.out.println("uniform = " + dataSizePair.get(i * 3 + 1).first().intValue());
					System.out.println("fieryness = " + dataSizePair.get(i * 3 + 2).first().intValue());
					System.out.println("temperature = " + dataSizePair.get(i * 3+ 3).first().intValue());
				}
			}
			
			@Override
			public void printRead() {
				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
				System.out.println(" read in cerateRemovePort, ExtinguishedBuilding");
				System.out.println(readString);
				System.out.println();
				readString = "";
			}
		};
	}
	
	public Port createNoComPort(final CommunicationUtil comUtil, final boolean isAggregator, final boolean isMessenger) {
		if (isAggregator) {
			return new NoComPort(comUtil, 180);
		}
		if (isMessenger) {
			return new NoComPort(comUtil, 80);
		} else {
			return new NoComPort(comUtil, 3);
		}
	}
		
	private class NoComPort implements Port {
		private boolean flag;
		private Set<EntityID> reportedBuildings = new HashSet<EntityID>();
		final private CommunicationUtil comUtil;
		final private int remain;
		
		public NoComPort(CommunicationUtil comUtil, int remain) {
			this.comUtil = comUtil;
			this.remain = remain;
		}
		
		@Override
		public void init(ChangeSet changed) {
			for (Iterator<EntityID> it = reportedBuildings.iterator(); it.hasNext();) {
				EntityID id = it.next();
				if (remain < world.getTime() - world.getTimestamp().getLastChangedTime(id)
						|| !world.getExtinguishedBuildings().contains(id)) {
					it.remove();
				}
			}
			
			for (EntityID id : changed.getChangedEntities()) {
				StandardEntity se = world.getEntity(id);
				if (se.getStandardURN() == StandardEntityURN.BUILDING) {
					Building building = (Building) se;
					if (world.getExtinguishedBuildings().contains(building)) {
						reportedBuildings.add(building.getID());
					}
				}
			}
			flag = reportedBuildings.isEmpty();
		}
		@Override
		public boolean hasNext() {
			return !flag;
		}
		@Override
		public MessageBitSection next() {
			flag = true;
			MessageBitSection sec = new MessageBitSection(50);
			final int n = Math.min(reportedBuildings.size(), MAX_NUMBER);
			sec.add(n, NUMBER_BIT_SIZE);
			List<EntityID> buildingsList = new ArrayList<EntityID>(reportedBuildings);
			for (int i = 0; i < n; i++) {
				Building building = (Building) world.getEntity(buildingsList.get(i));
				int uniform = world.getUniform().toUniform(building.getID());
				sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
				sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
				sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
				sec.add(world.getTimestamp().getLastChangedTime(building.getID()), comUtil.TIME_BIT_SIZE);
			}
			return sec;
		}
		@Override
		public void read(EntityID sender, int time, BitArrayInputStream stream) {
			final int n = stream.readBit(NUMBER_BIT_SIZE);
			for (int i = 0; i < n; i++) {
				final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
				EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
				final int fieryness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
				final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
				final int messageTime = stream.readBit(comUtil.TIME_BIT_SIZE);
				
				if (id == null) continue;
				if (messageTime < world.getTimestamp().getLastChangedTime(id)) continue;
				
				Building building = (Building) world.getEntity(id);
				reportedBuildings.add(id);
				
				if (0 <= fieryness
						&& fieryness < StandardEntityConstants.Fieryness.values().length) {
					building.setFieryness(fieryness);
				} else {
					building.setFieryness(StandardEntityConstants.Fieryness.WATER_DAMAGE.ordinal());
				}
				building.setTemperature(temperature);
				world.getTimestamp().setLastChangedTime(id, messageTime);
			}
		}
		
		@Override
		public MessageReportedType getMessageReportedType() {
			return MessageReportedType.REPORTRD_TO_ALL;
		}
	}*/
	
	public boolean contains(Building building) {
		return super.contains(building);
	}
	
	public boolean contains(EntityID id) {
		return super.contains(world.getEntity(id, Building.class));
	}
}
