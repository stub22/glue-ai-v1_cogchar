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

import java.io.InputStream;
import java.io.File;
import javax.swing.JFrame;

import java.util.List;
import java.util.ArrayList;

import org.osgi.framework.BundleContext;

import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.app.buddy.busker.DancingTriggerItem;
import org.cogchar.app.buddy.busker.TalkingTriggerItem;

import org.cogchar.bind.rk.robot.config.BoneRobotConfig;

import org.cogchar.app.buddy.busker.UpdateBonyConfig_TI;

import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.app.BodyController;
import org.cogchar.render.opengl.bony.app.VerbalController;

import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;

import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.robokind.api.common.services.ServiceConnectionDirectory;
import org.robokind.api.motion.jointgroup.JointGroup;
import org.robokind.api.motion.jointgroup.RobotJointGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext {

	static Logger theLogger = LoggerFactory.getLogger(PumaAppContext.class);
	
	private BundleContext			myBundleContext;
	private	HumanoidRenderContext	myHRC;
	
	private	UpdateBonyConfig_TI		myUpdateBonyConfigTI;
	

	public PumaAppContext(BundleContext bc, String sysContextURI, String localConfigRootPath) {
		myBundleContext = bc;
		myHRC = (HumanoidRenderContext) RenderBundleUtils.getBonyRenderContext(bc);
		BonyConfigEmitter bonyCE = myHRC.getBonyConfigEmitter();
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();
		if (sysContextURI != null) {
			behavCE.setSystemContextURI(sysContextURI);
		}
		if (localConfigRootPath != null) {
			behavCE.setLocalFileRootDir(localConfigRootPath);
		}
	}

	public List<PumaDualCharacter> makeDualCharsForSwingOSGi() throws Throwable {
		startOpenGLCanvas(true);
		return connectDualRobotChars();
	}

	public HumanoidRenderContext getHumanoidRenderContext() { 
		return myHRC;
	}


	public List<PumaDualCharacter> connectDualRobotChars() throws Throwable {
		List<PumaDualCharacter> pdcList = new ArrayList<PumaDualCharacter>();
		BonyRenderContext brc = getHumanoidRenderContext();
		BonyConfigEmitter bonyCE = brc.getBonyConfigEmitter();
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();		
		List<String> charURIs = bonyCE.getBonyCharURIs();
		for (String charURI : charURIs) {
			PumaDualCharacter pdc = connectDualRobotChar(charURI);
			pdcList.add(pdc);
		}
		// Let's be lame for the moment, and assume the first character found is the only one we want to control.
		PumaDualCharacter pdc = pdcList.get(0);
		if (pdc != null) {
			String chrURI = pdc.getCharURI();
			pdc.connectBonyCharToRobokindSvcs(myBundleContext);
			registerConfigReloadTrigger(pdc);			
			registerTestDanceTrigger(pdc);
			registerTestTalkTrigger(pdc);
			
			String jgPathTail = bonyCE.getJointGroupPathTailForChar(chrURI);
			String jgFullPathTemp = behavCE.getRKMotionTempFilePath(jgPathTail);
			File jgConfigFile = new File(jgFullPathTemp);
			if (jgConfigFile.canRead()) {
				PumaHumanoidMapper phm = pdc.getHumanoidMapper();
				RobotServiceContext rsc = phm.getRobotServiceContext();
				rsc.startJointGroup(jgConfigFile);
			}
		}
		return pdcList;
	}
	public PumaDualCharacter connectDualRobotChar(String bonyCharURI)
			throws Throwable {
		
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception ("HumanoidRenderContext is null");
		}
		BonyConfigEmitter bonyCE = hrc.getBonyConfigEmitter();
		String nickName = bonyCE.getNicknameForChar(bonyCharURI);
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, bonyCharURI, nickName);
		
		return pdc;
	}

	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		theLogger.info("Got BonyRenderContext: " + hrc);

		if (hrc != null) {
			if (wrapInJFrameFlag) {
				VirtualCharacterPanel vcp = hrc.getPanel();
				theLogger.info("Got VirtCharPanel: " + vcp);
				// Frame must be packed after panel created, but created  before startJMonkey.  
				// If startJMonkey is called first, we often hang in frame.setVisible() as JMonkey tries
				// to do some magic restart deal that doesn't work as of jme3-alpha4-August_2011.
				JFrame jf = vcp.makeEnclosingJFrame("CCRK-PUMA virtual character");
				theLogger.info("Got Enclosing Frame, adding to BonyRenderContext for WindowClose triggering: " + jf);
				// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
				hrc.setFrame(jf);
			}
			BonyVirtualCharApp app = hrc.getApp();

			if (app.isCanvasStarted()) {
				theLogger.warn("JMonkey Canvas was already started!");
			} else {

				theLogger.info("Starting JMonkey canvas - hold yer breath! [[[[[[[[[[[[[[[[[[[[[[[[[[");
				app.startJMonkeyCanvas();
				theLogger.info("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]  Finished starting JMonkey canvas!");
			}
			//((BonyStickFigureApp) app).setScoringFlag(true);			

		} else {
			theLogger.error("BonyRenderContext is NULL, cannot startOpenGLCanvas!");
		}
	}
	private void registerConfigReloadTrigger(PumaDualCharacter pdc) { 
		myUpdateBonyConfigTI = new UpdateBonyConfig_TI();
		
		// Hook up to a JME3 action to catch keypresses in OpenGL window.
		HumanoidPuppetActions.PlayerAction.UPDATE_BONY_CONFIG.getBinding().setTargetBox(pdc);
		HumanoidPuppetActions.PlayerAction.UPDATE_BONY_CONFIG.getBinding().setTargetTrigger(myUpdateBonyConfigTI);
		
		myUpdateBonyConfigTI.myOptResourceClassLoader = null;
	}
	private void registerTestDanceTrigger(PumaDualCharacter pdc) { 
		DancingTriggerItem dti = new DancingTriggerItem();
		
		// 1. Hook up to a JME3 action 
		HumanoidPuppetActions.PlayerAction.POKE.getBinding().setTargetBox(pdc);
		HumanoidPuppetActions.PlayerAction.POKE.getBinding().setTargetTrigger(dti);
		
		// 2. Hook up to the Swing-based "BodyController"
		// Kinda ugly, may be axed soon
		HumanoidRenderContext contextForSwingAction = getHumanoidRenderContext();
		if (contextForSwingAction != null) {
			
			VirtualCharacterPanel vcp = contextForSwingAction.getPanel();
			if (vcp != null) {
				BodyController bodCont = vcp.getBodyController();
				if (bodCont != null) {
					bodCont.setupPokeTrigger(pdc, dti);		
				} else {
					theLogger.warn("No BodyController found to attach poke-trigger to");
				}
			}
		}
	}
	private void registerTestTalkTrigger(PumaDualCharacter pdc) {

		TalkingTriggerItem tti = new TalkingTriggerItem();
		
		// 1. Hook up to a JME3 action 
		HumanoidPuppetActions.PlayerAction.TALK.getBinding().setTargetBox(pdc);
		HumanoidPuppetActions.PlayerAction.TALK.getBinding().setTargetTrigger(tti);

		// 2. Hook up to the Swing-based "BodyController"
		// Kinda ugly, may be axed soon		
		HumanoidRenderContext contextForSwingAction = getHumanoidRenderContext();		
		if (contextForSwingAction != null) {
			VirtualCharacterPanel vcp = contextForSwingAction.getPanel();
			if (vcp != null) {
				VerbalController verbCont = vcp.getVerbalController();
				if (verbCont != null) {
					verbCont.setupTalkTrigger(pdc, tti);
				} else {
					theLogger.warn("No VerbalController found to attach talk-trigger to");
				}
			}
		}
	}	

}
