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

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

class ResourceEntry(locUri : java.net.URI) extends Entry {
	override def getJavaURI : java.net.URI = locUri
}
class ResourcePlainEntry(locUri : java.net.URI) extends ResourceEntry(locUri) with PlainEntry {
}
class ResourceFolderEntry(locUri : java.net.URI) extends ResourceEntry(locUri) with FolderEntry {
	/*
         * The previous approach was attempting to use java.io.File objects but this does not allow
         * the user to search through jars for files. So instead the approach shown in this folder uses 
         * java.nio.file.Path objects which have all of the same functionality of File objects to my knowledge,
         * they will allow us to get their size and last modification time with the following methods. 
         * Files.size(path) The size in bytes, if in a jar the size will be of the compressed file.
         * Files.getLastModifiedTime(path)
         */

	lazy val myJnioDelegate: Option[JnioFolderEntry] = {

        var path_opt: Option[Path] = None
        try {
            
                import scala.collection.JavaConversions._
                debug1("Making Java NIO-path for locUri: {}", locUri)
                if(locUri.getScheme == "jar"){
					// Example debug output pointing to a folder inside a Jar in a local maven repo:
					// jar:file:/E:/mrepo_j7_m305/com/rkbots/milo/com.rkbots.milo.api.onto/1.1.0-SNAPSHOT/com.rkbots.milo.api.onto-1.1.0-SNAPSHOT.jar!/com/rkbots/milo/onto/

					/**
					 * Let's try to clarify this statement:
					 *
                     *		A path cannot contain "jar:" or "file:/" because of the colons.
					 *		
					 *	[What kind of "path" has this constraint?  A java.nio.file.Path?  Do we have a reference on that?]
					 *	
					 *  Most links to jars sent in here have
                     * "jar:file:/" appended to the beginning of their Uris so they must be removed.
                     * The path to the jar and the path inside the jar are separated by a "!" which is what I am using as a delimiter below.
					 * 
					 * [Was the code below copied from somewhere, or is this Ben's original solution? ]
                     */
                    val uriParts = locUri.toString.split("!")
                    val locJarPath = uriParts(0).replaceFirst("jar:", "").replaceFirst("file:/", "")
                    val directoryInJarPath = uriParts(1)
                    
                    // To read the contents of a jar you have to create a FileSystem object and use it to create paths inside the jar.
					// Three concerns here:
					//		A) It may be costly to call newFileSystem a lot.   
					//		B) Are we supposed to explicitly close these file systems at some point?
					//		C) Sometimes we may want a different classloader in order to resolve the path correctly.
					//		[This is a context-dependent question.  The classloader used here may not matter 
					//		too much in most cases where this code is being called, since we are probably outside
					//		OSGi in a main() program, and there is probably just one big classloader].
                    val jarFileSystem = FileSystems.newFileSystem(Paths.get(locJarPath), classOf[ResourceEntryHost].getClassLoader)
                    
                    // Create the path inside of the jar
                    val path = jarFileSystem.getPath(directoryInJarPath)
                    path_opt = Option(path)
                } else {
					// In this case the resource was probably resolved to a regular file: in our local project's "target/classes" directory.
                    val path = Paths.get(locUri)
                    path_opt = Option(path)
                }
        } catch {
            case t: Throwable => error2("Error instantiating java.nio.file.Path for uri: {}, exc: {}", locUri, t)
        }
        path_opt.map(new JnioFolderEntry(_))
    }
	
	
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		if (myJnioDelegate.isDefined) {
	
			myJnioDelegate.get.findDirectPlainEntries  
		} else {
			Set()
		}
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		if (myJnioDelegate.isDefined) {
			myJnioDelegate.get.findDirectSubFolders
		} else {
			Set()
		}
	}

}
class ResourceEntryHost(refClz : java.lang.Class[_]) extends EntryHost {

	private def resolvePathToURL(path: String) : Option[java.net.URL] = {
		val pathWithLeadingSlash : String = if (path.startsWith("/")) path else "/" + path
		val resolvedURL_opt = Option(refClz.getResource(pathWithLeadingSlash));
		if (resolvedURL_opt.isEmpty) {
			warn1("Could not resolve resource path: {}", pathWithLeadingSlash)
		}
		resolvedURL_opt
	}
	
	override def findFolderEntry(path : String) : Option[FolderEntry] = {
		val resolvedURL_opt : Option[java.net.URL] = resolvePathToURL(path) 
		resolvedURL_opt.map(url => new ResourceFolderEntry(url.toURI))

	}
	override def findPlainEntry(path : String) : Option[PlainEntry] = {
		val resolvedURL_opt : Option[java.net.URL] = resolvePathToURL(path) 
		resolvedURL_opt.map(url => new ResourcePlainEntry(url.toURI))
	}
}