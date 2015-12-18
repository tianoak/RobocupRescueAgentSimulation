package csu.model;

import java.util.HashSet;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * A searched building represent a building that someone has enter search it.
 * And do not include those buildings that others just have lookuped outside it.
 * <p>
 * Because we send each Agent's position in every cycle, there is no need to
 * send searched anymore.
 * 
 * @author appreciation-csu
 * 
 */
@SuppressWarnings("serial")
public class SearchedBuildings extends HashSet<EntityID> {
	// private ArrayList<Building> addRecords = new ArrayList<Building>();
	
	/** An instance of world model.*/
	private AdvancedWorldModel world;
	
	// constructor
	public SearchedBuildings(AdvancedWorldModel world) {
		this.world = world;
	}
	
	/** Update someone searched building in world model.*/
	public void update(AdvancedWorldModel world, Human me) {   ///test
		Human agent;
		for (StandardEntity next : world.getEntitiesOfType(AgentConstants.PLATOONS)) {
			if (next instanceof Human) {
				agent = (Human) next;
				if (agent.isPositionDefined()) {
					if (world.getEntity(agent.getPosition()) instanceof Building) {
						this.add(agent.getPosition());
					}
				}
			}
		}
		
		
//		addRecords.clear();
//		Area area = (Area) me.getPosition(world);
//		if (area instanceof Building) {
//			if (!world.getSearchedBuildings().contains((Building) area)) {
//				addRecords.add((Building) area);
//				world.getSearchedBuildings().add(((Building) area).getID());
//			}
//		}
	}
	

//	private final int MAX_N = 31;
//	private final int N_BIT_SIZE = BitUtil.needBitSize(MAX_N + 1);
//	
//	public Port createAddPort(final CommunicationUtil comUtil, final int timeToLive) {
//		return new Port() {
//			private List<MessageBitSection> packetList = new ArrayList<>();
//			private int counter = 0;
//			private String readString = "";
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
//			public void init(ChangeSet changed) {
//				MessageBitSection newPacket = createNewPacket();
//				if (newPacket != null)
//					this.packetList.add(newPacket);
//				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
//					if (it.next().getTimeToLive() <= 0)
//						it.remove();
//				}
//			}
//			
//			/** This method used to create packet of this cycle.*/
//			private MessageBitSection createNewPacket() {
//				if (addRecords.isEmpty())
//					return null;
//				MessageBitSection sec = new MessageBitSection(50);
//				sec.setTimeToLive(timeToLive);
//				final int n = Math.min(addRecords.size(), MAX_N);
//				sec.add(n, N_BIT_SIZE);
//				for (int i = 0; i < n; i++) {
//					Building building = addRecords.get(i);
//					int uniform = world.getUniform().toUniform(building.getID());
//					sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
//				}
//				return sec;
//			}
//
//			@Override
//			public MessageBitSection next() {
//				MessageBitSection next = packetList.get(counter);
//				counter++;
//				// next.decrementTTL();
//				return next;
//			}
//
//			@Override
//			public void read(EntityID sender, int time,BitArrayInputStream stream) {
//				final int n = stream.readBit(N_BIT_SIZE);
//				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
//				for (int i = 0; i < n; i++) {
//					final int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
//					EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
//					readString = readString + "uniform = " + uniform + ", buildingId = " + id + "\n";
//					if (id == null) 
//						continue;
//					Building building = (Building) world.getEntity(id);
//					
//					if(!world.getSearchedBuildings().contains(building)){
//						world.getSearchedBuildings().add(building.getID());
//					}
//				}
//			}
//			
//			@Override
//			public MessageReportedType getMessageReportedType() {
//				return MessageReportedType.REPORTRD_TO_ALL;
//			}
//			
//			@Override
//			public void printWrite(MessageBitSection packet, int channel) {
//				try {
//					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
//					int numberToSend = dataSizePair.get(0).first().intValue();
//					
//					int id = world.getControlledEntity().getID().getValue();
//					String fileName = "commOutput/write-" + id;
//					File file = new File(fileName);
//					if (!file.exists())
//						file.createNewFile();
//					
//					FileWriter writer = new FileWriter(fileName, true);
//					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//					writer.write(" write in createAddPort, SearchedBuildings\n");
//					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
//					writer.write("numberToSend = " + numberToSend + "\n");
//					for (int i = 0; i < numberToSend; i++) {
//						writer.write("buildingUniform = " + dataSizePair.get(i + 1).first().intValue() + "\n");
//					}
//					writer.write("\n");
//					writer.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
////				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
////				System.out.println(" write in createAddPort, SearchedBuildings");
////				System.out.println("priority = " + packet.getPriority());
////				List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
////				int numberToSend = dataSizePair.get(0).first().intValue();
////				System.out.println("numberToSend = " + numberToSend);
////				for (int i = 0; i < numberToSend; i++) {
////					System.out.println("buildingUniform = " + dataSizePair.get(i + 1).first().intValue());
////				}
//			}
//			
//			@Override
//			public void printRead(int channel) {
//				try {
//					int id = world.getControlledEntity().getID().getValue();
//					String fileName = "commOutput/read-" + id;
//					File file = new File(fileName);
//					if (!file.exists())
//						file.createNewFile();
//					
//					FileWriter writer = new FileWriter(fileName, true);
//					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//					writer.write(" read in createAddPort, SearchedBuildings\n");
//					writer.write("read from channel: " + channel + "\n");
//					writer.write(readString + "\n");
//					writer.write("\n");
//					writer.close();
//					
//					readString = "";
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
////				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
////				System.out.println(" read in createAddPort, SearchedBuildings");
////				System.out.println(readString);
////				System.out.println();
////				readString = "";
//			}
//		};
//	}
	
	public boolean contains(Building building) {
		return super.contains(building);
	}
	public boolean contains(EntityID id) {
		return super.contains(world.getEntity(id));
	}

}
