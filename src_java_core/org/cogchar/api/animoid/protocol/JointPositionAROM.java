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
package org.cogchar.api.animoid.protocol;
import static org.cogchar.api.animoid.protocol.JointStateCoordinateType.*;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class JointPositionAROM extends JointPosition {
	private static double EQUALITY_TOLERANCE  = 0.0001;

	public JointPositionAROM(Joint j) {
		super(j);
	}
	public JointPositionAROM(Joint j, double absRomPos) {
		this (j);
		setCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION, absRomPos);
	}
	public JointPositionAROM(JointPosition jp) {
		this(jp.getJoint(), jp.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION));
	}
	public double getPosAbsROM() {
		return getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
	}
	// Compute the delta to get from here to targetJP.
	// Both this and targetJP must be Abs-ROM
	public JointPositionRROM computeDeltaRelPos(JointPositionAROM targetJP) {
		double absPos = getPosAbsROM();
		double targetAbsPos = targetJP.getPosAbsROM();
		double moveDelta = targetAbsPos - absPos;
		return new JointPositionRROM(getJoint(), moveDelta);
	}
	public boolean equals(Object other) {
		if ((other != null) && (other instanceof JointPositionAROM)) {
			JointPositionAROM  otherJPAR = (JointPositionAROM) other;
			double otherCoord = otherJPAR.getPosAbsROM();
			double meCoord = getPosAbsROM();
			double diff = otherCoord - meCoord;
			double absDiff = Math.abs(diff);
			if (absDiff < EQUALITY_TOLERANCE) {
				return true;
			}
		}
		return false;
	}
}
