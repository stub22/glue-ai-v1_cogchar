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

import org.cogchar.name.lifter.ActionStrings
import org.cogchar.impl.web.util.LifterLogger

import org.cogchar.impl.web.wire.{LifterState, WebSessionState}
import org.cogchar.impl.web.config.{LiftAmbassador}
import org.cogchar.lifter.model.main.{PageCommander} // Used just to fetch LiftAmbassadors

import scala.collection.mutable.ArrayBuffer

class CommandContext(val myState:LifterState, 
					 val mySessionId:String, 
					 val mySlotNum:Int, 
					 val myCommand:String, 
					 val myInput:Array[String]) {
	def getSessionState() : WebSessionState = myState.stateBySession(mySessionId)
}
	
trait AbstractLifterCommandHandler extends LifterLogger {
	protected var myLiftAmbassador : LiftAmbassador = PageCommander.getLiftAmbassador
	
  def processCommand(state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	if (this.matchingTokens contains command.split(ActionStrings.commandTokenSeparator)(0)) {
		val cmdContext = new CommandContext(state, sessionId, slotNum, command, input)
		this.handleCommand(cmdContext) // state, sessionId, slotNum, command, input)
	}
	else {
	  if (this.myNextCommandHandler != null) {
		myNextCommandHandler.processCommand(state, sessionId, slotNum, command, input)
	  } else {
		myLogger.warn("Reached end of Lifter Command handling chain without finding handler for sessionId:{} " +
					  "and slotNum:{} with action: {}", Array[AnyRef](sessionId, slotNum.asInstanceOf[AnyRef], command))
	  }
	}
  }
  
  // Checks for actions which this control performs upon rendering, not actuation
  def checkForInitialAction(state:LifterState, sessionId:String, slotNum:Int, command:String) {
	if (this.matchingTokens contains command.split(ActionStrings.commandTokenSeparator)(0)) {
		val dummyInput = new Array[String](0)
		val cmdContext = new CommandContext(state, sessionId, slotNum, command, dummyInput)
		this.handleInitialActionHere(cmdContext) // state, sessionId, slotNum, command)
	}
	else {
	  if (this.myNextCommandHandler != null) {
		myNextCommandHandler.checkForInitialAction(state, sessionId, slotNum, command)
	  }
	}
  }
  
  private var myNextCommandHandler: AbstractLifterCommandHandler = null
  
  def setNextCommandHandler(handler: AbstractLifterCommandHandler) {
	myNextCommandHandler = handler
  }
  protected def getNextCommandHandler = myNextCommandHandler
  
  protected val matchingTokens: ArrayBuffer[String]
  protected def handleCommand(cmdContext : CommandContext) // state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String])
  // A blank method for handleInitialActionHere. If a command would like to perform tasks on rendering, it can override this method.
  protected def handleInitialActionHere(cmdContext : CommandContext) {} // state:LifterState, sessionId:String, slotNum:Int, command:String) {}
  
}
