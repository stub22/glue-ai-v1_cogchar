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

import org.appdapter.fancy.log.VarargsLogging

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
// "Plain" => not a sub-folder.
// Entries within a folder are "direct", while recursive sub-folder contents are "deep" (and dangerous!). 
// Our two primary verbs are defined to be:
// 
// "find" -> could be filtered or unfiltered, direct or deep.   Often (so far) naive about result sizes.
// "search" -> filtered in some way (predicates, suffixes), often deep, includes result-size limits. 
//  
//  We expect all result entries to reflect readable files/resources/folders.  
// If a file/resource/folder exists but is not accessible, it shouldn't be returned.

trait Entry extends VarargsLogging {
	def getJavaURI : java.net.URI 
	def getJavaURL : java.net.URL = getJavaURI.toURL  // See Java platform comments at bottom.
}

trait PlainEntry extends Entry {
	// So far is just a marker type.
}
// Here we have an abstract Folder for *read* only.  
trait FolderEntry extends Entry {
	// We return weak collections in form of Traversable, which *could* be lazy in an advanced implementation.
	// http://www.scala-lang.org/api/2.10.3/index.html#scala.collection.Traversable
	
	def findDirectPlainEntries : Traversable[PlainEntry]
	def findDirectSubFolders : Traversable[FolderEntry]
	// Q:  Do we want findDeepSubFolders?  A: Yes, but only in size-limited form.
	
	// Beware - naive unqualified deep search!   If you call this on a big tree, it's gonna hurt, yo.
	@Deprecated protected def findDeepPlainEntries :  Traversable[PlainEntry] = {
		findDirectPlainEntries ++ findDirectSubFolders.flatMap(_.findDeepPlainEntries)
	}
	// Search direct with arbitrary predicate filter.
	def searchDirectPlainEntries(filt : Function1[PlainEntry, Boolean]) : Set[PlainEntry] = {
		findDirectPlainEntries.filter(filt).toSet
	}
	// Beware:  We have a maxResultcount but it's implementation is a weak approximation.
	def searchDeepPlainEntries(filt : Function1[PlainEntry, Boolean], maxResultCount : Int) : Set[PlainEntry] = {
		val matchedDirectPEnts : Set[PlainEntry] = searchDirectPlainEntries(filt)
		val subFolders : Traversable[FolderEntry] = findDirectSubFolders
		// TODO: adjust this value properly at each step through the loop; as-is we may overshoot.
		val subResultMax = maxResultCount - matchedDirectPEnts.size
		val matchedDeepPEnts : Set[PlainEntry] = subFolders.flatMap(_.searchDeepPlainEntries(filt, subResultMax)).toSet
		(matchedDirectPEnts ++ matchedDeepPEnts).take(maxResultCount)
	}
	private def firstMatchingSuffix(pe : PlainEntry, suffixes : Seq[String]) : Option[String] = {
		val locUriTxt : String  = pe.getJavaURI.toString
		suffixes.find(locUriTxt.endsWith(_))
	}
		
	def searchDeepPlainEntriesBySuffix(suffixes : Set[String], maxResultCount : Int) : Set[PlainEntry] =  {
		val suffixSeq = suffixes.toSeq
		// Here is one of the longer, clearer ways of writing out what a Scala "function" is doing.
		// This can also be written in 99-Zillion forms of shorthand.
		val filterFunc  = new Function1[PlainEntry, Boolean] {
			def apply(pe : PlainEntry) : Boolean = firstMatchingSuffix(pe, suffixSeq).isDefined
		}
		searchDeepPlainEntries(filterFunc, maxResultCount)
	}


}
// EntryHost is a mechanism for finding physical paths to be indexed, and not a tool for 
// searching data contents, nor for writing data.

// EntryHost is a gateway into a bundle, a read-only filesystem, a classpath-space, or similar fast local data resource.
// Handles for these are typically registered with some EntryHostFinder.
// 
// It would *not* make much sense to add caching or indexing directly to these "Entry" mechanisms.
// Such features are managed in higher level code, outside of this package.
// 
// If we were going to read a remote web folder, then it should already supply an index graph for us.  
// We should not need to scan it with this kind of folder-iteration code, and we note that if we did, it would be slow.
// So we choose to *not* do that through this API.  Fast local resources only!
// 
// Current implementing types are:
// 
//		BundleEntryHost
//		DiskEntryHost
//		ResourceEntryHost

trait EntryHost extends VarargsLogging {

	// These input paths are relative to some implied "root" or "home" for each EntryHost.
	def findFolderEntry(path : String) : Option[FolderEntry]
	def findPlainEntry(path : String) : Option[PlainEntry]
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
