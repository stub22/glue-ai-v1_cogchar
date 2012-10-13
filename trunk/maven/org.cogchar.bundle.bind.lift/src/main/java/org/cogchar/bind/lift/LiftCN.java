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
package org.cogchar.bind.lift;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 * @author Ryan Biggs
 */
public class LiftCN {
	public static final String ccrt = "urn:ftd:cogchar.org:2012:runtime#"; // This itself could come from query if we want
	
	public static final Ident BLANK_ACTION = new FreeIdent("http://www.cogchar.org/lift/config/command#", "");

	public static final String CONFIG_VAR_NAME = "liftConfig";
	public static final String TEMPLATE_VAR_NAME = "template";
	public static final String CONTROL_VAR_NAME = "control";
	public static final String CONTROL_TYPE_VAR_NAME = "type";
	public static final String ACTION_VAR_NAME = "action";
	public static final String TEXT_VAR_NAME = "text";
	public static final String STYLE_VAR_NAME = "style";
	public static final String RESOURCE_VAR_NAME = "resource";
	
	public static final String TEMPLATE_QUERY_URI = "ccrt:find_lift_templates_99";
	public static final String START_CONFIG_QUERY_URI = "ccrt:find_lift_startConfig_99";
	
	public static final String CONTROL_QUERY_TEMPLATE_URI = "ccrt:template_lift_control_99";
	public static final String FREE_CONTROL_QUERY_TEMPLATE_URI = "ccrt:template_find_free_control_99";
	
	public static final String CONFIG_QUERY_VAR_NAME = "config";
	public static final String CONTROL_QUERY_VAR_NAME = "desiredControl";
	
	public static final String LIFT_CONFIG_INSTANCE_PREFIX = "http://www.cogchar.org/lift/config/instance#"; //lci:
}
