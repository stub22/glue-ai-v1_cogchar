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

import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.system.AppSettings;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class VW_HelpScreenMgr extends BasicDebugger {
	
	// Does this best live here or further up in one of the context superclasses? Dunno, but should be easy enough to move it up later (w/o private); be sure to remove imports
	// In order to access registry, must live in a class that extends CogcharRenderContext
	private BitmapText myCurrentHelpBT; // We need to save this now, so it can be turned off automatically for reconfigs

	protected void clearHelpText(WorkaroundAppStub appStub, final RenderRegistryClient rrc) { 
		if (myCurrentHelpBT != null) {
			final BitmapText btToRemove = myCurrentHelpBT;
			myCurrentHelpBT = null;
			appStub.enqueue(new Callable<Void>() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					rrc.getSceneFlatFacade(null).detachOverlaySpatial(btToRemove);
					return null;
				}
			});
		}
	}
	protected void initHelpScreen(AppSettings settings, InputManager inputManager, KeyBindingConfig bindingConfig,
					final RenderRegistryClient rrc, KeyBindingTracker kbt) {
		final String HELP_TAG = "Help"; // Perhaps should be defined elsewhere?
		final int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from bindingConfig
		int helpKey = NULL_KEY;
		String keyString = null;
		if (bindingConfig.myGeneralBindings.containsKey(HELP_TAG)) {
			keyString = bindingConfig.myGeneralBindings.get(HELP_TAG).myBoundKeyName;
		} else {
			logWarning("Attemping to retrieve key binding for help screen, but none is found");
		}
		try {
			if ((keyString.startsWith("AXIS")) || (keyString.startsWith("BUTTON"))) { // In this case, must be MouseInput
				logWarning("Mouse triggers not supported help screen");
			} else { // ... regular KeyInput
				Field keyField = KeyInput.class.getField("KEY_" + keyString.toUpperCase());
				helpKey = keyField.getInt(keyField);
			}
		} catch (Exception e) {
			logWarning("Error getting binding for help screen: " + e);
		}
		if (helpKey != NULL_KEY) {
			kbt.addBinding(HELP_TAG, helpKey); // Let's add ourselves to the help list!
			
			Map<String, Integer> keyBindingMap = kbt.getBindingMap();
			final BitmapText helpBT = rrc.getSceneTextFacade(null).makeHelpScreen(0.6f, settings, keyBindingMap); // First argument sets text size, really shouldn't be hard-coded
			myCurrentHelpBT = helpBT;
			KeyTrigger keyTrig = new KeyTrigger(helpKey);
			inputManager.addMapping(HELP_TAG, keyTrig);
			inputManager.addListener(new ActionListener() {

				private boolean helpDisplayed = false;

				public void onAction(String name, boolean isPressed, float tpf) {
					if (isPressed) {
						FlatOverlayMgr fom = rrc.getSceneFlatFacade(null);
						if (!helpDisplayed) {
							fom.attachOverlaySpatial(helpBT);
							helpDisplayed = true;
						} else {
							fom.detachOverlaySpatial(helpBT);
							helpDisplayed = false;
						}
					}
				}
			}, HELP_TAG);
		}
	}
}
