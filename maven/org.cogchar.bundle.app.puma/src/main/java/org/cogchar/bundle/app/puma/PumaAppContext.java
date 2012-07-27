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
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
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

	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}

	public HumanoidRenderContext getHumanoidRenderContext() {
		return myHRC;
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
		final HumanoidRenderContext hrc = getHumanoidRenderContext();

		hrc.runTaskOnJmeThreadAndWait(new CogcharRenderContext.Task() {
			public void perform() throws Throwable {
				hrc.initHumanoidStuff();
			}
		});

		Set<Ident> charIdents = HumanoidConfigEmitter.getRobotIdents();
		
		for (Ident charIdent : charIdents) {
			logInfo("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: " + charIdent);
			PumaDualCharacter pdc = connectDualRobotChar(charIdent);
			pdcList.add(pdc);
		}
		
		// hrc.initHumanoidStuff(); can also be here if needed
		
		// Right now, only Cajun Zero really works properly, but we're getting close...
		for (PumaDualCharacter pdc: pdcList) {
			if (pdc.getNickName().equals("rk-cajunZeno")) {
			//if (!pdc.getNickName().equals("rk-sinbad")) {
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
	public void setupCharacterBindingToRobokind(PumaDualCharacter pdc)  {
		try {
			pdc.connectBonyCharToRobokindSvcs(myBundleContext);
			setupRobokindJointGroup(pdc);
			pdc.connectSpeechOutputSvcs(myBundleContext);
		} catch (Throwable t) {
			logWarning("Problems in Robokind binding init", t);
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
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, bonyCharIdent, nickName);

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
