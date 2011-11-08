/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.protocol.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRobotUtils {
	public static void registerRobokindRobot(Robot robot, BundleContext bundleCtx) throws Exception {
		robot.connect();
		ServiceRegistration reg = RobotUtils.registerRobot(bundleCtx, robot, null);
		if(reg == null){
			 throw new Exception("Error Registering Robot");
		}
        //Starts a blender
        ServiceRegistration[] blenderRegs = RobotUtils.startDefaultBlender(
                bundleCtx, robot.getRobotId(), RobotUtils.DEFAULT_BLENDER_INTERVAL);
		//create and register the MotionTargetFrameSource,
        RobotFrameSource robotFS = new RobotFrameSource(bundleCtx, robot.getRobotId());
        RobotUtils.registerFrameSource(bundleCtx, robot.getRobotId(), robotFS);
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions
        
        //moves to the positions over 1.5 seconds
        robotFS.move(positions, 1500);
	}
}
