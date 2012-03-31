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

import org.appdapter.core.item.{Ident}

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

trait BehaviorAction {
	def perform(s: BScene);
}
abstract class BasicBehaviorAction extends BasicDebugger with BehaviorAction {
	var	myChannelIdents : List[Ident] = List();
	def addChannelIdent(id  : Ident) {
		myChannelIdents = myChannelIdents :+ id;
		logInfo("************ appended " + id + "  so list is now: " + myChannelIdents);
	}
}

class TextAction(val myActionText : String) extends BasicBehaviorAction() { 
	override def perform(s: BScene) {
		val media = new Media.BasicText(myActionText);
		for (val chanId : Ident <- myChannelIdents) {
			logInfo("Looking for channel[" + chanId + "] in scene [" + s + "]");
			val chan : Channel[_ <: Media, FancyTime] = s.getChannel(chanId);
			logInfo("Found channel: " + chan);
			if (chan != null) {
				
				chan match {
					case txtChan : Channel.Text[FancyTime] => {
						val perf : Performance[Media.Text, FancyTime] = txtChan.makePerformanceForMedia(media);
						val startResFlag = perf.attemptToScheduleAction(Performance.Action.START, null);
					}
					case  _ => {
						logWarning("************* TextAction cannot perform on non Text-Channel: " + chan);
					}
				}
			} else {
				logWarning("******************* Could not locate channel for: " + chanId);
			}
		}
	}
	override def toString() : String = {
		"TextAction[actionTxt=" + myActionText + ", channelIds=" + myChannelIdents + "]";
	}	
}


