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

import java.io.File
/**
 *  Gingerly starting on a minimal abstract API for reading a "Scannable Folder", mainly to support a variety of
 *  alternative scenarios where we want to load a set of files from either the disk, our classpath, or a particular
 *  OSGi bundle.  (Each of these is a different implementing sub-type of EntryHost).
 *  Needs at deployed end-user runtime are different from app developer in an IDE, and in between we
 *  have various kinds of test-build to run with different datasets.
 *  Also important is that we want input relative paths to appear conveniently identical across the different
 *  kinds of hosts.
 *  
 */

// An EntryHost can find entries, which are known primarily by URI.
// Each entry is either PlainEntry or FolderEntry.
trait Entry {
	def getJavaURI : java.net.URI 
	def getJavaURL : java.net.URL = getJavaURI.toURL  // See Java platform comments at bottom.
}
// Plain => not a sub-folder
trait PlainEntry extends Entry {
	// So far is just a marker type.
}
// Here we have an abstract Folder for *read* only.  
trait FolderEntry extends Entry {
	// We return weak collections in form of Traversable.
	// Implied:  We accept results to be "readable".  If a file exists but is not accessible, it shouldn't be returned.
	// 
	// Entries within this folder are "direct", sub contents are "deep" (and dangerous!). 
	def findDirectPlainEntries : Traversable[PlainEntry]
	def findDirectSubFolders : Traversable[FolderEntry]
	// Q:  Do we want findDeepSubFolders?
	
	// Beware naive unqualified deep search!   
	protected def findDeepPlainEntries :  Traversable[PlainEntry] = {
		findDirectPlainEntries ++ findDirectSubFolders.flatMap(_.findDeepPlainEntries)
	}
	
	def searchDirectPlainEntries(filt : Function1[PlainEntry, Boolean]) : Set[PlainEntry] = {
		findDirectPlainEntries.filter(filt).toSet
	}
	// TODO: 
	def searchDeepPlainEntries(filt : Function1[PlainEntry, Boolean]) : Set[PlainEntry] = {
		val matchedDirectPEnts : Set[PlainEntry] = searchDirectPlainEntries(filt)
		val subFolders : Traversable[FolderEntry] = findDirectSubFolders
		val matchedDeepPEnts : Set[PlainEntry] = subFolders.flatMap(_.searchDeepPlainEntries(filt)).toSet
		matchedDirectPEnts ++ matchedDeepPEnts
	}
		

}

trait EntryHost {
	def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry]
	def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry]
}

/*
 *  *  
 *  We pay attention to the following methods available for producing/converting URIs and URIs:
 *  Note that java.net.URL is somewhat deprecated in Java 7, in favor of java.net.URI.
 * public URI java.net.URL.toURI()
          throws URISyntaxException
Returns a URI equivalent to this URL. This method functions in the same way as new URI (this.toString()).

public URL java.net.URI.toURL()
          throws MalformedURLException
Constructs a URL from this URI.
This convenience method works as if invoking it were equivalent to evaluating the expression new URL(this.toString()) 
after first checking that this URI is absolute. * 

 public URI java.io.file.toURI()
"An absolute, hierarchical URI with a scheme equal to "file", a path representing this abstract pathname, ...
If it can be determined that the file denoted by this abstract pathname is a directory, then the resulting URI 
will end with a slash."
 
 * URL	java.io.File.toURL()
Deprecated. 
This method does not automatically escape characters that are illegal in URLs. It is recommended that new code 
convert an abstract pathname into a URL by first converting it into a URI, via the toURI method, and then converting 
the URI into a URL via the URI.toURL method.

 */