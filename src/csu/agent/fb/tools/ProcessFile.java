package csu.agent.fb.tools;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Util class.
 * 
 * @author appreciation-csu
 *
 */
public class ProcessFile implements Runnable{
	private static final String DESTINATIONS = "building output";

	private Map<String, Map<Integer, Integer>> timeEstimatedTemperature;
	private Map<String, Map<Integer, Integer>> timeEstimatedFieryness;

	private DateFormat format;
	
	private String fb;
	
	private final Object lock = new Object();
	
	@Override
	public void run() {
		format = new SimpleDateFormat("yyyy-MM-dd hh:mm");

		File destination = new File(DESTINATIONS);
		if (!destination.exists()) {
			destination.mkdir();
		}
		
		String date = format.format(new Date());
		String timeDirectory = DESTINATIONS + "/" + date;
		File timeFile = new File(timeDirectory);
		if (!timeFile.exists()) {
			timeFile.mkdir();
		}
		
		File fbFile = new File(timeDirectory + "/" + fb);
		if (!fbFile.exists()) {
			fbFile.mkdir();
		}
		
		String estimatedTemperatureFileName = timeDirectory + "/" + fb + "/estimatedTemperature.temp";
		String estimatedFierynessFileName = timeDirectory + "/" + fb + "/estimatedFieryness.fiery";
		
		synchronized (lock) {
			writeTemperatureFile(estimatedTemperatureFileName, true);
			writeFierynessFile(estimatedFierynessFileName, true);
		}
	}

	// constructor
	public ProcessFile(int id) {
		fb = id + "";
	}

	private void writeTemperatureFile(String fileName, boolean estimated) {
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(file);
			
			if (estimated) {
				for (String next : timeEstimatedTemperature.keySet()) {
					writer.print("et" + next);
					writer.print(" = [");

					for (Integer temperature : timeEstimatedTemperature.get(next).values()) {
						writer.print(temperature.toString() + ", ");
					}

					writer.print("0]");
					writer.println();
				}
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeFierynessFile(String fileName, boolean estimated) {
		File file = new File(fileName);

		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(file);
			
			if (estimated) {
				for (String next : timeEstimatedFieryness.keySet()) {
					writer.print("ef" + next);
					writer.print(" = [");

					for (Integer fieryness : timeEstimatedFieryness.get(next).values()) {
						writer.print(fieryness.toString() + ", ");
					}

					writer.print("0]");
					writer.println();
				}
			} 
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setTimeEstimatedTemperature(Map<String, Map<Integer, Integer>> timeTemperature) {
		this.timeEstimatedTemperature = timeTemperature;
	}
	
	public void setTimeEstimatedFieryness(Map<String, Map<Integer, Integer>> timeFieryness) {
		this.timeEstimatedFieryness = timeFieryness;
	}
}

