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
	import net.liftweb.http.js.JE._
	import net.liftweb.http.js.JsCmds
	import net.liftweb.http.js.JsCmds._
	import net.liftweb.http._
	import S._
	import net.liftweb.http.SHtml._
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
	import org.cogchar.platform.trigger.DummyBinding
	import java.util.concurrent.{Executors, TimeUnit}
	
	// What do we think about this being an object and not a class?
	// Well, a Scala Object actually is automatically a static instance singleton anyhow, so no sense in trying to make it one manually.
	// It appears to be standard practice for LiftActors which provide features to all sessions
	// to be singleton Objects.
	// We might eventually want parts of PageCommander to be performed via a class of 
	// actors with an instance of that class created for each session. (PageCommander is currently acting as an actor
	// but also in some un-Actor-like ways.)
	object PageCommander extends LiftActor with ListenerManager with Logger {

	  private var theLiftAmbassador:LiftAmbassador = null // Probably it makes sense to retain a pointer to the LiftAmbassador since it is used in several methods.
	  
	  private var updateInfo: String = ""
	  
	  private val theLifterState = new LifterState
	  private val toggler = new ControlToggler
	  private val firstActionHandler = HandlerConfigurator.initializeActionHandlers
	  private val firstControlInitializationHandler = HandlerConfigurator.initializeControlInitializationHandlers
	  
	  def createUpdate = updateInfo
	  
	  def getNode(sessionId:String, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  nodeOut = getState.controlsMap(sessionId)(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		//info("nodeOut for session " + sessionId + " and control " + controlId + " is " + nodeOut) // TEST ONLY
		nodeOut
	  }
	  
	  def getRequestedPage(sessionId:String) = {
		val pageToGet = getState.requestedPage(sessionId)
		getState.requestedPage(sessionId) = None // Once the page is read, the request is complete, so we set this back to Nothing
		pageToGet
	  }
	  
	  def getLiftAmbassador = {
		  if (theLiftAmbassador == null) {
			theLiftAmbassador = LiftAmbassador.getLiftAmbassador
		  }
		  theLiftAmbassador
	  }
	  
	  // This is sort of a funny deal and may need refactoring, but LifterVariableHandler needs to get this (for now at least)
	  // Could just have ControlToggler be a singleton object, which may be OK since it's stateless
	  def getToggler = {
		toggler
	  }
	  
	  def getState = theLifterState
	  
	  def initializeSession(sessionId:String) {
		info("Initializing Session " + sessionId) 
		getState.initializeSession(sessionId)
		updateListeners(controlId(sessionId, ActorCodes.TEMPLATE_CODE));
		setControlsFromMap(sessionId)
	  }
	  
	  /* Not implemented yet:
	  // This method clears the state info for a session from the state maps. May be desirable to perform on session shutdown.
	  def clearSessionInfo(sessionId:String) {
		
	  }
	  */
	  
	  def renderInitialControls {
		val appState = getState
		if (!appState.lifterInitialized) {
		  appState.sessionsAwaitingStart.foreach(sessionId => initializeSession(sessionId))
		  appState.sessionsAwaitingStart.clear
		  appState.lifterInitialized = true
		} else { // if lifterInitialized, this is a restart on config change
		  appState.activeSessions.foreach(sessionId => initializeSession(sessionId))
		}
	  }
	  
	  def requestStart(sessionId:String) {
		val appState = getState
	   if (appState.lifterInitialized) {
		 // If the session is in activeSessions, a timed-out session may be reconnecting.
		 // Don't re-initialize and clear state (if this continues to be what we want).
		 // This may not be necessary: it seems after a genuine time-out, a new connection from the same browser gets a different ID?
		 if (!(appState.activeSessions contains sessionId)) {
		    initializeSession(sessionId)
		 }
	   } else {
		 appState.sessionsAwaitingStart += sessionId
	   }
	  }
									
	  def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session " + sessionId)
		val appState = getState
		if (sessionId.equals(appState.INITIAL_CONFIG_ID)) {
		  appState.clearState
		} else { // otherwise reset maps for this session
		  appState.clearSession(sessionId)
		}
		
		appState.currentConfig(sessionId) = liftConfig

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
			// Below, we clone the controlDef with a copy constructor so the ControlConfigs in the controlDefMap are 
			// not the same objects as in LiftAmbassador's page cache.
			// That's important largly because ToggleButton modifies the actions in the controlDefMap.
			// Pretty darn messy, and likely a topic for further refactoring.
			appState.controlDefMap(sessionId)(slotNum) = new ControlConfig(controlDef) 
			// Trigger control initialization handler chain to fill proper XML into controlsMap
			appState.controlsMap(sessionId)(slotNum) = getXmlForControl(sessionId, slotNum, controlDef)
			// Check for initial nee "local" actions which PageCommander needs to handle, such as text display
			firstActionHandler.checkForInitialAction(sessionId, slotNum, controlDef)
		  })
		// Blank unspecified slots (out to 20)
		for (slot <- 1 to 20) {
		  if (!(appState.controlDefMap(sessionId) contains slot)) {
			appState.controlsMap(sessionId)(slot) = NodeSeq.Empty
		  }
		}
		appState.currentTemplate(sessionId) = liftConfig.template
		if (sessionId.equals(appState.INITIAL_CONFIG_ID)) { 
		  renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
		  val changedTemplate = (appState.currentTemplate(sessionId) != appState.lastConfig(sessionId).template)
		  if (changedTemplate) {
			updateInfo = controlId(sessionId, ActorCodes.TEMPLATE_CODE)
			updateListeners;
		  }
		  // ... and load new controls
		  setControlsFromMap(sessionId)
		}
	  }
	  
	  def getXmlForControl(sessionId: String, slotNum:Int, controlDef:ControlConfig): NodeSeq = {
		firstControlInitializationHandler.processHandler(sessionId, slotNum, controlDef)
	  }
					  
	  def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		getState.controlsMap(sessionId)(slotNum) = slotHtml 
		updateListeners(controlId(sessionId, slotNum))
	  }
	  
	  def setControlsFromMap(sessionId:String) {
		val slotIterator = getState.controlsMap(sessionId).keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateListeners(controlId(sessionId, nextSlot))
		}
	  }							
	  
	  def handleAction(sessionId:String, formId:Int, input:Array[String]) {
		//info("Handling action: " + getState.controlDefMap(sessionId)(formId).action) // TEST ONLY
		val processThread = new Thread(new Runnable { // A new thread to handle actions to make sure we don't block Ajax handling
			def run() {
			  firstActionHandler.processHandler(sessionId, formId, getState.controlDefMap(sessionId)(formId), input)
			}
		  })
		processThread.start();
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
	  
	  // Maps controls with actions only (buttons) to action handlers
	  def triggerAction(sessionId:String, id: Int) {
		if (getState.toggleButtonMap(sessionId) contains id) {
		  // Really we shouldn't run toggle on the Actor's thread, so we'll do this.
		  // One of these days snippets will talk back to PageCommander as an Actor instead of calling into it, and the threading
		  // will take care of itself instead of having to do things this messy way.
		  val toggleThread = new Thread(new Runnable {
			def run() {
			  toggler.toggle(sessionId,id)
			  handleAction(sessionId, id, null) // Starts yet another thread, but we need it started by the toggleThread so it won't run until toggle is complete
			}
		  })
		  toggleThread.start(); 
		} else {
		  handleAction(sessionId, id, null)
		}
	  }
	  
	  // Likely should go in different class...
	  def outputSpeech(sessionId:String, text: String) {
		getState.outputSpeech(sessionId) = text
		updateInfo = controlId(sessionId, ActorCodes.SPEECH_OUT_CODE) // To tell JavaScriptActor we want Android devices to say the text
		updateListeners()
	  }
	  
	  // Likely should go in different class...
	  def acquireSpeech(sessionId:String, slotNum:Int) {
		updateInfo = controlId(sessionId, ActorCodes.SPEECH_REQUEST_CODE)
		getState.lastSpeechReqSlotId = controlId(sessionId, slotNum); // Set this field - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
		updateListeners()
	  }
	  
	  // Likely should go in different class...
	  def requestContinuousSpeech(sessionId:String, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to " + desired + " for session " + sessionId)
		if (desired) {
		  getState.lastSpeechReqSlotId = controlId(sessionId, slotNum)
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_START_CODE)
		  updateListeners()
		} else {
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_STOP_CODE)
		  updateListeners()
		}
	  }
	  
	  def getSpeechReqControl = getState.lastSpeechReqSlotId
	  
	  def getCurrentTemplate(sessionId:String) = {
		var templateToLoad: String = null
		if (getState.currentTemplate contains sessionId) templateToLoad = getState.currentTemplate(sessionId)
		templateToLoad
	  }
	  
	  def getOutputSpeech(sessionId:String) = {
		var speechToOutput: String = ""
		if (getState.outputSpeech contains sessionId) speechToOutput = getState.outputSpeech(sessionId)
		speechToOutput
	  }
	  
	  
	  // A lot of these "helper" methods below likely belong in separate class:
	  
	  // I think Ident should include this method, but since it doesn't...
	  def getUriPrefix(uri: Ident) : String = {
		uri.getAbsUriString.stripSuffix(uri.getLocalName)
	  }
																	  
	  def controlId(sessionId:String, controlId: Int): String = {
		sessionId + "_" + controlId
	  }
	   
	  /* We don't support lift config Turtle files now after making the switch to action URIs, unless we want to 
	   * bring back that capability. See org.cogchar.bind.lift.ControlConfig for more info
	   def reconfigureControlsFromRdf(rdfFile:String) = {
	   getLiftAmbassador.activateControlsFromRdf(rdfFile)
	   }
	   */
	  
	  var theMessenger: CogcharMessenger = null
	
	  def getMessenger: LiftAmbassador.LiftInterface = { 
		if (theMessenger == null) {
		  theMessenger = new CogcharMessenger
		}
		theMessenger
	  }

	  class CogcharMessenger extends LiftAmbassador.LiftInterface {
		def notifyConfigReady {
		  initFromCogcharRDF(getState.INITIAL_CONFIG_ID, getLiftAmbassador.getInitialConfig)
		}
		def setConfigForSession(sessionId:String, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		def loadPage(sessionId:String, pagePath:String) {
		  getState.requestedPage(sessionId) = Some(pagePath)
		  updateInfo = controlId(sessionId, ActorCodes.LOAD_PAGE_CODE)
		  updateListeners()
		}
		def getVariable(key:String): String = { // returns value from "public" (global) app variables map
		  var contents:String = null
		  if (getState.publicAppVariablesMap contains key) contents = getState.publicAppVariablesMap(key)
		  contents
		}
		def getVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		  var contents:String = null
		  if (getState.appVariablesMap(sessionId) contains key) contents = getState.appVariablesMap(sessionId)(key)
		  contents
		}
		// Show error globally
		def showError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText);
		  val activeSessionIterator = getState.controlsMap.keysIterator
		  while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showError(errorSourceCode, errorText, sessionId)
		  }
		}
		// Show error in session
		def showError(errorSourceCode:String, errorText:String, sessionId:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText + "; session = " + sessionId);
		  if (getState.errorMap contains sessionId) {
			  if (getState.errorMap(sessionId) contains errorSourceCode) {
				val slotNum = getState.errorMap(sessionId)(errorSourceCode)
				if (errorText.isEmpty) {
				  setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
				  setControl(sessionId, slotNum, TextBox.makeBox(errorText, getState.controlDefMap(sessionId)(slotNum).style, true, false))
				}
			  }
		  }
		}
	  }
	
	}

  }
}

