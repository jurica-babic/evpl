package org.powertac.smartparkinglot.util;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * This class will do a single point estimate for WTP by using multiple linear regression. Parameters are learned in R langugage.
 * <p>
 * Important thing to mention regarding the variables:
 *
 * -
 * -
 * - WTP [0 cents - 300 cents]
 *
 * Deprecated as of 3.9.2019.
 */
@Deprecated
public class WTPMultipleLinearRegression {
    private static final double INTERCEPT = 56.1389;
    private static final double SOC = -0.5151;
    private static final double DELTA_SOC = 0.2275;
    private static final double PRICE_FOR_DELTA_SOC = 0.6321;

    /**
     * Predicts WTP based on SOC, delta SOC and price for delta SOC
     * @param soc SOC [0%-100%] = state of charge in percentages
     * @param deltaSOC DELTA_SOC [0%-100%] = additional SOC that could be possibly charged in one hour. In order to obtain this value in the simulator, one must calculate DELTA_SOC based on a charger speed and EV's battery capacity.
     * @param priceForDeltaSOCCents PRICE_FOR_DELTA [0-100 cents in data set] = total price one would pay for DELTA_SOC. In order to obtain this value in the simulator, one must multiply the current (or reference) price, DELTA_SOC and EV's battery capacity.
     * @return WTP in cents
     */
    /*public static final double predict(double soc, double deltaSOC, double priceForDeltaSOCCents){
        double wtp_cents = INTERCEPT + SOC * soc + DELTA_SOC * deltaSOC + PRICE_FOR_DELTA_SOC * priceForDeltaSOCCents;
        return wtp_cents;
    } */

}
