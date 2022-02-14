package org.powertac.smartparkinglot.domain;

import org.apache.commons.lang3.NotImplementedException;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WillingnessToPayType;
import sim.engine.SimState;

/**
 * A concrete implementation of an electric vehicle. If there will be
 * differences between EV types in terms of properties and behavior, we will
 * mark this class as abstract.
 *
 * @author Jurica Babic
 */
public class EVAgent extends AbstractCar implements EV {

    private EVType type;

    private WillingnessToPay willingnessToPay;

    private Battery battery;
    private MarketDataProvider marketDataProvider;

    private double chargingDuration;

    private double referenceElectricityPrice;

    public EVAgent(long id, EVType type, BatteryDescriptor batteryDescriptor,BatteryDistributionProvider batteryDistributionProvider, WillingnessToPayType willingnessToPayType, WTPBayesSpecification wtpBayesSpecification, WillingnessToPayDistributionProvider willingnessToPayDistributionProvider, MarketDataProvider marketDataProvider, MLRModel mlrModel) {
        super(id);
        this.type = type;
        this.willingnessToPay = new WillingnessToPay(willingnessToPayType, wtpBayesSpecification, willingnessToPayDistributionProvider, mlrModel);
        this.battery = new Battery(batteryDescriptor,batteryDistributionProvider);
        this.marketDataProvider = marketDataProvider;
    }

    @Override
    public double getChargingDuration() {
        return chargingDuration;
    }
    @Override
    public void setChargingDuration(double chargingDuration) {
        this.chargingDuration = chargingDuration;
    }

    public double getBatteryCapacity() {
        return battery.getBatteryDescriptor().getBatteryCapacity();
    }

    public double getBatteryStatus() {

        return battery.getBatteryStatus();
    }

    public boolean isEV() {
        return true;
    }

    public EVType getType() {
        return type;
    }

    public void step(SimState state) {

    }

    @Override
    public boolean analyzeChargingFeeOffer(double chargingFee) {
        willingnessToPay.drawAndSetWillingnessToPay(battery, referenceElectricityPrice);
        double wtp = willingnessToPay.getWillingnessToPay();
        // price matching:
        if(wtp>=chargingFee){
            return true;
        } else{
            return false;
        }
    }

    public void instantiateEVPreferences() {

        chargingDuration = 0;
        battery.drawAndSetBatteryStatus();
        this.referenceElectricityPrice = marketDataProvider.getCurrentElectricityPricePerKWh();

    }


    public double getReferenceElectricityPrice() {
        return referenceElectricityPrice;
    }

    public double getWillingnessToPay() {
        return willingnessToPay.getWillingnessToPay();
    }

    public void setBatteryStatus(double batteryStatus) {
        this.battery.setBatteryStatus(batteryStatus);
    }



}
