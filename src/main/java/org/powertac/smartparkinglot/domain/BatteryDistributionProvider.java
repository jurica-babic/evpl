package org.powertac.smartparkinglot.domain;

/**
 * Created by Jurica on 24.11.2016..
 */
public interface BatteryDistributionProvider {

    /**
     *
     * Uniform between 0 and batteryCapacity (excluding 0 and batteryCapacity)
     * @param batteryCapacity
     * @return
     */
    public double drawBatteryStatus(double batteryCapacity, double avgSoc, double stDevSoc);
}
