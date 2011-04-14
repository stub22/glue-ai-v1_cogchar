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

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.calc.function.BumpUF;
import org.cogchar.calc.number.NumberFactory;
import org.cogchar.calc.number.RealFuncs;
import org.cogchar.animoid.calc.optimize.ParameterVector;


import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.structure.Field;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.apache.log4j.BasicConfigurator;
/**
 *
 * @author Stu Baurmann
 */
public class CurveMatrixTest<RN extends Number<RN> & Field<RN>>{
	private static Logger	theLogger = LoggerFactory.getLogger(CurveMatrixTest.class.getName());
	private NumberFactory<RN>	myNumberFactory;

	CurveMatrixTest(NumberFactory<RN> numFact) {
		myNumberFactory = numFact;
	}
	public void testSDCACM() {
		SharedDurationCACM<RN>   sdcacm = new SharedDurationCACM(myNumberFactory);
		sdcacm.addEmptySequenceForName("jointOne");
		sdcacm.addEmptySequenceForName("jointTwo");
		sdcacm.appendInterval();
		sdcacm.appendInterval();
		sdcacm.appendInterval();

		ParameterVector<RN> durVec = new ParameterVector<RN>(myNumberFactory);
		durVec.setLength(3);
		durVec.setValue(0, 0.5);
		durVec.setValue(1, 2.0);
		durVec.setValue(2, 1.5);
		sdcacm.setDurations(durVec);

		ParameterVector<RN> accVec1 = new ParameterVector<RN>(myNumberFactory);
		accVec1.setLength(3);
		accVec1.setValue(0, 3.0);
		accVec1.setValue(1, 0.0);
		accVec1.setValue(2, -3.0);

		ConstAccelCurveSequence<RN> seq1 = sdcacm.getSequences().get(0);
		ConstAccelCurveSequence<RN> seq2 = sdcacm.getSequences().get(1);

		seq1.setAccelParams(accVec1);
		seq2.setAccelParams(accVec1);

		RN initPos1 = myNumberFactory.makeNumberFromDouble(17.2);
		RN initVel1 = myNumberFactory.makeNumberFromDouble(0.0);
		seq1.setInitialConditions(initPos1, initVel1);
		RN initPos2 = myNumberFactory.makeNumberFromDouble(0.0);
		RN initVel2 = myNumberFactory.makeNumberFromDouble(-5.0);
		seq2.setInitialConditions(initPos2, initVel2);

		seq1.propagateEndpointConditions();
		seq2.propagateEndpointConditions();

		theLogger.info("sdcacm: " + sdcacm.dumpSamples(20, 4.5));

		BumpUF<RN, RN> buf1 = seq1.getBumpFunction();
		dumpBump(buf1, 20, 4.5);
		BumpUF<RN, RN> buf2 = seq2.getBumpFunction();
		dumpBump(buf2, 20, 4.5);

	}
	public void dumpBump(BumpUF<RN, RN> bumpFunc, int sampleCount, double lastSampleTime) {
		double sampleWidth = lastSampleTime / (sampleCount -1);
		for (int idx = 0; idx < sampleCount; idx++) {
			double sampleTime = idx * sampleWidth;
			RN sampleTimeRN = myNumberFactory.makeNumberFromDouble(sampleTime);
			RN samplePos = bumpFunc.getOutputForInput(sampleTimeRN);
			RN sampleVel = bumpFunc.getDerivativeAtInput(sampleTimeRN, 1);
			theLogger.info("t=" + sampleTimeRN + ", x=" + samplePos + ", v=" + sampleVel);
		}
	}
	public static void main(String args[]) {
		try {
			// Use default config for Log4J.
			BasicConfigurator.configure();
			NumberFactory<Real> rnf = RealFuncs.getRealNumberFactory();
			CurveMatrixTest<Real> cmt = new CurveMatrixTest<Real>(rnf);
			cmt.testSDCACM();
		} catch (Throwable t) {
			theLogger.error("Caught: ", t);
		}
	}
}
