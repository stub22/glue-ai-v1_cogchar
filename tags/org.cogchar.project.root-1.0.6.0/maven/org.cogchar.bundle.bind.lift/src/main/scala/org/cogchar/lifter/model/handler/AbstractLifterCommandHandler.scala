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
import org.cogchar.lifter.model.{LifterState}
import scala.collection.mutable.ArrayBuffer

trait AbstractLifterCommandHandler extends Logger {
  
  def processHandler(state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	if (this.matchingTokens contains command.split(ActionStrings.commandTokenSeparator)(0)) {this.handleHere(state, sessionId, slotNum, command, input)}
	else {
	  if (this.nextHandler != null) {
		nextHandler.processHandler(state, sessionId, slotNum, command, input)
	  } else {
		warn("Reached end of Lifter Command handling chain without finding handler for sessionId:" + sessionId + " and slotNum:" + slotNum + " with action: " + command) // Need to fix
	  }
	}
  }
  
  // Checks for actions which this control performs upon rendering, not actuation
  def checkForInitialAction(state:LifterState, sessionId:String, slotNum:Int, command:String) {
	if (this.matchingTokens contains command.split(ActionStrings.commandTokenSeparator)(0)) {this.handleInitialActionHere(state, sessionId, slotNum, command)}
	else {
	  if (this.nextHandler != null) {
		nextHandler.checkForInitialAction(state, sessionId, slotNum, command)
	  }
	}
  }
  
  var nextHandler: AbstractLifterCommandHandler = null
  
  def setNextHandler(handler: AbstractLifterCommandHandler) {
	nextHandler = handler
  }
  
  protected val matchingTokens: ArrayBuffer[String]
  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String])
  // A blank method for handleInitialActionHere. If a command would like to perform tasks on rendering, it can override this method.
  protected def handleInitialActionHere(state:LifterState, sessionId:String, slotNum:Int, command:String) {}
  
}
