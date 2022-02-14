package org.powertac.smartparkinglot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.powertac.smartparkinglot.domain.BatteryDescriptor;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;
import org.powertac.smartparkinglot.util.WillingnessToPayType;
import sim.util.distribution.Exponential;

public class ProviderAgentTest {
    public static final double DELTA = 0.01;
    public static final double DELTA_ONE_DECIMAL = 0.1;
    private final double[] batteryCapacityShares = {0.81, 0.19};
    private final BatteryDescriptor[] batteryDescriptorsValues = {new BatteryDescriptor(0.52, 0.094, 24), new BatteryDescriptor(0.575, 0.102, 16)};

    private Simulation state;
    private String area = "test";
    private long duration = Simulation.HOURS_PER_YEAR * 2;

    @Before
    public void prepare() {

        state = new Simulation(1);
        try {
            Parameters parameters = new Parameters();
            parameters.withUntil(duration).withPopulationSize(5000).withEVShare(0).withNonEVParkingSpots(121).withEVParkingSpots(0).withArea(area, true, true).withBatteryDescriptorsShares(batteryCapacityShares).withBatteryDescriptors(batteryDescriptorsValues);
            parameters.withChargerSpeed(23.2).withChargerCost(333).withChargingFee(1.2).withParkingPrice(5.5).withWillingnessToPayType(WillingnessToPayType.UTILITY_FUNCTION);
            parameters.withWillingnessToPayType(WillingnessToPayType.UTILITY_FUNCTION).withArea("test", true, true);
            parameters.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY);

            state.parameters = parameters;
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDrawWillingnessToPayUtility(){
        Exponential exponential = new Exponential(1, new MersenneTwisterFast());
       System.out.println(exponential.nextDouble(5));

    }

    @Test
    public void testBatteryStatusDraw() {
        state.start();
        final int NO_ITERS = 10000;
        DescriptiveStatistics stat = new DescriptiveStatistics();
        for (int i = 0; i < NO_ITERS; i++) {
            double batteryStatus = state.providerAgent.drawBatteryStatus(24, 0.52, 0.094);
            stat.addValue(batteryStatus);
            assertTrue(batteryStatus > 0 && batteryStatus < 24);
        }
        assertEquals("Mean should be okayish...",0.52*24, stat.getMean(), DELTA_ONE_DECIMAL);
        assertTrue("Min should never be under zero:", stat.getMin()>=0);
        assertTrue("Max should neveer be over 24", stat.getMax()<=24);
        assertEquals("STDEV should be okayish", 0.094*24, stat.getStandardDeviation(), DELTA);
    }

    @Test
    public void testBatteryCapacityDistribution() {
        state.start();
        double[] probs = state.providerAgent.getBatteryDescriptorProbs();

        assertEquals("There are two probs.", 2, probs.length);
        assertEquals(probs[0], 0.81, DELTA);
        assertEquals(probs[1], 0.19, DELTA);

        BatteryDescriptor[] values = state.providerAgent.getBatteryDescriptorValues();

        assertEquals("There are two values.", 2, probs.length);
        assertEquals(values[0].getBatteryCapacity(), 24, DELTA);
        assertEquals(values[1].getBatteryCapacity(), 16, DELTA);

        final int DRAWS_CNT = 10000;
        double no16s = 0;
        double no24s = 0;
        for (int i = 0; i < 10000; i++) {
            double batteryCapacity = state.providerAgent.drawBatteryDescriptor().getBatteryCapacity();
            assertTrue(batteryCapacity == 24 || batteryCapacity == 16);
            if (batteryCapacity == 24) {
                no24s++;
            } else if (batteryCapacity == 16) {
                no16s++;
            }
        }
        assertEquals(0.19, no16s / DRAWS_CNT, 0.01);
        assertEquals(0.81, no24s / DRAWS_CNT, 0.1);

    }

    @Test
    public void testProviderAgent() {
        int expected = 4;
        state.start();
        do {
            if (!state.schedule.step(state))
                break;
            if (state.schedule.getTime() >= 0 && state.schedule.getTime() < 1) {

                assertEquals(4, state.providerAgent.getCurrentArrivalRate(), SimulationTest.DELTA);
                assertEquals(0.4564, state.providerAgent.getCurrentDepartureRate(), SimulationTest.DELTA);
            } else if (state.schedule.getTime() >= 8759 && state.schedule.getTime() < 8759.9) {
                assertEquals(4, state.providerAgent.getCurrentArrivalRate(), SimulationTest.DELTA);
                assertEquals(7.3022, state.providerAgent.getCurrentDepartureRate(), SimulationTest.DELTA);
            } else if (state.schedule.getTime() >= 8760 && state.schedule.getTime() < 8760.9) {
                assertEquals(4, state.providerAgent.getCurrentArrivalRate(), SimulationTest.DELTA);
                assertEquals(0.4564, state.providerAgent.getCurrentDepartureRate(), SimulationTest.DELTA);
            } else if (state.schedule.getTime() >= 8761 && state.schedule.getTime() < 8761.9) {
                assertEquals(4, state.providerAgent.getCurrentArrivalRate(), SimulationTest.DELTA);
                assertEquals(0.3294, state.providerAgent.getCurrentDepartureRate(), SimulationTest.DELTA);
            }
        } while (state.schedule.getTime() < duration);
        state.finish();
    }
}
