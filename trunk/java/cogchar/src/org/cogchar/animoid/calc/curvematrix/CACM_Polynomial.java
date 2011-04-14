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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.Float64Vector;

/**
 * @author Stu Baurmann
 *
 * A single polynomial function depending on (some subset of) all the curves in a CACM.
 * Derivatives are symbolic polynomial derivs WRT all state variables of the curves.
 */
public class CACM_Polynomial {
	ConstAccelCurveMatrix							myCurveMatrix;
	Polynomial<Real>								myPolynomial;
	Map<ConstAccelCurve, CAC_FunctionDerivatives>	myDerivativesMap;

	public CACM_Polynomial(ConstAccelCurveMatrix curveMatrix, Polynomial func) {
		myCurveMatrix = curveMatrix;
		myPolynomial = func;
		buildDerivativesMap();

	}
	private void buildDerivativesMap() {
		myDerivativesMap = new HashMap<ConstAccelCurve, CAC_FunctionDerivatives>();
		Collection<ConstAccelCurveSequence> seqs = myCurveMatrix.getSequences();
		for (ConstAccelCurveSequence seq: seqs) {
			int stepCount = seq.getStepCount();
			for (int i = 0; i < stepCount; i++) {
				ConstAccelCurve curve = seq.getStepCurve(i);
				CAC_FunctionDerivatives derivs = new CAC_FunctionDerivatives(curve, myPolynomial);
				myDerivativesMap.put(curve, derivs);
			}
		}
	}

	private Float64Vector getDerivativeValuesAtCurrentPos() {
		return null;
	}

}
