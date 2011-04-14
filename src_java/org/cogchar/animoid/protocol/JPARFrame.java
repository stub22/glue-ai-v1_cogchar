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

package org.cogchar.animoid.protocol;

import java.util.List;

/**
 *
 * @author Stu Baurmann
 */
public class JPARFrame extends Frame<JointPositionAROM> {
	// Frames must contain same joints, and all coords must be abs-rom
	public JPRRFrame computeRelFrame(JPARFrame targetPosAR) {
		JPRRFrame resultF = new JPRRFrame();
		List<JointPositionAROM> currAbsJPs = targetPosAR.getAllPositions();
		for (JointPositionAROM currAbsJP : currAbsJPs) {
			Joint j = currAbsJP.getJoint();
			JointPositionAROM prevJP = getJointPositionForJoint(j);
			JointPositionRROM relJP = prevJP.computeDeltaRelPos(currAbsJP);
			resultF.addPosition(relJP);
		}
		return resultF;
	}
	public static JPARFrame makeFrom(Frame f) {
		// Currently this does not "convert" from another coordinate system, e.g. lopsided.
		// It simply rewraps/retypes the arom coordinates in weakly-typed frame f.
		if (f instanceof JPARFrame) {
			return (JPARFrame) f;
		}
		JPARFrame jparf = new JPARFrame();
		List<JointPosition> jplist = f.getAllPositions();
		for(JointPosition jp : jplist) {
			JointPositionAROM jpar;
			if (jp instanceof JointPositionAROM) {
				jpar = (JointPositionAROM) jp;
			} else {
				jpar = new JointPositionAROM(jp);
			}
			jparf.addPosition(jpar);
		}
		return jparf;
	}

}
