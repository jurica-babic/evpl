package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.event.ArrivalEvent;
import org.powertac.smartparkinglot.event.DepartureEvent;

import sim.engine.Steppable;

/**
 * 
 * An abstract class for cars. If needed, it will contain some general
 * properties and behavior.
 * 
 * @author Jurica Babic
 *
 */
public abstract class AbstractCar implements Car, Steppable {
	protected ArrivalEvent arrival;
	protected DepartureEvent departure;

	protected VehicleState state = VehicleState.LEAVE;

	private final long id;

	public AbstractCar(long id) {
		this.id = id;
	}

	@Override
	public void setState(VehicleState state) {
		this.state = state;
	}

	@Override
	public VehicleState getState() {
		return state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCar other = (AbstractCar) obj;
		if (id != other.id)
			return false;
		return true;
	}



	public void clearOnExit() {
		arrival = null;
		departure = null;
		state = VehicleState.LEAVE;
	}

	public void setArrival(ArrivalEvent arrival) {
		this.arrival = arrival;
	}

	public void setDeparture(DepartureEvent departure) {
		this.departure = departure;
	}

	@Override
	public ArrivalEvent getArrival() {
		// TODO Auto-generated method stub
		return arrival;
	}

	@Override
	public DepartureEvent getDeparture() {
		// TODO Auto-generated method stub
		return departure;
	}
}
