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
public class JPRRFrame extends Frame<JointPositionRROM> {

	public JVFrame makeVelFrame(double moveDurationSec) {
		JVFrame velFrame = new JVFrame();
		for (JointPositionRROM jprr : getAllPositions()) {
			double relPos = jprr.getPosRelROM();
			double vel = relPos / moveDurationSec;
			JointVelocityAROMPS jointVel = new JointVelocityAROMPS(jprr.getJoint(), vel);
			velFrame.addPosition(jointVel);
		}
		return velFrame;
	}

	public static JPRRFrame make(Frame f) {
		if (f instanceof JPRRFrame) {
			return (JPRRFrame) f;
		}
		JPRRFrame jprrf = new JPRRFrame();
		List<JointPosition> jplist = f.getAllPositions();
		for(JointPosition jp : jplist) {
			JointPositionRROM jprr;
			if (jp instanceof JointPositionRROM) {
				jprr = (JointPositionRROM) jp;
			} else {
				jprr = new JointPositionRROM(jp);
			}
			jprrf.addPosition(jprr);
		}
		return jprrf;
	}
}
