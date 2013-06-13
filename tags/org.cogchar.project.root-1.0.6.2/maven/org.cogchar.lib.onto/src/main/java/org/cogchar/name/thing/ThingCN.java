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
	public  static String	TA_NS = NamespaceDir.TA_NS ; 
	public  static String	CCRT_NS = NamespaceDir.CCRT_NS;
	public  static String	ACTION_QUERY_URI = AssumedQueryDir.ACTION_QUERY_URI; //  "ccrt:find_thing_actions_99";
	public  static String	UNSEEN_ACTION_QUERY_URI = AssumedQueryDir.UNSEEN_ACTION_QUERY_URI; 
	public  static String	PARAM_QUERY_URI = AssumedQueryDir.PARAM_QUERY_URI; // "ccrt:find_thing_action_params_99";

	public  static String	V_attachedActionID = "attachedToAction";
	
	// Stu renamed variables to make them obviously *different* from the RDF property names.
	// In general, this is the pattern we want to follow.   In software, unlike poetry, ambiguity is the enemy!
	
	public  static String	V_actionID = "thingActionID";
	
	public  static String	V_verbID = "verbID";
	public  static String	V_targetThingID = "tgtThingID";
	
	public  static String	V_targetThingTypeID = "tgtThingTypeID";
	
	 
	 
    /** This explicit PARAM representation is used in our "weak" form of ThingAct-Param rep. 
	 * In this form, each param is explicitly reified as a resource.
	 * The "strong" form simply uses all *qualified* RDF-properties of the ThingAct as params, 
	 * where qual happens through RDF-sub-property relationship to an explicit/inferred marker.
	 * 
	 * Both forms are implemented in FancyThingModelWriter.scala.
	 */

	public  static String	V_actParamID = "actParamID";
	public  static String	V_actParamVal = "actParamVal";
	
	public  static String	V_postedTStampMsec = "postTStampMsec";
	public  static String	V_cutoffTStampMsec = "cutoffTStampMsec";
	public  static String	V_viewingAgentID = "viewingAgentID";

	public	static String	P_sourceAgent = TA_NS + "srcAgent";
	public	static String	P_viewedBy = TA_NS + "viewedBy";

	public	static String	P_verb = TA_NS + "verb";
	public	static String	P_targetThing = TA_NS + "targetThing";
	public	static String	P_postedTSMsec = TA_NS + "postTStampMsec";
	public	static String	P_paramIdent = TA_NS + "paramIdent";
	public	static String	P_paramValue = TA_NS + "paramValue";	
	public	static String	P_targetAction = TA_NS + "targetAction";

	
	public   static String   T_ThingAction = CCRT_NS + "ThingAction";
	public   static String   T_ThingActionParam = CCRT_NS + "ThingActionParam";
	
	
	
}
