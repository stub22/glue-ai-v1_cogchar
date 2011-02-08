/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.test;

import org.apache.log4j.BasicConfigurator;
import org.cogchar.animoid.calc.curve.PolynomialTest;
import org.cogchar.animoid.calc.number.Float64Funcs;
import org.cogchar.animoid.calc.number.NumberFactory;
import org.jscience.mathematics.number.Float64;

/**
 *
 * @author Stu B.
 */
public class CurveTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
		// Use default config for Log4J.
		BasicConfigurator.configure();
		NumberFactory<Float64> nf = Float64Funcs.getNumberFactory();
		PolynomialTest<Float64> pt = new PolynomialTest<Float64>();
        pt.testPolynomials(nf);
    }

}
