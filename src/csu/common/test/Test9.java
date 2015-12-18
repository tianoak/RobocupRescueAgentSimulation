package csu.common.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import rescuecore2.standard.entities.Building;

public class Test9 {
	public static void main(String[] args) {
		String str = "surroundRoads: 12 23 34 3467 34212";
		String string = str.replaceFirst("neighbours: ", "");
		System.out.println(string);
		String[] neigbourIds = string.split(" ");
		for (String neighborId : neigbourIds) {
			if (neighborId.isEmpty())
				continue;
			System.out.println(neighborId);
		}
		
		boolean flag1 = false;
		boolean flag2 = false;
		
		if (flag1 && flag2 || true) {
			System.out.println("----------------------");
		}
		
		Double double_1 = 64536.81436978432;
		Double double_2 = 57692.08764640087;
		
		if (double_1 > double_2) {
			System.out.println("double_1 > double_2");
		}
		
		if (double_1 < double_2) {
			System.out.println("double_1 < double_2");
		}
		
		if (double_1 == double_2) {
			System.out.println("double_1 = double_2");
		}
		
		System.out.println();
		for (double i = 0; i < 2; i += .1d) {
			System.out.println(i);
		}
		
		Set<Integer> hashSet = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			hashSet.add(new Integer(i));
		}
		
		System.out.println(hashSet.contains(1) ? "true" : "false");
		System.out.println("------------------");
		System.out.println(hashSet.contains(null) ? "true" : "false");
		
		Random random = new Random();
		Map<Integer, Integer> treeMap = new TreeMap<>();
		for (int i = 0; i < 20; i++) {
			treeMap.put(new Integer(i), new Integer(random.nextInt(100)));
		}
		String str_1 = null;
		for (Iterator<Integer> itor = treeMap.keySet().iterator(); itor.hasNext(); ) {
			Integer key = itor.next();
			Integer value = treeMap.get(key);
			if (str_1 == null) {
				str_1 = "(" + key.intValue() + ", " + value.intValue() + ")";
			} else {
				str_1 = str_1 + ", (" + key.intValue() + ", " + value.intValue() + ")";
			}
		}
		System.out.println(str_1);
		
		str_1 = null;
		for (Iterator<Integer> itor = treeMap.keySet().iterator(); itor.hasNext(); ) {
			Integer key = itor.next();
			if (key.intValue() >= 10)
				itor.remove();
		}
		for (Iterator<Integer> itor = treeMap.keySet().iterator(); itor.hasNext(); ) {
			Integer key = itor.next();
			Integer value = treeMap.get(key);
			if (str_1 == null) {
				str_1 = "(" + key.intValue() + ", " + value.intValue() + ")";
			} else {
				str_1 = str_1 + ", (" + key.intValue() + ", " + value.intValue() + ")";
			}
		}
		System.out.println(str_1);
		
		Building building = null;
		if (building instanceof Building) {
			System.out.println("I'm a building");
		} else {
			System.out.println("I'm not a building");
		}
	}
}
