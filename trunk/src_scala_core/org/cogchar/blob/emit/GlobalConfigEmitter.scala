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

package org.cogchar.blob.emit

import org.appdapter.core.name.{FreeIdent, Ident}
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.fancy.rclient.{RepoClient}
import org.appdapter.fancy.query.{SolutionHelper}

import org.cogchar.name.entity.EntityRoleCN._;


trait GlobalConfigResolver {
	def getEntityIdentsForMarker(marker : String) : Traversable[Ident]
}

// An object class to hold "global" configuration information loaded at "boot"
// Currently corresponds to "GlobalMode" bindings
class GlobalConfigEmitter(val myQI : RepoClient) extends GlobalConfigResolver with BasicDebugger {
	// Constants for query config - this could live elsewhere but may make sense here
	// As usual, meta-meta-data keeps squeezing out into code

	def getEntityTypeIdent(i : Int) = new FreeIdent(GC_NS+ENTITY_TYPES(i), ENTITY_TYPES(i))
	
	private val sh = new SolutionHelper()
  
	// A "triple map" keyed on config "entity" with values of maps keyed by "role", each of which have a "role" object
	//val ergMap = new scala.collection.mutable.HashMap[Ident, scala.collection.mutable.HashMap[Ident, Ident]]
	// Using Java maps for now to minimize interoperability issues
	val ergMap = new java.util.HashMap[Ident, java.util.HashMap[Ident, Ident]]
  
	// A map of lists which will contain the listed (enabled) "entities" now listed on the "GlobalModes" tab
	// Could be keyed by Idents of entity types, but probably simplier to use string tags here in Cog Char
	val entityMap = new java.util.HashMap[String, java.util.List[Ident]]
  
	// For now, we provide a constructor to build the Global Config from "GlobalModes" tab on query-based sheet
	// This could be joined by other constructors to alternately get the config from other resources
	def this(aqi :  RepoClient, globalModeID: Ident) = {
		this(aqi)
		// First, the ergMap of "bindings" is populated
	
		val solutionList = myQI.queryIndirectForAllSolutions(GLOBALMODE_QUERY_QN, GLOBALMODE_QUERY_VAR_NAME, globalModeID);
		solutionList.list.foreach(solution => {
				val entityIdent = sh.pullIdent(solution, ENTITY_VAR_NAME);
				var rgMap: java.util.HashMap[Ident, Ident] = null;
				if (ergMap containsKey entityIdent) {
					rgMap = ergMap.get(entityIdent)
				} else {
					rgMap = new java.util.HashMap[Ident, Ident]
					ergMap.put(entityIdent, rgMap)
				}
				val roleIdent = sh.pullIdent(solution, ROLE_VAR_NAME);
				val graphIdent = sh.pullIdent(solution, GRAPH_VAR_NAME);
				rgMap.put(roleIdent, graphIdent)
			})
		// Next, the entityMap is created
		for (i <- 0 until ENTITY_TYPES.length) {
			val qib = myQI.makeInitialBinding
			qib.bindIdent(ENTITY_TYPE_QUERY_VAR_NAME, getEntityTypeIdent(i))
			qib.bindIdent(GLOBALMODE_QUERY_VAR_NAME, globalModeID)
			val solutionList = myQI.queryIndirectForAllSolutions(ENTITIES_QUERY_QN, qib)
	  
			
			val entityList = sh.pullIdentsAsJava(solutionList, ENTITY_VAR_NAME)
			//println("GlobalConfigEmitter putting list for type " + GlobalConfigEmitter.ENTITY_TYPES(i) + ": " + entityList) // TEST ONLY
			entityMap.put(ENTITY_TYPES(i), entityList)
		}
	}
  
	// Below is an experiment in making Global Config a managed service, which is something I haven't truly
	// been at all convinced is the right thing to do. But necessary for our Lifter-as-managed-service
	// experiment. Managed services beget managed services!!
	// Right now this really feels wrong to make this a service! I don't believe in these maps enough yet.
	trait GlobalConfigService {
		def getErgMap: java.util.HashMap[Ident, java.util.HashMap[Ident, Ident]]
		def getEntityMap: java.util.HashMap[String, java.util.List[Ident]]
	}
}
