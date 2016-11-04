/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.render.rendtest;

import com.jme3.input.FlyByCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import org.apache.log4j.Level;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.render.app.bony.BonyVirtualCharApp;

import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;

import org.cogchar.render.goody.basic.BasicGoodyCtx;
import org.cogchar.render.goody.basic.BasicGoodyCtxImpl;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.CoreFeatureAdapter;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClientImpl;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import java.util.List;


/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Goal is to use the Goody-ThingAction API to directly (without a repo) make each of the Goody types display and do 
 * each of its "moves", in a way that is straightforward to check visually, allowing us to check position, size, 
 * color of everything displayed.
 */

public class GoodyRenderTestApp extends BonyVirtualCharApp<GoodyModularRenderContext> {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.ALL);
		/* Suppress:
		 * 7532 [LWJGL Renderer Thread] WARN org.appdapter.osgi.registry.RegistryServiceFuncs  - 
		 * %%%%%%%%%% Cannot get local bundle, so we are assumed to be outside OSGi (credentialClaz=class org.cogchar.blob.emit.SubsystemHandleFinder$)
		 * 7532 [LWJGL Renderer Thread] INFO org.appdapter.osgi.registry.RegistryServiceFuncs  - 
		 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Getting singleton WellKnownRegistry in non-OSGi cont
		 */
		org.apache.log4j.Logger.getLogger(org.appdapter.osgi.registry.RegistryServiceFuncs.class).setLevel(Level.ERROR);
		RenderConfigEmitter rce = new RenderConfigEmitter();
		GoodyRenderTestApp app = new GoodyRenderTestApp(rce);
		// This will trigger the makeCogcharRenderContext() and then the simpleInitApp() below.
		app.start();
	}
	public GoodyRenderTestApp(RenderConfigEmitter rce) { 
		super(rce);
	}
	@Override protected GoodyModularRenderContext makeCogcharRenderContext() {
		GoodyRenderRegistryClient grrc = new GoodyRenderRegistryClientImpl();
		RenderConfigEmitter rce = getConfigEmitter();
		GoodyModularRenderContext gmrc = new GoodyModularRenderContext(grrc, rce);
		gmrc.setApp(this);
		return gmrc;
	}
	// This occurs during start(), *on the JME3 thread* (so any attempts to wait() for something to happen 
	// on that thread would deadlock this execution!).
	
	@Override public void simpleInitApp() {
		getLogger().info("Hooray for Goodies!");
		super.simpleInitApp();
		FlyByCamera fbc = getFlyByCamera();
		fbc.setMoveSpeed(20);
				
		GoodyModularRenderContext renderCtx = getBonyRenderContext();
		GoodyRenderRegistryClient grrc = renderCtx.getGoodyRenderRegistryClient();
		// GoodyFactory gFactory = GoodyFactory.createTheFactory(grrc, renderCtx);
		BasicGoodyCtxImpl bgc = new BasicGoodyCtxImpl(grrc, renderCtx);
		bgc.setupAsMainGoodyCtx(); // Added 2016-09-03 for compat with changes to BasicGoodyCtxImpl
		try {
			initContentOnJME3Thread(bgc);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		// hideJmonkeyDebugInfo();
	}


	// We are on the JME3 thread!
	// The visual effects of this work are not shown until the method returns.
	private void initContentOnJME3Thread(BasicGoodyCtx bgc) throws Throwable {

		ViewPort pvp = getPrimaryAppViewPort();
		pvp.setBackgroundColor(ColorRGBA.Blue);
		shedLight();

		GoodyTestMsgMaker gtmm = new GoodyTestMsgMaker();

		List<BasicThingActionSpec> msgs =  gtmm.makeGoodyCreationMsgs();
		for (BasicThingActionSpec msg : msgs) {
			getLogger().info("Sending: {}", msg);
			sendActionSpec(bgc, msg);
		}
		//	GoodyRenderTestContent grtc = new GoodyRenderTestContent();
		// GoodySpace gSpace = getGoodySpace();
		// hrwMapper.addHumanoidGoodies(veActConsumer, hrc);
	}

	static private void sendActionSpec(BasicGoodyCtx bgc, BasicThingActionSpec actionSpec) {
		// This winds up calling enqueueForJmeAndWait in
		// BasicGoodyEntity.attachToVirtualWorldNode
		//  1) Why does it need to wait?  This slows down the action-consuming thread.
		//  2) If it does wait, and we are already on the JME thread when we call this - hello deadlock!
		// Ident srcGraphID = srcAgentID;
		WantsThingAction.ConsumpStatus consumpStatus = bgc.consumeAction(actionSpec);
	}
	private void shedLight() {
		CogcharRenderContext cpmrc = getRenderContext();
		CoreFeatureAdapter.setupLight(cpmrc);
		shedMoreLight(cpmrc);
	}

	private void shedMoreLight(CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
		DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
		CoreFeatureAdapter.addLightToRootNode(crc, odl);
	}
	/*
	public void refreshInputBindingsAndHelpScreen(KeyBindingConfig keyBindConfig, CommandSpace cspace) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		VW_InputBindingFuncs.setupKeyBindingsAndHelpScreen(rrc, keyBindConfig, getAppStub(), 
					getJMonkeyAppSettings(), cspace);
	}	
	*/
}


/*** 
 * For comparative study:
 * 
 * During full OSGi character init, what does the o.c.b.render.opengl bundle and o.c.b.app.puma bundes do?
 * 
 * 
 *  RenderBundleUUtils.buildBonyRenderContextInOSGi(BundleContext bundleCtx, String panelKind)
 * 
		RenderConfigEmitter bce = new RenderConfigEmitter();
		BonyVirtualCharApp bvcApp = new HumanoidPuppetApp(bce);
		VirtualCharacterPanel vcp = PanelUtils.makeVCPanel(bce, panelKind);
		theDbg.logInfo("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Value of org.lwjgl.librarypath = " + System.getProperty("org.lwjgl.librarypath"));		
		theDbg.logInfo("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Forcing lowPermissions during initCharPanelWithCanvas(), to prevent JME3 forcing value into org.lwjgl.librarypath");
		JmeSystem.setLowPermissions(true);
		bvcApp.initCharPanelWithCanvas(vcp);
		*			applySettings();
					hideJmonkeyDebugInfo();
					this.createCanvas();
					// Does not work at this time or subsq:
					//applySettings();
					Canvas c = WorkaroundFuncsMustDie.makeAWTCanvas(this);
					vcp.setRenderCanvas(c);
					getBonyRenderContext().setPanel(vcp);
		// assetManager does not exist until start is called, triggering simpleInit callback.
		JmeSystem.setLowPermissions(false);
		resultBRC = bvcApp.getBonyRenderContext();
		bundleCtx.registerService(BonyRenderContext.class.getName(), resultBRC, null);
		return resultBRC;
		* 
		* 
		* ---------------------------------------------------------------
		* 
Above is what the o.c.b.render.opengl BundleActivator does during it's start() method.
Note that this does *not* launch an actual JME3 window, it just sets the table for the process below
		* 
		* 
		* 
That happens
* 
* 	private void initVWorldUnsafe(final PumaAppContext pac, PumaContextMediator mediator) throws Throwable {
		// Mediator must be able to decide panelKind before the HumanoidRenderContext is built.
		String panelKind = mediator.getPanelKind();
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Calling initHumanoidRenderContext()");
		PumaVirtualWorldMapper pvwm = pac.getOrMakeVWorldMapper();
		HumanoidRenderContext hrc = pvwm.initHumanoidRenderContext(panelKind);
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Calling mediator.notifyPanelsConstructed()");
		mediator.notifyPanelsConstructed(pac);
		/*
		 * Start up the JME OpenGL canvas, which will in turn initialize the Cogchar rendering "App" (in JME3 lingo).
		 *
		 * Firing up the OpenGL canvas requires access to sun.misc.Unsafe, which must be explicitly imported by
		 * ext.bundle.osgi.jmonkey, and explicitly allowed by the container when using Netigso

		boolean allowJFrames = mediator.getFlagAllowJFrames();
		if (allowJFrames) {
			WindowAdapter winLis = new WindowAdapter() {
				@Override public void	windowClosed(WindowEvent e) {
					getLogger().warn("PumaBooter caught window CLOSED event for OpenGL frame:  {}", e);
					notifyVWorldWindowClosed();
				}
			};
			getLogger().debug("%%%%%%%%%%%%%%%%%%% Calling startOpenGLCanvas");
			pac.startOpenGLCanvas(allowJFrames, winLis);
			* calls  pvwm.startOpenGLCanvas(wrapInJFrameFlag, optWinLis); 
			* 
			* 	public void startOpenGLCanvas(boolean wrapInJFrameFlag, WindowListener optWindowEventListener) throws Exception {

		if (wrapInJFrameFlag) {
			VirtualCharacterPanel vcp = getPanel();
			logInfo("Making enclosing JFrame for VirtCharPanel: " + vcp);
			JFrame jf = vcp.makeEnclosingJFrame("CCRK-PUMA Virtual World");
			setFrame(jf);
			if (optWindowEventListener != null) { 
				jf.addWindowListener(optWindowEventListener);
			}
		}
		BonyVirtualCharApp app = getApp();
		if (app.isCanvasStarted()) {
			logWarning("JMonkey Canvas was already started!");
		} else {
			logInfo("Starting JMonkey canvas - hold yer breath! [[[[[[[[[[[[[[[[[[[[[[[[[[");
			app.startJMonkeyCanvas();
			logInfo("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]  Finished starting JMonkey canvas!");
		}
			* which does this:
			*			final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
						GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		
						RepoClient rc = pcm.getMainConfigRepoClient();		
						HumanoidRenderContext hrc = getHumanoidRenderContext();
							hrc.initCinematicParameters();
							hrc.setupHominoidCameraManager();
		
						KeyBindingConfig currKeyBindCfg = new KeyBindingConfig();
						// Hook-in for Goody system
						GoodyRenderRegistryClient grrc = hrc.getGoodyRenderRegistryClient();
						GoodyFactory gFactory = GoodyFactory.createTheFactory(grrc, hrc);
					// Setup the humanoid "goodies"
					HumanoidRenderWorldMapper hrwMapper = new HumanoidRenderWorldMapper();
						VWorldEntityActionConsumer veActConsumer = gFactory.getActionConsumer();
						hrwMapper.addHumanoidGoodies(veActConsumer, hrc);
							List<Ident> worldConfigIdents = gce.entityMap().get(EntityRoleCN.VIRTUAL_WORLD_ENTITY_TYPE);
							for (Ident configIdent : worldConfigIdents) {
							initCinematicStuff(gce, configIdent, rc, gFactory, router);
							Ident graphIdent = gce.ergMap().get(configIdent).get(EntityRoleCN.INPUT_BINDINGS_ROLE);
							KeystrokeConfigEmitter kce = new KeystrokeConfigEmitter();

						currKeyBindCfg.addBindings(rc, graphIdent, kce);
		
				hrc.refreshInputBindingsAndHelpScreen(currKeyBindCfg, cspace);
			
			getLogger().debug("%%%%%%%%%%%%%%%%%%% startOpenGLCanvas completed, enqueueing final boot phase on JME3 thread, and waiting for completion.");

	------------------------
	* After all that, PumaBooter does this.
		 * This call first waits for a WorkaroundAppStub to appear, so that it can enqueue the main call on the JME3 
		 * thread.  THEN it waits for that enqueued call to finish, allowing us to be sure that all this init is
		 * completed, on the proper thread, before we continue the PUMA boot process.
				
		hrc.runPostInitLaunchOnJmeThread();
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Context.runPostInitLaunch completed");
 */