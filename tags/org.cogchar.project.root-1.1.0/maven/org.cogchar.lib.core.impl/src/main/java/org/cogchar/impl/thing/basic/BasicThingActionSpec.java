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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.SerIdent;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.thing.SerTypedValueMap;

/**  Equivalent to the action of a certain subset of SPARQL-Update.
 * @author Stu B. <www.texpedient.com>
 */

public class BasicThingActionSpec extends KnownComponentImpl implements ThingActionSpec, Serializable {
	// Including the actionRecordID formally reifies the action for posterity.  
	private		SerIdent					myActionRecordID;
	// The "subject" of the update, and the subject of the prop-Vals
	private		SerIdent					myTargetThingID;
	// A type classifier for the target thing.  May be omitted, unless we need to create the targetThing.
	private		SerIdent					myTargetThingTypeID;
	
	// Verb is applied to the target, and can be simple C.(R.)U.D.  Or...can be more subtle.
	// Combined with targetThingType (and potentially other fields), determines the full (implied)
	// "type" of this action.  Working with that imlied type through RDF-rules + scala-cases, 
	// operating on the thing itself using the param data below, is our bread and butter.
	private		SerIdent					myActionVerbID;	
	
	private		SerIdent					mySourceAgentID;
	
	private		SerTypedValueMap			myParamTVMap;
	
	private		Long					myPostedTimestamp;
	
	/**
	 * 
	 * @param actionRecID - can often be null, sometimes set later
	 * @param targetThingID - Non-null unless we are doing a "Create with new URI" thing.
	 * @param verbID - occasionally null
	 */
	public BasicThingActionSpec(Ident actionRecID, Ident targetThingID, Ident targetThingTypeID,  Ident verbID, 
				Ident sourceAgentID, SerTypedValueMap paramTVMap, Long postedTimestamp) {
		myActionRecordID = ensureSerIdent(actionRecID);
		myTargetThingID = ensureSerIdent(targetThingID);
		myTargetThingTypeID = ensureSerIdent(targetThingTypeID);
		myActionVerbID = ensureSerIdent(verbID);
		mySourceAgentID = ensureSerIdent(sourceAgentID);
		
		myParamTVMap = paramTVMap;
		myPostedTimestamp = postedTimestamp;
	}
	private SerIdent ensureSerIdent(Ident in) {
		if (in == null) { 
			return null;
		} else if (in instanceof SerIdent) {
			return (SerIdent) in;
		} else {
			return new FreeIdent(in);
		}
	}
	/*
	private BasicThingActionSpec(ThingActionSpec template) { 
		// myActionRecordID = template.getActionSpecID();
		// mySourceAgentID = 
		myTargetThingID = template.getTargetThingID();
		myActionVerbID = template.getVerbID();
		
		myParamTVMap = null;  // copy of (paramTVMap)
	}
	*/
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

    public void setMyActionRecordID(Ident actionRecordID) {
        this.myActionRecordID = ensureSerIdent(actionRecordID);
    }

    public void setMyTargetThingID(Ident targetThingID) {
        this.myTargetThingID = ensureSerIdent(targetThingID);
    }

    public void setMyTargetThingTypeID(Ident targetThingTypeID) {
        this.myTargetThingTypeID = ensureSerIdent(targetThingTypeID);
    }

    public void setMyActionVerbID(Ident actionVerbID) {
        this.myActionVerbID = ensureSerIdent(actionVerbID);
    }

    public void setMySourceAgentID(Ident sourceAgentID) {
        this.mySourceAgentID = ensureSerIdent(sourceAgentID);
    }

    public void setMyParamTVMap(SerTypedValueMap paramTVMap) {
        this.myParamTVMap = paramTVMap;
    }

    public void setMyPostedTimestamp(Long myPostedTimestamp) {
        this.myPostedTimestamp = myPostedTimestamp;
    }
}
