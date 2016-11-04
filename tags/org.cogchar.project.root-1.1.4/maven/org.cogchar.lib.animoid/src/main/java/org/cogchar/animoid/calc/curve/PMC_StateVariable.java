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

import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;

public class PMC_StateVariable<StateVarSymbol extends StateVariableSymbol,
				RN extends Number<RN>>  {

	private		StateVarSymbol			mySymbol;
	private		String					myFullName;
	private		Variable<RN>			myPolyVariable;
	public		Polynomial<RN>			myDerivativePoly;


	public PMC_StateVariable(StateVarSymbol symbol, String fullName) {
		super();
		mySymbol = symbol;
		myFullName = fullName;
	}

	public Variable<RN> getPolyVar() {
		if (myPolyVariable == null) {
			myPolyVariable = new Variable.Local<RN>(myFullName);
		}
		return myPolyVariable;
	}
	public void setPolyVar(Variable<RN> pvar) {
		if (myPolyVariable != null) {
			throw new RuntimeException("Cannot change the polyVar on a StateVariable!");
		}
		myPolyVariable = pvar;
	}

	public Polynomial<RN> getDerivativePoly(Polynomial curvePoly) {
		if (myDerivativePoly == null) {
			Variable<RN> diffVar = getPolyVar();
			myDerivativePoly = curvePoly.differentiate(diffVar);
		}
		return myDerivativePoly;
	}

}
