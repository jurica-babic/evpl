package org.powertac.smartparkinglot;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powertac.smartparkinglot.exception.NoMoreArrivalsException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.*;

import ec.util.MersenneTwisterFast;

public class TestThinningAlgorithm {

    public static final String OUTPUT_FILE = "thinning-values.txt";
    public static final String CITY_SQUARE_ARRIVALS = "/City Square-arrivals.txt";
    public static final String CITY_SQUARE_DEPARTURES = "/City Square-departures.txt";

    public static final String TAVISTOCK_ARRIVALS = "/Tavistock-arrivals.txt";
    public static final String TAVISTOCK_DEPARTURES = "/Tavistock-departures.txt";


    public static final double ALPHA = 0.05;


    private static double[] arrivals;
   // private static double[] departureDataCity;

   // private static double[] arrivalDataPrinces;
   // private static double[] departurePrinces;

    @BeforeClass
    public static void setUp() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

       arrivals = Util.getHourlyValuesForAYear(CITY_SQUARE_ARRIVALS, 5, DataGranularity.HOURLY)[1];
     //   departureDataCity = Util.getHourlyValuesForAYear(CITY_SQUARE_DEPARTURES, 5, DataGranularity.HOURLY)[1];
     //   arrivalDataPrinces = Util.getHourlyValuesForAYear(PRINCES_ARRIVALS, 5, DataGranularity.HOURLY)[1];
     //   departurePrinces = Util.getHourlyValuesForAYear(PRINCES_DEPARTURES, 5, DataGranularity.HOURLY)[1];

      //  arrivals = Util.getHourlyValuesForAYear(TAVISTOCK_ARRIVALS, 5, DataGranularity.HOURLY)[1];

        System.err.println("It takes " + stopWatch + " to read " + CITY_SQUARE_ARRIVALS + "," + CITY_SQUARE_DEPARTURES + ".");
    }


    private void runThinningAlgorithm(final double[] rates, final int ITERATIONS) {

        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        double[] counterPerTimeslotSumAllIterations = new double[Simulation.HOURS_PER_YEAR];
        ArrayList<int[]> iterationCounters = new ArrayList<>();

        double[] totalCounts = new double[ITERATIONS];

        Random random = new Random();

        double avgSimDuration = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            long startSimNano = stopwatch.getTime();
            long seed = i;
            MersenneTwisterFast mersenneTwisterFast = new MersenneTwisterFast(seed);
            ThinningAlgorithm thinningAlgorithm = new ThinningAlgorithm(rates, Simulation.HOURS_PER_YEAR, mersenneTwisterFast);

            while (thinningAlgorithm.getLastArrivalTime() < Simulation.HOURS_PER_YEAR) {
                try {
                    thinningAlgorithm.generateNextArrivalTime();
                } catch (NoMoreArrivalsException e) {
                    System.err.println("Thinning algorithm finished");
                }
            }
            long simDuration = stopwatch.getTime() - startSimNano;
            avgSimDuration += simDuration;
            iterationCounters.add(thinningAlgorithm.getCounter());
            totalCounts[i] = thinningAlgorithm.getN();
        }
        avgSimDuration /= ITERATIONS;

        System.err.println("On average, it takes " + DurationFormatUtils.formatDurationHMS((long) avgSimDuration) + " to complete the thinning algorithm.");

        double perTimeslotRejectNullHypothesis = 0;

        // aggregate arrivals:
        for (int timeslot = 0; timeslot < Simulation.HOURS_PER_YEAR; timeslot++) {

            double[] observed = new double[ITERATIONS];
            for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                int[] counter = iterationCounters.get(iteration);
                double value = 0;
                value = counter[timeslot];
                counterPerTimeslotSumAllIterations[timeslot] = counterPerTimeslotSumAllIterations[timeslot] + value;
                observed[iteration] = value;
                // test whether per time slot samples correspond to the original rate. Hopefully they are and therefore we will not be able to reject the null hypothesis (false)

            }
            if (!TestUtils.tTest(rates[timeslot], observed, ALPHA)) {
                perTimeslotRejectNullHypothesis++;
            } else {
                if (perTimeslotRejectNullHypothesis == 0) {
                    System.err.println("The following time slots rejected the null hypothesis (meaning, they are different from a known rate):");
                }
                System.err.print("(" + timeslot + ": "+ rates[timeslot] + ") ");
            }
        }
        // average:
        double originalCount = 0;
        double averageCount = 0;
        double[] perTimeslotAverages = new double[Simulation.HOURS_PER_YEAR];
        for (int timeslot = 0; timeslot < Simulation.HOURS_PER_YEAR; timeslot++) {
            averageCount += counterPerTimeslotSumAllIterations[timeslot];
            originalCount += rates[timeslot];
            perTimeslotAverages[timeslot] = counterPerTimeslotSumAllIterations[timeslot] / ITERATIONS;
        }
        System.err.println("\n");
        for(int i=0;i<Simulation.HOURS_PER_YEAR; i++){
            System.err.println(i+"\t"+ arrivals[i]+"\t"+perTimeslotAverages[i]);
        }

        averageCount /= ITERATIONS;
        // print:
        System.err.println("\nTOTAL DURATION: " + stopwatch);

        for (double count : totalCounts) {
            System.err.print(count + ",");
        }

        boolean nullHypothesis = TestUtils.tTest(originalCount, totalCounts, ALPHA);
        System.err.println("\nOriginal count: " + originalCount + " Observed average: " + averageCount + " t-test p-value: " + TestUtils.tTest(originalCount, totalCounts) + ", reject null hypothesis (H0=H1)?:" + nullHypothesis);
        System.err.println("Per time slot null hypothesis rejection rate (higher the better): " + perTimeslotRejectNullHypothesis + "/" + Simulation.HOURS_PER_YEAR + " - " + 100 * perTimeslotRejectNullHypothesis / Simulation.HOURS_PER_YEAR + "%");
        assertFalse("The null hypothesis should not be rejected (mean equal to samples)", nullHypothesis);
    }

    @Test
    public void testArrivals() {
        System.err.println("\nARRIVALS CITY SQUARE:");
        runThinningAlgorithm(arrivals, 300);
    }

    @Test
    public void testDepartures() {
        //System.err.println("\nDEPARTURES CITY SQUARE:");
       // runThinningAlgorithm(departureDataCity, 100);
    }



}
