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

import org.cogchar.api.web.{WebControl}

// import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.impl.web.wire.{LifterState}
import org.cogchar.impl.web.util.HasLoggerConv

import scala.xml.NodeSeq

trait AbstractControlSnippet extends HasLoggerConv {
  
  def generateOutputXml(sessionId:String, slotNum:Int, control:WebControl): NodeSeq = {
	var result = NodeSeq.Empty
	if (this.matchingName equals control.getType) {
		result = this.generateXmlForControl(sessionId, slotNum, control)
	}
	else {
	  if (this.nextHandler != null) {
		result = nextHandler.generateOutputXml(sessionId, slotNum, control)
	  } else {
		warn3("Reached end of ControlSnippet chain without finding handler for sessionId {}" +
			 " and slotNum:{} with control type: {}", sessionId, slotNum.asInstanceOf[AnyRef], control.getType)
	  }
	}
	result
  }
  
  var nextHandler: AbstractControlSnippet = null
  
  def setNextHandler(handler: AbstractControlSnippet) {
	nextHandler = handler
  }
  
  protected val matchingName: String
  protected def generateXmlForControl(sessionId:String, slotNum:Int, control:WebControl): NodeSeq


}
