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
package org.cogchar.render.trial;

import org.cogchar.bind.midi.in.TempMidiBridge;
import org.cogchar.bind.midi.in.CCParamRouter;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;
// import org.cogchar.render.test.ZZConfigReader;

/**
 * @author Stu B. <www.texpedient.com>
 	// Goals:  Demonstrate moving+changing intersections of JME3 features, influenced by MIDI-input,
	// suppementing visual cues with optional MIDI output sounds and MIDI LED output.
	// Make 3D cameras+viewports flexible and useful.
	// Mark coordinates in space, on objects, and in camera views - to help the user/author visualize dimensions
	// Manage the OpenGL canvas space in efficient, useful ways.
	// (Later the 3D output will be supplemented by useful MIDI LED output).
	// Making use of avail math infrastructre:  Symja and the Cogchar-core "Space" API.
	// Make use of JME3 builtin mesh shapes, color effects, transparency, fonts, and node hierarchies.
	// Show uses of path-based animation, reconciled into world coordinates for analysis and action.
 */
public class TrialBalloon extends CogcharPresumedApp {

	protected TempMidiBridge myTMB = new TempMidiBridge();
	// In this test, we have the luxury of knowing the exact class of our associated context.
	protected TB_RenderContext myTBRC;
	protected TrialContent		myContent;
	protected List<TrialUpdater>		myUpdaters = new ArrayList<TrialUpdater>();

	public static void main(String[] args) {
		// These two lines activate Log4J without requiring a log4j.properties file.  
		// However, when a log4j.properties file is present, these commands should not be used.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		TrialBalloon tbApp = new TrialBalloon();
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main(R) calling initMidi()");
		// Initialize any MIDI stuff.
		tbApp.initMidi();
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main(R) calling JME3 start(), which will in turn call TrialBalloon.simpleInitApp()");
		// Start the JME3 Virtual world.
		tbApp.start();
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main(R) starting GridSpaceTest");
		org.cogchar.api.space.GridSpaceTest.goGoGo();
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^  main(R) starting config-load test");
		
		// app.optLoadConfig();
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ main(R) calling playMidiOutput()");
		tbApp.playMidiOutput();
		
		tbApp.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ End of main(R)");
	}
	public void initMidi() { 
		myTMB.initMidiRouter();
	}
	public void playMidiOutput() { 
		myTMB.playSomeOutput();
	}
	/*
	private void optLoadConfig() {
		ZZConfigReader zzcr = new ZZConfigReader();
		zzcr.readConf();
	}
	*/
	@Override public void start() {
		try {
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
			
			getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returning from start()");
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
/**
 * 
 */
	@Override public void simpleInitApp() {
		getLogger().info("^^^^^^^^^^^^^^^^^^ We are on JME3 thread (inside start()), calling super.simpleInitApp()");
		super.simpleInitApp();
		getLogger().info("Returned from super.simpleInitApp() - calling doMoreSimpleInit.");
		doMoreSimpleInit();
	}
	protected void doMoreSimpleInit() {
		getLogger().info("Returned from super.simpleInitApp() - still on JME3 thread, setting flyCam speed.");
		// Sets the speed of our POV camera movement.  The default is pretty slow.
		flyCam.setMoveSpeed(20);
		myContent = new TrialContent();
		CogcharRenderContext crc = getRenderContext();
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		getLogger().info("will now init, in order: lights, 3D content, 2D content, MIDI controllers, extra cameras");

		myContent.shedLight_onRendThread(crc);
		// The other args besides rrc are superfluous, since they are indirectly accessible through rrc.
		// Note that these other args are all instance variables of this TrialBalloon app, inherited from JME3 SimpleApp.
		myContent.initContent3D_onRendThread(rrc, rootNode);
		
		viewPort.setBackgroundColor(ColorRGBA.Blue);
		
		// Camera-viewports are placed in the screen coordinate system, so we might consider them to be a kind
		// of 2-D content.  They are part of that layout, anyhoo.
		myContent.initContent2D_onRendThread(rrc, guiNode, assetManager);
		
		attachVWorldUpdater(myContent);
		
		CCParamRouter ccpr = myTMB.getCCParamRouter();
		// Hand the MIDI 
		myContent.attachMidiCCs(ccpr);
		
		TrialCameras tcam = new TrialCameras();
		tcam.setupCamerasAndViews(rrc, crc, myContent);
		// Hand the MIDI bindings to the camera-aware app.
		tcam.attachMidiCCs(ccpr);
		
	}

	public void attachVWorldUpdater(TrialUpdater tu) {
		myUpdaters.add(tu);
	}

	@Override public void destroy() {
		getLogger().info("JME3 destroy() called in TrialBalloon");
		super.destroy();
		getLogger().info("Cleaning up MIDI bridge");
		myTMB.cleanup();
		getLogger().info("MIDI cleanup finished - end of TrialBalloon.destroy()");
	}
	public class TB_RenderContext extends ConfiguredPhysicalModularRenderContext {

		@Override public void doUpdate(float tpf) {
			// We are on the JME3 thread, so: 
			// 1) We want to be quick (avoid logging) 
			// 2) We have direct access to the scene graph.
			super.doUpdate(tpf);
			RenderRegistryClient rrc = getRenderRegistryClient();
			for (TrialUpdater u : myUpdaters) {				
				u.doUpdate(rrc, tpf);
			}
		}
	}

}
