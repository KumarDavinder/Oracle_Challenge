package aconex.vehiclesurvey.loader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import aconex.vehiclesurvey.model.Operation;

public class DataLoader {

	public Map<Integer, LinkedList<Integer>> [] loadFileInput(String inputFilePath){
		Map<Integer, LinkedList<Integer>> dayTimesNorthbound = new TreeMap<Integer, LinkedList<Integer>>();
		Map<Integer, LinkedList<Integer>> dayTimesSouthbound = new TreeMap<Integer, LinkedList<Integer>>();
		Map<Integer, LinkedList<Integer>> [] northSouth_maps = new TreeMap [2];
		northSouth_maps[0] = dayTimesNorthbound;
		northSouth_maps[1] = dayTimesSouthbound;
		Path filePath = Paths.get(inputFilePath);
		Scanner scanner;
		try {
			int countDays = 1; 
			String line2;
			int time1;
			int time2;
			int time3;
			int time4;
			int referenceForChangeDay = 0;
			scanner = new Scanner(filePath);
			while (scanner.hasNext()) {
				time1 = Integer.parseInt(scanner.next().substring(1)); //first value is always from sensor A
				line2 = scanner.next();
				time2 = Integer.parseInt(line2.substring(1));
				if (line2.startsWith("A")){		
					countDays = updateNumberDay(time2, referenceForChangeDay, countDays);
					addElementToTheMap(time1, time2, countDays, dayTimesNorthbound);
				}
				else {		
					time3 = Integer.parseInt(scanner.next().substring(1));
					time4 = Integer.parseInt(scanner.next().substring(1));
					countDays = updateNumberDay(time4, referenceForChangeDay, countDays);
					addElementToTheMap(time1, time2, countDays, dayTimesSouthbound);
					addElementToTheMap(time3, time4, countDays, dayTimesSouthbound);

				}
				referenceForChangeDay = time1;		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return northSouth_maps;
	}

	private int updateNumberDay(int time, int referenceForChangeDay, int countDays) {
		if (time - referenceForChangeDay < 0){
			countDays++;
		}
		return countDays;
	}

	private void addElementToTheMap(int timeX, int timeY, int countDays, Map<Integer, LinkedList<Integer>> dayTimes) {
		if (!dayTimes.containsKey(countDays))
			dayTimes.put(countDays, new LinkedList<Integer>());
		dayTimes.get(countDays).add(timeX);
		dayTimes.get(countDays).add(timeY);
	}

	public static void main(String[] args) {
		DataLoader dataLoader = new DataLoader();
		Map<Integer, LinkedList<Integer>> [] northSouth_maps = dataLoader.loadFileInput("InputFiles/Vehicle Survey Coding Challenge sample data.txt");
		Operation operations = new Operation();
		
		System.out.println("********************************************NUMBER OF VEHICLES********************************************");
		int vehiclesNorth_perRange = operations.getAvgVehiclesPerTimeRange(northSouth_maps[0], "18:03 19:45", "Northbound");
		System.out.println("Northbound: The number of vehicles per time range is: "+vehiclesNorth_perRange);
		int vehiclesNorth_perMnts = operations.getAvgVehiclesPerMinutes(northSouth_maps[0], 29, "Northbound");
		System.out.println("Northbound: The number of vehicles per minutes is: "+vehiclesNorth_perMnts);
		int vehiclesSouth_perRange = operations.getAvgVehiclesPerTimeRange(northSouth_maps[1], "18:03 19:45", "Southbound");
		System.out.println("Southbound: The number of vehicles per time range is: "+vehiclesSouth_perRange);
		int vehiclesSouth_perMnts = operations.getAvgVehiclesPerMinutes(northSouth_maps[1], 29, "Southbound");
		System.out.println("Southbound: The number of vehicles per minutes is: "+vehiclesSouth_perMnts);
	
		System.out.println("********************************************PEAK VOLUME TIME********************************************");
		System.out.println("If is reported a time X its mean that from (X - minutes) to X have passed the number of vehicles indicated");
		System.out.println("Northbound: Peak volume times per minutes (first three values)");
		Map<Integer, List<String>> vehiclesTimesNorthbound = operations.getPeakVolumeTimes(northSouth_maps[0], 145, "Northbound");
		System.out.println("NumberVehicles   Times");
		int count = 0;
		for (Integer key : vehiclesTimesNorthbound.keySet()) {
			if (count < 3){
				System.out.println(key+" 		"+vehiclesTimesNorthbound.get(key));
				count++;
			}
			else break;
		}
		System.out.println("Southbound: Peak volume times per minutes (first three values)");
		Map<Integer, List<String>> vehiclesTimesSouthbound = operations.getPeakVolumeTimes(northSouth_maps[1], 145, "Southbound");
		System.out.println("NumberVehicles   Times");
		count = 0;
		for (Integer key : vehiclesTimesSouthbound.keySet()) {
			if (count < 3){
				System.out.println(key+" 		"+vehiclesTimesSouthbound.get(key));
				count++;
			}
			else break;
		}
		
		System.out.println("********************************************SPEED DISTRIUTION OF TRAFFIC********************************************");
		double speedDistibution_N_range = operations.getSpeedDistributionOfTraffic(northSouth_maps[0], "Northbound" , "18:03 19:45");
		double speedDistibution_N_day = operations.getSpeedDistributionOfTraffic(northSouth_maps[0], "Northbound" , "24H");
		double speedDistibution_S_range = operations.getSpeedDistributionOfTraffic(northSouth_maps[1], "Southbound" , "18:03 19:45");
		double speedDistibution_S_day = operations.getSpeedDistributionOfTraffic(northSouth_maps[1], "Southbound" , "24H");
		System.out.println("Speed Distribution of traffic Northbound");
		System.out.println("Per time range	In all the day");
		System.out.println(speedDistibution_N_range+" 		"+speedDistibution_N_day);
		System.out.println("Speed Distribution of traffic Southbound");
		System.out.println("Per time range	In all the day");
		System.out.println(speedDistibution_S_range+"		"+speedDistibution_S_day);
		
		System.out.println("********************************************DISTANCE BETWEEN VEHICLES********************************************");
		double distBetweenVehicles_N_range = operations.getDistanceBetweenVehicles(northSouth_maps[0], "Northbound", "18:03 19:45");
		double distBetweenVehicles_N_day = operations.getDistanceBetweenVehicles(northSouth_maps[0], "Northbound", "24H");
		double distBetweenVehicles_S_range = operations.getDistanceBetweenVehicles(northSouth_maps[1], "Southbound", "18:03 19:45");
		double distBetweenVehicles_S_day = operations.getDistanceBetweenVehicles(northSouth_maps[1], "Southbound", "24H");
		System.out.println("Distance between vehicles Northbound");
		System.out.println("Per time range	In all the day");
		System.out.println(distBetweenVehicles_N_range+" 		"+distBetweenVehicles_N_day);
		System.out.println("Distance between vehicles Southbound");
		System.out.println("Per time range	In all the day");
		System.out.println(distBetweenVehicles_S_range+" 		"+distBetweenVehicles_S_day);
	}
}
