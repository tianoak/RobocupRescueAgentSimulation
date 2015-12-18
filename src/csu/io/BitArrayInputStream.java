package csu.io;

public class BitArrayInputStream {

	private long buf;
	private int offset;
	private byte[] data;
	private int length;
	private int index;

	public BitArrayInputStream(byte data[]) {
		reset(data);
	}

	public void reset(byte data[]) {
		this.buf = 0;
		this.offset = 0;
		this.data = data;
		this.length = data.length;
		this.index = 0;
	}

	/**
	 * Read given number of bits for the stream.
	 * 
	 * @param n
	 *            the number of bits to read
	 * @return the integer represent by the read bits
	 */
	public int readBit(int n) {
		if (n == 0) {
			return -1;
		}
		while (offset <= n) {
			buf <<= 8;
			buf += read() & 0xFF;
			offset += 8;
		}
		offset -= n;
		int val = (int) (buf >>> offset);
		buf &= 0xFFFFFFFFFFFFFFFFL >>> 64 - offset;
		return val;
	}

	/**
	 * Read a bit from the stream.
	 * 
	 * @return the read bit
	 */
	private int read() {
		if (index >= length) {
			return 0;
		}
		return data[index++];
	}

	public boolean hasNext() {
		return index < length || buf != 0;
	}
}