package csu.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import csu.agent.at.cluster.Cluster;
import csu.communication.CommunicationUtil;
import csu.communication.MessageBitSection;
import csu.communication.MessageConstant.MessageReportedType;
import csu.communication.Port;
import csu.io.BitArrayInputStream;
import csu.util.BitUtil;

/**
 * This class mainly store the remain clusers 
 * and sent/read the message of removed cluster.
 * 
 * @author nale
 *
 */
public class RemainCluster {

	/** To store the index of the new removed cluster*/
	private Set<Integer> removeClusterIndex;
	
	/** To store the remain cluster to search*/
	private Set<Cluster> remainClusters;
	
	/** To store all the clusters*/
	private ArrayList<Cluster> allClusters;
	
	/** The max bits of the cluster index needs*/
	private static final int CLUSTER_BIT = BitUtil.needBitSize(50);
	
	/** The AdvancedWorldModel*/ 
	private AdvancedWorldModel world;
	
	/** The maximum cluster index*/
	private final int MAX_CLUSTER_INDEX ;
	
	public RemainCluster(AdvancedWorldModel world){
		this.world = world;
		removeClusterIndex = new HashSet<Integer>();
		MAX_CLUSTER_INDEX = world.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM).size();
	}
	
	/**
	 * To initialize the object , only used by AT
	 * @param allClusters
	 */
	public void init(ArrayList<Cluster> allClusters){
		this.allClusters = allClusters;
		remainClusters = new HashSet<Cluster>(allClusters);
	}
	
	/**
	 * To remove the cluster , only used by AT
	 * @param index
	 */
	public void remove(int index){
		removeClusterIndex.add(index);
		remainClusters.remove(allClusters.get(index));
//		System.out.println(world.me + "  Time : " + world.getTime() + "  remove cluster : " + removeClusterIndex);
	}
	
	/**
	 * The get remain cluster , only used by AT
	 * @return
	 */
	public Set<Cluster> getRemainClusters(){
		return remainClusters;
	}
	
	public Port createRemovePort(final CommunicationUtil comUtil){
		return new Port(){

			private ArrayList<MessageBitSection> secToSent = new ArrayList<MessageBitSection>();
			private int timeToLive = 2;
			private int sentIndex;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				for (Iterator<MessageBitSection> iterator = secToSent.iterator() ; iterator.hasNext();){
					MessageBitSection sec = iterator.next();
					if (sec.getTimeToLive() <= 0)
						iterator.remove();
				}
				if (!removeClusterIndex.isEmpty()){
					MessageBitSection sec = createNewSection(7);
					sec.setTimeToLive(timeToLive);
					secToSent.add(sec);
				}
				sentIndex = secToSent.size();
			}

			@Override
			public boolean hasNext() {
				return (sentIndex > 0);
			}

			@Override
			public MessageBitSection next() {
				sentIndex -- ;
				return secToSent.get(sentIndex);
			}
			
			private MessageBitSection createNewSection(int priority){
				MessageBitSection sec = new MessageBitSection(priority);
				int n = removeClusterIndex.size();
				sec.add(n, CLUSTER_BIT);
				for (Integer index : removeClusterIndex){
					sec.add(index , CLUSTER_BIT);
				}
				removeClusterIndex.clear();
				return sec;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(CLUSTER_BIT);
				readString = "sender: " + sender + ", sendTime = " + time;
				for (int i = 0 ; i < n ; i++){
					final int removeIndex = stream.readBit(CLUSTER_BIT);
					readString = readString + ", removeIndex = " + removeIndex;
					if (world.getControlledEntity().getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString())){
						if (removeIndex >= MAX_CLUSTER_INDEX || removeIndex < 0)
							return ;
						else {
//							System.out.println(world.me + "  Time : " + world.getTime() + "  get remove Cluster : " + removeIndex);
							remainClusters.remove(allClusters.get(removeIndex));
						}
					}
				}
				readString = readString + "\n";
			}

			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTED_TO_AT;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePort, RemainCluster\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("removeIndex = " + dataSizePair.get(0).first().intValue() + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
//				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//				System.out.println(" write in createRemovePort, RemainCluster");
//				System.out.println("priority = " + packet.getPriority());
//				List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
//				System.out.println("removeIndex = " + dataSizePair.get(0).first().intValue());
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
					writer.write(" read in createRemovePort, RemainCluster\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
				
//				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//				System.out.println(" read in createRemovePort, RemainCluster");
//				System.out.println(readString);
//				System.out.println();
//				readString = "";
			}
		};
	}
	
	public Port createRemovePortForNoRadio(final CommunicationUtil comUtil){
		return new Port(){

			private ArrayList<Integer> removeList = new ArrayList<Integer>();
			private boolean removeSent = false;
			private static final int MAX_SENT = 5;
			private MessageBitSection secToSent;
			private String readString = "";
			
			@Override
			public void resetCounter() {
				// counter = 0;
			}
			
			@Override
			public void init(ChangeSet changed) {
				if (!removeClusterIndex.isEmpty()){
					removeList.addAll(removeClusterIndex);
					removeClusterIndex.clear();
				}
				
				if (!removeList.isEmpty()){
					secToSent = createMessageBitSection(20);
					removeSent = true;
				}
				else removeSent = false;
			}

			@Override
			public boolean hasNext() {
				return removeSent;
			}

			@Override
			public MessageBitSection next() {
				removeSent = false;
				return secToSent;
			}

			@Override
			public void read(EntityID sender, int time, BitArrayInputStream stream) {
				final int n = stream.readBit(CLUSTER_BIT);
				readString = "sender: " + sender + ", sendTime = " + time;
				for (int i = 0 ; i < n ; i++){
					final int removeIndex = stream.readBit(CLUSTER_BIT);
					readString = readString + ", removeIndex = " + removeIndex;
					if (removeIndex >= MAX_CLUSTER_INDEX || removeIndex < 0)
						return ;
					else if (!removeList.contains(removeIndex)){
						removeList.add(removeIndex);
						if (world.me.getURN().equals(StandardEntityURN.AMBULANCE_TEAM.toString()))
							remainClusters.remove(allClusters.get(removeIndex));
					}
				}
				readString = readString + "\n";
			}
			
			@Override
			public MessageReportedType getMessageReportedType() {
				return MessageReportedType.REPORTRD_TO_ALL;
			}
			
			public MessageBitSection createMessageBitSection(int prioriy){
				MessageBitSection sec = new MessageBitSection(prioriy);
				int sentCount = Math.min(removeList.size() , MAX_SENT);
				sec.add(sentCount , CLUSTER_BIT);
				for (int i = 0 , j = removeList.size() - 1 ; i < sentCount ; i++ , j--){
					sec.add(removeList.get(j).intValue() , CLUSTER_BIT);
				}
				return sec;
			}
			
			@Override
			public void printWrite(MessageBitSection packet, int channel) {
				try {
					List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
					
					int id = world.getControlledEntity().getID().getValue();
					String fileName = "commOutput/write-" + id;
					File file = new File(fileName);
					if (!file.exists())
						file.createNewFile();
					
					FileWriter writer = new FileWriter(fileName, true);
					writer.write("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
					writer.write(" write in createRemovePortForNoRadio, RemainCluster\n");
					writer.write("priority = " + packet.getPriority() + " channel = " + channel + "\n");
					writer.write("removeIndex = " + dataSizePair.get(0).first().intValue() + "\n");
					writer.write("\n");
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
//				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//				System.out.println(" write in createRemovePortForNoRadio, RemainCluster");
//				System.out.println("priority = " + packet.getPriority());
//				List<Pair<Integer, Integer>> dataSizePair = packet.getDataSizePair();
//				System.out.println("removeIndex = " + dataSizePair.get(0).first().intValue());
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
					writer.write(" read in createRemovePortForNoRadio, RemainCluster\n");
					writer.write("read from channel: " + channel + "\n");
					writer.write(readString + "\n");
					writer.write("\n");
					writer.close();
					
					readString = "";
				} catch (IOException e) {
					e.printStackTrace();
				}
				
//				System.out.print("In time: " + world.getTime() + " agent: " + world.getControlledEntity());
//				System.out.println(" read in createRemovePortForNoRadio, RemainCluster");
//				System.out.println(readString);
//				System.out.println();
//				readString = "";
			}
		};
	}
}
