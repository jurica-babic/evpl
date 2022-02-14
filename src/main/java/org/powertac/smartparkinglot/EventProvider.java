package org.powertac.smartparkinglot;

import org.powertac.smartparkinglot.domain.Car;
import org.powertac.smartparkinglot.domain.EV;
import org.powertac.smartparkinglot.domain.TrackerProvider;
import org.powertac.smartparkinglot.event.ArrivalEvent;
import org.powertac.smartparkinglot.event.ChargingExecutionEvent;
import org.powertac.smartparkinglot.event.DepartureEvent;

/**
 * 
 * Provides some methods for event generation.
 * 
 * @author Jurica Babic
 *
 */
public interface EventProvider extends TrackerProvider{
	
	/**
	 * Used to spawn a new car arrival.
	 */
	public ArrivalEvent scheduleNewArrival();
	
	/**
	 * Used to kick-off the simulation.
	 */
	public ArrivalEvent scheduleInitialArrival();
	
	/**
	 * Spawn a departure for a car.
	 * @param car
	 */
	public DepartureEvent scheduleDepartureEvent(Car car);
	
	/**
	 * A car should be "returned" to the outside world
	 * @param car
	 */
	public void onCarExit(Car car);
	
	public ChargingExecutionEvent scheduleChargingExecutionEvent(EV ev, double time, double amount);
	
	
	
	
	

}
