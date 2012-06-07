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

package org.cogchar.render.sys.core;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.sys.physics.ScoreBoard;
import com.jme3.app.SimpleApplication;
import com.jme3.input.FlyByCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import java.awt.Canvas;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WorkaroundFuncsMustDie {
	
	static Logger theLogger = LoggerFactory.getLogger(WorkaroundFuncsMustDie.class);
	
	public static void setupCameraLightAndViewport(BonyRenderContext bc) { 
		SimpleApplication app = bc.getApp();
		FlyByCamera fbc = app.getFlyByCamera();
        fbc.setDragToRotate(true);
		//fbc.setMoveSpeed(10f); //This is set in HumanoidRenderContext.initCameraAndLights()
		app.setPauseOnLostFocus(false);
		ViewPort vp = app.getViewPort();
		vp.setBackgroundColor(ColorRGBA.LightGray);
//    initKeys();

		// JME2-only so far, it seems
		// JoystickInput.setProvider( InputSystem.INPUT_SYSTEM_LWJGL );
		
		/* Working on killing this method - the items below no longer seem necessary - Ryan Biggs 9 May 2012
		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.4f, -0.5f, -0.5f).normalizeLocal());
		app.getRootNode().addLight(dl);	
		
		Camera cam = app.getCamera();

		Quaternion camRotQ = new Quaternion(0.0f, 1.0f, 0.5f, 0.0f);
		cam.setAxes(camRotQ);		
		*/ 
	}	
	public static void initScoreBoard(BonyRenderContext bc) {
		SimpleApplication app = bc.getApp(); // Should be from registry, not this way
		AppSettings settings = app.getContext().getSettings(); // Should be from registry, not this way
		int numScoreRows = 4;
		int rowHeight = 50;
		int boardWidth = settings.getWidth();
		int baseX = 20;
		int baseY = settings.getHeight() - numScoreRows * rowHeight;
		float textSizeMult = 0.5f;
		ScoreBoard sb = new ScoreBoard(app.getAssetManager(), app.getGuiNode(), baseX, baseY, boardWidth, rowHeight, numScoreRows, textSizeMult);
		bc.setScoreBoard(sb); // Goofy way to do it
	}
	public static Canvas makeAWTCanvas(SimpleApplication app) {
		AppSettings settings = app.getContext().getSettings();
		theLogger.info("making AWTCanvas in WorkaroundFuncsMustDie: Size is " + settings.getWidth() + "x" + settings.getHeight());
		return makeAWTCanvas(app, settings.getWidth(), settings.getHeight());	
	}	
	public static Canvas makeAWTCanvas(SimpleApplication app, int width, int height) {
/* In a silent applet, we might do:
 *         settings.setAudioRenderer(null);
		 // setLowPermissions has important effects on native libs and classpath resource loading.
        JmeSystem.setLowPermissions(true);
 */
		
		// This causes JME ClasspathLocator to use:
		//							url = ClasspathLocator.class.getResource("/" + name);
        //			instead of		url = Thread.currentThread().getContextClassLoader().getResource(name);
		// JmeSystem.setLowPermissions(true);
		// Again, see new workaround in Activator (actually SETTING the contextClassLoader!!)
		
		JmeContext ctx = app.getContext();
		JmeCanvasContext cctx = (JmeCanvasContext) ctx;
		Canvas awtCanvas = cctx.getCanvas();
        awtCanvas.setSize(width, height);
		return awtCanvas;
	}
	
	// Adding this so we can enqueue requests to add RDF lights on main thread - seems like this is a bit of a
	// WorkAroundFunc for now, so here it is:
	public static void enqueueCallable(BonyRenderContext bc, Callable callThis) {
		bc.getApp().enqueue(callThis);
	}	
	
	// Probably we just need this one. Plan to get rid of the first once I make sure I'm not blowing up lights!
	public static Future<Object> enqueueCallableReturn(BonyRenderContext bc, Callable callThis) {
		return bc.getApp().enqueue(callThis);
	}

}
