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

import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;

import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;

import org.cogchar.impl.perform.{ChannelSpec};
import org.appdapter.core.log.{BasicDebugger, Loggable};

/**  A Behavior is a thread of activity run by a BehaviorModulator.
 *   State information is generally shared with other behaviors through the BScene object
 *  that serves as context for the BehaviorModulator, which is passed in to each
 *  action method.  
 *   Behaviors are generally internally stateless, except for keeping track of 
 *  where they are in their own program of activity.
 * @author Stu B. <www.texpedient.com>
 */


abstract class Behavior(val mySpec: BehaviorSpec) extends EmptyTimedModule[BScene] {

	var	myStartStamp : Long = -1;
	// def logMe(msg: String) {logInfo("[" + this + "]-" + msg);}
	override protected def doStart(scn : BScene) {
		myStartStamp = System.currentTimeMillis();
		myRunDebugModulus = 20;
	}
	override protected def doStop(scn : BScene) {
		getLogger().info("doStop called for behavior {}", this)
	}
	def getMillsecSinceStart() : Long = { 
		System.currentTimeMillis() - myStartStamp;
	}
	
}

class TStamp () {
	val	mySysStamp : Long = System.currentTimeMillis();
	val	myFullSec = mySysStamp / 1000;
	val myMilSec = mySysStamp - myFullSec * 1000;
}
class TimelineBehavior (bs: BehaviorSpec) {
	
} 


abstract class BehaviorSpec() extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	
	def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode);
	def makeBehavior() : Behavior;
}

class BehaviorSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[BehaviorSpec](builderConfRes) {
	
	override protected def initExtendedFieldsAndLinks(bs: BehaviorSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		bs.completeInit(configItem, getReader(), assmblr, mode);
	}
}
