package csu.model.object.csuZoneEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class test {
	
	public test() {
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void readFile() throws IOException {
		File file = new File("/home/caotao/Desktop/connected.con");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String str = reader.readLine();
		List<Integer> connectedIds = new ArrayList<>();
		while (str != null) {
			if (str.startsWith("id 56434: ")) {
				String[] split = str.split(" ");
				for (int i = 0; i < split.length; i++) {
					if (i <= 1)
						continue;
					connectedIds.add(new Integer(Integer.parseInt(split[i])));
				}
				break;
			}
			
			str = reader.readLine();
		}
		reader.close();
		
		for (Integer next : connectedIds) {
			System.out.println(next.toString());
		}
	}
	
	public static void main(String[] args) {
		new test();
	}
}
