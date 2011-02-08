/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.number;

import org.jscience.mathematics.number.Float64;


/**
 *
 * @author Stu Baurmann
 */
public class Float64Funcs {
	public static Float64 makeFloat64(double doubleValue) {
		return Float64.valueOf(doubleValue);
	}
	public static NumberFactory<Float64> getNumberFactory() {
		return new NumberFactory<Float64> () {
			@Override public Float64 getZero() {
				return Float64.ZERO;
			}
			@Override public Float64 getOne() {
				return Float64.ONE;
			}
			@Override public Float64 getOneHalf() {
				return Float64.valueOf(0.5);
			}
			@Override public Float64 makeNumberFromDouble(double d) {
				return makeFloat64(d);
			}
			@Override public Float64[] makeArray(int size) {
				return makeArrayForClass(Float64.class, size);
			}
		};
	}
}
