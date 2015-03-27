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

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * 
 * Chunks and Handles are live objects which do not have their own URIs.
 * They are wrappers for objects that have URIs.
 * They are also generally cachable entities which are keyed by their (wrapped) URIs. 
 * They hold mutable state indicating the loaded+cached status of their contents,
 * and may also hold flags such "dirty".
 * 
 * Do we make them type-factored by mdir: type?
 */

import org.ontoware.rdf2go
import org.ontoware.rdfreactor

import rdf2go.model.{Model => R2GoModel}
import rdf2go.model.node.{URI => R2GoURI}

import rdfreactor.runtime.ReactorRuntimeEntity
import rdfreactor.schema.rdfs.{Class => RDFR_Class}





import org.appdapter.fancy.log.VarargsLogging
import org.cogchar.blob.circus.{ GraphScanTest}
object TestChunkIndex extends VarargsLogging {
	def main(args: Array[String]): Unit = {
		GraphScanTest.setupScanTestLogging
		info0("Starting TestChunkIndex")
	}
}




