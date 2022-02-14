package org.powertac.smartparkinglot.domain;

import org.apache.commons.lang3.NotImplementedException;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WillingnessToPayType;

/**
 * Know-how WTP model for EV owners
 * Created by Jurica on 24.11.2016..
 */
public class WillingnessToPay {

    private double willingnessToPay;

    private WillingnessToPayType willingnessToPayType;

    private WTPBayesSpecification wtpBayesSpecification;

    private WillingnessToPayDistributionProvider willingnessToPayDistributionProvider;

    private MLRModel mlrModel;

    public WillingnessToPay(WillingnessToPayType willingnessToPayType, WTPBayesSpecification wtpBayesSpecification, WillingnessToPayDistributionProvider willingnessToPayDistributionProvider, MLRModel mlrModel) {
        this.mlrModel = mlrModel;
        this.willingnessToPayType = willingnessToPayType;
        this.wtpBayesSpecification = wtpBayesSpecification;
        this.willingnessToPayDistributionProvider = willingnessToPayDistributionProvider;
    }

    public void drawAndSetWillingnessToPay(Battery battery, double referenceElectricityPrice){
        double wtp = 0;
        switch (willingnessToPayType) {
            case BAYESIAN_NETWORK:
                throw new NotImplementedException("Willingness to pay model for a bayesian network is not complete. We do not know how to map offered price to a discrete value.");
                // WTP is nominal, discrete:
                // wtp = distributionProvider.drawWillingnessToPayBayesianNetwork(batteryCapacity, batteryStatus, referenceElectricityPrice);
                // so we need charging fee in nominal as well:
                //offeredChargingFee = wtpBayesSpecification.priceToDiscretePrice(chargingFee);
                //break;
            case UTILITY_FUNCTION:
                // WTP is continuous
                wtp = willingnessToPayDistributionProvider.drawWillingnessToPayUtilityFunction(battery.getBatteryDescriptor().getBatteryCapacity(), battery.getBatteryStatus(), referenceElectricityPrice);
                break;
            case MULTIPLE_LINEAR_REGRESSION:
                wtp = willingnessToPayDistributionProvider.drawWillingnessToPayMultipleLinearRegression(battery.getBatteryDescriptor().getBatteryCapacity(), battery.getBatteryStatus(), referenceElectricityPrice, mlrModel);
                break;
            case WEALTHY_MOFO_TESTER:
                wtp = 999999999;
                break;
            default:
                break;
        }

        this.willingnessToPay = wtp;
    }

    public double getWillingnessToPay() {
        return willingnessToPay;
    }
}
