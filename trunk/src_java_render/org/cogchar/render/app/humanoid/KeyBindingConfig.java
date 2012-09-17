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
package org.cogchar.render.app.humanoid;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.QueryInterface;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionList;
import org.cogchar.blob.emit.KeystrokeConfigEmitter;

/**
 * This class loads the jMonkey key bindings via query config and makes them available to Cog Char Currently these
 * bindings are applied in HumanoidPuppetActions and SceneActions, both of which are here in o.c.lib.render. So for now,
 * this class will go here as well (even though not all bound actions are strictly Humanoid actions).
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class KeyBindingConfig extends BasicDebugger {

	public Map<String, KeyBindingConfigItem> myGeneralBindings = new HashMap<String, KeyBindingConfigItem>();
	public Map<String, KeyBindingConfigItem> mySceneBindings = new HashMap<String, KeyBindingConfigItem>();
	// Another instance of the "not permanent" way of getting the QueryInterface! Time to decide soon the permanent way...

	public KeyBindingConfig() {
		// Just a default constructor, if we want to just use the addBindings method
	}

	public KeyBindingConfig(QueryInterface qi, Ident qGraph, KeystrokeConfigEmitter kce) {
		addBindings(qi, qGraph, kce);
	}

	public void addBindings(QueryInterface qi, Ident qGraph, KeystrokeConfigEmitter kce) {
		SolutionList solutionList = qi.getQueryResultList(kce.BINDINGS_QUERY_URI(), qGraph);
		List<Solution> solnJL =  solutionList.javaList();
		logInfo("addBindings found " + solnJL.size() + " bindings");
		for (Solution solution : solnJL ) {
			KeyBindingConfigItem newItem = new KeyBindingConfigItem(qi, solution, kce);
			if (kce.GENERAL_BINDING_TYPE().equals(newItem.myTypeIdent)) {
				myGeneralBindings.put(newItem.myBoundEventName, newItem);
			} else if (kce.SCENE_BINDING_TYPE().equals(newItem.myTypeIdent)) {
				mySceneBindings.put(newItem.myBoundEventName, newItem);
			} else {
				logWarning("Found an item in KeyBindings resource with invalid type: " + newItem.myTypeIdent);
			}
		}
	}
}
