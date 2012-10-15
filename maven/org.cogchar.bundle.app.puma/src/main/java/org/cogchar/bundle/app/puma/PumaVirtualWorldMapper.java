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
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.blob.emit.KeystrokeConfigEmitter;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions.PlayerAction;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderWorldMapper;
import org.osgi.framework.BundleContext;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.cogchar.app.puma.cgchr.PumaDualCharacter;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaVirtualWorldMapper extends BasicDebugger {
	
	private HumanoidRenderContext	myHRC;
	private	PumaAppContext			myPAC;
	
	public PumaVirtualWorldMapper(PumaAppContext pac) {
		myPAC = pac;
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
		BundleContext bundleCtx = myPAC.getBundleContext();
		myHRC = (HumanoidRenderContext) RenderBundleUtils.buildBonyRenderContextInOSGi(bundleCtx, panelKind);
		myHRC.setUpdateInterface(new UpdateInterfaceImpl());
		return myHRC;
	}	
	
	class UpdateInterfaceImpl implements HumanoidRenderContext.UpdateInterface {
		protected boolean forceFreshDefaultMainConfig = false;
		@Override public boolean updateConfig(String request) {
			return myPAC.updateConfigByRequest(request, forceFreshDefaultMainConfig);
		}
	}
	
	
// The Lights/Camera/Cinematics init used to be done from HumanoidRenderContext, but the global config lives
	// here as does humanoid and bony config. So may make sense to have this here too, though we could move it
	// back to HRC if there are philosophical reasons for doing so. (We'd also have to pass two graph flavors to it for this.)
	// Added: since jMonkey key bindings are part of "virtual world" config like Lights/Camera/Cinematics, they are also 
	// set here
	public void initCinema() {
		PumaConfigManager pcm = myPAC.getConfigManager();
		 GlobalConfigEmitter gce = pcm.getGlobalConfig();
		 BundleContext bundleCtx = myPAC.getBundleContext();		 
		 RepoClient rc = myPAC.getOrMakeMainConfigRC();

		 PumaWebMapper webMapper = myPAC.getOrMakeWebMapper();
		 ClassLoader bonyRdfCL = myPAC.getCogCharResourcesClassLoader();
		 
		myHRC.initCinematicParameters();
		webMapper.connectLiftSceneInterface(bundleCtx);
		webMapper.connectLiftInterface(bundleCtx);	
		// The connectCogCharResources call below is currently still needed only for the "legacy" BallBuilder functionality
		webMapper.connectCogCharResources(bonyRdfCL, myHRC);
		KeyBindingConfig currKeyBindCfg = new KeyBindingConfig();
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		Ident graphIdent = null;
		try {
			List<Ident> worldConfigIdents = gce.entityMap().get(PumaModeConstants.VIRTUAL_WORLD_ENTITY_TYPE);
			// Multiple worldConfigIdents? Possible. It's possible duplicate cinematic definitions might cause problems
			// but we'll leave that for later, so sure, go ahead and load on multiple configs if they are requested.
			for (Ident configIdent : worldConfigIdents) {
				try {
					graphIdent = gce.ergMap().get(configIdent).get(PumaModeConstants.LIGHTS_CAMERA_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graph on which to query for Lights/Cameras config of {}", configIdent.getLocalName(), e);
				}
				try {
					myRenderMapper.initLightsAndCamera(rc, myHRC, graphIdent);
				} catch (Exception e) {
					getLogger().warn("Error attempting to initialize lights and cameras for {}: ", configIdent.getLocalName(), e);
				}
				graphIdent = null;
				try {
					graphIdent = gce.ergMap().get(configIdent).get(PumaModeConstants.CINEMATIC_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graph on which to query for Cinematics config of {}", configIdent.getLocalName(), e);
				}
				try {
					myRenderMapper.initCinematics(rc, myHRC, graphIdent);
				} catch (Exception e) {
					getLogger().warn("Error attempting to initialize Cinematics for {}: ", configIdent.getLocalName(), e);
				}
				// Like with everything else dependent on global config's graph settings (except for Lift, which uses a managed service
				// version of GlobalConfigEmitter) it seems logical to set the key bindings here.
				// Multiple worldConfigIdents? We decided above this is possible (if messy). If key bindings are duplicated
				// between the multiple world configs, we can't be certain which will end up in the KeyBindingConfig map.
				// But for now we'll assume user is smart enough to watch out for that (perhaps a dangerous idea) and pile
				// bindings from all worldConfigIdents into our KeyBindingConfig instance.
				try {
					graphIdent = gce.ergMap().get(configIdent).get(PumaModeConstants.INPUT_BINDINGS_ROLE);
					KeystrokeConfigEmitter kce = new KeystrokeConfigEmitter();
					
					currKeyBindCfg.addBindings(rc, graphIdent, kce);
				} catch (Exception e) {
					getLogger().error("Could not get valid graph on which to query for input bindings config of {}", configIdent.getLocalName(), e);
				}

			}
		} catch (Exception e) {
			getLogger().error("Could not retrieve any specified VirtualWorldEntity for this global configuration!");
		}
		myHRC.initBindings(currKeyBindCfg);
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

	protected void registerSpecialInputTriggers(PumaDualCharacter pdc) {

		hookItUp(PlayerAction.STOP_AND_RESET_CHAR, pdc, new TriggerItems.StopAndReset());
		hookItUp(PlayerAction.STOP_RESET_AND_RECENTER_CHAR, pdc, new TriggerItems.StopResetAndRecenter());

		hookItUp(PlayerAction.DANGER_YOGA, pdc, new TriggerItems.DangerYoga());
		hookItUp(PlayerAction.SAY_THE_TIME, pdc, new TriggerItems.SayTheTime());

		hookItUp(PlayerAction.USE_PERM_ANIMS, pdc, new TriggerItems.UsePermAnims());
		hookItUp(PlayerAction.USE_TEMP_ANIMS, pdc, new TriggerItems.UseTempAnims());

		hookItUp(PlayerAction.RELOAD_BEHAVIOR, pdc, new TriggerItems.ReloadBehavior());

		//myUpdateBonyConfigTI = new TriggerItems.UpdateBonyConfig();
		//myUpdateBonyConfigTI.myOptResourceClassLoader = null;

		//hookItUp(PlayerAction.UPDATE_BONY_CONFIG, pdc, myUpdateBonyConfigTI);
	}

	private void hookItUp(PlayerAction action, PumaDualCharacter pdc, TriggerItem trigItem) {
		// Hook up to a JME3 action (defined in our org.cogchar.lib.render project) to catch keypresses in OpenGL window.
		CogcharActionBinding db = action.getBinding();
		db.addTargetBox(pdc);
		// Overrides any existing trigger.
		db.setTargetTrigger(trigItem);
	}

	protected void clearSpecialInputTriggers() {

		unhookIt(PlayerAction.STOP_AND_RESET_CHAR);
		unhookIt(PlayerAction.STOP_RESET_AND_RECENTER_CHAR);

		unhookIt(PlayerAction.DANGER_YOGA);
		unhookIt(PlayerAction.SAY_THE_TIME);

		unhookIt(PlayerAction.USE_PERM_ANIMS);
		unhookIt(PlayerAction.USE_TEMP_ANIMS);

		unhookIt(PlayerAction.RELOAD_BEHAVIOR);

	}

	private void unhookIt(PlayerAction action) {
		CogcharActionBinding db = action.getBinding();
		db.clearTargetBoxes();
	}
	
	public void clearCinematicStuff() { 		
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		myRenderMapper.clearLights(myHRC);
		myRenderMapper.clearCinematics(myHRC);
		myRenderMapper.clearViewPorts(myHRC);		
	}
	protected void detachAllHumanoidFigures() { 
		myHRC.detachHumanoidFigures();
	}
}
