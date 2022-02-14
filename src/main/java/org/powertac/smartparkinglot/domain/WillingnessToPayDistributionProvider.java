package org.powertac.smartparkinglot.domain;

import org.powertac.smartparkinglot.util.WTPMultipleLinearRegression;

/**
 * Created by Jurica on 24.11.2016..
 */
public interface WillingnessToPayDistributionProvider {
    /**
     * Draw a willingness to pay value for an EV.
     * J. Babic, A. Carvalho, W. Ketter, and V. Podobnik, “Economic Benefits of Smart Parking Lots,” in Erasmus Energy Forum 2015 Science Day, 2015.

     * @return
     */
    public double drawWillingnessToPayUtilityFunction(double batteryCapacity, double batteryStatus, double referenceElectricityPrice);

    public double drawWillingnessToPayBayesianNetwork(double batteryCapacity, double batteryStatus, double referenceElectricityPrice);

    public double drawWillingnessToPayMultipleLinearRegression(double batteryCapacity, double batteryStatus, double referenceElectricityPrice, MLRModel mlrModel);
}
