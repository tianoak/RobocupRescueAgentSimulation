package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import csu.Viewer.layers.CSU_CriticalAreaLayer;
import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.Port;
import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import csu.model.route.pov.POVRouter;
import csu.util.BitUtil;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * We divide roads into three different kinds. One is called the entrance, for
 * this kind roads are connected to the buildings, so if you'd like to go into
 * the buildings you must pass these roads. The second one is called avenue, for
 * they can be combined into a long street. And the rest is the cross, these
 * roads are connected to more than three roads.
 * <p>
 * The cross of two long street often plays important role in traffic system, so
 * we them as critical areas. Also, refuges are critical area, too.
 * <p>
 * Date: Mar 10, 2014 Time: 2:03 am  improved by appreciation-csu
 * 
 * @author utisam
 */
public class CriticalArea {
	private AdvancedWorldModel world;
	private List<Area> criticalAreas = new ArrayList<Area>();
	
	private List<EntityID> criticalAreaIds = new ArrayList<>();

	public CriticalArea(AdvancedWorldModel world) {
		this.world = world;
		
		AREAMARK: for (StandardEntity se : world.getEntitiesOfType(AgentConstants.ROADS)) {
			Area area = (Area) se;
			List<Edge> edges = area.getEdges();
			if (edges.size() < 3)
				continue;
			for (Edge edge : edges) {
				if (!edge.isPassable())
					continue AREAMARK;
				if (world.getEntity(edge.getNeighbour()) instanceof Building)
					continue AREAMARK;
			}
			
			criticalAreas.add(area);
			criticalAreaIds.add(area.getID());
		}
		
		// remove neighbour of entrances
		for (Iterator<Area> itor = criticalAreas.iterator(); itor.hasNext();) {
			Area area = (Area)itor.next();
			List<EntityID> neighbours = area.getNeighbours();
			CRITICAL_MARK: for (EntityID entityId : neighbours) {
				Area neighbour = (Area) world.getEntity(entityId);
				List<EntityID> n_neighbours = neighbour.getNeighbours();
				if (n_neighbours.size() <= 2) {
					for (EntityID next : n_neighbours) {
						if (world.getEntity(next) instanceof Building) {
							criticalAreaIds.remove(area.getID());
							itor.remove();
							break CRITICAL_MARK;
						}
					}
				} else if (neighbour.getEdges().size() == n_neighbours.size()) {
					criticalAreaIds.remove(area.getID());
					itor.remove();
					break CRITICAL_MARK;
				}
			}
		}
		
		if (AgentConstants.LAUNCH_VIEWER) {
			if (CSU_CriticalAreaLayer.CRITICAL_AREA != null 
					&& CSU_CriticalAreaLayer.CRITICAL_AREA.isEmpty()) {
				CSU_CriticalAreaLayer.CRITICAL_AREA.addAll(criticalAreas);
			}
		}
	}
	
	private final int MAX_SEND_SIZE = 7; 
	private int SEND_SIZE_BIT = BitUtil.needBitSize(MAX_SEND_SIZE);
	private List<Area> sendRemovedAreas = new ArrayList<Area>();

	public void update(POVRouter router) {
		sendRemovedAreas.clear();
		for (Iterator<Area> it = criticalAreas.iterator(); it.hasNext();) {
			Area area = (Area) it.next();
			
			if (router.isSureReachable(area)) {
				if (sendRemovedAreas.size() <= MAX_SEND_SIZE) {
					sendRemovedAreas.add(area);
				}
				criticalAreaIds.remove(area.getID());
				it.remove();
			}
		}
	}

	public Port createPort(final CommunicationUtil comUtil, final int timeToLive) {
		return new Port() {
			private List<MessageBitSection> packetList = new ArrayList<>();
			private int counter = 0;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				MessageBitSection newPacket = createNewPacket();
				if (newPacket !=null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
					if (it.next().getTimeToLive() <= 0)
						it.remove();
			}
			
			private MessageBitSection createNewPacket() {
				if (sendRemovedAreas.isEmpty())
					return null;
				MessageBitSection sec = new MessageBitSection(10);
				sec.setTimeToLive(timeToLive);
				final int n = Math.min(sendRemovedAreas.size(), MAX_SEND_SIZE);
				sec.add(n, SEND_SIZE_BIT);
				for (int i = 0; i < n; i++) {
					comUtil.writeArea(sec, sendRemovedAreas.get(i), world.getUniform());
				}
				sendRemovedAreas.clear();
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
			public void read(EntityID sender, final int time, BitArrayInputStream stream) {
				final int n = Math.max(0, stream.readBit(SEND_SIZE_BIT));
				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
				for (int i = 0; i < n ; i++) {
					EntityID id = comUtil.readArea(stream, world.getUniform());
					readString = readString + "areaId = " + id + "\n";
					if (criticalAreas.remove(world.getEntity(id))) {
						criticalAreaIds.remove(id);
					}
				}
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPROTED_TO_PF;
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
					writer.write(" write in createPort, CriticalArea\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("numberToSend = " + numberToSend + "\n");
					for (int i = 0; i < numberToSend; i++) {
						writer.write("areaMraker = " + dataSizePair.get(i * 2 + 1).first().intValue() + "\n");
						writer.write("areaUniform = " + dataSizePair.get(i * 2 + 2).first().intValue() + "\n");
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
					writer.write(" read in createPort, CriticalArea\n");
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

	public int size() {
		return criticalAreas.size();
	}

	public Area get(int index) {
		return (Area) criticalAreas.get(index);
	}

	public List<Area> getAreas() {
		return criticalAreas;
	}
	
	public boolean isCriticalArea(Area area) {
		return this.isCriticalArea(area.getID());
	}
	
	public boolean isCriticalArea(EntityID area) {
		return this.criticalAreaIds.contains(area);
	}
}
