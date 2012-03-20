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
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;


/**
 * @author Stu B. <www.texpedient.com>
 */

class Behavior (val mySpec: BehaviorSpec) extends EmptyTimedModule[BScene] {
	var	myStartStamp : Long = -1;
	var myNextStepIndex : Int = 0;
	def logMe(msg: String) {logInfo("[" + this + "]-" + msg);}
	override protected def doStart(scn : BScene) {
		myStartStamp = System.currentTimeMillis();
	}
	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		if (myNextStepIndex >= mySpec.mySteps.size) {
			logMe("reached end of its steps, self-requesting module stop");
			markStopRequested();
			logMe("finished requesting stop, so, err...");
		} else {
			val step = mySpec.mySteps(myNextStepIndex);
			if (step.proceed(scn, this)) {
				myNextStepIndex += 1;
			}
		}
	}
	override protected def doStop(scn : BScene) {
		logMe("doStopping")
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
//class BoorishBehavior(val itemSpecs : List[OffsetItemSpec]) extends Behavior() { 
//	var	nextItemIndex = 0;
// }

class BehaviorModulator() extends BasicModulator[BScene](null, false) {

	def runUntilDone(msecDelay : Int) {
		var done = false;
		while (!done) {
			processOneBatch();
			val amc = activeModuleCount();
			logInfo("Active module count: " + amc)
			done = (amc == 0);
			if (!done) {
				Thread.sleep(msecDelay);
			}
		}
	}
	def  activeModuleCount() : Int = {
		val unfinishedModules : java.util.List[Module[BScene]] = getUnfinishedModules();
		return unfinishedModules.size();	
	}
	import scala.collection.JavaConversions._;
	def stopAllModules() {
		val unfinishedModules : java.util.List[Module[BScene]] = getUnfinishedModules();
		for (val um <- unfinishedModules) {
			um.markStopRequested();
		}
	}

}
class BehaviorSpec() extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	var		mySteps : List[BehaviorStep] = List();

	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", details=" + myDetails + ", steps=" + mySteps;
	}
}
class BehaviorSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[BehaviorSpec](builderConfRes) {

	import scala.collection.JavaConversions._;
	
	override protected def initExtendedFieldsAndLinks(bs: BehaviorSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		bs.myDetails = "brimmingOver";
		val stepItems = readLinkedItemSeq(configItem, SceneFieldNames.P_steps);
		logInfo("BSB got stepItems: " + stepItems);
		for (val si : Item <- stepItems) {
			logInfo("Got stepItem: " + si)
			val offsetSec = readConfigValDouble(si.getIdent, SceneFieldNames.P_startOffsetSec, si, null);
			val offsetMillisec : Int = (1000.0 * offsetSec.doubleValue()).toInt;
			val text = readConfigValString(si.getIdent(), SceneFieldNames.P_text, si, null);
			val action = new SpeechAction(text);
			val step = new ScheduledActionStep(offsetMillisec, action);
			logInfo("Built step: " + step);
			bs.mySteps = bs.mySteps :+ step;
		}		
	}
}
