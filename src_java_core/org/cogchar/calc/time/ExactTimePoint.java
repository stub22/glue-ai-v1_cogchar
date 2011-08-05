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

package org.cogchar.calc.time;

import org.jscience.mathematics.number.FieldNumber;



/**
 * Positive offset seconds indicates "later than" a reference point.
 * @author winston
 */
public abstract class ExactTimePoint<OffsetSecondsFN extends FieldNumber<OffsetSecondsFN>> {

	public abstract OffsetSecondsFN findMyOffsetToReferencePoint(ExactTimePoint<OffsetSecondsFN> refPoint) ;

	public ExactTimePoint<OffsetSecondsFN> addOffsetSeconds(OffsetSecondsFN offset) {
		return new RelativeTimePoint<OffsetSecondsFN>(this, offset);
	}

}
