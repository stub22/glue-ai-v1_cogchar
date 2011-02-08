/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.protocol;
import static org.cogchar.animoid.protocol.JointStateCoordinateType.*;
/**
 *
 * @author Stu Baurmann
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
