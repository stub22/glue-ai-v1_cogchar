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
import org.cogchar.lifter.model.main.{PageCommander, SpeechRecGateway}
import org.cogchar.impl.web.wire.{LifterState}
import org.cogchar.lifter.view.TextBox
import scala.collection.mutable.ArrayBuffer

class SpeechCommandHandler extends AbstractLifterCommandHandler {
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.acquireSpeech, ActionStrings.getContinuousSpeech,
											 ActionStrings.stopContinuousSpeech, ActionStrings.cogbotSpeech)
  
  override protected def handleCommand(cmdContext : CommandContext) { // appState:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {  
	val primaryToken = cmdContext.myCommand.split(ActionStrings.commandTokenSeparator)(0)
	primaryToken match {
	  case ActionStrings.acquireSpeech => {
		  if (cmdContext.myInput == null) { // If so, this is a button or etc. asking for speech acquisition to be triggered
			SpeechRecGateway.acquireSpeech(cmdContext.mySessionId, cmdContext.mySlotNum)
		  } else { // otherwise, we are getting speech back from an acquisition via the SpeechRestListener
			displayInputSpeech(cmdContext.myState, cmdContext.mySessionId, cmdContext.myInput(0))
			// Next we strip the acquireSpeech prefix and continue handling. For this to work, the SpeechCommandHandler
			// must be near the "top" of the chain of responsiblity, and actions performed on acquired speech
			// (such as submittext) must be farther down the chain.
			val nextCmdHandler = getNextCommandHandler
			val nextCommand = cmdContext.myCommand.stripPrefix(ActionStrings.acquireSpeech + ActionStrings.commandTokenSeparator)
			nextCmdHandler.processCommand(cmdContext.myState, cmdContext.mySessionId, cmdContext.mySlotNum, 
							nextCommand, cmdContext.myInput)
		  }
		}
	  case ActionStrings.getContinuousSpeech => {
		  if (cmdContext.myInput == null) { // If so, this is a button or etc. asking for speech acquisition to be triggered
			SpeechRecGateway.requestContinuousSpeech(cmdContext.mySessionId, cmdContext.mySlotNum, true)
		  } else { // otherwise, we are getting speech back from an acquisition via the SpeechRestListener
			displayInputSpeech(cmdContext.myState, cmdContext.mySessionId, cmdContext.myInput(0))
			// Next we strip the getContinuousSpeech prefix and continue handling. For this to work, the SpeechCommandHandler
			// must be near the "top" of the chain of responsiblity, and actions performed on acquired speech
			// (such as submittext) must be farther down the chain.
			val nextCmdHandler = getNextCommandHandler
			val nextCommand = cmdContext.myCommand.stripPrefix(ActionStrings.getContinuousSpeech + ActionStrings.commandTokenSeparator)
			nextCmdHandler.processCommand(cmdContext.myState, cmdContext.mySessionId, cmdContext.mySlotNum, nextCommand, cmdContext.myInput)
		  }
		}
	  case ActionStrings.stopContinuousSpeech => {
		  SpeechRecGateway.requestContinuousSpeech(cmdContext.mySessionId, cmdContext.mySlotNum, false)
		}
	  case ActionStrings.cogbotSpeech => {
		  val secondToken = cmdContext.myCommand.stripPrefix(ActionStrings.cogbotSpeech + ActionStrings.commandTokenSeparator)
		  val sessionState = cmdContext.getSessionState() // myState.stateBySession(cmdContext.mySessionId)
		  secondToken match {
			case ActionStrings.ENABLE_TOKEN => sessionState.cogbotTextToSpeechActive = true
			case ActionStrings.DISABLE_TOKEN => sessionState.cogbotTextToSpeechActive = false
			case _ => myLogger.error("Cogbot Speech lifter command seen, but following token {} is not understood", secondToken)
		  }  
		}
	  case _ =>
	}
  }
  
  private def displayInputSpeech(state:LifterState, sessionId:String, textToDisplay:String) {
	val sessionState = state.stateBySession(sessionId)
	sessionState.speechDisplaySlots.foreach(slotId => 
	  PageCommander.setControl(sessionId, slotId, 
		TextBox.makeBox("I think you said \"" + textToDisplay + "\"", sessionState.controlConfigBySlot(slotId).style, true)))
  }
  
}

