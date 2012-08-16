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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A very simple class with a static map to keep track of key bindings for help overlay
 *
 * @author Ryan Biggs
 */
public class KeyBindingTracker {

	static private Map<String, Integer> bindingMap = new LinkedHashMap<String, Integer>();

	static void addBinding(String action, Integer key) {
		bindingMap.put(action, key); // Or do we want (key, action)? May depend on future of this class
	}

	public static Map<String, Integer> getBindingMap() {
		return bindingMap;
	}

	public static void clearMap() {
		bindingMap.clear();
	}
}
