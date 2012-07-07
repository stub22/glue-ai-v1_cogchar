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

import org.appdapter.api.trigger.BoxAssemblyNames;

/**
 * @author Ryan Biggs
 */
public class LiftConfigNames extends BoxAssemblyNames {

	public static final String NS_CgcLC = "http://www.cogchar.org/lift/config#";
	public static final String P_template = NS_CgcLC + "template";
	public static final String partial_P_control = "control";
	public static final String P_control = NS_CgcLC + partial_P_control;
	public static final String P_controlType = NS_CgcLC + "type";
	public static final String P_controlAction = NS_CgcLC + "action";
	public static final String P_controlText = NS_CgcLC + "text";
	public static final String P_controlStyle = NS_CgcLC + "style";
	public static final String P_controlResource = NS_CgcLC + "resource";
	
	// These are not used in RDF parsing, but help LiftAmbassador know how to interpret prefixes found in RDF defined actions
	public static final String partial_P_triggerScene = "sceneTrig";
	public static final String partial_P_cinematic = "cinematic";
	public static final String partial_P_liftConfig = "liftconfig";
}
