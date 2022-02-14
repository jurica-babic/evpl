package org.powertac.smartparkinglot;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.time.StopWatch;
import org.powertac.smartparkinglot.domain.*;
import org.powertac.smartparkinglot.exception.InvalidBatteryCapacityDistributionException;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import sim.engine.SimState;
import sim.util.Bag;

import java.io.*;
import java.util.*;

/**
 * Main class, entry point for the simulation.
 *
 * @author Jurica Babic
 */
public class Simulation extends SimState {

    // CONSTANTS:
    public static final int WHOLESALE_MARKET_ORDER = 0;
    public static final int PROVIDER_AGENT_ORDER = 0;
    public static final int CAR_ARRIVAL_EVENT_ORDER = 2;
    private static final int TRACKER_AGENT_ORDER = 99;

    public static final int HOURS_PER_YEAR = 8760;

    public static final int DAYS_PER_YEAR = 366;
    public static final int FIRST_TS_HOLDER = 0;
    public static final int CHARGING_EXECUTION_EVENT = 98;


    // SIM PARAMETERS:
    public Parameters parameters;


    // AGENTS: /////////////////////////////////
    /**
     * Population of cars (both EVs and ICVs), potential smart parking lot
     * customers.
     */
    public Bag population;

    /**
     * Cars not in a parking lot.
     */
    public Bag outsideCars;

    /**
     * Smart parking lot agent, does parking and electricity services.
     */
    public SmartParkingLotAgent smartParkingLot;

    /**
     * Wholesale market agent, provides the electricity market price.
     */
    public WholesaleMarket wholesaleMarket;

    public SimulationTrackerAgent trackerAgent;


    ///////////////////////////////////////////

    public Simulation(long seed) {
        super(seed);
    }

    /**
     * Provides arrival and departure rates.
     */
    public ProviderAgent providerAgent;

//	public static void main(String[] args) {
//		Random generator = new Random(System.currentTimeMillis());
//		Simulation sim = new Simulation(generator.nextLong());
//		sim.args = args;
//
//		sim.start();
//		do
//			if (!sim.schedule.step(sim))
//				break;
//		while (sim.schedule.getTime() < sim.parameters.until);
//		sim.finish();
//
//	}

    /**
     * Support method for testing. Sunfire does not like calls to System.exit so we leave that operation to the main method.
     *
     * @param args
     */
    public static void jobExecutor(String[] args, boolean calledFromMain) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        Options options = new Options();
        Option[] requiredOptionArray = new Option[]{
                new Option("a", "area", true, "area you want to use (e.g. Docklands - 524 spots)"),//(e.g., Chinatown - 97.4 percentage - 91 spots , City Square - 97.8 percentage - 111 spots or Princes Theatre - 98.8 percentage - 221 spots)"),
                new Option("ev", "evParkingSpots", true, "number of parking spots with an EV charger"),
                new Option("spots", "totalParkingSpots", true, "total number of parking spots in EVPL (EV and non-EV spots combined)"),
                new Option("cf", "chargingFee", true, "premium price for EV spots"),
                new Option("s", "evShare", true, "the proportion of cars that are EVs"),
                new Option("r", "repetitions", true, "number of repetitions of a same scenario with different seed values"),
                new Option("p", "parkingPolicyType", true, "parking policy to be used: PARKING_LOT_DEDICATED_CHARGING, EV_EXCLUSIVE_SPOTS, FREE_FOR_ALL")

        };
        for (Option option : requiredOptionArray) {
            option.setRequired(true);
            options.addOption(option);
        }

        Option chargerSpeedOption = new Option("cs", "chargerSpeed", true, "Charger speed in kW to be used.");
        options.addOption(chargerSpeedOption);

        Option isLongitudinalTrackingOption = new Option("long", "longitudinalTracking", false, "if set, simulator will have per time slot values, otherwise, only aggregate values will be available");
        options.addOption(isLongitudinalTrackingOption);

        Option isPrintDiagnosticsOption = new Option("pd", "printDiagnostics", false, "if set, simulator will print some diagnostics on standard error (i.e., a batch processing duration).");
        options.addOption(isPrintDiagnosticsOption);

        Option helpOption = new Option("h", "help", false, "displays a help");
        options.addOption(helpOption);


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            for (String arg : args)
                if (arg.contains("help")) {
                    HelpFormatter helpFormatter = new HelpFormatter();
                    helpFormatter.printHelp("sim.jar", options);
                    if (calledFromMain) System.exit(1);
                    return;
                }
            e.printStackTrace();
            if (calledFromMain) System.exit(1);
            return;
        }
        Double chargerSpeed = null;
        String chargerSpeedString = cmd.getOptionValue("chargerSpeed");
        if (chargerSpeed != null) {
            chargerSpeed = Double.parseDouble(chargerSpeedString);
        }


        String area = cmd.getOptionValue("area");
        int evParkingSpots = Integer.parseInt(cmd.getOptionValue("evParkingSpots"));
        int totalParkingSpots = Integer.parseInt(cmd.getOptionValue("totalParkingSpots"));
        int nonEvParkingSpots = totalParkingSpots - evParkingSpots;
        double chargingFee = Double.parseDouble(cmd.getOptionValue("chargingFee"));
        double evShare = Double.parseDouble(cmd.getOptionValue("evShare"));
        ParkingPolicyType parkingPolicyType = ParkingPolicyType.valueOf(cmd.getOptionValue("parkingPolicyType"));
        boolean isLongitudinalTracking = false;
        if (cmd.hasOption("longitudinalTracking")) {
            isLongitudinalTracking = true;
        }
        boolean printDiagnostics = false;
        if (cmd.hasOption("printDiagnostics")) {
            // TODO implement
            printDiagnostics = true;
        }
        int repetitions = Integer.parseInt(cmd.getOptionValue("repetitions"));

        Parameters parameters = Parameters.createParametersWithSensibleDefaults(area, chargingFee, evShare, nonEvParkingSpots, evParkingSpots);
        if (chargerSpeed != null) {
            parameters.withChargerSpeed(chargerSpeed);
        }
        parameters.withParkingPolicyType(parkingPolicyType);
        parameters.withIsLongitudinalTracking(isLongitudinalTracking);

        // LOAD PROPERTIES:
        final Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            System.err.println("Cannot open properties!");
        }
        String version = properties.getProperty("version");
        String artifactId = properties.getProperty("artifactId");

        Random random = new Random();

        Writer arrivalLongitudinalWriter = null;
        Writer depratureLongitudinalWriter = null;
        Writer parkingDurationLongitudinalHrsWriter = null;
        List<HashMap<Integer, ValuesHolder>> perIterationValues = null;
        if (isLongitudinalTracking) {
            try {
                arrivalLongitudinalWriter = new PrintWriter("arrivalsLongitudinal.txt");
                depratureLongitudinalWriter = new PrintWriter("departuresLongitudinal.txt");
                parkingDurationLongitudinalHrsWriter = new PrintWriter("parkingDurationLongitudinal.txt");
                perIterationValues = new ArrayList<>();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // CSV:
        ICsvBeanWriter beanWriter = new CsvBeanWriter(new OutputStreamWriter(System.out), CsvPreference.TAB_PREFERENCE);
        try {
            beanWriter.writeHeader(ValuesHolder.getHeader());
            // run as many repetitions as needed. The sim will only change input parameters and seed:
            for (int i = 0; i < repetitions; i++) {
                parameters.withSeed(random.nextLong());
                Simulation sim = new Simulation(parameters.getSeed());
                sim.parameters = parameters;
                sim = run(sim);

                // report what you need (seed and KPI-es):
                ValuesHolder valueHolder = sim.trackerAgent.getTsValuesHolder().get(FIRST_TS_HOLDER);
                valueHolder.setVersion(version);
                valueHolder.setChargingFee(parameters.getChargingFee());
                valueHolder.setEvParkingSpots(parameters.getEvParkingSpots());
                valueHolder.setEvShare(parameters.getEvShare());
                valueHolder.setArea(parameters.getArea());
                valueHolder.setTotalParkingSpots(parameters.getEvParkingSpots() + parameters.getNonEVParkingSpots());
                valueHolder.setEvplTotalProfit(valueHolder.getEvplParkingMoneyAll() + valueHolder.getEvplElectricityMoneyFromEVSpot() - valueHolder.getEvplElectricityCostFromMarket());
                valueHolder.setParkingPolicyType(parameters.getParkingPolicyType().toString());

                double evplGrossProfitEVSpotElectricity = valueHolder.getEvplElectricityMoneyFromEVSpot() - valueHolder.getEvplElectricityCostFromMarket();
                double evplGrossProfitEVSpot = valueHolder.getEvplParkingMoneyEVSpot() + evplGrossProfitEVSpotElectricity;

                valueHolder.setEvplGrossProfitEVSpotElectricity(evplGrossProfitEVSpotElectricity);
                valueHolder.setEvplGrossProfitEVSpot(evplGrossProfitEVSpot);

                double duration = parameters.getUntil();
                int parkingSpots = parameters.getEvParkingSpots() + parameters.getNonEVParkingSpots();
                double parkingUtil = valueHolder.getEvplTotalParkingDurationHrs() / (parkingSpots * duration);
                valueHolder.setParkingUtil(parkingUtil);

                double evplTotalParkingDurationEVSpotHrs = valueHolder.getEvplTotalParkingDurationEVSpotHrs();
                double chargingUtil = evplTotalParkingDurationEVSpotHrs>0? valueHolder.getEvplTotalElectricityProcuredkWh() / (evplTotalParkingDurationEVSpotHrs * parameters.getChargerSpeed()):0;
                valueHolder.setChargingUtil(chargingUtil);


                // parking duration?
                //    valueHolder.setAvgParkingDurationHrs(sim.trackerAgent.getParkingTimeStatistics().getMean());
                beanWriter.write(valueHolder, ValuesHolder.getHeader(), ValuesHolder.getProcessors());

                if (isLongitudinalTracking) {
                    perIterationValues.add(sim.trackerAgent.getTsValuesHolder());
                }


            }
            beanWriter.close();
            stopWatch.stop();
            if (printDiagnostics) {
                System.err.println(stopWatch.toString());
            }

            if (isLongitudinalTracking) {

                for (int i = 0; i < parameters.getUntil(); i++) {
                    boolean first = true;
                    for (HashMap<Integer, ValuesHolder> holderMap : perIterationValues) {
                        String space = first ? "" : "\t";
                        first = false;
                        arrivalLongitudinalWriter.write(space + holderMap.get(i).getArrivalsCount());
                        depratureLongitudinalWriter.write(space + holderMap.get(i).getDeparturesCount());
                        parkingDurationLongitudinalHrsWriter.write(space + String.format(Locale.US, "%.2f", holderMap.get(i).getEvplTotalParkingDurationHrs()));
                    }
                    arrivalLongitudinalWriter.write("\n");
                    depratureLongitudinalWriter.write("\n");
                    parkingDurationLongitudinalHrsWriter.write("\n");
                }
                arrivalLongitudinalWriter.flush();
                arrivalLongitudinalWriter.close();
                depratureLongitudinalWriter.flush();
                depratureLongitudinalWriter.close();
                parkingDurationLongitudinalHrsWriter.flush();
                parkingDurationLongitudinalHrsWriter.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
            if (calledFromMain) System.exit(1);
            return;
        }
    }

    public static void main(String[] args) {
        jobExecutor(args, true);
        // FINISHED:
        System.exit(0);

    }


    /**
     * @param sim a Simulation object containing parameters object as well. This method will start the simulation.
     * @return
     */
    public static Simulation run(Simulation sim) {
        sim.start();
        do
            if (!sim.schedule.step(sim))
                break;
        while (sim.schedule.getTime() < sim.parameters.getUntil());
        sim.finish();

        return sim;

    }

    @Override
    public void start() {
        super.start();


        if (!parameters.checkParametersValidity()) {
            System.err.println("Problem with params :(");
            System.exit(1);
        }

        try {
            providerAgent = new ProviderAgent(this,
                    parameters.getArrivalValuesForYear(),
                    parameters.getDepartureValuesForYear(), parameters.getBatteryCapacityShares(), parameters.getBatteryDescriptors(), parameters.getWtpBayesSpecification());
        } catch (InvalidBatteryCapacityDistributionException e) {
            e.printStackTrace();
            System.err.println("There is an issue with battery capacity distribution, aborting sim.");
            System.exit(1);
        }

        schedule.scheduleRepeating(providerAgent, PROVIDER_AGENT_ORDER, 1);

        instantiatePopulation();

        wholesaleMarket = new WholesaleMarketAgent(parameters.getMarketPriceValuesForYear());

        // schedule wholesale market agent to do a step every time slot, with a
        // top priority (meaning, smart parking lot will be able to use updated
        // price).
        schedule.scheduleRepeating(wholesaleMarket, WHOLESALE_MARKET_ORDER, 1);

        // tracking
        trackerAgent = new SimulationTrackerAgent((int) parameters.getUntil(), parameters.isLongitudinalTracking(), parameters.getSeed());
        schedule.scheduleRepeating(trackerAgent, TRACKER_AGENT_ORDER, 1);

        smartParkingLot = new SmartParkingLotAgent(parameters.getEvParkingSpots(), parameters.getNonEVParkingSpots(),
                parameters.getChargingFee(), parameters.getParkingPrice(),parameters.getParkingPriceHalfHour(), parameters.getChargerSpeed(), providerAgent, parameters.getParkingPolicyType());

        schedule.scheduleRepeating(smartParkingLot);

        // kick-off the simulation:
        providerAgent.scheduleInitialArrival();


    }

    @Override
    public void finish() {
        super.finish();


    }

    private void instantiatePopulation() {
        population = new Bag(parameters.getPopulationSize());


        // how much EVs and ICVs
        int EVCnt = (int) (parameters.getPopulationSize() * parameters.getEvShare());
        int ICVCnt = parameters.getPopulationSize() - EVCnt;

        // instantiate EVs:
        for (int i = 0; i < EVCnt; i++) {
            population.add(new EVAgent(i, EVType.BEV, providerAgent.drawBatteryDescriptor(), providerAgent, parameters.getWillingnessToPayType(), parameters.getWtpBayesSpecification(), providerAgent, providerAgent, parameters.getMlrModel()));
        }
        // instantiate ICVs:
        for (int i = 0; i < ICVCnt; i++) {
            population.add(new ICVAgent(EVCnt + 1 + i));
        }
        // shuffle a bit:
        population.shuffle(random);

        // initially, all cars are outside the parking lot
        outsideCars = new Bag(population);

    }

}
