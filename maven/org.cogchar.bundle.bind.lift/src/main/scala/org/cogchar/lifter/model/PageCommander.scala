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
	  
	  private val theLifterState = new LifterState()
	  private val firstActionHandler = HandlerConfigurator.initializeActionHandlers
	  
	  def createUpdate = updateInfo
	  
	  // A list of possible control types -- maybe should be in own class?
	  object ControlType extends Enumeration { 
		type ControlType = Value
		val NULLTYPE, PUSHYBUTTON, TEXTINPUT, DUALTEXTINPUT, LOGINFORM, SELECTBOXES, RADIOBUTTONS, LISTBOX, VIDEOBOX, TOGGLEBUTTON, TEXTBOX = Value
	  }
	  import ControlType._
	  
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
			initSingleControl(controlDef, slotNum, sessionId)
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
	  
	  // Really want to refactor this so that new control types don't require updates to this method or anywhere
	  // else in PageCommander...
	  def initSingleControl(controlDef:ControlConfig, slotNum:Int, sessionId:String) {
		val appState = getState
		val controlType = getControlType(controlDef)
		val action = controlDef.action
		val text = controlDef.text
		val style = controlDef.style
		val resource = controlDef.resource
			
		controlType match {
		  case ControlType.PUSHYBUTTON => {
			  appState.controlsMap(sessionId)(slotNum) = PushyButton.makeButton(text, style, resource, slotNum)
			}
		  case ControlType.TEXTINPUT => {
			  appState.controlsMap(sessionId)(slotNum) = TextForm.makeTextForm(text, slotNum)
			}
		  case ControlType.DUALTEXTINPUT => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  appState.controlsMap(sessionId)(slotNum) = DualTextForm.makeForm(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.LOGINFORM => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  appState.controlsMap(sessionId)(slotNum) = LoginForm.makeForm(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.SELECTBOXES => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest checkbox labels
			  val textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  appState.controlsMap(sessionId)(slotNum) = SelectBoxes.makeSelectBoxes(titleText, labelItems, slotNum)
			}
		  case ControlType.RADIOBUTTONS => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  appState.controlsMap(sessionId)(slotNum) = RadioButtons.makeRadioButtons(titleText, labelItems, slotNum)
			}
		  case ControlType.LISTBOX => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  appState.controlsMap(sessionId)(slotNum) = ListBox.makeListBox(titleText, labelItems, slotNum)
			}
		  case ControlType.VIDEOBOX => {
			  appState.controlsMap(sessionId)(slotNum) = VideoBox.makeBox(resource, true)
			}
		  case ControlType.TOGGLEBUTTON => {
			  // For a ToggleButton, the first item in CSV text, action, style, image corresponds to the default condition, the second to the "toggled" condition
			  var textItems = List.fromArray(text.split(ActionStrings.stringAttributeSeparator))
			  var styleItems = List.fromArray(style.split(ActionStrings.stringAttributeSeparator))
			  var resourceItems = List.fromArray(resource.split(ActionStrings.stringAttributeSeparator))
			  var actionItems = List.fromArray(action.getLocalName.split(ActionStrings.multiCommandSeparator))
			  appState.toggleButtonFullActionMap(sessionId)(slotNum) = action
			  // Next we need to see if an app variable linked to this toggle button is already set and set the button state to match if so
			  val buttonState = LifterVariableHandler.getToggleButtonStateFromVariable(sessionId, action)
			  // Flag the fact this is a toggle button and set current state
			  appState.toggleButtonMap(sessionId)(slotNum) = buttonState
			  // Set control for state
			  // If only one parameter is specified in RDF, duplicate the first and use that parameter for the other state too (really we are prepending the one item in the list to itself, but that works ok here)
			  if (textItems.length < 2) textItems ::= textItems(0)
			  if (styleItems.length < 2) styleItems ::= styleItems(0)
			  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
			  if (actionItems.length < 2) actionItems ::= actionItems(0)
			  val stateIndex = if (buttonState) 1 else 0
			  appState.controlsMap(sessionId)(slotNum) = 
				PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum)
			  // A TOGGLEBUTTON trick: we have copied the full action for this control to toggleButtonFullActionMap - now
			  // we rewrite this control's action in the controlDefMap depending on its state.
			  // A bit problematic and there may be a better way, but this lets the action handler chain work the same for 
			  // TOGGLEBUTTONS as for everything else.
			  appState.controlDefMap(sessionId)(slotNum).action = new FreeIdent(getUriPrefix(action) + actionItems(stateIndex), actionItems(stateIndex))
			}
		  case ControlType.TEXTBOX => {
			  appState.controlsMap(sessionId)(slotNum) = TextBox.makeBox(text, style)
			  // Check for initial nee "local" actions which PageCommander needs to handle, such as text display
			  // This should very likely be applied to every control, not just TEXTBOXes, but right now only TEXTBOX
			  // uses it. So leaving it here for the moment to avoid unnecessary checks, but it will likely be moving soon.
			  firstActionHandler.checkForInitialAction(sessionId, slotNum, controlDef)
			}
		  case _ => appState.controlsMap(sessionId)(slotNum) = NodeSeq.Empty
		}
		
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
		val input:Array[String] = Array(subControl.toString)
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
		if (getState.toggleButtonMap(sessionId) contains id) {toggleButton(sessionId,id)}
		handleAction(sessionId, id, null)
	  }
	
	  def toggleButton(sessionId:String, slotNum: Int) {
		val appState = getState
		if (appState.controlDefMap(sessionId) contains slotNum) {
		  if (appState.controlDefMap(sessionId)(slotNum).controlType equals ControlType.TOGGLEBUTTON.toString) {
			val actionUriPrefix = getUriPrefix(appState.controlDefMap(sessionId)(slotNum).action);
			var textItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).text.split(ActionStrings.stringAttributeSeparator))
			if (appState.toggleButtonFullActionMap(sessionId) contains slotNum) {
			  var actionItems = List.fromArray(appState.toggleButtonFullActionMap(sessionId)(slotNum).getLocalName.split(ActionStrings.multiCommandSeparator))
			  var styleItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).style.split(ActionStrings.stringAttributeSeparator))
			  var resourceItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).resource.split(ActionStrings.stringAttributeSeparator))
			  if (appState.toggleButtonMap(sessionId) contains slotNum) {
				if (appState.toggleButtonMap(sessionId)(slotNum)) {
				  // Button is "selected" -- change back to "default" and perform action
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (actionItems.length < 2) actionItems ::= actionItems(0)
				  // This is a little goofy and may be refactored some more. We are modifying the controlDefMap action for this control
				  // so that the usual handler chain can parse it. BUT we must set the action to the "selected" value (with index 1)
				  // while the control itself is toggled back to the "default" state using setControl (with index 0)
				  appState.controlDefMap(sessionId)(slotNum).action = new FreeIdent(actionUriPrefix + actionItems(1), actionItems(1))
				  appState.toggleButtonMap(sessionId)(slotNum) = false
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(0), styleItems(0), resourceItems(0), slotNum))
				} else {
				  // Button is set as "default" -- set to "selected" and perform action
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (textItems.length < 2) textItems ::= textItems(0)
				  if (styleItems.length < 2) styleItems ::= styleItems(0)
				  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
				  // This is a little goofy and may be refactored some more. We are modifying the controlDefMap action for this control
				  // so that the usual handler chain can parse it. BUT we must set the action to the "default" value (with index 0)
				  // while the control itself is toggled to the "selected" state using setControl (with index 1)
				  appState.controlDefMap(sessionId)(slotNum).action = new FreeIdent(actionUriPrefix + actionItems(0), actionItems(0))
				  appState.toggleButtonMap(sessionId)(slotNum) = true
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(1), styleItems(1), resourceItems(1), slotNum))
				}
			  } else {
				error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in toggleButtonMap")
			  }
			} else {
			  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in toggleButtonFullActionMap")
			}
		  } else {
			error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no TOGGLEBUTTON found in controlDefMap")
		  }
		} else {
		  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in controlDefMap")
		}
	  }
	  
	  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
	  // This needs to be refactored somewhere, maybe into LifterVariableHandler but probably into a ToggleButton class?
	  def setAllPublicLiftvarToggleButtonsToState(varName:String, state:Boolean) {
		val appState = getState
		appState.activeSessions.foreach(sessionId => {
			appState.toggleButtonMap(sessionId).keySet.foreach(slotNum => {
				val actionIdent = appState.controlDefMap(sessionId)(slotNum).action
				if (ActionStrings.p_liftvar.equals(getUriPrefix(actionIdent)) && varName.equals(actionIdent.getLocalName)) {
				  var textItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).text.split(ActionStrings.stringAttributeSeparator))
				  var styleItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).style.split(ActionStrings.stringAttributeSeparator))
				  var resourceItems = List.fromArray(appState.controlDefMap(sessionId)(slotNum).resource.split(ActionStrings.stringAttributeSeparator))
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (textItems.length < 2) textItems ::= textItems(0)
				  if (styleItems.length < 2) styleItems ::= styleItems(0)
				  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
				  val stateIndex = if (state) 1 else 0
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum))
				  appState.toggleButtonMap(sessionId)(slotNum) = state
				}
			  })
		  })
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
	  
	  def getControlType(controlDef:ControlConfig): ControlType = {
		var controlType: ControlType = NULLTYPE
		ControlType.values foreach(testType => {
			if (controlDef.controlType equals(testType.toString)) controlType = testType
		  })
		controlType
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

