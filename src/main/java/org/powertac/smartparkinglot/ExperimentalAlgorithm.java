package org.powertac.smartparkinglot;

import ec.util.MersenneTwisterFast;
import org.powertac.smartparkinglot.exception.NoMoreArrivalsException;
import sim.util.distribution.Exponential;

import java.util.Arrays;

/**
 * 
 * An algorithm which uses two thinning algorithms. The novelty factor is in the way how departure process is treated. Its time should be in sync with arrivals.
 * We will see whether this makes sense or is it a total flop.
 * @author Jurica Babic
 *
 */
public class ExperimentalAlgorithm {

	private ThinningAlgorithm arrivalProcess;
	private ThinningAlgorithm departureProcess;
	private double clock;

	public ExperimentalAlgorithm(double arrivalRates[], double departureRates[], double finishTime,
								 MersenneTwisterFast random) {
		arrivalProcess = new ThinningAlgorithm(arrivalRates, finishTime, random);
		departureProcess = new ThinningAlgorithm(departureRates, finishTime, random);

	}

	public double generateNextArrivalTime() throws NoMoreArrivalsException {
		double nextArrivalTime = arrivalProcess.generateNextArrivalTime();
		this.clock = nextArrivalTime;
		return nextArrivalTime;
	}

	public double generateNextDepartureTimeDelta(double arrivalTime) throws NoMoreArrivalsException{
		// adjust departureProcess clock:
		departureProcess.overrideLastArrivalTime(arrivalTime);
		double nextDepartureTime =  departureProcess.generateNextArrivalTime();
		double delta = nextDepartureTime - arrivalTime;
		return delta;
	}

	public double getClock() {
		return clock;
	}
}
