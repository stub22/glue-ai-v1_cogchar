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

package org.cogchar.lifter.model.control

import org.cogchar.impl.web.config.ControlConfig
import org.cogchar.impl.web.wire.{LifterState}
import org.cogchar.impl.web.util.LifterLogger

import scala.xml.NodeSeq

trait AbstractControlInitializationHandler extends LifterLogger {
  
  def processControlInit(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
	var result = NodeSeq.Empty
	if (this.matchingName equals control.controlType) {result = this.handleControlInit(state, sessionId, slotNum, control)}
	else {
	  if (this.nextHandler != null) {
		result = nextHandler.processControlInit(state, sessionId, slotNum, control)
	  } else {
		myLogger.warn("Reached end of control initialization chain without finding handler for sessionId {}" +
			 " and slotNum:{} with control type: {}", Array[AnyRef](sessionId, slotNum.asInstanceOf[AnyRef], control.controlType))
	  }
	}
	result
  }
  
  var nextHandler: AbstractControlInitializationHandler = null
  
  def setNextHandler(handler: AbstractControlInitializationHandler) {
	nextHandler = handler
  }
  
  protected val matchingName: String
  protected def handleControlInit(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq


}
