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
	// but also in some un-Actor-like ways.) This would allow us to separate the page rendering actor parts of PageCommander from
	// the growing amount of "non-CogChar" "action" logic.
	object PageCommander extends LiftActor with ListenerManager with Logger {

	  private var theLiftAmbassador:LiftAmbassador = null // Probably it makes sense to retain a pointer to the LiftAmbassador since it is used in several methods.
	  private val controlDefMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,ControlConfig]]
	  private val controlsMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int, NodeSeq]]
	  private val currentConfig = new scala.collection.mutable.HashMap[String, LiftConfig]
	  private val lastConfig = new scala.collection.mutable.HashMap[String, LiftConfig]
	  private val singularAction = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,Ident]] // Holds action for currently enabled state of a multi-state control, such as a TOGGLEBUTTON
	  
	  // These guys hold lists of slotNums which will display text from Cogbot, or from Android speech input
	  private val cogbotDisplayers = new scala.collection.mutable.HashMap[String, scala.collection.mutable.ArrayBuffer[Int]]
	  private val speechDisplayers = new scala.collection.mutable.HashMap[String, scala.collection.mutable.ArrayBuffer[Int]]
	  // ... this one for ToggleButton states
	  private val toggleButtonMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,Boolean]]
	  
	  // This associates error source codes with the control on which they should be displayed
	  private val errorMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[String, Int]]
	  
	  // A place to hold variables that can be defined and set dynamically by the apps defined in the lift config files themselves
	  private val appVariablesMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[String, String]] // per-session
	  private val publicAppVariablesMap = new scala.collection.mutable.HashMap[String, String] // global
	  
	  // A map to hold paths to pages requested by LiftAmbassador
	  private val requestedPage = new scala.collection.mutable.HashMap[String, Option[String]]
	  // Holds speech we want Android to say
	  private val outputSpeech = new scala.collection.mutable.HashMap[String, String]
	  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
	  private var lastSpeechReqSlotId:String = ""
	  // Determines whether Cogbot speech out also triggers Android speech
	  private val cogbotSpeaks = new scala.collection.mutable.HashMap[String, Boolean]
	  // Name of current template (in /templates-hidden) which corresponds to current liftConfig
	  private val currentTemplate = new scala.collection.mutable.HashMap[String, String]
	  private var initialTemplate:String = "12slots"
	  private final var SINGLE_SLOT_TEMPLATE = "singleSlot"
	  final var INITIAL_CONFIG_ID = "InitialConfig" // "sessionId" for initial config for new sessions
	  private var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
	  private val sessionsAwaitingStart = new scala.collection.mutable.ArrayBuffer[String] 
	  private val activeSessions = new scala.collection.mutable.ArrayBuffer[String] 
	  
	  private var updateInfo: String = ""
	  
	  def createUpdate = updateInfo
	  
	  // A list of possible control types
	  object ControlType extends Enumeration { 
		type ControlType = Value
		val NULLTYPE, PUSHYBUTTON, TEXTINPUT, DUALTEXTINPUT, LOGINFORM, SELECTBOXES, RADIOBUTTONS, LISTBOX, VIDEOBOX, TOGGLEBUTTON, TEXTBOX = Value
	  }
	  import ControlType._
	  
	  def getNode(sessionId:String, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  nodeOut = controlsMap(sessionId)(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		nodeOut
	  }
	  
	  def getRequestedPage(sessionId:String) = {
		val pageToGet = requestedPage(sessionId)
		requestedPage(sessionId) = None // Once the page is read, the request is complete, so we set this back to Nothing
		pageToGet
	  }
	  
	  def getLiftAmbassador = {
		  if (theLiftAmbassador == null) {
			theLiftAmbassador = LiftAmbassador.getLiftAmbassador
		  }
		  theLiftAmbassador
	  }
	  
	  def initializeSession(sessionId:String) {
		info("Initializing Session " + sessionId) 
		// Fill in the controlsMap for the new session with the initial config
		controlsMap(sessionId) = controlsMap(INITIAL_CONFIG_ID).clone
		// Copy initial configuration for ControlDef
		controlDefMap(sessionId) = controlDefMap(INITIAL_CONFIG_ID).clone
		// Copy initial configuration for local action trackers
		cogbotDisplayers(sessionId) = cogbotDisplayers(INITIAL_CONFIG_ID).clone
		speechDisplayers(sessionId) = speechDisplayers(INITIAL_CONFIG_ID).clone
		errorMap(sessionId) = errorMap(INITIAL_CONFIG_ID).clone
		toggleButtonMap(sessionId) = toggleButtonMap(INITIAL_CONFIG_ID).clone
		cogbotSpeaks(sessionId) = false;
		// Add a blank singularAction for this session
		singularAction(sessionId) = new scala.collection.mutable.HashMap[Int,Ident]
		// Add a blank appVariablesMap for this session
		appVariablesMap(sessionId) = new scala.collection.mutable.HashMap[String, String]
		// Set currentConfig for this session
		currentConfig(sessionId) = currentConfig(INITIAL_CONFIG_ID)
		//Get initial template and request it be set
		currentTemplate(sessionId) = currentTemplate(INITIAL_CONFIG_ID)
		updateListeners(controlId(sessionId, ActorCodes.TEMPLATE_CODE));
		setControlsFromMap(sessionId)
		if (!(activeSessions contains sessionId)) {
		  activeSessions += sessionId
		}
	  }
	  
	  def renderInitialControls {
		if (!lifterInitialized) {
		  sessionsAwaitingStart.foreach(sessionId => initializeSession(sessionId))
		  sessionsAwaitingStart.clear
		  lifterInitialized = true
		} else { // if lifterInitialized, this is a restart on config change
		  activeSessions.foreach(sessionId => initializeSession(sessionId))
		}
	  }
	  
	  def requestStart(sessionId:String) {
	   if (lifterInitialized) {
		 initializeSession(sessionId)
	   } else {
		 sessionsAwaitingStart += sessionId
	   }
	  }
									
	  def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session " + sessionId)
		if (sessionId.equals(INITIAL_CONFIG_ID)) {
		  controlDefMap.clear
		  controlDefMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[Int,ControlConfig]
		  controlsMap.clear
		  controlsMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[Int, NodeSeq]
		  currentConfig.clear
		  lastConfig.clear
		  cogbotDisplayers.clear
		  cogbotDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
		  speechDisplayers.clear
		  speechDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
		  toggleButtonMap.clear
		  toggleButtonMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[Int,Boolean]
		  singularAction.clear
		  appVariablesMap.clear 
		  publicAppVariablesMap.clear
		  errorMap.clear
		  errorMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[String, Int]
		} else { // otherwise reset maps for this session
		  controlDefMap(sessionId).clear
		  controlsMap(sessionId).clear
		  cogbotDisplayers(sessionId).clear
		  speechDisplayers(sessionId).clear
		  toggleButtonMap(sessionId).clear
		  singularAction(sessionId).clear
		  errorMap(sessionId).clear
		  lastConfig(sessionId) = currentConfig(sessionId)
		}
		
		currentConfig(sessionId) = liftConfig

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
			controlDefMap(sessionId)(slotNum) = controlDef
			initSingleControl(controlDef, slotNum, sessionId)
		  })
		// Blank unspecified slots (out to 20)
		for (slot <- 1 to 20) {
		  if (!(controlDefMap(sessionId) contains slot)) {
			controlsMap(sessionId)(slot) = NodeSeq.Empty
		  }
		}
		currentTemplate(sessionId) = liftConfig.template
		if (sessionId.equals(INITIAL_CONFIG_ID)) { 
		  renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
		  val changedTemplate = (currentTemplate(sessionId) != lastConfig(sessionId).template)
		  if (changedTemplate) {
			updateInfo = controlId(sessionId, ActorCodes.TEMPLATE_CODE)
			updateListeners;
		  }
		  // ... and load new controls
		  setControlsFromMap(sessionId)
		}
	  }
	  
	  def initSingleControl(controlDef:ControlConfig, slotNum:Int, sessionId:String) {
		var controlType: ControlType = NULLTYPE
		ControlType.values foreach(testType => {
			if (controlDef.controlType equals(testType.toString)) controlType = testType
		  })
		val action = controlDef.action
		val text = controlDef.text
		val style = controlDef.style
		val resource = controlDef.resource
			
		controlType match {
		  case ControlType.PUSHYBUTTON => {
			  controlsMap(sessionId)(slotNum) = PushyButton.makeButton(text, style, resource, slotNum)
			}
		  case ControlType.TEXTINPUT => {
			  controlsMap(sessionId)(slotNum) = TextForm.makeTextForm(text, slotNum)
			}
		  case ControlType.DUALTEXTINPUT => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(","))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  controlsMap(sessionId)(slotNum) = DualTextForm.makeForm(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.LOGINFORM => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(","))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  controlsMap(sessionId)(slotNum) = LoginForm.makeForm(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.SELECTBOXES => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest checkbox labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  controlsMap(sessionId)(slotNum) = SelectBoxes.makeSelectBoxes(titleText, labelItems, slotNum)
			}
		  case ControlType.RADIOBUTTONS => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  controlsMap(sessionId)(slotNum) = RadioButtons.makeRadioButtons(titleText, labelItems, slotNum)
			}
		  case ControlType.LISTBOX => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  controlsMap(sessionId)(slotNum) = ListBox.makeListBox(titleText, labelItems, slotNum)
			}
		  case ControlType.VIDEOBOX => {
			  controlsMap(sessionId)(slotNum) = VideoBox.makeBox(resource, true)
			}
		  case ControlType.TOGGLEBUTTON => {
			  // For a ToggleButton, the first item in CSV text, action, style, image corresponds to the default condition, the second to the "toggled" condition
			  var textItems = List.fromArray(text.split(","))
			  var styleItems = List.fromArray(style.split(","))
			  var resourceItems = List.fromArray(resource.split(","))
			  // Next we need to see if an app variable linked to this toggle button is already set and set the button state to match if so
			  val buttonState = getToggleButtonStateFromVariable(sessionId, action)
			  // Flag the fact this is a toggle button and set current state
			  toggleButtonMap(sessionId)(slotNum) = buttonState
			  // Set control for state
			  // If only one parameter is specified in RDF, duplicate the first and use that parameter for the other state too (really we are prepending the one item in the list to itself, but that works ok here)
			  if (textItems.length < 2) textItems ::= textItems(0)
			  if (styleItems.length < 2) styleItems ::= styleItems(0)
			  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
			  val stateIndex = if (buttonState) 1 else 0
			  controlsMap(sessionId)(slotNum) = 
				PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum)
			}
		  case ControlType.TEXTBOX => {
			  controlsMap(sessionId)(slotNum) = TextBox.makeBox(text, style)
			  // Check for "local" actions which PageCommander needs to handle, such as text display
			  initLocalActions(slotNum, action, sessionId) // this method will modify action as necessary according to prefixes 
			}
		  case _ => controlsMap(sessionId)(slotNum) = NodeSeq.Empty
		}
	  }
					  
	  def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		controlsMap(sessionId)(slotNum) = slotHtml 
		updateListeners(controlId(sessionId, slotNum))
	  }
	  
	  def setControlsFromMap(sessionId:String) {
		val slotIterator = controlsMap(sessionId).keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateListeners(controlId(sessionId, nextSlot))
		}
	  }
	  
	  // Checks to see if a toggle button already has a state defined by a Lifter variable
	  // Returns this state if so, otherwise returns false
	  def getToggleButtonStateFromVariable(sessionId:String, action:Ident): Boolean = {
		val actionUriPrefix = getUriPrefix(action);
		var buttonState = false
		actionUriPrefix match {
		  case ActionStrings.p_liftvar => {
			  val mappedVariable = action.getLocalName();
			  if (publicAppVariablesMap contains mappedVariable) {
				buttonState = publicAppVariablesMap(mappedVariable).toBoolean
			  }
			}
		  case ActionStrings.p_liftsessionvar => {
			  val mappedVariable = action.getLocalName();
			  if (appVariablesMap(sessionId) contains mappedVariable) {
				buttonState = appVariablesMap(sessionId)(mappedVariable).toBoolean
			  }
			}
		  case _ =>
		}
		buttonState
	  }
									
	  // Check to see if any action requested requires PageCommander to do some local handling
	  def initLocalActions(slotNum:Int, action:Ident, sessionId:String) {
		if (action != null) {
		  if (action.getAbsUriString.startsWith(ActionStrings.p_liftcmd)) {
			val splitAction = action.getLocalName.split("_")
			splitAction(0) match {
			  case ActionStrings.showText => {splitAction(1) match {
					case ActionStrings.COGBOT_TOKEN => { // Show Cogbot speech on this control? Add it to the cogbotDisplayers list.
						cogbotDisplayers(sessionId) += slotNum
					}
					case ActionStrings.ANDROID_SPEECH_TOKEN => { // Add to the speechDisplayers list if we want Android speech shown here
						speechDisplayers(sessionId) += slotNum
					}
					case ActionStrings.ERROR_TOKEN => { // Associate the error source name with the slotNum where errors will display
						errorMap(sessionId)(splitAction(2)) = slotNum
					}
					case _ => warn("checkLocalActions doesn't know what to do in order to display text with token " + splitAction(1))
				  }
				}			  
			  case _ => // looks like this action doesn't require anything to happen locally, so do nothing
			}
		  }
		}
	  }
	    
	  // A central place to define actions performed by displayed controls - may want to move to its own class eventually
	  def controlActionMapper(sessionId:String, formId:Int, subControl:Int) {
		val actionUriPrefix = getUriPrefix(controlDefMap(sessionId)(formId).action);
		actionUriPrefix match {
		  case ActionStrings.p_liftvar => {
			  val textItems = List.fromArray(controlDefMap(sessionId)(formId).text.split(","))
			  val textIndex = subControl + 1
			  publicAppVariablesMap(controlDefMap(sessionId)(formId).action.getLocalName) = textItems(textIndex)
			  info("Global App Variable " + controlDefMap(sessionId)(formId).action.getLocalName + " set to " + textItems(textIndex))
			}
		  case ActionStrings.p_liftsessionvar => {
				val textItems = List.fromArray(controlDefMap(sessionId)(formId).text.split(","))
				val textIndex = subControl + 1
				appVariablesMap(sessionId)(controlDefMap(sessionId)(formId).action.getLocalName) = textItems(textIndex)
				info("Session App Variable " + controlDefMap(sessionId)(formId).action.getLocalName + " set to " + textItems(textIndex) + " for session " + sessionId)
			  }
		  case ActionStrings.p_liftcmd => {
			  val splitAction = controlDefMap(sessionId)(formId).action.getLocalName.split("_")
			  splitAction(0) match {
				case ActionStrings.oldDemo => { // Just a way to include the old hard-coded demo just a little longer; soon will configure all of this from RDF
					subControl match { // An early hard coded demo
					  case 0 => setControl(sessionId, 6, PushyButton.makeButton("A button", "buttonred", "", 6))
					  case 1 => setControl(sessionId, 6, TextForm.makeTextForm("A text box", 6))
					  case 2 => setControl(sessionId, 6, SelectBoxes.makeSelectBoxes("Checkboxes", List("an option", "and another"), 6))
					  case 3 => setControl(sessionId, 6, RadioButtons.makeRadioButtons("Radio buttons", List("Radio Option 1", "Radio Option 2"), 6))
					  case _ =>
					}
				  }
				case _ =>
			  }
			}
		  case _ =>
		}
	  }
	  
	  // Similarly, a central place to handle text input.
	  def textInputMapper(sessionId:String, formId:Int, text:String) {
		if (controlDefMap(sessionId)(formId).action.getAbsUriString.startsWith(ActionStrings.p_liftcmd)) {
		  var desiredAction = controlDefMap(sessionId)(formId).action.getLocalName
		  if (singularAction(sessionId) contains formId) {desiredAction = singularAction(sessionId)(formId).getLocalName} // If this is a "multi-state" control, get the action corresponding to current state
		  // If we see an action with a get speech command, set displayInputSpeech, strip off acquire command and see what's behind
		  var displayInputSpeech = false;
		  if (desiredAction startsWith ActionStrings.acquireSpeech) {
			desiredAction = desiredAction.stripPrefix(ActionStrings.acquireSpeech + "_")
			displayInputSpeech = true;
		  } else if (desiredAction startsWith ActionStrings.getContinuousSpeech) {
			desiredAction = desiredAction.stripPrefix(ActionStrings.getContinuousSpeech + "_")
			displayInputSpeech = true;
		  }
		  //info("In textInputMapper; desiredAction is " + desiredAction) // TEST ONLY
		  if (displayInputSpeech) {
			speechDisplayers(sessionId).foreach(slotNum => 
			  setControl(sessionId, slotNum, TextBox.makeBox("I think you said \"" + text + "\"", controlDefMap(sessionId)(slotNum).style, true)))
		  } // ... then continue to see if RDF tells us we need to do anything else with speech
		  if (desiredAction.startsWith(ActionStrings.submitText)) { //... otherwise, we don't have a properly defined action for this text input
			val stringToStrip = ActionStrings.submitText + "_"
			val actionToken = desiredAction.stripPrefix(stringToStrip)
			actionToken match {
			  case ActionStrings.COGBOT_TOKEN => {
				  if (cogbotDisplayers(sessionId) != Nil) { // Likely this check is not necessary - foreach just won't execute if list is Nil, right?
					val response = getLiftAmbassador.getCogbotResponse(text)
					val cleanedResponse = response.replaceAll("<.*>", ""); // For now, things are more readable if we just discard embedded XML
					cogbotDisplayers(sessionId).foreach(slotNum =>
					  setControl(sessionId, slotNum, TextBox.makeBox("Cogbot said \"" + cleanedResponse + "\"", controlDefMap(sessionId)(slotNum).style)))
					if (cogbotSpeaks(sessionId)) outputSpeech(sessionId, cleanedResponse) // Output Android speech if cogbotSpeaks is set
				  }
				}
			  case _ => {
				  // Send text to LiftAmbassador, see if it knows what to do with it
				  if (!getLiftAmbassador.sendTextToCogChar(actionToken, text)) { // May need to pass sessionId!
					warn("No action found in textInputMapper for token " + actionToken + " during session " + sessionId)
				  }
				}
			}
		  } else {
			warn("Action in control id " + formId + " for session " + sessionId + " is not recognized by textInputMapper: " + desiredAction)
		  }
		} else {
		  warn("Action URI prefix in control id " + formId + " for session " + sessionId + 
			   " does not provide a valid action to textInputMapper: " + getUriPrefix(controlDefMap(sessionId)(formId).action))
		}
	  }
	  
	  def multiTextInputMapper(sessionId:String, formId:Int, text:Array[String]) {
		if (controlDefMap(sessionId)(formId).action.getAbsUriString.startsWith(ActionStrings.p_liftcmd)) {
		  var desiredAction = controlDefMap(sessionId)(formId).action.getLocalName
		  if (desiredAction startsWith ActionStrings.submit) {
			desiredAction = desiredAction.stripPrefix(ActionStrings.submit + "_")
			if ((ActionStrings.NETWORK_CONFIG_TOKEN equals desiredAction) && (text.length == 2)) {
			  var encryptionName:String = null;
			  if (appVariablesMap(sessionId) contains ActionStrings.encryptionTypeVar) {
				encryptionName = appVariablesMap(sessionId)(ActionStrings.encryptionTypeVar)
			  } else {
				warn("No encryption type set for network config, assuming none")
				encryptionName = ActionStrings.noEncryptionName
			  }
			  getLiftAmbassador.requestNetworkConfig(text(0), encryptionName, text(1))
			} else if ((ActionStrings.LOGIN_TOKEN equals desiredAction) && (text.length == 2)) {
			  getLiftAmbassador.login(sessionId, text(0), text(1));
			} else {
			  warn("No action found in multiTextInputMapper for \"" + desiredAction + "\" and " + text.length + " inputs");
			}
		  } else {
			warn("Action in control id " + formId + " for session " + sessionId + " is not recognized by multiTextInputMapper: " + desiredAction)
		  }
		} else {
		  warn("Action URI prefix in control id " + formId + " for session " + sessionId + 
			   "does not provide a valid action to multiTextInputMapper: " + getUriPrefix(controlDefMap(sessionId)(formId).action))
		}
	  }
	  
	  // Perform action according to slotNum 
	  def triggerAction(sessionId:String, id: Int): Boolean  = {
		var success = false
		if (toggleButtonMap(sessionId) contains id) {success = toggleButton(sessionId,id)}
		else {success = continueTriggering(sessionId, id)}
		success
	  }
	  
	  // Another "segment" of the triggering operation, which we jump back into from toggleButton or directly from triggerAction
	  def continueTriggering(sessionId:String, id: Int): Boolean = {
		var success = false
		if (controlDefMap(sessionId).contains(id)) {
		  if (performLocalActions(sessionId, id)) success = true // Local action was performed! We're done.
		  else {
			var action = controlDefMap(sessionId)(id).action
			if (singularAction(sessionId) contains id) {action = singularAction(sessionId)(id)} // If this is a "multi-state" control, get the action corresponding to current state
			//info("About to trigger in LiftAmbassador with sessionId " + sessionId + " and slotNum " + id + "; action is " + action); // TEST ONLY
			success = getLiftAmbassador.triggerAction(sessionId, action)
			// If the action sent to LiftAmbassador was a scene trigger, show the "Scene Playing screen" if command was successful
			if ((action.getAbsUriString.startsWith(ActionStrings.p_scenetrig)) && (success)) {
			  val sceneRunningScreen = createSceneInfoScreen(sessionId, id)
			  initFromCogcharRDF(sessionId, sceneRunningScreen)
			}
		  }
		} else {warn("Action requested, but no control def found for slot " + id + " of session " + sessionId)}
		success
	  }
								
	  // A method to create a liftconfig locally to serve as a "Scene Playing" info screen
	  def createSceneInfoScreen(sessionId:String, slotNum:Int): LiftConfig = {
		val sceneInfoConfig = new LiftConfig(SINGLE_SLOT_TEMPLATE)
		val infoButton = new ControlConfig()
		infoButton.myURI_Fragment = "info_control_1"
		infoButton.controlType = ControlType.PUSHYBUTTON.toString
		infoButton.action = new FreeIdent(ActionStrings.p_liftcmd + ActionStrings.lastConfig, ActionStrings.lastConfig)
		infoButton.text = "Playing " + controlDefMap(sessionId)(slotNum).text
		infoButton.style = controlDefMap(sessionId)(slotNum).style
		infoButton.resource = controlDefMap(sessionId)(slotNum).resource
		sceneInfoConfig.myCCs.add(infoButton)
		sceneInfoConfig
	  }
	  
	  // Perform any button actions handled locally, and return true if we find one
	  def performLocalActions(sessionId:String, slotNum: Int) = {
		val actionUriPrefix = getUriPrefix(controlDefMap(sessionId)(slotNum).action);
		var actionSuffix = controlDefMap(sessionId)(slotNum).action.getLocalName();
		if (singularAction(sessionId) contains slotNum) {actionSuffix = singularAction(sessionId)(slotNum).getLocalName} // If this is a "multi-state" control, get the action corresponding to current state
		val splitAction = actionSuffix.split("_")
		var success = false;
		actionUriPrefix match {
		  case ActionStrings.p_liftcmd => {
			  splitAction(0) match {
				case ActionStrings.acquireSpeech => {
					updateInfo = controlId(sessionId, ActorCodes.SPEECH_REQUEST_CODE)
					lastSpeechReqSlotId = controlId(sessionId, slotNum); // Set this field - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
					updateListeners()
					success = true
				  }
				case ActionStrings.cogbotSpeech => splitAction(1) match {
					case ActionStrings.ENABLE_TOKEN => cogbotSpeaks(sessionId) = true; success = true
					case ActionStrings.DISABLE_TOKEN => cogbotSpeaks(sessionId) = false; success = true
					case _ => // No match, just exit (success=false)
				  }
				case ActionStrings.getContinuousSpeech => {
					requestContinuousSpeech(sessionId, slotNum, true)
				  }
				case ActionStrings.stopContinuousSpeech => {
					requestContinuousSpeech(sessionId, slotNum, false)
				  }
				case ActionStrings.lastConfig => {
					initFromCogcharRDF(sessionId, lastConfig(sessionId))
				  }
				case _ => // No match, just exit (success=false)
			  }
			}
		  case ActionStrings.p_liftvar => {
			  //A button wants to set a variable. That means we toggle the value between true and false.
			  if (toggleButtonMap(sessionId) contains slotNum) { // ... make sure the value is synced with the button state if so
				val toggleButtonState = toggleButtonMap(sessionId)(slotNum)
				publicAppVariablesMap(actionSuffix) = toggleButtonState.toString
				setAllPublicLiftvarToggleButtonsToState(actionSuffix, toggleButtonState)
			  } else if (publicAppVariablesMap contains actionSuffix) {
				if (publicAppVariablesMap(actionSuffix).toBoolean) {
				  publicAppVariablesMap(actionSuffix) = false.toString 
				} else {
				  publicAppVariablesMap(actionSuffix) = true.toString
				}
			  } else {
				publicAppVariablesMap(actionSuffix) = true.toString
			  }
			  info("Global App Variable " + actionSuffix + " set to " + publicAppVariablesMap(actionSuffix))
			}
		  case ActionStrings.p_liftsessionvar => {
				//A button wants to set a variable. That means we toggle the value between true and false.
				if (toggleButtonMap(sessionId) contains slotNum) { // ... make sure the value is synced with the button state if so
				  appVariablesMap(sessionId)(actionSuffix) = toggleButtonMap(sessionId)(slotNum).toString
				} else if (appVariablesMap(sessionId) contains actionSuffix) {
				  if (appVariablesMap(sessionId)(actionSuffix).toBoolean) {
					appVariablesMap(sessionId)(actionSuffix) = false.toString 
				  } else {
					appVariablesMap(sessionId)(actionSuffix) = true.toString
				  }
				} else {
				  appVariablesMap(sessionId)(actionSuffix) = true.toString
				}
				info("Session App Variable " + actionSuffix + " set to " + appVariablesMap(sessionId)(actionSuffix) + " for session " + sessionId)
			  }
		  case _ => // No match, just exit (success=false)
		}
		success
	  }
	
	  def toggleButton(sessionId:String, slotNum: Int) = {
		var success = false;
		if (controlDefMap(sessionId) contains slotNum) {
		  if (controlDefMap(sessionId)(slotNum).controlType equals ControlType.TOGGLEBUTTON.toString) {
			val actionUriPrefix = getUriPrefix(controlDefMap(sessionId)(slotNum).action);
			if ((actionUriPrefix.equals(ActionStrings.p_liftcmd)) || 
				(actionUriPrefix.equals(ActionStrings.p_liftvar))) {
			  var textItems = List.fromArray(controlDefMap(sessionId)(slotNum).text.split(","))
			  var actionItems = List.fromArray(controlDefMap(sessionId)(slotNum).action.getLocalName.split("__"))
			  var styleItems = List.fromArray(controlDefMap(sessionId)(slotNum).style.split(","))
			  var resourceItems = List.fromArray(controlDefMap(sessionId)(slotNum).resource.split(","))
			  if (toggleButtonMap(sessionId) contains slotNum) {
				if (toggleButtonMap(sessionId)(slotNum)) {
				  // Button is "selected" -- change back to "default" and perform action
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (actionItems.length < 2) actionItems ::= actionItems(0)
				  singularAction(sessionId)(slotNum) = new FreeIdent(actionUriPrefix + actionItems(1), actionItems(1))
				  toggleButtonMap(sessionId)(slotNum) = false
				  success = continueTriggering(sessionId, slotNum)
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(0), styleItems(0), resourceItems(0), slotNum))
				} else {
				  // Button is set as "default" -- set to "selected" and perform action
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (textItems.length < 2) textItems ::= textItems(0)
				  if (styleItems.length < 2) styleItems ::= styleItems(0)
				  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
				  singularAction(sessionId)(slotNum) = new FreeIdent(actionUriPrefix + actionItems(0), actionItems(0))
				  toggleButtonMap(sessionId)(slotNum) = true
				  success = continueTriggering(sessionId, slotNum)
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(1), styleItems(1), resourceItems(1), slotNum))
				}
			  } else {
				error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in toggleButtonMap")
			  }
			} else {
			  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + 
					", but action URI prefix does not provide a valid action: " + getUriPrefix(controlDefMap(sessionId)(slotNum).action))
			}
		  } else {
			error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no TOGGLEBUTTON found in controlDefMap")
		  }
		} else {
		  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in controlDefMap")
		}
		success
	  }
	  
	  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
	  def setAllPublicLiftvarToggleButtonsToState(varName:String, state:Boolean) {
		activeSessions.foreach(sessionId => {
			toggleButtonMap(sessionId).keySet.foreach(slotNum => {
				val actionIdent = controlDefMap(sessionId)(slotNum).action
				if (ActionStrings.p_liftvar.equals(getUriPrefix(actionIdent)) && varName.equals(actionIdent.getLocalName)) {
				  var textItems = List.fromArray(controlDefMap(sessionId)(slotNum).text.split(","))
				  var styleItems = List.fromArray(controlDefMap(sessionId)(slotNum).style.split(","))
				  var resourceItems = List.fromArray(controlDefMap(sessionId)(slotNum).resource.split(","))
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (textItems.length < 2) textItems ::= textItems(0)
				  if (styleItems.length < 2) styleItems ::= styleItems(0)
				  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
				  val stateIndex = if (state) 1 else 0
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum))
				  toggleButtonMap(sessionId)(slotNum) = state
				}
			  })
		  })
	  }
	  
	  def outputSpeech(sessionId:String, text: String) {
		outputSpeech(sessionId) = text
		updateInfo = controlId(sessionId, ActorCodes.SPEECH_OUT_CODE) // To tell JavaScriptActor we want Android devices to say the text
		updateListeners()
	  }
	  
	  def requestContinuousSpeech(sessionId:String, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to " + desired + " for session " + sessionId)
		if (desired) {
		  lastSpeechReqSlotId = controlId(sessionId, slotNum)
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_START_CODE)
		  updateListeners()
		} else {
		  updateInfo = controlId(sessionId, ActorCodes.CONTINUOUS_SPEECH_REQUEST_STOP_CODE)
		  updateListeners()
		}
	  }
	  
	  def getSpeechReqControl = lastSpeechReqSlotId
	  
	  def getCurrentTemplate(sessionId:String) = {
		var templateToLoad: String = null
		if (currentTemplate contains sessionId) templateToLoad = currentTemplate(sessionId)
		templateToLoad
	  }
	  
	  def getOutputSpeech(sessionId:String) = {
		var speechToOutput: String = ""
		if (outputSpeech contains sessionId) speechToOutput = outputSpeech(sessionId)
		speechToOutput
	  }
	  
	  
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
		  initFromCogcharRDF(INITIAL_CONFIG_ID, getLiftAmbassador.getInitialConfig)
		}
		def setConfigForSession(sessionId:String, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		def loadPage(sessionId:String, pagePath:String) {
		  requestedPage(sessionId) = Some(pagePath)
		  updateInfo = controlId(sessionId, ActorCodes.LOAD_PAGE_CODE)
		  updateListeners()
		}
		def getVariable(key:String): String = { // returns value from "public" (global) app variables map
		  var contents:String = null
		  if (publicAppVariablesMap contains key) contents = publicAppVariablesMap(key)
		  contents
		}
		def getVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		  var contents:String = null
		  if (appVariablesMap(sessionId) contains key) contents = appVariablesMap(sessionId)(key)
		  contents
		}
		// Show error globally
		def showError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText);
		  val activeSessionIterator = controlsMap.keysIterator
		  while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showError(errorSourceCode, errorText, sessionId)
		  }
		}
		// Show error in session
		def showError(errorSourceCode:String, errorText:String, sessionId:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText + "; session = " + sessionId);
		  if (errorMap contains sessionId) {
			  if (errorMap(sessionId) contains errorSourceCode) {
				val slotNum = errorMap(sessionId)(errorSourceCode)
				if (errorText.isEmpty) {
				  setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
				  setControl(sessionId, slotNum, TextBox.makeBox(errorText, controlDefMap(sessionId)(slotNum).style, true, false))
				}
			  }
		  }
		}
	  }
	
	}

  }
}

