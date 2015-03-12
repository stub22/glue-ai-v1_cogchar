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
 
Java 7 NIO Path based entries.
 */
class JnioEntry(somePath : Path) extends Entry with VarargsLogging {
	// Often it is file: form of URL, but not always.
	override def getJavaURI : java.net.URI = somePath.toUri 
	
	 
}

class JnioFolderEntry (jnioFolder : Path) extends JnioEntry(jnioFolder) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		readableSubPaths.filter(!Files.isDirectory(_)).map(new JnioPlainEntry(_))
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		readableSubPaths.filter(Files.isDirectory(_)).map(new JnioFolderEntry(_))
	}

    
	private def readableSubPaths : Array[Path]	= {
		if(Files.isDirectory(jnioFolder) && Files.isReadable(jnioFolder)) {
                    // Gets all of the files and directories inside of a directory. 
                    val directoryPathsStream = Files.newDirectoryStream(jnioFolder)
                    
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
			error1("jnioFolder {} is not readable as a directory", jnioFolder)
			Array()
		}
	}

}
class JnioPlainEntry(jnioPath : Path) extends JnioEntry(jnioPath) with PlainEntry {
	// We *could* offer size information, mod-stamp, and other.
        // Through using: 
        // java.nio.file.Files.size(path) The size in bytes, if in a jar the size will be of the compressed file.
        // java.nio.file.Files.getLastModifiedTime(path)
        // and similar methods.
}

// if rootPathOpt is supplied, it will be prepended to all paths we are queried on.
class JnioEntryHost(rootPathOpt : Option[String]) extends EntryHost {
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
		findReadablePath(path).filter(Files.isDirectory(_)).map(new JnioFolderEntry(_))
	}
	override def findPlainEntry(path : String) : Option[PlainEntry] = {
		findReadablePath(path).filter(Files.isRegularFile(_)).map(new JnioPlainEntry(_))
	}
}
