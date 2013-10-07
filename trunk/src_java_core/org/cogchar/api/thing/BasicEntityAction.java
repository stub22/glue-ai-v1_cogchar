/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This class is a basis for action implementation, wrapping a ThingActionSpec,
 * which was usually received from some client or GUI.
 * 
 * Prominent Subclasses:   VWorldEntityAction, WebEntityAction
 */

public abstract class BasicEntityAction {
	private	 ThingActionSpec	myTemplateActionSpec;
	
	private Ident				myActualEntityID;
	private Ident				myActualEntityTypeID;
	
	// Stu 2013-10-06 :  I can't justify this separate copy of (pointer to) params at this time, so removing it until 
	// need is clear again.
	// protected TypedValueMap		myParamTVMap;
	
	public BasicEntityAction(ThingActionSpec actionSpec) {
		myTemplateActionSpec = actionSpec;
		myActualEntityID = actionSpec.getTargetThingID();
		myActualEntityTypeID = actionSpec.getTargetThingTypeID();
		
	//	myParamTVMap = myTemplateActionSpec.getParamTVM();
	}

	protected void setEntityAndType(Ident actualEntityID, Ident actualEntityTypeID) { 
		myActualEntityID = actualEntityID;
		myActualEntityTypeID = actualEntityTypeID;
	}

	/**
	 * If our ThingActionSpec supplied a targetThingID, we use that by default.
	 * However, during creation or other "special" operation, our GoodyAction action may
	 * set its Entity-ID differently.
	 * @return
	 */
	public Ident getEntityID() {
		return myActualEntityID;
	}
	protected Ident getEntityTypeID() { 
		return myActualEntityTypeID;
	}
	public Ident getType() {
		return getEntityTypeID();
	}
	protected ThingActionSpec getActionSpec() { 
		return myTemplateActionSpec;
	}
	protected TypedValueMap getParameterMap() { 
		return getActionSpec().getParamTVM();
	}
	// Could have more elaborate type handling here, but for now, since params in repo are natively strings
	// we'll provide a way to load those raw strings by param name
	public String getSpecialString(Ident paramIdent) {
		return getParameterMap().getAsString(paramIdent);
	}



}
