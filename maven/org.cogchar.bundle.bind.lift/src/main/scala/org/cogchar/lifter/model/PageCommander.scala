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
	import org.appdapter.core.item.{FreeIdent, Ident}
	import org.cogchar.lifter.lib._
	import org.cogchar.lifter.snippet._
	import org.cogchar.lifter.view._
	import org.cogchar.bind.lift._
	import scala.collection.JavaConverters._
	import org.cogchar.platform.trigger.DummyBinding
	import java.util.concurrent.{Executors, TimeUnit}
	
	object PageCommander extends LiftActor with ListenerManager with Logger {

	  private var initialConfig: LiftConfig = null
	  private val initialControlDefMap = new scala.collection.mutable.HashMap[Int,ControlConfig] 
	  private val controlDefMap = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[Int,ControlConfig]]
	  private val initialControlsMap = new scala.collection.mutable.HashMap[Int, InitialControlConfig]
	  private val controlsMap = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[Int, NodeSeq]]
	  private val currentConfig = new scala.collection.mutable.HashMap[Int, LiftConfig]
	  private val lastConfig = new scala.collection.mutable.HashMap[Int, LiftConfig]
	  private val singularAction = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[Int,Ident]] // Holds action for currently enabled state of a multi-state control, such as a TOGGLEBUTTON
	  
	  // These guys hold lists of slotNums which will display text from Cogbot, or from Android speech input
	  private val cogbotDisplayers = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.ArrayBuffer[Int]]
	  private val speechDisplayers = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.ArrayBuffer[Int]]
	  private val initialCogbotDisplayers = new scala.collection.mutable.ArrayBuffer[Int]
	  private val initialSpeechDisplayers = new scala.collection.mutable.ArrayBuffer[Int]
	  // ... this one for ToggleButton states
	  private val toggleButtonMap = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[Int,Boolean]]
	  private val initialToggleButtonMap = new scala.collection.mutable.HashMap[Int,Boolean]
	  
	  // This associates error source codes with the control on which they should be displayed
	  private val errorMap = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[String, Int]]
	  private val initialErrorMap = new scala.collection.mutable.HashMap[String, Int]
	  
	  // A place to hold variables that can be defined and set dynamically by the apps defined in the lift config files themselves
	  private val appVariablesMap = new scala.collection.mutable.HashMap[Int, scala.collection.mutable.HashMap[String, String]]
	  // For now this publicAppVariablesMap just holds the most recent value of the same named variable set among all sessions
	  // Eventually we probably want to separate these concepts more fully
	  private val publicAppVariablesMap = new scala.collection.mutable.HashMap[String, String]
	  
	  // A map to hold paths to pages requested by LiftAmbassador
	  private val requestedPage = new scala.collection.mutable.HashMap[Int, Option[String]]
	  // Holds speech we want Android to say
	  private val outputSpeech = new scala.collection.mutable.HashMap[Int, String]
	  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
	  private var lastSpeechReqSlotId:String = ""
	  // Determines whether Cogbot speech out also triggers Android speech
	  private val cogbotSpeaks = new scala.collection.mutable.HashMap[Int, Boolean]
	  // Name of current template (in /templates-hidden) which corresponds to current liftConfig
	  private val currentTemplate = new scala.collection.mutable.HashMap[Int, String]
	  private var initialTemplate:String = "12slots"
	  private final var SINGLE_SLOT_TEMPLATE = "singleSlot"
	  private var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
	  
	  private var updateInfo: String = ""
	  
	  def createUpdate = updateInfo
	  
	  // A list of possible control types
	  object ControlType extends Enumeration { 
		type ControlType = Value
		val NULLTYPE, PUSHYBUTTON, TEXTINPUT, DUALTEXTINPUT, LOGINFORM, SELECTBOXES, RADIOBUTTONS, LISTBOX, VIDEOBOX, TOGGLEBUTTON, TEXTBOX = Value
	  }
	  import ControlType._
	  
	  // A superclass for the sessionless configs of initial controls to be presented to new session
	  class InitialControlConfig {
		var controlType:org.cogchar.lifter.snippet.ControlDefinition = null
	  }
	  
	  def getNode(sessionId:Int, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  //info("Getting node for sessionId " + sessionId + " and controlId " + controlId) // TEST ONLY
		  nodeOut = controlsMap(sessionId)(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		nodeOut
	  }
	  
	  def getRequestedPage(sessionId:Int) = {
		val pageToGet = requestedPage(sessionId)
		requestedPage(sessionId) = None // Once the page is read, the request is complete, so we set this back to Nothing
		pageToGet
	  }
	  
	  private var lastNewSessionId = 0;
	  def getNextSessionId = {
		lastNewSessionId += 1;
		if (lifterInitialized) initializeSession(lastNewSessionId)
		lastNewSessionId
	  }
	  
	  def initializeSession(sessionId:Int) {
		info("Initializing Session " + sessionId)
		// Fill in the controlsMap for the new session with the initial config
		controlsMap(sessionId) = new scala.collection.mutable.HashMap[Int, NodeSeq]
		initialControlsMap foreach (initialControlEntry => controlsMap(sessionId)(initialControlEntry._1) = 
			initialControlEntry._2.controlType.makeControl(initialControlEntry._2, sessionId))
		// Copy initial configuration for ControlDef
		controlDefMap(sessionId) = initialControlDefMap.clone
		// Copy initial configuration for local action trackers
		cogbotDisplayers(sessionId) = initialCogbotDisplayers.clone
		speechDisplayers(sessionId) = initialSpeechDisplayers.clone
		errorMap(sessionId) = initialErrorMap.clone
		toggleButtonMap(sessionId) = initialToggleButtonMap.clone
		cogbotSpeaks(sessionId) = false;
		// Add a blank singularAction for this session
		singularAction(sessionId) = new scala.collection.mutable.HashMap[Int,Ident]
		// Add a blank appVariablesMap for this session
		appVariablesMap(sessionId) = new scala.collection.mutable.HashMap[String, String]
		// Set currentConfig for this session
		currentConfig(sessionId) = initialConfig
		//Get initial template and request it be set
		currentTemplate(sessionId) = initialTemplate
		updateInfo = controlId(sessionId, 301) // Special code to trigger TemplateActor
		updateListeners;
		// Render controls via comet
		setControlsFromMap(sessionId)
	  }
	  
	  def renderInitialControls {
		// initialize sessions for sessions that were already started when initial lifter config was received
		for (sessionId <- 1 to lastNewSessionId) {
		  initializeSession(sessionId)
		}
		lifterInitialized = true
	  }
	  
	  def initFromCogcharRDF(sessionId:Int, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session " + sessionId)
		if ((sessionId >= 0) && (sessionId <= lastNewSessionId)) { // ... if so, we have a valid sessionId
		  if (sessionId == 0) { // sessionId of 0 tells us this is initial config; reset all maps
			controlDefMap.clear
			initialControlDefMap.clear
			controlsMap.clear
			initialControlsMap.clear
			currentConfig.clear
			lastConfig.clear
			cogbotDisplayers.clear
			speechDisplayers.clear
			initialCogbotDisplayers.clear
			initialSpeechDisplayers.clear
			toggleButtonMap.clear
			initialToggleButtonMap.clear
			singularAction.clear
			appVariablesMap.clear // Probably we won't want this to clear permanently. It's sort of a quick-fix for now to make sure that toggle button states don't get out of sync with app variables they control when a "page" is exited and reentered
			errorMap.clear
			initialErrorMap.clear
			initialConfig = liftConfig
		  } else { // otherwise reset maps for this session
			controlDefMap(sessionId).clear
			controlsMap(sessionId).clear
			cogbotDisplayers(sessionId).clear
			speechDisplayers(sessionId).clear
			toggleButtonMap(sessionId).clear
			singularAction(sessionId).clear
			appVariablesMap(sessionId).clear // Probably we won't want this to clear permanently. It's sort of a quick-fix for now to make sure that toggle button states don't get out of sync with app variables they control when a "page" is exited and reentered
			errorMap(sessionId).clear
			lastConfig(sessionId) = currentConfig(sessionId)
			currentConfig(sessionId) = liftConfig
		  }

		  //val liftConfig = LiftAmbassador.getConfig();
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
			  if (sessionId == 0) {
				initialControlDefMap(slotNum) = controlDef; // Save the controlDef for this slotNum for future reference
				initialControlsMap(slotNum) = initSingleControl(controlDef, slotNum, true, 0)
			  } else {
				controlDefMap(sessionId)(slotNum) = controlDef
				val newControlConfig = initSingleControl(controlDef, slotNum, false, sessionId)
				controlsMap(sessionId)(slotNum) = newControlConfig.controlType.makeControl(newControlConfig, sessionId)
			  }

			})
		  // Blank unspecified slots (out to 20)
		  for (slot <- 1 to 20) {
			if (sessionId == 0) {
			  if (!(initialControlDefMap contains slot)) {
				info("Setting blank control in slot " + slot + " of initialControlsMap")
				initialControlsMap(slot) = new BlankControl.BlankControlConfig()
			  }
			} else {
			  if (!(controlDefMap(sessionId) contains slot)) {
				controlsMap(sessionId)(slot) = NodeSeq.Empty
			  }
			}
		  }
		  if (sessionId == 0) { // for initial config, store initialTemplate
			initialTemplate = liftConfig.template
			renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		  } else { // otherwise, activate our new template for this session
			currentTemplate(sessionId) = liftConfig.template
			updateInfo = controlId(sessionId, 301) // Special code to trigger TemplateActor
			updateListeners;
			// ... and load new controls
			setControlsFromMap(sessionId)
		  }
		} else {
		  error("Config received for invalid sessionId: " + sessionId)
		}
	  }
	  
	  def initSingleControl(controlDef:ControlConfig, slotNum:Int, initialConfig:Boolean, sessionId:Int): InitialControlConfig = {
		var loadingConfig: InitialControlConfig = null;
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
			  loadingConfig = new PushyButton.PushyButtonConfig(text, style, resource, slotNum)
			}
		  case ControlType.TEXTINPUT => {
			  loadingConfig = new TextForm.TextFormConfig(text, slotNum)
			}
		  case ControlType.DUALTEXTINPUT => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(","))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  loadingConfig = new DualTextForm.DualTextFormConfig(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.LOGINFORM => {
			  // From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
			  val textItems = List.fromArray(text.split(","))
			  val label1 = textItems(0)
			  val label2 = textItems(1)
			  val submitLabel = textItems(2)
			  loadingConfig = new LoginForm.LoginFormConfig(label1, label2, submitLabel, slotNum)
			}
		  case ControlType.SELECTBOXES => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest checkbox labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  loadingConfig = new SelectBoxes.SelectBoxesConfig(titleText, labelItems, slotNum)
			}
		  case ControlType.RADIOBUTTONS => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  loadingConfig = new RadioButtons.RadioButtonsConfig(titleText, labelItems, slotNum)
			}
		  case ControlType.LISTBOX => {
			  // From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
			  val textItems = List.fromArray(text.split(","))
			  val titleText = textItems(0)
			  val labelItems = textItems.tail
			  loadingConfig = new ListBox.ListBoxConfig(titleText, labelItems, slotNum)
			}
		  case ControlType.VIDEOBOX => {
			  loadingConfig = new VideoBox.VideoBoxConfig(resource, true)
			}
		  case ControlType.TOGGLEBUTTON => {
			  // For a ToggleButton, the first item in CSV text, action, style, image corresponds to the default condition, the second to the "toggled" condition
			  val textItems = List.fromArray(text.split(","))
			  val styleItems = List.fromArray(style.split(","))
			  val resourceItems = List.fromArray(resource.split(","))
			  // Set control for initial (default) state
			  loadingConfig = new PushyButton.PushyButtonConfig(textItems(0), styleItems(0), resourceItems(0), slotNum)
			  // Flag the fact this is a toggle button, currently in the default (false) condition
			  if (initialConfig) {
				initialToggleButtonMap(slotNum) = false
			  } else {
				toggleButtonMap(sessionId)(slotNum) = false
			  }
			  
			}
		  case ControlType.TEXTBOX => {
			  loadingConfig = new TextBox.TextBoxConfig(text, style, false, true) // From legacy config, centered=false and displayAsCell = true are hardcoded for now but will probably end up in RDF definition
			  // Check for "local" actions which PageCommander needs to handle, such as text display
			  initLocalActions(slotNum, action, initialConfig, sessionId) // this method will modify action as necessary according to prefixes 
			}
		  case _ => loadingConfig = new BlankControl.BlankControlConfig
		}
		loadingConfig
	  }
					  
	  def setControl(sessionId: Int, slotNum: Int, slotHtml: NodeSeq) {
		controlsMap(sessionId)(slotNum) = slotHtml 
		updateInfo = controlId(sessionId, slotNum)
		updateListeners()
	  }
	  
	  def setControlsFromMap(sessionId:Int) {
		val slotIterator = controlsMap(sessionId).keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateInfo = controlId(sessionId, nextSlot)
		  updateListeners
		}
	  }
									
	  // Check to see if any action requested requires PageCommander to do some local handling
	  def initLocalActions(slotNum:Int, action:Ident, initialConfig:Boolean, sessionId:Int) {
		if (action != null) {
		  if (action.getAbsUriString.startsWith(ActionStrings.p_liftcmd)) {
			val splitAction = action.getLocalName.split("_")
			splitAction(0) match {
			  case ActionStrings.showText => {splitAction(1) match {
					case ActionStrings.COGBOT_TOKEN => { // Show Cogbot speech on this control? Add it to the cogbotDisplayers list.
						if (initialConfig) initialCogbotDisplayers += slotNum else cogbotDisplayers(sessionId) += slotNum
					}
					case ActionStrings.ANDROID_SPEECH_TOKEN => { // Add to the speechDisplayers list if we want Android speech shown here
						if (initialConfig) initialSpeechDisplayers += slotNum else speechDisplayers(sessionId) += slotNum
					}
					case ActionStrings.ERROR_TOKEN => { // Associate the error source name with the slotNum where errors will display
						if (initialConfig) initialErrorMap(splitAction(2)) = slotNum else errorMap(sessionId)(splitAction(2)) = slotNum
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
	  def controlActionMapper(sessionId:Int, formId:Int, subControl:Int) {
		val actionUriPrefix = getUriPrefix(controlDefMap(sessionId)(formId).action);
		actionUriPrefix match {
		  case ActionStrings.p_liftvar => {
			  val textItems = List.fromArray(controlDefMap(sessionId)(formId).text.split(","))
			  val textIndex = subControl + 1
			  appVariablesMap(sessionId)(controlDefMap(sessionId)(formId).action.getLocalName) = textItems(textIndex)
			  // Set the public (common to all sessions) version of this variable too - eventually there will likely be further distinction between "public" and "per-session" variables
			  publicAppVariablesMap(controlDefMap(sessionId)(formId).action.getLocalName) = textItems(textIndex)
			  info("App Variable " + controlDefMap(sessionId)(formId).action.getLocalName + " set to " + textItems(textIndex) + " for session " + sessionId)
			}
		  case ActionStrings.p_liftcmd => {
			  val splitAction = controlDefMap(sessionId)(formId).action.getLocalName.split("_")
			  splitAction(0) match {
				case ActionStrings.oldDemo => { // Just a way to include the old hard-coded demo just a little longer; soon will configure all of this from RDF
					subControl match { // An early hard coded demo
					  case 0 => setControl(sessionId, 6, PushyButton.makeButton("A button", "buttonred", "", sessionId, 6))
					  case 1 => setControl(sessionId, 6, TextForm.makeTextForm("A text box", sessionId, 6))
					  case 2 => setControl(sessionId, 6, SelectBoxes.makeSelectBoxes("Checkboxes", List("an option", "and another"), sessionId, 6))
					  case 3 => setControl(sessionId, 6, RadioButtons.makeRadioButtons("Radio buttons", List("Radio Option 1", "Radio Option 2"), sessionId, 6))
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
	  def textInputMapper(sessionId:Int, formId:Int, text:String) {
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
					val response = LiftAmbassador.getCogbotResponse(text)
					val cleanedResponse = response.replaceAll("<.*>", ""); // For now, things are more readable if we just discard embedded XML
					cogbotDisplayers(sessionId).foreach(slotNum =>
					  setControl(sessionId, slotNum, TextBox.makeBox("Cogbot said \"" + cleanedResponse + "\"", controlDefMap(sessionId)(slotNum).style)))
					if (cogbotSpeaks(sessionId)) outputSpeech(sessionId, cleanedResponse) // Output Android speech if cogbotSpeaks is set
				  }
				}
			  case _ => {
				  // Send text to LiftAmbassador, see if it knows what to do with it
				  if (!LiftAmbassador.sendTextToCogChar(actionToken, text)) { // May need to pass sessionId!
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
	  
	  def multiTextInputMapper(sessionId:Int, formId:Int, text:Array[String]) {
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
			  LiftAmbassador.requestNetworkConfig(text(0), encryptionName, text(1))
			} else if ((ActionStrings.LOGIN_TOKEN equals desiredAction) && (text.length == 2)) {
			  LiftAmbassador.login(sessionId, text(0), text(1));
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
	  def triggerAction(sessionId:Int, id: Int): Boolean  = {
		var success = false
		if (toggleButtonMap(sessionId) contains id) {success = toggleButton(sessionId,id)}
		else {success = continueTriggering(sessionId, id)}
		success
	  }
	  
	  // Another "segment" of the triggering operation, which we jump back into from toggleButton or directly from triggerAction
	  def continueTriggering(sessionId:Int, id: Int): Boolean = {
		var success = false
		if (controlDefMap(sessionId).contains(id)) {
		  if (performLocalActions(sessionId, id)) success = true // Local action was performed! We're done.
		  else {
			var action = controlDefMap(sessionId)(id).action
			if (singularAction(sessionId) contains id) {action = singularAction(sessionId)(id)} // If this is a "multi-state" control, get the action corresponding to current state
			info("About to trigger in LiftAmbassador with sessionId " + sessionId + " and slotNum " + id + "; action is " + action);
			success = LiftAmbassador.triggerAction(sessionId, action)
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
	  def createSceneInfoScreen(sessionId:Int, slotNum:Int): LiftConfig = {
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
	  def performLocalActions(sessionId:Int, slotNum: Int) = {
		val actionUriPrefix = getUriPrefix(controlDefMap(sessionId)(slotNum).action);
		var actionSuffix = controlDefMap(sessionId)(slotNum).action.getLocalName();
		if (singularAction(sessionId) contains slotNum) {actionSuffix = singularAction(sessionId)(slotNum).getLocalName} // If this is a "multi-state" control, get the action corresponding to current state
		val splitAction = actionSuffix.split("_")
		var success = false;
		actionUriPrefix match {
		  case ActionStrings.p_liftcmd => {
			  splitAction(0) match {
				case ActionStrings.acquireSpeech => {
					updateInfo = controlId(sessionId, 201) // Special "slotNum" to tell JavaScriptActor to request speech
					lastSpeechReqSlotId = updateInfo; // Set this field - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
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
				appVariablesMap(sessionId)(actionSuffix) = toggleButtonMap(sessionId)(slotNum).toString
				// Set the public (common to all sessions) version of this variable too - eventually there will likely be further distinction between "public" and "per-session" variables
				publicAppVariablesMap(actionSuffix) = toggleButtonMap(sessionId)(slotNum).toString
			  } else if (appVariablesMap(sessionId) contains actionSuffix) {
				if (appVariablesMap(sessionId)(actionSuffix).toBoolean) {
				  appVariablesMap(sessionId)(actionSuffix) = false.toString 
				  // Set the public (common to all sessions) version of this variable too - eventually there will likely be further distinction between "public" and "per-session" variables
				  publicAppVariablesMap(actionSuffix) = false.toString
				} else {
				  appVariablesMap(sessionId)(actionSuffix) = true.toString
				  // Set the public (common to all sessions) version of this variable too - eventually there will likely be further distinction between "public" and "per-session" variables
				  publicAppVariablesMap(actionSuffix) = true.toString
				}
			  } else {
				appVariablesMap(sessionId)(actionSuffix) = true.toString
				// Set the public (common to all sessions) version of this variable too - eventually there will likely be further distinction between "public" and "per-session" variables
				publicAppVariablesMap(actionSuffix) = true.toString
			  }
			  info("App Variable " + actionSuffix + " set to " + appVariablesMap(sessionId)(actionSuffix) + " for session " + sessionId)
			}
		  case _ => // No match, just exit (success=false)
		}
		success
	  }
	
	  def toggleButton(sessionId:Int, slotNum: Int) = {
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
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(0), styleItems(0), resourceItems(0), sessionId, slotNum))
				} else {
				  // Button is set as "default" -- set to "selected" and perform action
				  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
				  if (textItems.length < 2) textItems ::= textItems(0)
				  if (styleItems.length < 2) styleItems ::= styleItems(0)
				  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
				  singularAction(sessionId)(slotNum) = new FreeIdent(actionUriPrefix + actionItems(0), actionItems(0))
				  toggleButtonMap(sessionId)(slotNum) = true
				  success = continueTriggering(sessionId, slotNum)
				  setControl(sessionId, slotNum, PushyButton.makeButton(textItems(1), styleItems(1), resourceItems(1), sessionId, slotNum))
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
	  
	  def outputSpeech(sessionId:Int, text: String) {
		outputSpeech(sessionId) = text
		updateInfo = controlId(sessionId, 203) // To tell JavaScriptActor we want Android devices to say the text
		updateListeners()
	  }
	  
	  def requestContinuousSpeech(sessionId:Int, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to " + desired + " for session " + sessionId)
		if (desired) {
		  lastSpeechReqSlotId = controlId(sessionId, slotNum)
		  updateInfo = controlId(sessionId, 204) // To request start command - Should become a named constant soon
		  updateListeners()
		} else {
		  updateInfo = controlId(sessionId, 205) // To request stop command - Needs to become a named constant soon
		  updateListeners()
		}
	  }
	  
	  def getSpeechReqControl = lastSpeechReqSlotId
	  
	  def getCurrentTemplate(sessionId:Int) = {
		var templateToLoad: String = null
		if (currentTemplate contains sessionId) templateToLoad = currentTemplate(sessionId)
		templateToLoad
	  }
	  
	  def getOutputSpeech(sessionId:Int) = {
		var speechToOutput: String = ""
		if (outputSpeech contains sessionId) speechToOutput = outputSpeech(sessionId)
		speechToOutput
	  }
	  
	  
	  // I think Ident should include this method, but since it doesn't...
	  def getUriPrefix(uri: Ident) : String = {
		uri.getAbsUriString.stripSuffix(uri.getLocalName)
	  }
																	  
	  def controlId(sessionId:Int, controlId: Int): String = {
		sessionId + "_" + controlId
	  }
	   
	  /* We don't support lift config Turtle files now after making the switch to action URIs, unless we want to 
	   * bring back that capability. See org.cogchar.bind.lift.ControlConfig for more info
	   def reconfigureControlsFromRdf(rdfFile:String) = {
	   LiftAmbassador.activateControlsFromRdf(rdfFile)
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
		  initFromCogcharRDF(0, LiftAmbassador.getInitialConfig) // Set initial config with sessionId 0
		}
		def setConfigForSession(sessionId:Int, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		def loadPage(sessionId:Int, pagePath:String) {
		  requestedPage(sessionId) = Some(pagePath)
		  updateInfo = controlId(sessionId, 202) // Our "special control slot" for triggering page redirect
		  updateListeners()
		}
		def getVariable(key:String): String = { // returns value from "public" app variables map -- right now, just the most recently set value between all sessions
		  var contents:String = null
		  if (publicAppVariablesMap contains key) contents = publicAppVariablesMap(key)
		  contents
		}
		def getVariable(sessionId:Int, key:String): String = {
		  var contents:String = null
		  if (appVariablesMap(sessionId) contains key) contents = appVariablesMap(sessionId)(key)
		  contents
		}
		def showError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = " + errorSourceCode + "; text = " + errorText);
		  // Seems we want to display this error in all sessions currently featuring a control to monitor this error type
		  // We may modify this thinking in the future
		  for (sessionId <- 1 to lastNewSessionId) {
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
}

