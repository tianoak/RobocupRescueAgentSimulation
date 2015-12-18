package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastSet;

import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.Port;
import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import csu.model.object.CSUBuilding;
import csu.util.BitUtil;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

@SuppressWarnings("serial")
public class BurningBuildings extends HashSet<Building> {
	private List<Building> fromChangeSet = new ArrayList<>();
	private List<Building> fromChangeSetVeryLimit = new ArrayList<>();
	
	private Comparator<Pair<Building, Integer>> timeComparator = new Comparator<Pair<Building,Integer>>() {

		@Override
		public int compare(Pair<Building, Integer> o1, Pair<Building, Integer> o2) {
			if (o1.second() > o2.second()) 
				return -1;
			if (o1.second() < o2.second())
				return 1;
			if (o1.second() == o2.second()) {
				if (o1.first().getID().getValue() > o2.first().getID().getValue())
					return -1;
				if (o1.first().getID().getValue() < o2.first().getID().getValue())
					return 1;
			}
			return 0;
		}
	};
	
	private Set<Pair<Building, Integer>> allShouldSend = new TreeSet<>(timeComparator);
	private Set<Pair<Building, Integer>> newlyAddFromVoice = new FastSet<>();
	
	private AdvancedWorldModel world;
	
	private int repeatThreshold;
	
	// constructor
	public BurningBuildings(AdvancedWorldModel world) {
		this.world = world;
		this.repeatThreshold = world.getConfig().ignoreUntil + 1;
	}
	
	public void update(AdvancedWorldModel world, ChangeSet changeSet) {
		fromChangeSet.clear();
		fromChangeSetVeryLimit.clear();
		for (EntityID e : changeSet.getChangedEntities()) {
			StandardEntity entityId = world.getEntity(e);
			if (entityId instanceof Building) {
				Building building = (Building) entityId;
				
				if (building.isOnFire()) {
					
					if (world.getTime() < repeatThreshold) {
						add(building);
						fromChangeSet.add(building);
						fromChangeSetVeryLimit.add(building);
					} else {
						if (add(building)) {
							fromChangeSet.add(building);
							fromChangeSetVeryLimit.add(building);
						}
					}
				} else if (remove(building)) {
				}
			}
		}
	}
	
	public void remove() {
		for (Iterator<Building> itor = this.iterator(); itor.hasNext(); ) {
			Building building = itor.next();
			if(building.isOnFire())
				continue;
			itor.remove();
		}
	}
	
	private final int MAX_N = 31;
	private final int N_BIT_SIZE = BitUtil.needBitSize(MAX_N);
	
	public Port createAddPort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			private ChangeSet changed;
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";
			
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
				if (fromChangeSet.isEmpty())
					return null;
				MessageBitSection sec;
				if (comUtil.isNoRadio()) {
					sec = new MessageBitSection(1);
				} else {
					sec = new MessageBitSection(5);
				}
				sec.setTimeToLive(timeToLive);

				final int n = Math.min(fromChangeSet.size(), MAX_N + 1);
				sec.add(n, N_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Building building = fromChangeSet.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
				}
				fromChangeSet.clear();
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
				counter++;
				return next;
			}
			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				CSUBuilding csuBuilding;
				final int n = stream.readBit(N_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					final int fieryness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					readString = readString + "fieryness = " + fieryness + "\n";
					readString = readString + "temperature = " + temperature + "\n";
					
					if (id == null) continue;
					if (changed.getChangedEntities().contains(id))
						continue;
					Building building = (Building) world.getEntity(id);
					csuBuilding = world.getCsuBuilding(building.getID());
					
					if (0 <= fieryness && fieryness < 9) {
						building.setFieryness(fieryness);
					} else 
						continue;
					if (temperature == 0)
						continue;
					if (temperature < 2000)
						building.setTemperature(temperature);
					else
						continue;
					add(building);
					
					// for fb's simulator
					if (world.getControlledEntity() instanceof FireBrigade) {
						switch (building.getFieryness()) {
						case 0:
							csuBuilding.setFuel(csuBuilding.getInitialFuel());
							break;
						case 1:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.66) {
								csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.75));
		                    } else if (csuBuilding.getFuel() == csuBuilding.getInitialFuel()) {
		                    	csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.90));
		                    }
							break;
						case 2:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.33
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.66) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.50));
		                    }
							break;
						case 3:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.01
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.33) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.15));
		                    }
							break;
						case 8:
							csuBuilding.setFuel(0);
							break;
						default:
							break;
						}
						csuBuilding.setEnergy(building.getTemperature() * csuBuilding.getCapacity(), "BurningBuildings"); 
					}
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			@Override
			public String toString(){
				return "" + this.getClass().getName() + " ----- add burning building" ;
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
					writer.write(" write in createAddPort, BurningBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int e_uniform = dataSizePair.get(i * 3 + 1).first().intValue();
						EntityID e_id = world.getUniform().toID(StandardEntityURN.BUILDING, e_uniform);
						writer.write("uniform = " + e_uniform + ", id = " + e_id + "\n");
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
					writer.write(" read in createAddPort, BurningBuildings\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
		};
	}
	
	public Port createVeryLimitComPort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			
			private ChangeSet changed;
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";

			@Override
			public void init(ChangeSet changed) {
				counter = 0;
				this.changed = changed;
				
				// allShouldSend.addAll(newlyAddFromVoice);
				for (Building next : fromChangeSetVeryLimit) {
					allShouldSend.add(new Pair<Building, Integer>(next, world.getTime()));
				}
				
				fromChangeSetVeryLimit.clear();
				// newlyAddFromVoice.clear();
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> itor = packetList.iterator(); itor.hasNext(); ) {
					if (itor.next().getTimeToLive() <= 0)
						itor.remove();
				}
			}
			
			private MessageBitSection createNewPacket() {
				if (allShouldSend.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(1);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(allShouldSend.size(), MAX_N + 1);
				sec.add(n, N_BIT_SIZE);
				int count = 0;
				for (Pair<Building, Integer> next : allShouldSend) {
					count++;
					if (count > n)
						break;
					Building building = next.first();
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
					sec.add(next.second().intValue(), comUtil.TIME_BIT_SIZE);
				}
				allShouldSend.clear();
				return sec;
			}

			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				return false;
			}

			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				counter++;
				return next;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				CSUBuilding csuBuilding;
				final int n = stream.readBit(N_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", number to send = " + n + "\n";
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					final int fieryness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
					final int findTime = stream.readBit(comUtil.TIME_BIT_SIZE);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					readString = readString + "fieryness = " + fieryness + "\n";
					readString = readString + "temperature = " + temperature + "\n";
					readString = readString + "firstFindTime = " + findTime + "\n";
					
					if (id == null)
						continue;
					if (changed.getChangedEntities().contains(id)) 
						continue;
					Building building = (Building)world.getEntity(id);
					
					int temperatureLastChangeTime = 
							world.getTimestamp().getPropertyTimeStamp(id, StandardPropertyURN.TEMPERATURE);
					int fierynessLastChangeTime = 
							world.getTimestamp().getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS);
					if (temperatureLastChangeTime >= findTime || fierynessLastChangeTime >= findTime)
						continue;
					
					csuBuilding = world.getCsuBuilding(id);
					if (0 <= fieryness && fieryness < 9)
						building.setFieryness(fieryness);
					else
						continue;
					if (temperature == 0)
						continue;
					if (temperature < 2000)
						building.setTemperature(temperature);
					else
						continue;
					
					newlyAddFromVoice.add(new Pair<Building, Integer>(building, findTime));
					
					// for fb's simulator
					if (world.getControlledEntity() instanceof FireBrigade) {
						switch (building.getFieryness()) {
						case 0:
							csuBuilding.setFuel(csuBuilding.getInitialFuel());
							break;
						case 1:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.66) {
								csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.75));
		                    } else if (csuBuilding.getFuel() == csuBuilding.getInitialFuel()) {
		                    	csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.90));
		                    }
							break;
						case 2:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.33
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.66) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.50));
		                    }
							break;
						case 3:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.01
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.33) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.15));
		                    }
							break;
						case 8:
							csuBuilding.setFuel(0);
							break;
						default:
							break;
						}
						csuBuilding.setEnergy(building.getTemperature() * csuBuilding.getCapacity(), "BurningBuildings"); 
					}
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
					writer.write(" write in createVeryLimitComPort, BurningBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int e_uniform = dataSizePair.get(i * 4 + 1).first().intValue();
						EntityID e_id = world.getUniform().toID(StandardEntityURN.BUILDING, e_uniform);
						writer.write("uniform = " + e_uniform + ", id = " + e_id + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 4 + 2).first().intValue() + "\n");
						writer.write("temperature = " + dataSizePair.get(i * 4 + 3).first().intValue() + "\n");
						writer.write("firstFindTime = " + dataSizePair.get(i * 4 + 4).first().intValue() + "\n");
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
					writer.write(" read in createVeryLimitComPort, BurningBuildings\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
		};
	}
	
	public Port createNoComPort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			
			private ChangeSet changed;
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";

			@Override
			public void init(ChangeSet changed) {
				counter = 0;
				this.changed = changed;
				
				allShouldSend.addAll(newlyAddFromVoice);
				for (Building next : fromChangeSet) {
					allShouldSend.add(new Pair<Building, Integer>(next, world.getTime()));
				}
				
				fromChangeSet.clear();
				newlyAddFromVoice.clear();
				
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> itor = packetList.iterator(); itor.hasNext(); ) {
					if (itor.next().getTimeToLive() <= 0)
						itor.remove();
				}
			}
			
			private MessageBitSection createNewPacket() {
				if (allShouldSend.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(5);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(allShouldSend.size(), MAX_N + 1);
				sec.add(n, N_BIT_SIZE);
				int count = 0;
				for (Pair<Building, Integer> next : allShouldSend) {
					count++;
					if (count > n)
						break;
					Building building = next.first();
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
					sec.add(building.getFieryness(), comUtil.FIRERYNESS_BIT_SIZE);
					sec.add(building.getTemperature(), comUtil.UINT_BIT_SIZE);
					sec.add(next.second().intValue(), comUtil.TIME_BIT_SIZE);
				}
				allShouldSend.clear();
				return sec;
			}

			@Override
			public boolean hasNext() {
				if (counter < packetList.size())
					return true;
				return false;
			}

			@Override
			public MessageBitSection next() {
				MessageBitSection next = packetList.get(counter);
				counter++;
				return next;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				CSUBuilding csuBuilding;
				final int n = stream.readBit(N_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", number to send = " + n + "\n";
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					final int fieryness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
					final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
					final int findTime = stream.readBit(comUtil.TIME_BIT_SIZE);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					readString = readString + "fieryness = " + fieryness + "\n";
					readString = readString + "temperature = " + temperature + "\n";
					readString = readString + "firstFindTime = " + findTime + "\n";
					
					if (id == null)
						continue;
					if (changed.getChangedEntities().contains(id)) 
						continue;
					Building building = (Building)world.getEntity(id);
					
					int temperatureLastChangeTime = 
							world.getTimestamp().getPropertyTimeStamp(id, StandardPropertyURN.TEMPERATURE);
					int fierynessLastChangeTime = 
							world.getTimestamp().getPropertyTimeStamp(id, StandardPropertyURN.FIERYNESS);
					if (temperatureLastChangeTime >= findTime || fierynessLastChangeTime >= findTime)
						continue;
					
					csuBuilding = world.getCsuBuilding(id);
					if (0 <= fieryness && fieryness < 9)
						building.setFieryness(fieryness);
					else
						continue;
					if (temperature == 0)
						continue;
					if (temperature < 2000)
						building.setTemperature(temperature);
					else
						continue;
					
					newlyAddFromVoice.add(new Pair<Building, Integer>(building, findTime));
					
					// for fb's simulator
					if (world.getControlledEntity() instanceof FireBrigade) {
						switch (building.getFieryness()) {
						case 0:
							csuBuilding.setFuel(csuBuilding.getInitialFuel());
							break;
						case 1:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.66) {
								csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.75));
		                    } else if (csuBuilding.getFuel() == csuBuilding.getInitialFuel()) {
		                    	csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.90));
		                    }
							break;
						case 2:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.33
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.66) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.50));
		                    }
							break;
						case 3:
							if (csuBuilding.getFuel() < csuBuilding.getInitialFuel() * 0.01
		                            || csuBuilding.getFuel() > csuBuilding.getInitialFuel() * 0.33) {
		                        csuBuilding.setFuel((float) (csuBuilding.getInitialFuel() * 0.15));
		                    }
							break;
						case 8:
							csuBuilding.setFuel(0);
							break;
						default:
							break;
						}
						csuBuilding.setEnergy(building.getTemperature() * csuBuilding.getCapacity(), "BurningBuildings"); 
					}
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
					writer.write(" write in createNoComPort, BurningBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						int e_uniform = dataSizePair.get(i * 4 + 1).first().intValue();
						EntityID e_id = world.getUniform().toID(StandardEntityURN.BUILDING, e_uniform);
						writer.write("uniform = " + e_uniform + ", id = " + e_id + "\n");
						writer.write("fieryness = " + dataSizePair.get(i * 4 + 2).first().intValue() + "\n");
						writer.write("temperature = " + dataSizePair.get(i * 4 + 3).first().intValue() + "\n");
						writer.write("firstFindTime = " + dataSizePair.get(i * 4 + 4).first().intValue() + "\n");
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
					writer.write(" read in createAddPort, BurningBuildings\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
		};
	}
	
	/*public Port createRemovePort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";

			@Override
			public void init(ChangeSet changed) {
				MessageBitSection newPacket = createNewPacket();
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}

			private MessageBitSection createNewPacket() {
				if (removeRecords.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(10);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(removeRecords.size(), MAX_N);
				sec.add(n, N_BIT_SIZE);
				for (int i = 0; i < n; i++) {
					Building building = removeRecords.get(i);
					int uniform = world.getUniform().toUniform(building.getID());
					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
				}
				removeRecords.clear();
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
				counter++;
				return next;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(N_BIT_SIZE);
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				for (int i = 0; i < n; i++) {
					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
					readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
					if (id == null)
						continue;
					Building building = (Building) world.getEntity(id);
					if (building.isFierynessDefined()) {
						switch (building.getFierynessEnum()) {
						case UNBURNT:
						case BURNT_OUT:
							break;
						default:
							building.setFieryness(StandardEntityConstants.Fieryness.WATER_DAMAGE.ordinal());
						}
					}
					remove(building);
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
					writer.write(" write in createRemovePort, BurningBuildings\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("uniform = " + dataSizePair.get(i + 1).first().intValue() + "\n");
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
					writer.write(" read in createRemovePort, BurningBuildings\n");
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
		private boolean sent;
		private Set<EntityID> bringBuildings = new HashSet<EntityID>();
		final private CommunicationUtil comUtil;
		final private int remain;
		private String readString = "";
		
		public NoComPort(CommunicationUtil comUtil, int remain) {
			this.comUtil = comUtil;
			this.remain = remain;
		}
		
		@Override
		public void init(ChangeSet changed) {
			for (Iterator<EntityID> it = bringBuildings.iterator(); it.hasNext();) {
				EntityID id = it.next();
				if (remain < world.getTime() - world.getTimestamp().getLastChangedTime(id)
						|| !world.getBurningBuildings().contains(id)) {
					it.remove();
				}
			}
			
			for (EntityID id : changed.getChangedEntities()) {
				StandardEntity se = world.getEntity(id);
				if (se.getStandardURN() == StandardEntityURN.BUILDING) {
					Building building = (Building) se;
					if (world.getBurningBuildings().contains(building)) {
						bringBuildings.add(building.getID());
					}
				}
			}
			sent = bringBuildings.isEmpty();
		}
		@Override
		public boolean hasNext() {
			return !sent;
		}
		@Override
		public MessageBitSection next() {
			sent = true;
			MessageBitSection sec = new MessageBitSection(1);
			final int n = Math.min(bringBuildings.size(), MAX_N);
			sec.add(n, N_BIT_SIZE);
			List<EntityID> buildingsList = new ArrayList<EntityID>(bringBuildings);
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
		public void read(EntityID sender, int ttimeComparatorime, BitArrayInputStream stream) {
			final int n = stream.readBit(N_BIT_SIZE);
			readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
			for (int i = 0; i < n; i++) {
				final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
				EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
				final int fieryness = stream.readBit(comUtil.FIRERYNESS_BIT_SIZE);
				final int temperature = stream.readBit(comUtil.UINT_BIT_SIZE);
				final int messageTime = stream.readBit(comUtil.TIME_BIT_SIZE);
				readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
				readString = readString + "fieryness = " + fieryness + "\n";
				readString = readString + "temperature = " + temperature + "\n";
				readString = readString + "lastChangedTime = " + messageTime + "\n";
				
				if (id == null) continue;
				if (messageTime < world.getTimestamp().getLastChangedTime(id)) continue;
				
				Building building = (Building) world.getEntity(id);
				bringBuildings.add(id);
				
				if (0 <= fieryness
						&& fieryness < StandardEntityConstants.Fieryness.values().length) {
					building.setFieryness(fieryness);
				}
				else {
					building.setFieryness(StandardEntityConstants.Fieryness.BURNING.ordinal());
				}
				building.setTemperature(temperature);
				if (temperature > 0) {
					add(building);
				}
				world.getTimestamp().setLastChangedTime(id, messageTime);
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
				writer.write(" write in createRemovePort, BurningBuildings\n");
				writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
				writer.write("numberToSend = " + numberToSend + "\n");
				for (int i = 0; i < numberToSend; i++) {
					writer.write("uniform = " + dataSizePair.get(i * 4 + 1).first().intValue() + "\n");
					writer.write("fieryness = " + dataSizePair.get(i * 4 + 2).first().intValue() + "\n");
					writer.write("temperature = " + dataSizePair.get(i * 4+ 3).first().intValue() + "\n");
					writer.write("lastChangeTime = " + dataSizePair.get(i * 4+ 4).first().intValue() + "\n");
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
				writer.write(" read in createRemovePort, BurningBuildings\n");
				writer.write("read from channel: " + channel + "\n");
				writer.write(readString + "\n");
				writer.write("\n");
				writer.close();
				
				readString = "";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	public boolean contains(Building building) {
		return super.contains(building);
	}
	
	public boolean contains(EntityID id) {
		return super.contains(world.getEntity(id, Building.class));
	}
}
