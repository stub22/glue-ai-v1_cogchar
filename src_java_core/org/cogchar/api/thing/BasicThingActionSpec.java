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

import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;

/**  Equivalent to the action of a certain subset of SPARQL-Update.
 * @author Stu B. <www.texpedient.com>
 */

public class BasicThingActionSpec implements ThingActionSpec {
	// Including the actionRecordID formally reifies the action for posterity.  
	private		Ident					myActionRecordID;
	// The "subject" of the update, and the subject of the prop-Vals
	private		Ident					myTargetThingID;
	// Verb is applied to the target, and can be simple C.(R.)U.D.  Or...can be more subtle.
	private		Ident					myActionVerbID;	
	
	private		Ident					mySourceAgentID;
	
	private		TypedValueMap			myParamTVMap;
	
	/**
	 * 
	 * @param actionRecID - can often be null, sometimes set later
	 * @param targetThingID - Non-null unless we are doing a "Create with new URI" thing.
	 * @param verbID - occasionally null
	 */
	public BasicThingActionSpec(Ident actionRecID, Ident targetThingID, Ident verbID, Ident sourceAgentID, TypedValueMap paramTVMap) {
		myActionRecordID = actionRecID;
		myTargetThingID = targetThingID;
		myActionVerbID = verbID;
		mySourceAgentID = sourceAgentID;
		
		myParamTVMap = paramTVMap;
	}
	public BasicThingActionSpec(ThingActionSpec template) { 
		// myActionRecordID = template.getActionSpecID();
		// mySourceAgentID = 
		myTargetThingID = template.getTargetThingID();
		myActionVerbID = template.getVerbID();
		
		myParamTVMap = null;  // copy of (paramTVMap)
	}

	@Override public Ident getActionSpecID() {
		return myActionRecordID;
	}

	@Override  public Ident getVerbID() {
		return myActionVerbID;
	}

	@Override  public Ident getTargetThingID() {
		return myTargetThingID;
	}

	@Override  public Ident getTargetThingTypeID() {
		return myTargetThingID;
	}

	
	@Override  public Ident getSourceAgentID() {
		return mySourceAgentID;
	}

	@Override  public TypedValueMap getParamTVM() {
		return myParamTVMap;
	}

}
