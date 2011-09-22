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
		Robot robot = new BonyRobot(hbID);
		BonyJoint bj1 = new BonyJoint
		BonyRobotUtils.registerRobokindRobot(robot, bundleCtx);
	}


}
