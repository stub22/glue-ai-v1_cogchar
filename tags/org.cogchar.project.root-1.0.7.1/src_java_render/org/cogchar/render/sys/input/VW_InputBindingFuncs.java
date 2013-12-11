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

package org.cogchar.render.sys.input;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.gui.keybind.KeyBindingConfigItem;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;

// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.trigger.SceneActions;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.system.AppSettings;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.sys.registry.RenderRegistryClient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class VW_InputBindingFuncs {
static Logger theLogger = LoggerFactory.getLogger(VW_InputBindingFuncs.class);
	private static Logger getLogger() { 
		return theLogger;
	}
	final public static int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from config

	public static int getKeyConstantForName(String keyName) {
		int keyConstant = NULL_KEY; 
		try {
			if ((keyName.startsWith("AXIS")) || (keyName.startsWith("BUTTON"))) { // In this case, must be MouseInput
				Field keyField = MouseInput.class.getField(keyName);
				// Inverting this result to stuck with old trick (for now) of having mouse triggers be negative so
				// makeJME3InputTriggers can ignore mouse inputs for the purpose of the keyboard mapping help screen
				// setup. Value is re-inverted there for proper handling.
				keyConstant = -1 * keyField.getInt(keyField);
			
				// getLogger().warn("Mouse triggers not supported for scene actions -- {} not mapped.", mapping.myTargetActionName);
			} else { // ... regular KeyInput - must use reflection to get fron key names to jME KeyInput field values
				Field keyField = KeyInput.class.getField("KEY_" + keyName.toUpperCase());
				keyConstant = keyField.getInt(keyField);
			}
		} catch (Exception e) {
			getLogger().warn("Error getting key-constant for {}: {}", keyName, e);
		}
		return keyConstant;
	}
	
  /* 
    * Below static method converts from jME KeyInput/MouseInput codes to Triggers
    * and registers with KeyBindingTracker.
    * We switch on sign of keyCode to determine if this is a mouse binding or not
    * Negative codes are assumed to be mouse codes and inverted before trigger creation
    * This is possible because all JME KeyInput codes are > 0
    * Not very nice in long run, but gets it going with minimum of modification for now
    */
    public static Trigger[] makeJME3InputTriggers(int triggerKeyCode, String name, KeyBindingTracker kbTracker) { 
		Trigger[] newTrigger = null;
		if (triggerKeyCode != NULL_KEY) {
			if (triggerKeyCode > 0) {
				kbTracker.addBinding(name, triggerKeyCode);
				newTrigger = new Trigger[] { new KeyTrigger(triggerKeyCode)};
			}
			else {newTrigger = new Trigger[] { new MouseButtonTrigger(-1 * triggerKeyCode)};}
		}
		return newTrigger;
	}
	public static void registerActionListeners(final Map<String, CogcharActionBinding> actionBindingMap, 
					String actionNames[], Map<String, Integer> keyBindingMap, InputManager inputManager,
					KeyBindingTracker kbTracker) {

		// Now put sorted sequence of bindings in KeyBindingTracker
		kbTracker.addBindings(keyBindingMap);
		/*
		Iterator<Map.Entry<String, Integer>> bindingIterator = keyBindingMap.entrySet().iterator();
		while (bindingIterator.hasNext()) {
			Map.Entry<String, Integer> entry = bindingIterator.next();
			kbTracker.addBinding(entry.getKey(), entry.getValue());
		}
		* 
		*/ 
		inputManager.addListener(new ActionListener() {

			public void onAction(String name, boolean isPressed, float tpf) {
				if (isPressed) {
					CogcharActionBinding binding = actionBindingMap.get(name);
					if (binding != null) {
						theLogger.info("Performing bound action: {}", binding);
						binding.perform();
					} else {
						theLogger.info("Received trigger-press [{}], but binding = {}", name, binding);
					}
				}
			}
		}, actionNames);
	}
	
	static private VW_InputDirector theOpenGLInputDirector;
	
	/* The signature of this method is the evidence of needed  things that are not accessible from RenderRegistryClient
	 * 
	 */
	public static void setupKeyBindingsAndHelpScreen(final RenderRegistryClient rrc, KeyBindingConfig keyBindConfig, 
					WorkaroundAppStub appStub, AppSettings someSettings, CommandSpace cspace) {
		if (theOpenGLInputDirector == null) {
			theOpenGLInputDirector = new VW_InputDirector();
		}
		
		theOpenGLInputDirector.myRenderRegCli = rrc;
		theOpenGLInputDirector.myKeyBindCfg = keyBindConfig;
		theOpenGLInputDirector.myAppStub = appStub;
	//	theOpenGLInputDirector.myHRC_elim = hrc;
		theOpenGLInputDirector.myAppSettings = someSettings;
		
		theOpenGLInputDirector.myCommandSpace = cspace;
		
		theOpenGLInputDirector.clearKeyBindingsAndHelpScreen();
		
		theOpenGLInputDirector.setupKeyBindingsAndHelpScreen();
	}
	public static VW_HelpScreenMgr getHelpScreenMgr() { 
		return theOpenGLInputDirector.getHelpScreenMgr();
	}
	
}
