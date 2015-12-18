package csu.common.test;

import java.util.Comparator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

public class PairComparator {
	
	public static void main(String[] args) {
		PairComparator comparator = new PairComparator();
		comparator.printSortedBuilding(comparator.sort());
		System.out.println();
		comparator.printSortedBuilding(comparator.new_Sort(), 1);
	}
	
	private Random random = new Random();
	
	public void printSortedBuilding(SortedSet<Pair<EntityID, Integer>> sortedBuildings) {
		String str = null;
		for(Pair<EntityID, Integer> next : sortedBuildings) {
			if (str == null) {
				str = "sortedBuilding: [";
			} else {
				str = str + ", ";
			}
			str = str + "(" + next.first().getValue() + ", " + next.second() + ")";
		}
		str = str + "]";
		System.out.println(str);
	}
	
	public SortedSet<Pair<EntityID, Integer>> sort() {
		SortedSet<Pair<EntityID, Integer>> sortedBuildings = new TreeSet<>(pairComparator);
		String str = null;
		for (int i = 0; i < 10; i++) {
			int second = random.nextInt(100);
			for (int j = 0; j < i + 1; j++) {
				int first = random.nextInt(100);
				if (str == null) {
					str = "unSortedBuilding: [";
				} else {
					str = str + ", ";
				}
				str = str + "(" + (i + j) + ", " + i + ")";
				sortedBuildings.add(new Pair<EntityID, Integer>(new EntityID(first), second));
			}
		}
		str = str + "]";
		System.out.println(str);
		
		return sortedBuildings;
	}
	
	public SortedSet<Pair<Pair<EntityID, Double>, Double>> new_Sort() {
		SortedSet<Pair<Pair<EntityID, Double>, Double>> result = 
				new TreeSet<Pair<Pair<EntityID, Double>, Double>>(pairComparator_new);
		
		int[] id = {951, 959, 950, 953, 960, 248, 952};
		
		double[] finalValue = {1.0, 1.0, 1.0, 7.0, 6.0, 6.0, 2.0};
		
		double[] distance = {89643.5350150807, 76282.22840216455, 88841.24813396083, 
				64536.81436978432, 57692.08764640087, 74643.98415143715, 90835.89252052296};
		
		for (int i = 0; i < id.length; i++) {
			Pair<EntityID, Double> pair = new Pair<>(new EntityID(id[i]), distance[i]);
			result.add(new Pair<Pair<EntityID, Double>, Double>(pair, finalValue[i]));
		}
		
		return result;
	}
	
	public void printSortedBuilding(SortedSet<Pair<Pair<EntityID, Double>, Double>> sortedBuildings, int a) {
		System.out.println("size = " + sortedBuildings.size());
		String str = null;
		for(Pair<Pair<EntityID, Double>, Double> next : sortedBuildings) {
			if (str == null) {
				str = "sortedBuilding: [";
			} else {
				str = str + ", ";
			}
			str = str + "(" + next.first().first().getValue() + ", " + next.second() + ")";
		}
		str = str + "]";
		System.out.println(str);
	}

	public Comparator<Pair<EntityID, Integer>> pairComparator = new Comparator<Pair<EntityID, Integer>>() {
		/* sorted element in increase order*/
		@Override
		public int compare(Pair<EntityID, Integer> o1, Pair<EntityID, Integer> o2) {
			double value1 = o1.second();
			double value2 = o2.second();
			if (value1 > value2)
				return 1;
			if (value1 < value2)
				return -1;
			
			if (value1 == value2) {
				if (o1.first().getValue() > o2.first().getValue())
					return 1;
				if (o1.first().getValue() < o2.first().getValue())
					return -1;
			}
			
			return 0;
		}
	};  
	
	public static Comparator<Pair<Pair<EntityID, Double>, Double>> pairComparator_new = 
			new Comparator<Pair<Pair<EntityID, Double>, Double>>() {
		
		@Override
		public int compare(Pair<Pair<EntityID, Double>, Double> o1, Pair<Pair<EntityID, Double>, Double> o2) {
			if (o1.second().doubleValue() > o2.second().doubleValue())
				return 1;
			if (o1.second().doubleValue() < o2.second().doubleValue())
				return -1;
			
			if (o1.second().doubleValue() == o2.second().doubleValue()) {
				if (o1.first().second().doubleValue() > o2.first().second().doubleValue()) {
					return 1;
				}
				if (o1.first().second().doubleValue() < o2.first().second().doubleValue()) {
					return -1;
				}
			}
			
			return 0;
		}
	};
}
