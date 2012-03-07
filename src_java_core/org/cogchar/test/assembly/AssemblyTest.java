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
import org.appdapter.bind.rdf.jena.model.AssemblerUtils;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class AssemblyTest {
	
	public static void main(String args[]) {
		String triplesURL = "src/main/resources/org/cogchar/test/assembly/ca_test.ttl";
		AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(AssemblyTest.class.getClassLoader());
		logInfo("Loading triples from URL: " + triplesURL);
		Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
		logInfo("Loaded " + loadedStuff.size() + " objects");
		for (Object o : loadedStuff) {
			System.out.println("Loaded: " + o);
		}
	}
	public static void logInfo(String txt) {
		System.out.println(txt);
	}
}
