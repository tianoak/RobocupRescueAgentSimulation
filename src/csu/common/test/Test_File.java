package csu.common.test;

import java.io.File;

public class Test_File {
	public static void main(String[] args) {
		File testFile = new File("precompute/test.txt");
		boolean flag = false;
		if (!testFile.exists()) {
			try {
				flag = testFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String output = flag ? "Success" : "Fail";
		System.out.println(output);
	}
}
