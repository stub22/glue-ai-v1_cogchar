/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.test.assembly;

import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class AssemblyTest {
	
	public static void main(String args[]) {
/*
		// As noted in Appdapter DemoResources, we can use either the classloader or a file loader.
		// Hence the generic term "path"
//		1) Relative classloader path (with no leading "/") works is best for modular platform-independent deployment.
//	OR	2) Use a Jena-FM friendly URL like "file:/x/y/z/" - works if the classpath is frozen or hurtin, and you need 
		// a darn file!  Another example is when you want to reload the file easily at runtime, without
		// reloading the classLoader.
		// File load works OK with or without the "file:" prefix, to both relative and absolute file paths.
		// If you use a relative path, and leave off the "file:" prefix, THEN you have
		// a path string which can be satisfied by EITHER a classpath resource or a file.
		// Tricky, eh?
		
		// String triplesPath = "file:src/main/resources/org/cogchar/test/assembly/ca_test.ttl";
		// This will be interpreted as a classpath OR a relative filesystem path.
		// Which is checked first?  I don't know, that's up to Jena FileManager.  
		// Don't depend on that!
		 * 
		 * Network paths like http://www.xyz123.com/myFile.ttl 
		 * should work OK, too.
*/		
		String triplesPath = "org/cogchar/test/assembly/ca_test.ttl";
		
		Set<Object> loadedStuff = AssemblerUtils.buildObjSetFromPath (triplesPath, AssemblyTest.class.getClassLoader());
		logInfo("Loaded " + loadedStuff.size() + " objects");
		for (Object o : loadedStuff) {
			logInfo("Loaded: " + o);
		}
		logInfo("=====================================================================");
		
	}
	public static void logInfo(String txt) {
		System.out.println(txt);
	}
}
