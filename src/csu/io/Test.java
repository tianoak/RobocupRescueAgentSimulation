package csu.io;

import java.util.Random;

public class Test {
	public static void main(String[] args) {
		BitArrayOutputStream outPutStream = new BitArrayOutputStream();
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			int nextInt = random.nextInt(100);
			System.out.println(nextInt);
			outPutStream.writeBit(nextInt, 8);
		}
		System.out.println("----------------------------------");
		byte[] data = outPutStream.getData();
		
		BitArrayInputStream inputStream = new BitArrayInputStream(data);
		while (inputStream.hasNext()) {
			System.out.print(inputStream.readBit(8) + " ");
			
		}
	}
}
