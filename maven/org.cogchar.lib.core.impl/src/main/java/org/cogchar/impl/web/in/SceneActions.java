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
package org.cogchar.impl.web.in;

import org.cogchar.platform.gui.keybind.KeyBindingConfigItem;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import java.util.HashMap;
import java.util.Map;

import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CogcharEventActionBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.api.web.WebSceneInterface;


/**
 *
 */
public class SceneActions {

	static Logger theLogger = LoggerFactory.getLogger(SceneActions.class);

	public static Map<String, CogcharActionBinding> theBoundActionsByTrigName = new HashMap<String, CogcharActionBinding>();
	
	public static int numberOfBindings = 0;
	
	public static interface SceneTriggerManager {
		public void addKeyMapping(String sceneName, int keyCode);
	}

	public static void setTriggerBinding(String sceneTrigName, CogcharActionBinding ba) {
		// Problem is that this overwrites any existing binding for that sceneTrig,
		// so if we have more than one Theater posting bindings, we wind up with a race
		// condition regarding which one is active.
		theBoundActionsByTrigName.put(sceneTrigName, ba);
	}

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

	static class SceneLauncher implements WebSceneInterface {
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
