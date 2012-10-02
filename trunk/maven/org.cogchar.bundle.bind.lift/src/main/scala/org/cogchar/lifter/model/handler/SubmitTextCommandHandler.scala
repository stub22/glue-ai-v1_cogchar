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
import org.cogchar.lifter.model.{ActionStrings,LifterState,PageCommander}
import org.cogchar.lifter.view.TextBox
import scala.collection.mutable.ArrayBuffer

class SubmitTextCommandHandler extends AbstractLifterCommandHandler with Logger {
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.submitText)
  
  protected def handleHere(appState:LifterState, sessionId:String, slotId:Int, command:String, input:Array[String]) {
	val splitAction = command.split(ActionStrings.commandTokenSeparator)
	val actionToken = splitAction(1)
	actionToken match {
	  case ActionStrings.COGBOT_TOKEN => {
		  if (appState.cogbotDisplayers(sessionId) != Nil) { // Likely this check is not necessary - foreach just won't execute if list is Nil, right?
			val response = PageCommander.getLiftAmbassador.getCogbotResponse(input(0))
			val cleanedResponse = response.replaceAll("<.*>", ""); // For now, things are more readable if we just discard embedded XML
			appState.cogbotDisplayers(sessionId).foreach(slotNum =>
			  PageCommander.setControl(sessionId, slotNum, TextBox.makeBox("Cogbot said \"" + cleanedResponse + "\"", appState.controlDefMap(sessionId)(slotNum).style)))
			if (appState.cogbotSpeaks(sessionId)) PageCommander.outputSpeech(sessionId, cleanedResponse) // Output Android speech if cogbotSpeaks is set
		  }
		}
	  case ActionStrings.DATABALLS_TOKEN => {
		  if (splitAction.size > 2) {
			val databallsAction = command.split(ActionStrings.commandTokenSeparator)(2)
			PageCommander.getLiftAmbassador.performDataballAction(databallsAction, input(0));
		  } else {
			warn("Request found to submit text to databalls, but no destination (third Lifter command token) found during session " + sessionId)
		  }
		}
	  case _ => {
		  warn("No action found in SubmitTextCommandHandler for token " + actionToken + " during session " + sessionId)
		}
	}
  }

}

