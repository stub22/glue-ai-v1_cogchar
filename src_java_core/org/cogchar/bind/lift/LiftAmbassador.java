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
package org.cogchar.bind.lift;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.cogchar.render.app.humanoid.SceneActions; // Can't see it
//import org.cogchar.platform.trigger.DummyBinding;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador {

	static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class);
	private static List<ControlConfig> controls = new ArrayList<ControlConfig>();

	public static void storeControlsFromConfig(LiftConfig config) {
		controls = config.myCCs;
		// Use reflection to call method in bundle.lifter to set upon load?
	}

	public static ArrayList<ControlConfig> getControls() {
		return (ArrayList<ControlConfig>) controls;
	}
	/*
	 * Can't see into cogchar.lib.render public static boolean triggerScene(String scene) { boolean success = false;
	 * DummyBinding triggerBinding = SceneActions.getTriggerBinding(scene); if (triggerBinding != null)
	 * {triggerBinding.perform(); success = true;} return success; } /
	 */

	public static String getPrefix() {
		return LiftConfigNames.partial_P_control + "_";
	}
}
