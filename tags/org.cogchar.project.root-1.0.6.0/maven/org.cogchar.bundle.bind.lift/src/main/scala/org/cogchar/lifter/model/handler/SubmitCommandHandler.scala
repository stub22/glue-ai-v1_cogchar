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
import org.cogchar.name.lifter.{ActionStrings}
import org.cogchar.lifter.model.{LifterState,PageCommander}
import scala.collection.mutable.ArrayBuffer

class SubmitCommandHandler extends AbstractLifterCommandHandler with Logger {

  protected val matchingTokens = ArrayBuffer(ActionStrings.submit)
  
  protected def handleHere(appState:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	val desiredAction = command.stripPrefix(ActionStrings.submit + ActionStrings.commandTokenSeparator)
	desiredAction match {
	  case ActionStrings.NETWORK_CONFIG_TOKEN => {
		  if (input.length == 2) {
			var encryptionName:String = null;
			val sessionVariables = appState.stateBySession(sessionId).sessionLifterVariablesByName
			if (sessionVariables contains ActionStrings.encryptionTypeVar) {
			  encryptionName = sessionVariables(ActionStrings.encryptionTypeVar)
			} else {
			  warn("No encryption type set for network config, assuming none")
			  encryptionName = ActionStrings.noEncryptionName
			}
			PageCommander.getLiftAmbassador.requestNetworkConfig(input(0), encryptionName, input(1))
		  } else {
			error("Network config submit lifter action requested, but text input list length is not 2")
		  }
		}
	  case ActionStrings.LOGIN_TOKEN => {
		  if (input.length == 2) {
			PageCommander.getLiftAmbassador.login(sessionId, input(0), input(1));
		  } else {
			error("Login submit lifter action requested, but text input list length is not 2")
		  }
		}
	  case _ => {
		  error("No action found for submit lifter command with token " + desiredAction)
		}
	}
  }
  
}
