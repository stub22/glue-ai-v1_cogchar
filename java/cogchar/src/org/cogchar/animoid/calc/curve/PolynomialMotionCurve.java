/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;

import org.cogchar.animoid.calc.number.NumberFuncs;
import org.cogchar.animoid.calc.function.SmoothUF;
import java.util.ArrayList;
import java.util.List;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;

import org.jscience.mathematics.number.Number;

/**
 * This curve is a function of any number of variables, but it must specifically
 * be differentiable in one well-known variable for each StateVarSymbol value.
 * These known variables are called the StateVariables, and we hold a JScience
 * Variable reference and a derivative polynomial for each one.
 *
 * @param <StateVarSymbol>
 * @param <RN>
 * @author Stu Baurmann
 */
public class PolynomialMotionCurve<StateVarSymbol extends StateVariableSymbol,
				RN extends Number<RN>> {

	protected	Polynomial<RN>									myCurvePoly;
	  // To get array type, could try this
	  // public T[] asArray( Class<T> type) {
      // T[] array = (T[])Array. newInstance(type,size) ; // unchecked cast
	protected	List<PMC_StateVariable<StateVarSymbol, RN>>		myStateVars;
	protected	String											mySymbolSuffix;

	public PolynomialMotionCurve(String symbolSuffix) {
		mySymbolSuffix = symbolSuffix;
	}
	public void setCurvePoly(Polynomial c) {
		if (myCurvePoly != null) {
			// Must null out all derivatives, for starters...
			throw new RuntimeException("Changing poly on a curve not yet supported!");
		}
		myCurvePoly = c;
	}
	public Polynomial<RN> getCurvePoly() {
		return myCurvePoly;
	}

	protected PMC_StateVariable getStateVariable(StateVarSymbol symbol) {
		int varIndex = symbol.getSymbolIndex();
		if (myStateVars == null) {
			int symCount = symbol.getSymbolBlockSize();
			myStateVars = new ArrayList<PMC_StateVariable<StateVarSymbol, RN>>(symCount);
			for (int i=0; i < symCount; i++) {
				myStateVars.add(null);
			}
		}
		if (myStateVars.get(varIndex) == null) {
			String fullName = 	symbol.getSymbolString() + mySymbolSuffix;
			PMC_StateVariable<StateVarSymbol, RN> pmcsv = new PMC_StateVariable
					<StateVarSymbol, RN>(symbol, fullName);
			myStateVars.set(varIndex, pmcsv);
		}
		return myStateVars.get(varIndex);
	}
	public void setStateVarVal(StateVarSymbol symbol, RN val) {
		getPolyVar(symbol).set(val);
	}
	public RN getStateVarVal(StateVarSymbol symbol) {
		return getPolyVar(symbol).get();
	}
	protected Variable<RN> getPolyVar(StateVarSymbol symbol) {
		PMC_StateVariable sv = getStateVariable(symbol);
		return sv.getPolyVar();
	}
	protected void useExistingPolyVar(StateVarSymbol symbol, Variable<RN> pvar) {
		PMC_StateVariable sv = getStateVariable(symbol);
		sv.setPolyVar(pvar);
	}

	public String getFullSymbolStringForStateVar(StateVarSymbol svs) {
		return svs.getSymbolString() + mySymbolSuffix;
	}
	public void readStateFromFrame(ImmutableStateFrame frame) {
		// 
	}
	public ImmutableStateFrame getStateFrame() {
		return null;
	}
	public ImmutableStateFrame getDerivativeStateFrame() {
		return null;
	}
	protected Polynomial<RN> getDerivPolyForStateVarSymbol(StateVarSymbol symbol) {
		PMC_StateVariable sv = getStateVariable(symbol);
		return sv.getDerivativePoly(myCurvePoly);
	}
	public RN evalCurvePolyAtCurrentState() {
		return NumberFuncs.evalPoly(getCurvePoly());
	}
	public RN evalDerivPolyAtCurrentState(StateVarSymbol symbol) {
		Polynomial<RN> derivPoly = getDerivPolyForStateVarSymbol(symbol);
		return NumberFuncs.evalPoly(derivPoly);
	}
	/*
	 * Let y = output of this curve, and suppose derivativeOrder = 3
	 * Let y' = derivWRT_timeOffset of y
	 * @return   coeff((y''' - centerPoint)^power)
	 */
	public Polynomial<RN> makeOutputDerivativeNormPoly(int derivativeOrder,
			RN centerPoint, int power, RN coeff) {
		return null;
	}
	public SmoothUF<RN, RN> getFiniteTimeCurve() {
		// The issue is that FTC calls for random-access evaluation in time,
		// while this PMC is tied to a set of state variables.
		return null;
	}
}
