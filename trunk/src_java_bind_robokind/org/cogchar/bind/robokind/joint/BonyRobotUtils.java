/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.protocol.FrameSource;
import org.robokind.api.motion.protocol.MotionTargetFrameSource;
import org.robokind.api.motion.utils.MotionUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRobotUtils {
	public static void registerRobokindRobot(Robot robot, BundleContext bundleCtx) throws Exception {
		robot.connect();
		ServiceRegistration reg = MotionUtils.registerRobot(bundleCtx, robot, null);
		if(reg == null){
			 throw new Exception("Error Registering Robot");
		}
		//create and register the MotionTargetFrameSource,
		MotionTargetFrameSource mtfs = new MotionTargetFrameSource(bundleCtx,	robot.getRobotId(), 0.01, null);
		bundleCtx.registerService(FrameSource.class.getName(), mtfs, null);
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions
		mtfs.putPositions(positions);
	}
	public static BonyJoint makeBonyJointForRobot(BonyRobot robot, int jointNum, String jointName, 
					double defaultPos, double initPos) {
		Joint.Id bjID = new Joint.Id(jointNum);
		NormalizedDouble dpND = new NormalizedDouble(defaultPos);
		NormalizedDouble ipND = new NormalizedDouble(initPos);
		BonyJoint bj = new BonyJoint(bjID, jointName, dpND);
		bj.setGoalPosition(ipND);
		robot.registerBonyJoint(bj);
		return bj;
	}
}
