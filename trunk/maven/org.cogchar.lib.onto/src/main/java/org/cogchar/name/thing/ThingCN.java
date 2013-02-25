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

package org.cogchar.name.thing;

import org.cogchar.name.dir.AssumedQueryDir;
import org.cogchar.name.dir.NamespaceDir;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 * @author stub22
 */


public class ThingCN {
	public  static String	THING_NS = NamespaceDir.TA_NS ; 
	public  static String	ACTION_QUERY_URI = AssumedQueryDir.ACTION_QUERY_URI; //  "ccrt:find_thing_actions_99";
	public  static String	PARAM_QUERY_URI = AssumedQueryDir.PARAM_QUERY_URI; // "ccrt:find_thing_action_params_99";

	public  static String	ACTION_QUERY_VAR_NAME = "attachedToAction";
	
	// Stu renamed variables to make them obviously *different* from the RDF property names.
	// In general, this is the pattern we want to follow.   In software, unlike poetry, ambiguity is the enemy!
	
	public  static String	ACTION_URI_VAR_NAME = "thingActionID";
	
	public  static String	VERB_VAR_NAME = "verbID";
	public  static String	TARGET_VAR_NAME = "tgtThingID";
	
	public  static String	TARGET_TYPE_VAR_NAME = "tgtThingTypeID";
	
	 
	 
    /** This explicit PARAM representation is used in our "weak" form of ThingAct-Param rep. 
	 * In this form, each param is explicitly reified as a resource.
	 * The "strong" form simply uses all *qualified* RDF-properties of the ThingAct as params, 
	 * where qual happens through RDF-sub-property relationship to an explicit/inferred marker.
	 * 
	 * Both forms are implemented in FancyThingModelWriter.scala.
	 */

	public  static String	PARAM_IDENT_VAR_NAME = "actParamID";
	public  static String	PARAM_VALUE_VAR_NAME = "actParamVal";
}
