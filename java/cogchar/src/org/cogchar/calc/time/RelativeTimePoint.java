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
class RelativeTimePoint<OffsetSecondsFN extends FieldNumber<OffsetSecondsFN>> extends TimePoint<OffsetSecondsFN> {
	private		TimePoint<OffsetSecondsFN>			myReferencePoint;
	private		OffsetSecondsFN						myOffsetSeconds;
	public RelativeTimePoint(TimePoint<OffsetSecondsFN> refPoint, OffsetSecondsFN offsetSec) {
		myReferencePoint = refPoint;
		offsetSec = myOffsetSeconds;
	}
	@Override public OffsetSecondsFN findMyOffsetToSimilarPoint(TimePoint<OffsetSecondsFN> otherPoint) {
		OffsetSecondsFN		offsetOtherToRef = otherPoint.findMyOffsetToSimilarPoint(myReferencePoint);
		return myOffsetSeconds.minus(offsetOtherToRef);
	}
}
