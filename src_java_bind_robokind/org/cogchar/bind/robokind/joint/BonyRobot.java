/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

import org.robokind.api.motion.AbstractRobot;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionMap;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRobot extends AbstractRobot<BonyJoint> {

	public BonyRobot(Robot.Id robotId) {
		super(robotId);
	}

	@Override public boolean connect() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void disconnect() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override public void move(RobotPositionMap positions) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
