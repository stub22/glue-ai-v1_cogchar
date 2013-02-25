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

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 * A very basic BasicTypedValueMap implementation for testing, but the ultimate implementation probably won't look much like this.
 * This is good for roughing out functionality, but should really be a prettier Scala file in o.c.impl.thing (I believe)
 * The fact this isn't there is part of what indicates this is a temporary structure!
 * 
 * Stu 2013-02-24:  At present this class is used on both sides of our ThingAct - pump.
 * This class is used on the receiving sidedirectly in 
 * o.c.api.thing.ThingActionUpdater.buildActionParameterValueMap().
 * 
 * It is also used as the base class for ConcreteTVM, which is in the Scala file 
 * org/cogchar/impl/thing/FancyThingTest.scala
 * 
 * ... which is then used in the Robosteps 
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
 
public class BasicTypedValueMapTemporaryImpl extends BasicTypedValueMap {
	public String getSparqlText(Ident propName) {
		// Not sure how we'll be using this exactly, so for the moment we'll just return an empty string
		return "";
	}
	
	public Ident getAsIdent(Ident propName) {
		// return new FreeIdent(ThingCN.THING_NS + (String)getRaw(propName));
		return null;
	}
	public String getAsString(Ident propName) {
		return (String)getRaw(propName);
	}
	public Integer getAsInteger(Ident propName) {
		try {
			return Integer.valueOf((String)getRaw(propName));
		} catch (Exception e) {
			return null;
		}
	}
	public Long getAsLong(Ident propName) {
		try {
			return Long.valueOf((String)getRaw(propName));
		} catch (Exception e) {
			return null;
		}
	}
	public Float getAsFloat(Ident propName) {
		try {
			return Float.valueOf((String)getRaw(propName));
		} catch (Exception e) {
			return null;
		}
	}
	public Double getAsDouble(Ident propName) {
		try {
			return Double.valueOf((String)getRaw(propName));
		} catch (Exception e) {
			return null;
		}
	}
}
