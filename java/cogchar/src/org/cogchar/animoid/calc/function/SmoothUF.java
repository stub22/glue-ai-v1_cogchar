/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.function;

/**
 *  The timeline of a SmoothUF always starts at 0.0.
 * @author humankind
 */
public interface SmoothUF<DomainType, RangeType>
			extends UnivariateFunction<DomainType, RangeType> {

	public RangeType getDerivativeAtInput(DomainType inputValue, int derivativeOrder);
}
