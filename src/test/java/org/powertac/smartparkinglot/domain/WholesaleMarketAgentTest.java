package org.powertac.smartparkinglot.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import org.powertac.smartparkinglot.DataGranularity;
import org.powertac.smartparkinglot.Simulation;
import org.powertac.smartparkinglot.SimulationTest;
import org.powertac.smartparkinglot.Util;

import sim.engine.SimState;

public class WholesaleMarketAgentTest {

	@Test
	public void test() {

//		double duration = Simulation.HOURS_PER_YEAR * 1;
//		String[] args = { "-until", "" + duration, "-populationSize", "5000", "-arrivalRate", "140", "-departureRate",
//				"15", "-evShare", "0.0", "-nonEvParkingSpots", "10", "-evParkingSpots", "0" };
//
//
//		Simulation state = new Simulation(1);
//		state.args = args;
//
//		state.start();
//		do {
//			if (!state.schedule.step(state))
//				break;
//			if (state.schedule.getTime() >= 0 && state.schedule.getTime() < 1)
//				assertEquals(17.6 / 1000, state.wholesaleMarket.getCurrentPricePerKWh(), SimulationTest.DELTA);
//			else if (state.schedule.getTime() >= 8759 && state.schedule.getTime() < 8759.9)
//				assertEquals(25.44 / 1000, state.wholesaleMarket.getCurrentPricePerKWh(), SimulationTest.DELTA);
//			else if (state.schedule.getTime() >= 8761 && state.schedule.getTime() < 8761.9)
//				assertEquals(17.6 / 1000, state.wholesaleMarket.getCurrentPricePerKWh(), SimulationTest.DELTA);
//			else if (state.schedule.getTime() >= 8762 && state.schedule.getTime() < 8762.9)
//				assertEquals(17.6 / 1000, state.wholesaleMarket.getCurrentPricePerKWh(), SimulationTest.DELTA);
//		} while (state.schedule.getTime() < duration);
//		state.finish();
//
//		WholesaleMarket wholesaleMarket = state.wholesaleMarket;
//		assertEquals(17.6 / 1000, wholesaleMarket.getCurrentPricePerKWh(), SimulationTest.DELTA);

	}

}
