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
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.name.lifter.ActionStrings
import org.cogchar.lifter.model.main.{LifterState,PageCommander}
import org.cogchar.lifter.snippet.LinkList
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions.asScalaBuffer

// A handler for action URIs consisting of a Lifter query
class LifterQueryActionHandler extends AbstractLifterActionHandler {

  override protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_lifterQuery)
  
  override  protected def handleAction(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig, input:Array[String]) {
	myLogger.warn("Lifter does not know how handle a lifter query as a triggered action in session {}, control []",
				  sessionId, slotNum)
  }
  
  override def optionalInitialRendering(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig) {
	control.controlType match {
	  case LinkList.matchingName => {
		  val namesAndActionsList = PageCommander.getLiftAmbassador.getNamesAndActionsFromQuery(control.action)
		  val namesList = new ArrayBuffer[String]
		  val actionList = new ArrayBuffer[Ident]
		  namesAndActionsList.foreach{item =>
			namesList += item.getName
			actionList += item.getAction
		  }
		  state.stateBySession(sessionId).multiActionsBySlot(slotNum) = actionList.toArray
		  PageCommander.setControl(sessionId, slotNum, LinkList.makeMultiControl(state, sessionId, slotNum, control.text, namesList.toArray))
	  }
	  case _ => {
		  myLogger.warn("Lifter does not know how to interpret a query as action for control type {}" +
			" in session {}, control {}", Array[AnyRef](control.controlType, sessionId, slotNum.asInstanceOf[AnyRef]))
	  }
	}
  }
  
}
