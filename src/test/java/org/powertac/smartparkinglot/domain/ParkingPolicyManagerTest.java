package org.powertac.smartparkinglot.domain;

import static org.junit.Assert.*;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import org.powertac.smartparkinglot.DataGranularity;
import org.powertac.smartparkinglot.Parameters;
import org.powertac.smartparkinglot.Simulation;
import org.powertac.smartparkinglot.ValuesHolder;
import org.powertac.smartparkinglot.event.ArrivalEvent;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;
import org.powertac.smartparkinglot.util.WillingnessToPayType;
import sim.util.Bag;

/**
 * Created by Jurica on 11.7.2017..
 */
public class ParkingPolicyManagerTest {

    public static final double DELTA = 0.01;
    public static final double DELTA_ONE_DECIMAL = 0.1;
    public static final double CHARGING_FEE = 0.8;
    public static final double PARKING_FEE = 5.5;
    public static final int CHARGER_SPEED = 7;
    public static final double PARKING_FEE_HALF_HOUR = 2.7;
    private final double[] batteryCapacityShares = {0.81, 0.19};
    private final BatteryDescriptor[] batteryDescriptorsValues = {new BatteryDescriptor(0.52, 0.094, 24), new BatteryDescriptor(0.575, 0.102, 16)};

    private Simulation state;
    private String area = "test";
    private long duration = Simulation.HOURS_PER_YEAR * 2;

    private Bag evChargingList = new Bag();
    private Bag evNonChargingList = new Bag();
    private Bag nonEVList = new Bag();

    private SmartParkingLot spl;
    private ValuesHolder values;

    @Before
    public void prepare() {

        state = new Simulation(1);
        try {
            Parameters parameters = new Parameters();
            parameters.withUntil(duration).withPopulationSize(5000).withEVShare(0.5).withNonEVParkingSpots(3).withEVParkingSpots(2).withArea(area, true, true).withBatteryDescriptorsShares(batteryCapacityShares).withBatteryDescriptors(batteryDescriptorsValues);
            parameters.withChargerSpeed(CHARGER_SPEED).withChargerCost(333).withChargingFee(CHARGING_FEE).withParkingPrice(PARKING_FEE).withParkingPriceHalfHour(PARKING_FEE_HALF_HOUR).withWillingnessToPayType(WillingnessToPayType.MULTIPLE_LINEAR_REGRESSION).withArea("test2", true, true).withSeed(1);
            parameters.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY);
            state.parameters = parameters;
            state.start();

            do
                if (!state.schedule.step(state))
                    break;
            while (state.schedule.getTime() < DELTA);
   //         state.schedule.clear();


            spl = state.smartParkingLot;

            for(Object obj:state.outsideCars){
                Car car = (Car) obj;
                if(car.isEV()){
                    EV ev = (EV) car;
                    ev.instantiateEVPreferences();
                    if(ev.analyzeChargingFeeOffer(CHARGING_FEE)){
                        evChargingList.add(ev);
                    } else{
                        evNonChargingList.add(ev);
                    }
                } else{
                    nonEVList.add(car);
                }
            }

        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testTraditionalParkingPolicyEVSideBorderline(){
        instantitateOneSpotParking();

        state.smartParkingLot.getParkingPolicyManager().setCurrentParkingPolicy(ParkingPolicyType.PARKING_LOT_DEDICATED_CHARGING);

        MersenneTwisterFast random = state.random;



        // STEP 1: ---------------------------------------------------------------
        // ev that will park and charge:
        Car car1Charge = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        EV car1EV = (EV)car1Charge;

        EVAgent ev1 = new EVAgent(1, EVType.BEV, new BatteryDescriptor(0.5, 0, 15), state.providerAgent, WillingnessToPayType.WEALTHY_MOFO_TESTER, null,state.providerAgent, state.providerAgent, MLRModel.USA_MODEL);
        ev1.instantiateEVPreferences();
        ev1.setBatteryStatus(0);

        double car1BS = car1EV.getBatteryStatus();
        double car1BC = car1EV.getBatteryCapacity();

        double CURR_TIME = state.schedule.getTime();
        ArrivalEvent carChargeEvent = new ArrivalEvent(car1Charge,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carChargeEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT_WITH_CHARGING, car1Charge.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(1, values.getArrivalsCount());

        // STEP 2: ---------------------------------------------------------------
        // ev will not want to charge so it will not park in a non-EV spot because the parking lot is full:
        Car car2evPark = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent = new ArrivalEvent(car2evPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent);



        // test:
        assertEquals(VehicleState.LEAVE, car2evPark.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(2, values.getArrivalsCount());
        assertEquals(1, values.getRejectsCount());

        // TODO: odigrati evente do kraja (zanemariti spawnanje novih arrivala: hack - nek spawna s full malim rateom, a ti progressaj simulaciju za 24h, vrati agente koji se svakih 1h pozivaju..) provjeriti obracune

        emptyParkingLot();

        // now the parking lot should be empty, sanity check:
        assertEquals(0, spl.getNonEvCurrentlyParked());
        assertEquals(0, spl.getEvCurrentlyParked());



    }

    private void instantitateOneSpotParking() {
        // use different parking lot:
        state = new Simulation(1);
        try {
            Parameters parameters = new Parameters();
            parameters.withUntil(duration).withPopulationSize(5000).withEVShare(0.5).withNonEVParkingSpots(0).withEVParkingSpots(1).withArea(area, true, true).withBatteryDescriptorsShares(batteryCapacityShares).withBatteryDescriptors(batteryDescriptorsValues);
            parameters.withChargerSpeed(CHARGER_SPEED).withChargerCost(333).withChargingFee(CHARGING_FEE).withParkingPrice(PARKING_FEE).withParkingPriceHalfHour(PARKING_FEE_HALF_HOUR).withWillingnessToPayType(WillingnessToPayType.MULTIPLE_LINEAR_REGRESSION).withArea("test2", true, true).withSeed(1);
            parameters.withMlrModel(MLRModel.USA_MODEL);
            parameters.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY);
            state.parameters = parameters;
            state.start();

            do
                if (!state.schedule.step(state))
                    break;
            while (state.schedule.getTime() < DELTA);
            //         state.schedule.clear();


            spl = state.smartParkingLot;

            for(Object obj:state.outsideCars){
                Car car = (Car) obj;
                if(car.isEV()){
                    EV ev = (EV) car;
                    ev.instantiateEVPreferences();
                    if(ev.analyzeChargingFeeOffer(CHARGING_FEE)){
                        evChargingList.add(ev);
                    } else{
                        evNonChargingList.add(ev);
                    }
                } else{
                    nonEVList.add(car);
                }
            }

        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTraditionalParkingPolicyEVSide(){
        state.smartParkingLot.getParkingPolicyManager().setCurrentParkingPolicy(ParkingPolicyType.PARKING_LOT_DEDICATED_CHARGING);

        MersenneTwisterFast random = state.random;



        // STEP 1: ---------------------------------------------------------------
        // ev that will park and charge:
        Car car1Charge = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        EV car1EV = (EV)car1Charge;

        EVAgent ev1 = new EVAgent(1, EVType.BEV, new BatteryDescriptor(0.5, 0, 15), state.providerAgent, WillingnessToPayType.WEALTHY_MOFO_TESTER, null,state.providerAgent, state.providerAgent, MLRModel.USA_MODEL);
        ev1.instantiateEVPreferences();
        ev1.setBatteryStatus(0);

        double car1BS = car1EV.getBatteryStatus();
        double car1BC = car1EV.getBatteryCapacity();

        double CURR_TIME = state.schedule.getTime();
        ArrivalEvent carChargeEvent = new ArrivalEvent(car1Charge,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carChargeEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT_WITH_CHARGING, car1Charge.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(1, values.getArrivalsCount());

        // STEP 2: ---------------------------------------------------------------
        // ev will not want to charge so it will park in a non-EV spot:
        Car car2evPark = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent = new ArrivalEvent(car2evPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent);



        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, car2evPark.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(1, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(1, values.getAcceptsOnNonEvSpotCount());
        assertEquals(2, values.getArrivalsCount());

        // STEP 3: ---------------------------------------------------------------
        // ev that will park and charge:
        Car car3Charge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carCharge2Event = new ArrivalEvent(car3Charge2,spl, state.providerAgent, CURR_TIME+0.5);
        EV car3EV = (EV)car3Charge2;
        double car3EVBS = car3EV.getBatteryStatus();
        double car3EVBC = car3EV.getBatteryCapacity();

        progressDiscrete(CURR_TIME+0.5, carCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT_WITH_CHARGING, car3Charge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(1, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(1, values.getAcceptsOnNonEvSpotCount());
        assertEquals(3, values.getArrivalsCount());

        // STEP 4: ---------------------------------------------------------------
        // car will want to charge but it will be forced to switch to non-EV spot because there are not free EV spots out there:
        Car car4evCharge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evCharge2Event = new ArrivalEvent(car4evCharge2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, car4evCharge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(2, values.getAcceptsOnNonEvSpotCount());
        assertEquals(4, values.getArrivalsCount());

        // STEP 5: ---------------------------------------------------------------
        // non-ev will park on a non-ev spot:
        Car car5nonEV = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEVEvent = new ArrivalEvent(car5nonEV,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEVEvent);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, car5nonEV.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(5, values.getArrivalsCount());

        // STEP 6: ---------------------------------------------------------------
        // non-ev will leave because the parking lot is full:
        Car car6nonEV2 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEV2Event = new ArrivalEvent(car6nonEV2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEV2Event);

        // test:
        assertEquals(VehicleState.LEAVE, car6nonEV2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(6, values.getArrivalsCount());

        // STEP 7: ---------------------------------------------------------------
        // ev will leave because the parking lot is full:
        Car ev3 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent ev3Event = new ArrivalEvent(ev3,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, ev3Event);

        // test:
        assertEquals(VehicleState.LEAVE, ev3.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(7, values.getArrivalsCount());
        assertEquals(3, spl.getNonEvCurrentlyParked());
        assertEquals(2, spl.getEvCurrentlyParked());

        // TODO: odigrati evente do kraja (zanemariti spawnanje novih arrivala: hack - nek spawna s full malim rateom, a ti progressaj simulaciju za 24h, vrati agente koji se svakih 1h pozivaju..) provjeriti obracune

        emptyParkingLot();

        // now the parking lot should be empty, sanity check:
        assertEquals(0, spl.getNonEvCurrentlyParked());
        assertEquals(0, spl.getEvCurrentlyParked());



    }

    private void emptyParkingLot() {
        do
            if (!state.schedule.step(state))
                break;
        while (state.schedule.getTime() < 24);
    }

    @Test
    public void testEVExclusiveSpotsParkingPolicy(){
        state.smartParkingLot.getParkingPolicyManager().setCurrentParkingPolicy(ParkingPolicyType.EV_EXCLUSIVE_SPOTS);


        MersenneTwisterFast random = state.random;

        // STEP 1: ---------------------------------------------------------------
        // ev that will park and charge:
        Car carCharge = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        double CURR_TIME = state.schedule.getTime();
        ArrivalEvent carChargeEvent = new ArrivalEvent(carCharge,spl, state.providerAgent, CURR_TIME+0.5);

        progressDiscrete(CURR_TIME+0.5, carChargeEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT_WITH_CHARGING, carCharge.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(1, values.getArrivalsCount());

        // STEP 2: ---------------------------------------------------------------
        // ev will not want to charge but it will still park on EV spot:
        Car evPark = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent = new ArrivalEvent(evPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT, evPark.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(2, values.getArrivalsCount());

        // STEP 3: ---------------------------------------------------------------
        // ev that will park and charge will be forced to park on non-EV spot because there are no longer free EV spaces:
        Car carCharge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carCharge2Event = new ArrivalEvent(carCharge2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, carCharge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(1, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(1, values.getAcceptsOnNonEvSpotCount());
        assertEquals(3, values.getArrivalsCount());

        // STEP 4: ---------------------------------------------------------------
        // car will want to charge but it will be forced to switch to non-EV spot because there are not free EV spots out there:
        Car evCharge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evCharge2Event = new ArrivalEvent(evCharge2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, evCharge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(2, values.getAcceptsOnNonEvSpotCount());
        assertEquals(4, values.getArrivalsCount());

        // STEP 5: ---------------------------------------------------------------
        // non-ev will park on a non-ev spot:
        Car nonEV = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEVEvent = new ArrivalEvent(nonEV,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEVEvent);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, nonEV.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(5, values.getArrivalsCount());

        // STEP 6: ---------------------------------------------------------------
        // non-ev will leave because the parking lot is full:
        Car nonEV2 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEV2Event = new ArrivalEvent(nonEV2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEV2Event);

        // test:
        assertEquals(VehicleState.LEAVE, nonEV2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(6, values.getArrivalsCount());

        // STEP 7: ---------------------------------------------------------------
        // ev will leave because the parking lot is full:
        Car ev3 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent ev3Event = new ArrivalEvent(ev3,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, ev3Event);

        // test:
        assertEquals(VehicleState.LEAVE, ev3.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(7, values.getArrivalsCount());


        emptyParkingLot();

        // now the parking lot should be empty, sanity check:
        assertEquals(0, spl.getNonEvCurrentlyParked());
        assertEquals(0, spl.getEvCurrentlyParked());



    }

    @Test
    public void testFreeForAllParkingPolicyLeftSide(){
        state.smartParkingLot.getParkingPolicyManager().setCurrentParkingPolicy(ParkingPolicyType.FREE_FOR_ALL);


        MersenneTwisterFast random = state.random;

        // STEP 1: ---------------------------------------------------------------
        // ev that will park and charge:
        Car carCharge = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        double CURR_TIME = state.schedule.getTime();
        ArrivalEvent carChargeEvent = new ArrivalEvent(carCharge,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carChargeEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT_WITH_CHARGING, carCharge.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(1, values.getArrivalsCount());

        // STEP 2: ---------------------------------------------------------------
        // ev will not want to charge but it will still park on EV spot:
        Car evPark = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent = new ArrivalEvent(evPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT, evPark.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(0, values.getAcceptsOnNonEvSpotCount());
        assertEquals(2, values.getArrivalsCount());

        // STEP 3: ---------------------------------------------------------------
        // ev that will park and charge will be forced to park on non-EV spot because there are no longer free EV spaces:
        Car carCharge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carCharge2Event = new ArrivalEvent(carCharge2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, carCharge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(1, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(1, values.getAcceptsOnNonEvSpotCount());
        assertEquals(3, values.getArrivalsCount());

        // STEP 4: ---------------------------------------------------------------
        // car will want to charge but it will be forced to switch to non-EV spot because there are not free EV spots out there:
        Car evCharge2 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evCharge2Event = new ArrivalEvent(evCharge2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evCharge2Event);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, evCharge2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(2, values.getAcceptsOnNonEvSpotCount());
        assertEquals(4, values.getArrivalsCount());

        // STEP 5: ---------------------------------------------------------------
        // non-ev will park on a non-ev spot:
        Car nonEV = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEVEvent = new ArrivalEvent(nonEV,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEVEvent);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, nonEV.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(5, values.getArrivalsCount());

        // STEP 6: ---------------------------------------------------------------
        // non-ev will leave because the parking lot is full:
        Car nonEV2 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEV2Event = new ArrivalEvent(nonEV2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEV2Event);

        // test:
        assertEquals(VehicleState.LEAVE, nonEV2.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(6, values.getArrivalsCount());

        // STEP 7: ---------------------------------------------------------------
        // ev will leave because the parking lot is full:
        Car ev3 = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent ev3Event = new ArrivalEvent(ev3,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, ev3Event);

        // test:
        assertEquals(VehicleState.LEAVE, ev3.getState());
        assertEquals(2, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(2, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(7, values.getArrivalsCount());

        emptyParkingLot();

        // now the parking lot should be empty, sanity check:
        assertEquals(0, spl.getNonEvCurrentlyParked());
        assertEquals(0, spl.getEvCurrentlyParked());

     }

    @Test
    public void testFreeForAllParkingPolicyRightSide(){

        //TEST WHAT HAPPENS WHEN THERE ARE A LOT OF NON-EVs: they will start to park on EV spots so EV cars will struggle to find a space!

        state.smartParkingLot.getParkingPolicyManager().setCurrentParkingPolicy(ParkingPolicyType.FREE_FOR_ALL);


        MersenneTwisterFast random = state.random;

        // STEP 1: ---------------------------------------------------------------
        // non-ev
        Car carPark = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        double CURR_TIME = state.schedule.getTime();
        ArrivalEvent carParkEvent = new ArrivalEvent(carPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carParkEvent);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, carPark.getState());
        assertEquals(0, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(0, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(1, values.getAcceptsOnNonEvSpotCount());
        assertEquals(1, values.getArrivalsCount());

        // STEP 2: ---------------------------------------------------------------
        // non-ev
        Car carPark2 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carParkEvent2 = new ArrivalEvent(carPark2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carParkEvent2);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, carPark2.getState());
        assertEquals(0, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(0, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(2, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(2, values.getAcceptsOnNonEvSpotCount());
        assertEquals(2, values.getArrivalsCount());

        // STEP 3: ---------------------------------------------------------------
        // non-ev
        Car carPark3 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carParkEvent3 = new ArrivalEvent(carPark3,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carParkEvent3);

        // test:
        assertEquals(VehicleState.PARKED_NONEV_SPOT, carPark3.getState());
        assertEquals(0, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(0, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(0, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(3, values.getArrivalsCount());

        // STEP 4: ---------------------------------------------------------------
        // non-ev on ev
        Car carPark4 = (Car) nonEVList.remove(random.nextInt(nonEVList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carParkEvent4 = new ArrivalEvent(carPark4,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carParkEvent4);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT, carPark4.getState());
        assertEquals(0, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(1, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(4, values.getArrivalsCount());

        // STEP 5: ---------------------------------------------------------------
        // ev without charging parks on ev spot
        Car evPark = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent = new ArrivalEvent(evPark,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent);

        // test:
        assertEquals(VehicleState.PARKED_EV_SPOT, evPark.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(5, values.getArrivalsCount());

        // STEP 6: ---------------------------------------------------------------
        // ev with charging will have to leave
        Car carCharge = (Car) evChargingList.remove(random.nextInt(evChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent carChargeEvent = new ArrivalEvent(carCharge,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, carChargeEvent);

        // test:
        assertEquals(VehicleState.LEAVE, carCharge.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(6, values.getArrivalsCount());

        // STEP 7: ---------------------------------------------------------------
        // ev without charging will have to leave
        Car evPark2 = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent evParkEvent2 = new ArrivalEvent(evPark2,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, evParkEvent2);

        // test:
        assertEquals(VehicleState.LEAVE, evPark2.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(7, values.getArrivalsCount());

        // STEP 8: ---------------------------------------------------------------
        // non-EV charging will have to leave
        Car nonEV = (Car) evNonChargingList.remove(random.nextInt(evNonChargingList.size()));
        CURR_TIME = state.schedule.getTime();
        ArrivalEvent nonEVevent = new ArrivalEvent(nonEV,spl, state.providerAgent, CURR_TIME);

        progressDiscrete(CURR_TIME, nonEVevent);

        // test:
        assertEquals(VehicleState.LEAVE, nonEV.getState());
        assertEquals(1, values.getAcceptsEvParkedOnEvSpotCount());
        assertEquals(2, values.getAcceptsOnEvSpotCount());
        assertEquals(0, values.getAcceptsEvParkedOnNonEvSpotCount());
        assertEquals(1, values.getAcceptsNonEVParkedOnEVSpotCount());
        assertEquals(3, values.getAcceptsNonEVParkedOnNonEVSpotCount());
        assertEquals(3, values.getAcceptsOnNonEvSpotCount());
        assertEquals(8, values.getArrivalsCount());


    }


    private void progressDiscrete(double time, ArrivalEvent event) {
        state.schedule.scheduleOnce(time, event);
        values = state.providerAgent.getCurrentTimeslotValuesHolder();
        state.schedule.step(state);
    }
}