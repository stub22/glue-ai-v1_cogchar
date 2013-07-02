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
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.impl.thing.basic.BasicThingActionConsumer;
import org.cogchar.impl.thing.basic.BasicThingActionRouter;
import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.name.entity.EntityRoleCN;
import org.cogchar.blob.emit.*;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderWorldMapper;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.cogchar.render.sys.input.VW_HelpScreenMgr;
import org.cogchar.render.sys.input.VW_InputBindingFuncs;
import org.cogchar.render.sys.module.RenderGateway;
import org.cogchar.render.sys.module.RenderModule;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.osgi.framework.BundleContext;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaVirtualWorldMapper extends BasicDebugger implements RenderGateway {

	private HumanoidRenderContext myHRC;
	private PumaAppContext myPAC;

	public PumaVirtualWorldMapper(PumaAppContext pac) {
		myPAC = pac;
	}

	@Override public HumanoidRenderContext getHumanoidRenderContext() {
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
	public void initVirtualWorlds(CommandSpace cspace, PumaConfigManager pcm, BasicThingActionRouter router) { 
// , PumaWebMapper webMapper, 	BundleContext bundleCtx) {
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		
		RepoClient rc = pcm.getMainConfigRepoClient();		

		myHRC.initCinematicParameters();
		
		KeyBindingConfig currKeyBindCfg = new KeyBindingConfig();
		// Hook-in for Goody system
		RenderRegistryClient rrc = myHRC.getRenderRegistryClient();
		GoodyFactory gFactory = GoodyFactory.createTheFactory(rrc, myHRC);
		try {
			List<Ident> worldConfigIdents = gce.entityMap().get(EntityRoleCN.VIRTUAL_WORLD_ENTITY_TYPE);
			// Multiple worldConfigIdents? Possible. It's possible duplicate cinematic definitions might cause problems
			// but we'll leave that for later, so sure, go ahead and load on multiple configs if they are requested.
			for (Ident configIdent : worldConfigIdents) {
				initCinematicStuff(gce, configIdent, rc, gFactory, router);
				// Like with everything else dependent on global config's graph settings (except for Lift, which uses a managed service
				// version of GlobalConfigEmitter) it seems logical to set the key bindings here.
				// Multiple worldConfigIdents? We decided above this is possible (if messy). If key bindings are duplicated
				// between the multiple world configs, we can't be certain which will end up in the KeyBindingConfig map.
				// But for now we'll assume user is smart enough to watch out for that (perhaps a dangerous idea) and pile
				// bindings from all worldConfigIdents into our KeyBindingConfig instance.
				try {
					Ident graphIdent = gce.ergMap().get(configIdent).get(EntityRoleCN.INPUT_BINDINGS_ROLE);
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
			GoodyFactory gFactory, BasicThingActionRouter router) {
		HumanoidRenderWorldMapper renderMapper = new HumanoidRenderWorldMapper();
		Ident graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(EntityRoleCN.LIGHTS_CAMERA_CONFIG_ROLE);
		} catch (Exception e) {
			getLogger().warn("Could not get valid graph on which to query for Lights/Cameras config of {}", worldConfigIdent.getLocalName(), e);
		}
		try {
			renderMapper.initLightsAndCamera(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().warn("Error attempting to initialize lights and cameras for {}: ", worldConfigIdent.getLocalName(), e);
		}
		setupActionConsumer(router, gce, worldConfigIdent, repoCli, gFactory);
		graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(EntityRoleCN.WAYPOINTS_BINDINGS_ROLE);
			renderMapper.initWaypoints(repoCli, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not initialize waypoints/orientations with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}
		graphIdent = null;
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(EntityRoleCN.MOTIONPATH_CONFIG_ROLE);
		} catch (Exception e) {
			getLogger().warn("Could not get valid graph on which to query for Paths config of {}", worldConfigIdent.getLocalName(), e);
		}
		try {
			renderMapper.initPaths(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().warn("Error attempting to initialize Paths for {}: ", worldConfigIdent.getLocalName(), e);
		}
		try {
			graphIdent = gce.ergMap().get(worldConfigIdent).get(EntityRoleCN.THING_ANIM_BINDINGS_ROLE);
			renderMapper.initThingAnims(repoCli, myHRC, graphIdent);
		} catch (Exception e) {
			getLogger().error("Could not initialize Thing spatial animations with a config of {}",
					worldConfigIdent.getLocalName(), e);
		}
	}
	
	public void setupActionConsumer(BasicThingActionRouter router, GlobalConfigEmitter gce, Ident worldConfigID, RepoClient repoCli, 
			GoodyFactory gFactory){
		// Goodies should be initialized before paths/animations so that they can reference Goodies!
		try {
			Ident actionGraphID = gce.ergMap().get(worldConfigID).get(EntityRoleCN.THING_ACTIONS_BINDINGS_ROLE);
			BasicThingActionConsumer consumer = 	gFactory.getActionConsumer();
			consumer.consumeAllActions(repoCli, actionGraphID);
			router.appendConsumer(actionGraphID, consumer);
		} catch (Exception e) {
			getLogger().error("Could not initialize Thing actions with a config of {}",
					worldConfigID.getLocalName(), e);
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
		DataballGoodyBuilder ballBldr = DataballGoodyBuilder.getTheBallBuilder();
		ballBldr.setClassLoader("Cog Char", bonyRdfCl);
		ballBldr.initialize(myHRC);
		myHRC.setTheBallBuilder(ballBldr);
	}	
	// Previous functions now mostly done from within LifterLifecycle on create(). 
	// Retaining for now for legacy BallBuilder classloader hookup
	public void connectHrkindVisualizationContent(ClassLoader hrkindResourceCL) {
		DataballGoodyBuilder.getTheBallBuilder().setClassLoader("hrkind.content.preview", hrkindResourceCL); // Adds this classloader to the ones Databalls know about
	}
	public void toggleHelpScreenDisplay() { 
		VW_HelpScreenMgr hsm = VW_InputBindingFuncs.getHelpScreenMgr();
		RenderRegistryClient rrc = myHRC.getRenderRegistryClient();
		hsm.toggleHelpTextDisplay(rrc);
	}
	public void attachRenderModule(RenderModule rModule) {
		rModule.setRenderGateway(this);
		myHRC.attachModule(rModule);
		
	}
	public void detachRenderModule(RenderModule rModule) {
		myHRC.detachModule(rModule);
		rModule.setRenderGateway(null);
	}
}
