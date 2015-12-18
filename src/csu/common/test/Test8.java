package csu.common.test;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import csu.util.BitUtil;

public class Test8 {
	public static void main(String[] args) {
		Byte data = new Byte("100");
		System.out.println("data: " + data.toString());
		Byte p = 1 << 3;
		System.out.println("p: " + p.toString());
		Byte result = (byte)(data ^ p);
		System.out.println("result: " + result.toString());
		
		JFrame frame = new JFrame();
		frame.setSize(100, 100);
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		System.out.println("Double.MIN_VALUE: " + Double.MIN_VALUE);
		System.out.println("Double.MAX_VALUE: " + Double.MAX_VALUE);
		
		System.out.println(Math.hypot(3, 4));
		
		System.out.println("Need Bit Size: " + BitUtil.needBitSize(32));
	}
}
