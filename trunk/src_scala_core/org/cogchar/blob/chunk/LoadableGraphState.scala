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

import com.hp.hpl.jena
import jena.rdf.model.{ Model => JenaModel}

import org.cogchar.api.owrap.mdir.{GraphPointer => MdirGraphPointer, GraphHost => MdirGraphHost, GraphHost3Serial}

import org.cogchar.blob.ghost.{RRUtil, GHostUtil}
// There is no need for a class or trait called LoadableGraphHandle.
// What we do instead is provide the guts as LoadableGraphState, and the handle type is then
// TypedItemHandle[LoadableGraphState].  Colloquially we sometimes call that a LoadableGraph(State)Handle,
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

// 
// Note that we do *not* expect this type to be extended to hold application-specific information about the
// graph.   Any such information should be embodied in myIndexGP and whatever app-specific index-data it links to.
final class LoadableGraphState(val myIndexGP : MdirGraphPointer, private val myResolvedSourceGHost : MdirGraphHost) 
		extends VarargsLogging {
	// We call it a "payload" model to distinguish from associated index models.
	var		myPayloadModelR2GoM_opt : Option[R2GoModel] = None // Initially there is no payload model loaded.

	// This should only be able to fail under rare circumstances, or if we have bad code assumptions.
	def getOpenPayloadModel : Option[R2GoModel] = {
		if (myPayloadModelR2GoM_opt.isEmpty) {
			// TODO:  Get smarter about what kind of GHost we have.  Is it of dimension 4 or a 3?  
			// But so far we just assume its for a file-backed model (GraphHost3Serial) that we want to read into memory.
			val ser3GH : GraphHost3Serial = RRUtil.promote(myResolvedSourceGHost, classOf[GraphHost3Serial])
			val loadedModel_opt : Option[JenaModel] = GHostUtil.readModelFromGHost3Serial(ser3GH)
			if (loadedModel_opt.isDefined) {
				val contentR2GoM : R2GoModel = new rdf2go.impl.jena.ModelImplJena(loadedModel_opt.get)
				contentR2GoM.open
				myPayloadModelR2GoM_opt = Some(contentR2GoM)
			} else {
				error2("Cannot load graph for pointer {} at resolved host {}", myIndexGP, myResolvedSourceGHost)
			}
		}
		myPayloadModelR2GoM_opt
	}
	def releasePayloadModel : Unit = {
		if (myPayloadModelR2GoM_opt.isDefined) {
			myPayloadModelR2GoM_opt.get.close
			myPayloadModelR2GoM_opt = None
		}
	}
}

// Special case where we only care about the ability to find LoadableGraphsState-Handles.
class LGStateChunkHandle(chunk : FriendlyChunk, chunkUriWrap : HasPossiblyTypedURI, parentCH : ChunkHandle) 
		extends FriendlyChunkHandle(chunk, chunkUriWrap, parentCH) {
			
	def getLGStateHandle(graphPointerUriWrap : HasURI) : Option[TypedItemHandle[LoadableGraphState]] = {
		getTypedHandle(graphPointerUriWrap, LoadableGraphHandleFuncs.MGP_TypeMarkUriWrap, classOf[LoadableGraphState])
	}
}


object LoadableGraphHandleFuncs extends VarargsLogging {
	val MGP_TypeMarkUriWrap : HasTypeMarkURI = new HasR2GoClassURI(MdirGraphPointer.RDFS_CLASS)
	// This make-maker method returns a function closure with bindings for the required context values.
	// We supply it with readable index models as discussed above.
	// These index model-handles are required to exist when this maker is constructed, 
	// although the actual graph contents may change anytime efore the maker-func is actually invoked.
	def makeLGHandleMaker(gpIndexModel : R2GoModel, sourceGHostIndexModel : R2GoModel, parentCH : ChunkHandle) 
			: Function1[HasURI, TypedItemHandle[LoadableGraphState]] = {
		hasUri => {
			// Now we are inside the closure.  This is called when cache.getOrMakeTypedHandle(wuri : HasURI)
			// is invoked the first time for a given URI. We build a handle to contain an MdirGraphPointer 
			// and a MdirGraphHost.
			val grapPointerR2GoURI : R2GoURI = hasUri.getR2GoURI
			// Presumably the pointer already exists in the index model (otherwise the getAllPoints... is going to fail).
			//               false => Do *not* write an rdf:type for this pointer uri! [because all required types should
			//               already exist.  Some of those types may be app-specific, and mdir:GraphPointer itself 
			//               may be implied, inferred, asserted or ignored as the app sees fit.].  
			val indexGP : MdirGraphPointer = new MdirGraphPointer(gpIndexModel, grapPointerR2GoURI, false)
			// These are only partially resolved, because we don't know if they are connected to the right model.
			val partiallyResolvedGHosts : Array[MdirGraphHost] = indexGP.getAllPointsToGraphHost_as.asArray
			val prghCount = partiallyResolvedGHosts.size 
			if (prghCount == 1) {
				// Connect the URI to the model we were told to use for the sourceGHosts.
				val resolvedSourceGHost = new MdirGraphHost(sourceGHostIndexModel, partiallyResolvedGHosts(0).asURI, false)
				// Make a live object instance to track the loading state of the content data.
				val lg = new LoadableGraphState(indexGP, resolvedSourceGHost)
		
				val typedURI : HasPossiblyTypedURI = new HasRRInstance(indexGP)
				// Make the handle to be cached.
				val itemHandle = new TypedItemHandle[LoadableGraphState](lg, typedURI, parentCH)
				itemHandle
			} else {
				error3("Expected 1 resolvedGHost for {} but got {} : {}", indexGP, prghCount : Integer, partiallyResolvedGHosts)
				throw new RuntimeException("Got unexpected GHost count " + prghCount + " for " + indexGP)
			}
		}
	}
	// Makes a chunk with just one type cache in it, able to load all the graphs that have pointers in gpIdxModel,
	// resolved into gHosts in srcGHostIdxModel.
	def makeChunkForLoadableGraphs(chunkUriWrap : HasPossiblyTypedURI, parentCH : ChunkHandle, gpIdxModel : R2GoModel, 
				srcGHostIdxModel : R2GoModel) : LGStateChunkHandle = {
		val chunk = new InternalChunk
		val chunkHandle = new LGStateChunkHandle(chunk, chunkUriWrap, parentCH)
		val lgHandleMaker = makeLGHandleMaker(gpIdxModel, srcGHostIdxModel, chunkHandle)
		// The rdf:type of the uri key is mdir:GraphPointer, whereas the handle payload type is LoadableGraphState.
		chunk.setupTypedCache(MGP_TypeMarkUriWrap, classOf[LoadableGraphState], lgHandleMaker)
		chunkHandle
	}
	
}