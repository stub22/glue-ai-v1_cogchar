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

abstract class FancyUrlMediaHandle[MediaType](mediaID : Ident, myResolver : MediaPathResolver) 
			extends BaseUrlMediaHandle[MediaType](mediaID)  {
	override def getMediaURL() : URL = {
		val resolvedPath = myResolver.getMediaResourcePath(mediaID)
		new URL(resolvedPath)
	}
}

trait MediaPathResolver {
	def getMediaResourcePath(mediaID : Ident) : String
}
trait ClassLoaderUrlResolver extends MediaPathResolver {
	protected def getClassLoaders() : java.util.List[ClassLoader]

	def resolveMediaURL(mediaID : Ident) : URL = {
		val	mediaResPath = getMediaResourcePath(mediaID)
		val clList = getClassLoaders()
		ClassLoaderUtils.findResourceURL(mediaResPath, clList)
	}	
}

trait MediaPathModelResolver extends MediaPathResolver {
	val dummy : Int = -99
	protected def getPathModel() : Model 
	protected def getPathPropertyID () : Ident
	override def getMediaResourcePath(mediaID : Ident) : String = {
		"" // Use Jena-API directly(?) to fetch out the path value for this piece of media
	}
}



class FancyMediaPathResolver(myPathModel : Model, myPathPropID : Ident, myCLLoaders : java.util.List[ClassLoader]) 
		extends ClassLoaderUrlResolver with MediaPathModelResolver {
	
	override 	protected def getPathModel() : Model = myPathModel
	override	protected def getPathPropertyID() : Ident = myPathPropID
	override	protected def getClassLoaders()  : java.util.List[ClassLoader] = myCLLoaders
}
trait MediaHandleCache[MediaType] {
	val		myHandlesByID = new scala.collection.mutable.HashMap[Ident, MediaHandle[MediaType]]()
	
	protected def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType]
	def findMediaHandle(mediaID : Ident) : MediaHandle[MediaType] = {
		myHandlesByID.getOrElseUpdate(mediaID, {makeMediaHandle(mediaID)})
	}
}

abstract class FancyMediaHandleCache[MediaType](pathModel : Model, pathPropID : Ident, clLoaders : java.util.List[ClassLoader]) 
		extends  FancyMediaPathResolver(pathModel, pathPropID, clLoaders) with MediaHandleCache[MediaType] {
			
	protected def makeMediaHandle(mediaID : Ident, resolver: MediaPathResolver ) 
			: MediaHandle[MediaType]		
			
	override protected def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType] = {
		makeMediaHandle(mediaID, this) //  getPathModel,  getPathPropertyID, getClassLoaders)
	}

}

