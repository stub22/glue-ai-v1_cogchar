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

package org.cogchar.lifter.model.handler

import net.liftweb.common.Logger
import org.cogchar.lifter.model.{ActionStrings,PageCommander}
import org.cogchar.lifter.view.TextBox
import scala.collection.mutable.ArrayBuffer

class SpeechCommandHandler extends AbstractLifterCommandHandler with Logger {

  protected val matchingTokens = ArrayBuffer(ActionStrings.acquireSpeech, ActionStrings.getContinuousSpeech,
											 ActionStrings.stopContinuousSpeech, ActionStrings.cogbotSpeech)
  
  protected def handleHere(sessionId:String, slotNum:Int, command:String, input:Array[String]) {  
	val primaryToken = command.split(ActionStrings.commandTokenSeparator)(0)
	primaryToken match {
	  case ActionStrings.acquireSpeech => {
		  if (input == null) { // If so, this is a button or etc. asking for speech acquisition to be triggered
			PageCommander.acquireSpeech(sessionId, slotNum)
		  } else { // otherwise, we are getting speech back from an acquisition via the SpeechRestListener
			displayInputSpeech(sessionId, input(0))
			// Next we strip the acquireSpeech prefix and continue handling. For this to work, the SpeechCommandHandler
			// must be near the "top" of the chain of responsiblity, and actions performed on acquired speech
			// (such as submittext) must be farther down the chain.
			nextHandler.processHandler(sessionId, slotNum, command.stripPrefix(ActionStrings.acquireSpeech + ActionStrings.commandTokenSeparator), input)
		  }
		}
	  case ActionStrings.getContinuousSpeech => {
		  if (input == null) { // If so, this is a button or etc. asking for speech acquisition to be triggered
			PageCommander.requestContinuousSpeech(sessionId, slotNum, true)
		  } else { // otherwise, we are getting speech back from an acquisition via the SpeechRestListener
			displayInputSpeech(sessionId, input(0))
			// Next we strip the getContinuousSpeech prefix and continue handling. For this to work, the SpeechCommandHandler
			// must be near the "top" of the chain of responsiblity, and actions performed on acquired speech
			// (such as submittext) must be farther down the chain.
			nextHandler.processHandler(sessionId, slotNum, command.stripPrefix(ActionStrings.getContinuousSpeech + ActionStrings.commandTokenSeparator), input)
		  }
		}
	  case ActionStrings.stopContinuousSpeech => {
		  PageCommander.requestContinuousSpeech(sessionId, slotNum, false)
		}
	  case ActionStrings.cogbotSpeech => {
		  val appState = PageCommander.getState
		  val secondToken = command.stripPrefix(ActionStrings.cogbotSpeech + ActionStrings.commandTokenSeparator)
		  secondToken match {
			case ActionStrings.ENABLE_TOKEN => appState.cogbotSpeaks(sessionId) = true
			case ActionStrings.DISABLE_TOKEN => appState.cogbotSpeaks(sessionId) = false
			case _ => error("Cogbot Speech lifter command seen, but following token " + secondToken + " is not understood")
		  }  
		}
	  case _ =>
	}
  }
  
  private def displayInputSpeech(sessionId:String, textToDisplay:String) {
	PageCommander.getState.speechDisplayers(sessionId).foreach(slotId => 
	  PageCommander.setControl(sessionId, slotId, 
		TextBox.makeBox("I think you said \"" + textToDisplay + "\"", PageCommander.getState.controlDefMap(sessionId)(slotId).style, true)))
  }
  
}

