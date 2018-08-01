package aconex.vehiclesurvey.model;

import java.util.*;

public class Operation {
	private static final double WHEELBASE_VEHICLE = 2.5;

	public int getAvgVehiclesPerTimeRange(Map<Integer, LinkedList<Integer>> dayTimes, String timeRange, String vehicleDirection) {
		int [] startAndEndTime = getTimeRangeInMilliseconds(timeRange);
		int startTime = startAndEndTime[0];
		int endTime = startAndEndTime[1];
		int vehiclesPerTimeRange_day;
		List <Integer> vehiclesPerTimeRange_days = new LinkedList<Integer>();
		for (Integer day : dayTimes.keySet()) {
			vehiclesPerTimeRange_day = getVehiclesPerTimeRangeInADay(dayTimes.get(day), vehicleDirection, startTime, endTime);
			vehiclesPerTimeRange_days.add(vehiclesPerTimeRange_day);
		}
		return getAvg(vehiclesPerTimeRange_days);
	}

	private int getAvg(List<Integer> list) {
		int sum = 0;
		for (Integer element : list) {
			sum += element;
		}
		return sum/list.size();
	}

	private int getVehiclesPerTimeRangeInADay(List<Integer> times, String vehicleDirection, int startTime, int endTime) {
		int numHitsWithSensor = 0;
		for (Integer time : times) {
			if (time >= startTime && time <= endTime){
				numHitsWithSensor++;
			}
			else if (numHitsWithSensor != 0)
				break;
		}
		return getNumVehicles(vehicleDirection, numHitsWithSensor);
	}

	public int getAvgVehiclesPerMinutes(Map<Integer, LinkedList<Integer>> dayTimes, int minutes, String vehicleDirection) {
		LinkedList<Integer> avgsVehicles_days = new LinkedList<Integer>();
		LinkedList<Integer> vehiclesPerMnts_day;
		int avgVehicles_day;
		for (Integer day : dayTimes.keySet()) {
			vehiclesPerMnts_day = getNumsVehiclesPerMinutesInADay(dayTimes.get(day), minutes, vehicleDirection); 
			avgVehicles_day = getAvg(vehiclesPerMnts_day); 
			avgsVehicles_days.add(avgVehicles_day);	
		}
		return getAvg(avgsVehicles_days);		
	}

	private LinkedList<Integer> getNumsVehiclesPerMinutesInADay(LinkedList<Integer> times, int minutes, String vehicleDirection) {
		LinkedList<Integer> vehiclesPerMnts_day = new LinkedList<Integer>();
		int vehiclesPerMnts;
		int step = minutes*60000;	//from minutes to milliseconds
		int limit_time = step;
		int numHitsWithSensor = 0;
		for (Integer time : times) {
			if ((time <= limit_time)){
				numHitsWithSensor++;
			}
			else{
				vehiclesPerMnts = getNumVehicles(vehicleDirection, numHitsWithSensor);
				vehiclesPerMnts_day.add(vehiclesPerMnts);
				limit_time = limit_time + step;
				while (time > limit_time){
					vehiclesPerMnts_day.add(0);
					limit_time = limit_time + step;
				}
				numHitsWithSensor = 1;
			}
		}							
		//if "minutes"=35 mnts I will take the values up to 41*35=1435 mnts and I will not take into account the last 5 mnt
		if ((vehiclesPerMnts_day.size() + 1) * minutes <= 1440){ //1440 mnts = 24H
			vehiclesPerMnts = getNumVehicles(vehicleDirection, numHitsWithSensor);
			vehiclesPerMnts_day.add(vehiclesPerMnts); 
		}
		/*if "minutes"=1, I want a list of 1440 elements, it is possible that in a day the last vehicle pass at the minute 1435, 
		  so It is necessary to insert last 5 elements with value 0*/
		int missingValues = (86400000 - times.getLast())/(minutes * 60000);	//86400000 ms = 24H, 60000 ms = 1 mnt
		while (missingValues > 0){ 
			vehiclesPerMnts_day.add(0);
			missingValues--;
		}
		return vehiclesPerMnts_day;
	}

	private int getNumVehicles(String vehicleDirection, int numHitsWithSensor) {
		int numVehicles;
		if (vehicleDirection.equals("Northbound")){
			numVehicles = numHitsWithSensor/2;
		}
		else{
			numVehicles = numHitsWithSensor/4;
		}
		return numVehicles;
	}

	public Map<Integer, List<String>> getPeakVolumeTimes(Map<Integer, LinkedList<Integer>> dayTimes, int minutes, String vehicleDirection) {
		LinkedList<List<Integer>> vehiclesPerMnts_days = new LinkedList<List<Integer>>();
		LinkedList<Integer> vehiclesPerMnts_day;
		for (Integer day : dayTimes.keySet()) {			
			vehiclesPerMnts_day = getNumsVehiclesPerMinutesInADay(dayTimes.get(day) , minutes, vehicleDirection); 
			vehiclesPerMnts_days.add(vehiclesPerMnts_day);
		}
		int numTimeRanges = 0;
		int numDays = vehiclesPerMnts_days.size();
		if (numDays != 0){
			numTimeRanges = vehiclesPerMnts_days.get(0).size();
		}
		int sum;
		LinkedList<Integer> avgsPerTimeRanges = new LinkedList<Integer>();
		for (int i = 0; i < numTimeRanges; i++) {
			sum = 0;
			for (List<Integer> element : vehiclesPerMnts_days) {
				sum += element.get(i);
			}
			avgsPerTimeRanges.add(sum/numDays);
		}		
		return getRankingOfPeakVolumeTimes(avgsPerTimeRanges, minutes);
	}

	private Map<Integer, List<String>> getRankingOfPeakVolumeTimes(LinkedList<Integer> avgsPerTimeRanges, int minutes) {
		Map<Integer, List<String>> vehicles_times = new LinkedHashMap<Integer, List<String>>();
		LinkedList<Integer> listOrderedForNumVehicles = new LinkedList<Integer>(avgsPerTimeRanges);
		Collections.sort(listOrderedForNumVehicles, Collections.reverseOrder()); 		
		int index;
		String hourMinutes;
		for (Integer avgVehicles : listOrderedForNumVehicles) {
			index = avgsPerTimeRanges.indexOf(avgVehicles);
			hourMinutes = getTimeInHourAndMinutes((index+1)*minutes);
			if (vehicles_times.containsKey(avgVehicles)){
				vehicles_times.get(avgVehicles).add(hourMinutes);
			}
			else{
				vehicles_times.put(avgVehicles, new LinkedList<String>(Arrays.asList(hourMinutes)));
			}
			avgsPerTimeRanges.set(index, -1); 
		}
		return vehicles_times;
	}

	private String getTimeInHourAndMinutes(int minutes) {
		int hour = minutes / 60;
		minutes = minutes % 60;
		return String.format("%02d", hour)+":"+String.format("%02d", minutes);
	}
	
	//return kilometers per hour
	public double getSpeedDistributionOfTraffic(Map<Integer, LinkedList<Integer>> dayTimes, String vehicleDirection, String timeRange) {
		double sumAvgsSpeeds = 0;
		int [] startAndEndTime = getTimeRangeInMilliseconds(timeRange);
		int startTime = startAndEndTime[0];
		int endTime = startAndEndTime[1];
		int labelNorthSouth = getLabelNorthSouth(vehicleDirection);	
		for (Integer day : dayTimes.keySet()) {
			sumAvgsSpeeds += getAvgSpeedInADay(dayTimes.get(day), labelNorthSouth, startTime, endTime);
		}
		double avgSpeed = 3.6*sumAvgsSpeeds/dayTimes.size();	
		return Math.round(avgSpeed*10.0)/10.0;	
	}

	//return speed in meters per seconds
	private double getAvgSpeedInADay(LinkedList<Integer> times, int labelNorthSouth, int startTime, int endTime) {
		double avgTimeBetweenAxles = getAvgTimeBetweenAxlesInADay(times, labelNorthSouth, startTime, endTime, 1);
		return WHEELBASE_VEHICLE/avgTimeBetweenAxles;
	}

	/* return time in seconds
	 * average time can be between axles of the same vehicle (in case of speed) or between axles of different vehicles (in case of distance)
	 * "labelNorthSouth" and "k" are necessary to take in consideration this distinction. 
	 * labelNorthSouth=2 for northbound and 4 for southbound, k=1 for the speed and 2 for the distance*/
	private double getAvgTimeBetweenAxlesInADay(LinkedList<Integer> times, int labelNorthSouth, int startTime, int endTime, int k) {
		LinkedList<Integer> timesInterest = times;  
		if (startTime != -1)	//if is -1 its mean that range is not defined and it is of 24H
			timesInterest = getTimesInTheTimeRange(timesInterest, startTime, endTime, labelNorthSouth);
		double sumDifferencesTimes = 0;
		int count = 0; 
		for (int i = (labelNorthSouth/2)*k; i < timesInterest.size(); i = i + labelNorthSouth) {
			sumDifferencesTimes += timesInterest.get(i) - timesInterest.get(i - labelNorthSouth/2);
			count++;
		}
		return sumDifferencesTimes * 0.001 / count; 	
	}
	
	//return metres
	public double getDistanceBetweenVehicles(Map<Integer, LinkedList<Integer>> dayTimes, String vehicleDirection, String timeRange) {
		double sumAvgsDistances = 0;
		int [] startAndEndTime = getTimeRangeInMilliseconds(timeRange);
		int startTime = startAndEndTime[0];
		int endTime = startAndEndTime[1];
		int labelNorthSouth = getLabelNorthSouth(vehicleDirection);	
		for (Integer day : dayTimes.keySet()) {
			sumAvgsDistances += getAvgDistanceInADay(dayTimes.get(day), labelNorthSouth, startTime, endTime);
		}
		double avgDistance = sumAvgsDistances/dayTimes.size();
		return Math.round(avgDistance*10.0)/10.0;
	}

	//return distance in metres
	private double getAvgDistanceInADay(LinkedList<Integer> times, int labelNorthSouth, int startTime, int endTime) {
		double avgSpeed_day =  getAvgSpeedInADay(times, labelNorthSouth, startTime, endTime);
		double avgTimeBetweenAxles = getAvgTimeBetweenAxlesInADay(times, labelNorthSouth, startTime, endTime, 2);
		return avgSpeed_day*avgTimeBetweenAxles;
	}

	private LinkedList<Integer> getTimesInTheTimeRange(List<Integer> times, int startTime, int endTime, int labelNorthSouth) {
		LinkedList<Integer> timesInterest =  new LinkedList<Integer>();
		int time;
		for (int i = labelNorthSouth-1; i < times.size(); i = i+labelNorthSouth) {
			time = times.get(i);
			if (time > startTime && time <= endTime){	//Vehicles that report last time in the interval are considered
				if (labelNorthSouth == 4){		
					timesInterest.add(times.get(i-3));
					timesInterest.add(times.get(i-2));
				}
				timesInterest.add(times.get(i-1));
				timesInterest.add(time);
			}
			else if (timesInterest.size() != 0){
				break;
			}
		}
		return timesInterest;
	}

	private int[] getTimeRangeInMilliseconds(String timeRange) {	
		String [] times = timeRange.split(" ");
		int startTime = -1;
		int endTime = 0;
		if (times.length == 2){ 	//range defined
			int hour_ms = Integer.parseInt(times[0].split(":")[0])*3600000;
			int mnts_ms = Integer.parseInt(times[0].split(":")[1])*60000;	
			if (hour_ms == 86400000) //24H in ms
				hour_ms = 0;	 
			startTime = hour_ms + mnts_ms;  
			hour_ms = Integer.parseInt(times[1].split(":")[0])*3600000;
			mnts_ms = Integer.parseInt(times[1].split(":")[1])*60000;
			if (hour_ms == 0)
				hour_ms = 86400000;	
			endTime = hour_ms + mnts_ms;
		}
		return new int[]{startTime, endTime};
	}

	//labelNorthSouth = 2 for Northbound (2 times for 1 vehicle) otherwise labelNorthSouth = 4 (4 times for 1 vehicle)
	private int getLabelNorthSouth(String vehicleDirection) {
		int labelNorthSouth;
		if (vehicleDirection.equals("Northbound")){
			labelNorthSouth = 2;
		}
		else{
			labelNorthSouth = 4;
		}
		return labelNorthSouth;
	}	
}
