/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

import org.robokind.api.motion.AbstractRobot;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRobot extends AbstractRobot<BonyJoint> {
	static Logger theLogger = LoggerFactory.getLogger(BonyRobot.class);
    private boolean myConnectionFlag;
    
	public BonyRobot(Robot.Id robotId) {
		super(robotId);
	}

	@Override public boolean connect() {
        myConnectionFlag = true;
		theLogger.info("BonyRobot[" + getRobotId() + "] connecting");
		return true;
	}

	@Override public void disconnect() {
        myConnectionFlag = false;
		theLogger.info("BonyRobot[" + getRobotId() + "] disconnecting");
	}

	@Override public void move(RobotPositionMap positions, long lenMillisec) {
		theLogger.info("BonyRobot[" + getRobotId() + "] moving to: " + 
                positions + ", over " + lenMillisec + " milliseconds");
	}
	public void registerBonyJoint(BonyJoint bj) {
		theLogger.info("BonyRobot[" + getRobotId() + "] registering joint: " + bj);
		Robot.JointId brjID = new Robot.JointId(getRobotId(), bj.getId());
		myJointMap.put(brjID, bj);
	}

    @Override
    public boolean isConnected() {
        return myConnectionFlag;
    }
}
