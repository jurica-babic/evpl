package org.powertac.smartparkinglot.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathUtils;
import sim.util.distribution.Empirical;
import sim.util.distribution.EmpiricalWalker;

/**
 * Contains all relevant info for EV willingness to pay model. Nominal values are coded from 0, e.g.:
 * soc - 0,1,2 means LOW,MEDIUM,HIGH SOC.
 * <p>
 * It is expected that init method will be called before the start of a simulation run. With that method, a random number generator as well as DescriptiveStatistics object containing all the prices from the electricity market will be passed in.
 *
 * @author Jurica
 */
public class WTPBayesSpecification {

    public static final int INVALID = -1;
    public static final double MEDIUM = 10.0;
    public static final int SLOW = 3;
    private MultiKeyMap<Integer, double[]> probabilityMap;

    private int socCardinality;
    private int priceCardinality;
    private int speedCardinality;
    private int wtpCardinality;
    private DescriptiveStatistics priceStatistics;
    private double[] pricePercentiles;

    private MultiKeyMap<Integer, EmpiricalWalker> empiricalWalkerMap;


    public WTPBayesSpecification(MultiKeyMap<Integer, double[]> probabilityMap, int socCardinality, int priceCardinality,
                                 int speedCardinality, int wtpCardinality) {
        super();
        this.probabilityMap = probabilityMap;
        this.socCardinality = socCardinality;
        this.priceCardinality = priceCardinality;
        this.speedCardinality = speedCardinality;
        this.wtpCardinality = wtpCardinality;


        init(new MersenneTwisterFast(), new DescriptiveStatistics());

    }

    public static WTPBayesSpecification cloneMothafucka(WTPBayesSpecification original){
        MultiKeyMap<Integer, double[]> probabilityMapNew = new MultiKeyMap<>();
        Set<MultiKey<? extends Integer>> keys = original.probabilityMap.keySet();
        for (MultiKey<? extends Integer> key : keys) {
            probabilityMapNew.put(key, original.getProbabilityMap().get(key));
        }
        int socCardinality = original.getSocCardinality();
        int priceCardinality = original.getPriceCardinality();
        int speedCardinality = original.getSpeedCardinality();
        int wtpCardinality = original.getWtpCardinality();


        WTPBayesSpecification newWtpBayesSpecification = new WTPBayesSpecification(original.getProbabilityMap(), socCardinality, priceCardinality, speedCardinality, wtpCardinality);
        return newWtpBayesSpecification;
    }

    /**
     * To be called upon initialization. This will allow us to instantiate distributions depending on pdf's. To be called before the sim run.
     *
     * @param random
     */
    public void init(MersenneTwisterFast random, DescriptiveStatistics priceStatistics) {
        empiricalWalkerMap = new MultiKeyMap<>();
        this.priceStatistics = priceStatistics;

        Set<MultiKey<? extends Integer>> keys = probabilityMap.keySet();
        for (MultiKey<? extends Integer> key : keys) {
            empiricalWalkerMap.put(key, new EmpiricalWalker(probabilityMap.get(key), Empirical.NO_INTERPOLATION, random));
        }

        // find percentiles:
        pricePercentiles = new double[priceCardinality];
        if (priceStatistics.getN() != 0) {
            for (int i = 0; i < getPriceCardinality(); i++) {
                double k = ((i + 1.0d) / priceCardinality) * 100;
                pricePercentiles[i] = priceStatistics.getPercentile(k);
            }
        }

    }

    public DescriptiveStatistics getPriceStatistics() {
        return priceStatistics;
    }

    public int queryWTPGivenEvidence(int socDiscreteEvidence, int priceDiscreteEvidence, int speedDiscreteEvidence) {
        if (socDiscreteEvidence < 0 || priceDiscreteEvidence < 0 || speedDiscreteEvidence < 0 || socDiscreteEvidence >= getSocCardinality() || priceDiscreteEvidence >= getPriceCardinality() || speedDiscreteEvidence >= getSpeedCardinality()) {
            return INVALID;
        }

        EmpiricalWalker wtp = empiricalWalkerMap.get(socDiscreteEvidence, priceDiscreteEvidence, speedDiscreteEvidence);
        if(wtp == null){
            System.err.println("ZAJEB kod soc:"+socDiscreteEvidence+" price:"+priceDiscreteEvidence+" speed:"+speedDiscreteEvidence);
        }
        return wtp.nextInt();
    }

    /**
     * Converts a continous SOC into a discrete one.
     *
     * @param continuousSOC a continuous SOC
     * @return a discrete SOC, returns -1 if there is something wrong
     */
    public int socToDiscreteSOC(double continuousSOC) {

        if (continuousSOC < 0) {
            return INVALID;
        }

        int currentCardinality = 0;
        double split = 1.0d / socCardinality;

        while (currentCardinality < socCardinality) {
            if (continuousSOC > split) {
                currentCardinality++;
                split += 1.0d / socCardinality;
            } else {
                return currentCardinality;
            }
        }
        // if you end up here, return the maximum SOC discrete value available
        return getSocCardinality() - 1;
    }

    /**
     * PLEASE NOTE: current implementation differentiates only three levels of chargers!! Update CONSTANT's values with grounded values from the literature.
     *
     * @param contionousKWh
     * @return
     */
    public int speedToSpeedDiscrete(double contionousKWh) {
        if (contionousKWh < 0) {
            return INVALID;
        } else if (contionousKWh < SLOW) {
            return 0;
        } else if (contionousKWh < MEDIUM) {
            return 1;
        } else {
            return 2;
        }
    }

    public int priceToDiscretePrice(double continousPriceKWh) {
        // prices are internally stored as per MWh so we need to convert per kWh to mWh
        double queryPrice = continousPriceKWh * 1000;

        if (queryPrice < 0) {
            return INVALID;
        } else {
            // find an interval where queryPrice resides
            for (int candidateDiscreteValue = 0; candidateDiscreteValue < pricePercentiles.length; candidateDiscreteValue++) {
                double percentile = pricePercentiles[candidateDiscreteValue];
                if (queryPrice <= percentile) {
                    return candidateDiscreteValue;
                }
            }
        }
        // if you end up here, return the maximum price discrete value available
        return getPriceCardinality() - 1;
    }


    /**
     * Is there a match between EV owner and EVPL regarding the charging service.
     *
     * @param socNominalEvidence
     * @param priceDiscreteEvidence
     * @param speedDiscreteEvidence
     * @param reservationPriceDiscrete price offered by EVPL, nominal (discretized) value
     * @return
     */
    public boolean isMatch(int socNominalEvidence, int priceDiscreteEvidence, int speedDiscreteEvidence, int reservationPriceDiscrete) {
        int wtpNominal = queryWTPGivenEvidence(socNominalEvidence, priceDiscreteEvidence, speedDiscreteEvidence);
        if (wtpNominal >= reservationPriceDiscrete) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return THREE KEYS IN THE FOLLOWING ORDER OF KEYS: 1.soc, 2.price, 3.speed.
     * Each of keys has a certain cardinality. Nominal values of such start from 0.
     */
    public MultiKeyMap<Integer, double[]> getProbabilityMap() {
        return probabilityMap;
    }


    public int getPriceCardinality() {
        return priceCardinality;
    }

    public int getSocCardinality() {
        return socCardinality;
    }

    public int getSpeedCardinality() {
        return speedCardinality;
    }

    public int getWtpCardinality() {
        return wtpCardinality;
    }


    @Override
    public String toString() {
        return "WTPSpecification [probabilityMap=" + probabilityMap + ", socCardinality=" + socCardinality
                + ", priceCardinality=" + priceCardinality + ", speedCardinality=" + speedCardinality + ", wtpCardinality=" + wtpCardinality + "]";
    }

    public static WTPBayesSpecification generateWTPSpecification(String inputFile) {

        try {
            URL url = ClassLoader.getSystemResource(inputFile);
            if(url == null){
                return null;
            }
            String file = url.getFile();
            BufferedReader br = new BufferedReader(new FileReader(file));
            return generateWTPSpecification(br);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static WTPBayesSpecification generateWTPSpecification(BufferedReader br)
            throws NumberFormatException, IOException, Exception {
        String wtpExpression = "probability ( wtp | ";

        String line;

        boolean isWTPPeriodActive = false;

        HashMap<String, Integer> socMap = new HashMap<>();
        HashMap<String, Integer> priceMap = new HashMap<>();
        HashMap<String, Integer> speedMap = new HashMap<>();
        HashMap<String, List<Double>> wtpMap = new HashMap<>();
        MultiKeyMap<Integer, double[]> multiKeyMap = new MultiKeyMap<>();

        int wtpCardinality = 0;

        List<HashMap<String, Integer>> listMaps = new ArrayList<>();
        List<String> availableVars = new ArrayList<>();

        while ((line = br.readLine()) != null) {

            if (line.contains("variable soc {")) {
                // get the content of the soc variable
                line = br.readLine();
                fillMap(line, socMap);

            } else if (line.contains("variable price {")) {
                // get the content of the price variable
                line = br.readLine();
                fillMap(line, priceMap);

            } else if (line.contains("variable speed {")) {
                // get the content of the speed variable
                line = br.readLine();
                fillMap(line, speedMap);
            } else if (line.contains(wtpExpression)) {
                String[] inDependentVariables = line.substring(line.indexOf("|") + 1, line.indexOf(" ) {")).split(",");
                for (String var : inDependentVariables) {
                    var = var.trim();
                    // get the order of variables:
                    if (var.equals("soc")) {
                        availableVars.add("soc");
                        listMaps.add(socMap);
                    } else if (var.equals("price")) {
                        availableVars.add("price");
                        listMaps.add(priceMap);
                    } else if (var.equals("speed")) {
                        availableVars.add("speed");
                        listMaps.add(speedMap);
                    }
                }
                isWTPPeriodActive = true;
            } else if (isWTPPeriodActive) {
                if (line.contains("}")) {
                    //System.out.println("FINISHED!");
                } else {
                    // get two parts: variables and values:
                    String[] parts = line.split("\\) ");
                    String vars = parts[0];
                    vars = vars.replace("(", "").trim();
                    String probs = parts[1].replace(";", "").trim();

                    // generate key:
                    String[] keyParts = vars.split(", ");
                    String generatedKey = "";
                    for (int i = 0; i < keyParts.length; i++) {
                        generatedKey += listMaps.get(i).get(keyParts[i]);
                    }

                    // generate double array:
                    String[] probsParts = probs.split(", ");
                    List<Double> probList = new ArrayList<>();
                    for (String probPart : probsParts) {
                        probList.add(Double.parseDouble(probPart));
                    }
                    double[] probArray = probList.stream().mapToDouble(Double::doubleValue).toArray();

                    wtpCardinality = probArray.length;

                    // DIRTY HACK, force:
                    if (keyParts.length == 3) {
                        String var0 = availableVars.get(0);
                        int code0 = listMaps.get(0).get(keyParts[0]);

                        String var1 = availableVars.get(1);
                        int code1 = listMaps.get(1).get(keyParts[1]);

                        String var2 = availableVars.get(2);
                        int code2 = listMaps.get(2).get(keyParts[2]);

                        HashMap<String, Integer> mapper = new HashMap<>();
                        mapper.put(var0, code0);
                        mapper.put(var1, code1);
                        mapper.put(var2, code2);

                        // garuantee order of variables: SOC, PRICE, SPEED
                        multiKeyMap.put(mapper.get("soc"), mapper.get("price"), mapper.get("speed"), probArray);
                    } else {
                        throw new Exception("WTP does not depend on three variables");
                    }

                    wtpMap.put(generatedKey, probList);
                    // System.out.println("BEFORE:");
                    // System.out.println(vars+" "+probs);
                    // System.out.println("AFTER");
                    // System.out.println(generatedKey+" "+ probList);

                }
            }
        }
        //System.out.println(wtpMap);


        WTPBayesSpecification wtpSpecification = new WTPBayesSpecification(multiKeyMap, socMap.size(), priceMap.size(),
                speedMap.size(), wtpCardinality);

        return wtpSpecification;

    }

    private static void fillMap(String line, HashMap<String, Integer> map) {

        String[] values = line.substring(line.indexOf("{") + 1, line.indexOf("}")).split(",");
        int i = 0;
        for (String value : values) {
            value = value.trim();
            map.put(value, i++);
        }

    }


}
