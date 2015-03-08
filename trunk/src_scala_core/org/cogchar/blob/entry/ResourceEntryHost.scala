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

// class ResourceEntry(refClz : java.lang.Class[_], resPath : String) {
class ResourceEntry(locUri : java.net.URI) extends Entry {
	override def getJavaURI : java.net.URI = locUri
}
class ResourcePlainEntry(locUri : java.net.URI) extends ResourceEntry(locUri) with PlainEntry {
}
class ResourceFolderEntry(locUri : java.net.URI) extends ResourceEntry(locUri) with FolderEntry {
	// Here we leverage the fact that JVM can (in theory) return us a java.io.File object for a 
	// classpath resource folder.  [Not clear on the limitations of this yet, though].
	// Thus we can create a delegate DiskFolderEntry and let it do all our work!
	lazy val myPseudoDFE : Option[DiskFolderEntry] = {
		var file_opt : Option[File] = None
		try {
			debug1("Making pseudo-file for locUri: {}", locUri)
			val file = new File(locUri) // Does this support subFolders? 
			file_opt = Option(file)
		} catch  {
			case t : Throwable => error2("Error instantiating java.io.File for uri: {}, exc: {}", locUri, t)
		}
		file_opt.map(new DiskFolderEntry(_))
	}
	
	
	override def findDirectPlainEntries: Traversable[PlainEntry] = {
		if (myPseudoDFE.isDefined) {
			// Will return the FolderPlainEntries - why not? 
			myPseudoDFE.get.findDirectPlainEntries  // But we could also:  .map(new ResourcePlainEntry(_.getJavaURI))
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

