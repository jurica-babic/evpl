package org.powertac.smartparkinglot;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.powertac.smartparkinglot.domain.BatteryDescriptor;
import org.powertac.smartparkinglot.domain.CarDescriptor;
import org.powertac.smartparkinglot.domain.MLRModel;
import org.powertac.smartparkinglot.domain.ParkingPolicyType;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;
import org.powertac.smartparkinglot.util.WTPBayesSpecification;
import org.powertac.smartparkinglot.util.WillingnessToPayType;

import java.util.Arrays;
import java.util.Random;

/**
 * Simulation parameters holder.
 *
 * @author Jurica Babic
 */
public class Parameters {

    public static final int ZERO = 0;
    public static final int ONE = 1;
    private static final int VALUE_INDEX = 1;

    private boolean isLongitudinalTracking = false;

    public boolean isLongitudinalTracking() {
        return isLongitudinalTracking;
    }

    /**
     * If true, the sim will track per time slot values. Otherwise, only the aggregate values will be available.
     * @param isLongitudinalTracking
     * @return
     */
    public Parameters withIsLongitudinalTracking(boolean isLongitudinalTracking) {
        this.isLongitudinalTracking = isLongitudinalTracking;
        return this;
    }

    private int populationSize;


    /**
     * Population size: number of cars in simulation
     *
     * @param populationSize
     * @return
     */
    public Parameters withPopulationSize(int populationSize) {
        if (populationSize > ZERO) {
            this.populationSize = populationSize;
        } else {
            System.err.println("Population size is invalid!");
        }
        return this;
    }

    private double parkingPrice;

    /**
     * Parking price per time slot.
     *
     * @param parkingPrice
     * @return
     */
    public Parameters withParkingPrice(double parkingPrice) {
        if (parkingPrice >= ZERO) {
            this.parkingPrice = parkingPrice;
        } else {
            System.err.println("Parking price is invalid!");
        }
        return this;
    }

    private double parkingPriceHalfHour;

    /**
     * Parking price per half a time slot.
     *
     * @param parkingPriceHalfHour
     * @return
     */
    public Parameters withParkingPriceHalfHour(double parkingPriceHalfHour) {
        if (parkingPriceHalfHour >= ZERO) {
            this.parkingPriceHalfHour = parkingPriceHalfHour;
        } else {
            System.err.println("Parking price for half hour is invalid!");
        }
        return this;
    }

    public double getParkingPriceHalfHour() {
        return parkingPriceHalfHour;
    }

    //	@Option(name = "-referenceElectricityPrice", usage = "Reference electricity price per time slot.", required = true)
    //  private double referenceElectricityPrice; USED as RP in WTP function when there is no bayesian network around...

    private double chargingFee;

    /**
     * The amount of money an EV pays for each hour parking in an EV-enabled spot.
     *
     * @param chargingFee
     * @return
     */
    public Parameters withChargingFee(double chargingFee) {
        if (chargingFee >= ZERO) {
            this.chargingFee = chargingFee;
        } else {
            System.err.println("Charging fee is invalid!");
        }
        return this;
    }

    private double evShare;

    /**
     * EV share within the population. Percentage: [0-1]
     *
     * @param evShare
     * @return
     */
    public Parameters withEVShare(double evShare) {
        if (evShare >= ZERO && evShare <= ONE) {
            this.evShare = evShare;
        } else {
            System.err.println("EV share is invalid!");
        }
        return this;
    }

    //////////// Smart Parking Lot//////////////////

    private int nonEVParkingSpots;

    /**
     * Number of non-EV parking spots (for ICVs, such parking spots do not have chargers installed).
     *
     * @param nonEVParkingSpots
     * @return
     */
    public Parameters withNonEVParkingSpots(int nonEVParkingSpots) {
        if (nonEVParkingSpots >= ZERO) {
            this.nonEVParkingSpots = nonEVParkingSpots;
        } else {
            System.err.println("Non-EV parking spots number is invalid!");
        }
        return this;
    }

    private int evParkingSpots;

    /**
     * Number of EV parking spots (for EVs, such parking spots have chargers installed).
     *
     * @param evParkingSpots
     * @return
     */
    public Parameters withEVParkingSpots(int evParkingSpots) {
        if (evParkingSpots >= ZERO) {
            this.evParkingSpots = evParkingSpots;
        } else {
            System.err.println("EV parking spots number is invalid!");
        }
        return this;
    }

    private double chargerSpeed;

    /**
     * Charger speed, specified in kW.
     *
     * @param chargerSpeed
     * @return
     */
    public Parameters withChargerSpeed(double chargerSpeed) {
        if (chargerSpeed >= ZERO) {
            this.chargerSpeed = chargerSpeed;
        } else {
            System.err.println("Charger speed is invalid!");
        }
        return this;
    }

    private double chargerCost;

    /**
     * Investment cost per charger.
     *
     * @param chargerCost
     * @return
     */
    public Parameters withChargerCost(double chargerCost) {
        if (chargerCost >= ZERO) {
            this.chargerCost = chargerCost;
        } else {
            System.err.println("Charger cost is invalid!");
        }
        return this;
    }

    private String marketPriceFile = "/market-prices.txt";

    /**
     * Input market price file in MWh. The method will also load the prices from text files.
     *
     * @param marketPriceFile
     * @return
     */
    public Parameters withMarketPriceFile(String marketPriceFile, DataGranularity dataGranularity) throws HourlyValuesResolveException {
        if (marketPriceFile != null) {
            this.marketPriceFile = marketPriceFile;
            double[][] prices = Util.getHourlyValuesForAYear(marketPriceFile, -1, dataGranularity);
            marketPriceValuesForYear = prices;
        } else {
            System.err.println("Market price file is invalid!");
        }
        return this;
    }

    private String area;

    /**
     * Area name, e.g., 'Princes Theatre'. Each area has different arrival and departure rates. This method will also load arrival and departure values from text files
     *
     * @param area
     * @return
     */
    public Parameters withArea(String area, boolean shouldLoadValues, boolean inputeValuesForZero) throws HourlyValuesResolveException {
        if (area != null) {
            this.area = area;
            if (shouldLoadValues) {
                double[][] arrivals = Util.getHourlyValuesForAYear("/" + area + "-arrivals.txt", -1, DataGranularity.HOURLY);
                double[][] departures = Util.getHourlyValuesForAYear("/" + area + "-departures.txt", -1, DataGranularity.HOURLY);

                arrivalValuesForYear = arrivals;
                departureValuesForYear = departures;

                if(inputeValuesForZero){
                    double minNonZeroArrival = Double.MAX_VALUE;
                    double minNonZeroDeparture = Double.MAX_VALUE;
                    for(int i =0; i<arrivalValuesForYear[1].length;i++){
                        double currentArrival = arrivalValuesForYear[1][i];
                        if(currentArrival!=0.0 && currentArrival<minNonZeroArrival){
                            minNonZeroArrival = currentArrival;
                        }
                        double currentDeparture = departureValuesForYear[1][i];
                        if(currentDeparture!=0.0 && currentDeparture<minNonZeroDeparture){
                            minNonZeroDeparture = currentDeparture;
                        }
                    }
                    // inpute values:
                    for(int i =0; i<arrivalValuesForYear[1].length;i++){
                        if(arrivalValuesForYear[1][i] == 0.0){
                            arrivalValuesForYear[1][i] = minNonZeroArrival;
                        }
                        if(departureValuesForYear[1][i] == 0.0){
                            departureValuesForYear[1][i] = minNonZeroDeparture;
                        }
                    }

                }

            }
        } else {
            System.err.println("Area is invalid!");
        }
        return this;
    }

    private BatteryDescriptor[] batteryDescriptors;

    /**
     * Battery capacities that will be available for the population, e.g., 24kWh and 16kWh.
     *
     * @param values
     * @return
     */
    public Parameters withBatteryDescriptors(BatteryDescriptor[] values) {
        if (values != null) {
            this.batteryDescriptors = values;
        } else {
            System.err.println("Battery descriptors are invalid!");
        }
        return this;
    }

    private double[] batteryCapacityShares;

    /**
     * Shares from battery capacities, e.g., 0.81 for 24kWh and 0.19 for 16kWh.
     *
     * @param values
     * @return
     */
    public Parameters withBatteryDescriptorsShares(double[] values) {
        if (values != null) {
            this.batteryCapacityShares = values;
        } else {
            System.err.println("Battery capacity shares are invalid!");
        }
        return this;
    }

    private double[][] arrivalValuesForYear;

    /**
     * Arrival rates for one year. Each "line" in values (2D array) corresponds to (HOUR, NO_CARS_IN_THAT_HOUR) info
     *
     * @param values
     * @return
     */
    public Parameters withArrivalValuesForYear(double[][] values) {
        if (values != null) {
            this.arrivalValuesForYear = values;
        } else {
            System.err.println("Arrival values are invalid!");
        }
        return this;
    }

    private double[][] departureValuesForYear;

    /**
     * Departure rates for one year. Each "line" in values (2D array) corresponds to (HOUR, NO_CARS_IN_THAT_HOUR) info
     *
     * @param values
     * @return
     */
    public Parameters withDepartureValuesForYear(double[][] values) {
        if (values != null) {
            this.departureValuesForYear = values;
        } else {
            System.err.println("Arrival values are invalid!");
        }
        return this;
    }

    private double[][] marketPriceValuesForYear;

    /**
     * Market price values for year in MWh. Each "line" in values (2D array) corresponds to (HOUR, PRICE_PER_MWH_FOR_THAT_HOUR) info
     *
     * @param values
     * @return
     */
    public Parameters withMarketPriceValuesForYear(double[][] values) {
        if (values != null) {
            this.marketPriceValuesForYear = values;
        } else {
            System.err.println("Market price values are invalid!");
        }
        return this;
    }

    ////// MASON ARGS: //////////////////////

    private long seed;

    /**
     * Seed is used to instantiate random generator. It allows reproducibility of results.
     *
     * @param seed
     * @return
     */
    public Parameters withSeed(long seed) {
            this.seed = seed;
        return this;
    }

    private double until;

    public Parameters withUntil(double until) {
        if (until > ZERO) {
            this.until = until;
        } else {
            System.err.println("Until is invalid!");
        }
        return this;
    }

    private WillingnessToPayType willingnessToPayType;

    /**
     * Willingness to pay type. It is used by a simulation to select the method used to calculate WTP.
     *
     * @param type
     * @return
     */
    public Parameters withWillingnessToPayType(WillingnessToPayType type) {
        if (type != null) {
            this.willingnessToPayType = type;
        } else {
            System.err.println("WillingnessToPayType is invalid!");
        }
        return this;
    }

    public WillingnessToPayType getWillingnessToPayType() {
        return willingnessToPayType;
    }

    private MLRModel mlrModel = MLRModel.AUSTRALIA_MODEL;

    public Parameters withMlrModel(MLRModel mlrModel){
        if(mlrModel != null){
            this.mlrModel = mlrModel;
        } else{
            System.err.println("MLR model is invalid");
        }
        return  this;
    }

    public MLRModel getMlrModel() {
        return mlrModel;
    }

    private WTPBayesSpecification wtpBayesSpecification;

    public Parameters withWTPBayesSpecification(WTPBayesSpecification wtpBayesSpecification) {
        if (wtpBayesSpecification != null) {
            this.wtpBayesSpecification = wtpBayesSpecification;
        } else {
            System.err.println("WTP Bayes Specification is invalid!");
        }
        return this;
    }

    private ParkingPolicyType parkingPolicyType = ParkingPolicyType.PARKING_LOT_DEDICATED_CHARGING;
    public Parameters withParkingPolicyType(ParkingPolicyType parkingPolicyType) {
        if (parkingPolicyType != null) {
            this.parkingPolicyType = parkingPolicyType;
        } else {
            System.err.println("Parking policy is invalid!");
        }
        return this;
    }

    public ParkingPolicyType getParkingPolicyType() {
        return parkingPolicyType;
    }

    public WTPBayesSpecification getWtpBayesSpecification() {
        return wtpBayesSpecification;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public double getParkingPrice() {
        return parkingPrice;
    }

    public double getChargingFee() {
        return chargingFee;
    }

    public double getEvShare() {
        return evShare;
    }

    public int getNonEVParkingSpots() {
        return nonEVParkingSpots;
    }

    public int getEvParkingSpots() {
        return evParkingSpots;
    }

    public double getChargerSpeed() {
        return chargerSpeed;
    }

    public double getChargerCost() {
        return chargerCost;
    }

    public String getMarketPriceFile() {
        return marketPriceFile;
    }

    public String getArea() {
        return area;
    }

    public BatteryDescriptor[] getBatteryDescriptors() {
        return batteryDescriptors;
    }

    public double[] getBatteryCapacityShares() {
        return batteryCapacityShares;
    }

    public double[][] getArrivalValuesForYear() {
        return arrivalValuesForYear;
    }

    public double[][] getDepartureValuesForYear() {
        return departureValuesForYear;
    }

    public double[][] getMarketPriceValuesForYear() {
        return marketPriceValuesForYear;
    }

    public long getSeed() {
        return seed;
    }

    public double getUntil() {
        return until;
    }


    @Override
    public String toString() {
        return "Parameters{" +
                "isLongitudinalTracking=" + isLongitudinalTracking +
                ", populationSize=" + populationSize +
                ", parkingPrice=" + parkingPrice +
                ", chargingFee=" + chargingFee +
                ", evShare=" + evShare +
                ", nonEVParkingSpots=" + nonEVParkingSpots +
                ", evParkingSpots=" + evParkingSpots +
                ", chargerSpeed=" + chargerSpeed +
                ", chargerCost=" + chargerCost +
                ", marketPriceFile='" + marketPriceFile + '\'' +
                ", area='" + area + '\'' +
                ", batteryDescriptors=" + Arrays.toString(batteryDescriptors) +
                ", batteryCapacityShares=" + Arrays.toString(batteryCapacityShares) +
                ", arrivalValuesForYear=" + Arrays.toString(arrivalValuesForYear) +
                ", departureValuesForYear=" + Arrays.toString(departureValuesForYear) +
                ", marketPriceValuesForYear=" + Arrays.toString(marketPriceValuesForYear) +
                ", seed=" + seed +
                ", until=" + until +
                ", willingnessToPayType=" + willingnessToPayType +
                ", mlrModel="+ mlrModel +
                ", wtpBayesSpecification=" + wtpBayesSpecification +
                ", parkingPolicyType=" + parkingPolicyType +
                '}';
    }

    public boolean checkParametersValidity() {
        if (populationSize < 0 || parkingPrice < 0 || parkingPriceHalfHour<0 || chargingFee < 0 || evShare < 0 || evShare > 1 || nonEVParkingSpots < 0 || evParkingSpots < 0 || chargerSpeed < 0 || chargerCost < 0 || /*marketPriceFile == null ||*/ area == null || batteryDescriptors == null || batteryCapacityShares == null || batteryDescriptors.length != batteryCapacityShares.length || arrivalValuesForYear == null || departureValuesForYear == null || arrivalValuesForYear[VALUE_INDEX].length != departureValuesForYear[VALUE_INDEX].length || marketPriceValuesForYear == null  || until < 0  || willingnessToPayType == null || (willingnessToPayType.equals(WillingnessToPayType.BAYESIAN_NETWORK) && wtpBayesSpecification == null ) || parkingPolicyType == null) {
            return false;
        }
        return true;
    }

    public Parameters() {

    }

    public Parameters(BatteryDescriptor[] batteryCapacityValues, double[] batteryCapacityShares) {
        this.batteryDescriptors = batteryCapacityValues;
        this.batteryCapacityShares = batteryCapacityShares;
    }

    /**
     * Web-friendly constructor. It will not load any values from files. Instead, such values are passed in this constructor.
     *
     * @param populationSize
     * @param parkingPrice
     * @param chargingFee
     * @param evShare
     * @param nonEVParkingSpots
     * @param evParkingSpots
     * @param chargerSpeed
     * @param chargerCost
     * @param marketPrices
     * @param batteryDescriptors
     * @param batteryDescriptorsShares
     * @param area
     * @param arrivalRates
     * @param departureRates
     * @param seed
     * @param until
     * @param willingnessToPayType
     */
    public Parameters(int populationSize, double parkingPrice, double chargingFee, double evShare, int nonEVParkingSpots, int evParkingSpots, double chargerSpeed, double chargerCost, double[][] marketPrices, BatteryDescriptor[] batteryDescriptors, double[] batteryDescriptorsShares, String area, double[][] arrivalRates, double[][] departureRates, long seed, double until, WillingnessToPayType willingnessToPayType) {
        withPopulationSize(populationSize).withParkingPrice(parkingPrice).withChargingFee(chargingFee).withEVShare(evShare);
        withNonEVParkingSpots(nonEVParkingSpots).withEVParkingSpots(evParkingSpots).withChargerSpeed(chargerSpeed).withChargerCost(chargerCost);
        try {
            withMarketPriceValuesForYear(marketPrices);
            withArea(area, false, true);
            withArrivalValuesForYear(arrivalRates).withDepartureValuesForYear(departureRates);
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }

        withBatteryDescriptorsShares(batteryDescriptorsShares).withBatteryDescriptors(batteryDescriptors);
        withSeed(seed).withUntil(until).withWillingnessToPayType(willingnessToPayType);
        if (wtpBayesSpecification != null) {
            withWTPBayesSpecification(wtpBayesSpecification);
        }
    }
        /**
         * A constructor which sets a lot of sim parameters. 'area' will be used to load arrival and departure rates. 'marketPriceFile' will load prices with data granularity specified by 'marketPriceDataGranularity'.
         *
         * @param populationSize             Number of cars within the simulation.
         * @param parkingPrice               Parking price per hour
         * @param chargingFee                Premium price EV pays per hour while parked in an EV spot
         * @param evShare                    Share of EVs within the population
         * @param nonEVParkingSpots          Number of parking spots without installed chargers.
         * @param evParkingSpots             Number of parking spots with chargers installed
         * @param chargerSpeed               Charger speed in kW
         * @param chargerCost                Unit cost per charger
         * @param marketPriceFile            Filename of a file which contains electricity market prices.
         * @param marketPriceDataGranularity Granularity of a marketprice file.
         * @param batteryCapacityValues      Battery capacity values, e.g. {3.4, 5.7} => one type of cars have 3.4 kWh and second type of cars have 5.7kWh
         * @param batteryCapacityShares      Battery capacity shares, e.g. {0.91d, 0.09d} => one type of car batteries have 91% share in a population, whereas a second type of car batteries have 9% share in a population.
         * @param area                       Area name. Each area has different arrival and departure rates.
         * @param seed                       Seed used to create a number generator. It allows reproducibility of simulation results.
         * @param until                      Timestamp (in simulated hours) until the sim will run.
         * @param willingnessToPayType       Type of EV willingness to pay model to be used in the sim. Be careful to set other things such as wtpBayesSpecification outside this constructor.
         */

   public Parameters(int populationSize, double parkingPrice, double parkingPriceHalfHour, double chargingFee, double evShare, int nonEVParkingSpots, int evParkingSpots, double chargerSpeed, double chargerCost, String marketPriceFile, DataGranularity marketPriceDataGranularity, BatteryDescriptor[] batteryCapacityValues, double[] batteryCapacityShares, String area, long seed, double until, WillingnessToPayType willingnessToPayType) {
        withPopulationSize(populationSize).withParkingPrice(parkingPrice).withParkingPriceHalfHour(parkingPriceHalfHour).withChargingFee(chargingFee).withEVShare(evShare);
        withNonEVParkingSpots(nonEVParkingSpots).withEVParkingSpots(evParkingSpots).withChargerSpeed(chargerSpeed).withChargerCost(chargerCost);
        try {
            withMarketPriceFile(marketPriceFile, marketPriceDataGranularity);
            withArea(area, true, true);
        } catch (HourlyValuesResolveException e) {
            e.printStackTrace();
        }
        withBatteryDescriptorsShares(batteryCapacityShares).withBatteryDescriptors(batteryCapacityValues);
        withSeed(seed).withUntil(until);
        withWillingnessToPayType(willingnessToPayType);

    }

    public DescriptiveStatistics generateMarketPricesDescriptiveStatistics() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        if (stats != null) {
            for (double price : marketPriceValuesForYear[VALUE_INDEX]) {
                stats.addValue(price);
            }
        }

        return stats;
    }

    public static DescriptiveStatistics generateMarketPricesDescriptiveStatistics(double[][] marketPriceValuesForYear) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        if (stats != null) {
            for (double price : marketPriceValuesForYear[VALUE_INDEX]) {
                stats.addValue(price);
            }
        }

        return stats;
    }

    public static Parameters createParametersWithSensibleDefaults(String areaName, double chargingFee, double evShare, int nonEVParkingSpots, int evParkingSpots) {
        Parameters sensibleDefaults = new Parameters();

        CarDescriptor[] carDescriptors = {CarDescriptor.TESLA3, CarDescriptor.HYUNDAI_KONA, CarDescriptor.NISSAN_LEAF, CarDescriptor.HYUNDAI_IONIQ, CarDescriptor.TESLA_MODEL_X, CarDescriptor.JAGUAR_I_PACE, CarDescriptor.TESLA_MODEL_S, CarDescriptor.BMW_I3, CarDescriptor.RENAULT_ZOE};

        BatteryDescriptor[] batteryDescriptors = new BatteryDescriptor[carDescriptors.length];
        double[] batteryCapacityShares = new double[carDescriptors.length];

        for (int i = 0; i < carDescriptors.length ; i++) {
            batteryDescriptors[i] = carDescriptors[i].batteryDescriptor;
            batteryCapacityShares[i] = carDescriptors[i].share;
        }


        sensibleDefaults.withArea(areaName, true, false);
        sensibleDefaults.withMarketPriceFile("/market-prices.txt", DataGranularity.HOURLY);
        sensibleDefaults.withPopulationSize(5000).withParkingPrice(7).withParkingPriceHalfHour(3.5).withChargingFee(chargingFee).withEVShare(evShare).withEVParkingSpots(evParkingSpots).withNonEVParkingSpots(nonEVParkingSpots).withChargerSpeed(7.7).withChargerCost(3000);


        sensibleDefaults.withBatteryDescriptors(batteryDescriptors);


        sensibleDefaults.withBatteryDescriptorsShares(batteryCapacityShares);
        sensibleDefaults.withUntil(Simulation.HOURS_PER_YEAR).withWillingnessToPayType(WillingnessToPayType.MULTIPLE_LINEAR_REGRESSION);
        sensibleDefaults.withParkingPolicyType(ParkingPolicyType.PARKING_LOT_DEDICATED_CHARGING);
        sensibleDefaults.withMlrModel(MLRModel.KORONA_2020);
        Random random = new Random();
        long seed = random.nextLong();

        sensibleDefaults.withSeed(seed);

        return sensibleDefaults;
    }

}
