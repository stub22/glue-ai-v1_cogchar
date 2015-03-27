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

trait ChunkHandle extends Handle {
	// Provides a convenience API for searching the Chunk's *contents*.
	def getHandle(instUri : HasURI, tUri : HasTypeMarkURI) : Option[_ <: Handle]
}
// abstract class SubChunkHandle(uriWrap : HasURI, parentCH : ChunkHandle) extends ItemHandle(uriWrap, parentCH) with ChunkHandle

class RootChunkHandle(private val myUriWrap : HasURI) extends ChunkHandle {
	// val mySubChunkHandleCache = new THCacheHash[Chunk]
	override def getItemURI : HasURI = myUriWrap
	override def getParentChunkHandle = None
	// RootChunkHandle is generally used only as a parent for other ChunkHandles.
	override def getHandle(instUri : HasURI, tUri : HasTypeMarkURI) : Option[_ <: Handle] = ???
}

// Here chunkUriWrap is an identifier for the chunk.  The chunkUriWrap may have some type information available,
// which comes from outside the chunk's contents.
class FriendlyChunkHandle(chunk : FriendlyChunk, chunkUriWrap : HasPossiblyTypedURI, parentCH : ChunkHandle) 
			extends TypedItemHandle[FriendlyChunk](chunk, chunkUriWrap, parentCH) with ChunkHandle {
				
	override def getHandle(instUri : HasURI, typUri : HasTypeMarkURI) : Option[_ <: Handle] = {
		val qClz = classOf[Any]
		chunk.getTypedHandle(instUri, typUri, qClz)
	}
}


