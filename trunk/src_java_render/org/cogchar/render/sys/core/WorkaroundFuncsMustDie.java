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

import org.cogchar.render.opengl.bony.world.ScoreBoard;
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
import java.awt.Canvas;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WorkaroundFuncsMustDie {
	public static void setupCameraLightAndViewport(BonyRenderContext bc) { 
		SimpleApplication app = bc.getApp();
		FlyByCamera fbc = app.getFlyByCamera();
        fbc.setDragToRotate(true);
		fbc.setMoveSpeed(10f);
		app.setPauseOnLostFocus(false);
		ViewPort vp = app.getViewPort();
		vp.setBackgroundColor(ColorRGBA.LightGray);
//    initKeys();

		// JME2-only so far, it seems
		// JoystickInput.setProvider( InputSystem.INPUT_SYSTEM_LWJGL );

		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.4f, -0.5f, -0.5f).normalizeLocal());
		app.getRootNode().addLight(dl);		
		Camera cam = app.getCamera();

		Quaternion camRotQ = new Quaternion(0.0f, 1.0f, 0.5f, 0.0f);
		cam.setAxes(camRotQ);		
	}	
	public static void initScoreBoard(BonyRenderContext bc) {
		SimpleApplication app = bc.getApp();
		AppSettings settings = app.getContext().getSettings();
		int numScoreRows = 4;
		int rowHeight = 50;
		int boardWidth = settings.getWidth();
		int baseX = 20;
		int baseY = settings.getHeight() - numScoreRows * rowHeight;
		float textSizeMult = 0.5f;
		ScoreBoard sb = new ScoreBoard(app.getAssetManager(), app.getGuiNode(), baseX, baseY, boardWidth, rowHeight, numScoreRows, textSizeMult);
		bc.setScoreBoard(sb);
	}
	public static Canvas makeAWTCanvas(SimpleApplication app) {
		AppSettings settings = app.getContext().getSettings();
		return makeAWTCanvas(app, settings.getWidth(), settings.getHeight());	
	}	
	public static Canvas makeAWTCanvas(SimpleApplication app, int width, int height) {	
		// This must come after 
/* In a silent applet, we might do:
 *         settings.setAudioRenderer(null);
		 // setLowPermissions has many effects
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

}
