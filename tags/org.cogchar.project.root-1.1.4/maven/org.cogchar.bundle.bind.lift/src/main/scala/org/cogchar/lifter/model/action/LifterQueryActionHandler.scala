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
import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.name.lifter.ActionStrings
import org.cogchar.lifter.model.main.{PageCommander}
import org.cogchar.impl.web.wire.{LifterState, SessionOrganizer, WebappCommander}
import org.cogchar.lifter.snippet.{LinkList, LinkListFactory}
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions.asScalaBuffer
import org.cogchar.impl.web.config.{LiftAmbassador}


// A handler for action URIs consisting of a Lifter query
class LifterQueryActionHandler(liftAmb : LiftAmbassador, val mySessOrg : SessionOrganizer, myWebappCmdr : WebappCommander ) extends AbstractLifterActionHandler(liftAmb) {

  override protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_lifterQuery)
  
  override  protected def handleAction(sessionId:String, slotNum:Int, control:WebControlImpl, input:Array[String]) {
	myLogger.warn("Lifter does not know how handle a lifter query as a triggered action in session {}, control []",
				  sessionId, slotNum)
  }
  
  override def optionalInitialRendering(sessionId:String, slotNum:Int, control:WebControlImpl) {
	control.controlType match {
	  case LinkListFactory.matchingName => {
		  val namesAndActionsList = myLiftAmbassador.getNamesAndActionsFromQuery(control.action)
		  val namesList = new ArrayBuffer[String]
		  val actionList = new ArrayBuffer[Ident]
		  namesAndActionsList.foreach{item =>
			namesList += item.getName
			actionList += item.getAction
		  }
		  val sessState = mySessOrg.getSessionState(sessionId)
		  sessState.multiActionsBySlot(slotNum) = actionList.toArray
		  val multiCtrl = LinkListFactory.makeMultiControlImpl(control.text, namesList.toArray, slotNum)
		  myWebappCmdr.setControl(sessionId, slotNum, multiCtrl)
	  }
	  case _ => {
		  myLogger.warn("Lifter does not know how to interpret a query as action for control type {}" +
			" in session {}, control {}", Array[AnyRef](control.controlType, sessionId, slotNum.asInstanceOf[AnyRef]))
	  }
	}
  }
  
}
