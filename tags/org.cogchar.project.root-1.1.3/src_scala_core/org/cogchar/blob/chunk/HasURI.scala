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

package org.cogchar.blob.chunk
import org.appdapter.fancy.log.VarargsLogging

import org.ontoware.rdf2go
import org.ontoware.rdfreactor

import rdf2go.model.{Model => R2GoModel}
import rdf2go.model.node.{URI => R2GoURI}

import rdfreactor.runtime.{ReactorRuntimeEntity}

import rdfreactor.schema.rdfs.{Resource => RRResource}

import org.cogchar.api.owrap.mdir.{GraphPointer => MdirGraphPointer, GraphHost => MdirGraphHost}

/**
 * @author Stu B. <www.texpedient.com>
 */

trait HasURI {
	// Used as foundation cache-key, but also sometimes implemented by cache-values.
	// Must do a good job with HashCode and Equals!
	// 
	def getR2GoURI : R2GoURI
}

trait HasTypeMarkURI extends HasURI {
	// Wraps a URI that is known to be usable as an RDF:Type.  
}

// Some URI that we *may* be able to find rdf:types for, each of which is represented by a HasTypeMarkURI.

trait HasPossiblyTypedURI extends HasURI  {
	// The answer depends on what kind of model we are connected to, if any.  By default we got nuthin!
	def findKnownTypes : Traversable[HasTypeMarkURI] = Nil
}

// "case" marker means that hash-code and equals will delegate to myR2GoURI
case class HasR2GoURI(val myR2GoURI : R2GoURI) extends HasURI with HasPossiblyTypedURI {
	override def getR2GoURI : R2GoURI = myR2GoURI
	// At this level findKnownTypes() still comes up empty.  Use HasRRInstance if you really want types.
}

class HasR2GoClassURI(r2goClassURI : R2GoURI) extends HasR2GoURI(r2goClassURI) with HasTypeMarkURI

// This rrInst implies model backing.
class HasRRInstance(rrInst : RRResource) extends HasR2GoURI(rrInst.asURI) with HasPossiblyTypedURI {
	override def findKnownTypes : Traversable[HasTypeMarkURI] = {
		// We actually have *two* sources of type information.  
		// One is determined by the concrete java type of the rrInst.
		// Even if it turns out that the backing model has gone away, we should always be able to return this type.
		val javaBasedURI : R2GoURI = rrInst.getRDFSClassURI
		// The other types consist of whatever is asserted in the model behind rrInst.  
		// That set may overlap with the javaBasedURI, or not.
		// (It all depends on the client code that created the rrInst).
		val assertedClasses : Set[R2GoURI] = rrInst.getAllType_as().asArray.map(_.asURI).toSet
		(assertedClasses + javaBasedURI).toArray.map(new HasR2GoClassURI(_))
	}
}

object UriWrapFactory { 
}