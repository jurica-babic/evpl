package org.powertac.smartparkinglot;

import static org.junit.Assert.*;

import org.junit.Test;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;

public class UtilTest {

	public static final double DELTA = 0.001;

	@Test
	public void testDefaultPriceNoFile() {
		double[][] result;
		try {
			result = Util.getHourlyValuesForAYear("/non-existent-file.txt", 30.00, DataGranularity.HOURLY);
			assertEquals(30.00, result[1][222], DELTA);
		} catch (HourlyValuesResolveException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testMarketPriceLoading() {
		double[][] result;
		try {
			result = Util.getHourlyValuesForAYear("/market-prices.txt", 30.00, DataGranularity.HOURLY);
	//		System.out.println(Arrays.deepToString(result));



			assertEquals(78.43, result[1][0], DELTA);
			assertEquals(55.98, result[1][8714], DELTA);
			assertEquals(60.78, result[1][Simulation.HOURS_PER_YEAR-1], DELTA);

			assertEquals(8760, result[1].length);

		} catch (HourlyValuesResolveException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCitySquareDeparturesLoading() {
		double[][] result;
		try {
			result = Util.getHourlyValuesForAYear("/City Square-departures.txt", 30.00, DataGranularity.HOURLY);
			//		System.out.println(Arrays.deepToString(result));

			/*
			 * 342 25.15 343 28.03 344 25.26 345 19.64 346 23.69 347 24.17 348
			 * 23.74 349 23.72 350 25.41
			 */

			assertEquals(7.3022, result[1][Simulation.HOURS_PER_YEAR - 1], DELTA);

			assertEquals(Simulation.HOURS_PER_YEAR, result[1].length);

		} catch (HourlyValuesResolveException e) {
			e.printStackTrace();
		}

	}


	@Test
	public void testCitySquareArrivalsLoading() {
		double[][] result;
		try {
			result = Util.getHourlyValuesForAYear("/City Square-arrivals.txt", 30.00, DataGranularity.HOURLY);
	//		System.out.println(Arrays.deepToString(result));

			/*
			 * 342 25.15 343 28.03 344 25.26 345 19.64 346 23.69 347 24.17 348
			 * 23.74 349 23.72 350 25.41
			 */

			assertEquals(4, result[1][Simulation.HOURS_PER_YEAR - 1], DELTA);

			assertEquals(Simulation.HOURS_PER_YEAR, result[1].length);

		} catch (HourlyValuesResolveException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFailedValueLoading() {
		double[][] result = null;
		try {
			result = Util.getHourlyValuesForAYear("/non-existent-file.txt", -1, DataGranularity.HOURLY);
		} catch (HourlyValuesResolveException e) {
			assertTrue("The exception is thrown.", result == null);
		}
	}
	
	@Test
	public void testDataWithWrongGranularity() {
		double[][] result = null;
		try {
			result = Util.getHourlyValuesForAYear("/test-not-hourly-granularity.txt", -1, DataGranularity.HOURLY);
		} catch (HourlyValuesResolveException e) {
			assertTrue("The exception is thrown. "+ e.getMessage(), result == null);
			System.err.println(e.getMessage());
		}
	}

}
