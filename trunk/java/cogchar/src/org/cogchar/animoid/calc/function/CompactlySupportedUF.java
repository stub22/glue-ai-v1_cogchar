/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.function;

/**
 *
 * @author humankind
 */
public interface CompactlySupportedUF <DomainType, RangeType>
			extends UnivariateFunction<DomainType, RangeType> {
	public	DomainType getSupportLowerBound();
	public	DomainType getSupportUpperBound();
}
