/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.scalatest
import org.cogchar.gen.oname.{BehavChanAct_owl2, HominoidBodySchema_owl2, AnimMotivMapBlend_owl2, WebTier_owl2}
import org.cogchar.name.web.WebUserActionNames
/**
 * @author Stu B. <www.texpedient.com>
 */

object OntoTest {
	def main(args: Array[String]): Unit = {
		println(this.toString() + " says 'Hello!'");
		
		println("BCA-ANIM_FRAME_MEDIA=" + BehavChanAct_owl2.ANIM_FRAME_MEDIA)
		
		println ("WebTier_owl2.SENDER.localName: " + WebTier_owl2.SENDER.getLocalName())
		
		println ("ID from ontoName: " + WebUserActionNames.idFromOntoName)
		
		// Let's load some of our favorite Cogchar ontologies from the runtime classpath,
		// using the modern Jena file-finding API (which supercedes old "FileManager"),
		// and then query them using some of the constants from our Schemagen .java files.
		// 
		//    https://jena.apache.org/documentation/io/rdf-input.html
		//    https://jena.apache.org/documentation/javadoc/arq/org/apache/jena/riot/RDFDataMgr.html
		// 
		import org.apache.jena.riot.RDFDataMgr;
		// Since we are in a main() unit test, we can ignore any complexity of bundle-classpath.
		
		val mcPath = "org/cogchar/onto/WebTier_owl2.ttl"
		val m = RDFDataMgr.loadModel(mcPath) ;
		println("Read model from classpath at [" + mcPath + "] = " + m)
		
		// When we use the "include-source" option during schemagen build step of o.c.lib.onto, 
		// the oname classes contain their own embedded copy of their own source models, which is loaded
		// by the classloader.  That is good and bad.   It's pretty cool if it always works perfectly,
		// and doesn't consume too many resources.  
		// 
		// Currently we have include-source turned on for all our
	}
	
	def testOntoLookupUnderOSGi() { 
		// Before lastest StreamManager in Jena 2.10, we used some clumsy classloader-plugin approaches.
		// But now, in the age of awesome, we can ...
		// 
	}
}
