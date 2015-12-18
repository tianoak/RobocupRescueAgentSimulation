package csu.common.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Append something in the end of the given file.
 * 
 * @author appreciation-csu
 *
 */
public class FileOperation {
	private static final String FILE_NAME = "/home/rescue/Desktop/test";
	
	/**
	 * Append the content in the end of the given file.
	 * 
	 * @param fileName
	 *            the name of the given file
	 * @param content
	 *            the content to append
	 * @throws IOException
	 */
	public void write(String fileName, String content) throws IOException {
		FileWriter writer = new FileWriter(fileName, true);
		writer.write(content);
		writer.close();
	}
	
	public static void main(String[] args) {
		try {
			FileOperation operation = new FileOperation();
			String fileName = FILE_NAME;
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();

			for (int i = 0; i < 10; i++) {
				operation.write(fileName, i + " ");
			}
			for (int i = 10; i < 20; i++) {
				operation.write(fileName, i + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
