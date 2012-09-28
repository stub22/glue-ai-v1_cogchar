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
import org.appdapter.core.name.Ident
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.PageCommander
import scala.collection.mutable.ArrayBuffer

trait AbstractLifterActionHandler extends Logger {
  
  def processHandler(sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String]) {
	if (this.matchingPrefixes contains PageCommander.getUriPrefix(control.action)) {this.handleHere(sessionId, slotNum, control, input)}
	else {
	  if (this.nextHandler != null) {
		nextHandler.processHandler(sessionId, slotNum, control, input)
	  } else {
		warn("Reached end of action handling chain without finding handler for sessionId:" + sessionId + " and slotNum:" + slotNum + " with action: " + control.action) // Need to fix
	  }
	}
  }
  
  // Checks for actions which this control performs upon rendering, not actuation
  def checkForInitialAction(sessionId:String, slotNum:Int, control:ControlConfig) {
	if (this.matchingPrefixes contains PageCommander.getUriPrefix(control.action)) {this.handleInitialActionHere(sessionId, slotNum, control)}
	else {
	  if (this.nextHandler != null) {
		nextHandler.checkForInitialAction(sessionId, slotNum, control)
	  }
	}
  }
  
  var nextHandler: AbstractLifterActionHandler = null
  
  def setNextHandler(handler: AbstractLifterActionHandler) {
	nextHandler = handler
  }
  
  protected val matchingPrefixes: ArrayBuffer[String]
  protected def handleHere(sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String])
  // A blank method for handleInitialActionHere. If an action would like to perform tasks on rendering, it can override this method.
  protected def handleInitialActionHere(sessionId:String, slotNum:Int, control:ControlConfig) {}
  
}
