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

import java.io.File;
import javax.swing.JFrame;

import java.util.List;
import java.util.ArrayList;
import org.cogchar.app.buddy.busker.TriggerItem;

import org.osgi.framework.BundleContext;

import org.cogchar.bind.rk.robot.svc.RobotServiceContext;

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.bony.BodyController;
import org.cogchar.render.app.bony.VerbalController;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions.PlayerAction;

import org.cogchar.render.opengl.osgi.RenderBundleUtils;

import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.platform.trigger.DummyBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext {

	static Logger theLogger = LoggerFactory.getLogger(PumaAppContext.class);
	
	private BundleContext			myBundleContext;
	private	HumanoidRenderContext	myHRC;
	
	private	TriggerItems.UpdateBonyConfig		myUpdateBonyConfigTI;
	

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
		List<Ident> charIdents = bonyCE.getActiveBonyCharIdents();
		for (Ident charIdent : charIdents) {
			PumaDualCharacter pdc = connectDualRobotChar(charIdent);
			pdcList.add(pdc);
		}
		// Let's be lame for the moment, and assume the first character found is the only one we want to control.
		PumaDualCharacter pdc = pdcList.get(0);
		if (pdc != null) {
			Ident chrIdent = pdc.getCharIdent();
			pdc.connectBonyCharToRobokindSvcs(myBundleContext);
			
			pdc.connectSpeechOutputSvcs(myBundleContext);
			
			pdc.registerDefaultSceneTriggers();
			pdc.loadBehaviorConfig(false);
			
			registerSpecialInputTriggers(pdc);
			
			pdc.startTheater();
			
			String jgPathTail = bonyCE.getJointGroupPathTailForChar(chrIdent);
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
	public PumaDualCharacter connectDualRobotChar(Ident bonyCharIdent)
			throws Throwable {
		
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception ("HumanoidRenderContext is null");
		}
		BonyConfigEmitter bonyCE = hrc.getBonyConfigEmitter();
		String nickName = bonyCE.getNicknameForChar(bonyCharIdent);
		
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, bonyCharIdent, nickName);
		
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
	private void registerSpecialInputTriggers(PumaDualCharacter pdc) { 	
		
		hookItUp(PlayerAction.STOP_AND_RESET_CHAR, pdc, new TriggerItems.StopAndReset());
		hookItUp(PlayerAction.STOP_RESET_AND_RECENTER_CHAR, pdc, new TriggerItems.StopResetAndRecenter());

		hookItUp(PlayerAction.DANGER_YOGA, pdc, new TriggerItems.DangerYoga());
		hookItUp(PlayerAction.SAY_THE_TIME, pdc, new TriggerItems.SayTheTime());
				
		hookItUp(PlayerAction.USE_PERM_ANIMS, pdc, new TriggerItems.UsePermAnims());
		hookItUp(PlayerAction.USE_TEMP_ANIMS, pdc, new TriggerItems.UseTempAnims());

		hookItUp(PlayerAction.RELOAD_BEHAVIOR, pdc, new TriggerItems.ReloadBehavior());

		myUpdateBonyConfigTI = new TriggerItems.UpdateBonyConfig();
		myUpdateBonyConfigTI.myOptResourceClassLoader = null;
		
		hookItUp(PlayerAction.UPDATE_BONY_CONFIG, pdc, myUpdateBonyConfigTI);
	}
	private void hookItUp(PlayerAction action, PumaDualCharacter pdc, TriggerItem trigItem) {
		// Hook up to a JME3 action (defined in our org.cogchar.lib.render project) to catch keypresses in OpenGL window.
		DummyBinding db = action.getBinding();
		db.setTargetBox(pdc);
		db.setTargetTrigger(trigItem);
	}
}
