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

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.web.WebEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author StuB22
 */
 
// TODO:  Add error logging, paying attention to efficiency, since we might get
// a lot of errors here sometimes.

public class BasicTypedValueMapWithConversion extends BasicTypedValueMap {
	private static Logger theLogger = LoggerFactory.getLogger(BasicTypedValueMapWithConversion.class);
	protected Logger getLogger() { 
		return theLogger;
	}
	@Override public String getSparqlText(Ident propName) {
		// Not sure how we'll be using this exactly, so for the moment we'll just return an empty string
		return "";
	}
	
	@Override public Ident getAsIdent(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Ident) {
					return (Ident) rawVal;
				} else if (rawVal instanceof String) {
					String identString = (String) rawVal;
					// TODO: consider expanding QName here
					// return new FreeIdent(ThingCN.THING_NS + (String)getRaw(propName));					
					return new FreeIdent(identString);
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Ident from rawVal {} found at {}", rawVal, propName, e);
		}
		return null;			
	}
	@Override public String getAsString(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  String) {
					return (String) rawVal;
				} else {
					return rawVal.toString();
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Long from rawVal {} found at {}", rawVal, propName, e);
		}
		return null;			
	}
	@Override public Integer getAsInteger(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Integer) {
					return (Integer) rawVal;
				} else if (rawVal instanceof Number) { 
					return ((Number) rawVal).intValue();
				} else if (rawVal instanceof String) {
					return (Integer.valueOf((String) rawVal));
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Long from rawVal {} found at {}", rawVal, propName, e);
		}
		return null;		
	}
	@Override public Long getAsLong(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Long) {
					return (Long) rawVal;
				} else if (rawVal instanceof Number) { 
					return ((Number) rawVal).longValue();
				} else if (rawVal instanceof String) {
					return (Long.valueOf((String) rawVal));
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Long from rawVal {} found at {}", rawVal, propName, e);
			
		}
		return null;		
	}
	// TODO:  Autoconvert double to float and v.v.
	@Override public Float getAsFloat(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Float) {
					return (Float) rawVal;
				} else if (rawVal instanceof Number) { 
					return ((Number) rawVal).floatValue();
				} else if (rawVal instanceof String) {
					return (Float.valueOf((String) rawVal));
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Float from rawVal {} found at {}", rawVal, propName, e);
		}
		return null;
	}
	@Override public Double getAsDouble(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Double) {
					return (Double) rawVal;
				} else if (rawVal instanceof Number) { 
					return ((Number) rawVal).doubleValue();
				} else if (rawVal instanceof String) {
					return (Double.valueOf((String) rawVal));
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Double from rawVal {} found at {}", rawVal, propName, e);
		}
		return null;
	}

	@Override public Boolean getAsBoolean(Ident propName) {
		Object rawVal = null;
		try {
			rawVal = getRaw(propName);
			if (rawVal != null) {
				if (rawVal instanceof  Boolean) {
					return (Boolean) rawVal;
				} else if (rawVal instanceof String) {
					return (Boolean.valueOf((String) rawVal));
				}	
			} 
		} catch (Exception e) {
			getLogger().warn("Cannot make Boolean from rawVal {} found at {}", rawVal, propName, e);
		}	
		return null;
	}
}
