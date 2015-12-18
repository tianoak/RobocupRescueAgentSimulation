package csu.common.test;

import java.util.ArrayList;
import java.util.List;

public class Test5 {

	public static void main(String[] args) {
		Node node = new Node();
		List<Node> list1 = new ArrayList<>();
		List<Node> list2 = new ArrayList<>();
		list1.add(node);
		list2.add(node);
		list2.get(0).setA(20);
		System.out.println("List1: " + list1.get(0).getA());
		System.out.println("List2: " + list2.get(0).getA());
	}
	
	static class Node {
		int a;
		Node() {
			a = 10;
		}
		
		void setA(int a) {
			this.a = a;
		}
		
		int getA() {
			return a;
		}
	}
}
