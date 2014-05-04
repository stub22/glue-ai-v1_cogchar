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

package org.cogchar.lifter.model.action

import org.appdapter.core.name.Ident
import org.cogchar.impl.web.config.ControlConfig
import org.cogchar.impl.web.util.LifterLogger
import org.cogchar.lifter.model.main.{PageCommander}
import org.cogchar.impl.web.wire.{LifterState}
import scala.collection.mutable.ArrayBuffer

trait AbstractLifterActionHandler extends LifterLogger {
  private var myNextActionHandler: AbstractLifterActionHandler = null
  protected val matchingPrefixes: ArrayBuffer[String]

	
  def processAction(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String]) {
	if (this.matchingPrefixes contains PageCommander.getUriPrefix(control.action)) {
      this.handleAction(state, sessionId, slotNum, control, input)}
	else {
	  if (this.myNextActionHandler != null) {
		myNextActionHandler.processAction(state, sessionId, slotNum, control, input)
	  } else {
		myLogger.warn("Reached end of action handling chain without finding handler for sessionId:{}" + 
					  " and slotNum:{} with action: {}", Array[AnyRef](sessionId, slotNum.asInstanceOf[AnyRef], control.action))
	  }
	}
  }
  
  // Checks for actions which this control performs upon rendering, not actuation
  def optionalInitialRendering(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig) {
	if (this.matchingPrefixes contains PageCommander.getUriPrefix(control.action)) {this.handleRendering(state, sessionId, slotNum, control)}
	else {
	  if (this.myNextActionHandler != null) {
		myNextActionHandler.optionalInitialRendering(state, sessionId, slotNum, control)
	  }
	}
  }
  
 
  def setNextActionHandler(handler: AbstractLifterActionHandler) {
	myNextActionHandler = handler
  }
  
  protected def handleAction(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String])
  // A blank method for handleInitialActionHere. If an action would like to perform tasks on rendering, it can override this method.
  protected def handleRendering(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig) {}
  
}
