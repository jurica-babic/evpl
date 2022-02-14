package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.util.WillingnessToPayType;

/**
 * 
 * An interface for an electric vehicle.
 * 
 * @author Jurica Babic
 *
 */
public interface EV extends Car {

	public EVType getType();

	/**
	 * Done prior to arrival scheduling. An EV will come with a certain battery
	 * status, will have a willingness to pay.
	 */
	public void instantiateEVPreferences();

	public double getBatteryCapacity();

	public double getBatteryStatus();

	/**
	 * Willingness-to-pay is the amount of money an EV is willing to pay for the
	 * charging services. The value should be called after the instantiateEVPreferences method is called.
	 * 
	 * @return
	 */
	public double getWillingnessToPay();

	public double getReferenceElectricityPrice();

	public void setBatteryStatus(double batteryStatus);

	public void setChargingDuration(double chargingDuration);

	public double getChargingDuration();

	/**
	 * Price matching method. Parking lot will offer the chargingFee, EV will look into the WTP to see whether will be parked in an EV-spot.
	 * @param chargingFee
	 * @return true if EV is satisfied with the offer, false otherwise.
	 */
	boolean analyzeChargingFeeOffer(double chargingFee);
}
