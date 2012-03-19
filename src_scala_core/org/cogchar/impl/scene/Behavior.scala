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

package org.cogchar.impl.scene

import org.appdapter.core.item.{Ident, Item}

import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}


/**
 * @author Stu B. <www.texpedient.com>
 */

class Behavior () extends EmptyTimedModule[BehaviorModulator] {
}
//class BoorishBehavior(val itemSpecs : List[OffsetItemSpec]) extends Behavior() { 
//	var	nextItemIndex = 0;
// }

class BehaviorModulator() extends BasicModulator() {

	def runOneBatch() {
	
		processOneBatch();
	}
}
class BehaviorSpec() extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";

	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  "details=" + myDetails ;
	}
}
class BehaviorSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[BehaviorSpec](builderConfRes) {

	
	override protected def initExtendedFieldsAndLinks(bs: BehaviorSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		logInfo("BehaviorSpecBuilder.initExtendedFieldsAndLinks");	
		bs.myDetails = "brimmingOver";
		// Items are 
		
	}
}
