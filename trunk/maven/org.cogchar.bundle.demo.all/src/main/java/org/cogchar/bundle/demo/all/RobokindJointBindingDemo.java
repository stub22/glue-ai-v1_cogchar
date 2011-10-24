/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.demo.all;

import java.io.File;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.osgi.framework.BundleContext;

import org.robokind.api.motion.Robot;


import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyAnimUtils;
import org.cogchar.bind.robokind.joint.BonyRobotFactory;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.model.DemoBonyWireframeRagdoll;
import org.cogchar.render.opengl.bony.app.BonyRagdollApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobokindJointBindingDemo {
	static Logger theLogger = LoggerFactory.getLogger(RobokindJointBindingDemo.class);
	private	Robot		myBonyRobot;
	private	BundleContext	myBundleCtx;
	
	public static String	HARDCODED_ROBOT_ID = "myDevice1";
	
	public void createAndRegisterRobot(BundleContext bundleCtx, File file) throws Exception {
		
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start");
		//Create your Robot and register it
		myBonyRobot  = BonyRobotFactory.buildFromFile(file);
        if(myBonyRobot == null){
            theLogger.warn("Error building Robot from file: " + file.getAbsolutePath());
            return;
        }
		BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
	}
    public void createAndRegisterRobot(BundleContext bundleCtx) throws Exception {
		
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id(HARDCODED_ROBOT_ID);
		myBonyRobot  = new BonyRobot(hbID);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		//BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
	}
	public class DanceDoer implements DemoBonyWireframeRagdoll.Doer {
		
		public void doIt() { 
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Doing dance ");
			try {
				
				BonyAnimUtils.createAndPlayAnim(myBundleCtx);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	public void connectToVirtualChar(BundleContext bundleCtx) throws Exception {
		myBundleCtx = bundleCtx;
		BonyContext bc = CogcharDemoAllBundleActivator.getBonyContext(bundleCtx);
		BonyVirtualCharApp app = bc.getApp();
		BonyRagdollApp bra = (BonyRagdollApp) app;
		DemoBonyWireframeRagdoll dbwr = bra.getRagdoll();
		DanceDoer dd = new DanceDoer();
		dbwr.setDanceDoer(dd);
	}
		
}
