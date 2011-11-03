/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.demo.all;

import java.io.File;
import org.osgi.framework.BundleContext;



import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;

import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyRobotFactory;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.cogchar.bind.robokind.joint.BonyJoint;


import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;

import java.util.List;

import org.cogchar.bind.robokind.joint.JointRotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobokindJointBindingDemo {
	static Logger theLogger = LoggerFactory.getLogger(RobokindJointBindingDemo.class);
	private	BundleContext	myBundleCtx;
	private	BonyRobot		myBonyRobot;
	private	FigureState		myFigureState;

	
	public static String	HARDCODED_ROBOT_ID = "myDevice1";
	
	public Robot createAndRegisterRobot(BundleContext bundleCtx, File jointBindingConfigFile) throws Exception {
		String bindingFilePath = jointBindingConfigFile.getAbsolutePath();
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start, using file: " + bindingFilePath);
		//Create your Robot and register it
		myBonyRobot  = BonyRobotFactory.buildFromFile(jointBindingConfigFile);
        if(myBonyRobot == null){
            theLogger.warn("Error building Robot from file: " + bindingFilePath);
            return null;
        }
		BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
        return myBonyRobot;
	}
    public void createAndRegisterRobot(BundleContext bundleCtx) throws Exception {
		myBundleCtx = bundleCtx;
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id(HARDCODED_ROBOT_ID);
		myBonyRobot  = new BonyRobot(hbID);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		//BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
	}
	/*public class DanceDoer implements DemoBonyWireframeRagdoll.Doer {
		
		public void doIt() { 
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Doing dance ");
			try {
				
				BonyAnimUtils.createAndPlayAnim(myBundleCtx);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}*/
	public void connectToVirtualChar(final BonyContext bc) throws Exception {
		BonyVirtualCharApp app = bc.getApp();
		setupFigureState(bc);
		final BonyRobot br = getBonyRobot();
		br.registerMoveListener(new BonyRobot.MoveListener() {
			@Override public void notifyBonyRobotMoved(BonyRobot br) {
				propagateState(br, bc);
			}
			
		});
		/*
		BonyRagdollApp bra = (BonyRagdollApp) app;
		DemoBonyWireframeRagdoll dbwr = bra.getRagdoll();
		DanceDoer dd = new DanceDoer();
		dbwr.setDanceDoer(dd);
		 */
	}

	public BonyRobot getBonyRobot() { 
		return myBonyRobot;
	}
	public void setupFigureState(BonyContext bctx) { 
		BonyRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<Joint> allJoints = br.getJointList();
		for (Joint cursorJoint : allJoints) {
			BonyJoint bj = (BonyJoint) cursorJoint;
			String boneName = bj.getBoneName();
			BoneState bs = fs.obtainBoneState(boneName);
		}
		bctx.setFigureState(fs);
	}
	public static void propagateState(BonyRobot br, BonyContext bc) { 
		FigureState fs = bc.getFigureState();
		for (BoneState bs : fs.getBoneStates()) {
			String boneName = bs.getBoneName();
			List<BonyJoint> bjList = BonyRobotUtils.findJointsForBoneName(br, boneName);
            JointRotation rot = null;
			for (BonyJoint bj : bjList) {
                rot = JointRotation.add(bj.getGoalAngleRad(), rot);
			}
            bs.rot_X_pitch = (float)rot.getPitch();
            bs.rot_Y_roll = (float)rot.getRoll();
            bs.rot_Z_yaw = (float)rot.getYaw();
		}
	}
}
