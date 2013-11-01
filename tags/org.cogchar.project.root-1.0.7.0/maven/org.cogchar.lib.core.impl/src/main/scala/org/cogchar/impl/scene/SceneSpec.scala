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

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.core.log.{BasicDebugger};

import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.{Assembler, Mode}

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.name.behavior.{SceneFieldNames}
import org.cogchar.api.channel.{Channel, BasicChannel}
import org.cogchar.impl.channel.{FancyChannelSpec};
import org.cogchar.api.perform.{PerfChannel, Media, Performance};

import org.cogchar.impl.perform.{FancyTime, PerfChannelNames};

import org.cogchar.api.scene.{Scene};

import scala.collection.mutable.HashMap;

/**
 * @author Stu B. <www.texpedient.com>
 */

class SceneSpec () extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	var		myTrigName : Option[String] = None;
	val		myBehaviorSpecs = new HashMap[Ident,BehaviorSpec]();
	val		myChannelSpecs  = new HashMap[Ident,FancyChannelSpec]();

	
	def addBehaviorSpec(bs: BehaviorSpec) {
		myBehaviorSpecs.put(bs.getIdent(), bs);
	}
	def addChannelSpec(cs: FancyChannelSpec) {
		myChannelSpecs.put(cs.getIdent(), cs);
	}
	// The field summary is used only for logging
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails + ", behaviors=" + myBehaviorSpecs + ", channels=" + 
				myChannelSpecs;
	}
	def getChannelIdents() : scala.collection.Set[Ident] = {
		myChannelSpecs.keySet;
	} 
	def getChannelUriStrings () : scala.collection.Set[String] = {
		getChannelIdents.map({_.getAbsUriString()})
	}
	import collection.JavaConversions._
	def getChannelUriStringsJList() : java.util.List[String] = {
		getChannelUriStrings.toList
	}
	/*
		: java.util.List[String] { 
		for (val cs <- scn.mySceneSpec.myChannelSpecs.values) {
			val csJRI = cs.getIdent().asInstanceOf[JenaResourceItem];
		}
	*/	

}

class SceneSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[SceneSpec](builderConfRes) {
	import scala.collection.JavaConversions._;
	
	override protected def initExtendedFieldsAndLinks(ss: SceneSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		getLogger.debug("SceneBuilder.initExtendedFieldsAndLinks");	
		ss.myDetails = "ChockFilledUp";
		
		val reader = getReader();

		val optTrigName = reader.readConfigValString(configItem.getIdent(), SceneFieldNames.P_trigger, configItem, null);
		
		ss.myTrigName = if (optTrigName != null) Some(optTrigName) else None;
		
		val linkedBehaviorSpecs : java.util.List[Object] = reader.findOrMakeLinkedObjects(configItem, SceneFieldNames.P_behavior, assmblr, mode, null);
		for (o <- linkedBehaviorSpecs) {
			o match {
				case bs: BehaviorSpec => ss.addBehaviorSpec(bs);
				case _ => getLogger.warn("Unexpected object found at {} = {}", Array[Object]( SceneFieldNames.P_behavior, o));
			}
		}
		val linkedChannelSpecs : java.util.List[Object] = reader.findOrMakeLinkedObjects(configItem, SceneFieldNames.P_channel, assmblr, mode, null);
		for (o <- linkedChannelSpecs) {
			o match {
				case cs: FancyChannelSpec => ss.addChannelSpec(cs);
				case _ => getLogger.warn("Unexpected object found at {} = {}", Array[Object]( SceneFieldNames.P_channel, o));
			}
		}		
		getLogger.debug("Scene found linkedBehaviorSpecs: {}",  linkedBehaviorSpecs)
		getLogger.debug("Scene found linkedChannelSpecs: {} ",  linkedChannelSpecs)

	}
}
