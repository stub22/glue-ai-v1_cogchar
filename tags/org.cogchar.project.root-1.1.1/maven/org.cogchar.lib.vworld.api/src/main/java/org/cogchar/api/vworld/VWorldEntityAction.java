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

package org.cogchar.api.vworld;

import org.cogchar.api.thing.BasicEntityAction;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.name.goody.GoodyNames;

/**
 * A superclass to hold actions from ActionSpecs for execution by various Entity subclasses
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 * @author Stu B22
 */

public class VWorldEntityAction extends BasicEntityAction  {
	// We can optionallly play a game of equivalence between Java-enum-constant and URI, without an additional hashMap.
	// The price is that we must initialize the value in the enum constants.
	// But, well - the values inside those enum objects *could* be updatable from config, right? 
	public enum Kind {
		CREATE(GoodyNames.ACTION_CREATE.getAbsUriString());
		
		public	String myKindUriString;
		
		private Kind(String uriString) {
			myKindUriString = uriString;
		}
	}

	protected	Kind					myKind;
	
	public VWorldEntityAction(ThingActionSpec actionSpec) {
		super(actionSpec);
		String kindIdentString = actionSpec.getVerbID().getAbsUriString();
		for (Kind kindToCheck : Kind.values()) {
			if (kindToCheck.myKindUriString.equals(kindIdentString)) {
				myKind = kindToCheck;
				break;
			}
		}
	}
	// Is this something we want to expose publicly? Seems we may need to...
	public Kind getKind() {
		return myKind;
	}

}
