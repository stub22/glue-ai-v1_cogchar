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

import org.cogchar.impl.perform.{FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyPerformance, FancyTextPerfChan, FancyTextInstruction};

/**
 * @author Stu B. <www.texpedient.com>
 */

trait BehaviorActionExec {
	def perform(s: BScene) : List[FancyPerformance]
}

abstract class BehaviorActionSpec extends BasicDebugger {
	// Usually there will be just one channel ID attached to each Action.
	var	myChannelIdents : List[Ident] = List();
	def addChannelIdent(id  : Ident) {
		myChannelIdents = myChannelIdents :+ id;
		logInfo("************ appended " + id + "  so list is now: " + myChannelIdents);
	}
	def makeActionExec() : BehaviorActionExec
}

class TextActionSpec(val myActionText : String) extends BehaviorActionSpec() { 
	override def makeActionExec() : BehaviorActionExec = {
		new TextActionExec(this)
	}
	override def toString() : String = {
		"TextActionSpec[actionTxt=" + myActionText + ", channelIds=" + myChannelIdents + "]";
	}		
}
class TextActionExec(val mySpec : TextActionSpec) extends BasicDebugger with BehaviorActionExec {
	override def perform(s: BScene) : List[FancyPerformance] = {
		var perfListReverseOrder : List[FancyPerformance] = Nil
		val media = new FancyTextMedia(mySpec.myActionText);
		for (val chanId : Ident <- mySpec.myChannelIdents) {
			getLogger().info("Looking for channel[{}] in scene [{}]", chanId, s);
			val chan : PerfChannel = s.getChannel(chanId);
			getLogger().info("Found channel {}", chan);
			if (chan != null) {
				chan match {
					case txtChan : FancyTextPerfChan => { 
						val perf  = txtChan.makePerfAndPlayAtBeginNow(media)
						// prepending to the list is fastest, hence the "reverseOrder" approach.
						perfListReverseOrder = perf :: perfListReverseOrder
						/*
						 2013-04-21 This inline prototype code has been refactored out and will be removed soon.
						val initCursor  : FancyTextCursor = media.getCursorBeforeStart();
						val perf = new FancyTextPerf(media, txtChan, initCursor)
						val actionTime  = new FancyTime(0);
						val actionCursor = initCursor;
						val instruction  = new FancyTextInstruction(Performance.Instruction.Kind.PLAY, initCursor)
					
						val startResFlag = perf.attemptToScheduleInstruction(actionTime, instruction);
						*/
						
					}
					case  _ => {
						getLogger().warn("************* TextAction cannot perform on non Text-Channel: " + chan);
					}
				}
			} else {
				getLogger().warn("******************* Could not locate channel for: " + chanId);
			}
		}
		perfListReverseOrder.reverse  // we presume that Nil.reverse == Nil !
	}
	override def toString() : String = {
		"TextActionExec[spec=" + mySpec + "]";
	}	
}
