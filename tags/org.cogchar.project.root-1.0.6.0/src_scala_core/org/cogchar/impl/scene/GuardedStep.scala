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
class PerfMonitorGuard(mySpec : PerfMonGuardSpec) extends BasicDebugger with Guard {
	override def isSatisfied(scn : BScene) : Boolean = {
		scn match {
			case fbs : FancyBScene => {
				val perfStatus = fbs.getPerfStatusForStep(mySpec.myUpstreamStepID)
				// FIXME:  We actually need to check for any state "equal or later" than the stateToMatch.
				if ( (perfStatus == mySpec.myStateToMatchOrExceed) ||
						 ((mySpec.myStateToMatchOrExceed == Performance.State.PLAYING) 
						  && (perfStatus == Performance.State.STOPPING))) {
					getLogger().debug("Treating perf-state {} as a match for guard {}", perfStatus,  mySpec )
					true
				} else {
					getLogger().debug("Perf-state {} is not a match for guard {}", perfStatus,  mySpec )
					false
				}
			}
			case  _ => {
				throw new RuntimeException("Coding error:  PerfMonitorGuard asked to check on a non-fancy scene")
			}			
		}
	}
	override def toString() : String = {
		"PerfMonitorGuard[spec=" + mySpec + "]";
	}	
}
trait GuardSpec {
	def makeGuard : Guard
}
class PerfMonGuardSpec(val myUpstreamStepID : Ident, val myStateToMatchOrExceed : Performance.State) extends GuardSpec {
	override def makeGuard  = 	new PerfMonitorGuard(this)
	override def toString() : String = {
		"PerfMonGuardSpec[upStepID=" + myUpstreamStepID + ", stateToMatchOrExceed=" + myStateToMatchOrExceed + "]"
	}	
}
/** If the StepExec has any internal *state*, then it can only be used once, in one scene.
 *  But if we are going to notice that our performance is "complete", then that requires state 
 *  - somewhere.  Since the "spec" layer already forms an immutable + cachable structure, we
 *  will allow the implementation level "step" to contain state.
 *  
 */

class GuardedStepExec(stepSpec : GuardedStepSpec, actionExec : BehaviorActionExec) 
		extends BehaviorStepExec(stepSpec, actionExec) {
			
	var	myGuards : List[Guard] = Nil
	
	def addGuard(g : Guard) {
		myGuards = g :: myGuards
	}
	def checkAllGuardsSatisfied(scn: BScene) : Boolean = {
		for (g <- myGuards) {
			if (!g.isSatisfied(scn)) {
				getLogger().debug("Guard is not satisfied: {}", g)
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
		getLogger().info("All {} guards are satisfied, now proceeding with step {}", myGuards.size, stepSpec.myOptID)
		beginPerformances(s, b)
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
