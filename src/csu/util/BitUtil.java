package csu.util;

/**
 *  得到整数n所需要的最小比特数
 * @author nale
 *
 */
public class BitUtil {

	/**
	 *  得到整数n所需要的最小比特数
	 * @param n 等待确定的整数
	 * @return 最小的比特数
	 */
	public static int needBitSize(int n) {//需要位的大小
		int size = 0;
		do {
			size++;
			n >>>= 1;
		} while (n != 0);
		return size;
	}

}
