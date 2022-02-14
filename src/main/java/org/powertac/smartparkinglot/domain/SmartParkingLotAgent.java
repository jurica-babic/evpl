package org.powertac.smartparkinglot.domain;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import sim.engine.SimState;

/**
 * A concrete smart parking lot.
 * 
 * @author Jurica Babic
 *
 */
public class SmartParkingLotAgent implements SmartParkingLot, QueueStatistics {

	private int carsCurrentlyParked;

	private int carsAccepted;

	private int carsRejected;

	private int nonEVSpotCurrentlyInUse;

	private int evSpotsCurrentlyInUse;

	private int evParkingSpotSize;

	private int icvParkingSpotSize;

	private int totalParkingSpotSize;

	private double chargingFee;

	private double parkingFee;

	private double parkingFeeHalfHour;

	private double chargerSpeed;

	private SummaryStatistics carsCurrentlyParkedStatistics;

	private ParkingPolicyManager parkingPolicyManager;

	private ProviderAgent providerAgent;

	public SmartParkingLotAgent(int evParkingSpots, int nonEvParkingSpots,
			double chargingFee, double parkingFee, double parkingFeeHalfHour, double chargerSpeed, ProviderAgent trackerAgent, ParkingPolicyType initialParkingPolicyType) {
		this.evParkingSpotSize = evParkingSpots;
		this.icvParkingSpotSize = nonEvParkingSpots;
		this.totalParkingSpotSize = evParkingSpots + nonEvParkingSpots;
		this.chargingFee = chargingFee;
		this.parkingFee = parkingFee;
		this.parkingFeeHalfHour = parkingFeeHalfHour;
		this.chargerSpeed = chargerSpeed;
		this.providerAgent = trackerAgent;
		this.parkingPolicyManager = new ParkingPolicyManager(initialParkingPolicyType);

		carsCurrentlyParkedStatistics = new SummaryStatistics();
	}

	public ParkingPolicyManager getParkingPolicyManager() {
		return parkingPolicyManager;
	}

	public void acceptCar(Car car, boolean parkCarOnEvSpot) {
		carsCurrentlyParked++;
		carsAccepted++;
		if (parkCarOnEvSpot) {
			evSpotsCurrentlyInUse++;
		} else {

			nonEVSpotCurrentlyInUse++;
		}

	}

	public void rejectCar(Car car) {
		carsRejected++;

	}

	public void departureCar(Car car) {
		carsCurrentlyParked--;

		switch (car.getState()){
			case PARKED_EV_SPOT_WITH_CHARGING:
			case PARKED_EV_SPOT:
				evSpotsCurrentlyInUse--;
				break;
			case PARKED_NONEV_SPOT:
				nonEVSpotCurrentlyInUse--;
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSpotAvailable() {
		return carsCurrentlyParked<totalParkingSpotSize?true:false;
	}

	public int getEvCurrentlyParked() {
		return evSpotsCurrentlyInUse;
	}

	public int getNonEvCurrentlyParked() {
		return nonEVSpotCurrentlyInUse;
	}

	public void step(SimState state) {
		carsCurrentlyParkedStatistics.addValue(carsCurrentlyParked);

		// handle charging plans each TS: TODO

		providerAgent.getCurrentTimeslotValuesHolder().setCurrentEvSpotsInUse(evSpotsCurrentlyInUse);
		providerAgent.getCurrentTimeslotValuesHolder().setCurrentNonEvSpotsInUse(nonEVSpotCurrentlyInUse);

	}

	public void report() {
		//System.out.println(toString());
	}

	@Override
	public String toString() {
		return "SmartParkingLotAgent{" +
				"carsCurrentlyParked=" + carsCurrentlyParked +
				", carsAccepted=" + carsAccepted +
				", carsRejected=" + carsRejected +
				", nonEVSpotCurrentlyInUse=" + nonEVSpotCurrentlyInUse +
				", evSpotsCurrentlyInUse=" + evSpotsCurrentlyInUse +
				", evParkingSpotSize=" + evParkingSpotSize +
				", icvParkingSpotSize=" + icvParkingSpotSize +
				", totalParkingSpotSize=" + totalParkingSpotSize +
				", chargingFee=" + chargingFee +
				", parkingFee=" + parkingFee +
				", chargerSpeed=" + chargerSpeed +
				", carsCurrentlyParkedStatistics=" + carsCurrentlyParkedStatistics +
				", parkingPolicyManager=" + parkingPolicyManager +
				", providerAgent=" + providerAgent +
				'}';
	}

	public double getOccupancyRate() {
		// TODO Auto-generated method stub
		return carsCurrentlyParkedStatistics.getMean() / getParkingSpotSize();
	}

	public double getRejectionRate() {
		return (carsRejected * 1.0f / (carsAccepted + carsRejected));
	}

	public long getParkingSpotSize() {
		return icvParkingSpotSize + evParkingSpotSize;
	}

	public boolean isEvParkingSpotAvailable() {
		if (evSpotsCurrentlyInUse < evParkingSpotSize) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isIcvParkingSpotAvailable() {
		if (nonEVSpotCurrentlyInUse < icvParkingSpotSize) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public double getParkingFeeHalfHour() {
		return parkingFeeHalfHour;
	}

	public double getChargerSpeed() {
		return chargerSpeed;
	}

	public double getChargingFee() {
		return chargingFee;
	}

	public double getParkingFee() {
		return parkingFee;
	}

}
