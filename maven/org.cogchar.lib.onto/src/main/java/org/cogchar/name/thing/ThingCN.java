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
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */


public class ThingCN {
	public final static String	TA_NS = NamespaceDir.TA_NS ; 
	public final static String	CCRT_NS = NamespaceDir.CCRT_NS;
	public final static String	ACTION_QUERY_URI = AssumedQueryDir.ACTION_QUERY_URI; //  "ccrt:find_thing_actions_99";
	public final static String	UNSEEN_ACTION_QUERY_URI = AssumedQueryDir.UNSEEN_ACTION_QUERY_URI; 
	public final static String	PARAM_QUERY_URI = AssumedQueryDir.PARAM_QUERY_URI; // "ccrt:find_thing_action_params_99";

	public final static String	V_IdentAttachedToThingAction = "attachedToThingAction";
	
	// Stu renamed variables to make them obviously *different* from the RDF property names.
	// In general, this is the pattern we want to follow.   In software, unlike poetry, ambiguity is the enemy!
	
	public final static String	V_actionID = "thingActionID";
	
	public final static String	V_verbID = "verbID";
	public final static String	V_targetThingID = "tgtThingID";
	
	public final static String	V_targetThingTypeID = "tgtThingTypeID";
	
	 
	 
    /** This explicit PARAM representation is used in our "weak" form of ThingAct-Param rep. 
	 * In this form, each param is explicitly reified as a resource.
	 * The "strong" form simply uses all *qualified* RDF-properties of the ThingAct as params, 
	 * where qual happens through RDF-sub-property relationship to an explicit/inferred marker.
	 * 
	 * Both forms are implemented in FancyThingModelWriter.scala.
     * 
     * The BasicThingActionSpecBuilder also uses these Idents.
	 */

	public final static String	V_actParamID = "actParamID";
	public final static String	V_actParamVal = "actParamVal";
	
	public final static String	V_postedTStampMsec = "postTStampMsec";
	public final static String	V_cutoffTStampMsec = "cutoffTStampMsec";
	public final static String	V_viewingAgentID = "viewingAgentID";

	public final static String	P_sourceAgent = TA_NS + "srcAgent";
	public final static String	P_viewedBy = TA_NS + "viewedBy";

	public final static String	P_verb = TA_NS + "verb";
	public final static String	P_targetThing = TA_NS + "targetThing";
    public final static String  P_targetThingType  = TA_NS + "targetThingType";
	public final static String	P_postedTSMsec = TA_NS + "postTStampMsec";
	public final static String	P_paramIdent = TA_NS + "paramIdent";
    public final static String  P_paramIdentValue  = TA_NS + "paramIdentValue";
    public final static String  P_paramStringValue  = TA_NS + "paramStringValue";
    public final static String  P_paramIntValue  = TA_NS + "paramIntValue";
    public final static String  P_paramFloatValue  = TA_NS + "paramFloatValue";
    
	public final static String  T_ThingAction = CCRT_NS + "ThingAction";
	public final static String  T_ThingActionParam = CCRT_NS + "ThingActionParam";
	
}