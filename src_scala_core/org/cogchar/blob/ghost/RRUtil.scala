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

/**
 * @author Stu B. <www.texpedient.com>
 */


import org.ontoware.rdf2go
import rdf2go.model.{Model => R2GoModel}
import rdf2go.model.node.{URI => R2GoURI}

import org.ontoware.rdfreactor
import rdfreactor.runtime.ReactorRuntimeEntity
import rdfreactor.schema.rdfs.{Class => RDFR_Class}

object RRUtil {
	// promote does *not* check for any asserted rdf:type, and does *not* assert the rdf:type.
	// Thus this type change is purely within Java space.
	def promote[X <: ReactorRuntimeEntity](orig : ReactorRuntimeEntity, tgtClz : java.lang.Class[X]) : X = {
		// Check using java runtime whether orig is already of the correct java type.
		if(tgtClz.isInstance(orig)) {
			// Since we are already the correct type, we short-circuit to just a type-cast for the existing instance.
			orig.asInstanceOf[X]
		} else {
			// We need to produce a new java instance of the correct type.
			// If no special converter is registered for this type, castTo will wind up calling
			// RDFReactorRuntime.resource2reactorbase, which in turn does reflection to find constructor, and then calls:
			// return constructor.newInstance(new Object[] { model, node, false });
			orig.castTo(tgtClz).asInstanceOf[X]
		}
	}
	def maybePromote[X <: ReactorRuntimeEntity](orig : ReactorRuntimeEntity, classURI : R2GoURI, tgtClz : java.lang.Class[X]) : Option[X] = {
		// isInstanceof does (only) a check for rdf:type in the model.  
		// model.contains(rdfResource, RDF.type, classURI);
		// So if we want inheritance, must enable inference on the model and apply appropriate ontology.
		if (orig.isInstanceof(classURI)) {
			Option(promote(orig, tgtClz))
		} else 	None
	}
}