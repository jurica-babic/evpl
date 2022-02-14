package org.powertac.smartparkinglot;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;

/**
 * 
 * Provides a repository of values for each time slot.
 * 
 * @author Jurica Babic
 * 
 *
 */
public class SimulationTrackerAgent implements Steppable {



	private static final int EXTRA_TS = 1;

	/**
	 * K: time slot V: values for that time slot
	 */
	private HashMap<Integer, ValuesHolder> tsValuesHolderMap;


	/*
	 * (non-Javadoc)
	 * 
	 * It should be executed at the end of each time slot.
	 * 
	 * @see sim.engine.Steppable#step(sim.engine.SimState)
	 */
	public void step(SimState state) {

	}

	public SimulationTrackerAgent(int simulationDuration, boolean isLongitudalTracking, long seed) {

		tsValuesHolderMap = new HashMap<>();

		ValuesHolder valueHolderAggregate = new ValuesHolder();

		for (int i = 0; i < simulationDuration+EXTRA_TS; i++) {
			ValuesHolder valueHolder = valueHolderAggregate;
			if(isLongitudalTracking) {
			 valueHolder = new ValuesHolder();
			}

			tsValuesHolderMap.put(i, valueHolder);
		}
	}

	private SummaryStatistics parkingTimeStatistics = new SummaryStatistics();

	public SummaryStatistics getParkingTimeStatistics() {
		return parkingTimeStatistics;
	}

	public HashMap<Integer, ValuesHolder> getTsValuesHolder() {
		return tsValuesHolderMap;
	}

	public List<ValuesHolder> getOrderedTimeslotValues() {
		List<ValuesHolder> valuesHolders = new ArrayList<ValuesHolder>();
		SortedSet<Integer> keys = new TreeSet<Integer>(tsValuesHolderMap.keySet());
		for (Integer key : keys) {
			valuesHolders.add(tsValuesHolderMap.get(key));
		}
		return valuesHolders;
	}

}
