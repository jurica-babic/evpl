package org.powertac.smartparkinglot.event;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powertac.smartparkinglot.Parameters;
import org.powertac.smartparkinglot.Simulation;
import org.powertac.smartparkinglot.domain.*;
import org.powertac.smartparkinglot.util.WillingnessToPayType;
import sim.engine.SimState;
import sim.engine.Steppable;

import static org.junit.Assert.*;

/**
 * Created by Jurica on 12.7.2017..
 */
public class ArrivalEventTest {


    private static final double DELTA = 0.001;
    public static final double INITIAL_PROGRESS = 0.5;
    public static final double START_TIME = 1;
    public static final double DELTA_TIME = 0.25;
    private Simulation state;

    @Before
    public void prepare(){
        state = new Simulation(1);


    }

    private void initialSkip(double untilDelta) {
        double start = state.schedule.getTime();
        do
            if (!state.schedule.step(state))
                break;
        while (state.schedule.getTime() < start + untilDelta);
    }

    @Test
    public void testGenerateChargingPlanFastGuy() throws Exception {
        Parameters parameters = Parameters.createParametersWithSensibleDefaults("testFastDepartures",0.45,0.5,5,5);
        parameters.withChargerSpeed(7);
        parameters.withParkingPriceHalfHour(5.5);
        parameters.withParkingPriceHalfHour(2.7);
        state.parameters = parameters;
        state.start();
        initialSkip(INITIAL_PROGRESS);
        assertEquals(0.01, state.schedule.getTime(), START_TIME);

        EVAgent ev1 = new EVAgent(1, EVType.BEV, new BatteryDescriptor(0.5, 0, 7), state.providerAgent, WillingnessToPayType.WEALTHY_MOFO_TESTER, null,state.providerAgent, state.providerAgent, MLRModel.USA_MODEL);
        ev1.instantiateEVPreferences();
        ev1.setBatteryStatus(0);
        ArrivalEvent ev1Event = new ArrivalEvent(ev1, state.smartParkingLot, state.providerAgent, state.schedule.getTime());
        state.schedule.scheduleOnceIn(0, ev1Event);

        initialSkip(2);

        assertEquals(0.45,state.providerAgent.getCurrentTimeslotValuesHolder().getEvplElectricityMoneyFromEVSpot(), DELTA);

        // TEST CALCULATIONS:

        double parkingDurHrs = state.providerAgent.getCurrentTimeslotValuesHolder().getEvplTotalParkingDurationEVSpotHrs();
        assertTrue(parkingDurHrs < 1 && parkingDurHrs > 0);
        assertEquals(2.7, state.providerAgent.getCurrentTimeslotValuesHolder().getEvplParkingMoneyAll(), DELTA);
        assertEquals(2.7, state.providerAgent.getCurrentTimeslotValuesHolder().getEvplParkingMoneyEVSpot(), DELTA);
        assertEquals(0, state.providerAgent.getCurrentTimeslotValuesHolder().getEvplParkingMoneyNonEVSpot(), DELTA);
        double lessThanWhatIsPossibleinOneHourKwh = state.providerAgent.getCurrentTimeslotValuesHolder().getEvplTotalElectricityProcuredkWh();
        assertTrue(lessThanWhatIsPossibleinOneHourKwh < parameters.getChargerSpeed() && lessThanWhatIsPossibleinOneHourKwh > 0);

        double lessCostThanWhatIsPossibleInOneHr = state.providerAgent.getCurrentTimeslotValuesHolder().getEvplElectricityCostFromMarket();
        assertTrue(lessCostThanWhatIsPossibleInOneHr < parameters.getChargerSpeed()*0.02443 && lessCostThanWhatIsPossibleInOneHr > 0);
    }


    @Test
    public void testGenerateChargingPlanSlowGuy() throws Exception {
        Parameters parameters = Parameters.createParametersWithSensibleDefaults("testSlowDepartures",0.45,0.5,5,5);
        parameters.withChargerSpeed(7);
        state.parameters = parameters;
        state.start();
        initialSkip(INITIAL_PROGRESS);
        assertEquals(0.01, state.schedule.getTime(), START_TIME);

        EVAgent ev1 = new EVAgent(1, EVType.BEV, new BatteryDescriptor(0.5, 0, 15), state.providerAgent, WillingnessToPayType.WEALTHY_MOFO_TESTER, null,state.providerAgent, state.providerAgent, MLRModel.USA_MODEL);
        ev1.instantiateEVPreferences();
        ev1.setBatteryStatus(0);
        ArrivalEvent ev1Event = new ArrivalEvent(ev1, state.smartParkingLot, state.providerAgent, state.schedule.getTime()+ DELTA_TIME);
        state.schedule.scheduleOnceIn(DELTA_TIME, ev1Event);

        initialSkip(4);

        assertEquals(15,state.providerAgent.getCurrentTimeslotValuesHolder().getEvplTotalElectricityProcuredkWh(), DELTA);
        assertEquals((0.75*7*78.43 + 7*73.44 + 0.25*7*69.45+ 1*69.45)/1000,state.providerAgent.getCurrentTimeslotValuesHolder().getEvplElectricityCostFromMarket(), DELTA);
       // scumbag stayed parked for quite some time so no money from him!
        assertEquals(0,state.providerAgent.getCurrentTimeslotValuesHolder().getEvplElectricityMoneyFromEVSpot(), DELTA);

        // TEST CALCULATIONS:

        // TODO: VERIFY ELECTRICITY CALCULATIONS:
        assertEquals(15, state.providerAgent.getCurrentTimeslotValuesHolder().getEvplTotalElectricityProcuredkWh(), DELTA);


    }
}