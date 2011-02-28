/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.calc.time;

import org.jscience.mathematics.number.FieldNumber;



/**
 *
 * @author winston
 *
 * Duration must be positive <--->   End point must be after start point.
 */
public class TimeInterval<SecondsFN extends FieldNumber<SecondsFN>> {
	private		TimePoint<SecondsFN>		myStartPoint;
	private		TimePoint<SecondsFN>		myEndPoint;
	private		SecondsFN					myDurationSec;

	public TimeInterval(TimePoint<SecondsFN> start, TimePoint<SecondsFN> end) {
		myStartPoint = start;
		myEndPoint = end;
	}
	public TimeInterval(TimePoint<SecondsFN> start, SecondsFN duration) {
		myStartPoint = start;
		myDurationSec = duration;
	}
	public TimeInterval(SecondsFN duration, TimePoint<SecondsFN> end) {
		myDurationSec = duration;
		myEndPoint = end;
	}
	public TimePoint<SecondsFN> getStartPoint() {
		if (myStartPoint == null) {
			
		}
		return null;
	}
}
