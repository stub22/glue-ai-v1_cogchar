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
import javax.swing.JFrame;

import org.robokind.api.motion.Robot;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;

import org.osgi.framework.BundleContext;

import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelRobotUtils;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.sys.VirtCharPanel;

import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLoader;

import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext {

	static Logger theLogger = LoggerFactory.getLogger(PumaAppContext.class);
	private BundleContext myBundleContext;

	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}

	public PumaDualCharacter makeDualCharForSwingOSGi(String dualCharURI) throws Throwable {
		startOpenGLCanvas(dualCharURI, true);
		return makeDualRobotChar(dualCharURI);
	}

	private BonyRenderContext fetchBonyRenderContext() {
		return RenderBundleUtils.getBonyRenderContext(myBundleContext);
	}
	// TODO: add URI based lookup for multiple BCs

	public BonyRenderContext getBonyRenderContext(String bonyCharURI) {
		return fetchBonyRenderContext();
	}

	public PumaDualCharacter makeDualRobotChar(String bonyCharURI)
			throws Throwable {
		
		BonyRenderContext bc = getBonyRenderContext(bonyCharURI);
		PumaDualCharacter pdc = new PumaDualCharacter(bc, myBundleContext);
		pdc.connectBonyDualForURI(bonyCharURI);
		return pdc;
	}

	public void startOpenGLCanvas(String dualCharURI, boolean wrapInJFrameFlag) throws Exception {
		BonyRenderContext bc = getBonyRenderContext(dualCharURI);
		theLogger.info("Got BonyRenderContext: " + bc);

		if (bc != null) {
			if (wrapInJFrameFlag) {
				VirtCharPanel vcp = bc.getPanel();
				theLogger.info("Got VirtCharPanel: " + vcp);
				// Frame must be packed after panel created, but created 
				// before startJMonkey.  If startJMonkey is called first,
				// we often hang in frame.setVisible() as JMonkey tries
				// to do some magic restart deal that doesn't work as of
				// jme3-alpha4-August_2011.
				JFrame jf = vcp.makeEnclosingJFrame();
				theLogger.info("Got Enclosing Frame, adding to BonyRenderContext for WindowClose triggering: " + jf);
				// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
				bc.setFrame(jf);
			}
			BonyVirtualCharApp app = bc.getApp();

			if (app.isCanvasStarted()) {
				theLogger.warn("JMonkey Canvas was already started!");
			} else {
				// Set the contextCL before spawning the JMonkey run-loop thread,
				// so that JMonkey can find its builtin resources.
				
				// Also see HumanoidPuppetApp.initHumanoidStuff, which uses the 
				// contents-asset loader installed by RenderBundleActivator.start(),
				// which happens to be ResourceLoader from our render.resources bundle.
				
				// Finally, also see the commented out callback overrides in 
				// DemoApp, which could also be used to achieve the same purpose
				// as this wrapper, at a finer grain, in case we wanted to separate 
				// JMonkey builtin assets from the JMonkey classes.

				//JmonkeyAssetLoader frameworkAL = app.getFrameworkAssetLoader();
				// This does not seem to control the classloader of this thread
				/*
     [java] Dec 22, 2011 1:56:22 AM com.jme3.app.Application handleError
     [java] SEVERE: Uncaught exception thrown in Thread[LWJGL Renderer Thread,5,
main]
     [java] java.lang.IllegalStateException: No loader registered for type "fnt"

     [java]     at com.jme3.asset.DesktopAssetManager.loadAsset(DesktopAssetMana
ger.java:248)
     [java]     at com.jme3.asset.DesktopAssetManager.loadFont(DesktopAssetManag
er.java:374)
     [java]     at com.jme3.app.SimpleApplication.loadFPSText(SimpleApplication.
java:183)
     [java]     at com.jme3.app.SimpleApplication.initialize(SimpleApplication.j
ava:208)
     [java]     at org.cogchar.render.opengl.bony.app.BonyVirtualCharApp.initial
ize(BonyVirtualCharApp.java:105)
     [java]     at com.jme3.system.lwjgl.LwjglAbstractDisplay.initInThread(Lwjgl
AbstractDisplay.java:129)
     [java]     at com.jme3.system.lwjgl.LwjglAbstractDisplay.run(LwjglAbstractD
isplay.java:205)
     [java]     at java.lang.Thread.run(Thread.java:619)				
				 * 
				 */
				//frameworkAL.installClassLoader();
				theLogger.info("Starting JMonkey canvas - hold yer breath! [[[[[[[[[[[[[[[[[[[[[[[[[[");
				try {
					app.startJMonkeyCanvas();
				} finally {
					// Does this affect CL in child threads?
					// frameworkAL.restoreClassLoader();
				}
				theLogger.info("]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]  Finished starting JMonkey canvas!");
			}
			//((BonyStickFigureApp) app).setScoringFlag(true);			

		} else {
			theLogger.error("BonyRenderContext is NULL, cannot startOpenGLCanvas!");
		}
	}
}
