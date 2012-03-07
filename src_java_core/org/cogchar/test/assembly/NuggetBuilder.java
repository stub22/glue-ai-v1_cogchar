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

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.Trigger;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.List;

import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class NuggetBuilder extends DynamicCachingComponentAssembler<Nugget> {
	private void logInfo(String txt) {
		System.out.println(txt);
	}
	@Override protected void initExtendedFieldsAndLinks(Nugget nug, Item configItem, Assembler asmblr, Mode mode) {
		logInfo("NuggetBuilder.initExtendedFieldsAndLinks");
	//	List<Object> linkedTriggers = findOrMakeLinkedObjects(configItem, AssemblyNames.P_trigger, asmblr, mode, null);
	//	for (Object lt : linkedTriggers) {
	//		Trigger t = (Trigger) lt;
	//		box.attachTrigger(t);
	//	}
	}	
}
