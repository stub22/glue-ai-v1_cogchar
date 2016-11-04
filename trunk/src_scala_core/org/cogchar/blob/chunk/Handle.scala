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
	// For example we might have a handle for a graph-based story object, which refers to some audio files.
	// We may want those files to be located from a folder that is "nearby" to the story-graph.  
	// We can do that by starting from the story-handle, grabbing its chunk, and either:
	// A) Looking in other caches of that chunk
	// or
	// B) Proceeding up the chunk-handle-tree, looking for closest chunk that has the audio-handle-cache we want.
	// Setting up that search process is a big part of why the Handle+Chunk system exists in the first place!
	def getParentChunkHandle : Option[ChunkHandle]
	// Usually there is also some stored object data in the handle, which is defined by subtypes of handle.
}

// The most common way to add data to a handle in a typesafe manner is is 
trait TypedHandle[IT] extends Handle with WrapsTyped[IT]

// To create an ItemHandle there *must* be a parent chunk, [so ItemHandle cannot be used for a root-chunk-handle.]
case class ItemHandle(private val myURIWrap : HasURI, private val myParentChunkHandle : ChunkHandle) extends Handle {
	override def getItemURI : HasURI = myURIWrap
	override def getParentChunkHandle = Some(myParentChunkHandle)
	// Usually there is also some stored object data.
}

// Next layer we add parametric type 
// IT = "Item Type" = Type of the thing that we wrap.
// Beyond what ItemHandle assumes, we add these assumptions:  
//		1) Our uriWrap can (at least try to) access rdf:types
//		2) At construction time, we have an actual item object to wrap, 
//		which we can hold in an immutable val (though it may change internally)

class TypedItemHandle[IT](val myItem : IT, uriWrap : HasPossiblyTypedURI, parentCH : ChunkHandle) 
				extends ItemHandle(uriWrap, parentCH) with  TypedHandle[IT] {
	override def unwrap : IT = myItem
	// If the original uriWrap was connected to a Model, this can return useful rdf:types.
	def getTypedItemURI : HasPossiblyTypedURI = uriWrap
}

// Hmmm.  This would work in cases where we are only interested in caching an entity, but commonly we need 
// *more* state in the handle than what the entity itself can offer.  
// trait RRTHandle[RRT <: ReactorRuntimeEntity] extends TypedHandle[RRT] {}