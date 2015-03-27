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

// There is no need for a class or trait called LoadableGraphHandle.
// What we do instead is provide the guts as LoadableGraphState, and the handle type is then
// TypedItemHandle[LoadableGraphState].  Colloquially we sometimes call that a LoadableGraph[State]Handle,
// but that is not a defined scala type name.
// 
// We assume that both myIndexGP and myResolvedGHost are backed by open index models.
// Those may be the same index model, or separate ones.
// We do not write to the models from this layer of the code.
// We do not assume anything about their mutability.
// The user of the LGHandle might choose to write information into the index models (by writing to
// the myIndexGP and myResolvedSourceGHost).  If that succeeded or failed due to the index model being readonly,
// that would not be our problem.
// 
// TODO:  Get smarter about whether the GHost is of dimension 4 or a 3.
// 
// Note that we do *not* expect this type to be extended to hold application-specific information about the
// graph.   Any such information should be embodied in myIndexGP and whatever app-specific index-data it links to.
final class LoadableGraphState(val myIndexGP : MdirGraphPointer, private val myResolvedSourceGHost : MdirGraphHost) {
	var		myContentR2GoM_opt : Option[R2GoModel] = None // Initially there is no model loaded.
}

object LoadableGraphHandleFuncs extends VarargsLogging {
	// This make-maker method returns a function closure with bindings for the required context values.
	// We supply it with readable index models as discussed above.
	def makeLGHandleMaker(gpIndexModel : R2GoModel, sourceGHostIndexModel : R2GoModel, parentCH : ChunkHandle) 
			: Function1[HasURI, TypedItemHandle[LoadableGraphState]] = {
		hasUri => {
			// Now we are inside the closure.  This is called when cache.getOrMakeTypedHandle(wuri : HasURI)
			// is invoked the first time for a given URI. We build a handle to contain an MdirGraphPointer 
			// and a MdirGraphHost.
			val grapPointerR2GoURI : R2GoURI = hasUri.getR2GoURI
			// Presumably the pointer already exists in the index model (otherwise the getAllPoints... is going to fail).
			//               false => Do *not* write an rdf:type for this pointer uri!
			val indexGP : MdirGraphPointer = new MdirGraphPointer(gpIndexModel, grapPointerR2GoURI, false)
			// These are only partially resolved, because we don't know if they are connected to the right model.
			val partiallyResolvedGHosts : Array[MdirGraphHost] = indexGP.getAllPointsToGraphHost_as.asArray
			val prghCount = partiallyResolvedGHosts.size 
			if (prghCount == 1) {
				// Connect the URI to the model we were told to use for the sourceGHosts.
				val resolvedSourceGHost = new MdirGraphHost(sourceGHostIndexModel, partiallyResolvedGHosts(0).asURI, false)
				// Make a live object instance to track the loading state of the content data.
				val lg = new LoadableGraphState(indexGP, resolvedSourceGHost)
				// TODO:  Make the typedURI based on the indexGP and its backing model.
				val typedURI : HasTypedURI = null
				// Make the handle to be cached.
				val itemHandle = new TypedItemHandle[LoadableGraphState](lg, typedURI, parentCH)
				itemHandle
			} else {
				error3("Expected 1 resolvedGHost for {} but got {} : {}", indexGP, prghCount : Integer, partiallyResolvedGHosts)
				throw new RuntimeException("Got unexpected GHost count " + prghCount + " for " + indexGP)
			}
		}
	}
	// Makes a chunk with just one type cache in it
	def makeChunkForLoadableGraphs(parentCH : ChunkHandle, gpIdxModel : R2GoModel, srcGHostIdxModel : R2GoModel) : FriendlyChunk = {
		val chunk = new InternalChunk
		val handleMaker = makeLGHandleMaker(gpIdxModel, srcGHostIdxModel, parentCH)
		// The rdf:type of the uri key is mdir:GraphPointer, whereas the handle payload type is LoadableGraphState.
		val typURI : HasTypeMarkURI = new HasR2GoClassURI(MdirGraphPointer.RDFS_CLASS)
		chunk.setupTypedCache(typURI, classOf[LoadableGraphState], handleMaker)
		chunk
	}
}