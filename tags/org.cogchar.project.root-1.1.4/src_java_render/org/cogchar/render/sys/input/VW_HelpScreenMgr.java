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

	private BitmapText myCurrentHelpBT; // We need to save this now, so it can be turned off automatically for reconfigs
	private boolean myHelpDisplayedFlag = false;

	protected void clearHelpText(WorkaroundAppStub appStub, final RenderRegistryClient rrc) {
		if (myCurrentHelpBT != null) {
			final BitmapText btToRemove = myCurrentHelpBT;
			myCurrentHelpBT = null;
			appStub.enqueue(new Callable<Void>() { // Do this on main render thread

				@Override public Void call() throws Exception {
					rrc.getSceneFlatFacade(null).detachOverlaySpatial(btToRemove);
					return null;
				}
			});
		}
	}

	public void toggleHelpTextDisplay(final RenderRegistryClient rrc) {
		FlatOverlayMgr fom = rrc.getSceneFlatFacade(null);
		if (!myHelpDisplayedFlag) {
			fom.attachOverlaySpatial(myCurrentHelpBT);
			myHelpDisplayedFlag = true;
		} else {
			fom.detachOverlaySpatial(myCurrentHelpBT);
			myHelpDisplayedFlag = false;
		}
	}

	public void updateHelpTextContents(RenderRegistryClient rrc, AppSettings settings, KeyBindingConfig bindingConfig,
				KeyBindingTracker kbt) {
		Map<String, Integer> keyBindingMap = kbt.getBindingMap();
		// First argument sets text size, really shouldn't be hard-coded
		BitmapText helpBT = rrc.getSceneTextFacade(null).makeHelpScreen(0.6f, settings, keyBindingMap);
		myCurrentHelpBT = helpBT;
	}
}
