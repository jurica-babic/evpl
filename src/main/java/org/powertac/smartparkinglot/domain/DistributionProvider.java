package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.util.WillingnessToPayType;
import sim.util.distribution.EmpiricalWalker;
import sim.util.distribution.Exponential;
import sim.util.distribution.Uniform;

/**
 * 
 * The implementation of this interface will provide distributions needed for a
 * simulation.
 * 
 * @author Jurica Babic
 *
 */
public interface DistributionProvider {

	public Exponential getExponentialDistribution();

	public EmpiricalWalker getBatteryDescriptorDistribution();

	public BatteryDescriptor drawBatteryDescriptor();
	



	
	public Uniform getUniformDistribution();


}
