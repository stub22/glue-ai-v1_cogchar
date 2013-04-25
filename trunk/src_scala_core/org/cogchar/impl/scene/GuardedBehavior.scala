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
import org.appdapter.core.item.{Item, ItemFuncs};
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;


import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import org.cogchar.name.behavior.{SceneFieldNames};


import org.appdapter.module.basic.{EmptyTimedModule,BasicModulator}
import org.appdapter.api.module.{Module, Modulator}
import org.appdapter.api.module.Module.State;

import org.cogchar.impl.perform.{ChannelSpec};
import org.cogchar.api.perform.{Performance};
import org.appdapter.core.log.{BasicDebugger, Loggable};

import scala.collection.mutable.HashSet
/**
 * @author Stu B. <www.texpedient.com>
 * 
 */

class GuardedBehavior (val myGBS: GuardedBehaviorSpec) extends Behavior(myGBS) {
	var myNextStepIndex : Int = 0;

	import scala.collection.mutable.HashSet
	
	val myPendingStepExecs  = new HashSet[GuardedStepExec]()
	
	def makeStepExecs() {
		for (gss <- myGBS.myStepSpecs) {
			val gse = gss.makeStepExecutor.asInstanceOf[GuardedStepExec]
			myPendingStepExecs.add(gse)
		}
	}
	override protected def doStart(scn : BScene) {
		super.doStart(scn)
		makeStepExecs();
	}
	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		val pendingList = myPendingStepExecs.toList
		for (gse <- pendingList) {
			val didIt = gse.proceed(scn, this)
			if (didIt) {
				myPendingStepExecs.remove(gse)
			}
		}
		// TODO:  Check for "finalStep" processed without other steps necessary.
		if (myPendingStepExecs.size == 0) {
			getLogger().debug("Used up all my steps, so self-requesting module stop on : {}", myGBS.getIdent);
			markStopRequested();
			getLogger().info("Finished requesting stop, so this should be my last runOnce().");			
		}
	}
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", nextStepIndex=" + myNextStepIndex;
	}	
}

case class GuardedBehaviorSpec() extends BehaviorSpec {
	import scala.collection.JavaConversions._;
		
	var		myStepSpecs : List[BehaviorStepSpec] = List();

	// The field summary is used only for logging
	override def getFieldSummary() : String = {
		return  super.getFieldSummary() +  ", details=" + myDetails + ", steps=" + myStepSpecs;
	}
	
	override def makeBehavior() : Behavior = {
		new GuardedBehavior(this);
	}
	override def completeInit(configItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		/**
	public static String		P_initialStep		= NS_ccScn + "initialStep";
	public static String		P_step				= NS_ccScn + "step";
	public static String		P_finalStep			= NS_ccScn + "finalStep";
	public static String		P_waitForStart		= NS_ccScn + "waitForStart";
	public static String		P_waitForEnd		= NS_ccScn + "waitForEnd";
		 */
		myDetails = "brimmingOver";
		
		val stepPropID = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_step)
		val stepItems : java.util.Set[Item] = configItem.getLinkedItemSet(stepPropID)
		
		getLogger().debug("GBS got stepItems: {}", stepItems);
		for (val stepItem : Item  <- stepItems) {
		
			// Abstractly, a step has a set of guards and an action.  
			// The guards are predicates that must be satisfied for the step to be taken.			

			// Once the guard is passed, the step may "proceed", meaning the action is taken and then this step 
			// is complete.   We generally define action as an asynchronous act that cannot "fail" - that would
			// be a system failure rather than a step failure.
			// Simple action types:
			//    a) A piece of text to be passed to a channel, for example:
			//			a1) Animation name or command
			//			a2) Output speech text			
			
			getLogger().debug("Got stepItem: {}", stepItem)
			val stepIdent = stepItem.getIdent();
			
			// Haven't decided yet what to do with this "offsetSec" in the GuardedBehaviorStep case. 
			val offsetSec = reader.readConfigValDouble(stepIdent, SceneFieldNames.P_startOffsetSec, stepItem, null);
			val offsetMillisec : Int = if (offsetSec == null) 0 else (1000.0 * offsetSec.doubleValue()).toInt;

			val text = reader.readConfigValString(stepItem.getIdent(), SceneFieldNames.P_text, stepItem, null);
			val actionSpec = new TextActionSpec(text);
			actionSpec.readChannels(stepItem, reader, assmblr, mode)

			val guardSpecSet = new HashSet[GuardSpec]()
			
			val waitStartGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_waitForStart)
			val waitEndGuardProp = ItemFuncs.getNeighborIdent(configItem, SceneFieldNames.P_waitForEnd)
			
			val waitStartGuardItems = stepItem.getLinkedItemSet(waitStartGuardProp)
			val waitEndGuardItems = stepItem.getLinkedItemSet(waitEndGuardProp)
			
			for (wsg : Item <- waitStartGuardItems) {
				val guardSpec = new PerfMonGuardSpec(wsg.getIdent, Performance.State.PLAYING)
				guardSpecSet.add(guardSpec)
			}
			for (weg : Item <- waitEndGuardItems) {
				val guardSpec = new PerfMonGuardSpec(weg.getIdent, Performance.State.STOPPING)
				guardSpecSet.add(guardSpec)
			}
							
			val stepSpec = new GuardedStepSpec(stepIdent, actionSpec, guardSpecSet.toSet) // offsetMillisec, actionSpec);
			getLogger().debug("Built stepSpec: {}", stepSpec);
			myStepSpecs = myStepSpecs :+ stepSpec;
		}		
	}
}
