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


import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointPosition;
import org.cogchar.animoid.protocol.JointStateCoordinateType;
import org.cogchar.animoid.protocol.JointVelocityAROMPS;
import static org.cogchar.animoid.protocol.JointStateCoordinateType.*;

/**
 * <p>World joint has a range of motion, specified in degrees, and a sense
 * of positive/negative direction in the world space.  	E.G. is "eyes_right" 
 * a "positive" motion in our world coordinate frame? (Answer: yes!)
 * <br/><br/>
 * This direction refers to the world sense of the logical joint, not the
 * physical sense of a particular underlying servo.  The latter is indicated
 * by a "1" in the low-level servo-config, and is <b>irrelevant</b> to this class.</p>
 * Hint:  The world sense will be the same for all robots!
 * @author Stu Baurmann
 */
public abstract class WorldJoint {

	protected		Double		rangeOfMotionDegrees;

	protected		Joint		myJoint;

	protected		Integer		logicalJointID;

/**
 *
 * @return true if the joint coordinate is inverted w.r.t. world coordinate.
 * (e.g. a gaze joint pointing nominally left or down).  
 *  <br/><br/>The question of whether particular-robot-x has a <b>physically</b> inverted
 *  eyes-right servo (i.e. in servo_config.csv) is irrelevant to this determination,
 * which is based solely on the animoid_config.xml, and which should be the same
 * for all robots using this logical joint.
 **/

	public abstract boolean isWorldSenseInverted();
	
	public Joint getJoint() {
		return myJoint;
	}
	public void setJoint(Joint j) {
		myJoint = j;
	}
	public Integer getLogicalJointID() {
		return logicalJointID;
	}
	public Double getRangeOfMotionDegrees() {
		return rangeOfMotionDegrees;
	}
	protected double getCenterPosROM() {
		JointPosition centerPos = myJoint.getCenterPosition();
		return centerPos.getCoordinateFloat(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
	}
	/** @return Positive value indicating max degrees above center in logical-ROM coordinates */

	protected double getRawMaxDegreesOffset() {
		double centerValROM = getCenterPosROM();
		double highSideRangeROM = 1.0 - centerValROM;
		return highSideRangeROM * rangeOfMotionDegrees;
	}
	/** @return Negative value indicating 'largest' degrees below center in logical-ROM coordinates */
	protected double getRawMinDegreesOffset () {
		double centerValROM = getCenterPosROM();
		return -1.0 * centerValROM * rangeOfMotionDegrees;
	}
	/** @return The closest raw degree offset that is in range. */
	public double getTruncatedInternalDegreesOffset(double wildOffsetDeg) {
		double rawMax = getRawMaxDegreesOffset();
		double rawMin = getRawMinDegreesOffset();
		if (wildOffsetDeg > rawMax) {
			return rawMax;
		} else if (wildOffsetDeg < rawMin) {
			return rawMin;
		} else {
			return wildOffsetDeg;
		}
	}
	/**
	 *
	 * @return positive value indicating max degrees over center in world coord frame.
	 */
	public double getWorldMaxDegreesOffset() {
		
		double worldMaxDeg = (isWorldSenseInverted()) ? -1.0 * getRawMinDegreesOffset() : getRawMaxDegreesOffset();
		return worldMaxDeg;
	}
	/**
	 * @return Negative value indicating 'largest' degrees below center in world coord frame.
	 */

	public double getWorldMinDegreesOffset() {
		return (isWorldSenseInverted()) ? -1.0 * getRawMaxDegreesOffset() : getRawMinDegreesOffset();
	}

	public double getWorldInversionMultiplier() {
		return (isWorldSenseInverted()) ? -1.0 : 1.0;
	}
	public double getTruncatedWorldDegreesOffset(double wildWorldAngleDeg) {
		double mult = getWorldInversionMultiplier();
		return mult * getTruncatedInternalDegreesOffset(mult * wildWorldAngleDeg);
	}
	public double getROM_posForWorldAngleDeg(double worldAngleDeg) {
		double romOffset = getWorldInversionMultiplier() * worldAngleDeg / rangeOfMotionDegrees;
		double centerVal = getCenterPosROM();
		double romPos = centerVal + romOffset;
		return romPos;
	}
	public double getROM_velForWorldAngleSpeed(double worldAngleSpeedDegPS) {
		double romSpeed = getWorldInversionMultiplier() * worldAngleSpeedDegPS / rangeOfMotionDegrees;
		return romSpeed;
	}
	public double getInternalAngleDegForROMJP(JointPosition jp) {
		double posAbsRom = jp.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
		JointPosition centerJP = myJoint.getCenterPosition();
		double centerAbsRom = centerJP.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
		return (posAbsRom - centerAbsRom) * rangeOfMotionDegrees;
	}
	public double getWorldAngleDegForROMJP(JointPosition jp) {
		double internalAngleDeg = getInternalAngleDegForROMJP(jp);
		return getWorldInversionMultiplier() * internalAngleDeg;
	}
	public double getInternalAngleSpeedDegPS_forVelAROMPS(JointPosition velJP) {
		double velRom = velJP.getCoordinateFloat(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC);
		return velRom * rangeOfMotionDegrees;
	}
	public double getWorldAngleSpeedDegPS_forVelAROMPS(JointPosition velJP) {
		double internalAngleSpeed = getInternalAngleSpeedDegPS_forVelAROMPS(velJP);
		return getWorldInversionMultiplier() * internalAngleSpeed;
	}
	public double getRoomAboveWorldAngleDeg(double worldAngleDeg, boolean exceptionIfNeg) {
		double room = getWorldMaxDegreesOffset() - worldAngleDeg;
		if (room < 0.0) {
			throw new RuntimeException("Negative room above worldAngle=" + worldAngleDeg
						+ ", because max=" + getWorldMaxDegreesOffset() + ", worldJoint=" + this);
		}
		return room;
	}
	public double getRoomBelowWorldAngleDeg(double worldAngleDeg, boolean exceptionIfNeg) {
		double room = worldAngleDeg - getWorldMinDegreesOffset();
		if (room < 0.0) {
			throw new RuntimeException("Negative room below worldAngle=" + worldAngleDeg
						+ ", because min=" + getWorldMaxDegreesOffset() + ", worldJoint=" + this);
		}
		return room;
	}
	public JointVelocityAROMPS computeVelForJumpToTruncWorldDeg(double currWorldDeg, double targetWorldDeg, double jumpDurSec) {
		double truncTargetWorldDeg = getTruncatedWorldDegreesOffset(targetWorldDeg);
		double deltaWorldDeg = truncTargetWorldDeg - currWorldDeg;
		double worldDegVelPS = deltaWorldDeg / jumpDurSec;
		double romVelPS = getROM_velForWorldAngleSpeed(worldDegVelPS);
		JointVelocityAROMPS jvel = new JointVelocityAROMPS(getJoint(), romVelPS);
		return jvel;
	}
	public String toString() {
		return "\nWorldJoint["
				+ "\nlogicalJointID=" + getLogicalJointID()
				+ "\nworldSenseInverted=" + isWorldSenseInverted()
				+ "\nrangeDegrees=" + getRangeOfMotionDegrees()
				+ "\nworldMaxDeg=" + getWorldMaxDegreesOffset()
				+ "\nworldMinDeg=" + getWorldMinDegreesOffset()
				+ "\njoint=" + getJoint()
				+ "]";
	}

}
