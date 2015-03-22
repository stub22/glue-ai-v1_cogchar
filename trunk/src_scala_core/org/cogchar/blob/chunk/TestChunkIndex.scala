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
 * They are also generally cachable entities which are keyed by their URIs. 
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

trait HasURI {
	// Used as foundation cache-key, but also sometimes implemented by cache-values.
	// Must do a good job with HashCode and Equals!
	// 
	def getR2GoURI : R2GoURI
}

trait WrapsTyped[WT] {
	// This is a general wrapper-type consumable by code that doesn't require any kind of URI binding.
	def unwrap : WT
}

trait HasTypeMarkURI extends HasURI {
	// Wraps a URI that is known to be usable as an RDF:Type.  
}

// Some URI that we expect to be able to find rdf:types for, each of which is represented by a HasTypeMarkURI.
trait HasTypedURI extends HasURI  {
	// The answer depends on what kind of model we are connected to, if any.  By default we got nuthin!
	def findKnownTypes : Traversable[HasTypeMarkURI] = Nil
}

// Lowest layer of Handle+Cache system is non-parametric.  It would be OK to extend this layer directly for a plugin.
// 
// Client code sees Handles only, and never touches Chunks or Caches directly.
// 
// Case class generates hashCode() and equals(), so we hereby 
// define that any 2 ItemHandles with equal URIWrap and equal chunkHandles are "equal".
trait Handle { 
	// URI is unique identifier for this handle *within its chunk*.
	def getItemURI : HasURI 
	// A handle (including a ChunkHandle) may belong to at most one parent chunk.
	// Chunk handles thus form a tree.
	// Knowing this allows us to resolve difficult references relative to the handle.
	// For example we have a handle for a graph based story object, which refers to audio files.
	// We want those files to be located from a folder that is "nearby" to the story-graph.  
	// We can do that by looking in our chunk and in sibling chunks.  
	def getParentChunkHandle : Option[ChunkHandle]
	// Usually there is also some stored object data.
}

trait TypedHandle[IT] extends Handle with WrapsTyped[IT]

// To be an ItemHandle there *must* be a parent chunk, so ItemHandle cannot be used as a root-chunk-handle.
case class ItemHandle(val myURIWrap : HasURI, val myParentChunkHandle : ChunkHandle) extends Handle {
	override def getItemURI : HasURI = myURIWrap
	override def getParentChunkHandle = Some(myParentChunkHandle)
	// Usually there is also some stored object data.
}
// The meaning and type-constraint of the handle is not known at this layer.
// The Cache is empowered to *make* handles as needed.
trait HandleCache {
	def getHandle(instUri : HasURI) : Option[Handle]
	
	// Overwrites any existing handle for this URI (or a subtype may throw an exception if overwrite is undesirable).  
	protected def makeAndStoreHandle(instUri : HasURI) : Unit
}
trait Chunk {
	// Each type mark (rdf:type) denotes a caching-subspace of the chunk, and presumably the HandleCaches
	// are constrained to contain only appropriate subtypes of Handle.
	def getCacheForType(tUri : HasTypeMarkURI) : Option[HandleCache]
}
// A chunk is basically a 2-dimensional cache map, acessed via a chunkHandle.
// We may eventually want to generalize to a case where a ChunkHandle can be virtual or remote.
trait ChunkHandle extends Handle {
	def getHandle(instUri : HasURI, tUri : HasTypeMarkURI) : Option[_ <: Handle]
}
abstract class SubChunkHandle(uriWrap : HasURI, parentCH : ChunkHandle) extends ItemHandle(uriWrap, parentCH) with ChunkHandle

abstract class RootChunkHandle(uriWrap : HasURI) extends ChunkHandle {
	// Usually used only for accessing SubChunkHandles.  
}


// Next layer we add parametric type 
// IT = "Item Type" = Type of the thing that we wrap.
// Beyond what ItemHandle assumes, we add these assumptions:  
//		1) Our uriWrap can (at least try to) access rdf:types
//		2) At construction time, we have an actual item object to wrap, 
//		which we can hold in an immutable val (though it may change internally)

class TypedItemHandle[IT](val myItem : IT, uriWrap : HasTypedURI, parentCH : FriendlyChunkHandle) 
				extends ItemHandle(uriWrap, parentCH) with  TypedHandle[IT] {
	override def unwrap : IT = myItem
	// If the original uriWrap was connected to a Model, this can return useful rdf:types.
	def getTypedItemURI : HasTypedURI = uriWrap
}



trait TypedHandleCache[IT] extends HandleCache {
	override def getHandle(instUri : HasURI) : Option[Handle] = getTypedHandle(instUri)
	
	def getTypedHandle(instUri : HasURI) : Option[TypedItemHandle[IT]]
	
	protected def makeTypedHandle(wuri : HasURI) : TypedItemHandle[IT]
	protected def putTypedHandle(h : TypedItemHandle[IT]) : Unit
	
	override protected def makeAndStoreHandle(instUri : HasURI) : Unit = {
		val tih = makeTypedHandle(instUri)
		putTypedHandle(tih)
	}
	
	// Maker will need to know how to turn the input WrapsURI into a TypedURI (by attaching it to some model)
	// and also how to create the wrapped thing itself.
	// The maker func is the factory plugin that makes the handles real.
	// We know that handleMaker is going to need to have access to the outer chunkHandle.
	// , maker : Function1[HasURI, TypedItemHandle[IT]]
	def getOrMakeTypedHandle(wuri : HasURI) : TypedItemHandle[IT] = {
		val existing_opt : Option[TypedItemHandle[IT]] = getTypedHandle(wuri)
		existing_opt.getOrElse({
			makeAndStoreHandle(wuri)
			// val made = maker(wuri)
			// putTypedHandle(made)
			getTypedHandle(wuri).get
		})	
	}
	// If we assumed that a Maker is registered when the cache is built, then we could do
	// def getOrMakeTypedHandle(wuri : WrapsURI) : TypedItemHandle[HT] 
}

import scala.collection.mutable.HashMap

class THCacheHash[HT](handleMaker : Function1[HasURI, TypedItemHandle[HT]]) extends TypedHandleCache[HT] {
	private lazy val myMap = new HashMap[HasURI, TypedItemHandle[HT]]
	override def getTypedHandle(wuri : HasURI) : Option[TypedItemHandle[HT]] = myMap.get(wuri)
	override protected def putTypedHandle(h : TypedItemHandle[HT]) : Unit = myMap.put(h.getTypedItemURI, h)
	override protected def makeTypedHandle(wuri : HasURI) : TypedItemHandle[HT] = {
		handleMaker(wuri)
	}
}


trait FriendlyChunk extends Chunk {
	
//	protected def findOrMakeCache[HT](wturi : HasTypeMarkURI, handleMaker : Function1[HasURI, TypedItemHandle[HT]], clz : Class[HT]) : TypedHandleCache[HT] 

	
	override def getCacheForType(tUri : HasTypeMarkURI) : Option[HandleCache] = getTypedCache(tUri, classOf[Any])
	def getTypedCache[HT](typUri : HasTypeMarkURI, handleClz : Class[HT]) : Option[TypedHandleCache[HT]]
	
	def getTypedHandle[HT](instUri : HasURI, typUri : HasTypeMarkURI, handleClz : Class[HT]) : Option[TypedItemHandle[HT]] = {
		val cache_opt = getTypedCache(typUri, handleClz)
		cache_opt.flatMap(_.getTypedHandle(instUri))
		//val cache = findOrMakeCache[HT](wturi, null, clz)
		//cache.getTypedHandle(wiuri)
	}
}
class InternalChunk extends FriendlyChunk {
	lazy val myHandleCachesByType = new HashMap[HasTypeMarkURI, TypedHandleCache[_]]
	
	override def getTypedCache[IT](typUri : HasTypeMarkURI, handleClz : Class[IT]) : Option[TypedHandleCache[IT]] = {
		myHandleCachesByType.get(typUri).map(_.asInstanceOf[THCacheHash[IT]])
	}
	def putTypedCache(typUri : HasTypeMarkURI, cache : TypedHandleCache[_]) : Unit = myHandleCachesByType.put(typUri, cache)
	
	def setupTypedCache[IT](typUri : HasTypeMarkURI, handleClz : Class[IT],  handleMaker : Function1[HasURI, TypedItemHandle[IT]]) : Unit = {
		val cache = new THCacheHash[IT](handleMaker)
		putTypedCache(typUri, cache)
	}
	/*
	override protected def findOrMakeCache[HT](wturi : HasTypeMarkURI, handleMaker : Function1[HasURI, TypedItemHandle[HT]], clz : Class[HT]) : TypedHandleCache[HT] = {
		val existing_opt = myHandleCachesByType.get(wturi)
		existing_opt.getOrElse({
			val made = new THCacheHash[HT](null)
			myHandleCachesByType.put(wturi, made)
			made
		}).asInstanceOf[THCacheHash[HT]]
	}
	*/

	// lazy val myHandleCachesByType = new HashMap[WrapsTypeURI, ]
}
class FriendlyChunkHandle(chunk : FriendlyChunk, uriWrap : HasTypedURI, parentCH : FriendlyChunkHandle) 
			extends TypedItemHandle[FriendlyChunk](chunk, uriWrap, parentCH) with ChunkHandle {
				
	override def getHandle(instUri : HasURI, typUri : HasTypeMarkURI) : Option[_ <: Handle] = {
		val qClz = classOf[Any]
		chunk.getTypedHandle(instUri, typUri, qClz)
	}
	//def getOrMakeTypedHandle [HT](wiuri : WrapsURI, wturi : WrapsTypeURI, clz : Class[HT], 
	//							  maker : Function1[WrapsURI, TypedItemHandle[HT]) : TypedItemHandle[HT] = {
	// }
}
import org.appdapter.fancy.log.VarargsLogging
import org.cogchar.blob.circus.{ GraphScanTest}
object TestChunkIndex extends VarargsLogging {
	def main(args: Array[String]): Unit = {
		GraphScanTest.setupScanTestLogging
		info0("Starting TestChunkIndex")
	}
}



// "case" marker means that hash-code and equals will delegate to myR2GoURI
case class HasR2GoURI(val myR2GoURI : R2GoURI) extends HasURI {
	override def getR2GoURI : R2GoURI = myR2GoURI
}
// Hmmm.  This would work in cases where we are only interested in caching an entity, but commonly we need 
// *more* state in the handle than what the entity itself can offer.  
// The entity consists of a binding of a URI in a model context.
trait RRTHandle[RRT <: ReactorRuntimeEntity] extends TypedHandle[RRT] {	
}

import org.cogchar.api.owrap.mdir.{GraphPointer => MdirGraphPointer, GraphHost => MdirGraphHost}

// We assume that both myIndexGP and myResolvedGHost are backed by open index models.
// Those may be the same index model, or separate ones.
// 
// TODO:  Get smarter about whether the GHost is of dimension 4 or a 3.
class LoadableGraph(val myIndexGP : MdirGraphPointer, val myResolvedSourceGHost : MdirGraphHost) {
	var		myContentR2GoM_opt : Option[R2GoModel] = None
	
	
}

object LoadableGraphFuncs extends VarargsLogging {
	// This method returns a function closure with bindings of the required context values.
	def makeLGHandleMaker(gpIndexModel : R2GoModel, sourceGHostIndexModel : R2GoModel, parentFCH : FriendlyChunkHandle) 
			: Function1[HasURI, TypedItemHandle[LoadableGraph]] = {
		hasUri => {
			val r2goURI : R2GoURI = hasUri.getR2GoURI
			val indexGP : MdirGraphPointer = new MdirGraphPointer(gpIndexModel, r2goURI, false)
			val partiallyResolvedGHosts : Array[MdirGraphHost] = indexGP.getAllPointsToGraphHost_as.asArray
			val prghCount = partiallyResolvedGHosts.size 
			if (prghCount == 1) {
				val resolvedSourceGHost = new MdirGraphHost(sourceGHostIndexModel, partiallyResolvedGHosts(0).asURI, false)
				val lg = new LoadableGraph(indexGP, resolvedSourceGHost)
				val typedURI : HasTypedURI = null
				val itemHandle = new TypedItemHandle[LoadableGraph](lg, typedURI, parentFCH)
				itemHandle
			} else {
				error3("Expected 1 resolvedGHost for {} but got {} : {}", indexGP, prghCount : Integer, partiallyResolvedGHosts)
				throw new RuntimeException("Got unexpected GHost count " + prghCount + " for " + indexGP)
			}
		}
	}
}