/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
