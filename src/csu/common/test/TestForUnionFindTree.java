package csu.common.test;

import csu.util.UnionFindTree;

public class TestForUnionFindTree {

	UnionFindTree<Integer> UTF = new UnionFindTree<>();
	
	public void add(int elem) {
		UTF.add(new Integer(elem));
	}
	
	public boolean same(int elem_1, int elem_2) {
		return UTF.same(new Integer(elem_1), new Integer(elem_2));
	}
	
	public void unite(int elem_1, int elem_2) {
		UTF.unite(new Integer(elem_1), new Integer(elem_2));
	}
	
	public static void main(String[] args) {
		TestForUnionFindTree test = new TestForUnionFindTree();
		
		for (int i = 0; i < 10; i++) {
			test.add(i);
		}
		
		test.unite(5, 6);
		test.unite(8, 9);
		test.unite(6, 8);
		
		boolean flag_1 = test.same(5, 5);
		String str_1 = flag_1 ? "conncted" : "unconnected";
		System.out.println("5 and 5 is: " + str_1);
		
		boolean flag_2 = test.same(5, 9);
		String str_2 = flag_2 ? "conncted" : "unconnected";
		System.out.println("5 and 9 is: " + str_2);
	}
}
