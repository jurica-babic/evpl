package org.powertac.smartparkinglot.domain;

/**
 * Created by Jurica on 11.7.2017..
 * Parking policy decide what outcome does a certain vehicle should have upon entering the parking lot.
 */
public interface ParkingPolicy {

    /**
     *
     * @return State a vehicle should have after the method is executed
     */
    public VehicleState calculateVehicleState(SmartParkingLot parkingLot, Car vehicle);
}
