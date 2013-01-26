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
package org.cogchar.app.puma.vworld;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaModeConstants;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.KeystrokeConfigEmitter;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderWorldMapper;
import org.cogchar.render.model.databalls.BallBuilder;
import org.cogchar.render.model.goodies.GoodyFactory;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.cogchar.render.sys.input.VW_HelpScreenMgr;
import org.cogchar.render.sys.input.VW_InputBindingFuncs;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.osgi.framework.BundleContext;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaVirtualWorldMapper extends BasicDebugger {

	private HumanoidRenderContext myHRC;
	private PumaAppContext myPAC;

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

		return myHRC;
	}

// The Lights/Camera/Cinematics init used to be done from HumanoidRenderContext, but the global config lives
	// here as does humanoid and bony config. So may make sense to have this here too, though we could move it
	// back to HRC if there are philosophical reasons for doing so. (We'd also have to pass two graph flavors to it for this.)
	// Added: since jMonkey key bindings are part of "virtual world" config like Lights/Camera/Cinematics, they are also 
	// set here
	public void initVirtualWorlds(CommandSpace cspace, PumaConfigManager pcm) { 
// , PumaWebMapper webMapper, 	BundleContext bundleCtx) {
		GlobalConfigEmitter gce = pcm.getGlobalConfig();
		RepoClient rc = pcm.getMainConfigRepoClient();		

		myHRC.initCinematicParameters();
		
		KeyBindingConfig currKeyBindCfg = new KeyBindingConfig();
		// Hook-in for Goody system
		RenderRegistryClient rrc = myHRC.getRenderRegistryClient();
		GoodyFactory gFactory = GoodyFactory.createTheFactory(rrc, myHRC.getScreenSizeFromJmeAppSettings());
		try {
			List<Ident> worldConfigIdents = gce.entityMap().get(PumaModeConstants.VIRTUAL_WORLD_ENTITY_TYPE);
			// Multiple worldConfigIdents? Possible. It's possible duplicate cinematic definitions might cause problems
			// but we'll leave that for later, so sure, go ahead and load on multiple configs if they are requested.
			for (Ident configIdent : worldConfigIdents) {
				initCinematicStuff(gce, configIdent, rc, gFactory);
				// Like with everything else dependent on global config's graph settings (except for Lift, which uses a managed service
				// version of GlobalConfigEmitter) it seems logical to set the key bindings here.
				// Multiple worldConfigIdents? We decided above this is possible (if messy). If key bindings are duplicated
				// between the multiple world configs, we can't be certain which will end up in the KeyBindingConfig map.
				// But for now we'll assume user is smart enough to watch out for that (perhaps a dangerous idea) and pile
				// bindings from all worldConfigIdents into our KeyBindingConfig instance.
				try {
					Ident graphIdent = gce.ergMap().get(configIdent).get(PumaModeConstants.INPUT_BINDINGS_ROLE);
					KeystrokeConfigEmitter kce = new KeystrokeConfigEmitter();

					currKeyBindCfg.addBindings(rc, graphIdent, kce);
				} catch (Exception e) {
					getLogger().error("Could not get valid graph on which to query for input bindings config of {}",
							configIdent.getLocalName(), e);
				}
			}
		} catch (Exception e) {
			getLogger().error("Could not retrieve any specified VirtualWorldEntity for this global configuration!");
		}
		
		myHRC.refreshInputBindingsAndHelpScreen(currKeyBindCfg, cspace);
	}
	
	private void initCinematicStuff(GlobalConfigEmitter gce, Ident worldConfigIdent, RepoClient repoCli, 
			GoodyFactory gFactory) {
		HumanoidRenderWorldMapper renderMapper = new HumanoidRenderWorldMapper();
		Ident graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.LIGHTS_CAMERA_CONFIG_ROLE);
		} catch (Exception e) {
			getLogger().warn("Could not get valid graph on which to query for Lights/Cameras config of {}", worldConfigIdent.getLocalName(), e);
		}
		try {
			renderMapper.initLightsAndCamera(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().warn("Error attempting to initialize lights and cameras for {}: ", worldConfigIdent.getLocalName(), e);
		}
		graphIdent = null;
		// Goodies should be initialized before paths/animations so that they can reference Goodies!
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.THING_ACTIONS_BINDINGS_ROLE);
			gFactory.getTheGoodySpace().readAndApplyGoodyActions(repoCli, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not initialize Thing actions with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}
		graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.WAYPOINTS_BINDINGS_ROLE);
			renderMapper.initWaypoints(repoCli, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not initialize waypoints/orientations with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}
		graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.MOTIONPATH_CONFIG_ROLE);
		} catch (Exception e) {
			getLogger().warn("Could not get valid graph on which to query for Paths config of {}", worldConfigIdent.getLocalName(), e);
		}
		try {
			renderMapper.initPaths(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().warn("Error attempting to initialize Paths for {}: ", worldConfigIdent.getLocalName(), e);
		}
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.THING_ANIM_BINDINGS_ROLE);
			renderMapper.initThingAnims(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not initialize Thing spatial animations with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}
	}
	

	/**
	 * Second (and most crucial) stage of OpenGL init. This method blocks until the canvas initialization is complete,
	 * which requires that the simpleInitApp() methods have all completed.
	 *
	 * @param wrapInJFrameFlag
	 * @throws Exception
	 */
	public void startOpenGLCanvas(boolean wrapInJFrameFlag, java.awt.event.WindowListener optWinLis) throws Exception {
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc != null) {
			hrc.startOpenGLCanvas(wrapInJFrameFlag, optWinLis);
		} else {
			logError("HumanoidRenderContext is NULL, cannot startOpenGLCanvas!");
		}
	}

	public void clearCinematicStuff() {
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		myRenderMapper.clearLights(myHRC);
		myRenderMapper.clearCinematics(myHRC);
		myRenderMapper.clearViewPorts(myHRC);
	}

	public void detachAllHumanoidFigures() {
		myHRC.getHumanoidFigureManager().detachHumanoidFigures(myHRC);
	}
	public void connectVisualizationResources(ClassLoader bonyRdfCl) {
		BallBuilder ballBldr = BallBuilder.getTheBallBuilder();
		ballBldr.setClassLoader("Cog Char", bonyRdfCl);
		ballBldr.initialize(myHRC);
		myHRC.setTheBallBuilder(ballBldr);
	}	
	// Previous functions now mostly done from within LifterLifecycle on create(). 
	// Retaining for now for legacy BallBuilder classloader hookup
	public void connectHrkindVisualizationContent(ClassLoader hrkindResourceCL) {
		BallBuilder.getTheBallBuilder().setClassLoader("hrkind.content.preview", hrkindResourceCL); // Adds this classloader to the ones Databalls know about
	}
	public void toggleHelpScreenDisplay() { 
		VW_HelpScreenMgr hsm = VW_InputBindingFuncs.getHelpScreenMgr();
		RenderRegistryClient rrc = myHRC.getRenderRegistryClient();
		hsm.toggleHelpTextDisplay(rrc);
	}
	
	public void updateGoodySpace(RepoClient rc, GlobalConfigEmitter gce) {
		Ident worldConfigIdent = new FreeIdent("if/exception/while/reading/this/ident/report#null");
		try {
			// We shouldn't have more than one, so let's just assume there's one. This is a slightly different assumption
			// to what happens in PumaVirtualWorldMapper.
			worldConfigIdent = gce.entityMap().get(PumaModeConstants.VIRTUAL_WORLD_ENTITY_TYPE).get(0);
			Ident graphIdent = gce.ergMap().get(worldConfigIdent).get(PumaModeConstants.THING_ACTIONS_BINDINGS_ROLE);
			GoodyFactory.getTheFactory().getTheGoodySpace().readAndApplyGoodyActions(rc, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not recheck Thing actions with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}			
	}

}
