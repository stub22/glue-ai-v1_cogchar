/*  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;


import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.QueryEmitter;
import org.cogchar.blob.emit.QueryInterface;


import  org.appdapter.core.store.Repo;
import org.osgi.framework.Bundle;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaBooter extends BasicDebugger {
	
	
	
	public enum BootStatus {
		BOOTING,
		BOOTED_OK,
		BOOT_FAILED
	}
	public class BootResult {
		public	BootStatus			myStatus;
		public	List<String>		myMessageList = new ArrayList<String>();
		public	Throwable			myThrowable;
	}
	
	public static class ContextMediator extends BasicDebugger {
		/**
		 * Called after panels constructed but before startOpenGLCanvas.
		 * Allows GUI to intervene and realize the panels as needed.
		 * This step is generally, when getFlagAllowJFrames returns false, e.g. when we
		 * are running under NB platform and we want to manage windows manually).
		 * 
		 * @param ctx
		 * @throws Throwable 
		 */
		public void notifyContextBuilt(PumaAppContext ctx) throws Throwable { 
		}
		public boolean getFlagAllowJFrames() {
			return true;
		}
		public String getOptionalFilesysRoot() {
			return null;
		}
		public String getSysContextRootURI() {
			String uriPrefix = "http://model.cogchar.org/char/bony/";
			String bonyCharUniqueSuffix = "0x0000FFFF";
			String sysContextURI = uriPrefix + bonyCharUniqueSuffix;
			return sysContextURI;
		}
		public String getPanelKind() {
			return "SLIM";
		}
		
	}
	public BootResult bootUnderOSGi(BundleContext bundleCtx) {
		ContextMediator cm = new ContextMediator();
		return bootUnderOSGi(bundleCtx, cm);
	}
	
// 	public BootResult boot(ClassLoader appBootCL, String appBootModelResourcePath) {
	public BootResult bootUnderOSGi(BundleContext bundleCtx, ContextMediator mediator) {
		logInfo("%%%%%%%%%%%%%%%%%%% Beginning bootUnderOSGi");
		BootResult result  = new BootResult();
		result.myStatus = BootStatus.BOOTING;
		try {
			// forceLog4jConfig();

			// String debugTxt = "sysContextURI = [" + sysContextURI + "]";
			// logInfo("======================================== Starting " + debugTxt);
			String oprFilesysRoot = mediator.getOptionalFilesysRoot();
			logInfo("%%%%%%%%%%%%%%%%%%% Creating PumaAppContext");
			final PumaAppContext pac = new PumaAppContext(bundleCtx);
			// Mediator must be able to decide panelKind before the HumanoidRenderContext is built.
			String panelKind = mediator.getPanelKind();
			logInfo("%%%%%%%%%%%%%%%%%%% Calling initHumanoidRenderContext()");
			final HumanoidRenderContext hrc = pac.initHumanoidRenderContext(panelKind);
			logInfo("%%%%%%%%%%%%%%%%%%% Calling mediator.notifyContextBuilt()");
			mediator.notifyContextBuilt(pac);
			
			/* At this point we have a blank, generic hrc to work with.
			 * No characters or config have been populated, no OpenGL 
			 * window has been opened, and no connection has been made
			 * to Robokind.
			 * 
			 * We are ready to decide "what kind of application are we running?",
			 * without irrevocably committing any more than necessary (so that
			 * user/agents can adjust/reshape the runtime as it progresses).
			 * 
			 * As of 2012-07-19, we are still relying on a half-baked notion
			 * of "ConfigEmitters".  So now we will "set them up".  What
			 * does that mean?   Should it be possible for anything in these
			 * emitters to influence the pac.startOpenGLCanvas process?
			 * Perhaps not.  However, it makes more sense for them to influence
			 * the subsequent runPostInitLaunchOnJmeThread().
			 * 
			 */
			
			
			logInfo("%%%%%%%%%%%%%%%%%%% Calling setupConfigEmitters() and starting query service");
			setupConfigEmitters(hrc, mediator);
			startQueryService(bundleCtx);
			
			// This method performs the configuration actions associated with the developmental "Global Mode" concept
			// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
			// be updated to relect that
			applyGlobalMode(pac);
			
			boolean allowJFrames = mediator.getFlagAllowJFrames();
/*  
Start up the JME OpenGL canvas, which will in turn initialize the Cogchar rendering "App" (in JME3 lingo).
 		
 Firing up the OpenGL canvas requires access to sun.misc.Unsafe, which must be explicitly imported 
 by ext.bundle.osgi.jmonkey, and explicitly allowed by the container when using Netigso
 */
		
			logInfo("%%%%%%%%%%%%%%%%%%% Calling startOpenGLCanvas");
			pac.startOpenGLCanvas(allowJFrames);
			logInfo("%%%%%%%%%%%%%%%%%%% startOpenGLCanvas completed, enqueueing final boot phase on JME3 thread");
			
			/**
			 * Populate the virtual world with humanoids, cameras, lights, and other goodies.
			 * This step will load all the 3D models (and other rendering resources) that Cogchar needs, 
			 * based on what is implied by the sysContextURI we supplied to the PumaAppContext constructor above.

			 * We enqueue this work to occur on JME3 update thread.  Otherwise we'll get an:
			 *  IllegalStateException: Scene graph is not properly updated for rendering.
			 */
			
			hrc.runPostInitLaunchOnJmeThread();
			
/*
 Connect the Cogchar PUMA application (configured by implications of 
 the sysContextURI and sysLocalTempConfigDir used in setupConfigEmitters() above).
 The result is a list of connected "dual" characters, which each have a presence 
 in both Cogchar virtual space and Robokind physical space.			

If we try to do this inside the JME3Thread callable above (under certain conditions), we can get hung
up when RobotServiceContext calls RobotUtils.registerRobot()
*/ 

			logInfo("%%%%%%%%%%%%%%%%%%% Context.runPostInitLaunch completed , calling connectDualRobotChars()");
			pac.connectDualRobotChars();
			
			logInfo("%%%%%%%%%%%%%%%%%%% connectDualRobotChars() completed , calling Context.initCinema()");
			
			// Lights, Cameras, and Cinematics were once configured during PumaDualCharacter init
			// Since we can support multiple characters now (and connect cameras to them), this needs to happen after connectDualRobotChars()
			hrc.initCinema();
			
			logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% Context.initCinema() completed -  PUMA BOOT SUCCESSFUL!  8-)");
			
			result.myStatus = BootStatus.BOOTED_OK;
			
		} catch (Throwable t) {
			logError("Error in PumaBooter 8-(", t);
			result.myStatus = BootStatus.BOOT_FAILED;
			result.myThrowable = t;
		}
		return result;
	}
	private void setupConfigEmitters(BonyRenderContext brc, ContextMediator mediator) {
		BonyConfigEmitter bonyCE = brc.getBonyConfigEmitter();
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();
		String sysContextURI = mediator.getSysContextRootURI();
		if (sysContextURI != null) {
			behavCE.setSystemContextURI(sysContextURI);
		}
		String filesysRootPath = mediator.getOptionalFilesysRoot();
		if (filesysRootPath != null) {
			behavCE.setLocalFileRootDir(filesysRootPath);
		}
	}
	
	// Registers the QueryEmitter service, currently with an empty lifecycle.
	// This service will be used by managed services needing query config
	// Currently, that's: LifterLifecycle
	public static void startQueryService(BundleContext context) {
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(new QueryEmitter(), QueryInterface.class);
    	OSGiComponent queryComp = new OSGiComponent(context, lifecycle);
    	queryComp.start();
	}
	
	private Repo findMainRepo(ContextMediator mediator) {
		Repo r = null;
		
		return r;
	}
	
	private void applyGlobalMode(PumaAppContext prc) {
		GlobalConfigEmitter gce = new GlobalConfigEmitter(
				new FreeIdent(PumaModeConstants.rkrt+PumaModeConstants.globalMode, PumaModeConstants.globalMode));
		
		// Great, now we have a GlobalConfigEmitter! Now, what do we do with it?
		// GlobalConfigEmitter is in o.c.lib.core (for now) since it seems a "bottom level" function.
		// But for now, perhaps we can assume that the uses of the information in global config will occur at the
		// top, PUMA layer. So let's store this in our PumaAppContext.
		// This may change as we work through this GlobalMode stuff. gce could be stored "deeper", or maybe 
		// GlobalConfigEmitter (or similar) will move "updwards" to PUMA.
		prc.setGlobalConfig(gce);
	}
}
