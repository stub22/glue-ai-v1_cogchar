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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.HumanoidConfigEmitter;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions.PlayerAction;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;  // Perhaps we want to fetch this from a context instead, but it's a singleton, so no harm in getting it directly for the moment
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.osgi.framework.BundleContext;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext extends BasicDebugger {

	private BundleContext myBundleContext;
	private HumanoidRenderContext myHRC;
	private TriggerItems.UpdateBonyConfig myUpdateBonyConfigTI;
	// Here's a GlobalConfigEmitter for our PUMA instance. Does it really belong here? Time will tell.
	private GlobalConfigEmitter myGlobalConfig;

	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}

	public HumanoidRenderContext getHumanoidRenderContext() {
		return myHRC;
	}
	
	public void setGlobalConfig(GlobalConfigEmitter config) {
		myGlobalConfig = config;
	}
	
	public GlobalConfigEmitter getGlobalConfig() {
		return myGlobalConfig;
	}
	
	public void updateGlobalConfig() {
		// Now this is a little irregular. We're creating this initally in PumaBooter, but also the same 
		// (temporarily fixed) mode is reloaded here when we want to updateGlobalConfig. So far, that's mainly for our 
		// current "primative" bony config reload. This all is a bit goofy and should be quite temporary; once we really 
		// figure out how best to handle changes to this "GlobalMode" stuff this should become less hodge-podge
		myGlobalConfig = new GlobalConfigEmitter(new FreeIdent(PumaModeConstants.rkrt+PumaModeConstants.globalMode, PumaModeConstants.globalMode));
	}

	/**
	 * First (of three) stage init of world, done BEFORE startOpenGLCanvas().
	 *
	 * @param panelKind
	 * @return
	 */
	public HumanoidRenderContext initHumanoidRenderContext(String panelKind) {
		myHRC = (HumanoidRenderContext) RenderBundleUtils.buildBonyRenderContextInOSGi(myBundleContext, panelKind);
		return myHRC;
	}

	/**
	 * Third (and last) stage init of OpenGL, and all other systems. Done AFTER startOpenGLCanvas().
	 *
	 * @return
	 * @throws Throwable
	 */
	public List<PumaDualCharacter> connectDualRobotChars() throws Throwable {
		List<PumaDualCharacter> pdcList = new ArrayList<PumaDualCharacter>();

		Set<Ident> charIdents = HumanoidConfigEmitter.getRobotIdents(); // Soon needs to pop out of myGlobalConfig instead
		
		for (Ident charIdent : charIdents) {
			logInfo("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: " + charIdent);
			PumaDualCharacter pdc = connectDualRobotChar(charIdent);
			pdcList.add(pdc);
		}
		
		for (PumaDualCharacter pdc: pdcList) {
			if (!pdc.getNickName().equals("rk-sinbad")) { // TODO: Need to un-hardcode this sinbad ASAP, just need to check into best way
				setupCharacterBindingToRobokind(pdc);
				setupAndStartBehaviorTheater(pdc);
			}
		}
		
		return pdcList;
	}
	
	public void setupAndStartBehaviorTheater(PumaDualCharacter pdc) throws Throwable {
		pdc.registerDefaultSceneTriggers();
		pdc.loadBehaviorConfig(false);

		registerSpecialInputTriggers(pdc);

		pdc.startTheater();
		
	}
	public void setupCharacterBindingToRobokind(PumaDualCharacter pdc) {
		if (myGlobalConfig == null) {
			logWarning("GlobalConfigEmitter not available, cannot setup character binding to Robokind!");
		} else {
			Ident graphIdent;
			try {
				graphIdent = myGlobalConfig.ergMap().get(pdc.getCharIdent()).get(PumaModeConstants.BONY_CONFIG_ROLE);
				try {
					pdc.connectBonyCharToRobokindSvcs(myBundleContext, graphIdent);
					setupRobokindJointGroup(pdc);
					pdc.connectSpeechOutputSvcs(myBundleContext);
				} catch (Throwable t) {
					logWarning("Problems in Robokind binding init", t);
				}
			} catch (Exception e) {
				logWarning("Could not get a valid graph on which to query for config of " + pdc.getCharIdent().getLocalName());
			}
		}
	}
	public void setupRobokindJointGroup(PumaDualCharacter pdc) throws Throwable {		
		Ident chrIdent = pdc.getCharIdent();
		String jgFullPath = HumanoidConfigEmitter.getJointConfigPath(chrIdent);

		File jgConfigFile = new File(jgFullPath);
		if (jgConfigFile.canRead()) {
			PumaHumanoidMapper phm = pdc.getHumanoidMapper();
			RobotServiceContext rsc = phm.getRobotServiceContext();
			rsc.startJointGroup(jgConfigFile);
		}
	}
	
	public PumaDualCharacter connectDualRobotChar(Ident bonyCharIdent)
			throws Throwable {

		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception("HumanoidRenderContext is null");
		}
		String nickName = HumanoidConfigEmitter.getRobotId(bonyCharIdent);
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, this, bonyCharIdent, nickName);

		return pdc;
	}

	/**
	 * Second (and most crucial) stage of OpenGL init. This method blocks until the canvas initialization is complete,
	 * which requires that the simpleInitApp() methods have all completed.
	 *
	 * @param wrapInJFrameFlag
	 * @throws Exception
	 */
	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc != null) {
			hrc.startOpenGLCanvas(wrapInJFrameFlag);
		} else {
			logError("HumanoidRenderContext is NULL, cannot startOpenGLCanvas!");
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
