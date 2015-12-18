package csu.common.test;

import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import rescuecore2.misc.Pair;

public class TimeComparator {

	private Comparator<Pair<Integer, Integer>> timeComparator = new Comparator<Pair<Integer, Integer>>() {

		@Override
		public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
			if (o1.second() > o2.second()) 
				return -1;
			if (o1.second() < o2.second())
				return 1;
			
			if (o1.second() == o2.second()) {
				if (o1.first() > o2.first())
					return -1;
				if (o1.first() < o2.first())
					return 1;
			}
			return 0;
		}
	};
	
	private Set<Pair<Integer, Integer>> timeSet = new TreeSet<>(timeComparator);
	
	private Random keyRandom = new Random();
	private Random valueRandom = new Random();
	
	private void addElement() {
		String initValue = null;
		for (int i = 0; i < 10; i++) {
			int key = keyRandom.nextInt(50), value = valueRandom.nextInt(100);
			Pair<Integer, Integer> pair = new Pair<>(key, value);
			
			if (initValue == null) {
				initValue = "(" + key + ", " + value + ")";
			} else {
				initValue = initValue + ", " + "(" + key + ", " + value + ")";
			}
			timeSet.add(pair);
		}
		
		System.out.println("initValue = " + initValue);
	}
	
	public void printResult() {
		addElement();
		String finalValue = null;
		for (Pair<Integer, Integer> next : timeSet) {
			if (finalValue == null) {
				finalValue = "(" + next.first() + ", " + next.second() + ")";
			} else {
				finalValue = finalValue + ", " + "(" + next.first() + ", " + next.second() + ")";
			}
		}
		System.out.println();
		System.out.println("finalValue = " + finalValue);
	}
	
	public static void main(String[] args) {
		(new TimeComparator()).printResult();
	}
}
