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

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.opengl.scene.TextMgr;


import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

// import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.sys.context.CoreFeatureAdapter;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TrialBalloon extends CogcharPresumedApp {

	private		TempMidiBridge			myTMB = new TempMidiBridge();
	// In this test, we have the luxury of knowing the exact class of our associated context.
	private		TB_RenderContext		myTBRC;

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		TrialBalloon app = new TrialBalloon();
		app.start();
		app.getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ End of main()");
	}

	@Override public void start() {
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Calling super.start()");
		super.start();
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from super.start(), calling initMidiRouter()");
		myTMB.initMidiRouter();
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from initMidiRouter(), returning from start()");

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
		// At this point, the R
		flyCam.setMoveSpeed(20);
		TrialContent tc = new TrialContent();
		CogcharRenderContext crc = getRenderContext();
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		tc.shedLight(crc);
		tc.initContent3D_onJME3thread(rrc, rootNode, viewPort);
		tc.initContent2D_onJME3thread(rrc, guiNode, assetManager);
	}

	@Override public void destroy(){
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
		}
	}
}
