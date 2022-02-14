package org.powertac.smartparkinglot.util;

public class Key {
	
	private int SOC;
	private int RP;
	private int S;
	
	public Key(int soc, int rp, int s) {
		super();
		SOC = soc;
		RP = rp;
		S = s;
	}
	
	public int getRP() {
		return RP;
	}
	
	public int getS() {
		return S;
	}
	
	public int getSOC() {
		return SOC;
	}
	
 
}
