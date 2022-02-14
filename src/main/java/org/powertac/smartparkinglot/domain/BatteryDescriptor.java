package org.powertac.smartparkinglot.domain;

/**
 * Describes one kind of a battery an EV can have.
 * Created by Jurica on 24.11.2016..
 */
public class BatteryDescriptor {
    private final double meanSoc;
    private final double stDevSoc;
    /**
     * BC: Size of the battery in kWh.
     */
    private double batteryCapacity;

    public static final BatteryDescriptor TESLA3 = new BatteryDescriptor(0.5, 0.1, 75);

    public static final BatteryDescriptor HYUNDAI_KONA = new BatteryDescriptor(0.5, 0.1, 64);
    public static final BatteryDescriptor NISSAN_LEAF = new BatteryDescriptor(0.5, 0.1, 40);
    public static final BatteryDescriptor HYUNDAI_IONIQ = new BatteryDescriptor(0.5, 0.1, 38.3);
    public static final BatteryDescriptor TESLA_MODEL_X = new BatteryDescriptor(0.5, 0.1, 100);
    public static final BatteryDescriptor JAGUAR_I_PACE = new BatteryDescriptor(0.5, 0.1, 90);
    public static final BatteryDescriptor TESLA_MODEL_S = new BatteryDescriptor(0.5, 0.1, 100);
    public static final BatteryDescriptor BMW_I3 = new BatteryDescriptor(0.5, 0.1, 42.2);
    public static final BatteryDescriptor RENAULT_ZOE = new BatteryDescriptor(0.5, 0.1, 52);

    public BatteryDescriptor(double meanSoc, double stDevSoc, double batteryCapacity) {
        this.meanSoc = meanSoc;
        this.stDevSoc = stDevSoc;
        this.batteryCapacity = batteryCapacity;
    }

    public static BatteryDescriptor testInstance(){
        return new BatteryDescriptor(0.5,0,20);
    }

    public double getMeanSoc() {
        return meanSoc;
    }

    public double getStDevSoc() {
        return stDevSoc;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

}
