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

// Some URI that we expect to be able to find rdf:types for, each of which is represented by a HasTypeMarkURI.
trait HasTypedURI extends HasURI  {
	// The answer depends on what kind of model we are connected to, if any.  By default we got nuthin!
	def findKnownTypes : Traversable[HasTypeMarkURI] = Nil
}

// "case" marker means that hash-code and equals will delegate to myR2GoURI
case class HasR2GoURI(val myR2GoURI : R2GoURI) extends HasURI {
	override def getR2GoURI : R2GoURI = myR2GoURI
}

class HasR2GoClassURI(r2goClassURI : R2GoURI) extends HasR2GoURI(r2goClassURI) with HasTypeMarkURI