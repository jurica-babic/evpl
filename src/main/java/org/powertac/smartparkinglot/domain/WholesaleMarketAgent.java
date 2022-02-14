package org.powertac.smartparkinglot.domain;

import org.apache.log4j.Logger;
import org.powertac.smartparkinglot.DataGranularity;
import org.powertac.smartparkinglot.Simulation;

import sim.engine.SimState;

/**
 * 
 * A concrete wholesale market.
 * 
 * @author Jurica Babic
 *
 */
public class WholesaleMarketAgent implements WholesaleMarket {

	private double currentPricePerMWh;

	private double[][] perHourPriceForYear;

	private static Logger log = Logger.getLogger(WholesaleMarketAgent.class);

	public WholesaleMarketAgent(double[][] marketPrices) {
		perHourPriceForYear = marketPrices;

	}

	public void step(SimState state) {

		int timeIndex = (int) Math.floor((state.schedule.getTime()));
		timeIndex = timeIndex % Simulation.HOURS_PER_YEAR;
		currentPricePerMWh = perHourPriceForYear[1][timeIndex];
		//log.debug("Wholesale market at "+state.schedule.getTime()+", price per MWh: "+currentPricePerMWh);
	}



	public double getCurrentPricePerKWh() {
		return currentPricePerMWh / 1000;
	}

}
