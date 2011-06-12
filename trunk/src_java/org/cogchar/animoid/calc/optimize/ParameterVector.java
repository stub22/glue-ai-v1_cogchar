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

package org.cogchar.animoid.calc.optimize;

import org.cogchar.calc.number.NumberFactory;
import org.jscience.mathematics.function.Function;
import org.jscience.mathematics.number.Number;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ParameterVector<RN extends Number<RN>>  {
	// Values and penalties are both always of length=myLength.
	private		int			myLength;
	// For many purposes only the values matter.
	private		double		myValues[];
	// Penalties are stored here for use when optimizing a ParameterVector.
	// Penalties are generally for values exceeding "reasonable" bounds,
	// and often must be differentiable functions of the underlying param value.
	public		double		myPenalties[];
	public		double		myBulkPenalty;

	private		RN						myNumbers[];
	private		NumberFactory<RN>		myNumberFactory;

	public ParameterVector(NumberFactory<RN> numberFactory) {
		myNumberFactory = numberFactory;
	}
	public void setLength(int l) {
		myLength = l;
		myValues = new double[myLength];
		myPenalties = new double[myLength];
		myNumbers = myNumberFactory.makeArray(myLength);
	}
	public int getLength() {
		return myLength;
	}

	public void setValue(int i, double v) {
		if (v != myValues[i]) {
			myValues[i] = v;
			myNumbers[i] = null;
		}
	}
	public double getValue(int i) {
		return myValues[i];
	}
	public RN getNumber(int i) {
		if (myNumbers[i] == null) {
			myNumbers[i] = myNumberFactory.makeNumberFromDouble(getValue(i));
		}
		return myNumbers[i];
	}
	public void setAllValues(double v) {
		for (int i=0; i < myLength; i++) {
			setValue(i, v);
		}
	}
	public void setPenalty(int i, double v, boolean incremental) {
		if (incremental) {
			myPenalties[i] += v;
		} else {
			myPenalties[i] = v;
		}
	}
	public void setAllPenalties(double pv, boolean incremental) {
		for (int i=0; i < myLength; i++) {
			setPenalty(i, pv, incremental);
		}
	}
	public void readValuesFromArray(double array[], int arrayIndexStart,
				int internalIndexStart, int numVals) {
		for (int idx = 0; idx < numVals; idx++) {
			setValue(internalIndexStart + idx, array[arrayIndexStart + idx]);
		}
	}
	public void writeValuesToArray(double array[], int arrayIndexStart,
				int internalIndexStart, int numVals) {
		for (int idx = 0; idx < numVals; idx++) {
			array[arrayIndexStart + idx] = getValue(internalIndexStart + idx);
		}
	}

	public String toString() {
		return "ParameterVector[values=" + doubleArrayToString(myValues)
				+ ",\npenalties=" + doubleArrayToString(myPenalties) + "]";
	}
	public double getPenalty(int i) {
		return myPenalties[i];
	}
	public void applyPenaltyFunction(Function pfunc) {
		throw new RuntimeException("Not Implemented");
	}
	public double sumLeadingValues(int numVals) {
		double sum = 0.0;
		for (int i=0; i < numVals; i++) {
			sum += getValue(i);
		}
		return sum;
	}
	public Integer findIndexWithinImpliedSum(double val) {
		double sum = 0.0;
		for (int idx=0; idx < myLength; idx++) {
			sum += getValue(idx);
			if (sum >= val) {
				return idx;
			}
		}
		return null;
	}
	public double sumValues() {
		return sumLeadingValues(myLength);
	}
	public double sumIndividualPenalties() {
		double sum = 0.0;
		for (int i=0; i < myLength; i++) {
			sum += getPenalty(i);
		}
		return sum;
	}

	public double totalPenalties() {
		return sumIndividualPenalties() + myBulkPenalty;
	}
	public void reduceAllAndPenalizeProportionally(double reductAmt, double penaltyMult) {
		throw new RuntimeException("Not Implemented");
	}

	/* TODO:  Read values to/from JSCience math objects, also possibly to/from
	 * JointPos at some point.
	 */
	public static String doubleArrayToString(double array[]) {
		StringBuffer buffer = new StringBuffer("[(length=" + array.length + ")");
		for (int i=0; i < array.length; i++) {
			buffer.append(array[i]);
			if (i < array.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append("]");
		return buffer.toString();
	}
	public static double sumSquaredDoubleArray(double arr[]) {
		double sum= 0.0;
		for (int i=0; i < arr.length; i++) {
			sum  += arr[i] * arr[i];
		}
		return sum;
	}
}
