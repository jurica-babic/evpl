package org.powertac.smartparkinglot.domain;

/**
 * 
 * This should be implemented by a class that can adjust arrival and departure rates depending on a current time
 * 
 * @author Jurica Babic
 *
 */
public interface CarRateProvider {
	
	public double getCurrentArrivalRate();
	
	
	
	public double getCurrentDepartureRate();
	
	public int getCurrentTimeIndex();
	public int getCurrentModuledTimeIndex();
	
	public double getArrivalRate(int time);
	
	public double getCurrentTime();

}
