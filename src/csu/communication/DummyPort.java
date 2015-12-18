package csu.communication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import csu.communication.MessageConstant.MessageReportedType;
import csu.io.BitArrayInputStream;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 * A test class for Port, has no meaning.
 * 
 * @author appreciation-csu
 *
 */
public class DummyPort implements Port {

	private List<MessageBitSection> packetList = new ArrayList<>();
	private int counter = 0;

	@Override
	public void init(final ChangeSet changed) {
		MessageBitSection newPacket = createNewPacket();
		if (newPacket != null)
			packetList.add(newPacket);
		for (Iterator<MessageBitSection> it = packetList.iterator(); it.hasNext();)
			if (it.next().getTimeToLive() <= 0)
				it.remove();
	}
	
	private MessageBitSection createNewPacket() {
		MessageBitSection sec = new MessageBitSection(100 - counter);
		sec.setTimeToLive(1);
		sec.add('a' + counter, 8);
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
	public void read(final EntityID sender, final int time, final BitArrayInputStream stream) {
		char a = (char) stream.readBit(8);
		System.out.print(this.getClass().getName() + ": ");
		System.out.println(a);
		if(a !='a'){throw new IllegalStateException();}
	}

	@Override
	public MessageReportedType getMessageReportedType() {
		return MessageReportedType.REPORTRD_TO_ALL;
	}
	
	@Override
	public void printWrite(MessageBitSection packet, int channel) {
		System.out.println("In time I send ... ...");
	}
	
	@Override
	public void printRead(int channel) {
		System.out.println("In time I recevied ... ...");
	}
	
	@Override
	public void resetCounter() {
		counter = 0;
	}
}
