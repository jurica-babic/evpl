package org.powertac.smartparkinglot.event;

import org.apache.log4j.Logger;
import org.powertac.smartparkinglot.domain.*;

import sim.engine.SimState;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class models a car arrival.
 *
 * @author Jurica Babic
 *
 */
public class ArrivalEvent extends AbstractEvent {

	private static final int FULL_CHARGING_HOUR = 1;
	private static final double DELTA_KWH = 0.01;
	private static final int ONE_HOUR_PARKING = 1;
	private static final int INITIAL_STEP = 0;
	private static final Logger log = Logger.getLogger(ArrivalEvent.class);;

	public ArrivalEvent(Car car, SmartParkingLot spl, ProviderAgent providerAgent, double time) {
		super(car, spl, providerAgent, time);
		car.setArrival(this);
	}

	public void step(SimState state){
		providerAgent.getCurrentTimeslotValuesHolder().incrementArrivalsCount();
		VehicleState vehicleState = spl.getParkingPolicyManager().calculateVehicleState(spl, car);
		car.setState(vehicleState);

		// decide what happens wrt to the new car state:
		switch (car.getState()){
			case LEAVE:
				spl.rejectCar(car);
				// LOG:
				providerAgent.getCurrentTimeslotValuesHolder().incrementRejectsCount();
				providerAgent.onCarExit(car);
				break;

			case PARKED_NONEV_SPOT:
				spl.acceptCar(car, false);
				providerAgent.scheduleDepartureEvent(car);
				// LOG:
				providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsOnNonEvSpotCount();
				if(car.isEV()){
					providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsEvParkedOnNonEvCount();
				} else {
					providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsNonEVParkedOnNonEVCount();
				}

				break;
			case PARKED_EV_SPOT_WITH_CHARGING:
				parkedEVSpot();
				if(car.isEV()){
					generateChargingPlan((EV) car);
				}

				break;
			case PARKED_EV_SPOT:
				parkedEVSpot();
				break;
			default:
				break;
		}

		// spawn the next arrival:
		ArrivalEvent newArrival = providerAgent.scheduleNewArrival();

		if(newArrival ==null){
			System.err.println("Arrival was not generated because it's processing would take place after the sim finishes");
		}
	}

	private void parkedEVSpot() {
		spl.acceptCar(car, true);
		providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsOnEvSpotCount();
		if(car.isEV()){
			providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsEvParkedOnEvSpotCount();
		} else{
			providerAgent.getCurrentTimeslotValuesHolder().incrementAcceptsNonEVParkedOnEVSpotCount();
		}
		providerAgent.scheduleDepartureEvent(car);
	}


	public List<ChargingExecutionEvent> generateChargingPlan(EV ev) {
		List<ChargingExecutionEvent> chargingExecutionEvents = new ArrayList<>();

		double chargerSpeed = spl.getChargerSpeed();
		double amountNeeded = ev.getBatteryCapacity() - ev.getBatteryStatus();

		double arrivalTime = car.getArrival().getTime();
		double departureTime = car.getDeparture().getTime();
		double parkingDuration = departureTime - arrivalTime;

		double chargingDuration = Math.min(parkingDuration, amountNeeded / chargerSpeed);

		log.debug("From time slot "+ev.getArrival().getTime()+", EV will stay parked this long: "+parkingDuration+" and it will be charged this long:"+chargingDuration);

			double chargingTimeSchedulingDelta = 0;
			double remainingChargingDuration = chargingDuration;
			int step = 0;
			while(remainingChargingDuration>0){
				double perSlotChargingDuration = 0;
				if (step == INITIAL_STEP) {
					double maximumAvailableTimeForFirstSlotCharging = FULL_CHARGING_HOUR - arrivalTime % FULL_CHARGING_HOUR;
					// we are using minimum because remainingChargingDuration might be less than what is available
					perSlotChargingDuration = Math.min(maximumAvailableTimeForFirstSlotCharging, remainingChargingDuration);
				} else if (remainingChargingDuration>=FULL_CHARGING_HOUR) {
					perSlotChargingDuration = FULL_CHARGING_HOUR;
				} else {
					// last charging event and it is less than 1:
					perSlotChargingDuration = remainingChargingDuration;
				}
				remainingChargingDuration -= perSlotChargingDuration;

				ChargingExecutionEvent chargingEvent = providerAgent.scheduleChargingExecutionEvent(ev, chargingTimeSchedulingDelta,
						perSlotChargingDuration * chargerSpeed);
				chargingTimeSchedulingDelta += perSlotChargingDuration;
				step++;
				chargingExecutionEvents.add(chargingEvent);
			}
       ev.setChargingDuration(chargingDuration);
		return chargingExecutionEvents;
	}
}
