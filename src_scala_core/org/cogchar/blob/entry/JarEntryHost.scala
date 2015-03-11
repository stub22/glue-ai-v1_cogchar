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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList
import org.appdapter.fancy.log.VarargsLogging
import collection.JavaConversions._
/**
 
 Regular filesystem implementation of EntryHost stuff.   
 We are interested only in *reading* this filesystem.
 */
class JarEntry(somePath : Path) extends Entry with VarargsLogging {
	// Often it is file: form of URL, but not always.
	override def getJavaURI : java.net.URI = somePath.toUri 
	
	 
}
// Being able to read from a folder-tree of files on a jar is an important app-developer's feature.
// We also think this impl *might* work with some Resource folder structures in JDK7.  
// See comments in ResourceEntryHost.scala.
class JarFolderEntry (jarFolder : Path) extends JarEntry(jarFolder) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		readableSubPaths.filter(!Files.isDirectory(_)).map(new JarPlainEntry(_))
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		readableSubPaths.filter(Files.isDirectory(_)).map(new JarFolderEntry(_))
	}

    
	private def readableSubPaths : Array[Path]	= {
		if(Files.isDirectory(jarFolder) && Files.isReadable(jarFolder)) {
                    // Gets all of the files and directories inside of a directory. 
                    val directoryPathsStream = Files.newDirectoryStream(jarFolder)
                    
                    // Convert to an ArrayList before converting to an Array as
                    // there is no obvious way of converting directly to an Array
                    val paths = new ArrayList[Path]()
                    for (path <- directoryPathsStream) {
                      paths.add(path)
                    }
                    
                    var pathsAsArray = Array() : Array[Path]
                    pathsAsArray = paths.toArray(pathsAsArray)
                    pathsAsArray.filter(Files.isReadable(_))
		}
		else {
			error1("jarFolder {} is not readable as a directory", jarFolder)
			Array()
		}
	}

}
class JarPlainEntry(jarPath : Path) extends JarEntry(jarPath) with PlainEntry {
	// We *could* offer size information, mod-stamp, and other.
        // Through using: 
        // java.nio.file.Files.size(path) The size in bytes, if in a jar the size will be of the compressed file.
        // java.nio.file.Files.getLastModifiedTime(path)
        // and similar methods.
}

// if rootPathOpt is supplied, it will be prepended to all paths we are queried on.
class JarEntryHost(rootPathOpt : Option[String]) extends EntryHost {
	private def resolvePath(partialPath : String) = {
		if (rootPathOpt.isDefined) {
			rootPathOpt.get + partialPath
		} else partialPath
	}
	private def findReadablePath(partialPath : String) : Option[Path] = {
		val resolvedPath = resolvePath(partialPath)
		val path = Paths.get(new URI(resolvedPath))
		if (Files.exists(path) && Files.isReadable(path)) Some(path) else None
	}
	override def findFolderEntry(path : String) : Option[FolderEntry] = {
		findReadablePath(path).filter(Files.isDirectory(_)).map(new JarFolderEntry(_))
	}
	override def findPlainEntry(path : String) : Option[PlainEntry] = {
		findReadablePath(path).filter(Files.isRegularFile(_)).map(new JarPlainEntry(_))
	}
/*
	// Below are some working methods written before EntryHost absraction was defined.
	// Refactor and use these guts to implement the JarFolderEntry above.
	// 
	// We return Set because there is no ordering assumed on the returned collection.
	// Regarding equality of members within this set, we note the following about the Path.equals() method:
	// http://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#equals(java.lang.Object)
	// "Tests this abstract pathname for equality with the given object. Returns true if and only if the argument is 
	// not null and is an abstract pathname that denotes the same file or directory as this abstract pathname. 
	// Whether or not two abstract pathnames are equal depends upon the underlying system. On UNIX systems, 
	// alphabetic case is significant in comparing pathnames; on Microsoft Windows systems it is not."	 
	@Deprecated private def findReadablePlainPathsInFolder(folder : Path) : Set[Path] = {
		if(Files.exists(folder) && Files.isDirectory(folder) && Files.isReadable(folder)) {
                    // Gets all of the files and directories inside of a directory. 
                    val directoryPathsStream = Files.newDirectoryStream(folder)
                    
                    // Convert to an ArrayList before converting to an Array as
                    // there is no obvious way of converting directly to an Array
                    val paths = new ArrayList[Path]()
                    for (path <- directoryPathsStream) {
                      paths.add(path)
                    }
                    
                    var pathsAsArray = Array() : Array[Path]
                    pathsAsArray = paths.toArray(pathsAsArray)
                    val readablePlainPaths : Array[Path] = pathsAsArray.filter(p => { Files.isRegularFile(p) && Files.isReadable(p) })	
                    readablePlainPaths.toSet
		} else {
			Set[Path]()
		}
	}
	@Deprecated private def findReadableSubFoldersInFolder(folder : Path) : Set[Path] = {
		if(Files.exists(folder) && Files.isDirectory(folder) && Files.isReadable(folder)) {
			// Gets all of the files and directories inside of a directory. 
                    val directoryPathsStream = Files.newDirectoryStream(folder)
                    
                    // Convert to an ArrayList before converting to an Array as
                    // there is no obvious way of converting directly to an Array
                    val paths = new ArrayList[Path]()
                    for (path <- directoryPathsStream) {
                      paths.add(path)
                    }
                    
                    var pathsAsArray = Array() : Array[Path]
                    pathsAsArray = paths.toArray(pathsAsArray)
			val readableFolders : Array[Path] = pathsAsArray.filter(p => { Files.isDirectory(p) && Files.isReadable(p)})
			readableFolders.toSet
		} else {
			Set[Path]()
		}
	}
	
	// As of 2015-03-07, these 3 methods below are already supplanted by what's in the FolderEntry base trait.
	// So we can delete these when JarEntryHost is complete and tested.
	@Deprecated private def deepSearchMatchingReadablePlainPaths(folder : Path, filt : Function1[Path, Boolean]) : Set[Path] = {
		val matchingPlainPathsHere : Set[Path] = findReadablePlainPathsInFolder(folder).filter(filt)
		val subFolders : Set[Path] = findReadableSubFoldersInFolder(folder)
		val matchingSubPaths : Set[Path] = subFolders.flatMap(deepSearchMatchingReadablePlainPaths(_, filt))
		matchingPlainPathsHere ++ matchingSubPaths
	}
	@Deprecated private def firstMatchingSuffix(path : Path, suffixes : Seq[String]) : Option[String] = {
		val fileName = path.getFileName
		suffixes.find(fileName.endsWith(_))
	}
	// This method is temporariliy being called from some test harnesses
	@Deprecated def deepSearchReadablePlainPathsWithSuffixes(folder : Path, suffixes : Set[String]) : Set[Path] =  {
		val suffixSeq = suffixes.toSeq
		val filterFunc  = new Function1[Path, Boolean] {
			def apply(f : Path) : Boolean = firstMatchingSuffix(f, suffixSeq).isDefined
		}
		deepSearchMatchingReadablePlainPaths(folder, filterFunc)
	}
*/
}
