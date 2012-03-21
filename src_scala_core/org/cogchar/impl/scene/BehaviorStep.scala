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

import  org.cogchar.api.perform.{Channel, TextChannel, Performance, BasicPerformance}
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

class SpeechAction(val mySpeechText : String) extends BasicBehaviorAction() { 
	override def perform(s: BScene) {
		for (val chanId : Ident <- myChannelIdents) {
			logInfo("Looking for channel[" + chanId + "] in scene [" + s + "]");
			val chan : Channel = s.getChannel(chanId);
			logInfo("Found channel: " + chan);
			if (chan != null) {
				chan match {
					case txtChan : TextChannel => {
						txtChan.performText(mySpeechText)
					}
					case  _ => {
						logWarning("************* SpeechAction cannot perform on non Text-Channel: " + chan);
					}
				}
			} else {
				logWarning("******************* Could not locate channel for: " + chanId);
			}
		}
	}
	override def toString() : String = {
		"SpeechAction[speechText=" + mySpeechText + ", channelIds=" + myChannelIdents + "]";
	}	
}
