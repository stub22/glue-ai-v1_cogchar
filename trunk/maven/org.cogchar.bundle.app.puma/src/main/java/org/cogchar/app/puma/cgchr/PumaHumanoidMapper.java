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
package org.cogchar.app.puma.cgchr;



import java.util.List;

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.core.name.Ident;

import org.osgi.framework.BundleContext;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;

import org.cogchar.render.model.bony.FigureState;

import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClient;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;

import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.bundle.app.puma.PumaVirtualWorldMapper;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaHumanoidMapper extends BasicDebugger {

	
	private	ModelBlendingRobotServiceContext		myMBRSC;
	private	RobotAnimContext						myRAC;
	
	private	PumaVirtualWorldMapper					myVWorldMapper;
	private	Ident									myCharIdent;
	
	public PumaHumanoidMapper(PumaVirtualWorldMapper vWorldMapper, BundleContext bundleCtx, Ident charIdent) {
		myCharIdent = charIdent;
		myVWorldMapper = vWorldMapper;
		myMBRSC = new ModelBlendingRobotServiceContext(bundleCtx); 
	}
	
	public HumanoidRenderContext getHumanoidRenderContext() { 
		HumanoidRenderContext hrc = null;
		if (myVWorldMapper != null) { 
			hrc = myVWorldMapper.getHumanoidRenderContext();
		}
		return hrc;
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
	/**
	 * 
	 * @param qi
	 * @param brc
	 * @param qGraph
	 * @param hc
	 * @param behavCE
	 * @return true  if the "boneRobot" is "OK".  That means it is animatable, but it may or may not have a VWorld humanoid figure.
	 * @throws Throwable 
	 */
	public boolean initModelRobotUsingBoneRobotConfig(RepoClient qi, BoneRobotConfig brc, final Ident qGraph, final HumanoidConfig hc,
					BehaviorConfigEmitter behavCE) throws Throwable {
		if (myVWorldMapper != null) {
			HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
			// New with "GlobalModes": we'll run hrc.setupHumanoidFigure from here now
			HumanoidFigure hf = hrc.setupHumanoidFigure(qi, myCharIdent, qGraph, hc);
		} //	else -	getLogger().warn("initModelRobotUsingBoneRobotConfig() aborted setup due to null HumanoidFigure for {}", myCharIdent);

		if (brc != null) {
			// This creates our ModelRobot instance, and calls registerAndStart() in the RobotServiceContext base class.
			myMBRSC.makeModelRobotWithBlenderAndFrameSource(brc);
			if (behavCE != null) {
				// This gives us an animation triggering context, connecting behavior system to animation system.
				myRAC = new RobotAnimContext(myCharIdent, behavCE);
				// Connect the triggering RobotAnimContext to the running model robot.
				return myRAC.initConn(myMBRSC);
			}
		}
		return false;
	}		

	public void updateModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {	
		ModelRobot targetRobot = getBonyRobot();
		targetRobot.updateConfig(brc);
	}
	public void connectToVirtualChar() throws Exception {
		final ModelRobot br = getBonyRobot();
		if (br == null) {
			getLogger().warn("connectToVirtualChar() aborting due to missing ModelRobot, for char: {}", myCharIdent);
			return;
		}
		final HumanoidFigure hf = getHumanoidFigure();
		if (hf != null) {
			// It is optional to create this state object if there is no humanoid figure to animate.
			// It could be used for some other programming purpose.
			FigureState fs = setupFigureState(br);
			hf.setFigureState(fs);
			br.registerMoveListener(new ModelRobot.MoveListener() {
				@Override public void notifyBonyRobotMoved(ModelRobot br) {
					HumanoidFigure hf = getHumanoidFigure();
					if (hf != null) {
						ModelToFigureStateMappingFuncs.propagateState(br, hf);
					}
				}
			});
		}
	}
	
	public void stopAndReset() {
		if (myRAC != null) {
			myRAC.stopAndReset();
		}else {
			getLogger().warn("stopAndReset() ignored because RobotAnimContext = null for {}", myCharIdent);
		}
	}
	
	public HumanoidFigure getHumanoidFigure() {
		HumanoidFigure hf = null;
		if (myVWorldMapper != null) {
			HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
			if (hrc != null) {
				hf = hrc.getHumanoidFigure(myCharIdent);
			}
		}
		return hf;
	}
	
	public void playDangerYogaTestAnim() {
		if (myRAC != null) {
			myRAC.playDangerYogaTestAnimNow();
		} else {
			getLogger().warn("playDangerYogaTestAnim() ignored because RobotAnimContext = null for {}", myCharIdent);
		}
	}
	 
	private FigureState setupFigureState(ModelRobot br) { 

		FigureState fs = new FigureState();
		List<ModelJoint> allJoints = br.getJointList();
		for (ModelJoint mJoint : allJoints) {
			for (BoneProjectionRange bpr : mJoint.getBoneRotationRanges()) {
				String boneName = bpr.getBoneName();
				fs.obtainBoneState(boneName);
			}
		}
		return fs;
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
