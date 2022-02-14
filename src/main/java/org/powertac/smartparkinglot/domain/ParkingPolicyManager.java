package org.powertac.smartparkinglot.domain;

/**
 * Created by Jurica on 11.7.2017..
 *
 * ParkingPolicyManager will run the appropriate parking policy from all of the available parking policies.
 *
 */
public class ParkingPolicyManager implements ParkingPolicy {
    ParkingPolicyType currentParkingPolicy = ParkingPolicyType.PARKING_LOT_DEDICATED_CHARGING;

    public ParkingPolicyManager(ParkingPolicyType parkingPolicyType) {
        currentParkingPolicy = parkingPolicyType;
    }


    @Override
    public VehicleState calculateVehicleState(SmartParkingLot parkingLot, Car vehicle) {
        switch (currentParkingPolicy){
            case PARKING_LOT_DEDICATED_CHARGING:
                return new TraditionalParkingLotDedicatedChargingStationParkingPolicy().calculateVehicleState(parkingLot, vehicle);
            case EV_EXCLUSIVE_SPOTS:
                return new EVExclusiveParkingSpotParkingPolicy().calculateVehicleState(parkingLot, vehicle);
            case FREE_FOR_ALL:
                return new FreeForAllParkingSpotParkingPolicy().calculateVehicleState(parkingLot, vehicle);
            default:
                return new TraditionalParkingLotDedicatedChargingStationParkingPolicy().calculateVehicleState(parkingLot, vehicle);
        }
    }

    public ParkingPolicyType getCurrentParkingPolicy() {
        return currentParkingPolicy;
    }

    /***
     * For future compatibility: dynamically switch parking policies
     * @param currentParkingPolicy
     */
    public void setCurrentParkingPolicy(ParkingPolicyType currentParkingPolicy) {
        this.currentParkingPolicy = currentParkingPolicy;
    }

    private class TraditionalParkingLotDedicatedChargingStationParkingPolicy implements ParkingPolicy {
        @Override
        public VehicleState calculateVehicleState(SmartParkingLot parkingLot, Car vehicle) {

            // leave if the parking lot is full:
            if (!parkingLot.isSpotAvailable()) {
                return VehicleState.LEAVE;
            }
            if (vehicle.isEV()) {
                EV ev = (EV) vehicle;
                // AVAILABLE EV SPOT?
                if (parkingLot.isEvParkingSpotAvailable()) {
                    // PREMIUM PRICE LESS THAN RESERVATION PRICE?
                    if (ev.analyzeChargingFeeOffer(parkingLot.getChargingFee())) {
                        return VehicleState.PARKED_EV_SPOT_WITH_CHARGING;
                    } else{
                        // CHECK WHETHER THE NON-EV SPOT IS AVAILABLE:
                        return calculateFinalStateForNonEVSpotTraditional(parkingLot);
                    }
                } else {
                    return VehicleState.PARKED_NONEV_SPOT;
                }
            } else{
                // AVAILABLE NON-EV SPOT:
                return calculateFinalStateForNonEVSpotTraditional(parkingLot);
            }
        }
    }

    private VehicleState calculateFinalStateForNonEVSpotTraditional(SmartParkingLot parkingLot) {
        if(parkingLot.isIcvParkingSpotAvailable()) {
            return VehicleState.PARKED_NONEV_SPOT;
        } else{
            return VehicleState.LEAVE;
        }
    }

    private class EVExclusiveParkingSpotParkingPolicy implements ParkingPolicy {
        @Override
        public VehicleState calculateVehicleState(SmartParkingLot parkingLot, Car vehicle) {

            // leave if the parking lot is full:
            if (!parkingLot.isSpotAvailable()) {
                return VehicleState.LEAVE;
            }
            if (vehicle.isEV()) {
                EV ev = (EV) vehicle;
                // AVAILABLE EV SPOT?
                if (parkingLot.isEvParkingSpotAvailable()) {
                    // PREMIUM PRICE LESS THAN RESERVATION PRICE?
                    if (ev.analyzeChargingFeeOffer(parkingLot.getChargingFee())) {
                        return VehicleState.PARKED_EV_SPOT_WITH_CHARGING;
                    } else{
                        // !!! KEY CHANGE WRT TraditionalParkingLotDedicatedChargingStationParkingPolicy
                        return VehicleState.PARKED_EV_SPOT;
                    }
                } else {
                    return VehicleState.PARKED_NONEV_SPOT;
                }
            } else{
                // AVAILABLE NON-EV SPOT:
                return calculateFinalStateForNonEVSpotTraditional(parkingLot);
            }
        }
    }

    private class FreeForAllParkingSpotParkingPolicy implements ParkingPolicy {
        @Override
        public VehicleState calculateVehicleState(SmartParkingLot parkingLot, Car vehicle) {

            // leave if the parking lot is full:
            if (!parkingLot.isSpotAvailable()) {
                return VehicleState.LEAVE;
            }
            if (vehicle.isEV()) {
                EV ev = (EV) vehicle;
                // AVAILABLE EV SPOT?
                if (parkingLot.isEvParkingSpotAvailable()) {
                    // PREMIUM PRICE LESS THAN RESERVATION PRICE?
                    if (ev.analyzeChargingFeeOffer(parkingLot.getChargingFee())) {
                        return VehicleState.PARKED_EV_SPOT_WITH_CHARGING;
                    } else{
                        return VehicleState.PARKED_EV_SPOT;
                    }
                } else {
                    return VehicleState.PARKED_NONEV_SPOT;
                }
            } else{
                // AVAILABLE NON-EV SPOT:
                if(parkingLot.isIcvParkingSpotAvailable()){
                    return VehicleState.PARKED_NONEV_SPOT;
                } else {
                    // KEY CHANGE WRT EVExclusiveParkingSpotParkingPolicy
                    return VehicleState.PARKED_EV_SPOT;
                }
            }
        }
    }
}
