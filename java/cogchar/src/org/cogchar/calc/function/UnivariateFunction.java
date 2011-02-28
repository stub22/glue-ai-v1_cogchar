/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.calc.function;

/**
 *
 * @author Stu Baurmann
 */
public interface UnivariateFunction <DomainType, RangeType> {
	public RangeType getOutputForInput(DomainType inputValue);
}
