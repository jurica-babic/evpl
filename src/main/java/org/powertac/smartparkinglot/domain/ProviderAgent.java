package org.powertac.smartparkinglot.domain;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.powertac.smartparkinglot.*;
import org.powertac.smartparkinglot.ValuesHolder;
import org.powertac.smartparkinglot.event.ArrivalEvent;
import org.powertac.smartparkinglot.event.ChargingExecutionEvent;
import org.powertac.smartparkinglot.event.DepartureEvent;
import org.powertac.smartparkinglot.exception.InvalidBatteryCapacityDistributionException;
import org.powertac.smartparkinglot.exception.NoMoreArrivalsException;

import org.powertac.smartparkinglot.util.TruncatedNormalDistribution;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WTPMultipleLinearRegression;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.distribution.*;

public class ProviderAgent implements Steppable, EventProvider, CarRateProvider, DistributionProvider, TrackerProvider, BatteryDistributionProvider, WillingnessToPayDistributionProvider, MarketDataProvider {

    public static final int DUMMY = 0;
    public static final int MIN_SOC = 0;
    public static final int MAX_SOC = 1;
    private Simulation sim;

    public static final int VALUE_INDEX = 1;

    private double[][] arrivalRates;

    private double[][] departureRates;

    private double currentArrivalRate;

    private double currentDepartureRate;

    private Exponential exponentialDistribution;

    private EmpiricalWalker batteryDescriptorDistribution;

    private Uniform uniformDistribution;

    private Normal normal;


    private double[] batteryDescriptorProbs;

    private BatteryDescriptor[] batteryDescriptorValues;

    private WTPBayesSpecification wtpBayesSpecification;

    private ThinningAlgorithm arrivalsThinningAlgorithm;
    private static final Logger log = Logger.getLogger(ProviderAgent.class);
    ;


    // private ThinningAlgorithm departuresThinningAlgorithm;

    public ProviderAgent(Simulation simulation, double[][] arrivalRates, double[][] departureRates, double[] batteryDescriptorProbs, BatteryDescriptor[] batteryDescriptorValues, WTPBayesSpecification wtpBayesSpecification)
            throws InvalidBatteryCapacityDistributionException {
        this.sim = simulation;
        this.arrivalRates = arrivalRates;
        this.departureRates = departureRates;
        this.wtpBayesSpecification = wtpBayesSpecification;
        // exponential needs a rate in its constructor so we'll send a dummy one
        // (1) but we will use a concrete one when we draw a value.
        this.exponentialDistribution = new Exponential(1, sim.random);


        // check whether battery capacity values and probabilities match in
        // size:


        // prepare a distribution for battery capacities:
        this.batteryDescriptorProbs = batteryDescriptorProbs;
        // prepare values for battery capacities
        this.batteryDescriptorValues = batteryDescriptorValues;

        arrivalsThinningAlgorithm = new ThinningAlgorithm(arrivalRates[VALUE_INDEX], (double) Simulation.HOURS_PER_YEAR + 1,
                sim.random);


        batteryDescriptorDistribution = new EmpiricalWalker(batteryDescriptorProbs, Empirical.NO_INTERPOLATION, sim.random);

        uniformDistribution = new Uniform(sim.random);
        this.normal = new Normal(DUMMY, DUMMY, sim.random);
    }

    public SummaryStatistics getParkingDurationsStatistics(){
        return sim.trackerAgent.getParkingTimeStatistics();
    }

    public double getCurrentArrivalRate() {
        return currentArrivalRate;
    }

    public double getCurrentDepartureRate() {
        return currentDepartureRate;
    }

    public void onCarExit(Car car) {
        car.setState(VehicleState.LEAVE);
        // return it to the outside world:
        sim.outsideCars.add(car);

    }



    public ValuesHolder getCurrentTimeslotValuesHolder() {

        ValuesHolder timeslotValuesHolder = sim.trackerAgent.getTsValuesHolder().get(getCurrentTimeIndex());
        if (timeslotValuesHolder == null) {
            System.err
                    .println("Missing timeslot values holder for : " + getCurrentTimeIndex() + ", exit");
            System.exit(1);
        }
        return timeslotValuesHolder;
    }

    public ArrivalEvent scheduleInitialArrival() {
        // get a car that will arrive at the parking lot:
        Car car = (Car) sim.outsideCars.remove(sim.random.nextInt(sim.outsideCars.size()));

        if (car.isEV()) {
            // if you are an EV, prepare your battery status and willingness to
            // pay
            ((EV) car).instantiateEVPreferences();
        }

        double nextArrivalTime = 0;
        try {
            nextArrivalTime = arrivalsThinningAlgorithm.generateNextArrivalTime();

        } catch (NoMoreArrivalsException e) {
            System.err.println("Problem with arrival rates!!!");
            e.printStackTrace();
        }
        // }
        double time = nextArrivalTime;
        ArrivalEvent event = new ArrivalEvent(car, sim.smartParkingLot, this, time);
        sim.schedule.scheduleOnce(time, event);
        return event;
    }

    public ArrivalEvent scheduleNewArrival() {

        // get a car that will arrive at the parking lot:
        Car car = (Car) sim.outsideCars.remove(sim.random.nextInt(sim.outsideCars.size()));

        // clear a car, prepare it for SPL:
        car.clearOnExit();
        if (car.isEV()) {
            // if you are an EV, prepare your battery status and willingness to
            // pay
            ((EV) car).instantiateEVPreferences();
        }


        // double arrivalRate = getCurrentArrivalRate();
        double nextArrivalTime = 0;
        try {
            nextArrivalTime = arrivalsThinningAlgorithm.generateNextArrivalTime();

        } catch (NoMoreArrivalsException e) {
            System.err.println("Simulation should finish... Sim time:" + sim.schedule.getTime());
            // setting arrival time outside sim duration, so it will not be processed at all:
            return null;
        }
        ArrivalEvent event;


        event = new ArrivalEvent(car, sim.smartParkingLot, this, nextArrivalTime);
        // regular scheduling:
        // schedule at the start of the first non-zero time slot:
        sim.schedule.scheduleOnce(nextArrivalTime, Simulation.CAR_ARRIVAL_EVENT_ORDER, event);

        return event;

    }

    public DepartureEvent scheduleDepartureEvent(Car car) {
        // TODO: implement better fix:

        double delta = 1;
        delta = exponentialDistribution.nextDouble(getCurrentDepartureRate());
        if(Double.isInfinite(delta)){
            System.err.println("Departure time at TS:"+car.getArrival().getTime()+" is infinite!!");
        }

        DepartureEvent event = new DepartureEvent(car, sim.smartParkingLot, this, sim.schedule.getTime() + delta);
        sim.schedule.scheduleOnceIn(delta, event);
        return event;
    }

    @Override
    public ChargingExecutionEvent scheduleChargingExecutionEvent(EV ev, double delta, double amountKWh) {
        ChargingExecutionEvent chargingExecutionEvent = new ChargingExecutionEvent(ev, sim.smartParkingLot, this,
                sim.schedule.getTime() + delta, amountKWh);

        log.trace("Scheduling charging event: delta time=" + delta + ", amount=" + amountKWh);

        sim.schedule.scheduleOnceIn(delta, chargingExecutionEvent, Simulation.CHARGING_EXECUTION_EVENT);
        return chargingExecutionEvent;
    }

    public void step(SimState state) {

        int timeIndex = getCurrentModuledTimeIndex();

        currentArrivalRate = arrivalRates[VALUE_INDEX][timeIndex];
        currentDepartureRate = departureRates[VALUE_INDEX][timeIndex];

    }

    public Exponential getExponentialDistribution() {
        return exponentialDistribution;
    }

    public EmpiricalWalker getBatteryDescriptorDistribution() {
        return batteryDescriptorDistribution;
    }

    public BatteryDescriptor drawBatteryDescriptor() {
        int batteryDescriptorIndex = batteryDescriptorDistribution.nextInt();
        return batteryDescriptorValues[batteryDescriptorIndex];
    }

    @Override
    public double drawBatteryStatus(double batteryCapacity, double avgSoc, double stDevSoc) {
        return TruncatedNormalDistribution.nextDouble(normal, MIN_SOC, MAX_SOC, avgSoc, stDevSoc) * batteryCapacity;
    }

    public Uniform getUniformDistribution() {
        return uniformDistribution;
    }

    /**
     * test support methods
     *
     * @return
     */
    public double[] getBatteryDescriptorProbs() {
        return batteryDescriptorProbs;
    }

    /**
     * test support methods
     *
     * @return
     */
    public BatteryDescriptor[] getBatteryDescriptorValues() {
        return batteryDescriptorValues;
    }

    public int getCurrentTimeIndex() {
        return (int) Math.floor((getCurrentTime()));
    }

    public int getCurrentModuledTimeIndex() {
        return getCurrentTimeIndex() % Simulation.HOURS_PER_YEAR;
    }

    public double drawWillingnessToPayUtilityFunction(double batteryCapacity, double batteryStatus, double referenceElectricityPrice) {

        double lambda = batteryCapacity / ((batteryCapacity - batteryStatus) * referenceElectricityPrice
                * sim.parameters.getChargerSpeed());

        double willingnessToPay = exponentialDistribution.nextDouble(lambda);
        return willingnessToPay;
    }

    @Override
    public double drawWillingnessToPayMultipleLinearRegression(double batteryCapacity, double batteryStatus, double referenceElectricityPrice, MLRModel mlrModel) {
        double soc = (batteryStatus / batteryCapacity) * 100;
        double chargerSpeed = sim.parameters.getChargerSpeed();
        double deltaSOC = (chargerSpeed / batteryCapacity) * 100;
        double priceForDeltaSOCCents = (chargerSpeed * referenceElectricityPrice) * 100; // multiply by 100 to get cents, referenceElectricityPrice is in "dollars" per kWh.
        double predictInCents = mlrModel.predict(soc, deltaSOC, priceForDeltaSOCCents);

        return predictInCents / 100; // do not return in cents but in "dollars"
    }

    @Override
    public double drawWillingnessToPayBayesianNetwork(double batteryCapacity, double batteryStatus, double referenceElectricityPrice) {
        double soc = batteryStatus / batteryCapacity;
        return wtpBayesSpecification.queryWTPGivenEvidence(wtpBayesSpecification.socToDiscreteSOC(soc), wtpBayesSpecification.priceToDiscretePrice(referenceElectricityPrice), wtpBayesSpecification.speedToSpeedDiscrete(sim.smartParkingLot.getChargerSpeed()));
    }


    public double getArrivalRate(int time) {
        return arrivalRates[VALUE_INDEX][Util.getModulatedTimeIndex(time)];
    }

    public double getCurrentTime() {
        return sim.schedule.getTime();
    }

    public double getCurrentElectricityPricePerKWh() {
        return sim.wholesaleMarket.getCurrentPricePerKWh();
    }

}
