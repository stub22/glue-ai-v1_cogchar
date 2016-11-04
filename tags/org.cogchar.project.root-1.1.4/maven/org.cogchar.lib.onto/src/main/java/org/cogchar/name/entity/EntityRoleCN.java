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
package org.cogchar.name.entity;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.name.dir.AssumedQueryDir;
import org.cogchar.name.dir.NamespaceDir;

/**
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */


public class EntityRoleCN {
	// The (temporarily fixed) "GlobalMode". This won't be a constant for long.
	public static String DEFAULT_GLOBAL_MODE_NAME = "test_spread_reload";
	
	public static final String RKRT_NS_PREFIX = NamespaceDir.RKRT_NS_PREFIX;
	
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
	
	// Moved these here from GlobalConfigEmitter, which is the only place they were used, at that time.  (They were private there).
	public static final String GC_NS = NamespaceDir.GC_NS;
	public static final String GLOBALMODE_QUERY_QN = AssumedQueryDir.GLOBALMODE_QUERY_QN; // "ccrt:template_globalmode_99" ;
	public static final String ENTITIES_QUERY_QN = AssumedQueryDir.ENTITIES_QUERY_QN; //  "ccrt:template_global_entities_99";
	public static final String GLOBALMODE_QUERY_VAR_NAME = "mode";
	public static final String ENTITY_TYPE_QUERY_VAR_NAME = "type";
	public static final String ENTITY_VAR_NAME = "entity";
	public static final String ROLE_VAR_NAME = "role";
	public static final String GRAPH_VAR_NAME = "graph";
	public static final String ENTITY_TYPES[] = {"CharEntity", "VirtualWorldEntity", "WebappEntity", "SwingAppEntity", "MayaMappingEntity"};
  	
}
