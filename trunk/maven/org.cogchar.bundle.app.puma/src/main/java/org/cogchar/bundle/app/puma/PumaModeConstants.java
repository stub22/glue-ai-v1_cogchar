/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bundle.app.puma;

import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.item.Ident;

/**
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */

// As hard as we push to move data to external resources, it seems some sort of meta-meta-meta data keeps
// squeezing out and has to be hard coded. 
// This class contains the information for reading what we now are calling "Global Modes." This global configuration
// is applied at the top level in Cog Char, so the working theory at the moment is that this information belongs here,
// at the PUMA level. Maybe.
public class PumaModeConstants {
	// The (temporarily fixed) "GlobalMode". This won't be a constant for long.
	public static String globalMode = "test_spread_reload";
	
	public static final String rkrt = "urn:ftd:robokind.org:2012:runtime#";
	
	public static final String CHAR_ENTITY_TYPE = "CharEntity";
	public static final String VIRTUAL_WORLD_ENTITY_TYPE = "VirtualWorldEntity";
	
	public static final Ident BONY_CONFIG_ROLE = new FreeIdent(rkrt + "bonyAvatarConf", "bonyAvatarConf");
	public static final Ident LIGHTS_CAMERA_CONFIG_ROLE = new FreeIdent(rkrt + "camLightsConf", "camLightsConf");
	public static final Ident CINEMATIC_CONFIG_ROLE = new FreeIdent(rkrt + "cinematicsConf", "cinematicsConf");	
	public static final Ident HUMANOID_CONFIG_ROLE = new FreeIdent(rkrt + "humanoidConf", "humanoidConf");
	public static final Ident INPUT_BINDINGS_ROLE = new FreeIdent(rkrt + "inputBindings", "inputBindings");
}
