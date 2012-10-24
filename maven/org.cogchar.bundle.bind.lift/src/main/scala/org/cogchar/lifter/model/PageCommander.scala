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

package org.cogchar.lifter {
  package model {

	import net.liftweb.common._
	import net.liftweb.http._
	import net.liftweb.util._
	import Helpers._
	import scala.xml._
	import _root_.net.liftweb.util.Log
	import net.liftweb.actor._
	import org.appdapter.core.name.{FreeIdent, Ident}
	import org.cogchar.lifter.model.handler.{HandlerConfigurator, LifterVariableHandler}
	import org.cogchar.lifter.lib._
	import org.cogchar.lifter.snippet._
	import org.cogchar.lifter.view._
	import org.cogchar.bind.lift._
	import scala.collection.JavaConverters._
	import concurrent.ops._
	// import org.cogchar.platform.trigger.CogcharActionBinding
	
	// What do we think about this being an object and not a class?
	// It appears to be standard practice for LiftActors which provide features to all sessions
	// to be singleton Objects.
	// We might eventually want parts of PageCommander to be performed via a class of 
	// actors with an instance of that class created for each session. (PageCommander is currently acting as an actor
	// but also in some un-Actor-like ways.)
	object PageCommander extends LiftActor with ListenerManager with Logger {
	  
	  private var theLiftAmbassador:LiftAmbassador = null // Probably it makes sense to retain a pointer to the LiftAmbassador since it is used in several methods
	  
	  private var updateInfo: String = ""
	  
	  private val theLifterState = new LifterState
	  private val firstActionHandler = HandlerConfigurator.initializeActionHandlers
	  private val firstControlInitializationHandler = HandlerConfigurator.initializeControlInitializationHandlers
	  
	  private def getSessionState(sessionId:String) = theLifterState.stateBySession(sessionId)
	  
	  def createUpdate = updateInfo
	  
	  case class HtmlPageRequest(sessionId:String, pagePathOption:Option[String])
	  case class SpeechOutRequest(sessionId:String, text:String)
	  case class SpeechInRequest(sessionId:String, slotNum:Int)
	  case class ContinuousSpeechInStartRequest(sessionId:String, slotNum:Int)
	  case class ContinuousSpeechInStopRequest(sessionId:String)
	  case class HtmlPageRefreshRequest(sessionId:String)
	  
	  def getNode(sessionId:String, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  nodeOut = getSessionState(sessionId).controlXmlBySlot(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		//info("nodeOut for session " + sessionId + " and control " + controlId + " is " + nodeOut) // TEST ONLY
		nodeOut
	  }
	  
	  def getLiftAmbassador = {
		  if (theLiftAmbassador == null) {
			theLiftAmbassador = LiftAmbassador.getLiftAmbassador
		  }
		  theLiftAmbassador
	  }
	  
	  def getInitialConfigId = theLifterState.INITIAL_CONFIG_ID
	  
	  def initializeSession(sessionId:String) {
		  info("Initializing Session %s".format(sessionId))
		  theLifterState.initializeSession(sessionId)
	  }
	  
	  // This method clears the state info for a session from the state maps.
	  // Performed on session shutdown via LiftSession.onShutdownSession (in Boot.scala)
	  def removeSession(sessionId:String) {
		info("Removing state for session " + sessionId)
		theLifterState.removeSession(sessionId)
	  }
	  
	  def renderInitialControls {
		  theLifterState.lifterInitialized = true
		  // If this is a restart on config change, activeSessions will have sessions which need to be re-initialized
		  theLifterState.activeSessions.foreach(sessionId => initializeSession(sessionId))
		  theLifterState.sessionsAwaitingStart.foreach(sessionId => {
			  initializeSession(sessionId);
			  updateListeners(HtmlPageRequest(sessionId, Some(getCurrentTemplate(sessionId))))
			  setControlsFromMap(sessionId)
			})
		  theLifterState.sessionsAwaitingStart.clear
	  }
	  
	  def requestStart(sessionId:String) {
	   if (theLifterState.lifterInitialized) {
		 initializeSession(sessionId)
	   } else {
		 // Some situations may result in requestStart being called more than once, so we need to check if it's already in buffer
		 if (!(theLifterState.sessionsAwaitingStart contains sessionId)) {
		   theLifterState.sessionsAwaitingStart += sessionId
		 }
	   }
	  }
	  
	  // This method is used by templates to check if session is active on initial render. If not, session should
	  // be initialized and the caller notified it wasn't previously active by returning a false result
	  def checkForActiveSessionAndStartIfNot(sessionId:String):Boolean = {
		var sessionActive = true
		if (!(theLifterState.activeSessions contains sessionId)) {
		  sessionActive = false
		  requestStart(sessionId) // Needs a little more refactoring between this and requestStart
		  if (theLifterState.lifterInitialized) {
			setControlsFromMap(sessionId)
		  }
		} 
		return sessionActive
	  }
									
	  def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session " + sessionId)
		if (sessionId.equals(theLifterState.INITIAL_CONFIG_ID)) {
		  theLifterState.clearAndInitializeState
		} else { // otherwise reset maps for this session
		  theLifterState.prepareSessionForNewConfig(sessionId)
		}
		
		val sessionState = getSessionState(sessionId)
		sessionState.currentLiftConfig = liftConfig

		val controlList: java.util.List[ControlConfig] = liftConfig.myCCs

		val controlSet = controlList.asScala.toSet
		controlSet.foreach(controlDef => {
			var slotNum:Int = -1
			try {
			  val finalSplitterIndex = controlDef.myURI_Fragment.lastIndexOf("_")
			  slotNum = controlDef.myURI_Fragment.splitAt(finalSplitterIndex+1)._2.toInt
			} catch {
			  case _: Any =>  warn("Unable to get valid slotNum from loaded control; URI fragment was " + controlDef.myURI_Fragment) // The control will still be loaded into slot -1; could "break" here but it's messy and unnecessary
			}
			if (slotNum > theLifterState.MAX_CONTROL_QUANTITY) {
			  warn("Maximum number of controls exceeded (" + theLifterState.MAX_CONTROL_QUANTITY + "); some controls may not be cleared upon page change!")
			  warn("MAX_CONTROL_QUANTITY in LifterState can be increased if this is necessary.")
			}
			// Below, we clone the controlDef with a copy constructor so the ControlConfigs in the controlDefMap are 
			// not the same objects as in LiftAmbassador's page cache.
			// That's important largly because ToggleButton modifies the actions in the controlDefMap.
			// Pretty darn messy, and likely a topic for further refactoring.
			sessionState.controlConfigBySlot(slotNum) = new ControlConfig(controlDef) 
			// Trigger control initialization handler chain to fill proper XML into controlsMap
			sessionState.controlXmlBySlot(slotNum) = getXmlForControl(sessionId, slotNum, controlDef)
			// Check for initial nee "local" actions which PageCommander needs to handle, such as text display
			firstActionHandler.checkForInitialAction(theLifterState, sessionId, slotNum, controlDef)
		  })
		// Blank unspecified slots (out to MAX_CONTROL_QUANTITY)
		for (slot <- 1 to theLifterState.MAX_CONTROL_QUANTITY) {
		  sessionState.controlXmlBySlot.putIfAbsent(slot, NodeSeq.Empty)
		}
		sessionState.currentTemplateName = liftConfig.template
		if (sessionId.equals(theLifterState.INITIAL_CONFIG_ID)) { 
		  renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
		  val changedTemplate = !sessionState.currentTemplateName.equals(sessionState.lastLiftConfig.template)
		  // ... load new template if necessary
		  if (changedTemplate) {
			updateListeners(HtmlPageRequest(sessionId, Some(getCurrentTemplate(sessionId))))
		  } 
		  // ... load new controls
		  setControlsFromMap(sessionId)
		}
	  }
	  
	  def getXmlForControl(sessionId: String, slotNum:Int, controlDef:ControlConfig): NodeSeq = {
		firstControlInitializationHandler.processHandler(theLifterState, sessionId, slotNum, controlDef)
	  }
					  
	  def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		getSessionState(sessionId).controlXmlBySlot(slotNum) = slotHtml 
		updateListeners(controlId(sessionId, slotNum))
	  }
	  
	  def setControlsFromMap(sessionId:String) {
		val slotIterator = getSessionState(sessionId).controlXmlBySlot.keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateListeners(controlId(sessionId, nextSlot))
		}
	  }							
	  
	  def handleAction(sessionId:String, formId:Int, input:Array[String]) {
		//info("Handling action: " + getSessionState(sessionId).controlConfigBySlot(formId).action) // TEST ONLY
		spawn { // A new thread to handle actions to make sure we don't block Ajax handling
		  // Eventually this thread will no longer be needed since controls should call into PageCommander as an actor
		  firstActionHandler.processHandler(theLifterState, sessionId, formId, 
											getSessionState(sessionId).controlConfigBySlot(formId), input)
		}
	  }
	    
	  // Maps controls with multiple possible selections to action handlers
	  def multiSelectControlActionMapper(sessionId:String, formId:Int, subControl:Int) {
		val input:Array[String] = Array(ActionStrings.subControlIdentifier + subControl.toString)
		handleAction(sessionId, formId, input)
	  }
	  
	  // Maps controls with a text input to action handlers
	  def textInputMapper(sessionId:String, formId:Int, text:String) {
		val input:Array[String] = Array(text)
		handleAction(sessionId, formId, input)
	  }
	  
	  // Maps controls with multiple text inputs to action handlers
	  def multiTextInputMapper(sessionId:String, formId:Int, text:Array[String]) {
		handleAction(sessionId, formId, text)
	  }
	  
	  
	  import java.util.Date // needed for currently implemented "debouncing" function
	  // Maps controls with actions only (buttons) to action handlers
	  def triggerAction(sessionId:String, id: Int) {
		// Check last actuated time for this control, and ignore if it happened less than IGNORE_BOUNCE_TIME ago
		val time = new Date().getTime()
		val ignore = checkForBounce(sessionId, id, time)
		if (!ignore) {
		  if (getSessionState(sessionId).toggleControlStateBySlot contains id) {
			// Really we shouldn't run toggle on the Actor's thread, so we'll do this.
			// One of these days snippets will talk back to PageCommander as an Actor instead of calling into it, and the threading
			// will take care of itself instead of having to do things this messy way.
			spawn{
			  ControlToggler.getTheToggler.toggle(theLifterState,sessionId,id)
			  handleAction(sessionId, id, null) // Starts yet another thread, but we need it started by the toggleThread so it won't run until toggle is complete
			}
		  } else {
			handleAction(sessionId, id, null)
		  }
		}
	  }
	  
	  val bounceCheckLock: Object = new Object()
	  final val IGNORE_BOUNCE_TIME = 250 //ms
	  def checkForBounce(sessionId:String, id:Int, time:Long): Boolean = {
		bounceCheckLock.synchronized {
		  val bounceMap = getSessionState(sessionId).lastTimeAcutatedBySlot
		  var ignore = false
		  if (bounceMap contains id) {
			if ((time - bounceMap(id)) < IGNORE_BOUNCE_TIME) {
			  ignore = true;
			  warn("Debouncing control " + id + " in session " + sessionId)
			}
		  }
		  bounceMap(id) = time
		  ignore
		}
	  }
	  
	  // Likely should go in different class...
	  def outputSpeech(sessionId:String, text: String) {
		updateListeners(SpeechOutRequest(sessionId, text)) // To tell JavaScriptActor we want Android devices to say the text
	  }
	  
	  // Likely should go in different class...
	  def acquireSpeech(sessionId:String, slotNum:Int) {
		updateListeners(SpeechInRequest(sessionId, slotNum)); // Send this message - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
	  }
	  
	  // Likely should go in different class...
	  def requestContinuousSpeech(sessionId:String, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to " + desired + " for session " + sessionId)
		if (desired) {
		  updateListeners(ContinuousSpeechInStartRequest(sessionId, slotNum))
		} else {
		  updateListeners(ContinuousSpeechInStopRequest(sessionId))
		}
	  }
	  
	  def getCurrentTemplate(sessionId:String) = {
		var templateToLoad: String = null
		val sessionState = theLifterState.stateBySession
		if (sessionState contains sessionId) templateToLoad = sessionState(sessionId).currentTemplateName
		templateToLoad
	  }
	  
	  // A lot of these "helper" methods below likely belong in separate class:
	  
	  // I think Ident should include this method, but since it doesn't...
	  def getUriPrefix(uri: Ident) : String = {
		uri.getAbsUriString.stripSuffix(uri.getLocalName)
	  }
																	  
	  def controlId(sessionId:String, controlId: Int): String = {
		sessionId + "_" + controlId
	  }
	  
	  var theMessenger: CogcharMessenger = null
	
	  def getMessenger: LiftAmbassador.LiftInterface = { 
		if (theMessenger == null) {
		  theMessenger = new CogcharMessenger
		}
		theMessenger
	  }

	  class CogcharMessenger extends LiftAmbassador.LiftInterface {
		def notifyConfigReady {
		  initFromCogcharRDF(theLifterState.INITIAL_CONFIG_ID, getLiftAmbassador.getInitialConfig)
		}
		def setConfigForSession(sessionId:String, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		def setControlForSessionAndSlot(sessionId:String, slotNum:Int, newConfig:ControlConfig) {
		  val newControlXml = getXmlForControl(sessionId, slotNum, newConfig)
		  setControl(sessionId, slotNum, newControlXml)
		}
		def loadPage(sessionId:String, pagePath:String) {
		  updateListeners(HtmlPageRequest(sessionId, Some(pagePath)))
		}
		def getVariable(key:String): String = { // returns value from "public" (global) app variables map
		  var contents:String = null
		  val globalVariables = theLifterState.globalLifterVariablesByName
		  if (globalVariables contains key) contents = globalVariables(key)
		  contents
		}
		def getVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		  var contents:String = null
		  val sessionVariables = getSessionState(sessionId).sessionLifterVariablesByName
		  if (sessionVariables contains key) contents = sessionVariables(key)
		  contents
		}
		// Show error globally
		def showError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText);
		  val activeSessionIterator = theLifterState.activeSessions.iterator
		  while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showError(errorSourceCode, errorText, sessionId)
		  }
		}
		// Show error in session
		def showError(errorSourceCode:String, errorText:String, sessionId:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText + "; session = " + sessionId);
		  if (theLifterState.stateBySession contains sessionId) {
			  val sessionErrorMap = getSessionState(sessionId).errorDisplaySlotsByType
			  if (sessionErrorMap contains errorSourceCode) {
				val slotNum = sessionErrorMap(errorSourceCode)
				if (errorText.isEmpty) {
				  setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
				  setControl(sessionId, slotNum, TextBox.makeBox(errorText, 
							getSessionState(sessionId).controlConfigBySlot(slotNum).style, true, false))
				}
			  }
		  }
		}
	  }
	
	}

  }
}

