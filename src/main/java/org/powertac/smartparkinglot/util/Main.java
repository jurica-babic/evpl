package org.powertac.smartparkinglot.util;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

	public static void main(String[] args) throws Exception {

		// TODO: extract to method, do tests...
		BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("bn_learn.xml").getFile()));
		WTPBayesSpecification wtpSpecification = WTPBayesSpecification.generateWTPSpecification(br);
		

	}



}
