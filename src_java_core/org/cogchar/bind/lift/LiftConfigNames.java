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
	public static final String partial_P_control = "control";
	public static String P_control = NS_CgcLC + partial_P_control;
	public static String P_controlType = NS_CgcLC + "type";
	public static String P_controlId = NS_CgcLC + "id";
	public static String P_controlAction = NS_CgcLC + "action";
	public static String P_controlText = NS_CgcLC + "text";
	public static String P_controlStyle = NS_CgcLC + "style";
	public static String P_controlResource = NS_CgcLC + "resource";
}
