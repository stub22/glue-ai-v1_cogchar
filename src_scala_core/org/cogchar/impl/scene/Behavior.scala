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

import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;

import org.cogchar.impl.perform.{ChannelSpec};
import org.appdapter.core.log.{BasicDebugger, Loggable};
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
			logMe("Reached end of its steps at #" + myNextStepIndex + ", self-requesting module stop");
			markStopRequested();
			logMe("Finished requesting stop, so this should be my last runOnce().");
		} else {
			val step = mySpec.mySteps(myNextStepIndex);
			if (step.proceed(scn, this)) {
				val osi = myNextStepIndex;
				myNextStepIndex += 1;
				logMe("Proceed succeeded for step #" + osi + ", will attempt step# " + myNextStepIndex + " on next runOnce().");
			}
		}
	}
	override protected def doStop(scn : BScene) {
		logMe("doStopping")
	}
	def getMillsecSinceStart() : Long = { 
		System.currentTimeMillis() - myStartStamp;
	}
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", nextStepIndex=" + myNextStepIndex;
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
			done = (amc == 0);
			if (!done) {
				logInfo(Loggable.IMPO_LO, "runUntilDone() loop finished processOneBatch, active module count=" + amc + ", sleeping for " + msecDelay + "msec.");
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
		for (val stepItem : Item <- stepItems) {
			logInfo("Got stepItem: " + stepItem)
			val stepIdent = stepItem.getIdent();
			val offsetSec = readConfigValDouble(stepIdent, SceneFieldNames.P_startOffsetSec, stepItem, null);
			val offsetMillisec : Int = (1000.0 * offsetSec.doubleValue()).toInt;
			val text = readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
			
			readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
			val action = new SpeechAction(text);
			
			val stepChannelSpecs = findOrMakeLinkedObjects(configItem, SceneFieldNames.P_channel, assmblr, mode, null);
			for (val stepChanSpec <- stepChannelSpecs) {
				stepChanSpec match {
					case scs: ChannelSpec => {
						val chanId = scs.getIdent();
						val freeChanIdent = new FreeIdent(chanId);
						action.addChannelIdent(freeChanIdent);
					}
					case _ => logWarning("Unexpected object found in step[at " + SceneFieldNames.P_channel + " = " + stepChanSpec);
				}
			}
				
		
			val step = new ScheduledActionStep(offsetMillisec, action);
			logInfo("Built step: " + step);
			bs.mySteps = bs.mySteps :+ step;
		}		
	}
}
