package org.powertac.smartparkinglot.exception;

public class HourlyValuesResolveException extends RuntimeException {
	public HourlyValuesResolveException(String msg, Throwable t) {
		super(msg, t);
	}

	public HourlyValuesResolveException(String msg) {
		super(msg);
	}
}
