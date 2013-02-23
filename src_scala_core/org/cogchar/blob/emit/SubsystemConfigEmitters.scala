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

package org.cogchar.blob.emit

import org.appdapter.core.name.{FreeIdent, Ident}


/**
 * @author Stu B. <www.texpedient.com>
 * 
 * In this file we tackle exposure of subsystem-specific configuration data
 * that we will incrementally make more "private" to each subsys.
 */

object SubsystemConfigEmitters {
}
class SubsystemConfigEmitter {
	val COGCHAR_URN_PREFIX = "urn:ftd:cogchar.org:2012:";
	val	COGCHAR_CHAR_URN_PREFIX = COGCHAR_URN_PREFIX + "runtime#";
}
class ConvyConfigEmitter {
}
class RobokindBindingConfigEmitter {
		/*
	def getRobokindRobotID(robotURI : String) = {
		NB_BONY_ROBOT_ID; // or DUMMY_ROBOT_ID, ...
	}
	*/
}
class RenderConfigEmitter(val myOptSysCtxURI : Option[String]) extends SubsystemConfigEmitter {
	// Alternate no-args constructor
	def this() = this(None)
	
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

	// Used by PanelUtils
	def getVCPanelClassName(kind : String) : String = {
		kind match {
			case "FULL" => FANCY_PANEL_CLASSNAME;
			case "SLIM" => SLIM_PANEL_CLASSNAME;
			case _ => null
		}
	}
	val FANCY_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.FancyCharPanel";
	val SLIM_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.VirtCharPanel";	

	// Used (for now) by HumanoidPuppetActions for Sinbad special handling

	val SINBAD_NICKNAME = "char_sinbad_88";
	val	SINBAD_CHAR_URI = COGCHAR_CHAR_URN_PREFIX + SINBAD_NICKNAME;
	val	SINBAD_CHAR_IDENT = new FreeIdent(SINBAD_CHAR_URI, SINBAD_NICKNAME)
	
	// Used by HumanoidPuppetActions.setupActionListeners and BonyGameFeatureAdapter
	def isMinimalSim() : Boolean = {
		myOptSysCtxURI match {
			case Some(s) => s.startsWith("NB")
			case None => false
		}
	}
	
	// Used by BonyGameFeatureAdapter
	def getStickFigureScenePath : String = {
		if (isMinimalSim()) null else WINGED_OBELISK_SCENE;
	}
	val WINGED_OBELISK_SCENE = "leo_hanson_tests/test3/test3.scene";
	
	// Used by BonyGameFeatureAdapter
	def getStickFigureSceneScale : Float = 0.5f;
	
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

import org.cogchar.name.cmdbind.{KeystrokeConfigNames}
import org.cogchar.name.cmdbind.KeystrokeConfigNames.{GENERAL_BINDING_NAME, SCENE_BINDING_NAME, COMMAND_KEYBINDING_TYPE_NAME}

class KeystrokeConfigEmitter extends SubsystemConfigEmitter with KeystrokeConfigNames {
	
	val ccrt = COGCHAR_CHAR_URN_PREFIX;
	
	val BINDINGS_QUERY_URI = "ccrt:find_keybindings_99";
	val  GENERAL_BINDING_TYPE : Ident = new FreeIdent(ccrt + GENERAL_BINDING_NAME, GENERAL_BINDING_NAME);
	val  SCENE_BINDING_TYPE : Ident = new FreeIdent(ccrt + SCENE_BINDING_NAME, SCENE_BINDING_NAME);
	val  COMMAND_KEYBINDING_TYPE : Ident = new FreeIdent(ccrt + COMMAND_KEYBINDING_TYPE_NAME, COMMAND_KEYBINDING_TYPE_NAME);
	
	override def getBindingsQueryURI() : String = BINDINGS_QUERY_URI

	override def getGeneralKeybindingTypeID : Ident = GENERAL_BINDING_TYPE;
	override def getSceneKeybindingTypeID : Ident = SCENE_BINDING_TYPE;

	override def getCommandKeybindingTypeID : Ident = COMMAND_KEYBINDING_TYPE;
}