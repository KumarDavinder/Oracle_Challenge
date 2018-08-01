package aconex.vehiclesurvey.loader;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class DataLoaderTest {
	private DataLoader dataLoader;
	private Map<Integer, LinkedList<Integer>> [] northSouth_maps; 

	@Before
	public void setUp() throws Exception {
		this.dataLoader = new DataLoader();
		this.northSouth_maps =  dataLoader.loadFileInput("inputFiles/InputForTesting.txt");
	}

	@Test
	public void testCheckSizeOfArrayReturned() {
		assertTrue(this.northSouth_maps.length == 2);
	}
	
	@Test
	public void testCheckSizeMapNorthboud() {
		assertTrue(this.northSouth_maps[0].size() == 2);
	}
	
	@Test
	public void testCheckSizeMapSouthboud() {
		assertTrue(this.northSouth_maps[1].size() == 2);
	}
	
	@Test
	public void testCheckNumberOfTimesFirstDay_northboud() {
		assertTrue(this.northSouth_maps[0].get(1).size() == 4);
	}
	
	@Test
	public void testCheckNumberOfTimesFirstDay_southbound() {
		assertTrue(this.northSouth_maps[1].get(1).size() == 8);
	}
	
	@Test
	public void testCheckNumberOfTimesSecondDay_northboud() {
		assertTrue(this.northSouth_maps[0].get(2).size() == 2);
	}
	
	@Test
	public void testCheckNumberOfTimesSecondDay_southbound() {
		assertTrue(this.northSouth_maps[1].get(2).size() == 4);
	}
	
	@Test
	public void testCheckFirstTimesOfEachDay_northbound() {
		assertTrue(this.northSouth_maps[0].get(1).getFirst() == 98186);
		assertTrue(this.northSouth_maps[0].get(2).getFirst() == 457868);
	}
	
	@Test
	public void testCheckLastTimesOfEachDay_northbound() {
		assertTrue(this.northSouth_maps[0].get(1).getLast() == 499886);
		assertTrue(this.northSouth_maps[0].get(2).getLast() == 457996);
	}
	
	@Test
	public void testCheckFirstTimesOfEachDay_southbound() {
		assertTrue(this.northSouth_maps[1].get(1).getFirst() == 638379);
		assertTrue(this.northSouth_maps[1].get(2).getFirst() == 156007);
	}
	
	@Test
	public void testCheckLastTimesOfEachDay_southbound() {
		assertTrue(this.northSouth_maps[1].get(1).getLast() == 86351672);
		assertTrue(this.northSouth_maps[1].get(2).getLast() == 156224);
	}
	
	@Test
	public void testCheckTotatNumberOfTimesInFirstDay() {
		assertTrue(this.northSouth_maps[0].get(1).size() + this.northSouth_maps[1].get(1).size() == 12);
	}
	
	@Test
	public void testCheckTotatNumberOfTimesInSecondDay() {
		assertTrue(this.northSouth_maps[0].get(2).size() + this.northSouth_maps[1].get(2).size() == 6);
	}
}
