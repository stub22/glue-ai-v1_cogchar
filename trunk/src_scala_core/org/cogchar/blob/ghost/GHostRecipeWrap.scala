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

package org.cogchar.blob.ghost


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


/*
 * 
 216	public boolean isInstanceof( URI classURI ) {
217		return Base.hasInstance(this.model, classURI, this.instanceIdentifier);
 = return model.contains(rdfResource, RDF.type, classURI);
 */