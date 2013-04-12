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


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;

import org.cogchar.impl.perform.{ChannelSpec};
import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.cogchar.name.behavior.{SceneFieldNames};

/**
 * @author Stu B. <www.texpedient.com>
 */

class GuardedBehavior (val mySBS: GuardedBehaviorSpec) extends Behavior(mySBS) {
	var myNextStepIndex : Int = 0;

	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		if (myNextStepIndex >= mySBS.mySteps.size) {
			getLogger().debug("Reached end of its steps at #{} self-requesting module stop on : {}", myNextStepIndex, this);
			markStopRequested();
			getLogger().info("Finished requesting stop, so this should be my last runOnce().");
		} else {
			val step = mySBS.mySteps(myNextStepIndex);
			if (step.proceed(scn, this)) {
				val osi = myNextStepIndex;
				myNextStepIndex += 1;
				getLogger().debug("Proceed succeeded for step # {} will attempt step# {} on next runOnce()", osi, myNextStepIndex);
			}
		}
	}
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", nextStepIndex=" + myNextStepIndex;
	}	
}

case class GuardedBehaviorSpec() extends BehaviorSpec {
	import scala.collection.JavaConversions._;
		
	var		mySteps : List[BehaviorStep] = List();

	// The field summary is used only for logging
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", details=" + myDetails + ", steps=" + mySteps;
	}
	
	override def makeBehavior() : Behavior = {
		new GuardedBehavior(this);
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		myDetails = "brimmingOver";
		val stepItems = reader.readLinkedItemSeq(configItem, SceneFieldNames.P_steps);
		getLogger().debug("BSB got stepItems: {}", stepItems);
		for (val stepItem : Item <- stepItems) {
			
			// Abstractly, a step has a guard and an action.  
			// The guard is a predicate that must be satisfied for the step to be taken.
			// This guard is not checked until all previous steps have been taken, so they
			// are part of the implied guard.  This point is relevant when we consider 
			// structures beyond lists of steps.  Some simple guard types are provided inline:
			//   a) absolute clock time
			
			
			getLogger().debug("Got stepItem: {}", stepItem)
			val stepIdent = stepItem.getIdent();
			val offsetSec = reader.readConfigValDouble(stepIdent, SceneFieldNames.P_startOffsetSec, stepItem, null);
			val offsetMillisec : Int = (1000.0 * offsetSec.doubleValue()).toInt;

			// Once the guard is passed, the step may "proceed", meaning the action is taken and then this step 
			// is complete.   We generally define action as an asynchronous act that cannot "fail" - that would
			// be a system failure rather than a step failure.
			// Simple action types:
			//    a) A piece of text to be passed to a channel, for example:
			//			a1) Animation name or command
			//			a2) Output speech text			

			val text = reader.readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
			val action = new TextAction(text);
			
			val stepChannelSpecs = reader.findOrMakeLinkedObjects(stepItem, SceneFieldNames.P_channel, assmblr, mode, null);
			getLogger().debug("Got step channel specs: {} ", stepChannelSpecs);
			for (val stepChanSpec <- stepChannelSpecs) {
				stepChanSpec match {
					case scs: ChannelSpec => {
						val chanId = scs.getIdent();
						val freeChanIdent = new FreeIdent(chanId);
						action.addChannelIdent(freeChanIdent);
					}
					case _ => getLogger().warn("Unexpected object found in step at {} = {}", SceneFieldNames.P_channel, stepChanSpec);
				}
			}
				
		
			val step = new ScheduledActionStep(offsetMillisec, action);
			getLogger().debug("Built step: {}", step);
			mySteps = mySteps :+ step;
		}		
	}
}
