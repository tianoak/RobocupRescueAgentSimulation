package csu.common.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Test1 {

	private Comparator<Integer> comp = new Comparator<Integer>() {

		@Override
		public int compare(Integer arg0, Integer arg1) {
			if (arg0.intValue() < arg1.intValue())
				return -1;
			if (arg0.intValue() > arg1.intValue())
				return 1;
			return 0;
		}
	};
	
	private List<Integer> list = new ArrayList<>();
	
	private Random random = new Random();
	
	private void addElement() {
		for (int i = 0; i < 20; i++) {
			int a = this.random.nextInt(100);
			list.add(a);
			System.out.print(a + "  ");
		}
		System.out.println();
	}
	
	private void sortList() {
		Collections.sort(this.list, this.comp);
		for (Integer next : this.list) {
			System.out.print(next + "  ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		Test1 test = new Test1();
		test.addElement();
		test.sortList();
	}
}
