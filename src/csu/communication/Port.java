package csu.communication;

import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * 通信接口类。如果需要收发某种类型的消息，需要通过实现该接口来实现。
 * <p>
 * 在我们代码中，需要处理很多种消息，例如着火的建筑物和被埋的市民就是两种不同类型的消息。
 * 由于不同类型的消息需要处理的内容不同，不可能将它们混在一起处理。将不同的消息分开处理，
 * 我们就需要一套标准来约束每种类型的消息的行为，使得代码更加简洁规范。而Port接口就是这
 * 个标准。每种类型的消息都需要遵守这套规则，也就是每种消息都需要实现这个接口。
 * <p>
 * 在代码中，我们创建了很多个Port，可以简单的认为，一个Port就是一种类型的消息。而我们
 * 代码中消息收发的基本单位是MessageBitSection，也就是说每个Port下面可能会有多个
 * MessageBitSection。
 * <p>
 * 不同类型的消息处理不同的内容，所以不同的Port下面的MessageBitSecion中包含的内容格式
 * 是不一样的，而同一个Port下的MessageBitSection中的内容格式是一样的。因此每个Port
 * 需要有自己特定的读(Read)和写(Write)方法来处理它下面的MessageBitSection。
 * <p>
 * 不同类型的消息，它的接受对象也是不一样的，有些消息只是FB需要，有些消息只是PF需要，另外
 * 还有些消息是大家都需要的，因此，我们还需要确定这种类型的消息的发送类型，既这种消息需要
 * 发送给谁。这个是由MessageReportedType决定的。
 * 
 * @see MessageBitSection
 * @see MessageConstant.MessageReportedType
 */
public interface Port {
	/**
	 * 初始化操作，用于决定这个周期需要发送的内容，也就是创建新的MessageBitSection。
	 * 
	 * @param changed
	 *            the change set
	 */
	public void init(final ChangeSet changed);

	/**
	 * 用于判断当前周期该类型的消息下面是否还有MessageBitSection需要发送，如果有的话，返回true。
	 * 否则，返回false。
	 */
	public boolean hasNext();

	/**
	 * 获取该消息类型下面下一个将要发送的MessageBitSection。
	 * 
	 * @return 下一个将要发送的MessageBitSection
	 */
	public MessageBitSection next();

	/**
	 * 该类型消息的特定读方法，由该消息下的MessageBitSection的格式决定。
	 * 
	 * @param sender
	 *            发送者的ID
	 * @param time
	 *            消息的发送时间
	 * @param stream
	 *            input stream消息字节流
	 */
	public void read(final EntityID sender, final int time, final BitArrayInputStream stream);

	/**
	 * 获取消息的发送类型。这个类型是人为规定的，既你认为这个消息需要发送给谁，就规定为相应的发送类型。
	 * 
	 * @return 这种消息的发送类型
	 */
	public MessageReportedType getMessageReportedType();
	
	/**
	 * 打印将要发送的消息，仅用于测试。
	 * 
	 * @param packet
	 *            the packet will write into channel
	 * @param channel
	 *            the channel this message will write to
	 */
	public void printWrite(MessageBitSection packet, int channel);
	
	/**
	 * 打印接受到的消息，仅用于测试。
	 * 
	 * @param channel
	 *            the channel read message from
	 */
	public void printRead(int channel);
	
	public void resetCounter();

}
