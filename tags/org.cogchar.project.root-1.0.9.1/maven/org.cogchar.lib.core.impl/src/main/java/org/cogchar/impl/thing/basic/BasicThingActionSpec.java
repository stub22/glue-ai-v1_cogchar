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

package org.cogchar.impl.thing.basic;

import java.util.HashMap;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;

/**  Equivalent to the action of a certain subset of SPARQL-Update.
 * @author Stu B. <www.texpedient.com>
 */

public class BasicThingActionSpec extends KnownComponentImpl implements ThingActionSpec {
	// Including the actionRecordID formally reifies the action for posterity.  
	private		Ident					myActionRecordID;
	// The "subject" of the update, and the subject of the prop-Vals
	private		Ident					myTargetThingID;
	// A type classifier for the target thing.  May be omitted, unless we need to create the targetThing.
	private		Ident					myTargetThingTypeID;
	
	// Verb is applied to the target, and can be simple C.(R.)U.D.  Or...can be more subtle.
	// Combined with targetThingType (and potentially other fields), determines the full (implied)
	// "type" of this action.  Working with that imlied type through RDF-rules + scala-cases, 
	// operating on the thing itself using the param data below, is our bread and butter.
	private		Ident					myActionVerbID;	
	
	private		Ident					mySourceAgentID;
	
	private		TypedValueMap			myParamTVMap;
	
	private		Long					myPostedTimestamp;
	
	/**
	 * 
	 * @param actionRecID - can often be null, sometimes set later
	 * @param targetThingID - Non-null unless we are doing a "Create with new URI" thing.
	 * @param verbID - occasionally null
	 */
	public BasicThingActionSpec(Ident actionRecID, Ident targetThingID, Ident targetThingTypeID,  Ident verbID, 
				Ident sourceAgentID, TypedValueMap paramTVMap, Long postedTimestamp) {
		myActionRecordID = actionRecID;
		myTargetThingID = targetThingID;
		myTargetThingTypeID = targetThingTypeID;
		myActionVerbID = verbID;
		mySourceAgentID = sourceAgentID;
		
		myParamTVMap = paramTVMap;
		myPostedTimestamp = postedTimestamp;
	}
	private BasicThingActionSpec(ThingActionSpec template) { 
		// myActionRecordID = template.getActionSpecID();
		// mySourceAgentID = 
		myTargetThingID = template.getTargetThingID();
		myActionVerbID = template.getVerbID();
		
		myParamTVMap = null;  // copy of (paramTVMap)
	}
    // Empty constructor for assembler usage
    public BasicThingActionSpec() {}

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
		return myTargetThingTypeID;
	}

	
	@Override  public Ident getSourceAgentID() {
		return mySourceAgentID;
	}

	@Override  public TypedValueMap getParamTVM() {
		return myParamTVMap;
	}
	
	public Long getPostedTimestamp() {
		return myPostedTimestamp;
	}
	
	@Override  public String toString() {
		return "BasicThingActionSpec[actRecID=" + myActionRecordID + ", tgtThgID=" + myTargetThingID +
					", tgtThgTypeID=" + myTargetThingTypeID + ", actVerbID=" + myActionVerbID 
					+ ", srcAgtID=" + mySourceAgentID + ", paramTVMap=" + myParamTVMap + ", "
					+ "postedTStamp=" + myPostedTimestamp + "]";
	}

    public void setMyActionRecordID(Ident myActionRecordID) {
        this.myActionRecordID = myActionRecordID;
    }

    public void setMyTargetThingID(Ident myTargetThingID) {
        this.myTargetThingID = myTargetThingID;
    }

    public void setMyTargetThingTypeID(Ident myTargetThingTypeID) {
        this.myTargetThingTypeID = myTargetThingTypeID;
    }

    public void setMyActionVerbID(Ident myActionVerbID) {
        this.myActionVerbID = myActionVerbID;
    }

    public void setMySourceAgentID(Ident mySourceAgentID) {
        this.mySourceAgentID = mySourceAgentID;
    }

    public void setMyParamTVMap(TypedValueMap myParamTVMap) {
        this.myParamTVMap = myParamTVMap;
    }

    public void setMyPostedTimestamp(Long myPostedTimestamp) {
        this.myPostedTimestamp = myPostedTimestamp;
    }
}
