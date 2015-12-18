package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import csu.agent.fb.FireBrigadeAgent;
import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.MessageConstant.MessageReportedType;
import csu.communication.Port;
import csu.io.BitArrayInputStream;
import csu.model.object.CSUBuilding;

public class WaterPort {
	private AdvancedWorldModel world;
	private EntityID owner;
	/** The id of the target extinguish building of this Agent.*/
	private EntityID target = null;
	private int waterPower;
	
	private int ignoreUnit;
	private StandardEntity controlledEntity;
	
	Map<Integer, List<EntityID>> recore = new TreeMap<Integer, List<EntityID>>();
	
	public WaterPort(AdvancedWorldModel world) {
		this.world = world;
		this.owner = world.getAgent().getID();
		this.ignoreUnit = world.getConfig().ignoreUntil;
		this.controlledEntity = world.getControlledEntity();
	}
	
	public void update(AdvancedWorldModel world) {
		for(Iterator<Integer> itor = recore.keySet().iterator(); itor.hasNext(); ) {
			int next = itor.next().intValue();
			if (world.getTime() - next > 10)
				itor.remove();
		}
	}
	
	public Port createWaterPort(final CommunicationUtil comUtil, final int timeToLive) {
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
				if (newPacket != null)
					packetList.add(newPacket);
				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
					if (it.next().getTimeToLive() <= 0) {
						it.remove();
					}
				}
			}
			
			private  MessageBitSection createNewPacket() {
				if (controlledEntity instanceof FireBrigade && world.getTime() >= ignoreUnit) {
					FireBrigadeAgent fbAgent = (FireBrigadeAgent)world.getAgent();
					StandardMessageURN lastTimeMessage = fbAgent.getCommandHistory(world.time);
					StandardMessageURN extinguishMessage = StandardMessageURN.AK_EXTINGUISH;
					
					if (lastTimeMessage != null && lastTimeMessage.equals(extinguishMessage)) {
						target = fbAgent.extinguishTarget;
						waterPower = fbAgent.waterPower;
						fbAgent.waterPower = 0;
					} else {
						target = null;
						waterPower = 0;
					}
				} else {
					target = null;
					waterPower = 0;
				}
				
				if (target == null || waterPower <= 0)
					return null;
				MessageBitSection sec = new MessageBitSection(3);
				sec.setTimeToLive(timeToLive);
				int uniform = world.getUniform().toUniform(target);
				sec.add(uniform, comUtil.BUILDING_UNIFORM_BIT_SIZE);
				sec.add(waterPower, comUtil.WATER_POWER_BIT_SIZE);
				sec.add(world.getTime(), comUtil.TIME_BIT_SIZE);
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
				readString = "sender: " + sender + ", senderTime = " + time + "\n";
				int uniform = stream.readBit(comUtil.BUILDING_UNIFORM_BIT_SIZE);
				EntityID id = world.getUniform().toID(StandardEntityURN.BUILDING, uniform);
				readString = readString + "uniform = " + uniform + ", id = " + id + "\n";
				int waterPower = stream.readBit(comUtil.WATER_POWER_BIT_SIZE);
				readString = readString + "waterPower = " + waterPower + "\n";
				int firstSendTime = stream.readBit(comUtil.TIME_BIT_SIZE);
				readString = readString + "firstSendTime = " + firstSendTime + "\n";

				/*
				 * If this message send by me, this means the reported building
				 * is extinguished by me, and I had increased its water
				 * quantity. So there is no need to increase its water quantity
				 * anymore.
				 */
				if (owner.getValue() == sender.getValue())
					return;
				
				List<EntityID> values = recore.get(new Integer(firstSendTime));
				if (values != null) {
					if (values.contains(sender))
						return;
					else
						values.add(sender);
				} else {
					List<EntityID> list = new ArrayList<>();
					list.add(sender);
					recore.put(new Integer(firstSendTime), list);
				}

				if (id == null)
					return;
				CSUBuilding csuBuilding = world.getCsuBuilding(id);
				if (waterPower >= world.getConfig().maxPower) {
					waterPower = world.getConfig().maxPower;
				}
				
//				if (AgentConstants.FB) {
//					System.out.println(world.getTime() + ", " + world.getControlledEntity()
//							+ " increase water quantity of building = " + csuBuilding.getId() 
//							+ ", and sender = " + sender + ", firstSendTime = " + firstSendTime);
//					System.out.println("------WaterPort: read");
//				}
				
				csuBuilding.setWaterQuantity(csuBuilding.getWaterQuantity() + waterPower);
			}

			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTED_TO_FB;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					int targetUniform = dataSizePair.get(0).first().intValue();
					int targetId = world.getUniform().toID(StandardEntityURN.BUILDING, targetUniform).getValue();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createWaterPort, WaterPort\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("targetUniform = " + targetUniform + ", targetId = " + targetId + "\n");
					writer.write("waterPower = " + dataSizePair.get(1).first().intValue() + "\n");
					writer.write("firstSendTime = " + dataSizePair.get(2).first().intValue() + "\n");
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
					writer.write(" read in createWaterPort, WaterPort");
					writer.write(" from channel: " + channel + "\n");
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
}
