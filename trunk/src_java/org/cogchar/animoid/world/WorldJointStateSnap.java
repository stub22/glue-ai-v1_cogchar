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


package org.cogchar.animoid.world;

import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointPosition;
import static org.cogchar.animoid.protocol.JointStateCoordinateType.*;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class WorldJointStateSnap {
	private WorldJoint			myWorldJoint;
	// Note that JointPosition objects (and Frames) can represent abs/rel position, velocity, accel...
	private JointPosition		myPositionJP;
	private JointPosition		myVelocityJP;
	public WorldJointStateSnap(WorldJoint wj, JointPosition posJP, JointPosition velJP) {
		super();
		myWorldJoint = wj;
		setPositionJP(posJP);
		setVelocityJP(velJP);
	}
	public WorldJointStateSnap(WorldJoint wj, Frame posFrame, Frame velFrame) {
		this(wj, posFrame.getJointPositionForJoint(wj.getJoint()),
				velFrame.getJointPositionForJoint(wj.getJoint()));
	}
	public Joint getJoint() {
		WorldJoint wj = getWorldJoint();
		return (wj != null) ? wj.getJoint() : null;
	}
	public WorldJoint getWorldJoint() {
		return myWorldJoint;
	}
	public void setPositionJP(JointPosition posJP) {
		myPositionJP = posJP;
		double posAbsRom = myPositionJP.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
		JointPosition centerJP = getJoint().getCenterPosition();
		double centerAbsRom = centerJP.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
	}
	public void setVelocityJP(JointPosition velJP) {
		myVelocityJP = velJP;
	}

	public double getPosAbsRom() {
		return myPositionJP.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
	}

	public double getPosInternalDegOffCenter() {
		return getWorldJoint().getInternalAngleDegForROMJP(myPositionJP);
	}
	public double getWorldPosDeg() {
		return getWorldJoint().getWorldAngleDegForROMJP(myPositionJP);
	}

	public JointPosition getPositionJP() {
		return myPositionJP;
	}

	public double getTotalRomDegrees() {
		return getWorldJoint().getRangeOfMotionDegrees();
	}

	public double getInternalVelDegPerSec() {
		return getWorldJoint().getInternalAngleSpeedDegPS_forVelAROMPS(myVelocityJP);
	}
	public double getWorldVelDegPerSec() {
		return getWorldJoint().getWorldAngleSpeedDegPS_forVelAROMPS(myVelocityJP);
	}
	public double getVelRomPerSec() {
		return myVelocityJP.getCoordinateFloat(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC);
	}

	public JointPosition getVelocityJP() {
		return myVelocityJP;
	}

	public double getRoomAbovePosInWorldDeg() {
		return getWorldJoint().getRoomAboveWorldAngleDeg(getWorldPosDeg(), true);
	}
	public double getRoomBelowPosInWorldDeg() {
		return getWorldJoint().getRoomBelowWorldAngleDeg(getWorldPosDeg(), true);
	}
	public String toString() {
		return "GJLS[joint=" + getJoint()
			+ ", romTotalDeg=" + getTotalRomDegrees()
			+ ", posAbsRom=" + getPosAbsRom()
			+ ", posDegOffCenter=" + getPosInternalDegOffCenter()
			+ ", velRomPS=" + getVelRomPerSec()
			+ ", velDegPS=" + getInternalVelDegPerSec() + "]";
	}
	public String getShortDescriptiveName() {
		// During unit tests we sometimes do not have a joint object
		Joint j = getJoint();
		if (j != null) {
			return j.getJointName();
		} else if (myWorldJoint != null) {
			return "J" + myWorldJoint.getLogicalJointID();
		} else {
			return String.valueOf(this.hashCode());
		}
	}
}
