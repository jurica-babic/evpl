package org.powertac.smartparkinglot;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ec.util.MersenneTwisterFast;
import sim.util.distribution.Exponential;

public class ExponentialTest {

	public static final double ALPHA = 0.05;

	@Test
	public void testExponential() {
		Exponential exp = new Exponential(0, new MersenneTwisterFast(1));
		double d = exp.nextDouble();
		assertTrue("This thing is infinity and we are able to check it.",  Double.isInfinite(d));
		d+=1;
		System.err.println(d);
	}

	@Test
	public void testExponentialLow(){
		Exponential exp = new Exponential(0.0001, new MersenneTwisterFast(1));
		System.out.println(exp.nextDouble());
	}

	@Test
	public void testExponentialDepartureRate(){
		final int SIZE = 1000;
		double value = 3;

		Exponential exp = new Exponential(value, new MersenneTwisterFast());
		double d = exp.nextDouble();


		SummaryStatistics values = new SummaryStatistics();

		for(int i=0; i<SIZE; i++){
			values.addValue(exp.nextDouble());

		}
		assertFalse(TestUtils.tTest(1/value, values, ALPHA));
		System.err.println("Expected:"+value+" Actual:"+1/values.getMean());
	}



}
