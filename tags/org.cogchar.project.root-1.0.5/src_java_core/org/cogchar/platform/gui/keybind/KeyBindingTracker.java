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
package org.cogchar.platform.gui.keybind;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A very simple class with a static map to keep track of key bindings for help overlay
 *
 * @author Ryan Biggs
 */
public class KeyBindingTracker {

	static private	KeyBindingTracker theTracker = new KeyBindingTracker();
	
	private Map<String, Integer>  myBindingMap = new LinkedHashMap<String, Integer>();

	public static KeyBindingTracker getTheTracker() { 
		return theTracker;
	}
	public void addBinding(String action, Integer key) {
		myBindingMap.put(action, key); // Or do we want (key, action)? May depend on future of this class
	}

	public Map<String, Integer> getBindingMap() {
		return myBindingMap;
	}

	public void clearMap() {
		myBindingMap.clear();
		
	}
	
	public void addBindings (Map<String, Integer> keyBindingMap) {
		myBindingMap.putAll(keyBindingMap);
	}
}
