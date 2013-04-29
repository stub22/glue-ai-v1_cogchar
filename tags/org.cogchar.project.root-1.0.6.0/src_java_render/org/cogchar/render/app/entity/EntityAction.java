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

package org.cogchar.render.app.entity;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.name.goody.GoodyNames;

/**
 * A superclass to hold actions from ActionSpecs for execution by various Entity subclasses
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

public class EntityAction  {
	// We can optionallly play a game of equivalence between Java-enum-constant and URI, without an additional hashMap.
	// The price is that we must initialize the value in the enum constants.
	public enum Kind {
		CREATE(GoodyNames.ACTION_CREATE.getAbsUriString());
		
		public	String myKindUriString;
		
		private Kind(String uriString) {
			myKindUriString = uriString;
		}
	}

	protected	Kind					myKind;
	
	
	protected	ThingActionSpec			mySpec;

	protected	Ident					myEntityID;
	protected	Ident					myEntityTypeID;
	
	protected	TypedValueMap			paramTVMap;
	
	public EntityAction(ThingActionSpec actionSpec) {
		mySpec = actionSpec;
		myEntityID = actionSpec.getTargetThingID();
		myEntityTypeID = actionSpec.getTargetThingTypeID();
		
		paramTVMap = mySpec.getParamTVM();
		String kindIdentString = actionSpec.getVerbID().getAbsUriString();
		for (Kind kindToCheck : Kind.values()) {
			if (kindToCheck.myKindUriString.equals(kindIdentString)) {
				myKind = kindToCheck;
				break;
			}
		}
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
	// Is this something we want to expose publically? Seems we may need to...
	public Kind getKind() {
		return myKind;
	}
	
	public Ident getType() {
		return myEntityTypeID;
	}
	
	
	// Could have more elaborate type handling here, but for now, since params in repo are natively strings
	// we'll provide a way to load those raw strings by param name
	public String getSpecialString(Ident paramIdent) {
		return paramTVMap.getAsString(paramIdent);
	}

}
