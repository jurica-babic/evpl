package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.ValuesHolder;

public interface TrackerProvider {
	public ValuesHolder getCurrentTimeslotValuesHolder();
}
