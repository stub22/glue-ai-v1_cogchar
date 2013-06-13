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

package org.cogchar.name.behavior;

import org.cogchar.name.dir.NamespaceDir;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */


public class SceneFieldNames {
	public static String		NS_ccScn =	NamespaceDir.NS_ccScn;
	public static String		NS_ccScnInst = NamespaceDir.NS_ccScnInst;

	public static String		P_behavior	= NS_ccScn + "behavior";
	public static String		P_channel	= NS_ccScn + "channel";	
	public static String		P_trigger	= NS_ccScn + "trigger";
	
	public static String		P_steps				= NS_ccScn + "steps";	// Plural indicates RDF-collection
	public static String		P_startOffsetSec	= NS_ccScn + "startOffsetSec";
	public static String		P_text				= NS_ccScn + "text";
	public static String		P_path				= NS_ccScn + "path";
	
	public static String		P_rules				= NS_ccScn + "rules";
	public static String		P_query				= NS_ccScn + "query";	
	
	public static String		N_rooty		=		"rooty";
	public static String		I_rooty		=		NS_ccScnInst + N_rooty;	
	
	public static String		P_initialStep		= NS_ccScn + "initialStep";
	public static String		P_step				= NS_ccScn + "step";
	public static String		P_finalStep			= NS_ccScn + "finalStep";
	public static String		P_waitForStart		= NS_ccScn + "waitForStart";
	public static String		P_waitForEnd		= NS_ccScn + "waitForEnd";
	public static String		P_waitForChan		= NS_ccScn + "waitForChan";
	public static String		P_chanFilter		= NS_ccScn + "chanFilter";
	
}
