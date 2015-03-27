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

// The meaning and type-constraint of the handle is not known at this layer.
// The Cache is empowered to *make* handles as needed.
trait HandleCache extends VarargsLogging {
	def getHandle(instUri : HasURI) : Option[Handle]
	
	// Overwrites any existing handle for this URI (or a subtype may throw an exception if overwrite is undesirable).  
	protected def makeAndStoreHandle(instUri : HasURI) : Unit
}
// Next layer we add parametric type 
// IT = "Item Type" = Type of the thing that we wrap.

trait TypedHandleCache[IT] extends HandleCache {
	override def getHandle(instUri : HasURI) : Option[Handle] = getTypedHandle(instUri)
	
	def getTypedHandle(instUri : HasURI) : Option[TypedItemHandle[IT]]
	
	protected def makeTypedHandle(wuri : HasURI) : TypedItemHandle[IT]
	protected def putTypedHandle(h : TypedItemHandle[IT]) : Unit
	
	override protected def makeAndStoreHandle(instUri : HasURI) : Unit = {
		val tih = makeTypedHandle(instUri)
		// debug2("For {} made typed handle {}", instUri, tih)
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
