package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.EventProvider;
import org.powertac.smartparkinglot.SimulationTrackerAgent;

import sim.engine.Steppable;

public abstract class AbstractEvent implements Steppable {

	protected Car car;
	protected SmartParkingLot spl;
	protected ProviderAgent providerAgent;

	protected double time;

	public AbstractEvent(Car car, SmartParkingLot spl, ProviderAgent providerAgent,
			double time) {
		this.car = car;
		this.spl = spl;
		this.providerAgent = providerAgent;
		this.time = time;
	}

	public double getTime() {
		return time;
	}

}
