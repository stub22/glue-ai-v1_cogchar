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
	/**
	 * Public API method used by the handle consumer.  If it fails it returns None.
	 * If called again, it will re-attempt to load the media on each call until it succeeds,
	 * after which time it always returns our cached result.
	 */
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
	/** This is what each concrete MediaHandle type must implement for its allowed range of MediaTypes */
	protected def getMediaFromUrl(url : URL) : Option[MediaType]
}
trait MediaPathFinder {
	def findMediaPath(mediaID : Ident) : String
}
trait UrlSearcher  {
	def resolveMediaPathToURL(rawMediaPath : String) : URL 
}
class ClasspathUrlSearcher(classLoaders : Seq[ClassLoader]) extends UrlSearcher {
	import scala.collection.JavaConversions._
	val		myClassLoadersJL : java.util.List[ClassLoader] = classLoaders

	override def resolveMediaPathToURL(rawMediaPath : String) : URL = {
		ClassLoaderUtils.findResourceURL(rawMediaPath, myClassLoadersJL)
	}	
}
abstract class FancyUrlMediaHandle[MediaType](mediaID : Ident, myPathFinder : MediaPathFinder, myUrlSearcher : UrlSearcher) 
			extends BaseUrlMediaHandle[MediaType](mediaID)  {
				
	override def getMediaURL() : URL = {
		val rawPath : String = myPathFinder.findMediaPath(mediaID)
		myUrlSearcher.resolveMediaPathToURL(rawPath)
	}
}

import scala.collection.mutable.HashMap
import org.cogchar.impl.channel.FancyFile

class FancyFileSpecMediaPathFinder extends MediaPathFinder {
	
	val	myFilesByID = new HashMap[Ident, FancyFile]()
	
	override def findMediaPath(mediaID : Ident) : String = {
		if (myFilesByID.contains(mediaID)) {
			// From scaladoc (v.2.10) for HashMap.apply:  Retrieves the value which is associated with the given key. 
			// This method invokes the default method of the map if there is no mapping from the given key to a value. 
			// Unless overridden, the default method throws a NoSuchElementException.
			val ff : FancyFile = myFilesByID.apply(mediaID)
			ff.myResolvedFullPath
		} else {
			"could_not_resolve_media_id[" + mediaID + "]";
		}
	}
	def absorbFancyFileSpecs(fileSpecs : Traversable[FancyFile]) : Unit = {
		for (ff <- fileSpecs) {
			
		}
	}
	// Optional :  Add "absorb path model" or similar
}

/*
trait ModelBackedPathResolver extends MediaPathResolver {
	val dummy : Int = -99
	protected def getPathModel() : Model 
	protected def getPathPropertyID () : Ident
	override def getMediaResourcePath(mediaID : Ident) : String = {
		"" // TODO:  Use Jena-API directly(?) to fetch out the path value for this piece of media
	}
}
class FancyModelBackedPathResolver(myPathModel : Model, myPathPropID : Ident, myCLLoaders : java.util.List[ClassLoader]) 
		extends ClassLoaderUrlResolver with ModelBackedPathResolver {
	
	override 	protected def getPathModel() : Model = myPathModel
	override	protected def getPathPropertyID() : Ident = myPathPropID
	override	protected def getClassLoaders()  : java.util.List[ClassLoader] = myCLLoaders
}
*/
trait MediaHandleCache[MediaType] {
	val		myHandlesByID = new scala.collection.mutable.HashMap[Ident, MediaHandle[MediaType]]()
	
	protected def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType]
	def findMediaHandle(mediaID : Ident) : MediaHandle[MediaType] = {
		myHandlesByID.getOrElseUpdate(mediaID, {makeMediaHandle(mediaID)})
	}
}

// abstract class FancyMediaHandleCache[MediaType](pathModel : Model, pathPropID : Ident, clLoaders : java.util.List[ClassLoader]) 
abstract class FancyMediaHandleCache[MediaType](private val myPathFinder : MediaPathFinder, 
		private val myUrlSearcher : UrlSearcher) extends MediaHandleCache[MediaType]   {
			
	//protected def getMediaPathFinder() = myPathFinder
	// protected def getUrlSearcher() = myUrlSearcher

	override def makeMediaHandle(mediaID : Ident) : MediaHandle[MediaType]	= {
		makeFancyUrlMediaHandle(mediaID, myPathFinder, myUrlSearcher)
	}
	
	// Override this factory method for particular MediaHandle/MediaType subtypes.
	protected def makeFancyUrlMediaHandle(mediaID : Ident, pathFinder : MediaPathFinder, 
		urlSearcher : UrlSearcher) : FancyUrlMediaHandle[MediaType]
	
}

object MediaResolverFactory {
	def  makeFancyFileSpecMediaPathFinder (fancyFileSpecs : Traversable[FancyFile]) : MediaPathFinder = {
		val ffResolver = new FancyFileSpecMediaPathFinder()
		ffResolver.absorbFancyFileSpecs(fancyFileSpecs);
		ffResolver
	}
	def makeClasspathUrlSearcher(classLoaders : Seq[ClassLoader]) :  UrlSearcher =  {
		new ClasspathUrlSearcher(classLoaders)
	}
	def makeClasspathUrlSearcher(classLoaders : java.util.List[ClassLoader]) :  UrlSearcher =  {
		import scala.collection.JavaConversions._
		val clSeq : Seq[ClassLoader] = classLoaders;
		makeClasspathUrlSearcher(clSeq)
	}
}
