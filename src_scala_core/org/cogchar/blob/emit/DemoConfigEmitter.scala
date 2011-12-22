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

package org.cogchar.blob.emit

/**
 * @author Stu B. <www.texpedient.com>
 */

class DemoConfigEmitter {
	// Set to OpenGL - 1 mode for 915GM graphics controller
	val DEFAULT_RENDERER_NAME = "LWJGL-OpenGL-Any";
	val DEFAULT_CANVAS_WIDTH = 800;
	val DEFAULT_CANVAS_HEIGHT = 600;
	val PATH_DEFAULT_FONT = "Interface/Fonts/Default.fnt";	
	val PATH_UNSHADED_MAT =  "Common/MatDefs/Misc/Unshaded.j3md";
	
	def getLWJGL_RendererName : String = DEFAULT_RENDERER_NAME;
	
	def getCanvasWidth : Int = DEFAULT_CANVAS_WIDTH;
	def getCanvasHeight : Int = DEFAULT_CANVAS_HEIGHT;
	def getFontPath : String = PATH_DEFAULT_FONT;	
	def getMaterialPath : String = PATH_UNSHADED_MAT;
	
	def getAppSettingsDefloadFlag : Boolean = true;
	
	
	/*
	 *         "LWJGL-OpenGL-Any";
	 *         = "LWJGL-OpenGL3";
	 *         
	 *          "LWJGL-OpenGL2";
	 *          
	 *          "LWJGL-OPENGL1";
	 *          
	 *  defaults.put("Width", 640);
        defaults.put("Height", 480);
        defaults.put("BitsPerPixel", 24);
        defaults.put("Frequency", 60);
        defaults.put("DepthBits", 24);
        defaults.put("StencilBits", 0);
        defaults.put("Samples", 0);
        defaults.put("Fullscreen", false);
        defaults.put("Title", "jMonkey Engine 3.0");
        defaults.put("Renderer", LWJGL_OPENGL2);
        defaults.put("AudioRenderer", LWJGL_OPENAL);
        defaults.put("DisableJoysticks", true);
        defaults.put("UseInput", true);
        defaults.put("VSync", false);
        defaults.put("FrameRate", -1);
        defaults.put("SettingsDialogImage", "/com/jme3/app/Monkey.png");

		Application.initAssetManager does:
	             String assetCfg = settings.getString("AssetConfigURL");
	 */
}
