package org.powertac.smartparkinglot.event;

import org.powertac.smartparkinglot.ValuesHolder;
import org.powertac.smartparkinglot.domain.AbstractEvent;
import org.powertac.smartparkinglot.domain.EV;
import org.powertac.smartparkinglot.domain.ProviderAgent;
import org.powertac.smartparkinglot.domain.SmartParkingLot;

import sim.engine.SimState;

/**
 * Charging-event, once per time slot.
 * 
 * @author Jurica
 *
 */
public class ChargingExecutionEvent extends AbstractEvent {

	private double amountKWh;

	public ChargingExecutionEvent(EV car, SmartParkingLot spl, ProviderAgent eventProvider, double time,
			double amountKWh) {
		super(car, spl, eventProvider, time);
		this.amountKWh = amountKWh;
	}

	@Override
	public void step(SimState state) {
		// this event is only for EVs so we can safely down-cast it:
		EV ev = ((EV) car);

		// charge the car:
		ev.setBatteryStatus(ev.getBatteryStatus() + amountKWh);

		// LOG payments:
		ValuesHolder valuesHolder = providerAgent.getCurrentTimeslotValuesHolder();
		double electricityPricePerKWh = providerAgent.getCurrentElectricityPricePerKWh();
		double electricityCost = amountKWh * electricityPricePerKWh;
		valuesHolder.addEvplElectricityCostFromMarket(electricityCost);
		// PLEASE NOTE: parking money and electricity fee are paid during departure, not here
		valuesHolder.addTotalElectricityProcuredkWh(amountKWh);

	}

}
