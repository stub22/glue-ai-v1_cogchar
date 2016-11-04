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

import org.appdapter.core.name.{Ident, FreeIdent};
import org.ontoware.rdfreactor
import rdfreactor.runtime.ReactorRuntimeEntity

import com.hp.hpl.jena
import org.ontoware.rdf2go

trait RdfNodeUtils {
	// The random URIs from R2Go look like:  urn:rnd:-5b2ee5ac:1492ee4f206:-7ffc
	// FreeIdent must supply a localName that endsWith absURI
	
	def yieldIdent(thing : ReactorRuntimeEntity) : Ident = {
		val thingUriTxt = thing.asURI.toString
		// val tailChop = thingUriTxt.lastIndexOf(x$1)
		yieldIdent(thing.asURI)
	}
	def yieldIdent(r2goURI : rdf2go.model.node.URI) : Ident =  {
		val uriTxt = r2goURI.toString
		// TODO: grab a less verbose tail fragment to use for localName
		val localName = uriTxt
		new FreeIdent(uriTxt, localName)
	}
	
	def yieldIdent(jenaRes : jena.rdf.model.Resource) : Ident = {
		// We could instead use JenaResourceItem.
		new FreeIdent(jenaRes.getURI, jenaRes.getLocalName)
	}
	
	def yieldR2GoURI(id : Ident) : rdf2go.model.node.URI = {
		val uriTxt = id.getAbsUriString
		new rdf2go.model.node.impl.URIImpl(uriTxt)
	}
	def yieldR2GoURI(jenaRes : jena.rdf.model.Resource) : rdf2go.model.node.URI = {
		new rdf2go.model.node.impl.URIImpl(jenaRes.getURI)
	}
	
	
	
}
