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

package org.cogchar.lifter.model.command
import org.cogchar.name.lifter.{ActionStrings}
import org.cogchar.impl.web.wire.{LifterState}
import scala.collection.mutable.ArrayBuffer

class ShowTextCommandHandler extends AbstractLifterCommandHandler {
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.showText)
  
  // This command shouldn't be attached to a control which is actuated
  override protected def handleCommand(cmdContext : CommandContext) { // state:LifterState, sessionId:String, slotId:Int, command:String, input:Array[String]) {
	myLogger.warn("Handling actuated control with command showtext - that's strange! Nothing to do...")
  }
  
  override protected def handleInitialActionHere(cmdContext : CommandContext) { // state:LifterState, sessionId:String, slotNum:Int, command:String) {
	val splitAction = cmdContext.myCommand.split("_")
	val sessionState = cmdContext.getSessionState()// myState.stateBySession(cmdContext.mySessionId)
	splitAction(1) match {
	  case ActionStrings.COGBOT_TOKEN => { // Show Cogbot speech on this control? Add it to the cogbotDisplayers list.
		  sessionState.cogbotDisplaySlots += cmdContext.mySlotNum
		}
	  case ActionStrings.ANDROID_SPEECH_TOKEN => { // Add to the speechDisplayers list if we want Android speech shown here
		  sessionState.speechDisplaySlots += cmdContext.mySlotNum
		}
	  case ActionStrings.ERROR_TOKEN => { // Associate the error source name with the slotNum where errors will display
		  sessionState.errorDisplaySlotsByType(splitAction(2)) = cmdContext.mySlotNum
		}
	  case _ => myLogger.warn("ShowTextCommandHandler doesn't know what to do in order to display text with token {}", splitAction(1))
	}
  }
}
