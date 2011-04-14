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

package org.cogchar.animoid.calc.curvematrix;

import org.slf4j.Logger;
import org.cogchar.calc.number.NumberFactory;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.structure.Field;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu Baurmann
 */
public class RampingFramedCACM<RN extends Number<RN> & Field<RN>> extends ConstAccelCurveMatrix<RN> {
	private static Logger		theLogger = LoggerFactory.getLogger(RampingFramedCACM.class.getName());



	public RampingFramedCACM(NumberFactory<RN> numFact) {
		super(numFact);
	}

	public RampingFramedCurveSeq makeSequence(String name) {
		RampingFramedCurveSeq cs = new RampingFramedCurveSeq(name, myNumberFactory);
		addSequence(cs);
		return cs;
	}
	// Both args must be nonnegative, yo?
	public static int minFramesToChangeSpeed(double totalDeltaVelRequired,
				double maxDeltaVelPerFrame) {
		if (totalDeltaVelRequired < 0.0) {
			throw new RuntimeException("Negative deltaVelReq: " + totalDeltaVelRequired);
		}
		if (maxDeltaVelPerFrame < 0.0) {
			throw new RuntimeException("Negative maxDeltaVel: " + maxDeltaVelPerFrame);
		}
		double ratio = totalDeltaVelRequired / maxDeltaVelPerFrame;
		return (int) Math.ceil(ratio);
	}
	public int minFramesToStopAllSeqsFromInitVel() {
		int maxMin = 0;
		for (ConstAccelCurveSequence<RN> cacs : getSequences()) {
			RampingFramedCurveSeq cs = (RampingFramedCurveSeq) cacs;
			int csMinBrakeFrames = cs.minFramesToStopFromInitVel();
			if (csMinBrakeFrames > maxMin) {
				maxMin = csMinBrakeFrames;
			}
		}
		return maxMin;
	}
}
