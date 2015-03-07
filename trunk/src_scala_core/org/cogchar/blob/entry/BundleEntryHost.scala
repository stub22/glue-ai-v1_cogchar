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

class BundleEntryHost(bundle : org.osgi.framework.Bundle) extends EntryHost { 
	// Using Bundle's nice method: findEntries(java.lang.String path, java.lang.String filePattern, boolean recurse) 
	protected def getMatchingEntryURLs(path: String, filePattern : String, recurse : Boolean) : Set[java.net.URL] = {
		// See extensive docs of exactly what this does, here:
		// https://osgi.org/javadoc/r4v43/core/org/osgi/framework/Bundle.html#findEntries(java.lang.String, java.lang.String, boolean)
		val urlEnum : java.util.Enumeration[java.net.URL] = bundle.findEntries(path, filePattern, recurse)
		import scala.collection.JavaConverters._
		urlEnum.asScala.toSet
	}
	override def findFolderEntry(path : String) : Option[FolderEntry] = ???
	override def findPlainEntry(path : String) : Option[PlainEntry] = ???
}
class BundleEntry(locUri : java.net.URI) extends Entry {
	override def getJavaURI : java.net.URI = locUri
}
class BundleFolderEntry(host : BundleEntryHost, locUri : java.net.URI) extends BundleEntry (locUri) with FolderEntry {
	def findDirectPlainEntries: Traversable[PlainEntry] = ???
	def findDirectSubFolders: Traversable[FolderEntry] = ???	
	// BundleFolderEntry could supply its own search impls based on the getMatchingEntryURLs method above
	// 
	// Note these 2 different function-arg type syntaxes are equivalent
	// override def searchDirectPlainEntries(filt: PlainEntry => Boolean): Set[PlainEntry] = ??? // recurse = false
	// override def searchDeepPlainEntries(filt: Function1[PlainEntry,Boolean]): Set[PlainEntry] = ???  // recurse = true
	
	
}
class BundlePlainEntry(locUri : java.net.URI) extends BundleEntry(locUri) with PlainEntry {
}