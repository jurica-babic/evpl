package org.powertac.smartparkinglot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.powertac.smartparkinglot.exception.NoMoreArrivalsException;

import ec.util.MersenneTwisterFast;
import sim.util.distribution.Exponential;
import sim.util.distribution.Uniform;

/**
 * 
 * Implementation of the Thinning algorithm, used for a nonstationary Poisson
 * Process (time-varying arrival rates).
 * 
 * @author Jurica Babic
 *
 */
public class ThinningAlgorithm {

	private double lastArrivalTime = 0;
	private int n = 0;
	private double finishTime;
	double[] arrivalRates;
	private Exponential exponential;
	private double lambdaMax;
	private MersenneTwisterFast random;
	private int[] counter;

	public ThinningAlgorithm(double arrivalRates[], double finishTime,
			MersenneTwisterFast random) {
		this.arrivalRates = arrivalRates;
		this.finishTime = finishTime;
		this.exponential = new Exponential(1, random);
		this.random = random;
		this.counter = new int[arrivalRates.length];

		initializeMaxLambda();
	}

	private void initializeMaxLambda() {
		for (int i = 0; i < arrivalRates.length; i++) {
			double value = arrivalRates[i];
			if (value > lambdaMax) {
				lambdaMax = value;
			}

//			// data inputation: iz zero, set to 1:
//			if (arrivalRates[i] == 0) {
//				arrivalRates[i] = 1;
//			}
		}
	}

	public double generateNextArrivalTime() throws NoMoreArrivalsException {
		double u;
		int timeIndex;
		while (lastArrivalTime < finishTime) {
			double s = lastArrivalTime;
			do {
				s = s + exponential.nextDouble(lambdaMax);
				u = random.nextDouble(true, true) * lambdaMax;
				timeIndex = (int) (Math.floor(s));
				timeIndex = timeIndex % arrivalRates.length;
			} while (u > arrivalRates[timeIndex]);
			lastArrivalTime = s;
			n++;
			counter[timeIndex]=counter[timeIndex]+1;

			return lastArrivalTime;

		}

		throw new NoMoreArrivalsException("Last Arrival Time: "+lastArrivalTime + "Finish Time: "+finishTime);
	}

	/**
	 * Grande doprinos doktorskog istrazivanja ili zesci flop?
	 * @param newLastArrivalTime
	 */
	public void overrideLastArrivalTime(double newLastArrivalTime){
		this.lastArrivalTime = newLastArrivalTime;
	}

	public int[] getCounter() {
		return counter;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public int getN() {
		return n;
	}

	public double getLambdaMax() {
		return lambdaMax;
	}

	public double getLastArrivalTime() {
		return lastArrivalTime;
	}

	public double[] getArrivalRates() {
		return arrivalRates;
	}

	@Override
	public String toString() {
		return "ThinningAlgorithm [lastArrivalTime=" + lastArrivalTime + ", n=" + n + ", finishTime=" + finishTime
				+ ", arrivalRates=" + Arrays.toString(arrivalRates) + ", exponential=" + exponential + ", lambdaMax="
				+ lambdaMax + ", random=" + random + "]";
	}
	
	

}
