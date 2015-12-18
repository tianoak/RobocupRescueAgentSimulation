package csu.common.test;

import java.util.ArrayList;
import java.util.List;

public class Test7 {

	public static final String ARRAY_REGEX = ".";
	
	public Test7() {
		// empty constructor, do nothing
	}

	private List<String> splitArrayValue(String value) {
		List<String> result = new ArrayList<String>();
		String[] s = value.split(ARRAY_REGEX);
		for (String next : s) {
			if (!"".equals(next)) {
				result.add(next);
			}
		}
		return result;
	}
	
	public static void main(String args[]) {

		Test7 te = new Test7();
		System.out.println("-----------------------------");
		String str = "rescuecore2.standard.kernel.LineOfSightPerception";
		List<String> keyArray = te.splitArrayValue(str);
		if (keyArray.isEmpty()) 
			System.out.println("Empty");
		for (String keyArr : keyArray) {
			System.out.println(keyArr);
		}
	}

}
