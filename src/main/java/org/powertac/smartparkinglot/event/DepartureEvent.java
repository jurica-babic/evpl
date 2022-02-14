package org.powertac.smartparkinglot.event;

import org.powertac.smartparkinglot.domain.*;

import sim.engine.SimState;

/**
 * This class models a departure event.
 * 
 * @author Jurica Babic
 *
 */
public class DepartureEvent extends AbstractEvent {

	public static final int ONE_HOUR = 1;
	public static final double HALF_HOUR = 0.5;

	public DepartureEvent(Car car, SmartParkingLot spl, ProviderAgent providerAgent, double time) {
		super(car, spl, providerAgent, time);
		car.setDeparture(this);
	}

	public void step(SimState state) {
		// LOG:
		providerAgent.getCurrentTimeslotValuesHolder().incrementDeparturesCount();

		DepartureEvent departure = car.getDeparture();
		ArrivalEvent arrival = car.getArrival();

		double timeSpentParked = departure.getTime() - arrival.getTime();
		double remainingLessThanHour = timeSpentParked % ONE_HOUR;
		double fullParkingHrs = timeSpentParked - remainingLessThanHour; // Math.ceil(timeSpentParked);
		double halfHourParkingHrs = 0;
		if(remainingLessThanHour > HALF_HOUR){
			fullParkingHrs++;
		} else{
			halfHourParkingHrs++;
		}

		double parkingMoney = fullParkingHrs * spl.getParkingFee() + halfHourParkingHrs * spl.getParkingFeeHalfHour();

		// ----------------PAYMENTS---------------------------------------------------

		// PARKING IN GENERAL:
		providerAgent.getCurrentTimeslotValuesHolder().addEvplParkingMoneyAll(parkingMoney);

		switch (car.getState()){
			case PARKED_NONEV_SPOT:
				parkedNonEVSpotPaymentLogs(parkingMoney);
				break;
			case PARKED_EV_SPOT:
				parkedEVSpotPaymentLogs(parkingMoney, timeSpentParked);
				break;
			case PARKED_EV_SPOT_WITH_CHARGING:
				parkedEVSpotPaymentLogs(parkingMoney, timeSpentParked);
				if(car.isEV()){
					providerAgent.getCurrentTimeslotValuesHolder().addTotalChargingDurationHrs(((EV) car).getChargingDuration());
				}
				chargingPaymentLogs(fullParkingHrs+halfHourParkingHrs);
				break;
		}

		// FINISH:
		spl.departureCar(car);
		providerAgent.getParkingDurationsStatistics().addValue(timeSpentParked);
		providerAgent.onCarExit(car);
		providerAgent.getCurrentTimeslotValuesHolder().addTotalParkingDurationHrs(timeSpentParked);
	}

	private void parkedNonEVSpotPaymentLogs(double parkingMoney) {
		providerAgent.getCurrentTimeslotValuesHolder().addEvplParkingMoneyFromNonEVSpot(parkingMoney);
	}

	private void chargingPaymentLogs(double totalParkingHrs) {
		double electricityMoneyEVPaidSPL = spl.getChargingFee() * totalParkingHrs;
		providerAgent.getCurrentTimeslotValuesHolder().addEvplElectricityMoneyFromEV(electricityMoneyEVPaidSPL);
	}

	private void parkedEVSpotPaymentLogs(double parkingMoney, double timeSpentParked) {
		providerAgent.getCurrentTimeslotValuesHolder().addEvplParkingMoneyFromEVSpot(parkingMoney);
		providerAgent.getCurrentTimeslotValuesHolder().addTotalParkingDurationEVSpotHrs(timeSpentParked);
	}

}
