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
package org.cogchar.name.lifter;

import org.appdapter.api.trigger.BoxAssemblyNames;
import org.cogchar.name.dir.NamespaceDir;

/**
 * @author Ryan Biggs
 */
public class LiftAN extends BoxAssemblyNames {

	public static final String NS_CgcLC =  NamespaceDir.NS_CgcLC;
	public static final String P_template = NS_CgcLC + "template";
	public static final String partial_P_control = "control";
	public static final String P_control = NS_CgcLC + partial_P_control;
	public static final String P_controlType = NS_CgcLC + "type";
	public static final String P_controlAction = NS_CgcLC + "action";
	public static final String P_controlText = NS_CgcLC + "text";
	public static final String P_controlStyle = NS_CgcLC + "style";
	public static final String P_controlResource = NS_CgcLC + "resource";
	public static final String NS_user = NamespaceDir.NS_LifterUser; // "http://www.cogchar.org  /lift/user#";
	public static final String NS_uai = NamespaceDir.NS_LifterUserAccessInstance;
	public static final String NS_LifterConfig = NamespaceDir.NS_LifterConfig;
	public static final String NS_LifterInstance = NamespaceDir.NS_LifterInstance;
	
	
	// These are not used in RDF parsing, but help LiftAmbassador know how to interpret prefixes found in RDF defined actions - 
	// really these should probably be replaced with real URIs
	public static final String partial_P_triggerScene = "sceneTrig";
	public static final String partial_P_cinematic = "cinematic";
	public static final String partial_P_liftConfig = "liftconfig";
	public static final String partial_P_databalls = "databalls";
	public static final String partial_P_update = "reload";
	public static final String refreshLift = "refreshliftcache";
	
	// These prefixes are/were three-quarters identical with ones in ActionStrings.

	public static final String NS_scenetrig =  NamespaceDir.NS_SceneTrig; // "http://www.cogchar.org  /schema/scene/trigger#";
	// This is different from ActionStrings - which used schema/path/definition.
	public static final String NS_cinematic = NamespaceDir.NS_CineDef; // "http://www.cogchar.org  /schema/cinematic/definition#";
	public static final String NS_liftconfig = NamespaceDir.NS_LifterConfig; //   "http://www.cogchar.org  /lift/config/configroot#";
	public static final String NS_liftcmd = NamespaceDir.NS_LifterCmd; //  "http://www.cogchar.org  /lift/config/command#";
	
}
