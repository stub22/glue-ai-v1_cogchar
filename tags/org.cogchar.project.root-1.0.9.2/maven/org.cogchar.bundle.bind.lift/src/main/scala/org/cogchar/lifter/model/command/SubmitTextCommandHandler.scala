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
import org.cogchar.lifter.view.TextBoxFactory
import scala.collection.mutable.ArrayBuffer
import org.cogchar.lifter.model.main.{PageCommander, SpeechRecGateway}

class SubmitTextCommandHandler extends AbstractLifterCommandHandler {
  
  def warn(msg: String, params: Any*) {
      myLogger.warn(msg, params.map(_.asInstanceOf[Object]).toArray)
  }
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.submitText)
  
  override protected def handleCommand(cmdContext : CommandContext) { // appState:LifterState, sessionId:String, slotId:Int, command:String, input:Array[String]) {
	val splitAction = cmdContext.myCommand.split(ActionStrings.commandTokenSeparator)
	val actionToken = splitAction(1)
	actionToken match {
	  case ActionStrings.COGBOT_TOKEN => {
		  val sessionState = cmdContext.getSessionState() // myState.stateBySession(cmdContext.mySessionId)
		  val cogbotDisplayList = sessionState.cogbotDisplaySlots
		  if (cogbotDisplayList != Nil) { // Likely this check is not necessary - foreach just won't execute if list is Nil, right?
			val response = myLiftAmbassador.getCogbotResponse(cmdContext.myInput(0))
			val cleanedResponse = cleanCogbotResponse(response)
			cogbotDisplayList.foreach(slotNum =>
			  PageCommander.setControl(cmdContext.mySessionId, slotNum, 
						TextBoxFactory.makeBox("Cogbot said \"" + cleanedResponse 
										
																  + "\"", sessionState.controlConfigBySlot(cmdContext.mySlotNum).style)))
			if (sessionState.cogbotTextToSpeechActive) 
				SpeechRecGateway.outputSpeech(cmdContext.mySessionId, cleanedResponse) // Output Android speech if cogbotTextToSpeechActive is set
		  }
		}
	  case ActionStrings.DATABALLS_TOKEN => {
		  if (splitAction.size > 2) {
			val databallsAction = cmdContext.myCommand.split(ActionStrings.commandTokenSeparator)(2)
			myLiftAmbassador.performDataballAction(databallsAction, cmdContext.myInput(0));
		  } else {
			warn("Request found to submit text to databalls, but no destination (third Lifter command token) found during session {}", 
				 cmdContext.mySessionId)
		  }
		}
	  case _ => {
		  warn("No action found in SubmitTextCommandHandler for token {} during session {}", actionToken, cmdContext.mySessionId)
		}
	}
  }
  
  // For now, things are more readable if we just discard embedded XML
  // May not be necessary in the longer term
  private def cleanCogbotResponse(response:String) = {
	response.replaceAll("<.*>", "")
  }

}

