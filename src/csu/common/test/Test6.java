package csu.common.test;

import java.util.PriorityQueue;

public class Test6 implements Comparable<Test6>{

	private int a;
	public Test6(int a) {
		this.a = a;
	}
	
	public int getA() {
		return a;
	}
	
	@Override
	public int compareTo(Test6 arg0) {
		return this.a - arg0.a;
	}

	public static void main(String[] args) {
		PriorityQueue<Test6> que = new PriorityQueue<>();
		for (int i = 0; i < 10; i++) {
			que.add(new Test6(i));
		}
		for(Test6 test : que) {
			System.out.println("Priority is: " + test.getA());
		}
	}
}
