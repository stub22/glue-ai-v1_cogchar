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

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;

import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.impl.perform.ChannelSpec;

import scala.collection.mutable.HashMap;
/**
 * @author Stu B. <www.texpedient.com>
 */

class SceneSpec () extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	val		myBehaviorSpecs = new HashMap[Ident,BehaviorSpec]();
	val		myChannelSpecs  = new HashMap[Ident,ChannelSpec]();

	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails;
	}
	def addBehaviorSpec(bs: BehaviorSpec) {
		myBehaviorSpecs.put(bs.getIdent(), bs);
	}
	def addChannelSpec(cs: ChannelSpec) {
		myChannelSpecs.put(cs.getIdent(), cs);
	}
	
}
class SceneSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[SceneSpec](builderConfRes) {

	
	override protected def initExtendedFieldsAndLinks(ss: SceneSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		logInfo("SceneBuilder.initExtendedFieldsAndLinks");	
		ss.myDetails = "ChockFilledUp";
		val linkedBehaviorSpecs : java.util.List[Object] = findOrMakeLinkedObjects(configItem, SceneFieldNames.P_behavior, assmblr, mode, null);
		logInfo("Scene found linkedBehaviorSpecs: " + linkedBehaviorSpecs)
	}
}
object SceneFieldNames extends org.appdapter.gui.assembly.AssemblyNames {
	val		NS_ccScn =	"http://www.cogchar.org/schema/scene#";
	val		NS_ccScnInst = "http://www.cogchar.org/schema/scene/instance#";

	val		P_behavior	= NS_ccScn + "behavior";
}

