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

import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.item.Ident;

/**
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */


public class KeyBindingQueryNames {
	
	static final String ccrt = "urn:ftd:cogchar.org:2012:runtime#";
	
	static final String BINDINGS_QUERY_URI = "ccrt:find_keybindings_99";
	
	static final String TYPE_VAR_NAME = "type";
	static final String BINDING_IDENT_VAR_NAME = "name";
	static final String ACTION_VAR_NAME = "action";
	static final String KEY_VAR_NAME = "key";
	
	static final String GENERAL_BINDING_NAME = "keybinding";
	static final String SCENE_BINDING_NAME = "scene_keybinding";
	
	static final Ident GENERAL_BINDING_TYPE = new FreeIdent(ccrt + GENERAL_BINDING_NAME, GENERAL_BINDING_NAME);
	static final Ident SCENE_BINDING_TYPE = new FreeIdent(ccrt + SCENE_BINDING_NAME, SCENE_BINDING_NAME);
	
}
