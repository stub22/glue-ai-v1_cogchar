/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;

import java.util.HashMap;
import java.util.Map;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.number.Number;

/**
 *
 * @author Stu Baurmann
 * Symbolic derivatives of a function for all state variables of a curve.
 * The function may well depend on other variables not in the state of this curve!
 */
public class CAC_FunctionDerivatives<RN extends Number<RN>> {
	private ConstAccelCurve					myCurve;
	private Polynomial						myFunction;
	private Map<String, Polynomial<RN>>	myDerivPolysByVarSymbol = new HashMap<String, Polynomial<RN>>();

	public CAC_FunctionDerivatives(ConstAccelCurve curve, Polynomial polyFunc) {
		myCurve = curve;
		myFunction = polyFunc;
	}
	private Polynomial<RN> getDerivPolyForVarSymbol(String symbol) {
		Polynomial<RN> derivPoly = myDerivPolysByVarSymbol.get(symbol);
		if (derivPoly == null) {
			Polynomial posPoly = myFunction;
			Variable diffVar = posPoly.getVariable(symbol);
			derivPoly = posPoly.differentiate(diffVar);
			myDerivPolysByVarSymbol.put(symbol, derivPoly);
		}
		return derivPoly;
	}
	/*
	public Polynomial<Real> getDerivPolyWRT_TimeOffset() {
		return getDerivPolyForVarSymbol(myCurve.getTimeOffsetSymbol());
	}
	public Polynomial<Real> getDerivPolyWRT_ConstAccel() {
		return getDerivPolyForVarSymbol(myCurve.getConstAccSymbol());
	}
	public Polynomial<Real> getDerivPolyWRT_InitPos()  {
		return getDerivPolyForVarSymbol(myCurve.getInitPosSymbol());
	}
	public Polynomial<Real> getDerivPolyWRT_InitVel()  {
		return getDerivPolyForVarSymbol(myCurve.getInitVelSymbol());
	}
	 */
	public ImmutableStateFrame getDerivStateFrame() {
		// Take derivatives of myFunction WRT each state variable, and collect the
		// values into a new StateFrame.
		return null;
	}
}
