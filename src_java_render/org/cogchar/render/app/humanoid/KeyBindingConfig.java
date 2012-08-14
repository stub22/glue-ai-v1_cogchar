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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.QuerySheet;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.SolutionList;

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
	private static QueryInterface qi = QuerySheet.getInterface();

	public KeyBindingConfig() {
		// Just a default constructor, if we want to just use the addBindings method
	}

	public KeyBindingConfig(Ident qGraph) {
		addBindings(qGraph);
	}

	public void addBindings(Ident qGraph) {
		SolutionList solutionList = qi.getQueryResultList(KeyBindingQueryNames.BINDINGS_QUERY_URI, qGraph);
		for (Solution solution : solutionList.javaList()) {
			KeyBindingConfigItem newItem = new KeyBindingConfigItem(solution);
			if (KeyBindingQueryNames.GENERAL_BINDING_TYPE.equals(newItem.type)) {
				myGeneralBindings.put(newItem.boundAction, newItem);
			} else if (KeyBindingQueryNames.SCENE_BINDING_TYPE.equals(newItem.type)) {
				mySceneBindings.put(newItem.boundAction, newItem);
			} else {
				logWarning("Found an item in KeyBindings resource with invalid type: " + newItem.type);
			}
		}
	}
}
