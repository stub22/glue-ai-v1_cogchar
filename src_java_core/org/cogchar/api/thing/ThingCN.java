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

package org.cogchar.api.thing;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class ThingCN {
	
	// Hmmmm - this is used to by BasicTypedValueMapImpl to generate URIs from parameters originally in string local
	// name form in repo. But we need this to be the same as the GOODY_NS for GoodyAction to return GoodyNames.TYPE_BIT_BOX
	// for a "BitBox" string. So this is sort of nonsense, until we figure out (a) a more handsome BasicTypedValueMap implementation
	// and/or (b) better ways to structure the repo.
	public  static String	THING_NS = "urn:ftd:cogchar.org:2012:goody#";
	
	public  static String	ACTION_QUERY_URI = "ccrt:find_thing_actions_99";
	public  static String	PARAM_QUERY_URI = "ccrt:find_thing_action_params_99";

	public  static String	ACTION_QUERY_VAR_NAME = "attachedToAction";
	
	// Stu renamed variables to make them obviously *different* from the RDF property names.
	// In general, this is the pattern we want to follow.   In software, unlike poetry, ambiguity is the enemy!
	
	public  static String	ACTION_URI_VAR_NAME = "thingActionID";
	
	public  static String	VERB_VAR_NAME = "verbID";
	public  static String	TARGET_VAR_NAME = "tgtThingID";
	
	public  static String	TARGET_TYPE_VAR_NAME = "tgtThingTypeID";
	public  static String	PARAM_IDENT_VAR_NAME = "actParamID";
	public  static String	PARAM_VALUE_VAR_NAME = "actParamVal";
}
