package org.powertac.smartparkinglot.util;

import ec.util.MersenneTwisterFast;
import sim.util.distribution.Normal;

/**
 * Created by Jurica on 24.11.2016..
 */
public class TruncatedNormalDistribution {

    public static double nextDouble(Normal normal, double min, double max, double mean, double standardDeviation) {
        double value = -1;
        do{
            value = normal.nextDouble(mean, standardDeviation);
        } while(value<min || value>max);
        return value;
    }
}
