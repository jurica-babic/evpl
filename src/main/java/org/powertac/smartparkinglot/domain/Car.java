package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.event.ArrivalEvent;
import org.powertac.smartparkinglot.event.DepartureEvent;

import sim.engine.SimState;

/**
 * 
 * An interface for a car.
 * 
 * @author Jurica Babic
 *
 */
public interface Car {
	public boolean isEV();

	/**
	 * Remove old arrivals and departures
	 */
	public void clearOnExit();

	public void setDeparture(DepartureEvent event);

	public void setArrival(ArrivalEvent event);
	
	public ArrivalEvent getArrival();
	
	public DepartureEvent getDeparture();

	public VehicleState getState();

	public void setState(VehicleState newState);




	

}
