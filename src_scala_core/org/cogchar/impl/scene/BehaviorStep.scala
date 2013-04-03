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

import  org.cogchar.api.perform.{Media, Channel, Performance, BasicPerformance}

import org.cogchar.impl.perform.{FancyTime};
/**
 * @author Stu B. <www.texpedient.com>
 */

trait BehaviorStep {
	def proceed(s: BScene, b: Behavior) : Boolean;
}

class ScheduledActionStep (val myOffsetMillisec : Int, val myAction: BehaviorAction) extends BehaviorStep() { 
	override def toString() : String = {
		"ScheduledActionStep[offsetMsec=" + myOffsetMillisec + ", action=" + myAction + "]"
	}
	def proceed(s: BScene, b: Behavior) : Boolean = {
		val msecSinceStart = b.getMillsecSinceStart();
		if (msecSinceStart >= myOffsetMillisec) {
			myAction.perform(s);
			true;
		} else {
			false;
		}
	}

}


