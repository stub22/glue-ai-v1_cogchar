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

import org.cogchar.animoid.calc.curve.*;
import java.util.Set;
import java.util.Vector;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.RationalFunction;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.number.Number;
/**
 *
 * @author Stu Baurmann
 */
public class SDCACM_Interval<RN extends Number<RN>> {
	public Integer						myIntervalNum;
	public Variable<RN>					myTimeOffsetVar;
	public Set<ConstAccelCurve<RN>>		myCurves;
	public Polynomial<RN>				myPositionSumPoly;

	public SDCACM_Interval(Integer intervalNum, String nameSuffix) {
		myIntervalNum = intervalNum;
		myTimeOffsetVar =  new Variable.Local<RN>("_t_" + nameSuffix);
	}
	public Variable<RN> getTimeOffsetVariable() {
		return myTimeOffsetVar;
	}
	public void setCurveSet(Set<ConstAccelCurve<RN>> curveSet) {
		myCurves = curveSet;
		for (ConstAccelCurve curve: myCurves) {
			Polynomial<RN> curvePoly = curve.getCurvePoly();
			if (myPositionSumPoly == null) {
				myPositionSumPoly = curvePoly;
			} else {
				myPositionSumPoly = myPositionSumPoly.plus(curvePoly);
			}
		}
	}
	public Vector<RationalFunction<Real>> getSumPositionGradientWRT_InitPos() {
		return null;
	}
	public Vector<RationalFunction<Real>> getSumPositionGradientWRT_InitVel() {
		return null;
	}
	public Vector<RationalFunction<Real>> getSumPositionGradientWRT_ConstAcc() {
		return null;
	}
	public RationalFunction<Real> getSumPositionGradientWRT_TimeOffset() {
		return null;
	}

}
