/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.app.puma;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.robokind.joint.BlendingBonyRobotContext;
import org.cogchar.bind.robokind.joint.BlendingRobotContext;
import org.cogchar.bind.robokind.joint.BoneRotationRange;
import org.cogchar.bind.robokind.joint.BoneRotationRange.BoneRotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter {
	static Logger theLogger = LoggerFactory.getLogger(PumaDualCharacter.class);
	private	BlendingBonyRobotContext	myBBRC;
	

	
	public PumaDualCharacter(BundleContext bundleCtx) {
		myBBRC = new BlendingBonyRobotContext(bundleCtx);
	}
	public void setupBonyRobotWithBlender(File jointBindingConfigFile) throws Exception {
		myBBRC.makeBonyRobotWithBlenderAndFrameSource(jointBindingConfigFile);
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
				BonyStateMappingFuncs.propagateState(br, bc);
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
		return myBBRC.getRobot();
	}
	public void setupFigureState(BonyContext bctx) { 
		BonyRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<BonyJoint> allJoints = br.getJointList();
		for (BonyJoint bj : allJoints) {
            for(BoneRotationRange range : bj.getBoneRotationRanges()){
                String name = range.getBoneName();
                fs.obtainBoneState(name);
            }
		}
		bctx.setFigureState(fs);
	}
	
    

}
