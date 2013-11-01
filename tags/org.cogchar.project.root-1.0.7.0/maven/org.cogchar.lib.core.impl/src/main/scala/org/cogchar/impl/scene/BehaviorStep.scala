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

import org.appdapter.core.log.BasicDebugger
import org.appdapter.core.name.{FreeIdent, Ident}
import org.cogchar.api.perform.{BehaviorActionExec}
import org.cogchar.api.perform.{PerfChannel, Media, Performance, FancyPerformance};
import org.cogchar.impl.perform.{ FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyTextPerfChan, FancyTextInstruction};
import org.cogchar.api.scene.Behavior



/**
 * @author Stu B. <www.texpedient.com>
 */

abstract class BehaviorStepExec(val myStepSpec : BehaviorStepSpec, val myActionExec : BehaviorActionExec) extends BasicDebugger  {
	// Generally all comm should be through the scene, and the behavior should be ignored by the step.
	def proceed(s: BScene, b: Behavior[BScene]) : Boolean;

	protected def beginPerformances(s: BScene, b: Behavior[BScene]) : Boolean = {

		val perfList : java.util.List[FancyPerformance] = myActionExec.performExec(s)

		// We can't truly support multiple-performances yet (which happens if a step is bound to multiple output
		// channels).  Doing that now will lead to only the last performance being monitor-able.
		//
		// If actionExec failed in some way, then we will have 0 perfs, and that is OK.
		// But then any other (guarded) steps blocked on this one will then not be able to proceed.

		if (perfList.size > 1) {
			throw new RuntimeException("PerfList has unexpected size (> 1) : " +  perfList.size)
		}
		for (perf0 <- perfList.toArray) {
			val perf = perf0.asInstanceOf[FancyPerformance]
			val perfKeyID = getPerfKeyID(perf, s, b);
			// This registration allows the perf to satisfy guards of other steps, who find it by looking under
			// our stepSpecID - for now.1
			registerPerfWithScene(s, perfKeyID, perf)
		}
		true
	}
	protected def registerPerfWithScene(scn: BScene, stepSpecID : Ident, perf: FancyPerformance) {
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
	protected def getPerfKeyID(perf : FancyPerformance, s: BScene, b: Behavior[BScene]) : Ident = {
		myStepSpec.myOptID match {
			case Some(stepSpecID) => stepSpecID
			case None => new FreeIdent(org.cogchar.name.behavior.SceneFieldNames.NS_ccScnInst + "perf_" + perf.hashCode);
		}
	}

}
abstract class BehaviorStepSpec(val myOptID : Option[Ident]) extends BasicDebugger {
	def makeStepExecutor() : BehaviorStepExec



}
class ScheduledActionStepExec(stepSpec : ScheduledActionStepSpec, actionExec : BehaviorActionExec)
			extends BehaviorStepExec (stepSpec, actionExec) {
	def proceed(s: BScene, b: Behavior[BScene]) : Boolean = {
		val msecSinceStart = b.getMillsecSinceStart();
		if (msecSinceStart >= stepSpec.myOffsetMillisec) {
			beginPerformances(s, b);
		} else {
			false;
		}
	}

}

class ScheduledActionStepSpec (val myOffsetMillisec : Int, val myActionSpec: BehaviorActionSpec)
			extends BehaviorStepSpec(None) {

	def makeStepExecutor() : BehaviorStepExec = {
		val actionExec = myActionSpec.makeActionExec
		new ScheduledActionStepExec(this, actionExec)
	}
	override def toString() : String = {
		"ScheduledActionStepSpec[offsetMsec=" + myOffsetMillisec + ", action=" + myActionSpec + "]"
	}
}


