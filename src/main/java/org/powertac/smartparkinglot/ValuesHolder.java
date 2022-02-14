package org.powertac.smartparkinglot;

import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Stores what has happened in a sim. It can be used to track each time slot of a simulation.
 *
 * @author Jurica Babic
 */
public class ValuesHolder {

    private int arrivalsCount;
    private int departuresCount;
    private int acceptsOnNonEvSpotCount;
    private int acceptsOnEvSpotCount;

    private int acceptsEvParkedOnEvSpotCount;
    private int acceptsEvParkedOnNonEvSpotCount;
    private int acceptsNonEVParkedOnNonEVSpotCount;
    private int acceptsNonEVParkedOnEVSpotCount;


    private int rejectsCount;

    private int currentEvSpotsInUse;
    private int currentNonEvSpotsInUse;

    private double evplParkingMoneyAll;
    private double evplParkingMoneyEVSpot;
    private double evplParkingMoneyNonEVSpot;
    private double evplElectricityCostFromMarket;
    private double evplElectricityMoneyFromEVSpot;
    private double evplGrossProfitEVSpot;
    private double evplGrossProfitEVSpotElectricity;

    private String version;
    private double chargingFee;
    private int evParkingSpots;
    private double evShare;
    private String area;
    private String parkingPolicyType;
    //SEED?
    private int totalParkingSpots;


    private double evplTotalProfit;
    private double evplTotalElectricityProcuredkWh;
    private double evplTotalParkingDurationHrs;
    private double evplTotalParkingDurationEVSpotHrs;
    private double evplTotalChargingDurationHrs;

    private double parkingUtil;
    private double chargingUtil;


    public static final CellProcessor[] getProcessors() {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.ENGLISH);
        decimalSymbol.setDecimalSeparator('.');
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalFormatSymbols(decimalSymbol);

        final CellProcessor[] processors = new CellProcessor[]{

                new NotNull(), // arrivalsCount,
                new NotNull(), //departuresCount,
                new NotNull(), //acceptsOnNonEvSpotCount,
                new NotNull(), //acceptsOnEvSpotCount,

                new NotNull(), // acceptsEvParkedOnEvSpotCount;
                new NotNull(), // acceptsEvParkedOnNonEvSpotCount;
                new NotNull(), // acceptsNonEVParkedOnNonEVSpotCount;
                new NotNull(), // acceptsNonEVParkedOnEVSpotCount;

                new NotNull(), //rejectsCount,

                new NotNull(), // currentEvSpotsInUse,
                new NotNull(), // currentNonEvSpotsInUse,

                new FmtNumber(decimalFormat), // evplParkingMoneyAll,
                new FmtNumber(decimalFormat), // evplParkingMoneyEVSpot,
                new FmtNumber(decimalFormat), // evplParkingMoneyNonEVSpot
                new FmtNumber(decimalFormat), // evplElectricityCostFromMarket,
                new FmtNumber(decimalFormat), // evplElectricityMoneyFromEVSpot
                new FmtNumber(decimalFormat), // evplGrossProfitEVSpot;
                new FmtNumber(decimalFormat), // evplGrossProfitEVSpotElectricity;

                new NotNull(), // version
                new FmtNumber(decimalFormat), // chargingFee
                new NotNull(), // evParkingSpots
                new FmtNumber(decimalFormat), // evShare
                new NotNull(), // area
                new NotNull(), //parkingPolicyType

                new NotNull(), // totalParkingSpots
                new FmtNumber(decimalFormat), // evplTotalProfit
                new FmtNumber(decimalFormat), // evplTotalElectricityProcuredkWh
                new FmtNumber(decimalFormat), // evplTotalParkingDurationHrs
                new FmtNumber(decimalFormat), // evplTotalParkingDurationEVSpotHrs
                new FmtNumber(decimalFormat), // evplTotalChargingDurationHrs

                new FmtNumber(decimalFormat), // parkingUtil
                new FmtNumber(decimalFormat) // chargingUtil
        };

        return processors;
    }

    public double getChargingFee() {
        return chargingFee;
    }

    public void setChargingFee(double chargingFee) {
        this.chargingFee = chargingFee;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public double getEvShare() {
        return evShare;
    }

    public void setEvShare(double evShare) {
        this.evShare = evShare;
    }

    public int getEvParkingSpots() {
        return evParkingSpots;
    }

    public void setEvParkingSpots(int evParkingSpots) {
        this.evParkingSpots = evParkingSpots;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static final String[] getHeader() {
        String header[] = new String[]{"arrivalsCount","departuresCount","acceptsOnNonEvSpotCount","acceptsOnEvSpotCount","acceptsEvParkedOnEvSpotCount","acceptsEvParkedOnNonEvSpotCount","acceptsNonEVParkedOnNonEVSpotCount","acceptsNonEVParkedOnEVSpotCount","rejectsCount","currentEvSpotsInUse","currentNonEvSpotsInUse","evplParkingMoneyAll","evplParkingMoneyEVSpot","evplParkingMoneyNonEVSpot","evplElectricityCostFromMarket","evplElectricityMoneyFromEVSpot", "evplGrossProfitEVSpot", "evplGrossProfitEVSpotElectricity","version","chargingFee","evParkingSpots","evShare","area","parkingPolicyType","totalParkingSpots","evplTotalProfit","evplTotalElectricityProcuredkWh", "evplTotalParkingDurationHrs", "evplTotalParkingDurationEVSpotHrs","evplTotalChargingDurationHrs", "parkingUtil", "chargingUtil"};
        return header;
    }

    public double getParkingUtil() {
        return parkingUtil;
    }

    public void setParkingUtil(double parkingUtil) {
        this.parkingUtil = parkingUtil;
    }

    public double getChargingUtil() {
        return chargingUtil;
    }

    public void setChargingUtil(double chargingUtil) {
        this.chargingUtil = chargingUtil;
    }

    public void setParkingPolicyType(String parkingPolicyType) {
        this.parkingPolicyType = parkingPolicyType;
    }

    public String getParkingPolicyType() {
        return parkingPolicyType;
    }

    public void addTotalElectricityProcuredkWh(double procuredkWh){
        this.evplTotalElectricityProcuredkWh+=procuredkWh;
    }

    public void addTotalParkingDurationHrs(double parkingDurationHrs){
        this.evplTotalParkingDurationHrs+=parkingDurationHrs;
    }

    public void addTotalParkingDurationEVSpotHrs(double evplTotalParkingDurationHrs){
        this.evplTotalParkingDurationEVSpotHrs += evplTotalParkingDurationHrs;
    }

    public void addTotalChargingDurationHrs(double chargingDuration) {
        this.evplTotalChargingDurationHrs += chargingDuration;
    }

    public double getEvplTotalChargingDurationHrs() {
        return evplTotalChargingDurationHrs;
    }


    public double getEvplTotalParkingDurationEVSpotHrs() {
        return evplTotalParkingDurationEVSpotHrs;
    }

    public void setAcceptsNonEVParkedOnNonEVSpotCount(int acceptsNonEVParkedOnNonEVSpotCount) {
        this.acceptsNonEVParkedOnNonEVSpotCount = acceptsNonEVParkedOnNonEVSpotCount;
    }

    public void setRejectsCount(int rejectsCount) {
        this.rejectsCount = rejectsCount;
    }

    public void setEvplParkingMoneyEVSpot(double evplParkingMoneyEVSpot) {
        this.evplParkingMoneyEVSpot = evplParkingMoneyEVSpot;
    }

    public void setEvplParkingMoneyNonEVSpot(double evplParkingMoneyNonEVSpot) {
        this.evplParkingMoneyNonEVSpot = evplParkingMoneyNonEVSpot;
    }

    public double getEvplParkingMoneyNonEVSpot() {
        return evplParkingMoneyNonEVSpot;
    }

    public void setEvplElectricityCostFromMarket(double evplElectricityCostFromMarket) {
        this.evplElectricityCostFromMarket = evplElectricityCostFromMarket;
    }

    public void setEvplElectricityMoneyFromEVSpot(double evplElectricityMoneyFromEVSpot) {
        this.evplElectricityMoneyFromEVSpot = evplElectricityMoneyFromEVSpot;
    }

    public double getEvplTotalProfit() {
        return evplTotalProfit;
    }

    public void setEvplTotalProfit(double evplTotalProfit) {
        this.evplTotalProfit = evplTotalProfit;
    }

    public double getEvplTotalElectricityProcuredkWh() {
        return evplTotalElectricityProcuredkWh;
    }

    public void setEvplTotalElectricityProcuredkWh(double evplTotalElectricityProcuredkWh) {
        this.evplTotalElectricityProcuredkWh = evplTotalElectricityProcuredkWh;
    }

    public double getEvplTotalParkingDurationHrs() {
        return evplTotalParkingDurationHrs;
    }

    public void setEvplTotalParkingDurationHrs(double evplTotalParkingDurationHrs) {
        this.evplTotalParkingDurationHrs = evplTotalParkingDurationHrs;
    }

    public int getRejectsCount() {
        return rejectsCount;
    }

    public int getTotalParkingSpots() {
        return totalParkingSpots;
    }

    public void setTotalParkingSpots(int totalParkingSpots) {
        this.totalParkingSpots = totalParkingSpots;
    }

    public int getAcceptsNonEVParkedOnNonEVSpotCount() {
        return acceptsNonEVParkedOnNonEVSpotCount;
    }

    public void incrementAcceptsNonEVParkedOnNonEVCount() {
        acceptsNonEVParkedOnNonEVSpotCount++;
    }


    public double getEvplParkingMoneyEVSpot() {
        return evplParkingMoneyEVSpot;
    }



    public void addEvplParkingMoneyFromNonEVSpot(double amount){
        this.evplParkingMoneyNonEVSpot += amount;
    }

    public void addEvplParkingMoneyFromEVSpot(double amount) {
        this.evplParkingMoneyEVSpot += amount;
    }

    public void addEvplParkingMoneyAll(double amount) {
        evplParkingMoneyAll += amount;
    }

    public void addEvplElectricityCostFromMarket(double amount) {
        evplElectricityCostFromMarket += amount;
    }

    public void addEvplElectricityMoneyFromEV(double amount) {
        evplElectricityMoneyFromEVSpot += amount;
    }

    public void incrementArrivalsCount() {
        arrivalsCount++;
    }

    public void incrementRejectsCount() {
        rejectsCount++;
    }

    public int getRejects() {
        return rejectsCount;
    }

    public void incrementDeparturesCount() {
        departuresCount++;
    }

    public void incrementAcceptsOnNonEvSpotCount() {
        acceptsOnNonEvSpotCount++;
    }

    public void incrementAcceptsOnEvSpotCount() {
        acceptsOnEvSpotCount++;
    }

    public void incrementAcceptsEvParkedOnEvSpotCount(){
        acceptsEvParkedOnEvSpotCount++;
    }

    public int getAcceptsEvParkedOnEvSpotCount() {
        return acceptsEvParkedOnEvSpotCount;
    }

    public void incrementAcceptsNonEVParkedOnEVSpotCount(){
        acceptsNonEVParkedOnEVSpotCount++;
    }

    public int getAcceptsNonEVParkedOnEVSpotCount() {
        return acceptsNonEVParkedOnEVSpotCount;
    }

    public void incrementAcceptsEvParkedOnNonEvCount() {
        acceptsEvParkedOnNonEvSpotCount++;
    }

    public int getArrivalsCount() {
        return arrivalsCount;
    }

    public int getDeparturesCount() {
        return departuresCount;
    }

    public int getAcceptsOnNonEvSpotCount() {
        return acceptsOnNonEvSpotCount;
    }

    public int getAcceptsOnEvSpotCount() {
        return acceptsOnEvSpotCount;
    }

    public int getAcceptsEvParkedOnNonEvSpotCount() {
        return acceptsEvParkedOnNonEvSpotCount;
    }

    public int getCurrentEvSpotsInUse() {
        return currentEvSpotsInUse;
    }

    public int getCurrentNonEvSpotsInUse() {
        return currentNonEvSpotsInUse;
    }

    public double getEvplParkingMoneyAll() {
        return evplParkingMoneyAll;
    }

    public double getEvplElectricityCostFromMarket() {
        return evplElectricityCostFromMarket;
    }

    public double getEvplElectricityMoneyFromEVSpot() {
        return evplElectricityMoneyFromEVSpot;
    }

    public void setCurrentEvSpotsInUse(int currentEvSpotsInUse) {
        this.currentEvSpotsInUse = currentEvSpotsInUse;
    }

    public void setCurrentNonEvSpotsInUse(int currentNonEvSpotsInUse) {
        this.currentNonEvSpotsInUse = currentNonEvSpotsInUse;
    }


    public void setArrivalsCount(int arrivalsCount) {
        this.arrivalsCount = arrivalsCount;
    }

    public void setDeparturesCount(int departuresCount) {
        this.departuresCount = departuresCount;
    }

    public void setAcceptsOnNonEvSpotCount(int acceptsOnNonEvSpotCount) {
        this.acceptsOnNonEvSpotCount = acceptsOnNonEvSpotCount;
    }

    public void setAcceptsOnEvSpotCount(int acceptsOnEvSpotCount) {
        this.acceptsOnEvSpotCount = acceptsOnEvSpotCount;
    }

    public void setAcceptsEvParkedOnNonEvSpotCount(int acceptsEvParkedOnNonEvSpotCount) {
        this.acceptsEvParkedOnNonEvSpotCount = acceptsEvParkedOnNonEvSpotCount;
    }

    public double getEvplGrossProfitEVSpot() {
        return evplGrossProfitEVSpot;
    }

    public double getEvplGrossProfitEVSpotElectricity() {
        return evplGrossProfitEVSpotElectricity;
    }

    public void setEvplGrossProfitEVSpot(double evplGrossProfitEVSpot) {
        this.evplGrossProfitEVSpot = evplGrossProfitEVSpot;
    }

    public void setEvplGrossProfitEVSpotElectricity(double evplGrossProfitEVSpotElectricity) {
        this.evplGrossProfitEVSpotElectricity = evplGrossProfitEVSpotElectricity;
    }

    public void setEvplParkingMoneyAll(double evplParkingMoneyAll) {
        this.evplParkingMoneyAll = evplParkingMoneyAll;
    }

    @Override
    public String toString() {
        return "ValuesHolder{" +
                "arrivalsCount=" + arrivalsCount +
                ", departuresCount=" + departuresCount +
                ", acceptsOnNonEvSpotCount=" + acceptsOnNonEvSpotCount +
                ", acceptsOnEvSpotCount=" + acceptsOnEvSpotCount +
                ", acceptsEvParkedOnEvSpotCount=" + acceptsEvParkedOnEvSpotCount +
                ", acceptsEvParkedOnNonEvSpotCount=" + acceptsEvParkedOnNonEvSpotCount +
                ", acceptsNonEVParkedOnNonEVSpotCount=" + acceptsNonEVParkedOnNonEVSpotCount +
                ", acceptsNonEVParkedOnEVSpotCount=" + acceptsNonEVParkedOnEVSpotCount +
                ", rejectsCount=" + rejectsCount +
                ", currentEvSpotsInUse=" + currentEvSpotsInUse +
                ", currentNonEvSpotsInUse=" + currentNonEvSpotsInUse +
                ", evplParkingMoneyAll=" + evplParkingMoneyAll +
                ", evplParkingMoneyEVSpot=" + evplParkingMoneyEVSpot +
                ", evplParkingMoneyNonEVSpot=" + evplParkingMoneyNonEVSpot +
                ", evplElectricityCostFromMarket=" + evplElectricityCostFromMarket +
                ", evplElectricityMoneyFromEVSpot=" + evplElectricityMoneyFromEVSpot +
                ", evplGrossProfitEVSpot=" + evplGrossProfitEVSpot +
                ", evplGrossProfitEVSpotElectricity=" + evplGrossProfitEVSpotElectricity +
                ", version='" + version + '\'' +
                ", chargingFee=" + chargingFee +
                ", evParkingSpots=" + evParkingSpots +
                ", evShare=" + evShare +
                ", area='" + area + '\'' +
                ", parkingPolicyType='" + parkingPolicyType + '\'' +
                ", totalParkingSpots=" + totalParkingSpots +
                ", evplTotalProfit=" + evplTotalProfit +
                ", evplTotalElectricityProcuredkWh=" + evplTotalElectricityProcuredkWh +
                ", evplTotalParkingDurationHrs=" + evplTotalParkingDurationHrs +
                ", evplTotalParkingDurationEVSpotHrs=" + evplTotalParkingDurationEVSpotHrs +
                ", evplTotalChargingDurationHrs=" + evplTotalChargingDurationHrs +
                ", parkingUtil=" + parkingUtil +
                ", chargingUtil=" + chargingUtil +
                '}';
    }
}
