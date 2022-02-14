package org.powertac.smartparkinglot.domain;

public interface QueueStatistics {
	
	/**
	 * Fraction of cars that were rejected.
	 * 
	 * @return
	 */
	public double getRejectionRate();
	
	/**
	 * The fraction of time a parking spot is occupied by a car.
	 * @return
	 */
	public double getOccupancyRate();

}
