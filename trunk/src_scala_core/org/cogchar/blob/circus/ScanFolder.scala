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

package org.cogchar.blob.circus

import java.io.File
/**
 *  Gingerly starting on some abstractions of the concept of "Scannable Folder", mainly to support a variety of
 *  alternative scenarios where we want to load a set of files from either the disk, our classpath, or a particular
 *  OSGi bundle.  (Each of these is termed a different kind of EntryHost).
 *  Needs at deployed end-user runtime are different from app developer in an IDE, and in between we
 *  have various kinds of test-build to run with different datasets.
 *  
 *  Also important is that we want input relative paths to appear conveniently identical across the different
 *  kinds of hosts.
 *  
 *  
 * public URI java.net.URL.toURI()
          throws URISyntaxException
Returns a URI equivalent to this URL. This method functions in the same way as new URI (this.toString()).

public URL java.net.URI.toURL()
          throws MalformedURLException
Constructs a URL from this URI.
This convenience method works as if invoking it were equivalent to evaluating the expression new URL(this.toString()) 
after first checking that this URI is absolute. * 

 * URL	java.io.File.toURL()
Deprecated. 
This method does not automatically escape characters that are illegal in URLs. It is recommended that new code 
convert an abstract pathname into a URL by first converting it into a URI, via the toURI method, and then converting 
the URI into a URL via the URI.toURL method.
 */
trait Entry {
	def getJavaURI : java.net.URI 
	def getJavaURL : java.net.URL = getJavaURI.toURL
}
trait PlainEntry extends Entry {
	
}
trait FolderEntry extends Entry {
	// We use weak collection form of Traversable.
	// Implied:  We accept results to be "readable".  If a file exists but is not accessible, it shouldn't be returned.
	// 
	// TODO:  Add filter arguments.
	// 
	def findDirectPlainEntries : Traversable[PlainEntry]
	def findDirectSubFolders : Traversable[FolderEntry]
	
	def findDeepPlainEntries :  Traversable[PlainEntry] = {
		findDirectPlainEntries ++ findDirectSubFolders.flatMap(_.findDeepPlainEntries)
	}
	// Do we want DeepSubFolders?
}

trait EntryHost {
	def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry]
	def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry]
}

class DiskEntry(someFile : File) extends Entry {
	// Often it is file: form of URL, but not always.
	override def getJavaURI : java.net.URI = someFile.toURI 
}
// Being able to read from a folder-tree of files on a disk is an important user feature.
class DiskFolderEntry (diskFolder : File) extends DiskEntry(diskFolder) with FolderEntry {
	override def findDirectPlainEntries: Traversable[PlainEntry] = ???
	override def findDirectSubFolders: Traversable[FolderEntry] = ???
}
class DiskPlainEntry(diskFile : File) extends DiskEntry(diskFile) with PlainEntry {
	
}

class DiskFileSystem() extends EntryHost { 
	override def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry] = None
	override def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry] = None
}

// *******************************************************************
// Second important case is when we want to scan through all files in a folder in some OSGi bundle.

class BundleEntryHost(bundle : org.osgi.framework.Bundle) extends EntryHost { 
	// findEntries(java.lang.String path, java.lang.String filePattern, boolean recurse) 
	protected def getMatchingEntryURLs(path: String, filePattern : String, recurse : Boolean) : Set[java.net.URL] = {
		// See extensive docs of exactly what this does, here:
		// https://osgi.org/javadoc/r4v43/core/org/osgi/framework/Bundle.html#findEntries(java.lang.String, java.lang.String, boolean)
		val urlEnum : java.util.Enumeration[java.net.URL] = bundle.findEntries(path, filePattern, recurse)
		import scala.collection.JavaConverters._
		urlEnum.asScala.toSet
	}
	override def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry] = None
	override def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry] = None
}
class BundleEntry(locUri : java.net.URI) extends Entry {
	override def getJavaURI : java.net.URI = locUri
}
class BundleFolderEntry(host : BundleEntryHost, locUri : java.net.URI) extends BundleEntry (locUri) with FolderEntry {
	def findDirectPlainEntries: Traversable[org.cogchar.blob.circus.PlainEntry] = ???
	def findDirectSubFolders: Traversable[org.cogchar.blob.circus.FolderEntry] = ???	
}
class BundlePlainEntry(locUri : java.net.URI) extends BundleEntry(locUri) with PlainEntry {
	
}

// class ResourceEntry(refClz : java.lang.Class[_], resPath : String) {
	
class ResourceFolderEntry(locUri : java.net.URI) extends FolderEntry {
	
	lazy val myPseudoDFE : Option[DiskFolderEntry] = {
		var file_opt : Option[File] = None
		try {
			val file = new File(locUri) // Does this support subFolders? 
			file_opt = Option(file)
		}
		file_opt.map(new DiskFolderEntry(_))
	}
	override def getJavaURI : java.net.URI = locUri
	
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		if (myPseudoDFE.isDefined) {
			myPseudoDFE.get.findDirectPlainEntries
		} else {
			Set()
		}
	}
	override def findDirectSubFolders: Traversable[FolderEntry] = {
		if (myPseudoDFE.isDefined) {
			// But does this work?   Does the "File" know about its sub-folders?
			myPseudoDFE.get.findDirectSubFolders
		} else {
			Set()
		}
	}
}
class ResourceEntryHost(refClz : java.lang.Class[_]) extends EntryHost {
	// refClz : java.lang.Class[_], resPath : String
	
	def folderEntryForRelPath(path : String) : Option[FolderEntry] = {
		val url_opt : Option[java.net.URL] = Option(refClz.getResource(path));
		url_opt.map(url => new ResourceFolderEntry(url.toURI))
	}
	override def findFolderEntry(uriLoc : java.net.URI) : Option[FolderEntry] = {
		// lazy val myUri_opt : Option[java.net.URI] = Option(refClz.getResource(resPath)).map(_.toURI)
		None
	}
	override def findPlainEntry(uriLoc : java.net.URI) : Option[PlainEntry] = None
}

// *********************************************************************************
// Third case is the messiest, historically:  We want to access some folder that we expect is on our classpath,
// but we are not in OSGi.  This is helpful when we are unit testing bits of software that want to read folders
// of resources in the current project, or some upstream project (e.g. some onto test-data project).
// 
// This discussion offers some alternatives:
// http://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
// http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
// 
// "If you pass in a directory to the getResourceAsStream method then it will return a listing of files in the 
// directory ( or at least a stream of it)." - on all platforms?

// 
// Variations on:
// URL url = MyClass.class.getResource("/com/abc/package/resources/");
//     final URL url = Launcher.class.getResource("/" + path);
//     if (url != null) {
//        final File dir = new File(url.toURI());
//			for (File nextFile : dir.listFiles()) {          
//			
//			
//	"...work when you run the application on IDE (not with jar file), You can remove it if you don't like that."
// 
// But in some cases (still under Java 7?  or all better now?) we might need something like:
// final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
// 
// if(jarFile.isFile()) {  // Run with JAR file
//    final JarFile jar = new JarFile(jarFile);
//    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
//    while(entries.hasMoreElements()) {
//        final String name = entries.nextElement().getName();
//        if (name.startsWith(path + "/")) { //filter according to the path
//            System.out.println(name);
//        }
//    }
//    jar.close();
// } else { // Run with IDE
// 
// "Note that in Java 7, you can create a FileSystem from the JAR (zip) file, and then use NIO's directory walking 
// and filtering mechanisms to search through it. This would make it easier to write code that handles JARs and 
// "exploded" directories."
// 
// 

/*
 *     URI uri = MyClass.class.getResource("/resources").toURI();
    Path myPath;
    if (uri.getScheme().equals("jar")) {
        FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        myPath = fileSystem.getPath("/resources");
    } else {
        myPath = Paths.get(uri);
    }
    Stream<Path> walk = Files.walk(myPath, 1);
    for (Iterator<Path> it = walk.iterator(); it.hasNext();){
        System.out.println(it.next());
    }
 */