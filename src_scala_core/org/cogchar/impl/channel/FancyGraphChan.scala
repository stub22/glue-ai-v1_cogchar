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

package org.cogchar.impl.channel

import org.cogchar.api.channel.{BasicGraphChan}
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient} 
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}
import org.cogchar.api.thing.{ThingActionUpdater, ThingActionSpec}
/**
 * @author Stu B. <www.texpedient.com>
 */

class FancyGraphChan(chanID : Ident, val myRepoClient : RepoClient) extends BasicGraphChan(chanID) with FancyChannel {
	
}
class SingleSourceGraphChan(chanID : Ident, rc : RepoClient, val myMatchGraphID : Ident) extends FancyGraphChan(chanID, rc) {
}

class TypePollingGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident, matchTypeID : Ident) 
		extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
	
}
// chanID is used as the agent
class ThingActionGraphChan(chanID : Ident, rc : RepoClient, matchGraphID : Ident) 
		extends SingleSourceGraphChan(chanID, rc, matchGraphID) {
	
	def seeThingActions() : java.util.List[ThingActionSpec] = {
		val tau = new ThingActionUpdater();
		val tasList : java.util.List[ThingActionSpec] = tau.seeActions(rc, matchGraphID, chanID);
		tasList;
	}
	
	
}