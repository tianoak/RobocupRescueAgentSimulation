//package csu.model;
//
//import java.awt.geom.Ellipse2D;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import csu.communication.CommunicationUtil;
//import csu.communication.MessageBitSection;
//import csu.communication.Port;
//import csu.communication.MessageConstant.MessageReportedType;
//import csu.io.BitArrayInputStream;
//import csu.util.BitUtil;
//
//import rescuecore2.misc.Pair;
//import rescuecore2.standard.entities.AmbulanceTeam;
//import rescuecore2.standard.entities.Blockade;
//import rescuecore2.standard.entities.FireBrigade;
//import rescuecore2.standard.entities.Human;
//import rescuecore2.standard.entities.PoliceForce;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.standard.entities.StandardEntityURN;
//import rescuecore2.worldmodel.ChangeSet;
//import rescuecore2.worldmodel.EntityID;
//
//public class StuckHandler{
//	private ArrayList<Human> addRecoreds = null;
//	private ArrayList<Human> removeRecords = null;
//	
//	/**
//	 * The human(map.key) located in the road(map.value.first()) and stucked by
//	 * blockade(map.value.second())
//	 */
//	private Map<EntityID, Pair<EntityID, EntityID>> stuckedAgentLocationMap = null;
//	
//	private List<EntityID> stuckedAgent = null;
//	
//	private AdvancedWorldModel world;
//	
//	private int stuckThreshold;
//    private int randomCount = 10;
//    private Random random;
//	
//	// constructor      stuckThreshold = 80, randomCount = 6
//	public StuckHandler(AdvancedWorldModel world, int stuckThreshold, int randomCount) {
//		this.world = world;
//		
//		this.addRecoreds = new ArrayList<Human>();
//		this.removeRecords = new ArrayList<Human>();
//		this.stuckedAgent = new ArrayList<>();
//		this.stuckedAgentLocationMap = new HashMap<EntityID, Pair<EntityID, EntityID>>();
//		
//		this.stuckThreshold = stuckThreshold;
//		this.random = new Random(System.currentTimeMillis());
//	}
//	
//	/** Update stucked human in this world model.*/
//	public void update(ChangeSet changeSet) {
////		this.addRecoreds.clear();
////		this.removeRecords.clear();
////		for (EntityID id : changeSet.getChangedEntities()) {
////			StandardEntity entity = world.getEntity(id);
////		
////			switch (entity.getStandardURN()) {
////			case FIRE_BRIGADE: 
////			case AMBULANCE_TEAM: 
////			case POLICE_FORCE: {
////				final Human human = (Human) entity;
////				if (getStuck(human) != null) {
////					EntityID roadId = human.getPosition();  		// the road this Agent on
////					EntityID blockadeId = getStuck(human).getID();  // the blockade stucked this human
////					Pair<EntityID, EntityID> pair = new Pair<EntityID, EntityID>(roadId, blockadeId);
////					
////					if (!this.stuckedAgentLocationMap.containsKey(human.getID())) {
////						this.stuckedAgentLocationMap.put(human.getID(), pair);
////						this.stuckedAgent.add(human.getID());
////						this.addRecoreds.add(human);
////					}
////				} else {
////					this.stuckedAgentLocationMap.remove(human.getID());
////					stuckedAgent.remove(human.getID());
////					this.removeRecords.add(human);
////				}
////			}
////			break;
////			default:
////				break;
////			}
////		}
//	}
//	
//	
//	/** Get the blockade target human stucked in.*/
//	public Blockade getStuck(Human human) {
//		if (human.isXDefined() && human.isYDefined()) {
//			return isLocateInBlockade(human.getX(), human.getY());
//		}
//		return null;
//	}
//
//	/** Determines whether target human was stucked in blockade.*/
//	public Blockade isLocateInBlockade(Pair<Integer, Integer> human) {
//		return isLocateInBlockade(human.first(), human.second());
//	}
//	
//	private Blockade isLocateInBlockade(int x, int y) {
//		Ellipse2D humanShape = new Ellipse2D.Double(x - 600, y - 600, 600, 600);
//		java.awt.geom.Area humanArea = new java.awt.geom.Area(humanShape);
//		
//		for (StandardEntity se : world.getEntitiesOfType(StandardEntityURN.BLOCKADE)) {
//			Blockade blockade = (Blockade) se;
//			
//			java.awt.geom.Area blockadeArea = new java.awt.geom.Area(blockade.getShape());
//			blockadeArea.intersect(humanArea);
//			
//			if (blockadeArea.getPathIterator(null).isDone())
//				continue;
//			else
//				return blockade;
//		}
//		return null;
//	}
//	
//	/**
//	 * Get all stucked humans this Agent has known, including stuckedAgents and
//	 * stuckedCivilians.
//	 */
//	public Map<EntityID, Pair<EntityID, EntityID>> getStuckedHumans() {
//		Map<EntityID, Pair<EntityID, EntityID>> stuckedHumans;
//		stuckedHumans = new HashMap<EntityID, Pair<EntityID, EntityID>>();
//
//		stuckedHumans.putAll(stuckedAgentLocationMap);
//
//		return stuckedHumans;
//	}
//	
//	public List<EntityID> getStuckedAgent() {
//		return this.stuckedAgent;
//	}
//
//	
////	public boolean isBlockedInARegion() {
////        boolean isStuck = false;
////        if (!(world.getControlledEntity() instanceof PoliceForce)) {
////            Pair<Integer, Integer> location = world.getSelfLocation();
////            List<EntityID> possibleTargets;
////            possibleTargets = findPossibleTargets(location.first(), location.second());
////            if (possibleTargets.size() > 0) {
////                isStuck = checkStatus(chooseRandomTargets(possibleTargets));
////            }
////
////        }
////        return isStuck;
////    }
////
////    private Set<EntityID> chooseRandomTargets(List<EntityID> targets) {
////        Set<EntityID> randomTargets = new FastSet<EntityID>();
////        int rand;
////        int count = 0;
////        int tryCount = 1000;
////        int minCount = Math.min(randomCount, targets.size());
////        List<EntityID> tempTargetList = new ArrayList<EntityID>(targets);
////
////        while (count < minCount) {
////            rand = random.nextInt(tempTargetList.size());
////
////            tryCount--;
////            if (tryCount < 0) {
////                System.err.println(" RIDIIIII " + world.getAgent() + " " + world.getTime());
////            }
////
////            randomTargets.add(tempTargetList.get(rand));
////            tempTargetList.remove(rand);
////
////            count++;
////        }
////
////        return randomTargets;
////    }
////
////
////	/**
////	 * this function create two circles, first is inner circle that centre is
////	 * human(x,y) and radius viewDistance second is outer circle that centre is
////	 * human(x,y) and radius is viewDistance * 1.5
////	 * 
////	 * @param x
////	 *            integer representing the x position of the agent in the map
////	 * @param y
////	 *            integer representing the x position of the agent in the map
////	 * @return
////	 */
////    private List<EntityID> findPossibleTargets(int x, int y) {
////        List<EntityID> targets = new ArrayList<EntityID>();
////        int radius = world.getConfig().viewDistance;
////        double minorCircle;
////        double majorCircle;
////        if (world.isMapHuge()) {
////            radius *= 2;
////        }
////        minorCircle = radius * 2;
////        majorCircle = radius * 2.5;
////        Circle2D innerCircle = new Circle2D(new Point2D(x, y), minorCircle);
////        Circle2D outerCircle = new Circle2D(new Point2D(x, y), majorCircle);
////
////        targets = findPoints(innerCircle, outerCircle, majorCircle);
////        return targets;
////    }
////
////	/**
////	 * findPoints functions find all of entities that in area between
////	 * innerCircle and outerCircle
////	 * 
////	 * @param innerCircle
////	 *            the Circle with agent(x, y) and viewDistance radius
////	 * @param outerCircle
////	 *            the Circle with agent(x, y) and 1.5 * viewDistance radius
////	 * @param majorCircle
////	 */
////    private List<EntityID> findPoints(Circle2D innerCircle, Circle2D outerCircle, double majorCircle) {
////        List<EntityID> targets = new ArrayList<EntityID>();
////        Pair<Integer, Integer> location;
////
////        Collection<StandardEntity> objectInRange;
////        objectInRange = world.getObjectsInRange(world.getSelfPosition(), (int) Math.ceil(majorCircle));
////        for (StandardEntity se : objectInRange) {
////            location = se.getLocation(world);
////            if (contains(outerCircle, location.first(), location.second())
////                    && !contains(innerCircle, location.first(), location.second())) {
////                targets.add(se.getID());
////            }
////        }
////        return targets;
////    }
////
////
////	/**
////	 * In this function we send move act to all of @targets and if more than
////	 * stuckThreshold move act is failed, this human is in stuck else agent is
////	 * free
////	 * 
////	 * @param entityIDs
////	 *            , roads and buildings entityIds that choice random from
////	 *            targets
////	 * @return boolean true if the agent is stuck
////	 */
////    private boolean checkStatus(Set<EntityID> entityIDs) {
////
////        if (entityIDs == null || entityIDs.isEmpty()) {
////            return false;
////        }
////
////        int unSuccessfulMove = 0;
////
////        StandardEntity se;
////        Area source = (Area) world.getSelfPosition();
////        for (EntityID id : entityIDs) {
////            se = world.getEntity(id);
////            if (se instanceof Area) {
////            	if (world.getRouter().getAStar(source, (Area)se, new Point(
////            			world.getSelfLocation().first(), world.getSelfLocation().second())).isEmpty()) {
////                    unSuccessfulMove++;
////                }
////            }
////        }
////
////        if (((unSuccessfulMove / entityIDs.size()) * 100) > stuckThreshold) {
////            return true;
////        }
////        return false;
////    }
////
////    private boolean contains(Circle2D circle2D, double x, double y) {
////        double dx = circle2D.getCenter().getX() - x;
////        double dy = circle2D.getCenter().getY() - y;
////
////        return ((dx * dx) + (dy * dy)) < circle2D.getRadius() * circle2D.getRadius();
////    }
//	
//	
//	
///* ------------------------------- following codes are creating Ports ------------------------------------- */	
//	
//	/** Maximum number of human can reported each time.*/
//	private final int MAX_NUMBER = 31;
//	private final int NUMBER_BIT_SIZE = BitUtil.needBitSize(MAX_NUMBER);
//	
//	public Port createPort(final StandardEntity me, final CommunicationUtil comUtil, final int timeToLive) {
//		return new Port() {
//			private List<MessageBitSection> packetList = new ArrayList<>();
//			private int counter;
//			private String readString = "";
//			
//			@Override
//			public void init(ChangeSet changed) {
//				MessageBitSection newPacket = createNewPacket();
//				if (newPacket != null)
//					packetList.add(newPacket);
//				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();) {
//					if (it.next().getTimeToLive() <= 0)
//						it.remove();
//				}
//			}
//			
//			private MessageBitSection createNewPacket() {
//				if (addRecoreds.isEmpty())
//					return null;
//				MessageBitSection sec = new MessageBitSection(2);
//				sec.setTimeToLive(timeToLive);
//				final int n = Math.min(MAX_NUMBER, addRecoreds.size());
//				sec.add(n, NUMBER_BIT_SIZE);
//				for (int i = 0; i < n; i++) {
//					Human human = addRecoreds.get(i);
//					// the uniform of target agent
//					final int agentUniform = world.getUniform().toUniform(human.getID());
//					// the uniform of road that target agent is on
//					final int roadUniform = world.getUniform().toUniform(human.getPosition());
//					// the is of blockade that target agent stucked in
//					final int blockade = getStuck(human).getID().getValue();
//					
//					if (human instanceof FireBrigade)
//						sec.add('f', 8);
//					if (human instanceof PoliceForce)
//						sec.add('p', 8);
//					if (human instanceof AmbulanceTeam)
//						sec.add('a', 8);
//					sec.add(agentUniform, comUtil.AGENT_UNIFORM_BIT_SIZE);
//					sec.add(roadUniform, comUtil.ROAD_UNIFORM_BIT_SIZE);
//					sec.add(blockade, comUtil.UINT_BIT_SIZE);
//				}
//				
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
//			public MessageBitSection next() {
//				MessageBitSection next = packetList.get(counter);
//				counter++;
//				// next.decrementTTL();
//				return next;
//			}
//			
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				final int n = stream.readBit(NUMBER_BIT_SIZE);
//				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
//				for (int i = 0; i < n; i++) {
//					char mark = (char)stream.readBit(8);
//					readString = readString + "marker = " + mark + "\n";
//					final int agentUniform = stream.readBit(comUtil.AGENT_UNIFORM_BIT_SIZE);
//					readString = readString + "agentUniform = " + agentUniform + "\n";
//					EntityID agentId = null;
//					switch (mark) {
//					case 'f': {
//						agentId = world.getUniform().toID(StandardEntityURN.FIRE_BRIGADE, agentUniform);
//					}
//					break;
//					case 'p': {
//						agentId = world.getUniform().toID(StandardEntityURN.POLICE_FORCE, agentUniform);
//					}
//					break;
//					case 'a': {
//						agentId = world.getUniform().toID(StandardEntityURN.AMBULANCE_TEAM, agentUniform);
//					}
//					break;
//					default: 
//						break;
//					}
//					
//					final int roadUniform = stream.readBit(comUtil.ROAD_UNIFORM_BIT_SIZE);
//					readString = readString + "roadUniform = " + roadUniform + "\n";
//					EntityID roadId = world.getUniform().toID(StandardEntityURN.ROAD, roadUniform);
//					
//					final int blockade = stream.readBit(comUtil.UINT_BIT_SIZE);
//					readString = readString + "blockadeId = " + blockade + "\n";
//					EntityID blockadeId = new EntityID(blockade);
//					StandardEntity entity = world.getEntity(blockadeId);
//					if (entity != null && entity instanceof Blockade) {
//						blockadeId = entity.getID();
//					} else {
//						blockadeId = null;
//					}
//					
//					if (agentId == null)
//						continue;
//					if (blockadeId == null)
//						continue;
//					if (stuckedAgentLocationMap.containsKey(agentId))
//						continue;
//					stuckedAgentLocationMap.put(agentId, new Pair<EntityID, EntityID>(roadId, blockadeId));
//					stuckedAgent.add(agentId);
//				}
//			}
//			
//			@Override
//			public MessageReportedType getMessageReportedType() {
//				return MessageReportedType.REPROTED_TO_PF;
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
//					writer.write(" write in createPort, StuckHandler\n");
//					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
//					writer.write("numberToSend = " + numberToSend + "\n");
//					for (int i = 0; i < numberToSend; i++) {
//						char marker = (char)dataSizePair.get(i * 4 + 1).first().intValue();
//						writer.write("agentTypeMarker = " + marker);
//						writer.write("agentUniform = " + dataSizePair.get(i * 4 + 2).first().intValue() + "\n");
//						writer.write("roadUniform = " + dataSizePair.get(i * 4 + 3).first().intValue() + "\n");
//						writer.write("blockadeId = " + dataSizePair.get(i * 4 + 4).first().intValue() + "\n");
//					}
//					writer.write("\n");
//					writer.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
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
//					writer.write(" read in createPort, StuckHandler\n");
//					writer.write("read from channel: " + channel + "\n");
//					writer.write(readString + "\n");
//					writer.write("\n");
//					writer.close();
//					
//					readString = "";
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		};
//	}
//
//	public Port createRemovePort(final StandardEntity me, final CommunicationUtil comUtil) {
//		return new Port() {
//			private List<MessageBitSection> packetList = new ArrayList<>();
//			private int counter = 0;
//			private String readString = "";
//		
//			@Override
//			public void read(EntityID sender, int time, BitArrayInputStream stream) {
//				final int n = stream.readBit(NUMBER_BIT_SIZE);
//				readString = "sender: " + sender + ", sendTime = " + time + ", numberToRead = " + n + "\n";
//				for (int i = 0; i < n; i++) {
//					final char mark = (char)stream.readBit(8);
//					readString = readString + "marker = " + mark + "\n";
//					final int uniform = stream.readBit(comUtil.AGENT_UNIFORM_BIT_SIZE);
//					readString = readString + "agentUniform = " + uniform + "\n";
//					EntityID agentId = null;
//					switch (mark) {
//					case 'f': {			
//						agentId = world.getUniform().toID(StandardEntityURN.FIRE_BRIGADE, uniform);
//					}
//						break;
//					case 'p': {
//						agentId = world.getUniform().toID(StandardEntityURN.POLICE_FORCE, uniform);
//					}
//						break;
//					case 'a': {
//						agentId = world.getUniform().toID(StandardEntityURN.AMBULANCE_TEAM, uniform);
//					}
//						break;
//					default:
//						break;
//					}
//					if (agentId == null)
//						continue;
//					stuckedAgentLocationMap.remove(agentId);
//					stuckedAgent.remove(agentId);
//				}
//			}
//			
//			@Override
//			public MessageBitSection next() {
//				MessageBitSection next = packetList.get(counter);
//				// next.decrementTTL();
//				counter++;
//				return next;
//			}
//			
//			@Override
//			public void init(ChangeSet changed) {
//				MessageBitSection newPacket = createNewPacket();
//				if (newPacket != null)
//				    packetList.add(newPacket);
//				for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
//					if (it.next().getTimeToLive() <= 0)
//						it.remove();
//			}
//			
//			private MessageBitSection createNewPacket() {
//				if (removeRecords.isEmpty())
//					return null;
//				MessageBitSection sec = new MessageBitSection(30);
//				sec.setTimeToLive(1);
//				final int n = Math.min(MAX_NUMBER, removeRecords.size());
//				sec.add(n, NUMBER_BIT_SIZE);
//				for (int i = 0; i < n; i++) {
//					Human human = removeRecords.get(i);
//					if (human instanceof FireBrigade) {
//						sec.add('f', 8);
//					}
//					if (human instanceof PoliceForce) {
//						sec.add('p', 8);
//					}
//					if (human instanceof AmbulanceTeam) {
//						sec.add('a', 8);
//					}
//					final int uniform = world.getUniform().toUniform(human.getID());
//					sec.add(uniform, comUtil.AGENT_UNIFORM_BIT_SIZE);
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
//				return MessageReportedType.REPROTED_TO_PF;
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
//					writer.write(" write in createRemovePort, StuckHandler\n");
//					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
//					writer.write("numberToSend = " + numberToSend + "\n");
//					for (int i = 0; i < numberToSend; i++) {
//						char marker = (char)dataSizePair.get(i * 2 + 1).first().intValue();
//						writer.write("agentTypeMarker = " + marker + "\n");
//						writer.write("agentUniform = " + dataSizePair.get(i * 2 + 2).first().intValue() + "\n");
//					}
//					writer.write("\n");
//					writer.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
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
//					writer.write(" read in createRemovePort, StuckHandler\n");
//					writer.write("read from channel: " + channel + "\n");
//					writer.write(readString + "\n");
//					writer.write("\n");
//					writer.close();
//					
//					readString = "";
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		};
//	}	
//
////	public Port createNoComPort(final CommunicationUtil comUtil, final boolean isAggregator, final boolean isMessenger) {
////		if (isAggregator) 
////			return new NoComPort(comUtil, 180);
////		if (isMessenger)
////			return new NoComPort(comUtil, 80);
////		else
////			return new NoComPort(comUtil, 3);
////	}
////	
////	private class NoComPort implements Port {
////		private boolean flag;
////		private java.util.List<EntityID> reportedHuman;
////		final private CommunicationUtil comUtil;
////		final private int remain;
////		
////		public NoComPort(CommunicationUtil comUtil, int remain) {
////			this.comUtil = comUtil;
////			this.remain = remain;
////			this.reportedHuman = new ArrayList<EntityID>();
////		}
////		
////		@Override
////		public void init(ChangeSet changed) {
////			for (Iterator<EntityID> it = reportedHuman.iterator(); it.hasNext();) {
////				EntityID id = it.next();
////				if (remain < world.getTime() - world.getTimestamp().getLastChangedTime(id))
////					it.remove();
////			}
////			
////			for (EntityID id : changed.getChangedEntities()) {
////				StandardEntity se = world.getEntity(id);
////				if (se.getStandardURN() == StandardEntityURN.AMBULANCE_TEAM
////						|| se.getStandardURN() == StandardEntityURN.POLICE_FORCE
////						|| se.getStandardURN() == StandardEntityURN.FIRE_BRIGADE) {
////					Human human = (Human) se;
////					if (getStuck(human) != null) {
////						reportedHuman.add(id);
////					}
////				}
////			}
////			this.flag = reportedHuman.isEmpty();
////		}
////
////		@Override
////		public boolean hasNext() {
////			return !flag;
////		}
////
////		@Override
////		public MessageBitSection next() {
////			this.flag = true;
////			MessageBitSection sec = new MessageBitSection(20);
////			final int n = Math.max(MAX_NUMBER, reportedHuman.size());
////			sec.add(n, NUMBER_BIT_SIZE);
////			
////			for (int i = 0; i < n; i++) {
////				Human human = world.getEntity(reportedHuman.get(i), Human.class);
////				final int agentUniform = world.getUniform().toUniform(human.getID());
////				final int roadUniform = world.getUniform().toUniform(human.getPosition());
////				final int blockade = getStuck(human).getID().getValue();
////				if (human instanceof FireBrigade)
////					sec.add('f', 8);
////				if (human instanceof PoliceForce)
////					sec.add('p', 8);
////				if (human instanceof AmbulanceTeam)
////					sec.add('a', 8);
////				
////				sec.add(agentUniform, comUtil.AGENT_UNIFORM_BIT_SIZE);
////				sec.add(roadUniform, comUtil.ROAD_UNIFORM_BIT_SIZE);
////				sec.add(blockade, comUtil.UINT_BIT_SIZE);
////				sec.add(world.getTimestamp().getLastChangedTime(human.getID()), comUtil.TIME_BIT_SIZE);
////			}
////			return sec;
////		}
////
////		@Override
////		public void read(EntityID sender, int time, BitArrayInputStream stream) {
////			final int n = stream.readBit(NUMBER_BIT_SIZE);
////			for (int i = 0; i < n; i++) {
////				char mark = (char)stream.readBit(8);
////				final int agentUniform = stream.readBit(comUtil.AGENT_UNIFORM_BIT_SIZE);
////				final int roadUniform = stream.readBit(comUtil.ROAD_UNIFORM_BIT_SIZE);
////				final int blockade = stream.readBit(comUtil.UINT_BIT_SIZE);
////				final int messageTime = stream.readBit(comUtil.TIME_BIT_SIZE);
////				
////				EntityID agentId = null;
////				switch (mark) {
////				case 'f': {
////					agentId = world.getUniform().toID(StandardEntityURN.FIRE_BRIGADE, agentUniform);
////				}
////				break;
////				case 'p': {
////					agentId = world.getUniform().toID(StandardEntityURN.POLICE_FORCE, agentUniform);
////				}
////				break;
////				case 'a': {
////					agentId = world.getUniform().toID(StandardEntityURN.AMBULANCE_TEAM, agentUniform);
////				}
////				break;
////				default: 
////					break;
////				}
////				
////				if (agentId == null)
////					continue;
////				if (messageTime < world.getTimestamp().getLastChangedTime(agentId))
////					continue;
////				
////				EntityID roadId = world.getUniform().toID(StandardEntityURN.ROAD, roadUniform);
////				EntityID blockadeId = new EntityID(blockade);
////				blockadeId = world.getEntity(blockadeId, Blockade.class).getID();
////				
////				
////				if (stuckedAgents.containsKey(agentId))
////					continue;
////				stuckedAgents.put(agentId, new Pair<EntityID, EntityID>(roadId, blockadeId));
////			}
////		}
////
////		@Override
////		public MessageReportedType getMessageReportedType() {
////			return MessageReportedType.REPROTED_TO_PF;
////		}
////	}
//}