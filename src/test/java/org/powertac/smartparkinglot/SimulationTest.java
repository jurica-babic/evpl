package org.powertac.smartparkinglot;

import java.io.*;

import static org.junit.Assert.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Before;
import org.junit.Test;

import org.powertac.smartparkinglot.domain.BatteryDescriptor;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WillingnessToPayType;

public class SimulationTest {

    public static final double DELTA = 0.001;

    public static final double DELTA_RATES = 1.0;

    private String marketPriceFilename = "/market-prices.txt";

    private String area = "Docklands";

    private int populationSize = 5000;

    private double parkingPrice = 5.5;

    private double chargingFee = 0.3;

    private double evShare = 0.9;

    private int nonEvParkingSpots = 500;

    private int evParkingSpots = 500;

    private double chargerSpeed = 7.7;

    private double chargerCost = 3000;

    private final BatteryDescriptor[] batteryDescriptorsValues = {new BatteryDescriptor(0.52, 0.094, 24), new BatteryDescriptor(0.575, 0.102, 16)};


    private double[] batteryCapacityShares = {0.81, 0.19};

    private long until = Simulation.HOURS_PER_YEAR;

    private long seed = 2;

    Simulation finishedSimulation;

    @Before
    public void before() {
        WillingnessToPayType willingnessToPayType = WillingnessToPayType.MULTIPLE_LINEAR_REGRESSION;
        try {
            double[][] prices = Util.getHourlyValuesForAYear("/market-prices.txt", 10, DataGranularity.HOURLY);
            Parameters parameters = new Parameters(1000, 5.5, 2,
                    0.3, 150, 221 - 150, 7.7, 8200,
                    prices, batteryDescriptorsValues, batteryCapacityShares, area, Util.getHourlyValuesForAYear("/" + area + "-arrivals.txt", 10,
                    DataGranularity.HOURLY), Util.getHourlyValuesForAYear("/" + area + "-departures.txt", 10,
                    DataGranularity.HOURLY), seed, until, willingnessToPayType);
            DescriptiveStatistics descriptiveStatistics = Parameters.generateMarketPricesDescriptiveStatistics(prices);

            DescriptiveStatistics newMarketPrices = descriptiveStatistics == null ? new DescriptiveStatistics() : new DescriptiveStatistics(descriptiveStatistics.getValues());


            finishedSimulation = new Simulation(seed);
            // if needed add wtp bayes specification:
            switch (willingnessToPayType) {
                case UTILITY_FUNCTION:
                    break;
                case BAYESIAN_NETWORK:
                    InputStream is = Util.class.getResourceAsStream("/bn_learn.xml");
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    WTPBayesSpecification wtpBayes = WTPBayesSpecification.generateWTPSpecification(br);
                    WTPBayesSpecification wtpBayesSpecification = WTPBayesSpecification.cloneMothafucka(wtpBayes);
                    wtpBayesSpecification.init(finishedSimulation.random, newMarketPrices);
                    parameters.withWTPBayesSpecification(wtpBayesSpecification);
                    break;
                case MULTIPLE_LINEAR_REGRESSION:
                    break;
                default:
                    break;
            }
            finishedSimulation.parameters = parameters;
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testDocklandsAllIn(){
        String[] args = {"--area", "Docklands", "--parkingPolicyType", "PARKING_LOT_DEDICATED_CHARGING", "--evParkingSpots", "524", "--totalParkingSpots", "524", "--chargingFee", "0.0", "--evShare", "1.0", "--repetitions", "3", "--printDiagnostics"};
        // test will pass if no error accurs:
        Simulation.jobExecutor(args,false);
    }

    @Test
    public void testDocklandsLongitudinal(){
        String[] args = {"--longitudinalTracking","--area", "Docklands","--parkingPolicyType", "PARKING_LOT_DEDICATED_CHARGING",  "--evParkingSpots", "0", "--totalParkingSpots", "524", "--chargingFee", "0.0", "--evShare", "0.0", "--repetitions", "3", "--printDiagnostics"};
        // test will pass if no error accurs:
        Simulation.jobExecutor(args,false);
    }

//    @Test
//    public void testTavistock2(){
//        String[] args = {"--area", "Tavistock","--parkingPolicyType", "PARKING_LOT_DEDICATED_CHARGING",  "--evParkingSpots", "2", "--totalParkingSpots", "7", "--chargingFee", "0.45", "--evShare", "0.5", "--repetitions", "100", "--printDiagnostics"};
//        // test will pass if no error accurs:
//        Simulation.jobExecutor(args,false);
//    }
//
//    @Test
//    public void testCitySquare(){
//        String[] args = {"--area", "City Square","--parkingPolicyType", "PARKING_LOT_DEDICATED_CHARGING",  "--evParkingSpots", "0", "--totalParkingSpots", "111", "--chargingFee", "0.0", "--evShare", "0.0", "--repetitions", "100", "--printDiagnostics"};
//        // test will pass if no error accurs:
//        Simulation.jobExecutor(args,false);
//    }
//
//    @Test
//    public void testPrincesTheatre(){
//        //String[] args = {"--area", "Princes Theatre", "--evParkingSpots", "111", "--totalParkingSpots", "221", "--chargingFee", "0.1", "--evShare", "0.8", "--repetitions", "10", "--printDiagnostics"};
//        //Simulation.jobExecutor(args,false);
//    }

    @Test
    public void testMainHelp(){
        String[] args = {"--help"};
        Simulation.jobExecutor(args,false);

    }

    @Test
    public void testMultipleLinearRegression() {

        finishedSimulation.start();



        SummaryStatistics marketPrices = new SummaryStatistics();
        for(double value : finishedSimulation.parameters.getMarketPriceValuesForYear()[1]){
            marketPrices.addValue(value/1000);
        }
//        double groundedReferenceElectricityPrice = 0.2187;
//        System.err.println("WTPs for the grounded price:"+ groundedReferenceElectricityPrice);
//        printWTP(groundedReferenceElectricityPrice, 16);
//        System.err.println("WTPs for the grounded price:"+ groundedReferenceElectricityPrice);
//        printWTP(groundedReferenceElectricityPrice, 24);


        System.err.println("WTPs for the MIN price:"+marketPrices.getMin());

        printWTP(marketPrices.getMin(), 16);
        System.err.println("WTPs for the MEAN price:"+marketPrices.getMean());
        printWTP(marketPrices.getMean(), 16);
        System.err.println("WTPs for the MEAN price:"+marketPrices.getMean());
        printWTP(marketPrices.getMean(), 24);
        System.err.println("WTPs for the MAX price:"+marketPrices.getMax());
        printWTP(marketPrices.getMax(), 16);

    }

    private void printWTP(double referenceElectricityPrice, double batteryCapacity) {
        System.err.printf("BC\tBS\tRP(h)\tWTP\t\n");

        for(double batteryStatus=0; batteryStatus<=batteryCapacity;batteryStatus++){
            System.err.printf("%.0f\t%.2f\t%.4f\t%.2f\t\n", batteryCapacity, batteryStatus, referenceElectricityPrice*finishedSimulation.parameters.getChargerSpeed(), finishedSimulation.providerAgent.drawWillingnessToPayMultipleLinearRegression(batteryCapacity, batteryStatus, referenceElectricityPrice, finishedSimulation.parameters.getMlrModel()));
        }
    }

//    @Test
//    public void testTavistock() {
//
//        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(myOut));
//
//
//        try {
//            finishedSimulation.parameters.withArea("Tavistock", true, false);
//        } catch (HourlyValuesResolveException e) {
//            e.printStackTrace();
//        }
//        finishedSimulation.parameters.withEVShare(0.5).withChargingFee(0.45);
//        finishedSimulation.parameters.withEVParkingSpots(5).withNonEVParkingSpots(2);
//        assertTrue(finishedSimulation.parameters.checkParametersValidity());
//        finishedSimulation = Simulation.run(finishedSimulation);
//
//
//        final String standardOutput = myOut.toString();
//
//        System.out.println("Ispis stderr: " + standardOutput);
//    }

    @Test
    public void testHighEVShareScenarioFreeElectricityAllEVPL() {
        try {
            finishedSimulation.parameters.withArea("City Square", true, true);
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }
        finishedSimulation.parameters.withEVShare(1).withChargingFee(0);
        finishedSimulation.parameters.withEVParkingSpots(221).withNonEVParkingSpots(0);
        assertTrue(finishedSimulation.parameters.checkParametersValidity());
        finishedSimulation = Simulation.run(finishedSimulation);
    }


    @Test
    public void test() {
        assertTrue(finishedSimulation.parameters.checkParametersValidity());
        finishedSimulation = Simulation.run(finishedSimulation);
    }

    //@Test
    //public void testQueueingModel() {


	/*	long duration = Simulation.HOURS_PER_YEAR * 5;
        String[] args = { "-until", "" + duration, "-populationSize", "5000", "-defaultArrivalRate", "140",
				"-defaultDepartureRate", "15", "-evShare", "0.0", "-nonEvParkingSpots", "10", "-evParkingSpots", "0",
				"-batteryCapacitySharesString", "1", "-batteryCapacityValuesString", "1.0", "-chargerCost", "333", "-chargerSpeed",
				"1", "-electricityFee", "1.2", "-parkingPrice", "5.5", "-referenceElectricityPrice", "0.2187" };

		

		Simulation state = new Simulation(1);
		Parameters parameters = new Parameters();
		parameters.withUntil(duration).withPopulationSize(5000).witharr
		state.parameters =
		state.start();
		do
			if (!state.schedule.step(state))
				break;
		while (state.schedule.getTime() < duration);
		state.finish();

		System.out.println(
				"Steps:" + state.schedule.getSteps() + " REJECTION RATE:" + state.smartParkingLot.getRejectionRate()
						+ " OCCUPANCY RATE:" + state.smartParkingLot.getOccupancyRate());

		// calculated values from:
		// http://obp.math.vu.nl/healthcare/software/erlangB/erlangb.php,
		// arrivals: 140, ALOS: 0.0666666666666666, beds: 10
		double expectedRejections = 0.1836;
		double expectedOccupancy = 0.762;

		// TODO: statistical test instead of junit. However, for a specified
		// seed the results are fine.
		assertEquals(expectedOccupancy, state.smartParkingLot.getOccupancyRate(), DELTA_RATES);
		assertEquals(expectedRejections, state.smartParkingLot.getRejectionRate(), DELTA_RATES); */


//	}

}
