/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.demo.all;

import java.nio.channels.Channel;
import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.robokind.joint.BonyJoint;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.osgi.framework.BundleContext;

import org.robokind.api.motion.Robot;


import org.cogchar.bind.robokind.joint.BonyRobotUtils;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobokindJointBindingDemo {

	public static void createAndRegisterRobot(BundleContext bundleCtx) throws Exception {
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id("hedonismBot");
		BonyRobot robot = new BonyRobot(hbID);
		BonyRobotUtils.makeBonyJointForRobot(robot, 22, "JTwentyTwo", 0.5, 0.2);
		BonyRobotUtils.makeBonyJointForRobot(robot, 22, "JNinetyNine", 0.8, 0.9);
		BonyRobotUtils.registerRobokindRobot(robot, bundleCtx);
	}


}
