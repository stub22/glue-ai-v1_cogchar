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
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.appdapter.core.store.Repo;
import org.slf4j.LoggerFactory;

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

		public BootStatus myStatus;
		public List<String> myMessageList = new ArrayList<String>();
		public Throwable myThrowable;
	}

	/**
	 * Convenience version uses default PumaContextMediator.
	 *
	 */
	public BootResult bootUnderOSGi(BundleContext bundleCtx) {
		PumaContextMediator cm = new PumaContextMediator();
		return bootUnderOSGi(bundleCtx, cm);
	}

	/**
	 * Client controls the boot process through the mediator.
	 *
	 * @param bundleCtx
	 * @param mediator
	 * @throws Throwable
	 */
	protected void pumaBootUnsafeUnderOSGi(BundleContext bundleCtx, PumaContextMediator mediator) throws Throwable {


		// forceLog4jConfig();

		// String debugTxt = "sysContextURI = [" + sysContextURI + "]";
		// logInfo("======================================== Starting " + debugTxt);
		/* Stu 2012-10-10 - the fileSys root isn't being used currently.  The sysConfigURI thing is mostly unused, too.
		 * So, what we need now is
		 */
		String optFilesysRoot = mediator.getOptionalFilesysRoot();
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Creating PumaAppContext");
		final PumaAppContext pac = new PumaAppContext(bundleCtx, mediator);

		/*
		 * At this point we have blank, generic PAC + HRC (if vWorld) context objects to work with. PAC + HRC are
		 * typically each a singleton, but not assumed to be. No characters or config have been populated, no OpenGL
		 * window has been opened, and no connection has been made to Robokind.
		 */

		getLogger().debug("%%%%%%%%%%%%%%%%%%% Starting repository-backed config services");

		pac.startRepositoryConfigServices();

		/*The mediator should now do any special init that it wants to, but without assuming GUI exists.
		 * This stage includes getting the FIRST whack (both read and write) at the configuration services.  
		 * However, the default mediator impl does nothing.  */

		mediator.notifyContextBuilt(pac);
		// Mediator must have an answer for this before any config is loaded, presently.
		boolean wantVWorld = mediator.getFlagIncludeVirtualWorld();
		if (wantVWorld) {
			initVWorldUnsafe(pac, mediator);
		}
		/*
		 * Connect the Cogchar PUMA application (configured by implications of the sysContextURI and
		 * sysLocalTempConfigDir used in setupConfigEmitters() above). The result is a list of connected "dual"
		 * characters, which each have a presence in both Cogchar virtual space and Robokind physical space.			 *
		 * If we try to do this inside the JME3Thread callable above (under certain conditions), we can get hung up when
		 * RobotServiceContext calls RobotUtils.registerRobot()
		 */

		
		boolean wantChars = mediator.getFlagIncludeCharacters();
		if (wantChars) {
			// Currently this btarget bundle contains bony-config stuff that goes beyond just rendering resources.
			ClassLoader myInitialBonyRdfCL = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
			pac.setCogCharResourcesClassLoader(myInitialBonyRdfCL);

			getLogger().debug("%%%%%%%%%%%%%%%%%%% calling connectDualRobotChars()");
			pac.connectDualRobotChars();

			mediator.notifyCharactersLoaded(pac);
		}
		
		if (wantVWorld) {
			getLogger().debug("%%%%%%%%%%%%%%%%%%% connectDualRobotChars() completed , calling initCinema()");

			// Lights, Cameras, and Cinematics were once configured during PumaDualCharacter init
			// Since we can support multiple characters now (and connect cameras to them), this needs to 
			// happen after connectDualRobotChars().
			// We'll let pac take care of this, since it is currently "Home of the Global Mode"
			pac.initCinema();
		}
		mediator.notifyBeforeBootComplete(pac);
	}

	/**
	 * Entry point for the PUMA application system when running in an OSGi environment. Presumes that an SLF4J logging
	 * binding is already in place. Normally this is *not* called directly from another bundle's activator. Instead, it
	 * is called after all initial bundles (for your application) are loaded, which is signaled in a FrameworkEvent
	 *
	 * @param bundleCtx - an OSGi bundle
	 * @param mediator - users customize this object to configure/influence the boot process.
	 * @return a BootResult indicating the status of boot attempt. No app data is passed here.
	 */
	public BootResult bootUnderOSGi(BundleContext bundleCtx, PumaContextMediator mediator) {
		logInfo("%%%%%%%%%%%%%%%%%%% Beginning bootUnderOSGi");
		logError("^^^^ not really an error - just high priority ^^^^^^^^ SLF4J ILoggerFactory = " + LoggerFactory.getILoggerFactory());
		BootResult result = new BootResult();
		result.myStatus = BootStatus.BOOTING;
		try {

			pumaBootUnsafeUnderOSGi(bundleCtx, mediator);

			getLogger().info("%%%%%%%%%%%%%%%%%%%%%%%%% pumaBootUnsafe() completed without exception - PUMA BOOT SUCCESSFUL!  8-)");
			result.myStatus = BootStatus.BOOTED_OK;
		} catch (Throwable t) {
			getLogger().error("Error in PumaBooter 8-(", t);
			result.myStatus = BootStatus.BOOT_FAILED;
			result.myThrowable = t;
		}
		return result;
	}

	private Repo findMainRepo(PumaContextMediator mediator) {
		Repo r = null;

		return r;
	}

	private void initVWorldUnsafe(final PumaAppContext pac, PumaContextMediator mediator) throws Throwable {
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
		 */
		boolean allowJFrames = mediator.getFlagAllowJFrames();
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Calling startOpenGLCanvas");
		pac.startOpenGLCanvas(allowJFrames);
		getLogger().debug("%%%%%%%%%%%%%%%%%%% startOpenGLCanvas completed, enqueueing final boot phase on JME3 thread");
		/**
		 * Populate the virtual world with humanoids, cameras, lights, and other goodies. This step will load all the 3D
		 * models (and other rendering resources) that Cogchar needs, based on what is implied by the sysContextURI we
		 * supplied to the PumaAppContext constructor above.
		 *
		 * We enqueue this work to occur on JME3 update thread. Otherwise we'll get an: IllegalStateException: Scene
		 * graph is not properly updated for rendering.
		 */
		hrc.runPostInitLaunchOnJmeThread();
		getLogger().debug("%%%%%%%%%%%%%%%%%%% Context.runPostInitLaunch completed");
	}
}
