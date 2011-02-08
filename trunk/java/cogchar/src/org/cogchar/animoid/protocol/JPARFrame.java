/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
