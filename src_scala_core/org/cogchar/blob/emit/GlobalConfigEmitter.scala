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

import org.appdapter.core.item.{FreeIdent, Ident}

object GlobalConfigEmitter {
  // Constants for query config - this could live elsewhere but may make sense here
  // As usual, meta-meta-data keeps squeezing out into code
  private final val gr = "http://www.cogchar.org/general/config#"
  private final val GLOBALMODE_QUERY_TEMPLATE_URI = "ccrt:template_globalmode_99" 
  private final val ENTITIES_QUERY_TEMPLATE_URI = "ccrt:template_global_entities_99"
  private final val GLOBALMODE_QUERY_VAR_NAME = "mode"
  private final val ENTITY_TYPE_QUERY_VAR_NAME = "type"
  private final val ENTITY_VAR_NAME = "entity"
  private final val ROLE_VAR_NAME = "role"
  private final val GRAPH_VAR_NAME = "graph"
  private final val ENTITY_TYPES = Array("CharEntity", "VirtualWorldEntity", "WebappEntity", "SwingAppEntity")
}

// An object class to hold "global" configuration information loaded at "boot"
// Currently corresponds to "GlobalMode" bindings
class GlobalConfigEmitter {
  
  // We'll get the QueryInterface from QuerySheet for the moment. Goofy. Should this really come from a registry? Use the
  // (likely needlessly) managed service instance? Or will we decide referencing the singleton QueryEmitter is robust
  // and acceptable enough that its simplicity and "always-on" nature trump uneasy feelings about that technique?
  // This issue is still being worked out.
  private val qi = QuerySheet.getInterface
  
  // A "triple map" keyed on config "entity" with values of maps keyed by "role", each of which have a "role" object
  //val ergMap = new scala.collection.mutable.HashMap[Ident, scala.collection.mutable.HashMap[Ident, Ident]]
  // Using Java maps for now to minimize interoperability issues
  val ergMap = new java.util.HashMap[Ident, java.util.HashMap[Ident, Ident]]
  
  // A map of lists which will contain the listed (enabled) "entities" now listed on the "GlobalModes" tab
  // Could be keyed by Idents of entity types, but probably simplier to use string tags here in Cog Char
  val entityMap = new java.util.HashMap[String, java.util.List[Ident]]
  
  // For now, we provide a constructor to build the Global Config from "GlobalModes" tab on query-based sheet
  // This could be joined by other constructors to alternately get the config from other resources
  def this(globalModeUri: Ident) = {
	this()
	// First, the ergMap of "bindings" is populated
	val query = qi.getCompletedQueryFromTemplate(GlobalConfigEmitter.GLOBALMODE_QUERY_TEMPLATE_URI, 
												 GlobalConfigEmitter.GLOBALMODE_QUERY_VAR_NAME, globalModeUri);
	val solutionList = qi.getTextQueryResultList(query);
	solutionList.list.foreach(solution => {
		val entityIdent = qi.getIdentFromSolution(solution, GlobalConfigEmitter.ENTITY_VAR_NAME);
		var rgMap: java.util.HashMap[Ident, Ident] = null;
		if (ergMap containsKey entityIdent) {
		  rgMap = ergMap.get(entityIdent)
		} else {
		  rgMap = new java.util.HashMap[Ident, Ident]
		  ergMap.put(entityIdent, rgMap)
		}
		val roleIdent = qi.getIdentFromSolution(solution, GlobalConfigEmitter.ROLE_VAR_NAME);
		val graphIdent = qi.getIdentFromSolution(solution, GlobalConfigEmitter.GRAPH_VAR_NAME);
		rgMap.put(roleIdent, graphIdent)
	  })
	// Next, the entityMap is created
	for (i <- 0 until GlobalConfigEmitter.ENTITY_TYPES.length) {
	  val query = qi.getCompletedQueryFromTemplate(GlobalConfigEmitter.ENTITIES_QUERY_TEMPLATE_URI,
												   GlobalConfigEmitter.ENTITY_TYPE_QUERY_VAR_NAME,
												   new FreeIdent(GlobalConfigEmitter.gr+GlobalConfigEmitter.ENTITY_TYPES(i), GlobalConfigEmitter.ENTITY_TYPES(i)))
	  val queryWithMode = qi.setQueryVar(query, GlobalConfigEmitter.GLOBALMODE_QUERY_VAR_NAME, globalModeUri)
	  val solutionList = qi.getTextQueryResultList(queryWithMode)
	  val entityList = qi.getIdentsFromSolutionAsJava(solutionList, GlobalConfigEmitter.ENTITY_VAR_NAME)
	  //println("GlobalConfigEmitter putting list for type " + GlobalConfigEmitter.ENTITY_TYPES(i) + ": " + entityList) // TEST ONLY
	  entityMap.put(GlobalConfigEmitter.ENTITY_TYPES(i), entityList)
	}
  }
  
}
