package org.powertac.smartparkinglot.domain;

public enum MLRModel {

    KORONA_2020(51.62697, -0.5131541, 0.318984,  0.6766471), USA_MODEL(56.1389, -0.5151, 0.2275, 0.6321), AUSTRALIA_MODEL(53.16599   , -0.38232, 0.22683  , 0.60736 );

    public final double INTERCEPT;
    public final double SOC;
    public final double DELTA_SOC;
    public final double PRICE_FOR_DELTA_SOC;

    MLRModel(double intercept, double soc, double deltaSOC, double priceForDeltaSoc) {
        this.INTERCEPT = intercept;
        this.SOC = soc;
        this.DELTA_SOC = deltaSOC;
        this.PRICE_FOR_DELTA_SOC = priceForDeltaSoc;
    }

    public double predict(double soc, double deltaSOC, double priceForDeltaSOCCents){
        double wtp_cents = INTERCEPT + SOC * soc + DELTA_SOC * deltaSOC + PRICE_FOR_DELTA_SOC * priceForDeltaSOCCents;
        return wtp_cents;
    }



}
