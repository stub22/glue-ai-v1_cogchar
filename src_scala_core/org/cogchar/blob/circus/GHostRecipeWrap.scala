/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.circus


import org.appdapter.fancy.log.VarargsLogging
import org.cogchar.api.owrap



// import owrap.mdir.GraphHost
import owrap.crcp.{GhostRecipe, GhRRefer}
import org.cogchar.api.owrap.mdir.{GraphPointer => MdirGraphPointer, GraphHost => MdirGraphHost}

class GHostRecipeWrap(val myGR : GhostRecipe) extends VarargsLogging {
	// Utility methods for working with a GHostRecipe record, associated GHost, and graphs produced from it.  
	// Similar/related to several existing files in Cogchar and FriendU, which are:  PutTest.scala, MDirEdit.scala, ___

	def isDirectGHostReference : Boolean = myGR.isInstanceof(GhRRefer.RDFS_CLASS)

	// This method carries us across the boundary from ccrp: GraphHost to mdir: GraphHost.
	def getAllResultGHosts : Traversable[owrap.mdir.GraphHost]  = {
		val ccrpGHosts : Array[owrap.crcp.GraphHost] = myGR.getAllGraphHost_as().asArray
		debug1("Found referredGhosts: {}", ccrpGHosts)
		ccrpGHosts.map(RRUtil.promote(_, classOf[owrap.mdir.GraphHost])) 
	}
	def getResultGHost : Option[owrap.mdir.GraphHost]  = {
		getAllResultGHosts.headOption
	}
	// It may be possible for a single GHostRecipe to product multiple result GHosts, but that is an exception to 
	// normal pattern.   To pay attention to those we do something like:
	// val referredGHosts : Array[owrap.crcp.GraphHost] = myGR.getAllGraphHost_as().asArray
	// referredGHosts.map(RRUtil.promote(_, classOf[owrap.mdir.GraphHost]))
	
}
object GHostRecipeUtil { 
	
}


import org.ontoware.rdf2go
import rdf2go.model.{Model => R2GoModel}
import rdf2go.model.node.{URI => R2GoURI}

import org.ontoware.rdfreactor
import rdfreactor.runtime.ReactorRuntimeEntity
import rdfreactor.schema.rdfs.{Class => RDFR_Class}

object RRUtil {
	// promote does *not* check for any asserted rdf:type, and does *not* assert the rdf:type.
	// Thus this type change is purely within Java space.
	def promote[X](orig : ReactorRuntimeEntity, tgtClz : java.lang.Class[X]) : X = {
		// If no special converter is registered for this type, castTo will wind up calling
		// RDFReactorRuntime.resource2reactorbase, which in turn does reflection to find constructor, and then calls:
		// return constructor.newInstance(new Object[] { model, node, false });
		orig.castTo(tgtClz).asInstanceOf[X]
	}
	def maybePromote[X](orig : ReactorRuntimeEntity, classURI : R2GoURI, tgtClz : java.lang.Class[X]) : Option[X] = {
		// isInstanceof does (only) a check for rdf:type in the model.  
		// model.contains(rdfResource, RDF.type, classURI);
		// So if we want inheritance, must enable inference on the model and apply appropriate ontology.
		if (orig.isInstanceof(classURI)) {
			Option(promote(orig, tgtClz))
		} else 	None
	}
}
/*
 * 
 216	public boolean isInstanceof( URI classURI ) {
217		return Base.hasInstance(this.model, classURI, this.instanceIdentifier);
 = return model.contains(rdfResource, RDF.type, classURI);
 */