package csu.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import csu.agent.CommunicationAgent;

import rescuecore2.misc.Pair;

/**
 * 该类规定了一套从实际值(字符串和数字)到字节流的转换的规则。
 * <p>
 * 该类中，通过变量{@link MessageBitSection#datasizepairs}来存储实际要发送的消息。
 * datasizepairs是一个list，用于存储Pair。Pair也是一种数据结构，Pair.first表示实际
 * 要发送的内容，Pair.second表是发送这个内容所需要的字节大小。
 * <p>
 * 其实，MessageBitSection就是规定了一种从实际值向字节流转换的规则。在我们的通信中，我们
 * 将MessageBitSection作为一个基本单位。发送的时候，会发送多个MessageBitSection。同时
 * 接受到的也是多个MessageBitSection。因此我们简单的将MessageBitSection称为一个消息
 * 包(packet)。
 * <p>
 * MessageBitSection只是一套转换规则，而实际需要转换的东西是不确定的。不同类型的消息，需要
 * 转换的内容是不同的，也就是说，不同类型的消息所对应的MessageBitSection的格式是不同的。举个
 * 例子：着火建筑这种类型的消息，就需要发送ID，Temperature，Fieryness，也就是三个Pair。而
 * 被埋的市民这种类型的消息就需要发送市民ID，所在建筑物ID，市民受伤程度Damage，市民被埋程度
 * Buriedness和使命生命值HP，也就是五个Pair。
 * <p>
 * 不同的消息类型(Port)下面的MessageBitSection的格式是不一样的，每个Port都有它特定的Read
 * 和write方法来处理与之相关的MessageBitSection。因此在收发的时候，我们需要知道某个特定的
 * MessageBitSection它是属于那个Port，然后再调用那个Port下的Read和Write方法来实现收发。
 * {@link MessageBitSection#headerNumber}就是用来确定一个MessageBitSection属于那个
 * Port的变量。
 * <p>
 * 消息的重要性是不同的，因此我们给MessageBitSection赋予了优先级(priority)这个属性。在通
 * 信带宽受限制的情况下，总是有限考虑优先级高的MessageBitSection。优先级实际上是一个int型
 * 的数，它的值越小，则优先级越高。因此在我们的代码中，1代表最高优先级。
 * <p>
 * 对于零信道或特殊情况，为了确保消息能发送出去，一个MessageBitSection可能需要发送多个周期，
 * {@link MessageBitSection#timeToLive}就是指定这个MessageBitSection需要发送多少个
 * 周期的变量。
 * 
 * @see CommunicationManagerPortsBuilder
 * @see CommunicationManager
 * @see CommunicationAgent
 * 
 * @author apppreciation-csu
 * 
 */
public class MessageBitSection implements Comparable<MessageBitSection>, Iterable<Pair<Integer, Integer>> {

	/** The priority of this message. */
	private int priority;
	/**
	 * Header numbers of this message. And we will set it to the index of its
	 * Port in Port list.
	 */
	private int headerNumber;
	/** The time this message should remain. */
	private int timeToLive = 0;

	/** The bit lenght of this message. */
	private int bitLength = 0;
	/** Stores the data and size pair of each blocks. */
	final ArrayList<Pair<Integer, Integer>> datasizepairs = new ArrayList<Pair<Integer, Integer>>();

	/**
	 * To construct the object of MessageBitSection with the given priority.
	 * 
	 * @param priority
	 *            the priority of this packet
	 */
	public MessageBitSection(final int priority) {
		this.priority = priority;
	}

	/**
	 * Add a new data to this message.
	 * 
	 * @param data
	 *            the new data will be added
	 * @param n
	 *            the bit length of this new data
	 */
	public void add(final int data, final int n) {
		bitLength += n;
		datasizepairs.add(new Pair<Integer, Integer>(data, n));
	}

	/**
	 * Set the priority of this message.
	 * 
	 * @param priority
	 *            the priority of this message
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Compare the priority of this message to a specified message.
	 */
	@Override
	public int compareTo(MessageBitSection o) {
		return this.priority - o.priority;
	}

	/**
	 * Set the header number of this message.
	 * 
	 * @param headerNumber
	 *            the new value of header number
	 */
	public void setHeaderNumber(int headerNumber) {
		this.headerNumber = headerNumber;
	}

	/**
	 * Get the header number of this message.
	 * 
	 * @return the header number of this message
	 */
	public int getHeaderNumber() {
		return headerNumber;
	}
	
	public List<Pair<Integer, Integer>> getDataSizePair() {
		return Collections.unmodifiableList(datasizepairs);
	}

	@Override
	public Iterator<Pair<Integer, Integer>> iterator() {
		return datasizepairs.iterator();
	}

	/**
	 * Get the bit length of this message.
	 * 
	 * @return the bit length of this message
	 */
	public int getBitLength() {
		return bitLength;
	}

	/** Set the time this packet should live. */
	public void setTimeToLive(int time) {
		this.timeToLive = time;
	}

	/** Get the time this packet can live. */
	public int getTimeToLive() {
		return this.timeToLive;
	}

	/** Decrement the live time each cycle. */
	public void decrementTTL() {
		this.timeToLive--;
	}

	public int getPriority() {
		return this.priority;
	}

	@Override
	public String toString() {
		return "package with priority: " + priority + "";
	}
}
