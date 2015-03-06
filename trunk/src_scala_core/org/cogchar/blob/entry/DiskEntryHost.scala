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
}
// Being able to read from a folder-tree of files on a disk is an important user feature.
// We also think this impl *might* work with some Resource folder structures in JDK7.  
// See comments in ResourceEntryHost.scala.
class DiskFolderEntry (diskFolder : File) extends DiskEntry(diskFolder) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = ???
	override def findDirectSubFolders: Traversable[FolderEntry] = ???
//	override def searchDirectPlainEntries(filt: Function1[PlainEntry,Boolean]): Set[PlainEntry] = ??? // recurse = false
//	override def searchDeepPlainEntries(filt: Function1[PlainEntry,Boolean]): Set[PlainEntry] = ???  // recurse = true
	
}
class DiskPlainEntry(diskFile : File) extends DiskEntry(diskFile) with PlainEntry {
	// We *could* offer size information, mod-stamp, and other.
}

class DiskEntryHost() extends EntryHost { 
	override def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry] = None
	override def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry] = None

	// Below are some working methods written before EntryHost absraction was defined.
	// Refactor and use these guts to implement the DiskFolderEntry above.
	def findReadablePlainFilesInFolder(folder : File) : Set[File] = {
		if(folder.exists && folder.isDirectory && folder.canRead) {
			val allFiles : Array[File] = folder.listFiles
			val readablePlainFiles : Array[File] = allFiles.filter(p => { p.isFile && p.canRead })	
			readablePlainFiles.toSet
		} else {
			Set[File]()
		}
	}
	def findReadableSubFoldersInFolder(folder : File) : Set[File] = {
		if(folder.exists && folder.isDirectory && folder.canRead) {
			val allFiles : Array[File] = folder.listFiles
			val readableFolders : Array[File] = allFiles.filter(p => { p.isDirectory && p.canRead })
			readableFolders.toSet
		} else {
			Set[File]()
			
		}
	}
	
	// TODO:  These methods below were written before the EntryHost concept was defined.
	// They should be reworked into abstractions that can work with any kind of FolderEntry (not just a File).
	def deepSearchMatchingReadablePlainFiles(folder : File, filt : Function1[File, Boolean]) : Set[File] = {
		val matchingPlainFilesHere : Set[File] = findReadablePlainFilesInFolder(folder).filter(filt)
		val subFolders : Set[File] = findReadableSubFoldersInFolder(folder)
		val matchingSubFiles : Set[File] = subFolders.flatMap(deepSearchMatchingReadablePlainFiles(_, filt))
		matchingPlainFilesHere ++ matchingSubFiles
	}
	private def firstMatchingSuffix(f : File, suffixes : Seq[String]) : Option[String] = {
		val fileName = f.getName
		suffixes.find(fileName.endsWith(_))
	}
	def deepSearchReadablePlainFilesWithSuffixes(folder : File, suffixes : Set[String]) : Set[File] =  {
		val suffixSeq = suffixes.toSeq
		val filterFunc  = new Function1[File, Boolean] {
			def apply(f : File) : Boolean = firstMatchingSuffix(f, suffixSeq).isDefined
		}
		deepSearchMatchingReadablePlainFiles(folder, filterFunc)
	}
	
}