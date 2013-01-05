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
package org.cogchar.app.puma.config;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */


public class PumaModeConstants {
	// The (temporarily fixed) "GlobalMode". This won't be a constant for long.
	public static String DEFAULT_GLOBAL_MODE_NAME = "test_spread_reload";
	
	public static final String RKRT_NS_PREFIX = "urn:ftd:robokind.org:2012:runtime#";
	
	public static final String CHAR_ENTITY_TYPE = "CharEntity";
	public static final String VIRTUAL_WORLD_ENTITY_TYPE = "VirtualWorldEntity";
	
	public static Ident makeRoleIdent(String roleShortName) {
		return new FreeIdent(RKRT_NS_PREFIX + roleShortName, roleShortName);
	}
	public static final Ident BONY_CONFIG_ROLE =  makeRoleIdent("bonyAvatarConf");
	public static final Ident LIGHTS_CAMERA_CONFIG_ROLE = makeRoleIdent ("camLightsConf");
	public static final Ident MOTIONPATH_CONFIG_ROLE = makeRoleIdent("pathConf");	
	public static final Ident HUMANOID_CONFIG_ROLE = makeRoleIdent("humanoidConf");
	public static final Ident INPUT_BINDINGS_ROLE = makeRoleIdent("inputBindings");
	public static final Ident THING_ACTIONS_BINDINGS_ROLE = makeRoleIdent("thingActions");
	public static final Ident WAYPOINTS_BINDINGS_ROLE = makeRoleIdent("thingWaypoints");
	public static final Ident THING_ANIM_BINDINGS_ROLE = makeRoleIdent("thingAnims");
}
