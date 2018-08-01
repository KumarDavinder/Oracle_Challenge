package aconex.vehiclesurvey.model;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class OperationTest {
	private Operation operation;
	private Map<Integer, LinkedList<Integer>> dayTimes;

	@Before
	public void setUp() throws Exception {
		operation = new Operation();
		this.dayTimes =  new TreeMap<Integer, LinkedList<Integer>>();
	}

	/* *********************************************************************************
	 * A day is divided in 2 part from 00:00 am to 12:00 pm and from 12:00 pm to 00:00 pm.
	 * 8 times are inserted each 30 minutes (times are inserted in the second half of 30 minutes).
	 * 
	 * FIRST DAY
	 * The time difference between the axles is of 225 in the first part of the day and 180 in the second part
	 * The time difference between the vehicles is 20000
	 * SECOND DAY
	 * The time difference between the axles is of 180 in the first part of the day and 150 in the second part
	 * The time difference between the vehicles is 30000
	 * THIRD DAY
	 * The time difference between the axles is of 150 in the first part of the day and 128 in the second part
	 * The time difference between the vehicles is 40000
	 * 
	 * The values are in milliseconds and the time difference between the axles is referred to the sensor A.
	 * The parameter "labelNorthSouth" has value 2 for northbound and 4 for southbound (it is necessary to create the appropriate maps).
	 * To test the first two points (the number of vehicles and peak volume time) the value of "labelNorthSouth" is insignificant
	 * ********************************************************************************* */

	private LinkedList<Integer> createMisures(int timeDiffAxles_AM, int timeDiffAxles_PM, int timeBetweenVehicles, int labelNorthSouth) {
		int halfHourInMilliseconds = 1800000;
		LinkedList<Integer> times = new LinkedList<Integer>();
		int timeDiffAxles = timeDiffAxles_AM;
		int time = 0;
		for (int i = 1; i <= 48; i++){	//24H are 48*30 mnts
			if (i > 24)
				timeDiffAxles = timeDiffAxles_PM;
			if (labelNorthSouth == 4)
				time = (halfHourInMilliseconds * i) - (timeDiffAxles * 2) - timeBetweenVehicles - 3;
			else
				time = (halfHourInMilliseconds * i) - (timeDiffAxles * 4) - (timeBetweenVehicles * 3);
			for (int j = 0; j < 8; j++){
				times.add(time);
				if (labelNorthSouth == 4){
					times.add(time + 3);
					time += timeDiffAxles;
					times.add(time);
					times.add(time + 3);
					j = j + 3;
				}
				else{
					time += timeDiffAxles;
					times.add(time);
					j++;
				}
				time += timeBetweenVehicles;
			}
		}
		return times;	
	}
	
	/*********************************************************************************************************/
	/**************************************VEHICLE SURVEY CODING TEST*****************************************/
	/*********************************************************************************************************/
	
	/*====================================AVERAGE VEHICLES PER RANGE TIME====================================*/
	
	@Test
	public void testGetAvgVehiclesPerTimeRange_northbound() {
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "8:00 9:00", "Northbound") == 8);
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "22:05 00:05", "Northbound") == 16);
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "00:00 03:00", "Northbound") == 24);
	}

	@Test
	public void testGetAvgVehiclesPerTimeRange_southbound() {
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "08:00 09:00", "Southbound") == 4);
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "22:05 00:05", "Southbound") == 8);
		assertTrue(operation.getAvgVehiclesPerTimeRange(this.dayTimes, "00:00 03:00", "Southbound") == 12);
	}

	/*=====================================AVERAGE VEHICLES PER MINUTES=======================================*/

	@Test
	public void testGetAvgVehiclesPerMinutes_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 15, "Northbound") == 2);
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 30, "Northbound") == 4);
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 60, "Northbound") == 8);
	}

	@Test
	public void testGetAvgVehiclesPerMinutes_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 15, "Southbound") == 1);
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 30, "Southbound") == 2);
		assertTrue(operation.getAvgVehiclesPerMinutes(this.dayTimes, 60, "Southbound") == 4);
	}

	/*==========================================PEAK VOLUME TIMES=============================================*/

	/********NORTHBOUND********/

	@Test
	public void testGetPeakVolumeTimes_checkSizeMap_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Northbound").size() == 1);
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").size() == 2);
	}

	@Test
	public void testGetPeakVolumeTimes_checkKeyMap_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Northbound").containsKey(4));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").containsKey(0));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").keySet().iterator().next() == 4);
	}

	@Test
	public void testGetPeakVolumeTimes_checkValueMap_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertEquals("00:30", operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").get(4).get(0));
		assertEquals("00:15", operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").get(0).get(0));
	}

	@Test
	public void testGetPeakVolumeTimes_checkSizeValueMap_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Northbound").get(4).size() == 48);
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Northbound").get(4).size() == 48);
	}
	 
	/********SOUTHBOUND********/
	
	@Test
	public void testGetPeakVolumeTimes_checkSizeMap_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Southbound").size() == 1);
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").size() == 2);
	}

	@Test
	public void testGetPeakVolumeTimes_checkKeyMap_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Southbound").containsKey(2));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").containsKey(0));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").keySet().iterator().next() == 2);
	}

	@Test
	public void testGetPeakVolumeTimes_checkValueMap_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertEquals("00:30", operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").get(2).get(0));
		assertEquals("00:15", operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").get(0).get(0));
	}

	@Test
	public void testGetPeakVolumeTimes_checkSizeValueMap_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 30, "Southbound").get(2).size() == 48);
		assertTrue(operation.getPeakVolumeTimes(this.dayTimes, 15, "Southbound").get(2).size() == 48);
	}

	/*=======================================SPEED DISTRIBUTION OF TRAFFIC============================================*/

	/********NORTHBOUND********/
	
	@Test
	public void testGetSpeedDistributionOfTraffic_firstDay_AM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "00:00 12:00") == 40.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_secondDay_AM_northbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "00:00 12:00") == 50.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_thirdDay_AM_northbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "00:00 12:00") == 60.0);	
	}
	
	//intermediate results are not approximated
	@Test
	public void testGetSpeedDistributionOfTraffic_AM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "00:00 12:00") == 50.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_firstDay_PM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "12:00 00:00") == 50.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_secondDay_PM_northbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "12:00 00:00") == 60.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_thirdDay_PM_northbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "12:00 00:00") == 70.3);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_PM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "12:00 00:00") == 60.1);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Northbound", "24H") == 54.6);
	}
	
	/********SOUTHBOUND********/
	/* The speed values will be same of northbound but the calculation way is a bit different 
	 * (Northbound A1A2 --> A2-A1; Southbound A1B1A2B2 --> A2-A1)
	 * The values will be same because the time difference between the axles is the same */
	
	@Test
	public void testGetSpeedDistributionOfTraffic_AM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Southbound", "00:00 12:00") == 50.0);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_PM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Southbound", "12:00 00:00") == 60.1);	
	}
	
	@Test
	public void testGetSpeedDistributionOfTraffic_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getSpeedDistributionOfTraffic(this.dayTimes, "Southbound", "24H") == 54.6);
	}
	
	/*===============================================DISTANCE BETWEEN VEHICLES====================================================*/
	
	/* S = V * t  
	 * V is calculated like above
	 * t is the average time between the axles of the vehicle on the sensor A
	 * 
	 * Northbound
	 * t = ((24 * timeBetweenVehicles * 3) + (23 * Y)) / ((24 * 3) + 23) 
	 * Y = halfHourInMilliseconds - (timeDiffAxles * 4) - (timeBetweenVehicles * 3);
	 * 
	 * Southboud
	 * t = ((24 * timeBetweenVehicles) + (23 * Z)) / (24 + 23) 
	 * Z = halfHourInMilliseconds - (timeDiffAxles * 2) - timeBetweenVehicles
	 * 
	 * halfHourInMilliseconds, timeDiffAxles and timeBetweenVehicles are parameters present in the method "createMisures".
	 * 
	 * Explanation for northbound (same is for southbound):
	 * In twelve hours (00:00am to 12:00pm or from 12:00 to 00:00) there are 24 * 30 minutes, each 30 minutes there are 
	 * 8 times (4 vehicles for northbound --> 3 time intervals).
	 * The sum of the difference time among vehicles is: timeBetweenVehicles * 3 * 24
	 * There is a time interval between a set of vehicles and the following ones in which no vehicle passes (time difference 
	 * between the fourth vehicle of the first set and the first vehicle of the second set)...These time intervals are in total 
	 * 23 and have a value Y. */
	
	/********NORTHBOUND********/	
	
	@Test
	public void testGetDistanceBetweenVehicles_oneDay_AM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "00:00 12:00") == 4846.7);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_secondDay_AM_northbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "00:00 12:00") == 6063.4);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_AM_northbound(){	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "00:00 12:00") == 7281.8);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_AM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "00:00 12:00") == 6064.0);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_oneDay_PM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "12:00 00:00") == 6059.0);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_secondDay_PM_northbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "12:00 00:00") == 7276.5);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_PM_northbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "12:00 00:00") == 8533.8);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_PM_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "12:00 00:00") == 7289.8);
	}
	
	//t = ((24 * timeBetweenVehicles * 3 * 2) + (23 * Y_firstPartDay)) + (24 * Y_secondPartDay))/ (23 + 23 + 1 + 24*2*3)
	@Test
	public void testGetDistanceBetweenVehicles_firstDay_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "24H") == 5469.7);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_secondDay_northbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "24H") == 6715.8);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_northbound(){	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "24H") == 7975.2);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_northbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 2));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 2));	
		this.dayTimes.put(3, createMisures(150, 128, 40000, 2));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Northbound", "24H") == 6720.2);
	}

	/********SOUTHBOUND********/
	
	@Test
	public void testGetDistanceBetweenVehicles_oneDay_AM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "00:00 12:00") == 9789.5);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_secondDay_AM_southbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "00:00 12:00") == 12240.5);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_AM_southbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "00:00 12:00") == 14692.6);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_AM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "00:00 12:00") == 12240.9);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_oneDay_PM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "12:00 00:00") == 12237.5);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_secondDay_PM_southbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "12:00 00:00") == 14689.0);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_PM_southbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "12:00 00:00") == 17218.3);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_PM_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "12:00 00:00") == 14714.9);
	}
	
	//t = ((24 * timeBetweenVehicles * 2) + (23 * Y_firstPartDay)) + (24 * Y_secondPartDay))/ (23 + 23 + 1 + 24*2)
	@Test
	public void testGetDistanceBetweenVehicles_firstDay_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));	
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "24H") == 10994.3);
	}

	@Test
	public void testGetDistanceBetweenVehicles_secondDay_southbound(){
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "24H") == 13495.1);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_thirdDay_southbound(){
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "24H") == 16021.8);
	}
	
	@Test
	public void testGetDistanceBetweenVehicles_southbound(){
		this.dayTimes.put(1, createMisures(225, 180, 20000, 4));	
		this.dayTimes.put(2, createMisures(180, 150, 30000, 4));
		this.dayTimes.put(3, createMisures(150, 128, 40000, 4));
		assertTrue(operation.getDistanceBetweenVehicles(this.dayTimes, "Southbound", "24H") == 13503.7);
	}
}
