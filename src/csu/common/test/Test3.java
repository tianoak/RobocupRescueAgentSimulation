package csu.common.test;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

public class Test3 {

	public static void main(String[] args) {
		Random random = new Random();
		Comparator<Integer> com = new Comparator<Integer> () {
			@Override
			public int compare(Integer i1, Integer i2) {
				return i1-i2;
//				if (i1 > i2)
//					return 1;
//				if (i1 < i2)
//					return -1;
//				return 0;
			}
			
		};
		SortedSet<Integer> result = new TreeSet<>(com);
		PriorityQueue<Integer> queue = new PriorityQueue<Integer> (com);
		for (int i = 0; i < 10; i++) {
			Integer integer = new Integer(random.nextInt(100));
			queue.add(integer);
			result.add(integer);
		}
		System.out.println(result);
		System.out.println(queue);
	}
}
