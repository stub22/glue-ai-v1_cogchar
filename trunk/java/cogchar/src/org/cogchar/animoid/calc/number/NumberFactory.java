/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.number;
import java.lang.reflect.Array;
import org.jscience.mathematics.number.Number;

/**
 * @param <RN>
 * @author Stu Baurmann
 */
public abstract class NumberFactory<RN extends Number<RN>> {
	public abstract RN makeNumberFromDouble(double d);
	public abstract RN getZero();
	public abstract RN getOne();
	public abstract RN getOneHalf();
	public abstract RN[] makeArray(int size);

	public static <SRN> SRN[] makeArrayForClass(Class<SRN>  type, int size) {
		return (SRN[])Array.newInstance(type,size);
	}
}
