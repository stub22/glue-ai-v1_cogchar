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

import org.cogchar.impl.perform.{FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyTextChan, FancyTextInstruction};

/**
 * @author Stu B. <www.texpedient.com>
 */

trait BehaviorAction {
	def perform(s: BScene);
}
abstract class BasicBehaviorAction extends BasicDebugger with BehaviorAction {
	// Usually there will be just one channel ID attached to each Action.
	var	myChannelIdents : List[Ident] = List();
	def addChannelIdent(id  : Ident) {
		myChannelIdents = myChannelIdents :+ id;
		logInfo("************ appended " + id + "  so list is now: " + myChannelIdents);
	}
}

class TextAction(val myActionText : String) extends BasicBehaviorAction() { 
	override def perform(s: BScene) {
		val media = new FancyTextMedia(myActionText);
		for (val chanId : Ident <- myChannelIdents) {
			getLogger().info("Looking for channel[" + chanId + "] in scene [" + s + "]");
			// val chan : Channel[_ <: Media, FancyTime] = s.getChannel(chanId);
			// val chan : Channel[_, _, _] = s.getChannel(chanId);
			val chan : PerfChannel = s.getChannel(chanId);
			getLogger().info("Found channel: " + chan);
			if (chan != null) {
				
				chan match {
					case txtChan : FancyTextChan => { // Channel.Text[FancyTime] => {
						// val perf : Performance[Media.Text, FancyTime] = txtChan.makePerformanceForMedia(media);
						// val perf = txtChan.makePerformanceForMedia(media);
						val initCursor  : FancyTextCursor = media.getCursorBeforeStart();
						val perf = new FancyTextPerf(media, txtChan, initCursor)
						val actionTime  = new FancyTime(0);
						val actionCursor = initCursor;
						val instruction  = new FancyTextInstruction(Performance.Instruction.Kind.PLAY, initCursor)
					
						val startResFlag = perf.attemptToScheduleInstruction(actionTime, instruction);
					}
					case  _ => {
						getLogger().warn("************* TextAction cannot perform on non Text-Channel: " + chan);
					}
				}
			} else {
				getLogger().warn("******************* Could not locate channel for: " + chanId);
			}
		}
	}
	override def toString() : String = {
		"TextAction[actionTxt=" + myActionText + ", channelIds=" + myChannelIdents + "]";
	}	
}
