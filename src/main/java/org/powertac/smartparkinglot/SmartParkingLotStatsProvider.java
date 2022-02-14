package org.powertac.smartparkinglot;

public interface SmartParkingLotStatsProvider {

	public int getEvCurrentlyParked();
	
	public int getIcvCurrentlyParked();

	public int getArrivalsCount();

	public int getDeparturesCount();

	public int getAcceptsOnNonEvSpotCount();

	public int getAcceptsOnEvSpotCount();

	public int getAcceptsEvParkedOnNonEvCount();

	public double getSplParkingMoney();

	public double getSplElectricityMoney();

	public double getEvElectricityMoney();

}
