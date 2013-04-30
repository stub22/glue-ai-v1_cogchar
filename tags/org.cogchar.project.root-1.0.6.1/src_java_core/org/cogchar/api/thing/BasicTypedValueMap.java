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
import java.util.Iterator;
import java.util.Map;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicTypedValueMap implements TypedValueMap {
	
	private	Map<Ident, Object>		myRawObjsByID = new HashMap<Ident, Object>();

	@Override public int getSize() { 
		return myRawObjsByID.size();
	}
	@Override public Iterator<Ident>	iterateKeys() {
		return myRawObjsByID.keySet().iterator();
	}

		
	@Override public Object getRaw(Ident name) {
		return myRawObjsByID.get(name);
	}
	protected <VT> VT getValueAtNameAs(Ident name, Class<VT> valClass) {
		VT typedResult = null;
		Object rawVal = myRawObjsByID.get(name);
		if (rawVal != null) {
			typedResult = (VT) rawVal;
		}
		return typedResult;
	}
	public void putValueAtName(Ident name, Object val) {
		myRawObjsByID.put(name, val);
	}
	public void putNameAtName(Ident name, Ident nameVal) { 
		putValueAtName(name, nameVal);
	}
	public Ident getNameAtName(Ident name) {
		return getValueAtNameAs(name, Ident.class);
	}
}
