package org.powertac.smartparkinglot.domain;

import sim.engine.Steppable;

/**
 * 
 * Electricity market price provider.
 * 
 * @author Jurica Babic
 *
 */
public interface WholesaleMarket extends Steppable {
	
	public double getCurrentPricePerKWh();
	
	

}
