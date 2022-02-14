package org.powertac.smartparkinglot.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.powertac.smartparkinglot.DataGranularity;
import org.powertac.smartparkinglot.Util;

import static org.junit.Assert.*;

public class WTPSpecificationTest {

    public static final int VALUE_INDEX = 1;
    private WTPBayesSpecification wtpSpecification;
    private static final double DELTA = 0.001;
    private static final double DELTA2 = 0.1;
    private static final double[] lowlowslow = {0.167664671, 0.410179641, 0.167664671, 0.248502994, 0.005988024};
    private static final double[] highhighhigh = {0.144329897, 0.005154639, 0.213917526, 0.353092784, 0.283505155};


    @Before
    public void init() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("bn_learn.xml").getFile()));
            wtpSpecification = WTPBayesSpecification.generateWTPSpecification(br);

            DescriptiveStatistics stats = new DescriptiveStatistics();
            double[] prices = Util.getHourlyValuesForAYear("/market-prices.txt", 10, DataGranularity.HOURLY)[VALUE_INDEX];
            for (double price : prices) {
                stats.addValue(price);
            }
            wtpSpecification.init(new MersenneTwisterFast(0), stats);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testWTP() {
        assertEquals("Price cardinality should be as expected", 3, wtpSpecification.getPriceCardinality());
        assertArrayEquals(lowlowslow, wtpSpecification.getProbabilityMap().get(0, 0, 0), DELTA);
        assertArrayEquals(highhighhigh, wtpSpecification.getProbabilityMap().get(2, 2, 2), DELTA);
    }

    @Test
    public void testSOCtoDiscreteSOC() {
        int invalidSOC = wtpSpecification.socToDiscreteSOC(-5);
        assertEquals("SOC should not be invalid", -1, invalidSOC);
        int invalidSOC2 = wtpSpecification.socToDiscreteSOC(33);
        assertEquals("SOC should be maximum", 2, invalidSOC2);
        int validSOC0 = wtpSpecification.socToDiscreteSOC(0);
        assertEquals("SOC should be 0", 0, validSOC0);
        int validSOC100 = wtpSpecification.socToDiscreteSOC(1);
        assertEquals("SOC should be 2 (HIGH)", 2, validSOC100);

        assertEquals("SOC should be 0 (LOW)", 0, wtpSpecification.socToDiscreteSOC(0.32));
        assertEquals("SOC should be 1 (MED)", 1, wtpSpecification.socToDiscreteSOC(0.66));
        assertEquals("SOC should be 2 (HIGH)", 2, wtpSpecification.socToDiscreteSOC(0.99));
    }


    @Test
    public void testIsMatchAndQuery() {
        double[] lowlowlowCDFExpected = {lowlowslow[0] + lowlowslow[1] + lowlowslow[2] + lowlowslow[3] + lowlowslow[4], lowlowslow[1] + lowlowslow[2] + lowlowslow[3] + lowlowslow[4], lowlowslow[2] + lowlowslow[3] + lowlowslow[4], lowlowslow[3] + lowlowslow[4], lowlowslow[4]};
        double[] lowlowlowCDFActual = {0d, 0d, 0d, 0d, 0d};
        double[] lowlowlowActual = {0d, 0d, 0d, 0d, 0d};
        int iterations = 1000000;

        // if we run a lot of queries, it is highly expected that the expected distribution matches the actual distribution with a certain DELTA tolerance.

        // VERY LOW WTP:
        final int VERY_LOW_WTP = 0;
        final int LOW_WTP = 1;
        final int MED_WTP = 2;
        final int HIGH_WTP = 3;
        final int VERY_HIGH_WTP = 4;

        // IS MATCH:
        for (int i = 0; i < iterations; i++) {
            if (wtpSpecification.isMatch(0, 0, 0, VERY_LOW_WTP)) {
                lowlowlowCDFActual[VERY_LOW_WTP]++;
            }
            if (wtpSpecification.isMatch(0, 0, 0, LOW_WTP)) {
                lowlowlowCDFActual[LOW_WTP]++;
            }
            if (wtpSpecification.isMatch(0, 0, 0, MED_WTP)) {
                lowlowlowCDFActual[MED_WTP]++;
            }
            if (wtpSpecification.isMatch(0, 0, 0, HIGH_WTP)) {
                lowlowlowCDFActual[HIGH_WTP]++;
            }
            if (wtpSpecification.isMatch(0, 0, 0, VERY_HIGH_WTP)) {
                lowlowlowCDFActual[VERY_HIGH_WTP]++;
            }
        }
        lowlowlowCDFActual[VERY_LOW_WTP] = lowlowlowCDFActual[VERY_LOW_WTP] / iterations;
        lowlowlowCDFActual[LOW_WTP] = lowlowlowCDFActual[LOW_WTP] / iterations;
        lowlowlowCDFActual[MED_WTP] = lowlowlowCDFActual[MED_WTP] / iterations;
        lowlowlowCDFActual[HIGH_WTP] = lowlowlowCDFActual[HIGH_WTP] / iterations;
        lowlowlowCDFActual[VERY_HIGH_WTP] = lowlowlowCDFActual[VERY_HIGH_WTP] / iterations;

        assertArrayEquals("Distributions should be equal", lowlowlowCDFExpected, lowlowlowCDFActual, DELTA);

        for (int i = 0; i < iterations; i++) {

            switch (wtpSpecification.queryWTPGivenEvidence(0, 0, 0)) {
                case VERY_LOW_WTP:
                    lowlowlowActual[VERY_LOW_WTP]++;
                    break;
                case LOW_WTP:
                    lowlowlowActual[LOW_WTP]++;
                    break;
                case MED_WTP:
                    lowlowlowActual[MED_WTP]++;
                    break;
                case HIGH_WTP:
                    lowlowlowActual[HIGH_WTP]++;
                    break;
                case VERY_HIGH_WTP:
                    lowlowlowActual[VERY_HIGH_WTP]++;
                    break;
                default:
                    break;
            }
        }
        lowlowlowActual[VERY_LOW_WTP] = lowlowlowActual[VERY_LOW_WTP] / iterations;
        lowlowlowActual[LOW_WTP] = lowlowlowActual[LOW_WTP] / iterations;
        lowlowlowActual[MED_WTP] = lowlowlowActual[MED_WTP] / iterations;
        lowlowlowActual[HIGH_WTP] = lowlowlowActual[HIGH_WTP] / iterations;
        lowlowlowActual[VERY_HIGH_WTP] = lowlowlowActual[VERY_HIGH_WTP] / iterations;

        assertArrayEquals("Distributions should be equal", lowlowslow, lowlowlowActual, DELTA);

        assertEquals("Query with invalid discrete values should return -1", -1, wtpSpecification.queryWTPGivenEvidence(10, 10, 10));

    }

    @Test
    public void testSpeedToSpeedDiscrete(){
        assertEquals(-1, wtpSpecification.speedToSpeedDiscrete(-3));
        assertEquals(0, wtpSpecification.speedToSpeedDiscrete(1));

        assertEquals(1, wtpSpecification.speedToSpeedDiscrete(9));
        assertEquals(2, wtpSpecification.speedToSpeedDiscrete(9999));
    }

    @Test
    public void testPriceToDiscretePrice() {
        // First let us build some confidence in our dataset and DescriptiveStatistics class by comparing results to results from RStudio:
        /*
        *    Min. 1st Qu.  Median    Mean 3rd Qu.    Max.
            6.69   24.09   26.80   29.37   30.30  495.10

            Three quantiles:
                 33%      66%     100%
                25.0136  28.5420 495.0700

        * */

     /*
        TODO update below tests for the new dataset
        DescriptiveStatistics priceStatistics = wtpSpecification.getPriceStatistics();
        assertEquals(6.69, priceStatistics.getMin(), DELTA2);
        assertEquals(29.37, priceStatistics.getMean(), DELTA2);
        assertEquals(495.10, priceStatistics.getMax(), DELTA2);
        assertEquals(26.80, priceStatistics.getPercentile(50), DELTA2);

        // quantile TEST:
        assertEquals(25.0136, priceStatistics.getPercentile(33), DELTA2);
        assertEquals(28.5420, priceStatistics.getPercentile(66), DELTA2);
        assertEquals(495.0700, priceStatistics.getPercentile(100), DELTA2);

        // OK, now test quantiles from WTP bayes by testing priceToDiscretePrice method.
        assertEquals(-1, wtpSpecification.priceToDiscretePrice(-25/1000.0));
        assertEquals(2, wtpSpecification.priceToDiscretePrice(495.55/1000.0));
        assertEquals(0, wtpSpecification.priceToDiscretePrice(10/1000.0));
        assertEquals(1, wtpSpecification.priceToDiscretePrice(27/1000.0));
        assertEquals(2, wtpSpecification.priceToDiscretePrice(30/1000.0));
        assertEquals(2, wtpSpecification.priceToDiscretePrice(234432234.0d));
        */

    }

    @Test
    public void testInvalidWTP(){
        WTPBayesSpecification wtpSpec = WTPBayesSpecification.generateWTPSpecification("INVALID FILE NAME");
        assertNull(wtpSpec);
    }

    @Test
    public void testGoodWTP(){
        WTPBayesSpecification wtpSpec = WTPBayesSpecification.generateWTPSpecification("bn_learn.xml");
        assertNotNull(wtpSpec);
    }

}
