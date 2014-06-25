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

package org.cogchar.impl.web.wire

import org.cogchar.impl.web.util.HasLoggerConv
import org.cogchar.impl.web.config.SessionGroupManager

/**
 * @author Stu B. <www.texpedient.com>
 */

abstract class SessionOrganizer(val myCmdr : WebappCommander, val myWebappGlobalState : WebappGlobalState) 
		extends SessionGroupManager with HasLoggerConv {
			
	def getInitialConfigID : String = myWebappGlobalState.getSessionInitialConfigID

	private val mySessionGroupState = new LifterState(getInitialConfigID)
	def getSessionState(sessionId:String) = mySessionGroupState.stateBySession(sessionId)
	
	
	def initializeSession(sessionId:String) {
		info1("Initializing Session {}", sessionId)
		mySessionGroupState.initializeSession(sessionId)
	}
	  
	// This method clears the state info for a session from the state maps.
	// Performed on session shutdown via LiftSession.onShutdownSession (in Boot.scala)
	def removeSession(sessionId:String) {
		info1("Removing state for session {}", sessionId)
		mySessionGroupState.removeSession(sessionId)
	}
	// Get list of active sessionIds -- to control independent sessions from repo updates, but not sure how we'll
	// really want to handle that need. This is a temporary(?) idea:
	import collection.JavaConversions._
	override def getActiveSessions() : java.util.List[String] = {
		mySessionGroupState.activeSessions
	}
	
	def requestStart(sessionId:String) {
		if (mySessionGroupState.lifterInitialized) {
			initializeSession(sessionId)
		} else {
			// Some situations may result in requestStart being called more than once, so we need to check if it's already in buffer
			if (!(mySessionGroupState.sessionsAwaitingStart contains sessionId)) {
				mySessionGroupState.sessionsAwaitingStart += sessionId
			}
		}
	}
	override def getSessionVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		var contents:String = null
		val sessionVariables = getSessionState(sessionId).sessionLifterVariablesByName
		if (sessionVariables contains key) contents = sessionVariables(key)
		contents
	}	

	// ...because of the combination of our unique situation of having snippets invoked via
	// Comet and the fact PageCommander is a non session-aware object natively (so SessionVars don't work here), it's
	// necessary to expose the state needed to populate snippet invocations here:
		
	def hackIntoSnippetDataMap(sessionId:String) =   mySessionGroupState.getSnippetDataMapForSession(sessionId)

	def getCurrentTemplateForSession(sessionId:String) : String = {
		var templateToLoad: String = null
		val sessionState : WebSessionState = getSessionState(sessionId) 
		if (sessionState != null) {
			templateToLoad = sessionState.currentTemplateName
		}
		templateToLoad
	}
	
	def hasSession(sessionId: String) : Boolean = mySessionGroupState.stateBySession contains sessionId
	
	def hasActiveSession(sessionId: String) : Boolean = mySessionGroupState.activeSessions contains sessionId
	
	def initSession(sessionId: String) {
	// Confused:  Why do we call globalLifterVariablesByName.clear every time we load a liftConfig for a *session*?
	// We are doing this once for each session, right? 		
		// info("Loading LiftConfig for session {}", sessionId)
		val initConfigID = getInitialConfigID
		if (sessionId.equals(initConfigID)) {
			mySessionGroupState.clearAndInitializeState
			// globalLifterVariablesByName.clear
			myWebappGlobalState.clearAndInit
		} else { // otherwise reset maps for this session
			mySessionGroupState.prepareSessionForNewConfig(sessionId)
		}
	}
	
	def markSessionGroupInitFlag(flagVal : Boolean) { 
		mySessionGroupState.lifterInitialized = flagVal
	}
	def isSessionGroupStarted() : Boolean = mySessionGroupState.lifterInitialized
	
	def renderInitControlsOnAllSessions() {
		// TODO: reduce code bulk by creating a joined buffer
		// If this is a restart on config change, activeSessions will have sessions which need to be re-initialized
		mySessionGroupState.activeSessions.foreach(sessionId => myCmdr.initializeSessionAndRedirectToNewTemplate(sessionId))
		// If it's an initial startup, there may be sessions in sessionsAwaitingStart
		mySessionGroupState.sessionsAwaitingStart.foreach(sessionId => myCmdr.initializeSessionAndRedirectToNewTemplate(sessionId))
		mySessionGroupState.sessionsAwaitingStart.clear
	}
	import scala.collection.mutable.{Map,SynchronizedBuffer};	
	def getLastTimeAcutatedBySlot:Map[String, Map[Int,Long]] = mySessionGroupState.lastTimeAcutatedBySlot
/*
 * 		myWebappState.activeSessions.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))
		// If it's an initial startup, there may be sessions in sessionsAwaitingStart
		myWebappState.sessionsAwaitingStart.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))

 * 
 */		
import org.cogchar.impl.web.util.{WebHelper}
import org.cogchar.name.lifter.{ActionStrings}
import org.appdapter.core.name.Ident

def getStateFromVariable(sessionId:String, action:Ident): Boolean = {
	val actionUriPrefix = WebHelper.getUriPrefix(action);
	var state = false
	actionUriPrefix match {
	  case ActionStrings.p_liftvar => {
		  val mappedVarName = action.getLocalName();
		  if (myWebappGlobalState.hasGlobalVariable(mappedVarName)) {
			try {
			  state = myWebappGlobalState.getGlobalVariable(mappedVarName).toBoolean
			} catch {
			  case e: NumberFormatException => // just leave state false if not a boolean value
			}
		  }
		}
	  case ActionStrings.p_liftsessionvar => {
		  val mappedVariable = action.getLocalName();
		  val sessionState : WebSessionState = getSessionState(sessionId) 
		  val sessionVariables = sessionState.sessionLifterVariablesByName
		  if (sessionVariables contains mappedVariable) {
			try {
			  state = sessionVariables(mappedVariable).toBoolean
			} catch {
			  case e: NumberFormatException => // just leave state false if not a boolean value
			}
		  }
		}
	  case _ =>
	}
	state
  }	
	
}
