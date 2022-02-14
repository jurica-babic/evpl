package org.powertac.smartparkinglot.domain;

public class CarDescriptor {

    public final double share;
    public final BatteryDescriptor batteryDescriptor;


    public static final CarDescriptor TESLA3 = new CarDescriptor(BatteryDescriptor.TESLA3, 0.633);
    public static final CarDescriptor HYUNDAI_KONA = new CarDescriptor(BatteryDescriptor.HYUNDAI_KONA, 0.096);
    public static final CarDescriptor NISSAN_LEAF = new CarDescriptor(BatteryDescriptor.NISSAN_LEAF, 0.078);
    public static final CarDescriptor HYUNDAI_IONIQ = new CarDescriptor(BatteryDescriptor.HYUNDAI_IONIQ, 0.067);
    public static final CarDescriptor TESLA_MODEL_X = new CarDescriptor(BatteryDescriptor.TESLA_MODEL_X, 0.048);
    public static final CarDescriptor JAGUAR_I_PACE = new CarDescriptor(BatteryDescriptor.JAGUAR_I_PACE, 0.030);
    public static final CarDescriptor TESLA_MODEL_S = new CarDescriptor(BatteryDescriptor.TESLA_MODEL_S, 0.029);
    public static final CarDescriptor BMW_I3 = new CarDescriptor(BatteryDescriptor.BMW_I3, 0.018);
    public static final CarDescriptor RENAULT_ZOE = new CarDescriptor(BatteryDescriptor.RENAULT_ZOE, 0.001);







    public CarDescriptor(BatteryDescriptor batteryDescriptor, double share) {
        this.share = share;
        this.batteryDescriptor = batteryDescriptor;
    }
}
