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
package org.cogchar.render.app.trigger;

import org.cogchar.platform.gui.keybind.KeyBindingConfigItem;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import java.util.HashMap;
import java.util.Map;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CogcharActionTrigger;
import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CogcharEventActionBinder;

import org.cogchar.bind.lift.LiftAmbassador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 */
public class SceneActions {

	static Logger theLogger = LoggerFactory.getLogger(SceneActions.class);

	private static Map<String, CogcharActionBinding> theBoundActionsByTrigName = new HashMap<String, CogcharActionBinding>();
	//private static DummyBinding theBoundActions[]; // Is this ever really used?
	
	private static int numberOfBindings = 0;
	
/*
	public static String getSceneTrigName(int keyIndex) {
		// Two digit suffix, zero padded
		return String.format("sceneTrig_%02d", keyIndex);
	}
*/
	public static void setupActionListeners(InputManager inputManager, KeyBindingConfig config) {
		numberOfBindings = config.mySceneBindings.size();
		String actionNames[] = new String[numberOfBindings];
		//theBoundActions = new DummyBinding[numberOfBindings];
		Iterator<KeyBindingConfigItem> sceneMappings = config.mySceneBindings.values().iterator();
		// We'll put the bindings in this temporary map so we can deliver a sorted sequence to KeyBindingTracker
		Map<String, Integer> bindingMap = new TreeMap<String, Integer>();
		int idx = 0;
		while (sceneMappings.hasNext()) {
			KeyBindingConfigItem nextMapping = sceneMappings.next();
			int sceneTrigKeyNum = getKey(nextMapping);
			if (sceneTrigKeyNum != NULL_KEY) {
				KeyTrigger keyTrig = new KeyTrigger(sceneTrigKeyNum);
				String sceneTrigName = nextMapping.myTargetActionName;
				inputManager.addMapping(sceneTrigName, keyTrig);
				bindingMap.put(sceneTrigName, sceneTrigKeyNum);
				actionNames[idx] = sceneTrigName;
				idx++;
			}
		}
		// Now put sorted sequence of bindings in KeyBindingTracker
		Iterator<Map.Entry<String, Integer>> bindingIterator = bindingMap.entrySet().iterator();
		while (bindingIterator.hasNext()) {
			Map.Entry<String, Integer> entry = bindingIterator.next();
			KeyBindingTracker.addBinding(entry.getKey(), entry.getValue());
		}
		inputManager.addListener(new ActionListener() {

			public void onAction(String name, boolean isPressed, float tpf) {
				if (isPressed) {
					CogcharActionBinding binding = theBoundActionsByTrigName.get(name);
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

	public static void setTriggerBinding(String sceneTrigName, CogcharActionBinding ba) {
		// Problem is that this overwrites any existing binding for that sceneTrig,
		// so if we have more than one Theater posting bindings, we wind up with a race
		// condition regarding which one is active.
		theBoundActionsByTrigName.put(sceneTrigName, ba);
	}

	/* Hmm is it just me, or does this method not actually do anything?
	public static void setTriggerBinding(int sceneTrigIdx, DummyBinding ba) {
		String sceneTrigName = getSceneTrigName(sceneTrigIdx);
	}
	*/ 

/*
	private static void setTriggerBinding(int sceneTrigIdx, CogcharScreenBox box, CogcharActionTrigger trigger) {
		BoundAction ba = new BoundAction();
		ba.addTargetBox(box);
		ba.setTargetTrigger(trigger);
		//setTriggerBinding(sceneTrigIdx, ba); // Did this ever do anything? (see method above)
	}
	* 
	*/ 

	public static CogcharActionBinding getTriggerBinding(String sceneTrigName) {
		return theBoundActionsByTrigName.get(sceneTrigName);
	}
	static Binder theBinder;

	public static Binder getBinder() {
		if (theBinder == null) {
			theBinder = new Binder();
		}
		return theBinder;
	}
	
	final static int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from config
	private static int getKey(KeyBindingConfigItem mapping) {
		int keyInput = NULL_KEY; 
		String keyString = mapping.myBoundKeyName;
		try {
			if ((keyString.startsWith("AXIS")) || (keyString.startsWith("BUTTON"))) { // In this case, must be MouseInput
				theLogger.warn("Mouse triggers not supported for scene actions -- {} not mapped.", mapping.myTargetActionName);
			} else { // ... regular KeyInput - must use reflection to get fron key names to jME KeyInput field values
				Field keyField = KeyInput.class.getField("KEY_" + keyString.toUpperCase());
				keyInput = keyField.getInt(keyField);
			}
		} catch (Exception e) {
			theLogger.warn("Error getting binding for {}: {}", mapping.myTargetActionName, e);
		}
		return keyInput;
	}

	static class Binder implements CogcharEventActionBinder {

		@Override public void setBindingForEvent(String boundEventName, CogcharActionBinding binding) {
			setTriggerBinding(boundEventName, binding);
		}

		@Override public CogcharActionBinding getBindingForEvent(String boundEventName) {
			return getTriggerBinding(boundEventName);
		}
	}

	public static int getSceneTrigKeyCount() {
		return numberOfBindings;
	}
	
	// The following SceneLauncher stuff allows Lift app to hook in and trigger scenes
	static SceneLauncher theLauncher;

	public static SceneLauncher getLauncher() {
		if (theLauncher == null) {
			theLauncher = new SceneLauncher();
		}
		return theLauncher;
	}

	static class SceneLauncher implements LiftAmbassador.LiftSceneInterface {
		@Override public boolean triggerScene(String scene) {
			boolean success = false;
			CogcharActionBinding triggerBinding = getTriggerBinding(scene);
			if (triggerBinding != null) {
				triggerBinding.perform();
				success = true;
			}
			return success;
		}
	}
}
