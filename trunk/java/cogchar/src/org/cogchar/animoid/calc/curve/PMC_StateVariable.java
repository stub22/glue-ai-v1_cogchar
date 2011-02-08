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
