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

import org.appdapter.core.item.{Ident, Item, FreeIdent}
import org.appdapter.core.log.{BasicDebugger};

import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.{Assembler, Mode}

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.api.perform.{Channel};
import org.cogchar.impl.perform.{FancyChan, ChannelSpec, ChannelNames};

import org.cogchar.api.scene.{Scene, SceneBuilder};

import scala.collection.mutable.HashMap;
/**
 * @author Stu B. <www.texpedient.com>
 */

class BSceneChan (id : Ident, val scn: BScene) extends FancyChan(id)  {
}
class BScene(ss: SceneSpec) extends BasicDebugger with Scene[BSceneChan] {
	val rootyID = new FreeIdent(SceneFieldNames.I_rooty, SceneFieldNames.N_rooty);
	val myRootChan = new BSceneChan(rootyID, this);
	val		myWiredChannels  = new HashMap[Ident,Channel]();
	
	override def getRootChannel() : BSceneChan = {	myRootChan	}
	import scala.collection.JavaConversions._;
	override def wirePerformanceChannels(chans : java.util.Collection[Channel]) : Unit = {
		// TODO:  reconcile the actually wired channels with the ones in the SceneSpecs.		
		for (val c <- chans) {
			logInfo("Wiring to channel: " + c);
			c match {
				case fc: FancyChan => {
					myWiredChannels.put(fc.getIdent, fc);
				}
				case _ => logWarning("Cannot wire channel[" + c + "] because it doesn't have a 'fancy' ident");
			}
		}
	}
	def attachBehaviorsToModulator(bm : BehaviorModulator) {
		for (val bs : BehaviorSpec <- ss.myBehaviorSpecs.values) {
			val b = new Behavior(bs);
			bm.attachModule(b);
		}
	}
	def getChannel(id : Ident) : Channel = {
		return myWiredChannels.getOrElse(id, null);
	}
}
class SceneSpec () extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	val		myBehaviorSpecs = new HashMap[Ident,BehaviorSpec]();
	val		myChannelSpecs  = new HashMap[Ident,ChannelSpec]();

	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails + ", behaviors=" + myBehaviorSpecs + ", channels=" + 
				myChannelSpecs;
	}
	def addBehaviorSpec(bs: BehaviorSpec) {
		myBehaviorSpecs.put(bs.getIdent(), bs);
	}
	def addChannelSpec(cs: ChannelSpec) {
		myChannelSpecs.put(cs.getIdent(), cs);
	}
	
}

class SceneSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[SceneSpec](builderConfRes) {
	import scala.collection.JavaConversions._;
	
	override protected def initExtendedFieldsAndLinks(ss: SceneSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		logInfo("SceneBuilder.initExtendedFieldsAndLinks");	
		ss.myDetails = "ChockFilledUp";
		val linkedBehaviorSpecs : java.util.List[Object] = findOrMakeLinkedObjects(configItem, SceneFieldNames.P_behavior, assmblr, mode, null);
		for (val o <- linkedBehaviorSpecs) {
			o match {
				case bs: BehaviorSpec => ss.addBehaviorSpec(bs);
				case _ => logWarning("Unexpected object found at " + SceneFieldNames.P_behavior + " = " + o);
			}
		}
		val linkedChannelSpecs : java.util.List[Object] = findOrMakeLinkedObjects(configItem, SceneFieldNames.P_channel, assmblr, mode, null);
		for (val o <- linkedChannelSpecs) {
			o match {
				case cs: ChannelSpec => ss.addChannelSpec(cs);
				case _ => logWarning("Unexpected object found at " + SceneFieldNames.P_channel + " = " + o);
			}
		}		
		logInfo("Scene found linkedBehaviorSpecs: " + linkedBehaviorSpecs)
		logInfo("Scene found linkedChannelSpecs: " + linkedChannelSpecs)

	}
}
object SceneFieldNames extends org.appdapter.gui.assembly.AssemblyNames {
	val		NS_ccScn =	ChannelNames.NS_ccScn;
	val		NS_ccScnInst = ChannelNames.NS_ccScnInst;

	val		P_behavior	= NS_ccScn + "behavior";
	val		P_channel	= NS_ccScn + "channel";	
	
	val		P_steps				= NS_ccScn + "steps";	// Plural indicates RDF-collection
	val		P_startOffsetSec	= NS_ccScn + "startOffsetSec";
	val		P_text				= NS_ccScn + "text";
	
	val		N_rooty		=		"rooty";
	val		I_rooty		=		NS_ccScnInst + N_rooty;
}

