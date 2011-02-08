/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.number;

import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.structure.Field;


/**
 *
 * @author Stu Baurmann
 */
public class FieldNumber<RN extends Number<RN> & Field<RN>> {
	public	RN		value;

	public double getDoubleValue() {
		return value.doubleValue();
	}
}
