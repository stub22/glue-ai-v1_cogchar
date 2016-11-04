/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.xml.convoid.behavior;

import org.dom4j.Document;


import com.thoughtworks.xstream.XStream;
import org.cogchar.api.convoid.act.Act;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;



/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class BehaviorDataTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Let's mouth off!");

//		StringWriter		sw = new StringWriter();
//		PrettyPrintWriter	ppw = new PrettyPrintWriter(sw, "\t");
		
		Category	parentCat = new Category("Papa");
		Category	childCat = new Category("Sonny");
		parentCat.addSubCategory(childCat);
		Act		act01 = new Act("tightrope");
		Step step01 = new Step();
		Step step02 = new Step();
		
		// Escapes the XML on output, if our Step.converter not deployed
		step01.setText("<ok>Toddler Step\n<tag>intag</tag>\nIs this dropped?</ok>");
		step01.setType("Awesome");
// The CDATA is eaten during conversion, then <tag> gets escaped - and that's OK.
		step02.setText("<yeah>Silly Step\n<![CDATA[ <tag>intag</tag>]]>\nHow's that?</yeah>");
		step02.setType("Bodacious");
		
		act01.addStep(step01);
		act01.addStep(step02);
		childCat.addAct(act01);
		
		// xstream.marshal(parentCat, ppw);
		
		try {
			// This preserves mixedContent (important) as well as CDATA (nice!).
			Document doc = BehaviorDataSaver.writeToDom4JDoc(parentCat);
			
			String parentCatCompactXML = doc.asXML();
			System.out.println("Compact XML for parentCat:[\n" + parentCatCompactXML + "\n]");
			
			String parentCatPrettyXML = BehaviorDataSaver.writeDocumentToString(doc);
			System.out.println("Pretty XML for parentCat:[\n" + parentCatPrettyXML + "\n]");
			
			XStream xstream = null;//BehaviorDataLoader.buildDom4jXStreamForRead();

			Category reconstitutedCompact = (Category) xstream.fromXML(parentCatCompactXML);
			System.out.println("Reconstituted from compact:\n" + reconstitutedCompact);
			Category reconstitutedPretty = (Category) xstream.fromXML(parentCatPrettyXML);
			System.out.println("Reconstituted from pretty:\n" + reconstitutedPretty);
			
			Document doc2 = BehaviorDataSaver.writeToDom4JDoc(reconstitutedPretty);
			String outPrettyAgain = BehaviorDataSaver.writeDocumentToString(doc2);
			System.out.println("Out pretty again:\n" + outPrettyAgain);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
