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

public interface ThingActionPattern {

}
