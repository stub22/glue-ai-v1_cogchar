/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.calc.time;

import org.jscience.mathematics.number.FieldNumber;



/**
 *
 * @author winston
 */
public abstract class TimePoint<OffsetSecondsFN extends FieldNumber<OffsetSecondsFN>> {

	public abstract OffsetSecondsFN findMyOffsetToSimilarPoint(TimePoint<OffsetSecondsFN> otherPoint) ;

	public TimePoint<OffsetSecondsFN> addOffsetSeconds(OffsetSecondsFN offset) {
		return new RelativeTimePoint<OffsetSecondsFN>(this, offset);
	}
}
