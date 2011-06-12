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

import java.util.HashMap;
import java.util.Map;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.number.Number;

/**
 *
 * @author Stu B. <www.texpedient.com>
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
