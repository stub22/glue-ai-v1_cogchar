/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import java.io.File;
import java.io.InputStream;

import java.util.List;

import java.util.Map;
import org.osgi.framework.BundleContext;

import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;

import org.cogchar.bind.rk.robot.model.ModelRobotUtils;
import org.cogchar.bind.rk.robot.model.ModelRobotFactory;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;


import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.demo.HumanoidRenderContext;
import org.cogchar.render.opengl.bony.model.HumanoidFigure;

import org.cogchar.bind.rk.robot.svc.BlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.model.ModelBoneRotRange;
import org.cogchar.bind.rk.robot.model.ModelBoneRotation;
import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;

import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.platform.trigger.DummyTrigger;
import org.cogchar.platform.trigger.DummyBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter implements DummyBox {
	
	static Logger theLogger = LoggerFactory.getLogger(PumaDualCharacter.class);
	private	ModelBlendingRobotServiceContext	myMBRSC;
	private	BonyRenderContext					myBRC;
	private	RobotAnimClient						myRAC;
	private SpeechOutputClient					mySOC;
	
	private	String								myCharURI;
	
	public PumaDualCharacter(BonyRenderContext brc, BundleContext bundleCtx, String charURI) {
		myBRC = brc;
		myMBRSC = new ModelBlendingRobotServiceContext(bundleCtx);
		myCharURI = charURI;
	}
	public void connectBonyDualToModelRobot() throws Throwable {
		BundleContext bundleCtx = myMBRSC.getBundleContext();
		String jointConfigAssetName = myBRC.getJointConfigAssetNameForChar(myCharURI);
		InputStream jointConfigAssetStream = openAssetStream(jointConfigAssetName);
		myMBRSC.makeModelRobotWithBlenderAndFrameSource(jointConfigAssetStream, jointConfigAssetName);
		// File jointBindingConfigFile = myBRC.getJointConfigFileForChar();
        //rjbd.registerDummyRobot();
		ModelRobot br = getBonyRobot();
		Robot.Id brid = br.getRobotId();
		if (br != null) {
	        connectToVirtualChar();
			applyInitialBoneRotations();
			myRAC = new RobotAnimClient(bundleCtx); 
			/*
			try {
		        RobotServiceFuncs.createAndRegisterFrameReceiver(bundleCtx, brid);
			} catch (Throwable t) {
				theLogger.warn("Could not register AMQP network server for robot with ID=" + brid, t);
			}
			 * 
			 */
		}
		mySOC = new SpeechOutputClient(bundleCtx);
	}
	private InputStream openAssetStream(String assetName) { 
		return myBRC.openAssetStream(assetName);
	}	
	public ModelRobot getBonyRobot() { 
		return myMBRSC.getRobot();
	}	

	public void connectToVirtualChar() throws Exception {
		setupFigureState();
		final ModelRobot br = getBonyRobot();
		br.registerMoveListener(new ModelRobot.MoveListener() {
			@Override public void notifyBonyRobotMoved(ModelRobot br) {
				HumanoidFigure hf = getHumanoidFigure();
				ModelToFigureStateMappingFuncs.propagateState(br, hf);
			}
			
		});
	}
	public HumanoidFigure getHumanoidFigure() { 
		HumanoidRenderContext hrc = (HumanoidRenderContext) myBRC;
		return hrc.getHumanoidFigure(myCharURI);
	}
	public void setupFigureState() { 
		ModelRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<ModelJoint> allJoints = br.getJointList();
		for (ModelJoint bj : allJoints) {
            for(ModelBoneRotRange range : bj.getBoneRotationRanges()){
                String name = range.getBoneName();
                fs.obtainBoneState(name);
            }
		}
		HumanoidFigure hf = getHumanoidFigure();
		hf.setFigureState(fs);
	}
	
    public void applyInitialBoneRotations() {
		HumanoidFigure hf = getHumanoidFigure();
		FigureState fs = hf.getFigureState();
		ModelRobot br = getBonyRobot();
		Map<String,List<ModelBoneRotation>> initialRotationMap = ModelRobotUtils.getInitialRotationMap(br);
        ModelToFigureStateMappingFuncs.applyAllSillyEulerRotations(fs, initialRotationMap);
    }
	public void triggerTestAnim() { 
		try {
			myRAC.createAndPlayTestAnim();
		} catch (Throwable t) {
			theLogger.error("problem playing test anim", t);
		}
	}
	public void sayText(String txt) {
		try {
			mySOC.speakText(txt);
		} catch (Throwable t) {
			theLogger.error("problem speaking", t);
		}
	}
}
