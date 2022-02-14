package org.powertac.smartparkinglot.domain;

/**
 * EV battery.
 * Created by Jurica on 24.11.2016..
 */
public class Battery {

    private BatteryDescriptor batteryDescriptor;
    private BatteryDistributionProvider batteryDistributionProvider;
    /**
     * BS: the amount of electricity currently present in the battery, measured
     * in kWh. State-of-charge (SoC) but NOT in percentages.
     */
    private double batteryStatus;



    public Battery(BatteryDescriptor batteryDescriptor){
        this.batteryDescriptor = batteryDescriptor;
    }

    public Battery(BatteryDescriptor batteryDescriptor, BatteryDistributionProvider batteryDistributionProvider) {
        this.batteryDescriptor = batteryDescriptor;
        this.batteryDistributionProvider = batteryDistributionProvider;
    }

    public void setBatteryStatus(double batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public void drawAndSetBatteryStatus(){
        setBatteryStatus(batteryDistributionProvider.drawBatteryStatus(batteryDescriptor.getBatteryCapacity(), batteryDescriptor.getMeanSoc(), batteryDescriptor.getStDevSoc()));
    }

    public BatteryDescriptor getBatteryDescriptor() {
        return batteryDescriptor;
    }

    public double getBatteryStatus() {
        return batteryStatus;
    }
}
