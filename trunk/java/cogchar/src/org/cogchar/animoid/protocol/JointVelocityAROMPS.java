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
import static org.cogchar.animoid.protocol.JointStateCoordinateType.*;
/**
 * The extension to JointPosition is a temporary hack.
 * For the moment this is just a marker subclass.
 * But soon it will extend JointStateItem instead of JointPosition.
 * @author Stu Baurmann
 */
public class JointVelocityAROMPS extends JointPosition {
	public JointVelocityAROMPS(Joint j) {
		super(j);
	}
	public JointVelocityAROMPS(Joint j, double velAbsRomPerSec) {
		this (j);
		setCoordinateFloat(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC, velAbsRomPerSec);
	}

	public static JointVelocityAROMPS makeVelocityAsRateOfPositionChange(JointPosition prevPos, JointPosition nextPos, double timeSec) {
		double rate = computeRateOfChange(FLOAT_ABS_RANGE_OF_MOTION, prevPos, nextPos, timeSec);
		return new JointVelocityAROMPS(prevPos.getJoint(), rate);
	}

	public double getVelAROMPS() {
		return getCoordinateFloat(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC);
	}
}
