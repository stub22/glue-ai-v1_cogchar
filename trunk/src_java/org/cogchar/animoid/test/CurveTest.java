/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.animoid.test;

import org.apache.log4j.BasicConfigurator;
import org.cogchar.animoid.calc.curve.PolynomialTest;
import org.cogchar.calc.number.Float64Funcs;
import org.cogchar.calc.number.NumberFactory;
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
