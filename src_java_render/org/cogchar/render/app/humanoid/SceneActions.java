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
package org.cogchar.render.app.humanoid;

import java.util.HashMap;
import java.util.Map;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyTrigger;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.platform.trigger.DummyBinder;

import org.cogchar.render.app.core.BoundAction;

import org.cogchar.bind.lift.LiftAmbassador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jme3.input.KeyInput.*;

/**
 *
 */
public class SceneActions {

	static Logger theLogger = LoggerFactory.getLogger(SceneActions.class);
	private static int theSceneTrigKeyNums[] = {KEY_NUMPAD0, KEY_NUMPAD1, KEY_NUMPAD2, KEY_NUMPAD3, KEY_NUMPAD4,
		KEY_NUMPAD5, KEY_NUMPAD6, KEY_NUMPAD7, KEY_NUMPAD8, KEY_NUMPAD9,
		KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9
	};
	private static Map<String, DummyBinding> theBoundActionsByTrigName = new HashMap<String, DummyBinding>();
	private static DummyBinding theBoundActions[] = new DummyBinding[theSceneTrigKeyNums.length];

	public static String getSceneTrigName(int keyIndex) {
		// Two digit suffix, zero padded
		return String.format("sceneTrig_%02d", keyIndex);
	}

	public static void setupActionListeners(InputManager inputManager) {
		String actionNames[] = new String[theSceneTrigKeyNums.length];
		for (int idx = 0; idx < theSceneTrigKeyNums.length; idx++) {
			int sceneTrigKeyNum = theSceneTrigKeyNums[idx];
			String sceneTrigName = getSceneTrigName(idx);
			KeyTrigger keyTrig = new KeyTrigger(sceneTrigKeyNum);
			inputManager.addMapping(sceneTrigName, keyTrig);
			KeyBindingTracker.addBinding(sceneTrigName, sceneTrigKeyNum);
			actionNames[idx] = sceneTrigName;
		}
		inputManager.addListener(new ActionListener() {

			public void onAction(String name, boolean isPressed, float tpf) {
				if (isPressed) {
					DummyBinding binding = theBoundActionsByTrigName.get(name);
					if (binding != null) {
						binding.perform();
					} else {
						theLogger.info("Received trigger-press [" + name + "], but binding=" + binding);
					}
				}
			}
		}, actionNames);
	}

	public static void setTriggerBinding(String sceneTrigName, DummyBinding ba) {
		theBoundActionsByTrigName.put(sceneTrigName, ba);
	}

	public static void setTriggerBinding(int sceneTrigIdx, DummyBinding ba) {
		String sceneTrigName = getSceneTrigName(sceneTrigIdx);
	}

	public static void setTriggerBinding(int sceneTrigIdx, DummyBox box, DummyTrigger trigger) {
		BoundAction ba = new BoundAction();
		ba.setTargetBox(box);
		ba.setTargetTrigger(trigger);
		setTriggerBinding(sceneTrigIdx, ba);
	}

	public static DummyBinding getTriggerBinding(String sceneTrigName) {
		return theBoundActionsByTrigName.get(sceneTrigName);
	}
	static Binder theBinder;

	public static Binder getBinder() {
		if (theBinder == null) {
			theBinder = new Binder();
		}
		return theBinder;
	}

	static class Binder implements DummyBinder {

		@Override
		public void setBinding(String boundEventName, DummyBinding binding) {
			setTriggerBinding(boundEventName, binding);
		}

		@Override
		public DummyBinding getBindingFor(String boundEventName) {
			return getTriggerBinding(boundEventName);
		}
	}

	public static int getSceneTrigKeyCount() {
		return theSceneTrigKeyNums.length;
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
		@Override
		public boolean triggerScene(String scene) {
			boolean success = false;
			DummyBinding triggerBinding = getTriggerBinding(scene);
			if (triggerBinding != null) {
				triggerBinding.perform();
				success = true;
			}
			return success;
		}
	}
}
