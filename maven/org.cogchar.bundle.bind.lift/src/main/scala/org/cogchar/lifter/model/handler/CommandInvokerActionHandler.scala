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

import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.impl.web.config.{LiftAmbassador}
import org.cogchar.name.lifter.ActionStrings

import org.cogchar.impl.web.wire.{LifterState, SessionOrganizer}
import scala.collection.mutable.ArrayBuffer

import org.cogchar.lifter.model.command.AbstractLifterCommandHandler;
import org.cogchar.lifter.model.action.AbstractLifterActionHandler;


/* This tricky little devil is taking responsibility for invoking the entire command chain.
	So, as written, he needs this nasty little import
*/
class CommandInvokerActionHandler(liftAmb: LiftAmbassador, mySessOrg : SessionOrganizer) extends AbstractLifterActionHandler(liftAmb) {
  
  override protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_liftcmd)
  
  private var myFirstCommandHandler:AbstractLifterCommandHandler = HandlerConfigurator.initializeCommandHandlers
  
  override protected def handleAction(sessionId:String, slotNum:Int, control:WebControlImpl, input:Array[String]) {
		myFirstCommandHandler.processCommand(mySessOrg, sessionId, slotNum, control.action.getLocalName, input)
  }
  
	override protected def handleRendering(sessionId:String, slotNum:Int, control:WebControlImpl) {
		myFirstCommandHandler.checkForInitialAction(mySessOrg, sessionId, slotNum, control.action.getLocalName)
  }
  
}
