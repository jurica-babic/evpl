package org.powertac.smartparkinglot.domain;

import sim.engine.SimState;

/**
 * 
 * A concrete internal combustion vehicle.
 * 
 * @author Jurica Babic
 *
 */
public class ICVAgent extends AbstractCar implements ICV {
	public ICVAgent(long id) {
		super(id);
	}

	public boolean isEV() {
		return false;
	}

	public void step(SimState state) {

	}

}
