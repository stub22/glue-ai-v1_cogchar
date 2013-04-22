/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.perform
import  org.cogchar.api.perform.{Media, PerfChannel, BasicPerfChan, Performance, BasicPerformance, BasicPerformanceListener, BasicPerformanceEvent}

/**
 * @author Stu B. <www.texpedient.com>
 */
// We mark OutJob as nullable to facilitate Java interop.
trait FancyPerfChan[OutJob >: Null] {
	val		myJobsByPerf = new scala.collection.mutable.HashMap[FancyPerformance, OutJob]()
	
	def registerOutJobForPerf(perf : FancyPerformance, oj : OutJob) {
		myJobsByPerf.put(perf, oj)
	}
	def markPerfStoppedAndForget(perf : FancyPerformance) {
		myJobsByPerf.remove(perf)
		perf.markFancyState(Performance.State.STOPPING);
	}
	def getOutJobOrNull(perf : FancyPerformance) : OutJob = myJobsByPerf.getOrElse(perf, null)
}

/**
 * This trait sets up the monitorModule, using features from our abstract types.
 */
trait FancyPerformance  { //  extends Performance[_, _, _ <: FancyTime] {
	def getFancyPerfState : Performance.State
	def syncWithFancyPerfChanNow : Unit
	def markFancyState(s : Performance.State) : Unit
	// We cannot narrow the type of this type param in the overrides.  We can only widen it!
	// So, this def winds up being no better than passing in Object as the argument.
	// def markFancyCursor[Cur](c : Cur, notify : Boolean) : Unit
}
