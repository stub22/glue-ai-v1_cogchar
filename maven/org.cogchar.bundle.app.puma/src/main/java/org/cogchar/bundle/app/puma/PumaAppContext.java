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
import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions.PlayerAction;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;  // Perhaps we want to fetch this from a context instead, but it's a singleton, so no harm in getting it directly for the moment
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;


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
	
	// From the commentary in PumaBooter:
	// Now here's something I was hoping to avoid, but it necessary for our experiment in making Lift a managed
	// service. This is best seen as a trial of one possible way to handle the "GlobalMode" graph configuration.
	// What we'll do here is tell the PumaAppContext to make the GlobalConfigEmitter available as a no-lifecycle
	// managed service. (Why no-lifecycle? Because these lifecycles have to end somewhere! But it would make sense
	// to make this service depend on the query interface if we decide to keep it.)
	// Then Lifter can access it to load its config.
	// The problem with this approach is that it elevates the GlobalConfigEmitter to a data structure of particular 
	// importance outside of PUMA (we're putting it on the OSGi registry for crying out loud!), when at this early
	// point I've been trying to keep non-PUMA code "agnostic" to any details of the graph "mode" config other than
	// the Idents of the graph.
	// So this may be a bad-idea-dead-end. Unless we decide we've fallen in love with both the GlobalConfigEmitter
	// and the idea of doing config via managed services, in which it may turn out to be just what we need.
	// For now, we'll restrict usage of this to the LifterLifeCycle only...
	boolean startGlobalConfigService() {
		boolean success = false;
		if (myGlobalConfig != null) {
			ServiceLifecycleProvider lifecycle = 
					new SimpleLifecycle(new GlobalConfigServiceImpl(), GlobalConfigEmitter.GlobalConfigService.class);
			OSGiComponent ergComp = new OSGiComponent(myBundleContext, lifecycle);
			ergComp.start();
			success = true;
		}
		return success;
	}
	
	// Right now this really feels wrong to make this a service! I don't believe in these maps enough yet.
	// Putting it here since it's more experimental than the GlobalConfigEmitter itself, but if this ends up
	// being the "preferred" solution this interface should probably go into o.c.blob.emit
	class GlobalConfigServiceImpl implements GlobalConfigEmitter.GlobalConfigService {
		@Override
		public java.util.HashMap<Ident, java.util.HashMap<Ident, Ident>> getErgMap() {
			return myGlobalConfig.ergMap();
		}
		@Override
		public java.util.HashMap<String, java.util.List<Ident>> getEntityMap() {
			return myGlobalConfig.entityMap();
		}
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
		List<Ident> charIdents = new ArrayList<Ident>(); // A blank list, so if the try fails below, the for loop won't throw an Exception
		try {
			List<Ident> identsFromConfig = myGlobalConfig.entityMap().get(PumaModeConstants.CHAR_ENTITY_TYPE);
			if (identsFromConfig != null) {
				charIdents = identsFromConfig;
			} else {
				logWarning("Did not find character entity list in global config map");
				throw new Throwable();
			}
		} catch (Throwable t) {
			logWarning("Could not retrieve list of characters from global configuration");
		}
		
		if (myGlobalConfig == null) {
			logWarning("GlobalConfigEmitter not available, cannot setup characters!");
		} else {
			for (Ident charIdent : charIdents) {
				logInfo("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: " + charIdent);
				Ident graphIdentForBony;
				Ident graphIdentForHumanoid;
				try {
					graphIdentForBony = myGlobalConfig.ergMap().get(charIdent).get(PumaModeConstants.BONY_CONFIG_ROLE);
					graphIdentForHumanoid = myGlobalConfig.ergMap().get(charIdent).get(PumaModeConstants.HUMANOID_CONFIG_ROLE);
				} catch (Exception e) {
					logWarning("Could not get valid graphs on which to query for config of " + charIdent.getLocalName());
					break;
				}
				HumanoidConfig myHumanoidConfig = new HumanoidConfig(charIdent, graphIdentForHumanoid);
				PumaDualCharacter pdc = connectDualRobotChar(charIdent, myHumanoidConfig.nickname);
				pdcList.add(pdc);
				setupCharacterBindingToRobokind(pdc, graphIdentForBony, myHumanoidConfig);
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
	
	public void setupCharacterBindingToRobokind(PumaDualCharacter pdc, Ident graphIdentForBony, HumanoidConfig hc) {

		try {
			pdc.connectBonyCharToRobokindSvcs(myBundleContext, graphIdentForBony, hc);
			setupRobokindJointGroup(pdc, hc.jointConfigPath);
			pdc.connectSpeechOutputSvcs(myBundleContext);
		} catch (Throwable t) {
			logWarning("Problems in Robokind binding init", t);
		}
	}
	
	public void setupRobokindJointGroup(PumaDualCharacter pdc, String jgFullPath) throws Throwable {		
		//Ident chrIdent = pdc.getCharIdent();
		File jgConfigFile = new File(jgFullPath);
		if (jgConfigFile.canRead()) {
			PumaHumanoidMapper phm = pdc.getHumanoidMapper();
			RobotServiceContext rsc = phm.getRobotServiceContext();
			rsc.startJointGroup(jgConfigFile);
		}
	}
	
	public PumaDualCharacter connectDualRobotChar(Ident bonyCharIdent, String nickName)
			throws Throwable {

		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception("HumanoidRenderContext is null");
		}
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
