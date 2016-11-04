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

package org.cogchar.name.cmdbind;

import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface KeystrokeConfigNames {
	public final static String TYPE_VAR_NAME = "type";
	public final static String BINDING_IDENT_VAR_NAME = "name";
	public final static String ACTION_VAR_NAME = "action";
	public final static String KEY_VAR_NAME = "key";
	public final static String COMMAND_ID_NAME = "cmdID";
	public final static String FEATURE_CATEGORY_NAME = "featCat";
	
	public final static String GENERAL_BINDING_NAME = "keybinding";
	public final static String SCENE_BINDING_NAME = "scene_keybinding";
	
	public final static String COMMAND_KEYBINDING_TYPE_NAME = "CommandKeybinding";
	
	public String getBindingsQueryURI();
	
	public Ident getGeneralKeybindingTypeID();
	public Ident getSceneKeybindingTypeID();
	public Ident getCommandKeybindingTypeID();
}
