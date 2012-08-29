/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.bundle.app.puma;



import java.util.List;

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.core.item.Ident;

import org.osgi.framework.BundleContext;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;

import org.cogchar.render.model.bony.FigureState;

import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import org.appdapter.core.log.BasicDebugger;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;

import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.impl.perform.FancyTextChan;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaHumanoidMapper extends BasicDebugger {

	
	private	ModelBlendingRobotServiceContext		myMBRSC;
	private	RobotAnimContext						myRAC;
	
	private	HumanoidRenderContext					myHRC;
	private	Ident									myCharIdent;
	
	public PumaHumanoidMapper(HumanoidRenderContext brc, BundleContext bundleCtx, Ident charIdent) {
		myCharIdent = charIdent;
		myHRC = brc;
		myMBRSC = new ModelBlendingRobotServiceContext(bundleCtx); 
	}
	
	public HumanoidRenderContext getHumanoidRenderContext() { 
		return myHRC;
	}
	public ModelRobot getBonyRobot() { 
		return myMBRSC.getRobot();
	}
	public ModelBlendingRobotServiceContext getRobotServiceContext() { 
		return myMBRSC;
	}
	public FancyTextChan getBestAnimOutChan() { 
		return myRAC.getTriggeringChannel();
	}
	
	public void initModelRobotUsingBoneRobotConfig(BoneRobotConfig brc, final Ident qGraph, final HumanoidConfig hc) throws Throwable {
		// New with "GlobalModes": we'll run hrc.setupHumanoidFigure from here now
		myHRC.setupHumanoidFigure(myCharIdent, qGraph, hc);

		// This creates our ModelRobot instance, and calls registerAndStart() in the RobotServiceContext base class.
		myMBRSC.makeModelRobotWithBlenderAndFrameSource(brc);
		
		myRAC = new RobotAnimContext(myCharIdent, getHumanoidRenderContext().getBonyConfigEmitter().getBehaviorConfigEmitter());
		myRAC.initConn(myMBRSC);
	}

	public void updateModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {	
		ModelRobot targetRobot = getBonyRobot();
		targetRobot.updateConfig(brc);
	}
	public void connectToVirtualChar() throws Exception {
		setupFigureState();
		final ModelRobot br = getBonyRobot();
		br.registerMoveListener(new ModelRobot.MoveListener() {
			@Override public void notifyBonyRobotMoved(ModelRobot br) {
				HumanoidFigure hf = getHumanoidFigure();
				if (hf != null) {
					ModelToFigureStateMappingFuncs.propagateState(br, hf);
				}
			}
			
		});
	}
	
	public void stopAndReset() {
		if (myRAC != null) {
			myRAC.stopAndReset();
		}
	}
	
	public HumanoidFigure getHumanoidFigure() { 
		return myHRC.getHumanoidFigure(myCharIdent);
	}
	
	public void playDangerYogaTestAnim() {
		if (myRAC != null) {
			myRAC.playDangerYogaTestAnimNow();
		}
		
	}
	
	
	public void setupFigureState() { 
		ModelRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<ModelJoint> allJoints = br.getJointList();
		for (ModelJoint mJoint : allJoints) {
			for (BoneProjectionRange bpr : mJoint.getBoneRotationRanges()) {
				String boneName = bpr.getBoneName();
				fs.obtainBoneState(boneName);
			}
		}
		HumanoidFigure hf = getHumanoidFigure();
		hf.setFigureState(fs);
	}
	public void registerFrameReceiver() throws Throwable { 
		ModelRobot mr = getBonyRobot();		
			/*
			 Robot.Id mrid = mr.getRobotId();		
			try {
		        RobotServiceFuncs.createAndRegisterFrameReceiver(bundleCtx, brid);
			} catch (Throwable t) {
				theLogger.warn("Could not register AMQP network server for robot with ID=" + brid, t);
			}
			 */
	}
	
}
