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


import org.appdapter.core.item.Item;


import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class NuggetBuilder extends DynamicCachingComponentAssembler<Nugget> {
	/**
	 *  com.hp.hpl.jena.assembler.AssemblerHelp.runAnyAssemblerConstructor() looks for a constructor of this form (then falls back to no-args)
	 * @param builderConfRes 
	 */
	public NuggetBuilder (Resource builderConfRes) {
		super(builderConfRes);
	}
			
			
	@Override protected void initExtendedFieldsAndLinks(Nugget nug, Item configItem, Assembler assmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();
		logDebug("NuggetBuilder.initExtendedFieldsAndLinks");
		nug.myDetails = reader.readConfigValString(configItem.getIdent(), AssemblyTestNames.P_details, configItem, null);
		if (nug instanceof MegaNugget) {	
			MegaNugget mn = (MegaNugget) nug;
			mn.myGaucho = reader.readConfigValString(configItem.getIdent(), AssemblyTestNames.P_gaucho, configItem, null);
			mn.myCount = reader.readConfigValLong(configItem.getIdent(), AssemblyTestNames.P_count, configItem, null);
			mn.myAngle = reader.readConfigValDouble(configItem.getIdent(), AssemblyTestNames.P_angle, configItem, null);
			mn.myOtherNugs = reader.findOrMakeLinkedObjSeq(configItem, AssemblyTestNames.P_otherNugs, assmblr, mode);			
			mn.myTriggers = reader.findOrMakeLinkedObjects(configItem, AssemblyTestNames.P_trigger, assmblr, mode, null);
			List<Object> friendlyNugs = reader.findOrMakeLinkedObjects(configItem, AssemblyTestNames.P_friendlyNug, assmblr, mode, null);
			if (friendlyNugs.size() == 1) { 
				Object fn = friendlyNugs.get(0);
				mn.myFriendlyNug = (Nugget) fn;
				
			}
			// List<Item> otherNugs 
		}
	}
	//	List<Object> linkedTriggers = findOrMakeLinkedObjects(configItem, AssemblyNames.P_trigger, asmblr, mode, null);
	//	for (Object lt : linkedTriggers) {
	//		Trigger t = (Trigger) lt;
	//		box.attachTrigger(t);
	//	}

	
	
}
