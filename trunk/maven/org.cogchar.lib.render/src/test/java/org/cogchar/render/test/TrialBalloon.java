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
package org.cogchar.render.test;

import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TrialBalloon extends CogcharPresumedApp {

	private TempMidiBridge myTMB = new TempMidiBridge();
	// In this test, we have the luxury of knowing the exact class of our associated context.
	private TB_RenderContext myTBRC;

	public static void main(String[] args) {
		// These two lines activate Log4J without requiring a log4j.properties file.  
		// However, when a log4j.properties file is present, these commands should not be used.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		TrialBalloon app = new TrialBalloon();
		
		app.start();
		app.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main() starting GridSpaceTest");
		org.cogchar.api.space.GridSpaceTest.go();
		app.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main() starting config-load test");
		// app.optLoadConfig();
		app.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ End of main()");
	}
	private void optLoadConfig() {
		ZZConfigReader zzcr = new ZZConfigReader();
		zzcr.readConf();
	}
	@Override public void start() {
		try {
			getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Calling initMidiRouter()");
			myTMB.initMidiRouter();
			
			getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Calling super.start()");
			super.start();
			getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from super.start() - JME3 render thread (probably LWJGL) is now launched.");
			boolean flag_sleepTest = false;
			if (flag_sleepTest) {
				getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Sleeping 5 sec as a test, to allow JME3 thread to get ahead.");
				Thread.sleep(5000);
				getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from sleep()");
			}
			// With no sleep, we get:
			// 1992 [main] INFO org.cogchar.render.test.TrialBalloon  - ^^^^^^^^^^^^^^^^^^^^^^^^ Returned from initMidiRouter(), returning from start()
			// 2154 [main] INFO org.cogchar.render.test.TrialBalloon  - ^^^^^^^^^^^^^^^^^^^^^^^^ End of main()
			// 2345 [LWJGLRenderer Thread] INFO org.cogchar.render.app.core.CogcharPresumedApp  - ********************* CogcharPresumedApp.initialize() called
			// But with sleep:
			// 2536 [main] INFO org.cogchar.render.test.TrialBalloon  - ^^^^^^^^^^^^^^^^^^^^^^^^ Returned from super.start() - render thread is now launched? Sleeping 5 sec
			// 2982 [LWJGL Renderer Thread] INFO org.cogchar.render.app.core.CogcharPresumedApp  - ********************* CogcharPresumedApp.initialize() called
			// It is bad to depend on whether the rest of this start() method executes before or after the App-init()
			// or subsequent activity.  So, generally speaking, this start() is an uncertain place to do anything
			// involving JME3.  But launching some other system threads, e.g. MIDI, is a reasonable thing to do.
			

			getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from initMidiRouter(), returning from start()");
		} catch (Throwable t) {
			getLogger().error("start() caught: ", t);
		}
	}
	// This is an important setup callback, linking us in to the Cogchar rendering abstraction layer.
	// We minimize our dependence on JME3 by coding against the Cogchar RenderContext APIs, rather than
	// in our "Application" class (TrialBalloon, in this case).

	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Making CogcharRenderContext");
		TB_RenderContext rc = new TB_RenderContext();
		myTBRC = rc;
		return rc;
	}

	@Override public void simpleInitApp() {
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Calling super.simpleInitApp()");
		super.simpleInitApp();
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from super.simpleInitApp()");
		// Sets the speed of our POV camera movement.  The default is pretty slow.
		flyCam.setMoveSpeed(20);
		TrialContent tc = new TrialContent();
		CogcharRenderContext crc = getRenderContext();
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		tc.shedLight_onRendThread(crc);
		// The other args besides rrc are superfluous, since they are indirectly accessible through rrc.
		// Note that these other args are all instance variables of this TrialBalloon app, inherited from JME3 SimpleApp.
		tc.initContent3D_onRendThread(rrc, rootNode, viewPort);
		tc.initContent2D_onRendThread(rrc, guiNode, assetManager);
		tc.attachMidiCCs(myTMB);
		
		TrialCameras tcam = new TrialCameras();
		tcam.setupCamerasAndViews(rrc, crc);
		
		tcam.attachMidiCCs(myTMB);
		
		
	}

	@Override public void destroy() {
		getLogger().info("JME3 destroy() called");
		super.destroy();
		getLogger().info("Cleaning up MIDI bridge");
		myTMB.cleanup();
		getLogger().info("MIDI cleanup finished");
	}

	public class TB_RenderContext extends ConfiguredPhysicalModularRenderContext {

		@Override public void doUpdate(float tpf) {
			// We are on the JME3 thread, so: 
			// 1) We want to be quick (avoid logging) 
			// 2) We have direct access to the scene graph.
			super.doUpdate(tpf);
			// Goals:  Demonstrate moving+changing intersections of JME3 features, influenced by MIDI-input.
			// Make 3D cameras+viewports flexible and useful.
			// Manage the OpenGL canvas space in efficient, useful ways.
			// (Later the 3D output will be supplemented by useful MIDI LED output).
			// Making use of avail math infrastructre:  Symja and the Cogchar-core "Space" API.
		}
	}
}
