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
