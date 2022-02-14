package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.SimulationTrackerAgent;

import sim.engine.Steppable;

/**
 * Interface for a smart parking lot agent.
 * 
 * @author Jurica Babic
 *
 */
public interface SmartParkingLot extends Steppable {

	public void rejectCar(Car car);

	public void acceptCar(Car car, boolean parkInEvSpot);

	public void departureCar(Car car);

	public boolean isSpotAvailable();
	
	public boolean isEvParkingSpotAvailable();
	
	public boolean isIcvParkingSpotAvailable();
	
	public void report();
	
	public long getParkingSpotSize();
	
	
	public double getChargerSpeed();
	
	public double getChargingFee();
	
	public double getParkingFee();

	public double getParkingFeeHalfHour();

	public ParkingPolicyManager getParkingPolicyManager();

	public int getEvCurrentlyParked();
	public int getNonEvCurrentlyParked();
	

}
