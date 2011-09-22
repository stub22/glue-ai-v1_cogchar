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
import org.cogchar.render.opengl.bony.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.DemoBonyWireframeRagdoll;
import org.cogchar.render.opengl.bony.BonyRagdollApp;
import org.cogchar.render.opengl.bony.BonyContext;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobokindJointBindingDemo {
	private	BonyRobot	myBonyRobot;
	
	public void createAndRegisterRobot(BundleContext bundleCtx) throws Exception {
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id("hedonismBot");
		myBonyRobot  = new BonyRobot(hbID);
		BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
	}
	public static class DanceDoer implements DemoBonyWireframeRagdoll.Doer {
		public void doIt() { 
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Doing dance ");
		}
	}
	public void connectToVirtualChar(BundleContext bundleCtx) throws Exception {
		BonyContext bc = CogcharDemoAllBundleActivator.getBonyContext(bundleCtx);
		BonyVirtualCharApp app = bc.getApp();
		BonyRagdollApp bra = (BonyRagdollApp) app;
		DemoBonyWireframeRagdoll dbwr = bra.getRagdoll();
		DanceDoer dd = new DanceDoer();
		dbwr.setDanceDoer(dd);
	}
		
}
