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

package org.cogchar.blob.entry

import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.util.ArrayList
import org.appdapter.fancy.log.VarargsLogging
import collection.JavaConversions._
/**
 
Java 7 NIO URL based entries.
 */
class OSGIEntry(bundle : org.osgi.framework.Bundle, someURL : URL) extends Entry with VarargsLogging {
	// Often it is file: form of URL, but not always.
	override def getJavaURI : java.net.URI = someURL.toURI 
	
	 
}

class OSGIFolderEntry (bundle : org.osgi.framework.Bundle, osgiURL : URL) extends OSGIEntry(bundle, osgiURL) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		readableSubURLs.filter(!isDirectory(_)).map(new OSGIPlainEntry(bundle, _))
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		readableSubURLs.filter(isDirectory(_)).map(new OSGIFolderEntry(bundle, _))
	}
        
  def isDirectory(url: URL): Boolean = {
    var directory = true
    try {
      val conn = url.openConnection()
      val is = conn.getInputStream
      // Returns how many bytes can be read, if it isn't 0, then it is a directory
      if (is.available() != 0) {
         directory = false
      }
    } catch {
      case e: Exception => 
    }
     directory
  }
  
  // I don't know how to tell if a URL is readable...
    def isReadable(url: URL): Boolean = {
    true
  }
  
    def exists(pathURL: URL): Boolean = {
    bundle.getResource(pathURL.getPath) != null
  }
  

    
	private def readableSubURLs : Array[URL]	= {
		if(isDirectory(osgiURL) && isReadable(osgiURL)) {
                    // Gets all of the files and directories inside of a directory. 
                    val entryPaths = bundle.getEntryPaths(osgiURL.getPath)
    
                    
                    // Convert to an ArrayList before converting to an Array as
                    // there is no obvious way of converting directly to an Array
                    val paths = new ArrayList[URL]()
                    while (entryPaths.hasMoreElements()) {
                        paths.add(bundle.getResource(entryPaths.nextElement()))
                    }
                    
                    var pathsAsArray = Array() : Array[URL]
                    pathsAsArray = paths.toArray(pathsAsArray)
                    pathsAsArray.filter(isReadable(_))
		}
		else {
			error1("osgiFolder {} is not readable as a directory", osgiURL)
			Array()
		}
	}

}
class OSGIPlainEntry(bundle : org.osgi.framework.Bundle, osgiURL : URL) extends OSGIEntry(bundle, osgiURL) with PlainEntry {
	// We *could* offer size information, mod-stamp, and other.
        // Through using: 
        // java.nio.file.Files.size(path) The size in bytes, if in a jar the size will be of the compressed file.
        // java.nio.file.Files.getLastModifiedTime(path)
        // and similar methods.
}

// if rootURLOpt is supplied, it will be prepended to all paths we are queried on.
class OSGIEntryHost(bundle : org.osgi.framework.Bundle, rootURLOpt : Option[String]) extends EntryHost {
	private def resolveURL(partialURL : String) = {
		if (rootURLOpt.isDefined) {
			rootURLOpt.get + partialURL
		} else partialURL
	}
	private def findReadableURL(partialURL : String) : Option[URL] = {
		val resolvedURL = resolveURL(partialURL)
		val path = new URL(resolvedURL)
		if (exists(path) && isReadable(path)) Some(path) else None
	}
	override def findFolderEntry(path : String) : Option[FolderEntry] = {
		findReadableURL(path).filter(isDirectory(_)).map(new OSGIFolderEntry(bundle, _))
	}
	override def findPlainEntry(path : String) : Option[PlainEntry] = {
		findReadableURL(path).filter(isRegularFile(_)).map(new OSGIPlainEntry(bundle, _))
	}
       
  def isDirectory(url: URL): Boolean = {
    var directory = true
    try {
      val conn = url.openConnection()
      val is = conn.getInputStream
      // Returns how many bytes can be read, if it isn't 0, then it is a directory
      if (is.available() != 0) {
         directory = false
      }
    } catch {
      case e: Exception => 
    }
     directory
  }
    
  // I don't know how to tell if a URL is readable...
  def isReadable(url: URL): Boolean = {
    true
  }
  
    def exists(pathURL: URL): Boolean = {
    bundle.getResource(pathURL.getPath) != null
  }
  
    def isRegularFile(pathURL: URL): Boolean = {
    !isDirectory(pathURL)
  }
}