package org.powertac.smartparkinglot.exception;

public class NoMoreArrivalsException extends Exception {
	public NoMoreArrivalsException(String msg, Throwable t) {
		super(msg, t);
	}

	public NoMoreArrivalsException(String msg) {
		super(msg);
	}
}
