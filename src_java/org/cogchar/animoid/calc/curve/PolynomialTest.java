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

package org.cogchar.animoid.calc.curve;

import org.cogchar.calc.number.NumberFactory;
import java.util.logging.Level;
import org.cogchar.calc.number.PolyFuncs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;


/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class PolynomialTest<RN extends Number<RN>> {
	private static Logger	theLogger = LoggerFactory.getLogger(PolynomialTest.class.getName());

	public void testPolynomials(NumberFactory<RN> numFact) {

		Variable<RN> rangeOffsetTimeVar = new Variable.Local<RN>("tOff");


		ConstAccelCurve		mc1 = new ConstAccelCurve<RN>("1", rangeOffsetTimeVar, numFact);
		ConstAccelCurve		mc2 = new ConstAccelCurve<RN>("2", rangeOffsetTimeVar, numFact);

		Polynomial<RN> mc1Poly = mc1.getCurvePoly();
		Polynomial<RN> mc2Poly = mc2.getCurvePoly();
		Polynomial<RN> sumPoly = mc1Poly.plus(mc2Poly);
		Polynomial<RN> sqSumPoly = sumPoly.times(sumPoly);
		Polynomial<RN> integSqSumPoly = sqSumPoly.integrate(rangeOffsetTimeVar);

		mc1.configureWithDoubles(0.4, -10.0, -5.0, 4.0);
		mc2.configureWithDoubles(2.2, 3.0, -2.0, -5.0);

		boolean		printFlag = true;
		double pos1_1 = PolyFuncs.evalPrintReturnPoly(mc1Poly, "pos1_1", theLogger, Level.INFO, printFlag);
		double pos2_1 = PolyFuncs.evalPrintReturnPoly(mc2Poly, "pos2_1", theLogger, Level.INFO, printFlag);
		double sqsum_1 = PolyFuncs.evalPrintReturnPoly(sqSumPoly, "sqSum_1", theLogger, Level.INFO, printFlag);

		rangeOffsetTimeVar.set(numFact.makeNumberFromDouble(100.0));

		double pos1_100 = PolyFuncs.evalPrintReturnPoly(mc1Poly, "pos1_100", theLogger, Level.INFO, printFlag);
		double pos2_100 = PolyFuncs.evalPrintReturnPoly(mc2Poly, "pos2_100", theLogger, Level.INFO, printFlag);
		double sqsum_100 = PolyFuncs.evalPrintReturnPoly(sqSumPoly, "sqSum_100", theLogger, Level.INFO, printFlag);

		mc1.configureWithDoubles(7.5, -0.1, 100.0, 1.0);
		mc2.configureWithDoubles(12.5, -20.0, -20.0, 2.0);

		double pos1p_10 = PolyFuncs.evalPrintReturnPoly(mc1Poly, "pos1p_10", theLogger, Level.INFO, printFlag);
		double pos2p_10 = PolyFuncs.evalPrintReturnPoly(mc2Poly, "pos2p_10", theLogger, Level.INFO, printFlag);
		double sqsumP_10 = PolyFuncs.evalPrintReturnPoly(sqSumPoly, "sqSumP_10", theLogger, Level.INFO, printFlag);

		// What we want to do is curry the integral function, right?
		double integSqSumP_10 = PolyFuncs.evalPrintReturnPoly(integSqSumPoly, "integSqSumP_10", theLogger, Level.INFO, printFlag);

		rangeOffsetTimeVar.set(numFact.makeNumberFromDouble(0.0));

		double integSqSumP_0 = PolyFuncs.evalPrintReturnPoly(integSqSumPoly, "integSqSumP_0", theLogger, Level.INFO, printFlag);

		double definiteIntegSqSumP_0_10 = integSqSumP_10 - integSqSumP_0;

		theLogger.info("definite integral value = " + definiteIntegSqSumP_0_10);

		// rangeAccelVar.set(RealFuncs.makeReal(5.0));
		// finalPos = posPoly.evaluate();

		// theLogger.info("Bumped accel up to 5.0, and result is now: " + finalPos);

		/*
		double fixedTargetPosDeg;
		double startTimeSec;
		double endTimeSec;

		// theLogger.finest("Computing isqDist to " + fixedTargetPosDeg
		//		+ " over [" + startTimeSec + "," + endTimeSec + "]");
		// The zero point of this time variable is at our range start.

		// Summation polynomial for the position of the gaze in degrees in this time range.
		Polynomial<Real> plannedPosPolyDeg = Polynomial.valueOf(Real.ZERO, Term.ONE);
		// This period must lie within a single plan-interval on all joints.
		// Each such planInterval can supply us with a degree-position polynomial valid for the given range.
		for (GazeJointMotionPlan gjmp: myJointPlans) {
			Polynomial<Real> jointPosPoly = gjmp.getPositionPolynomialForTimeRange(startTimeSec, endTimeSec,
					rangeOffsetTimeVar);
		if (checkTimeHit(startTimeSec) && checkTimeHit(endTimeSec)) {
			Real rangeStartPos = RealFuncs.makeReal(getPosAtTime(startTimeSec));
			Real rangeStartVel = RealFuncs.makeReal(getVelAtTime(startTimeSec));
			Real rangeAccel = RealFuncs.makeReal(getAccelDegPSPS());
			// x = x0 + v0t + .5 a t^2
			Term parabolicTerm = Term.valueOf(relativeTimeVar, 2);
			Real oneHalf = RealFuncs.makeReal(0.5);
			Real parabolicCoeff = oneHalf.times(rangeAccel);
			Polynomial<Real> parabolicPart  = Polynomial.valueOf(parabolicCoeff, parabolicTerm);
			Polynomial<Real> linearPart  = Polynomial.valueOf(rangeStartVel, relativeTimeVar);
			Polynomial<Real> motionPart = linearPart.plus(parabolicPart);
			Polynomial<Real> fullPosPoly = motionPart.plus(rangeStartPos);
			return fullPosPoly;
			// theLogger.finest("JointPoly=" + jointPosPoly);
			plannedPosPolyDeg = plannedPosPolyDeg.plus(jointPosPoly);
		}
		// theLogger.finest("Creating real value for targetPos=" + fixedTargetPosDeg);
		Real targetPos = RealFuncs.makeReal(fixedTargetPosDeg);
		// Currently we just use the fixed target gaze location, but we could instead use
		// a designed "swoosh" to guide our gaze on a particular trejectory.
		Polynomial<Real> targetPoly = Polynomial.valueOf(targetPos, Term.ONE);
		Polynomial<Real> signedErrPoly = plannedPosPolyDeg.minus(targetPoly);
		Polynomial<Real> squaredErrPoly = signedErrPoly.times(signedErrPoly);
		Polynomial<Real> integSqErrPoly = squaredErrPoly.integrate(rangeOffsetTimeVar);
		// theLogger.finest("Integrated squared error polynomial=" + integSqErrPoly);
		rangeOffsetTimeVar.set(Real.ZERO);
		Real startVal = integSqErrPoly.evaluate();
		double duration = endTimeSec - startTimeSec;
		rangeOffsetTimeVar.set(RealFuncs.makeReal(duration));
		Real endVal = integSqErrPoly.evaluate();
		Real integralSum = endVal.minus(startVal);
		// theLogger.finest("Integral sum=" + integralSum + ", start=" + startVal + ", endVal=" + endVal);
		return integralSum.doubleValue();
		 */
	}
}
