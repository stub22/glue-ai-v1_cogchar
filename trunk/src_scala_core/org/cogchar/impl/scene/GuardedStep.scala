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

import org.cogchar.impl.perform.{FancyTime};
/**
 * @author Stu B. <www.texpedient.com>
 */

trait Guard {
	def isSatisfied() : Boolean
}
abstract class PerformanceGuard() extends Guard {
	
}
class GuardSpec {
	/*
	def makeGuard(scene : BScene) : NotifyingGuard = {
		new NotifyingGuard();
	}
	*/
}
class GuardedStep {
	
}
/** If the step has any internal *state*, then it can only be used once, in one scene.
 *  But if we are going to notice that our performance is "complete", then that requires state 
 *  - somewhere.  Since the "spec" layer already forms an immutable + cachable structure, we
 *  will allow the implementation level "step" to contain state.
 *  
 */
class GuardedStepSpec(val myActionSpec: BehaviorActionSpec, val myGuardSpecs : Set[GuardSpec]) {
	private def startExec(s: BScene) {
		
	}
}
