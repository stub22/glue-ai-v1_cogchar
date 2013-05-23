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

import org.appdapter.core.name.Ident;

/**
 * This Spec defines a spec that could or should or has or will happen(ed).
 * Spec = [Agent] Verb Target {prop1 = val1, prop2 = val2...}
 * 
 * It is used both hypothetically and concretely
 * Recursive bottom:  Verbs and ActionSpecs are not "things" (cannot themselves be "targeted" for action)
 * However, agents *are* things.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public interface ThingActionSpec {
	
	/** @return  null or the Ident-URI of the reified action spec (essentially: this object).	 */
	public	Ident			getActionSpecID();
	
	/** @return Should not be null.   The Verb-URI of the action (which is often an RDF:type of this actionSpec's 
	 * resource).     What are we doing to the target thing?	 */
	public	Ident			getVerbID();

	/** @return null or the target Thing-URI of the action.  What are we operating on?	 */
	public	Ident			getTargetThingID();

	/** @return null or the Type-URI of the target Thing.  What type of thing are we operating on?	 */
	public  Ident			getTargetThingTypeID();
	
	/** @return null or the source Agent-URI of the action.  Who initiated the operation?	 */
	public	Ident			getSourceAgentID();
	
	/**  @return  Parameters of the action (keyed by URI), which are usually the updated properties of the target 
	 * thing.	 */
	public	TypedValueMap	getParamTVM();

	/**
	 * 
	 * @return null or the Java-timestamp (MSec since 1970) at which this ThingActionSpec was "posted" into a repo.
	 * This is non-null only on the "receiving" side of the spec-transmission.
	 */
	public Long				getPostedTimestamp();
}
