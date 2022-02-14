# Electric vehicle enabled Parking Lot Simulator

A Java-based tool for analysing electric vehicle enabled parking lot (EVPL) ecosystem which comprises of:

 - an EVPL, 
 - electric vehicles and 
 - the electricity market.

## Imporatant notice
Before you use the simulator, please make sure to read the paper:
 '*A Data-Driven Approach to Managing Electric Vehicle Charging Infrastructure in Parking Lots*' 
 by Jurica Babic, Arthur Carvalho, Wolfgang Ketter and Vedran Podobnik, 
 published in **Transportation Reasearch Part D: Transport and Environment**.

> If you use the simulator in your academic work, you **have to cite the aforementioned paper in your publications**.

## Run

After you build the project (see the `installation instructions` below), use the *fat jar* (`target` folder) to run the simulation experiement. The best place to start is to run the program with the `--help` option so that you get the a sense of all the available options:

    java -jar smartparkinglot-0.0.9-SNAPSHOT-jar-with-dependencies.jar --help

    usage: sim.jar
     -a,--area <arg>                    area you want to use (e.g. Docklands -                               524 spots)
     -cf,--chargingFee <arg>            premium price for EV spots
     -cs,--chargerSpeed <arg>           Charger speed in kW to be used.
     -ev,--evParkingSpots <arg>         number of parking spots with an EV
                                    charger
     -h,--help                          displays a help
     -long,--longitudinalTracking       if set, simulator will have per time
                                    slot values, otherwise, only aggregate
                                    values will be available
     -p,--parkingPolicyType <arg>       parking policy to be used:
                                    PARKING_LOT_DEDICATED_CHARGING,
                                    EV_EXCLUSIVE_SPOTS, FREE_FOR_ALL
     -pd,--printDiagnostics             if set, simulator will print some
                                    diagnostics on standard error (i.e., a
                                    batch processing duration).
     -r,--repetitions <arg>             number of repetitions of a same
                                    scenario with different seed values
     -s,--evShare <arg>                 the proportion of cars that are EVs
     -spots,--totalParkingSpots <arg>   total number of parking spots in EVPL
                                    (EV and non-EV spots combined)

## Installation instructions
> Make sure to install Maven 3.8.1+ and Java 1.8+.

1. Install: `mvn install`
2. open the `target` folder
3. use the *fat jar* (cca 22MB) to run the experiements

## License
See the `LICENSE` file for information about the license.
