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

package org.cogchar.impl.thing.filters;

import java.util.Set;

import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionFilter;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.impl.thing.basic.BasicTypedValueMapTemporaryImpl;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This concept is primarily implemented in Scala.
 * 
 * Includes patterns across all aspects of the entire contents of a ThingAction(Spec)
 * Not all of this data is present for every ThingAction(Spec)
 * 
 * First the more administrative aspects:
 *		actionSpecID - Resource-URI of the spec itself, sometimes absent.
 *		actionSpecPostedTimestamp - needed for filtering out "old" stuff.
 *		sourceAgentID - usually we shouldn't care
 * 
 * Then the meatier part - what the source agent "said":
 *		verbID (which is often a TypeID of the ThingAction, and we probably do *not* want verbTypeID).
 *			Often is along the lines of Know(/Create)Thing or UpdateThing or ForgetThing, where 
 *			actionParams are then the "contents" of the update, and targetThing is the handle for all 
 *			agents to use for followup references.
 *		targetThingID
 *		targetThingTypeID
 *		actionParams - arbitrary map of RDF-encodable stuff, with possible type markings.
 *			Match by the parameter name, value contents, types, and other related metadata.
 */

public class ThingActionFilterImpl extends KnownComponentImpl implements ThingActionFilter {
	/** @return  null or the Ident-URI of the reified filter spec (essentially: this object).	 */
	@Override public Ident getFilterSpecID() {
		return getIdent();
	}

	public ThingActionFilterImpl() {

	}

	private Ident hasVerb;

	private Ident hasThing;

	private Ident hasThingType;

	private Ident hasSourceAgent;

	private Long hasTStampGT = null;

	private Ident getNotMarkedByAgent;
    
    //Don't need to match yet
	private Set<Ident> hasPattern;

    //Param stuff below already matched
	private Ident hasParamName;

	private String hasParamNameAsString;

	private Ident hasParamObject;

	private Integer hasParamInt;

	private Float hasParamFloat;

	private String hasParamString;

	private TypedValueMap myTypedValueMap;

	/**
	 *  Return true if the params are are matchable
	 */
	@Override public boolean test(ThingActionSpec aSpec) {
		if (hasTStampGT != null && !(aSpec.getPostedTimestamp() > hasTStampGT))
			return false;

		if (!JenaLiteralUtils.isMatchAny(hasThingType)) {
			if (!JenaLiteralUtils.isTypeMatch(hasThingType, aSpec.getTargetThingTypeID()))
				return false;
		}
		if (!JenaLiteralUtils.isMatchAny(hasThing)) {
			if (!JenaLiteralUtils.isIndividualMatch(hasThing, aSpec.getTargetThingID()))
				return false;
		}
		if (!JenaLiteralUtils.isMatchAny(hasVerb)) {
			if (!JenaLiteralUtils.isIndividualMatch(hasVerb, aSpec.getVerbID()))
				return false;
		}
		if (!JenaLiteralUtils.isMatchAny(hasParamName)) {
			Object raw = aSpec.getParamTVM().getRaw(hasParamName);
			Object mustBe = getExpectedParamValue();
			if (!JenaLiteralUtils.isMatch(mustBe, raw))
				return false;
		}
		return true;
	}

	/**
	 * @param hasPattern Set is the set of sub ThingActionFilters that must be true in test(...) 
	 */
	void setHasPattern(Set<Ident> val) {
		this.hasPattern = val;
	}

	/**
	 * @return the hasPattern
	 */
	Set<Ident> getHasPattern() {
		return hasPattern;
	}

	/**
	 * @param verbID the hasVerb to set
	 */
	public void setHasVerb(Ident verbID) {
		this.hasVerb = verbID;
	}

	/** @return Should not be null.   The Verb-URI of the action (which is often an RDF:type of this actionSpec's 
	 * resource).     What are we doing to the target thing?	 */
	@Override public Ident getHasVerb() {
		return hasVerb;
	}

	/**
	 * @param hasThing the hasThing to set
	 */
	void setHasThing(Ident val) {
		this.hasThing = val;
	}

	/** @return null or the target Thing-URI of the action.  What are we operating on?	 */
	Ident getHasThing() {
		return hasThing;
	}

	public void setHasThingType(Ident val) {
		this.hasThingType = val;
	}

	/** @return null or the Type-URI of the target Thing.  What type of thing are we operating on?	 */
	@Override public Ident getHasThingType() {
		return hasThingType;
	}

	/**
	 * @param val the hasSourceAgent to set
	 */
	public void setHasSourceAgent(Ident val) {
		this.hasSourceAgent = val;
	}

	/** @return null or the source Agent-URI of the action.  Who initiated the operation?	 */
	@Override public Ident getHasSourceAgent() {
		return hasSourceAgent;
	}

	/**
	 * Java-timestamp (MSec since 1970) at which this ThingFilterSpec must be greater than
	 */
	public void setHasTStampGT(long val) {
		this.hasTStampGT = val;
	}

	/**
	 * 
	 * @return null or the Java-timestamp (MSec since 1970) at which this ThingFilterSpec must be greater than
	 * This is non-null only on the "receiving" side of the spec-transmission.
	 */
	@Override public Long getHasTStampGT() {
		return hasTStampGT;
	}

	/**
	 * @param val the getNotMarkedByAgent to set
	 */
	public void setGetNotMarkedByAgent(Ident val) {
		this.getNotMarkedByAgent = val;
	}

	/**
	 * @return the getNotMarkedByAgent
	 */
	@Override public Ident getGetNotMarkedByAgent() {
		return getNotMarkedByAgent;
	}

	/**  @return  Parameters of the action (keyed by URI), which are usually the updated properties of the target 
	 * thing.	 */
	@Override public TypedValueMap getParamTVM() {
		return myTypedValueMap;
	}

	/**
	 * @return the hasParamName
	 */
	public Ident getHasParamName() {
		return hasParamName;
	}

	/**
	 * @return the hasParamName
	 */
	public void sethasParamName(Ident val) {
		hasParamName = val;
	}

	/**
	 * @param val the hasParamName to set
	 */
	public void setHasParamNameAsString(String val) {
		val = hasParamNameAsString;
	}

	/**
	 * @return the hasParamName
	 */
	public String getHasParamNameAsString() {
		if (hasParamNameAsString == null && hasParamName != null	)
			return hasParamName.getLocalName();
		return hasParamNameAsString;
	}

    private Object getExpectedParamValue() {
        if(hasParamObject != null){
            return hasParamObject;
        }else if(hasParamString != null){
            return hasParamString;
        }else if(hasParamInt != null){
            return hasParamInt;
        }else if(hasParamFloat != null){
            return hasParamFloat;
        }
        return null;
    }

	public class ParamMapFromFields extends BasicTypedValueMapTemporaryImpl {
		boolean typeMapSet;

		/**
		 * @param val the hasParamObject to set
		 */
		public void setHasParamObject(Ident val) {
			hasParamObject = val;
		}

		/**
		 * @return the hasParamObject
		 */
		public Ident getHasParamObject() {
			return hasParamObject;
		}

		/**
		 * @param val the hasParamInt to set
		 */
		public void setHasParamInt(Integer val) {
			hasParamInt = val;
		}

		/**
		 * @return the hasParamInt
		 */
		public Integer getHasParamInt() {
			if (typeMapSet)
				return getAsInteger(hasParamName);
			return hasParamInt;
		}

		/**
		 * @param val the hasParamFloat to set
		 */
		public void setHasParamFloat(Float val) {
			hasParamFloat = val;
		}

		/**
		 * @return the hasParamFloat
		 */
		public float getHasParamFloat() {
			if (typeMapSet)
				return getAsInteger(hasParamName);
			return hasParamFloat;
		}

		/**
		 * @param val the hasParamString to set
		 */
		public void setHasParamString(String val) {
			hasParamString = val;
		}

		/**
		 * @return the hasParamString
		 */
		public String getHasParamString() {
			return hasParamString;
		}
	}

}
