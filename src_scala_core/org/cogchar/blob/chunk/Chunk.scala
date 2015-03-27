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
 */

// A chunk is basically a 2-dimensional cache map, acessed via a chunkHandle.
// We may eventually want to generalize to a case where a ChunkHandle can be virtual or remote.
trait Chunk {
	// Each type mark (rdf:type) denotes a caching-subspace of the chunk, and presumably the HandleCaches
	// are constrained to contain only appropriate subtypes of Handle.
	def getCacheForType(tUri : HasTypeMarkURI) : Option[HandleCache]
}


// Friendly chunk provides typesafe methods to fetch a cache or a handle.
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

import scala.collection.mutable.HashMap

// InternalChunk is a *class* with a data-map of typed-handle-caches.
// Thus it may hold one typesafe cache of objects of type A, another typesafe cache of objects of type B, and so on.
// We can add caches to it for additional types by calling "setupTypedCache".
class InternalChunk extends FriendlyChunk {
	private lazy val myHandleCachesByType = new HashMap[HasTypeMarkURI, TypedHandleCache[_]]
	
	override def getTypedCache[IT](typUri : HasTypeMarkURI, handlePayloadClz : Class[IT]) : Option[TypedHandleCache[IT]] = {
		myHandleCachesByType.get(typUri).map(_.asInstanceOf[THCacheHash[IT]])
	}
	protected def putTypedCache(typUri : HasTypeMarkURI, cache : TypedHandleCache[_]) : Unit = myHandleCachesByType.put(typUri, cache)
	
	// Here we add a new typed cache to our chunk.
	def setupTypedCache[IT](typUri : HasTypeMarkURI, handlePayloadClz : Class[IT],  handleMaker : Function1[HasURI, TypedItemHandle[IT]]) : Unit = {
		val cache = new THCacheHash[IT](handleMaker)
		putTypedCache(typUri, cache)
	}

}