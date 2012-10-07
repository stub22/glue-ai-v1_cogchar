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
	import org.cogchar.platform.trigger.DummyBinding
	
	// What do we think about this being an object and not a class?
	// Well, a Scala Object actually is automatically a static instance singleton anyhow, so no sense in trying to make it one manually.
	// It appears to be standard practice for LiftActors which provide features to all sessions
	// to be singleton Objects.
	// We might eventually want parts of PageCommander to be performed via a class of 
	// actors with an instance of that class created for each session. (PageCommander is currently acting as an actor
	// but also in some un-Actor-like ways.)
	object PageCommander extends LiftActor with ListenerManager with Logger {
	  
	  private var theLiftAmbassador:LiftAmbassador = null // Probably it makes sense to retain a pointer to the LiftAmbassador since it is used in several methods
	  
	  private var updateInfo: String = ""
	  
	  private val theLifterState = new LifterState
	  private val toggler = new ControlToggler
	  private val firstActionHandler = HandlerConfigurator.initializeActionHandlers
	  private val firstControlInitializationHandler = HandlerConfigurator.initializeControlInitializationHandlers
	  
	  def createUpdate = updateInfo
	  
	  def getNode(sessionId:String, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  nodeOut = theLifterState.controlsMap(sessionId)(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		//info("nodeOut for session " + sessionId + " and control " + controlId + " is " + nodeOut) // TEST ONLY
		nodeOut
	  }
	  
	  def getRequestedPage(sessionId:String) = {
		val pageToGet = theLifterState.requestedPage(sessionId)
		theLifterState.requestedPage(sessionId) = None // Once the page is read, the request is complete, so we set this back to Nothing
		pageToGet
	  }
	  
	  def getLiftAmbassador = {
		  if (theLiftAmbassador == null) {
			theLiftAmbassador = LiftAmbassador.getLiftAmbassador
		  }
		  theLiftAmbassador
	  }
	  
	  def getInitialConfigId = theLifterState.INITIAL_CONFIG_ID
	  
	  // This is sort of a funny deal and may need refactoring, but LifterVariableHandler needs to get this (for now at least)
	  // Could just have ControlToggler be a singleton object, which may be OK since it's stateless
	  def getToggler = {
		toggler
	  }
	  
	  def initializeSession(sessionId:String) {
		info("Initializing Session %s".format(sessionId))
		theLifterState.initializeSession(sessionId)
		updateListeners(controlId(sessionId, ActorCodes.TEMPLATE_CODE));
		setControlsFromMap(sessionId)
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
		  theLifterState.sessionsAwaitingStart.foreach(sessionId => initializeSession(sessionId))
		  theLifterState.sessionsAwaitingStart.clear
	  }
	  
	  def requestStart(sessionId:String) {
	   if (theLifterState.lifterInitialized) {
		  initializeSession(sessionId)
	   } else {
		 theLifterState.sessionsAwaitingStart += sessionId
	   }
	  }
									
	  def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session " + sessionId)
		if (sessionId.equals(theLifterState.INITIAL_CONFIG_ID)) {
		  theLifterState.clearState
		} else { // otherwise reset maps for this session
		  theLifterState.clearSession(sessionId)
		}
		
		theLifterState.currentConfig(sessionId) = liftConfig

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
			theLifterState.controlDefMap(sessionId)(slotNum) = new ControlConfig(controlDef) 
			// Trigger control initialization handler chain to fill proper XML into controlsMap
			theLifterState.controlsMap(sessionId)(slotNum) = getXmlForControl(sessionId, slotNum, controlDef)
			// Check for initial nee "local" actions which PageCommander needs to handle, such as text display
			firstActionHandler.checkForInitialAction(theLifterState, sessionId, slotNum, controlDef)
		  })
		// Blank unspecified slots (out to MAX_CONTROL_QUANTITY)
		for (slot <- 1 to theLifterState.MAX_CONTROL_QUANTITY) {
		  theLifterState.controlsMap(sessionId).putIfAbsent(slot, NodeSeq.Empty)
		}
		theLifterState.currentTemplate(sessionId) = liftConfig.template
		if (sessionId.equals(theLifterState.INITIAL_CONFIG_ID)) { 
		  renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
		  val changedTemplate = (theLifterState.currentTemplate(sessionId) != theLifterState.lastConfig(sessionId).template)
		  if (changedTemplate) {
			updateInfo = controlId(sessionId, ActorCodes.TEMPLATE_CODE)
			updateListeners;
		  }
		  // ... and load new controls
		  setControlsFromMap(sessionId)
		}
	  }
	  
	  def getXmlForControl(sessionId: String, slotNum:Int, controlDef:ControlConfig): NodeSeq = {
		firstControlInitializationHandler.processHandler(theLifterState, sessionId, slotNum, controlDef)
	  }
					  
	  def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		theLifterState.controlsMap(sessionId)(slotNum) = slotHtml 
		updateListeners(controlId(sessionId, slotNum))
	  }
	  
	  def setControlsFromMap(sessionId:String) {
		val slotIterator = theLifterState.controlsMap(sessionId).keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateListeners(controlId(sessionId, nextSlot))
		}
	  }							
	  
	  def handleAction(sessionId:String, formId:Int, input:Array[String]) {
		//info("Handling action: " + theLifterState.controlDefMap(sessionId)(formId).action) // TEST ONLY
		val processThread = new Thread(new Runnable { // A new thread to handle actions to make sure we don't block Ajax handling
			def run() {
			  firstActionHandler.processHandler(theLifterState, sessionId, formId, theLifterState.controlDefMap(sessionId)(formId), input)
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
	  
	  
	  import java.util.Date // needed for currently implemented "debouncing" function
	  final val IGNORE_BOUNCE_TIME = 200 //ms
	  // Maps controls with actions only (buttons) to action handlers
	  def triggerAction(sessionId:String, id: Int) {
		// Check last actuated time for this control, and ignore if it happened less than IGNORE_BOUNCE_TIME ago
		val time = new Date().getTime()
		var ignore = false;
		if (theLifterState.bounceMap(sessionId) contains id) {
		  if (time - theLifterState.bounceMap(sessionId)(id) < IGNORE_BOUNCE_TIME) {
			ignore = true;
		  }
		}
		theLifterState.bounceMap(sessionId)(id) = time;
		if (!ignore) {
		  if (theLifterState.toggleButtonMap(sessionId) contains id) {
			// Really we shouldn't run toggle on the Actor's thread, so we'll do this.
			// One of these days snippets will talk back to PageCommander as an Actor instead of calling into it, and the threading
			// will take care of itself instead of having to do things this messy way.
			val toggleThread = new Thread(new Runnable {
				def run() {
				  toggler.toggle(theLifterState,sessionId,id)
				  handleAction(sessionId, id, null) // Starts yet another thread, but we need it started by the toggleThread so it won't run until toggle is complete
				}
			  })
			toggleThread.start(); 
		  } else {
			handleAction(sessionId, id, null)
		  }
		} else {
		  warn("Debouncing control " + id + " in session + " + sessionId)
		}
	  }
	  
	  // Likely should go in different class...
	  def outputSpeech(sessionId:String, text: String) {
		theLifterState.outputSpeech(sessionId) = text
		updateInfo = controlId(sessionId, ActorCodes.SPEECH_OUT_CODE) // To tell JavaScriptActor we want Android devices to say the text
		updateListeners()
	  }
	  
	  // Likely should go in different class...
	  def acquireSpeech(sessionId:String, slotNum:Int) {
		updateInfo = controlId(sessionId, ActorCodes.SPEECH_REQUEST_CODE)
		theLifterState.lastSpeechReqSlotId(sessionId) = controlId(sessionId, slotNum); // Set this value - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
		updateListeners()
	  }
	  
	  // Likely should go in different class...
	  def requestContinuousSpeech(sessionId:String, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to " + desired + " for session " + sessionId)
		if (desired) {
		  theLifterState.lastSpeechReqSlotId(sessionId) = controlId(sessionId, slotNum)
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_START_CODE)
		  updateListeners()
		} else {
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_STOP_CODE)
		  updateListeners()
		}
	  }
	  
	  def getSpeechReqControl(sessionId:String) = theLifterState.lastSpeechReqSlotId(sessionId)
	  
	  def getCurrentTemplate(sessionId:String) = {
		var templateToLoad: String = null
		if (theLifterState.currentTemplate contains sessionId) templateToLoad = theLifterState.currentTemplate(sessionId)
		templateToLoad
	  }
	  
	  def getOutputSpeech(sessionId:String) = {
		var speechToOutput: String = ""
		if (theLifterState.outputSpeech contains sessionId) speechToOutput = theLifterState.outputSpeech(sessionId)
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
		  initFromCogcharRDF(theLifterState.INITIAL_CONFIG_ID, getLiftAmbassador.getInitialConfig)
		}
		def setConfigForSession(sessionId:String, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		def loadPage(sessionId:String, pagePath:String) {
		  theLifterState.requestedPage(sessionId) = Some(pagePath)
		  updateInfo = controlId(sessionId, ActorCodes.LOAD_PAGE_CODE)
		  updateListeners()
		}
		def getVariable(key:String): String = { // returns value from "public" (global) app variables map
		  var contents:String = null
		  if (theLifterState.publicAppVariablesMap contains key) contents = theLifterState.publicAppVariablesMap(key)
		  contents
		}
		def getVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		  var contents:String = null
		  if (theLifterState.appVariablesMap(sessionId) contains key) contents = theLifterState.appVariablesMap(sessionId)(key)
		  contents
		}
		// Show error globally
		def showError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText);
		  val activeSessionIterator = theLifterState.controlsMap.keysIterator
		  while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showError(errorSourceCode, errorText, sessionId)
		  }
		}
		// Show error in session
		def showError(errorSourceCode:String, errorText:String, sessionId:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText + "; session = " + sessionId);
		  if (theLifterState.errorMap contains sessionId) {
			  if (theLifterState.errorMap(sessionId) contains errorSourceCode) {
				val slotNum = theLifterState.errorMap(sessionId)(errorSourceCode)
				if (errorText.isEmpty) {
				  setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
				  setControl(sessionId, slotNum, TextBox.makeBox(errorText, theLifterState.controlDefMap(sessionId)(slotNum).style, true, false))
				}
			  }
		  }
		}
	  }
	
	}

  }
}

