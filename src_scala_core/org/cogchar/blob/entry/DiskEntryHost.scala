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
 
 Regular filesystem implementation of EntryHost stuff.   
 We are interested only in *reading* this filesystem.
 */
class DiskEntry(someFile : File) extends Entry {
	// Often it is file: form of URL, but not always.
	override def getJavaURI : java.net.URI = someFile.toURI 
	
	// Methods supported by java.io.File
	// --------------------------------
	// length : long, lastModified : long
	// toURI : URI      Constructs a file: URI that represents this abstract pathname.
	// isAbsolute() -- Tests whether this abstract pathname is absolute.
	// File	getCanonicalFile()- 	Returns the canonical form of this abstract pathname.
	// String	getCanonicalPath() Returns the canonical pathname string of this abstract pathname.
	// String	getName()  -- Returns the name of the file or directory denoted by this abstract pathname.
	// Path		toPath() -- Returns a java.nio.file.Path object constructed from the this abstract path.
	// String	toString()  -- Returns the pathname string of this abstract pathname.	
	
}
// Being able to read from a folder-tree of files on a disk is an important app-developer's feature.
// We also think this impl *might* work with some Resource folder structures in JDK7.  
// See comments in ResourceEntryHost.scala.
class DiskFolderEntry (diskFolder : File) extends DiskEntry(diskFolder) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = ???
	override def findDirectSubFolders: Traversable[FolderEntry] = ???
	// Basic search forms are implemented in base trait.	
}
class DiskPlainEntry(diskFile : File) extends DiskEntry(diskFile) with PlainEntry {
	// We *could* offer size information, mod-stamp, and other.
}

class DiskEntryHost() extends EntryHost { 
	override def findFolderEntry(path : String) : Option[FolderEntry] = ???
	override def findPlainEntry(path : String) : Option[PlainEntry] = ???

	// Below are some working methods written before EntryHost absraction was defined.
	// Refactor and use these guts to implement the DiskFolderEntry above.
	@Deprecated def findReadablePlainFilesInFolder(folder : File) : Set[File] = {
		if(folder.exists && folder.isDirectory && folder.canRead) {
			val allFiles : Array[File] = folder.listFiles
			val readablePlainFiles : Array[File] = allFiles.filter(p => { p.isFile && p.canRead })	
			readablePlainFiles.toSet
		} else {
			Set[File]()
		}
	}
	@Deprecated def findReadableSubFoldersInFolder(folder : File) : Set[File] = {
		if(folder.exists && folder.isDirectory && folder.canRead) {
			val allFiles : Array[File] = folder.listFiles
			val readableFolders : Array[File] = allFiles.filter(p => { p.isDirectory && p.canRead })
			readableFolders.toSet
		} else {
			Set[File]()
		}
	}
	
	// As of 2015-03-07, these 3 methods below are already supplanted by what's in the FolderEntry base trait.
	// So we can delete these when DiskEntryHost is complete and tested.
	@Deprecated def deepSearchMatchingReadablePlainFiles(folder : File, filt : Function1[File, Boolean]) : Set[File] = {
		val matchingPlainFilesHere : Set[File] = findReadablePlainFilesInFolder(folder).filter(filt)
		val subFolders : Set[File] = findReadableSubFoldersInFolder(folder)
		val matchingSubFiles : Set[File] = subFolders.flatMap(deepSearchMatchingReadablePlainFiles(_, filt))
		matchingPlainFilesHere ++ matchingSubFiles
	}
	@Deprecated private def firstMatchingSuffix(f : File, suffixes : Seq[String]) : Option[String] = {
		val fileName = f.getName
		suffixes.find(fileName.endsWith(_))
	}
	@Deprecated def deepSearchReadablePlainFilesWithSuffixes(folder : File, suffixes : Set[String]) : Set[File] =  {
		val suffixSeq = suffixes.toSeq
		val filterFunc  = new Function1[File, Boolean] {
			def apply(f : File) : Boolean = firstMatchingSuffix(f, suffixSeq).isDefined
		}
		deepSearchMatchingReadablePlainFiles(folder, filterFunc)
	}
	
}
