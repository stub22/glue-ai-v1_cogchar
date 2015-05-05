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

// *******************************************************************
// Second important case is when we want to scan through all files in a folder in some OSGi bundle.
/**
 * I am choosing to keep track of URLs because they will allow us to get the size of the files and last modified time of the files
 * Using java.nio.file.Path objects would require us to create quite a few FileSystem objects for each jar used in the felix cache.
 * Using java.io.File objects would fail because you cannot create a File object for a path inside of a jar.
 * You can get a java.io.File object by using bundle.getDataFile(path) but it doesn't have size or modified info for some reason...
 * Below is an example of how to get the size and last modified time out of a URL.
 * The last modified time will be the same for every item in the bundle, as the bundle was generated which counts as modifying a file...
 *          String filePath; // An actual file path
 *          URL fileURL = bundle.getEntry(filePath);
 *          long size = -1;
 *          long lastModifiedTime = -1;
 *          URLConnection conn = null;
 *          try {
 *              conn =  fileURL.openConnection(); 
 *              size = conn.getContentLength();
 *              // Do not use conn.getLastModifiedLong() the URLConnection type that is returned is specifically implemented
 *              // By apache and they did not properly implement the long version of this method... 
 *              lastModifiedTime = conn.getLastModified(); 
 *          } catch (IOException e) {
 *              System.out.println("Failed to open url");
 *          } 
 */

import java.net.URL

class BundleEntryHost(bundle : org.osgi.framework.Bundle) extends EntryHost { 
	// Using Bundle's nice method: findEntries(java.lang.String path, java.lang.String filePattern, boolean recurse) 
	protected def getMatchingEntryURLs(path: String, filePattern : String, recurse : Boolean) : Set[java.net.URL] = {
		// See extensive docs of exactly what this does, here:
		// https://osgi.org/javadoc/r4v43/core/org/osgi/framework/Bundle.html#findEntries(java.lang.String, java.lang.String, boolean)
		val urlEnum : java.util.Enumeration[java.net.URL] = bundle.findEntries(path, filePattern, recurse)
		import scala.collection.JavaConverters._
		urlEnum.asScala.toSet
	}
        
	override def findFolderEntry(path : String) : Option[FolderEntry] = {
		val resolvedURL_opt : Option[java.net.URL] = resolvePathToURL(path) 
		resolvedURL_opt.map(url => new BundleFolderEntry(bundle, url.toURI))

	}
	override def findPlainEntry(path : String) : Option[PlainEntry] = {
		val resolvedURL_opt : Option[java.net.URL] = resolvePathToURL(path) 
		resolvedURL_opt.map(url => new BundlePlainEntry(bundle, url.toURI))
	}
        
    private def resolvePathToURL(path: String) : Option[java.net.URL] = {
		val pathWithLeadingSlash : String = if (path.startsWith("/")) path else "/" + path
		val resolvedURL_opt = Option(bundle.getResource(pathWithLeadingSlash));
		if (resolvedURL_opt.isEmpty) {
			warn1("Could not resolve resource path: {}", pathWithLeadingSlash)
		}
		resolvedURL_opt
	}
}
class BundleEntry(bundle : org.osgi.framework.Bundle, locUri : java.net.URI) extends Entry {
	override def getJavaURI : java.net.URI = locUri
}
class BundleFolderEntry(bundle : org.osgi.framework.Bundle, locUri : java.net.URI) extends BundleEntry (bundle, locUri) with FolderEntry {
    
    
	lazy val myBundleDelegate: Option[OSGIFolderEntry] = {

        var path_opt: Option[URL] = None
        try {
            import scala.collection.JavaConversions._
            debug1("Making Java OSGI Bundle URL for locUri: {}", locUri)
            val path = locUri.toURL
            path_opt = Option(path)
                
        } catch {
            case t: Throwable => error2("Error instantiating java.nio.file.Path for uri: {}, exc: {}", locUri, t)
        }
        path_opt.map(new OSGIFolderEntry(bundle, _))
    }
	
	
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		if (myBundleDelegate.isDefined) {
	
			myBundleDelegate.get.findDirectPlainEntries  
		} else {
			Set()
		}
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		if (myBundleDelegate.isDefined) {
			myBundleDelegate.get.findDirectSubFolders
		} else {
			Set()
		}
	}	
	
	
	
}
class BundlePlainEntry(bundle : org.osgi.framework.Bundle, locUri : java.net.URI) extends BundleEntry(bundle, locUri) with PlainEntry {
}