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
import org.cogchar.bind.lift.ControlConfig

trait AbstractControlInitializationHandler extends Logger {
  
  def processHandler(sessionId:String, slotNum:Int, control:ControlConfig) {
	if (this.matchingName equals control.controlType) {this.handleHere(sessionId, slotNum, control)}
	else {
	  if (this.nextHandler != null) {
		nextHandler.processHandler(sessionId, slotNum, control)
	  } else {
		warn("Reached end of control initialization chain without finding handler for sessionId:" + sessionId + " and slotNum:" + slotNum + " for control type: " + control.controlType) // Need to fix
	  }
	}
  }
  
  var nextHandler: AbstractControlInitializationHandler = null
  
  def setNextHandler(handler: AbstractControlInitializationHandler) {
	nextHandler = handler
  }
  
  protected val matchingName: String
  protected def handleHere(sessionId:String, slotNum:Int, control:ControlConfig)


}
