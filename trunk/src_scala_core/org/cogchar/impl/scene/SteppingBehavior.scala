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
/**
 * @author Stu B. <www.texpedient.com>
 */

class SteppingBehavior (val mySBS: SteppingBehaviorSpec) extends Behavior(mySBS) {
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

case class SteppingBehaviorSpec() extends BehaviorSpec {
	import scala.collection.JavaConversions._;
		
	var		mySteps : List[BehaviorStep] = List();

	// The field summary is used only for logging
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", details=" + myDetails + ", steps=" + mySteps;
	}
	
	override def makeBehavior() : Behavior = {
		new SteppingBehavior(this);
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		myDetails = "brimmingOver";
		val stepItems = reader.readLinkedItemSeq(configItem, SceneFieldNames.P_steps);
		getLogger().debug("BSB got stepItems: {}", stepItems);
		for (val stepItem : Item <- stepItems) {
			getLogger().debug("Got stepItem: {}", stepItem)
			val stepIdent = stepItem.getIdent();
			val offsetSec = reader.readConfigValDouble(stepIdent, SceneFieldNames.P_startOffsetSec, stepItem, null);
			val offsetMillisec : Int = (1000.0 * offsetSec.doubleValue()).toInt;
			
			// Text actions
			val text = reader.readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
			// val path = readConfigValString(stepItem.getIdent(), SceneFieldNames.P_path, stepItem, null);
			
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
					case _ => getLogger().warn("Unexpected object found in step[at " + SceneFieldNames.P_channel + " = " + stepChanSpec);
				}
			}
				
		
			val step = new ScheduledActionStep(offsetMillisec, action);
			getLogger().debug("Built step: {}", step);
			mySteps = mySteps :+ step;
		}		
	}
}
