/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.lifter.model.main
import org.cogchar.name.lifter.{ActionStrings}

import net.liftweb.http.ListenerManager
import scala.xml.NodeSeq
import net.liftweb.actor.LiftActor
import org.appdapter.core.name.{FreeIdent, Ident}
import org.cogchar.impl.web.util.HasLogger
import org.cogchar.impl.web.wire.{LifterState, WebSessionState, SessionOrganizer, WebappGlobalState, WebappCommander}
import org.cogchar.lifter.model.handler.{HandlerConfigurator}
import org.cogchar.lifter.model.action.{AbstractLifterActionHandler, LifterVariableHandler}
import org.cogchar.lifter.model.control.{AbstractControlInitializationHandler}
import org.cogchar.lifter.view.TextBoxFactory
import org.cogchar.api.web.{WebControl}
import org.cogchar.impl.web.config.{WebControlImpl, LiftAmbassador, LiftConfig, WebInstanceGlob}
import scala.collection.JavaConverters._
/**

 */

class CogcharMessenger(cmdr : WebappCommander, wgs : WebappGlobalState) extends SessionOrganizer(cmdr, wgs) 
			with WebInstanceGlob with HasLogger {
	
	def info(msg: String, params: Any*) {
		myLogger.info(msg, params.map(_.asInstanceOf[Object]).toArray)
	}        
    
	override def notifyConfigReady {
		myCmdr.initFromCogcharRDF(myCmdr.getInitialConfigId, myCmdr.getInitConfig)
	}
	override def setConfigForSession(sessionId:String, config:LiftConfig) {
		myCmdr.initFromCogcharRDF(sessionId, config)
	}
	override def setControlForSessionAndSlot(sessionId:String, slotNum:Int, webControl:WebControl) {
		// Set control on display
		val newControlXml = myCmdr.getXmlForControl(sessionId, slotNum, webControl)
		myCmdr.setControl(sessionId, slotNum, newControlXml)
		// Write control to state
		myCmdr.loadControlDefToState(sessionId, slotNum, webControl)
	}
	override def loadPage(sessionId:String, pagePath:String) {
		myCmdr.exposedUpdateListeners(HtmlPageRequest(sessionId, Some(pagePath)))
	}
	override def getGlobalVariable(key : String) : String = myWebappGlobalState.getGlobalVariable(key)

	// Show error globally
	override def showGlobalError(errorSourceCode:String, errorText:String) {
		info("In showError; code = {}; text = {}", errorSourceCode, errorText);
		val activeSessionIterator = getActiveSessions.iterator
		while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showSessionError(errorSourceCode, errorText, sessionId)
		}
	}

	// Show error in session
	override def showSessionError(errorSourceCode:String, errorText:String, sessionId:String) {
		info("In showError; code = {}; text = {}; session = {}", Array[AnyRef](errorSourceCode, errorText, sessionId));
		
		if (hasSession(sessionId)) {
			val wsState : WebSessionState = getSessionState(sessionId)
			val sessionErrorMap = wsState.errorDisplaySlotsByType
			if (sessionErrorMap contains errorSourceCode) {
				val slotNum = sessionErrorMap(errorSourceCode)
				if (errorText.isEmpty) {
					myCmdr.setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
					myCmdr.setControl(sessionId, slotNum, TextBoxFactory.makeBox(errorText, 
									   wsState.controlConfigBySlot(slotNum).style, true, false))
				}
			}
		}
	}

}
