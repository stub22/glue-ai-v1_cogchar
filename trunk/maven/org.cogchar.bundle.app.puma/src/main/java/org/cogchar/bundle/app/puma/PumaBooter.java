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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;

import org.appdapter.help.repo.QueryInterface;
import org.appdapter.help.repo.QueryEmitter;

import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.QueryTester;

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
	
	public BootResult bootUnderOSGi(BundleContext bundleCtx) {
		PumaContextMediator cm = new PumaContextMediator();
		return bootUnderOSGi(bundleCtx, cm);
	}
	
// 	public BootResult boot(ClassLoader appBootCL, String appBootModelResourcePath) {
	public BootResult bootUnderOSGi(BundleContext bundleCtx, PumaContextMediator mediator) {
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
			
			
			logInfo("%%%%%%%%%%%%%%%%%%% Starting query service");
			QueryInterface vqi = startVanillaQueryInterface(bundleCtx);
			
			// This method performs the configuration actions associated with the developmental "Global Mode" concept
			// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
			// be updated to relect that
			applyGlobalMode(pac);
			
			// Not in love with this, but not sure what else to do right now. PumaWebMapper implements the LiftInterface,
			// which we now want to be able to trigger updates. Currently PumaAppContext is handling those updates, so
			// PumaWebMapper needs to be able to access our pac. There's only this one PumaAppContext instance per 
			// virtual world, right? Hopefully!
			PumaWebMapper.getTheWebMapper().setAppContext(pac);
			
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
			
			// As long as we still need the classloader for bundle.render.resources, this seems a good place to set it up:
			ClassLoader myInitialBonyRdfCL = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
			pac.setCogCharResourcesClassLoader(myInitialBonyRdfCL);

			logInfo("%%%%%%%%%%%%%%%%%%% Context.runPostInitLaunch completed , calling connectDualRobotChars()");
			pac.setContextMediator(mediator);
			pac.connectDualRobotChars();
			
			logInfo("%%%%%%%%%%%%%%%%%%% connectDualRobotChars() completed , calling initCinema()");
			
			// Lights, Cameras, and Cinematics were once configured during PumaDualCharacter init
			// Since we can support multiple characters now (and connect cameras to them), this needs to happen after connectDualRobotChars()
			// We'll let pac take care of this, since it is currently "Home of the Global Mode"
			// (even though global mode is currently initially applied here. Any of this may change.)
			pac.initCinema(vqi);
			
			logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% initCinema() completed -  PUMA BOOT SUCCESSFUL!  8-)");
			
			result.myStatus = BootStatus.BOOTED_OK;
			
		} catch (Throwable t) {
			logError("Error in PumaBooter 8-(", t);
			result.myStatus = BootStatus.BOOT_FAILED;
			result.myThrowable = t;
		}
		return result;
	}
	
	// Registers the QueryEmitter service, currently with an empty lifecycle.
	// This service will be used by managed services needing query config
	// Currently, that's: LifterLifecycle
	public static QueryInterface startVanillaQueryInterface(BundleContext context) {
		// We want to make explicity the assumptions about what goes into our QueryEmitter.
		// On 2012-09-12 Stu changed "new QueryEmitter()" to makeVanillaQueryEmitter,
		// but perhaps there is some more adjustment to do here for lifecycle compat.
		QueryEmitter qemit = QueryTester.makeVanillaQueryEmitter();
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(qemit, QueryInterface.class);
    	OSGiComponent queryComp = new OSGiComponent(context, lifecycle);
    	queryComp.start();
		return qemit;
	}
	
	private Repo findMainRepo(PumaContextMediator mediator) {
		Repo r = null;
		
		return r;
	}
	
	// Another bad thing about having lifter access this GlobalConfigService stuff (see below) through managed services:
	// This method must be public so things like o.f.bundle.repo can call it (see below)
	public static void applyGlobalMode(PumaAppContext pac) {
		GlobalConfigEmitter gce = new GlobalConfigEmitter(
				new FreeIdent(PumaModeConstants.rkrt+PumaModeConstants.globalMode, PumaModeConstants.globalMode));
		
		// Great, now we have a GlobalConfigEmitter! Now, what do we do with it?
		// GlobalConfigEmitter is in o.c.lib.core (for now) since it seems a "bottom level" function.
		// But for now, perhaps we can assume that the uses of the information in global config will occur at the
		// top, PUMA layer. So let's store this in our PumaAppContext.
		// This may change as we work through this GlobalMode stuff. gce could be stored "deeper", or maybe 
		// GlobalConfigEmitter (or similar) will move "upwards" to PUMA.
		pac.setGlobalConfig(gce);
		
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
		pac.startGlobalConfigService();
	}
}
