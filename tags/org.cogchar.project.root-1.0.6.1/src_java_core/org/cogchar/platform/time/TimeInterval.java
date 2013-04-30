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

package org.cogchar.platform.time;

import org.jscience.mathematics.number.FieldNumber;



/**
 *
 * @author Stu B.  <www.texpedient.com>
 *
 * Duration must be positive <--->   End point must be after start point.
 */
public class TimeInterval<SecondsFN extends FieldNumber<SecondsFN>> {
	private		ExactTimePoint<SecondsFN>		myStartPoint;
	private		ExactTimePoint<SecondsFN>		myEndPoint;
	private		SecondsFN					myDurationSec;

	public TimeInterval(ExactTimePoint<SecondsFN> start, ExactTimePoint<SecondsFN> end) {
		myStartPoint = start;
		myEndPoint = end;
	}
	public TimeInterval(ExactTimePoint<SecondsFN> start, SecondsFN duration) {
		myStartPoint = start;
		myDurationSec = duration;
	}
	public TimeInterval(SecondsFN duration, ExactTimePoint<SecondsFN> end) {
		myDurationSec = duration;
		myEndPoint = end;
	}
	public ExactTimePoint<SecondsFN> getStartPoint() {
		if (myStartPoint == null) {
			
		}
		return null;
	}
}
