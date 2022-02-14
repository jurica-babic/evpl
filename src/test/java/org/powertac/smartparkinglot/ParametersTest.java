package org.powertac.smartparkinglot;

import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import org.powertac.smartparkinglot.domain.BatteryDescriptor;
import org.powertac.smartparkinglot.domain.MLRModel;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WillingnessToPayType;

import static org.junit.Assert.*;

/**
 * Created by Jurica on 27.10.2016..
 */
public class   ParametersTest {
    public static final int VALUE_INDEX = 1;
    private Parameters parameters;

    @Before
    public void setUp() {
        parameters = new Parameters();
    }

    @Test
    public void testEmptyParameters() {
        assertFalse("Default, empty parameters object should be invalid", parameters.checkParametersValidity());
    }

    @Test
    public void testMlrModel(){
        parameters.withMlrModel(null);
        assertNotNull("MLR model should never be null",parameters.getMlrModel());

        assertEquals("MLR model should default to AUSTRALIA", MLRModel.AUSTRALIA_MODEL, parameters.getMlrModel());

        parameters.withMlrModel(MLRModel.KORONA_2020);
        assertEquals("MLR model should be changed to KORONA2020", MLRModel.KORONA_2020, parameters.getMlrModel());

    }

    @Test
    public void testArea() {

        boolean exceptionThrown = false;
        try {
            parameters.withArea("WRONG AREA", true, true);
        } catch (HourlyValuesResolveException e) {
            exceptionThrown = true;
        }
        assertTrue("Exception is thrown", exceptionThrown);

        try {
            parameters.withArea("test", true, true);
            assertTrue("test".equals(parameters.getArea()));
            assertTrue(parameters.getArrivalValuesForYear()[VALUE_INDEX].length == Simulation.HOURS_PER_YEAR);
            assertTrue(parameters.getArrivalValuesForYear()[VALUE_INDEX].length == parameters.getDepartureValuesForYear()[VALUE_INDEX].length);
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMarketPriceFilename() {
        boolean notThrown = false;

        try {
            parameters.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY);
        } catch (HourlyValuesResolveException e) {
            notThrown = true;
        }
        assertFalse(notThrown);

        assertTrue(parameters.getMarketPriceValuesForYear()[VALUE_INDEX].length == Simulation.HOURS_PER_YEAR);

    }

    @Test
    public void testInputeRates(){
        parameters.withArea("Docklands", true, true);

        double[] arrivals = parameters.getArrivalValuesForYear()[VALUE_INDEX];
        double[] departures = parameters.getDepartureValuesForYear()[VALUE_INDEX];

        double minimumArrival = Double.MAX_VALUE;
        double minimumDeparture = Double.MAX_VALUE;

        for(int i = 0; i< arrivals.length; i++){
            assertNotEquals(0,arrivals[i], 0.001);
            assertNotEquals(0,departures[i], 0.001);
            if(arrivals[i]<minimumArrival){
                minimumArrival = arrivals[i];
            }
            if(departures[i]<minimumDeparture){
                minimumDeparture = departures[i];
            }
        }
        System.err.println("MIN ARR:"+minimumArrival+" MIN DEP:"+minimumDeparture);
    }

   /* @Test
    public void testBayesianParameters() {
        double[] shares = {0.91d, 0.09d};
        BatteryDescriptor[] batteryValues = {new BatteryDescriptor(0,0,0),new BatteryDescriptor(0,0,0)};
        try {
            parameters.withPopulationSize(5000).withParkingPrice(5).withChargingFee(2).withEVShare(0).withNonEVParkingSpots(0).withEVParkingSpots(150).withChargerSpeed(10).withChargerCost(1000);
            parameters.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY).withArea("test", true, true).withBatteryDescriptors(batteryValues).withBatteryDescriptorsShares(shares);
            parameters.withSeed(1).withUntil(10);

            parameters.withWillingnessToPayType(WillingnessToPayType.BAYESIAN_NETWORK);

            assertFalse("Bayesian network type without WTPspec does not make sense!", parameters.checkParametersValidity());

            parameters.withWTPBayesSpecification(WTPBayesSpecification.generateWTPSpecification("bn_learn.xml"));


            assertTrue("Now we have a valid set of parameters.", parameters.checkParametersValidity());

            parameters.withWillingnessToPayType(WillingnessToPayType.UTILITY_FUNCTION);
            assertTrue("When we set a utility function, the parameter's set becomes valid ", parameters.checkParametersValidity());


        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }
    }
    */


    @Test
    public void testConstructor() {
        int populationSize = 5000;
        double parkingPrice = 5.0d;

        double chargingFee = 3.0d;
        double evShare = 0.0d;
        int nonEVParkingSpots = 100;
        int evParkingSpots = 0;
        double chargerSpeed = 10.0d;
        double chargerCost = 1000.0d;
        String marketPrices = "/market-prices.txt";
        DataGranularity dataGranularity = DataGranularity.HOURLY;
        String area = "test";
        int seed = 1;
        double until = 3000.0d;
        WillingnessToPayType willingnessToPayType = WillingnessToPayType.UTILITY_FUNCTION;

        BatteryDescriptor[] batteryCapacityValues = {new BatteryDescriptor(0,0,0), new BatteryDescriptor(0,0,0)};
        double[] batteryCapacityShares = {0.9, 0.1};
        double parkingPriceHalfHour = 2.7;
        parameters = new Parameters(populationSize, parkingPrice, parkingPriceHalfHour, chargingFee, evShare, nonEVParkingSpots, evParkingSpots, chargerSpeed, chargerCost, marketPrices, dataGranularity, batteryCapacityValues, batteryCapacityShares, area, seed, until, willingnessToPayType);

        assertTrue("This constructor should result in a valid parameter set", parameters.checkParametersValidity());

        parameters = new Parameters(populationSize, parkingPrice,parkingPriceHalfHour, chargingFee, evShare, nonEVParkingSpots, evParkingSpots, chargerSpeed, chargerCost, marketPrices, dataGranularity, batteryCapacityValues, batteryCapacityShares, area, seed, until, WillingnessToPayType.BAYESIAN_NETWORK);
        assertFalse("However, this baby boy will not be valid", parameters.checkParametersValidity());

        MersenneTwisterFast randomer = new MersenneTwisterFast(parameters.getSeed());
        WTPBayesSpecification wtpBayesSpecification = WTPBayesSpecification.generateWTPSpecification("bn_learn.xml");
        wtpBayesSpecification.init(randomer, parameters.generateMarketPricesDescriptiveStatistics());
        parameters.withWTPBayesSpecification(wtpBayesSpecification);

        assertTrue("Now, this baby boy will be valid", parameters.checkParametersValidity());
    }

    @Test
    public void testCreateParametersWithSensibleDefaults(){
        Parameters parameters = Parameters.createParametersWithSensibleDefaults("Docklands", 0.5, 0.8, 100, 1);
        assertTrue(parameters.checkParametersValidity());
    }
}
