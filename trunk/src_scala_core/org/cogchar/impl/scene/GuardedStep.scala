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
import org.appdapter.core.log.{BasicDebugger, Loggable};

import org.appdapter.core.name.{Ident}

import  org.cogchar.api.perform.{Media, PerfChannel, Performance, BasicPerformance}

import org.cogchar.impl.perform.{FancyTime, FancyPerformance};
/**
 * @author Stu B. <www.texpedient.com>
 */

trait Guard {
	def isSatisfied(scn : BScene) : Boolean
}
// Watches the performance of a previous step, and is satisfied when that Perf is marked STOPPING.
// Note that the guard itself is immutable (so far) and in principle could be used by many scenes.
// So far, the separation of the Guard and the spec is unnecessarily formal in this case, but that
// will probably help us soon.
class PerfMonitorGuard(mySpec : PerfMonGuardSpec) extends Guard {
	override def isSatisfied(scn : BScene) : Boolean = {
		scn match {
			case fbs : FancyBScene => {
				val perfStatus = fbs.getPerfStatusForStep(mySpec.myUpstreamStepID)
				perfStatus == mySpec.myStateToMatch 
			}
			case  _ => {
				throw new RuntimeException("Coding error:  PerfMonitorGuard asked to check on a non-fancy scene")
			}			
		}
	}
}
trait GuardSpec {
	def makeGuard : Guard
}
class PerfMonGuardSpec(val myUpstreamStepID : Ident, val myStateToMatch : Performance.State) extends GuardSpec {
	override def makeGuard  = 	new PerfMonitorGuard(this)
	override def toString() : String = {
		"PerfMonGuardSpec[upStepID=" + myUpstreamStepID + ", stateToMatch=" + myStateToMatch + "]"
	}	
}
/** If the StepExec has any internal *state*, then it can only be used once, in one scene.
 *  But if we are going to notice that our performance is "complete", then that requires state 
 *  - somewhere.  Since the "spec" layer already forms an immutable + cachable structure, we
 *  will allow the implementation level "step" to contain state.
 *  
 */

class GuardedStepExec(val myStepSpec : GuardedStepSpec, val myActionExec : BehaviorActionExec) extends BehaviorStepExec {
	var	myGuards : List[Guard] = Nil
	
	def addGuard(g : Guard) {
		myGuards = g :: myGuards
	}
	def checkAllGuardsSatisfied(scn: BScene) : Boolean = {
		for (g <- myGuards) {
			if (!g.isSatisfied(scn)) {
				return false
			}
		}
		true
	}	
	override def proceed(s: BScene, b: Behavior) : Boolean = {
		// Check guards, if all are satisfied, then perform action, register performance(s), and return true = complete.
		if (!checkAllGuardsSatisfied(s)) {
			return false
		}
		val perfList : List[FancyPerformance] = myActionExec.perform(s)
		val stepSpecID = myStepSpec.myOptID.get
		// We can't truly support multiple-performances yet (which happens if a step is bound to multiple output
		// channels).  Doing that now will lead to only the last performance being monitor-able.
		if (perfList.size != 1) {
			throw new RuntimeException("PerfList has unexpected size (!=1) : " +  perfList.size)
		}
		for (perf <- perfList) {
			// This registration allows the perf to satisfy guards of other steps, who find it by looking under
			// our stepSpecID - for now.
			registerPerfWithScene(s, stepSpecID, perf)
		}
		true
	}

	
	def registerPerfWithScene(scn: BScene, stepSpecID : Ident, perf: FancyPerformance) {
		scn match {
			case fbs : FancyBScene => {		
				val perfMonMod = new FancyPerfMonitorModule(perf)
				fbs.registerPerfForStep(stepSpecID, perf)
			}
			case  _ => {
				getLogger().warn("Cannot register FancyPerf with non-Fancy scene")
			}					
		}
	}
}
class GuardedStepSpec(stepSpecID : Ident, val myActionSpec: BehaviorActionSpec, val myGuardSpecs : Set[GuardSpec]) 
			extends BehaviorStepSpec(Some(stepSpecID)) {

	override def toString() : String = {
		"GuardedStepSpec[stepSpecID=" + stepSpecID + ", actionSpec=" + myActionSpec + ", guardSpecs=[" + myGuardSpecs + "]]"
	}
	override def makeStepExecutor() : BehaviorStepExec = {
		val actionExec = myActionSpec.makeActionExec
		val gse = new GuardedStepExec(this, actionExec)
		for (gs <- myGuardSpecs) {
			val guardExec = gs.makeGuard
			gse.addGuard(guardExec)
		}
		gse
	}
}
