package csu.io;

import java.util.ArrayList;

public class BitArrayOutputStream {

	private long buf;
	private int offset;
	private final ArrayList<Byte> data;
	private int bitlength = 0;

	public BitArrayOutputStream() {
		this.buf = 0;
		this.offset = 0;
		this.data = new ArrayList<Byte>();
	}

	public byte[] getData() {
		flush();
		byte[] res = new byte[data.size()];
		int i = 0;
		for (byte b : data) {
			res[i++] = b;
		}
		return res;
	}

	public int getDatasize() {
		return data.size();
	}

	public int getBitLength() {
		return bitlength;
	}

	public void clear() {
		buf = 0;
		offset = 0;
		data.clear();
	}

	public void writeBit(int val, int n) {
		bitlength += n;
		buf <<= n;
		buf += val & (0xFFFFFFFF >>> 32 - n);
		offset += n;
		while (offset >= 8) {
			offset -= 8;
			write((buf >>> offset) & 0xFF);
		}
	}

	private void write(long b) {
		data.add((byte) b);
	}

	private void flush() {
		if (offset == 0) {
			return;
		}
		write((buf << 8 - offset) & 0xFF);
		offset = 0;
	}

}
