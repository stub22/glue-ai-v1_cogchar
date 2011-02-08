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
public class JointPositionRROM  extends JointPosition {
	public JointPositionRROM(Joint j) {
		super(j);
	}
	public JointPositionRROM(Joint j, double relRomPos) {
		this (j);
		setCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION, relRomPos);
	}
	public JointPositionRROM(JointPosition jp) {
		this(jp.getJoint(), jp.getCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION));
	}
	public double getPosRelROM() {
		return getCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION);
	}
}
