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

import org.appdapter.core.item.Ident;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.QuerySheet;
import org.cogchar.blob.emit.Solution;

/**
 * A class to hold the individual jMonkey key bindings. For more info see KeyBindingConfig.java
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class KeyBindingConfigItem {

	public Ident type;
	public String myIdent;
	public String boundAction;
	public String boundKey;
	// Another instance of the "not permanent" way of getting the QueryInterface! Time to decide soon the permanent way...
	private static QueryInterface qi = QuerySheet.getInterface();

	public KeyBindingConfigItem(Solution solution) {
		type = qi.getIdentFromSolution(solution, KeyBindingQueryNames.TYPE_VAR_NAME);
		myIdent = qi.getIdentFromSolution(solution, KeyBindingQueryNames.BINDING_IDENT_VAR_NAME).getLocalName();
		boundAction = qi.getStringFromSolution(solution, KeyBindingQueryNames.ACTION_VAR_NAME);
		boundKey = qi.getStringFromSolution(solution, KeyBindingQueryNames.KEY_VAR_NAME);
	}
}
