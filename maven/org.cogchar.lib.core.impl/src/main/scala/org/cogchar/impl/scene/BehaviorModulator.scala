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

import org.appdapter.module.basic.{BasicModulator}
import org.appdapter.api.module.{Module}
import org.appdapter.core.log.{Loggable};

/**
 * The behavior-modulator uses a BScene as the (potentially writable) 
 * application "Context" for the modules.   Through this context they
 * can share access to state and resources.
 * @author Stu B. <www.texpedient.com>
 */

class BehaviorModulator() extends BasicModulator[BScene](null, true) {
	def setSceneContext(scene : BScene) { 
		setDefaultContext(scene);
	}
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
	def requestStopOnAllModules() {
		val unfinishedModules : java.util.List[Module[BScene]] = getUnfinishedModules();
		for (um <- unfinishedModules) {
			um.markStopRequested();
		}
	}
	def detachAllFinishedModules() {
		val finishedModules : java.util.List[Module[BScene]] = getFinishedModules();
		for (fm <- finishedModules) {
			detachModule(fm);
		}
	}
	def findUnfinishedModules(modSet : Set[Module[BScene]]) : Set[Module[BScene]] = {
		getUnfinishedModules().toSet.intersect(modSet)
	}
}
