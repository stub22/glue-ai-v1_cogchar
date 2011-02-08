/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
