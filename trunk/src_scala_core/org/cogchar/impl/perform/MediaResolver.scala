/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.perform

import java.net.URL;
import org.appdapter.core.name.Ident;

import org.cogchar.platform.util.ClassLoaderUtils;

import com.hp.hpl.jena.rdf.model.{Model}

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Defines and implements how we find Media for performances, using available
 * metadata and provided media libraries
 */

trait MediaHandle[MediaType] {
	def getMediaID() : Ident
	def getMedia() : Option[MediaType]
}
trait UrlMediaHandle[MediaType] extends MediaHandle[MediaType] {
	def getMediaURL() : URL
}
abstract class BaseUrlMediaHandle[MediaType](val myID : Ident) extends UrlMediaHandle[MediaType] {
	var	myCachedMedia : Option[MediaType] = None
	override def getMediaID() : Ident = myID
	def getMedia() : Option[MediaType] = {
		if (myCachedMedia.isEmpty) {
			myCachedMedia = resolveUrlAndFetchMedia() 
		}
		myCachedMedia
	}
	private def resolveUrlAndFetchMedia() : Option[MediaType] = { 
		val mediaURL = getMediaURL()
		getMediaFromUrl(mediaURL)
	}
	protected def getMediaFromUrl(url : URL) : Option[MediaType]
}	
trait ClassLoaderUrlResolver {
	protected def getClassLoaders() : java.util.List[ClassLoader]
	protected def getMediaResourcePath() : String
	def getMediaURL() : URL = {
		val	mediaResPath = getMediaResourcePath()
		val clList = getClassLoaders()
		ClassLoaderUtils.findResourceURL(mediaResPath, clList)
	}	
}

trait MediaPathModelResolver {
	protected def getPathModel() : Model 
	protected def getPathPropertyID () : Ident
	protected def getMediaResourcePath() : String = {
		""
	}
}

abstract class FancyUrlMediaHandle[MediaType](mediaID : Ident, myPathModel : Model, myPathPropID : Ident, myCLLoaders : java.util.List[ClassLoader]) 
		extends BaseUrlMediaHandle[MediaType](mediaID) with ClassLoaderUrlResolver with MediaPathModelResolver {
	
	override 	protected def getPathModel() : Model = myPathModel
	override	protected def getClassLoaders()  : java.util.List[ClassLoader] = myCLLoaders
	override	protected def getPathPropertyID() : Ident = myPathPropID
}
abstract class BaseMediaHandleCache[MediaType]() {
	val		myHandlesByID = new scala.collection.mutable.HashMap[Ident, MediaHandle[MediaType]]()
	
	protected def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType]
	def findMediaHandle(mediaID : Ident) : MediaHandle[MediaType] = {
		myHandlesByID.getOrElseUpdate(mediaID, {makeMediaHandle(mediaID)})
	}
}

abstract class FancyUrlMediaHandleCache[MediaType](myPathModel : Model, myPathPropID : Ident, myCLLoaders : java.util.List[ClassLoader]) 
		extends BaseMediaHandleCache[MediaType] {
	protected def makeMediaHandle(mediaID : Ident, pathModel : Model, pathPropID : Ident, clLoaders : java.util.List[ClassLoader]) 
			: MediaHandle[MediaType]		
			
	override protected def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType] = {
		makeMediaHandle(mediaID, myPathModel,  myPathPropID, myCLLoaders)
	}

}

