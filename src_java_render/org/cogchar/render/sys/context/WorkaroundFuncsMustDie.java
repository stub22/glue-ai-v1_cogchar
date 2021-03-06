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

package org.cogchar.render.sys.context;

import com.jme3.app.SimpleApplication;
import com.jme3.input.FlyByCamera;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext;
import java.awt.Canvas;

import org.cogchar.render.app.core.WorkaroundAppStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WorkaroundFuncsMustDie {
	
	static Logger theLogger = LoggerFactory.getLogger(WorkaroundFuncsMustDie.class);
	
	public static void setupRegularCameraLightAndViewport(WorkaroundAppStub was) {
		FlyByCamera fbc = was.getFlyByCamera();
        fbc.setDragToRotate(true);
		//fbc.setMoveSpeed(10f); //This is set in HumanoidRenderContext.initCameraAndLights()
		was.setPauseOnLostFocus(false);
		//ViewPort vp = was.getPrimaryAppViewPort(); // Background color now set in HumanoidRenderWorldMapper.setBackgroundColor
		//vp.setBackgroundColor(ColorRGBA.LightGray);
//    initKeys();

		// JME2-only so far, it seems
		// JoystickInput.setProvider( InputSystem.INPUT_SYSTEM_LWJGL );

	}	

	public static Canvas makeAWTCanvas(SimpleApplication app) {
		AppSettings settings = app.getContext().getSettings();
		theLogger.info("making AWTCanvas in WorkaroundFuncsMustDie: Size is {}x{}", settings.getWidth(), settings.getHeight());
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
	public static Vector3f makeVector(float xyz[]) {
		if (xyz.length != 3) {
			throw new RuntimeException("Cannot make 3F vector from vector of length " + xyz.length);
		}
		return new Vector3f(xyz[0], xyz[1], xyz[2]);
	}	
}
