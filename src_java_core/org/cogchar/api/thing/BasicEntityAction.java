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
 */

public abstract class BasicEntityAction {
	private	 ThingActionSpec	myActionSpec;
	
	protected Ident				myEntityID;
	protected Ident				myEntityTypeID;
	
	protected TypedValueMap		paramTVMap;
	
	public BasicEntityAction(ThingActionSpec actionSpec) {
		myActionSpec = actionSpec;
		myEntityID = actionSpec.getTargetThingID();
		myEntityTypeID = actionSpec.getTargetThingTypeID();
		
		paramTVMap = myActionSpec.getParamTVM();
	}


	/**
	 * If our ThingActionSpec supplied a targetThingID, we use that by default.
	 * However, during creation or other "special" operation, our GoodyAction action may
	 * set its GoodyID differently.
	 * @return
	 */
	public Ident getEntityID() {
		return myEntityID;
	}

	// Could have more elaborate type handling here, but for now, since params in repo are natively strings
	// we'll provide a way to load those raw strings by param name
	public String getSpecialString(Ident paramIdent) {
		return paramTVMap.getAsString(paramIdent);
	}

	public Ident getType() {
		return myEntityTypeID;
	}
	protected ThingActionSpec getActionSpec() { 
		return myActionSpec;
	}

}
