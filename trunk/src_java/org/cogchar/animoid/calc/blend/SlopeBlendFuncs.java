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

package org.cogchar.animoid.calc.blend;

import org.cogchar.animoid.protocol.JPRRFrame;
import org.cogchar.animoid.protocol.JointPositionRROM;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SlopeBlendFuncs {

	public static JointPositionRROM computeActualDeltaJP(JointPositionRROM naiveDeltaJP,
				JointPositionRROM goalDeltaJP, double goalAllowanceCoeff,
				double fixedAllowance) {

		double naiveDelta = naiveDeltaJP.getPosRelROM();
		double goalDelta = goalDeltaJP.getPosRelROM();
		double maxMag = goalAllowanceCoeff * Math.abs(goalDelta) + fixedAllowance;
		double actualMag = Math.min(maxMag, Math.abs(naiveDelta));
		double actualDelta = Math.signum(naiveDelta) * actualMag;
		return new JointPositionRROM(naiveDeltaJP.getJoint(), actualDelta);
	}
	public static JPRRFrame computeActualDeltaFrame(
				JPRRFrame naiveDeltaFrame, JPRRFrame goalDeltaFrame,
				double goalAllowanceCoeff, double fixedAllowance) {
		JPRRFrame resultFrame = new JPRRFrame();
		for (JointPositionRROM naiveJPRR : naiveDeltaFrame.getAllPositions()) {
			JointPositionRROM goalJPRR = goalDeltaFrame.getJointPositionForJoint(naiveJPRR.getJoint());
			JointPositionRROM actualJPRR = computeActualDeltaJP(naiveJPRR, goalJPRR, goalAllowanceCoeff, fixedAllowance);
			resultFrame.addPosition(actualJPRR);
		}
		return resultFrame;
	}
}
