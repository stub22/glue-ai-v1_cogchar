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

class SubmitCommandHandler extends AbstractLifterCommandHandler {
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.submit)
  
  override protected def handleCommand(cmdContext : CommandContext) { // appState:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	val desiredAction = cmdContext.myCommand.stripPrefix(ActionStrings.submit + ActionStrings.commandTokenSeparator)
	desiredAction match {
	  case ActionStrings.NETWORK_CONFIG_TOKEN => {
		  if (cmdContext.myInput.length == 2) {
			var encryptionName:String = null;
			val sessionVariables = cmdContext.getSessionState().sessionLifterVariablesByName
			if (sessionVariables contains ActionStrings.encryptionTypeVar) {
			  encryptionName = sessionVariables(ActionStrings.encryptionTypeVar)
			} else {
			  myLogger.warn("No encryption type set for network config, assuming none")
			  encryptionName = ActionStrings.noEncryptionName
			}
			myLiftAmbassador.requestNetworkConfig(cmdContext.myInput(0), encryptionName, cmdContext.myInput(1))
		  } else {
			myLogger.error("Network config submit lifter action requested, but text input list length is not 2")
		  }
		}
	  case ActionStrings.LOGIN_TOKEN => {
		  if (cmdContext.myInput.length == 2) {
			myLiftAmbassador.login(cmdContext.mySessionId, cmdContext.myInput(0), cmdContext.myInput(1));
		  } else {
			myLogger.error("Login submit lifter action requested, but text input list length is not 2")
		  }
		}
	  case _ => {
		  myLogger.error("No action found for submit lifter command with token {}", desiredAction)
		}
	}
  }
  
}
