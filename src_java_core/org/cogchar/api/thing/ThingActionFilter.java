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

import java.util.Set;

import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.core.name.Ident;

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

public interface ThingActionFilter extends MutableKnownComponent, KnownComponent {
	/**
	 *  Return true if the params are are matchable
	 */
	public boolean test(ThingActionSpec aSpec);

	/** @return  null or the Ident-URI of the reified filter spec (essentially: this object).	 */
	public abstract Ident getFilterSpecID();

	/** @return Should not be null.   The Verb-URI of the action (which is often an RDF:type of this actionSpec's 
	 * resource).     What are we doing to the target thing?	 */
	public abstract Ident getHasVerb();

	/** @return null or the Type-URI of the target Thing.  What type of thing are we operating on?	 */
	public abstract Ident getHasThingType();

	/** @return null or the source Agent-URI of the action.  Who initiated the operation?	 */
	public abstract Ident getHasSourceAgent();

	/**
	 * 
	 * @return null or the Java-timestamp (MSec since 1970) at which this ThingFilterSpec must be greater than
	 * This is non-null only on the "receiving" side of the spec-transmission.
	 */
	public abstract Long getHasTStampGT();

	/**
	 * @return the getNotMarkedByAgent
	 */
	public abstract Ident getGetNotMarkedByAgent();

	/**  @return  Parameters of the action (keyed by URI), which are usually the updated properties of the target 
	 * thing.	 */
	public abstract TypedValueMap getParamTVM();
}
