/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;
import org.jscience.mathematics.number.Number;


/**
 * @param <StateVarSymbol>
 * @param <RN>
 * @author Stu Baurmann
 */
public class ImmutableStateFrame<StateVarSymbol extends StateVariableSymbol,
			RN extends Number<RN>> {
	private		double		myStateValues[];
	
	protected ImmutableStateFrame(int stateCount) {
		myStateValues = new double[stateCount];
	}
	protected void initValueAtStateIndex(int idx, double v) {
		myStateValues[idx] = v;
	}
	protected void initValueForSymbol(StateVarSymbol sym, double v) {
		myStateValues[sym.getSymbolIndex()] = v;
	}	
	public int getStateCount() {
		return myStateValues.length;
	}
	public double[] getStateValuesArray() {
		return myStateValues;
	}
	public double getValueAtStateIndex(int idx) {
		return myStateValues[idx];
	}

}
