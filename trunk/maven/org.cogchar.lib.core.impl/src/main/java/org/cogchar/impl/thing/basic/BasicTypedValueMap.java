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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.SerIdent;
import org.cogchar.api.thing.SerTypedValueMap;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicTypedValueMap implements SerTypedValueMap {
	
	public BasicTypedValueMap() {
		
	}
	private	Map<SerIdent, Serializable>		myRawObjsByID = new HashMap<SerIdent, Serializable>();

	@Override public int getSize() { 
		return myRawObjsByID.size();
	}
	@Override public Iterator<Ident>	iterateKeys() {
		Set<Ident> idSet = new HashSet<Ident>();
		idSet.addAll(myRawObjsByID.keySet());
		return idSet.iterator();
	}

	@Override public Object getRaw(Ident name) {
		SerIdent serID = ensureSerIdent(name);
		return myRawObjsByID.get(serID);
	}
	protected <VT> VT getValueAtNameAs(Ident name, Class<VT> valClass) {
		SerIdent serID = ensureSerIdent(name);
		VT typedResult = null;
		Object rawVal = myRawObjsByID.get(serID);
		if (rawVal != null) {
			typedResult = (VT) rawVal;
		}
		return typedResult;
	}
	@Override public void putValueAtName(Ident name, Object val) {
		if (val instanceof Serializable) {
			SerIdent serID = ensureSerIdent(name);
			myRawObjsByID.put(serID, (Serializable) val);
		} else {
			throw new RuntimeException("Cannot put nonserializable object of type " + val.getClass() + " into a BasicTypedValueMap");
		}
	}
	@Override public void putNameAtName(Ident name, Ident nameVal) { 
		putValueAtName(name, nameVal);
	}
	@Override public Ident getNameAtName(Ident name) {
		return getValueAtNameAs(name, Ident.class);
	}
	@Override  public String toString() {
		return "BasicTypedValueMap[objsByID=" + myRawObjsByID + "]";
	}	
	
	private SerIdent ensureSerIdent(Ident in) { 
		if (in instanceof SerIdent) {
			return (SerIdent) in;
		} else {
			return new FreeIdent(in);
		}
	}
}
