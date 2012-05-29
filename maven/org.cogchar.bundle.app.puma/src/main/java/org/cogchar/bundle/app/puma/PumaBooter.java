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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;

import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.render.app.bony.BonyVirtualCharApp;

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
			logInfo("%%%%%%%%%%%%%%%%%%% Calling setupConfigEmitters()");
			setupConfigEmitters(hrc, mediator);
			boolean allowJFrames = mediator.getFlagAllowJFrames();
/*  
Start up the JME OpenGL canvas, which will in turn initialize the Cogchar rendering "App" (in JME3 lingo).
 This step will load all the 3D models (and other rendering resources) that Cogchar needs, based
 on what is implied by the sysContextURI we supplied to the PumaAppContext constructor above.
		
 Firing up the OpenGL canvas requires access to sun.misc.Unsafe, which must be explicitly imported 
 by ext.bundle.osgi.jmonkey, and explicitly allowed by the container when using Netigso
 */
		
			logInfo("%%%%%%%%%%%%%%%%%%% Calling startOpenGLCanvas");
			pac.startOpenGLCanvas(allowJFrames);
			logInfo("%%%%%%%%%%%%%%%%%%% startOpenGLCanvas completed, enqueueing final boot phase on JME3 thread");
			
			
			BonyVirtualCharApp bvcJME3App = hrc.getApp();
	
	/*
 Now we have an Cogchar+JME3+OpenGL canvas, connected to our Netbeans Swing App.  
 So, let's connect the Cogchar PUMA application (configured by implications of 
 the sysContextURI and sysLocalTempConfigDir used in setupConfigEmitters() above).
 The result is a list of connected "dual" characters, which each have a presence 
 in both Cogchar virtual space and Robokind physical space.			
 * 
 * To avoid IllegalStateException: Scene graph is not properly updated for rendering, we enqueue
 * this work to occure on JME3 update thread.
 */	
			java.util.concurrent.Future<Throwable> finalBootPhaseFut = bvcJME3App.enqueue(new java.util.concurrent.Callable<Throwable>() {
				public Throwable call() throws Exception {
					try {
						logInfo("%%%%%%%%%%%%%%%%%%% Callable on JME3 thread is calling postInitLaunch()");

						hrc.postInitLaunch();

						logInfo("%%%%%%%%%%%%%%%%%%% postInitLaunch() completed, Callable on JME3 thread is returning");
						return null;
					} catch (Throwable t) {
						
						return t;
					}
				}
			});
			
			logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% Waiting for our postInitLaunch-bootPhase to complete()");
			Throwable fbpThrown = finalBootPhaseFut.get();
			if (fbpThrown != null) {
				throw new Exception("FinalBootPhase returned an error", fbpThrown);
			}
			
			logInfo("%%%%%%%%%%%%%%%%%%% Final bootPhase future completed , calling connectDualRobotChars()");
			// If we try to do this inside the callable above (under certain conditions), we can get hung
			// up when RobotServiceContext calls RobotUtils.registerRobot()
			pac.connectDualRobotChars();
			
			logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% connectDualRobotChars() completed -  PUMA BOOT SUCCESSFUL!  8-)");
			
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

	
/*  Old init under NB:
		
		String sysContextURI = "NBURI:huzzah";             
		String sysLocalTempConfigDir = Installer.getVirtcharNBClusterDir();
		PumaAppContext pac = new PumaAppContext(bundleCtx, sysContextURI, sysLocalTempConfigDir);
		BonyRenderContext brc = pac.getHumanoidRenderContext();
		initVirtualCharPanel(brc);
		pac.startOpenGLCanvas(false);
		pac.connectDualRobotChars();
 */
}
