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

package org.cogchar.lifter.model

import org.cogchar.bind.lift.{ControlConfig, LiftConfig}
import scala.xml.NodeSeq
import org.appdapter.core.name.Ident

// A class to hold the state maps, etc. for Lifter. It looks like there are no getters/setters, but this is Scala so there are!
class LifterState {
  
  final val INITIAL_CONFIG_ID = "InitialConfig" // "sessionId" for initial config for new sessions
  final val SINGLE_SLOT_TEMPLATE = "singleSlot"
  
  var initialTemplate:String = "12slots"
  val controlDefMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,ControlConfig]]
  val controlsMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int, NodeSeq]]
  val currentConfig = new scala.collection.mutable.HashMap[String, LiftConfig]
  val lastConfig = new scala.collection.mutable.HashMap[String, LiftConfig]
  val singularAction = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,Ident]] // Holds action for currently enabled state of a multi-state control, such as a TOGGLEBUTTON
	  
  // These guys hold lists of slotNums which will display text from Cogbot, or from Android speech input
  val cogbotDisplayers = new scala.collection.mutable.HashMap[String, scala.collection.mutable.ArrayBuffer[Int]]
  val speechDisplayers = new scala.collection.mutable.HashMap[String, scala.collection.mutable.ArrayBuffer[Int]]
  // ... this one for ToggleButton states
  val toggleButtonMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,Boolean]]
  // ... and ToggleButton full action URIs
  val toggleButtonFullActionMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[Int,Ident]]
	  
  // This associates error source codes with the control on which they should be displayed
  val errorMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[String, Int]]
	  
  // A place to hold variables that can be defined and set dynamically by the apps defined in the lift config files themselves
  val appVariablesMap = new scala.collection.mutable.HashMap[String, scala.collection.mutable.HashMap[String, String]] // per-session
  val publicAppVariablesMap = new scala.collection.mutable.HashMap[String, String] // global
	  
  // A map to hold paths to pages requested by LiftAmbassador
  val requestedPage = new scala.collection.mutable.HashMap[String, Option[String]]
  // Holds speech we want Android to say
  val outputSpeech = new scala.collection.mutable.HashMap[String, String]
  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
  var lastSpeechReqSlotId:String = ""
  // Determines whether Cogbot speech out also triggers Android speech
  val cogbotSpeaks = new scala.collection.mutable.HashMap[String, Boolean]
  // Name of current template (in /templates-hidden) which corresponds to current liftConfig
  val currentTemplate = new scala.collection.mutable.HashMap[String, String]
  
  var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
  var sessionsAwaitingStart = new scala.collection.mutable.ArrayBuffer[String] 
  val activeSessions = new scala.collection.mutable.ArrayBuffer[String] 
  
  // All the cloning in here probably indicates there is a Better Way (TM)
  def initializeSession(sessionId:String) {
	// Fill in the controlsMap for the new session with the initial config
	controlsMap(sessionId) = controlsMap(INITIAL_CONFIG_ID).clone
	// Copy initial configuration for ControlDef
	controlDefMap(sessionId) = controlDefMap(INITIAL_CONFIG_ID).clone
	// Copy initial configuration for local action trackers
	cogbotDisplayers(sessionId) = cogbotDisplayers(INITIAL_CONFIG_ID).clone
	speechDisplayers(sessionId) = speechDisplayers(INITIAL_CONFIG_ID).clone
	errorMap(sessionId) = errorMap(INITIAL_CONFIG_ID).clone
	toggleButtonMap(sessionId) = toggleButtonMap(INITIAL_CONFIG_ID).clone
	toggleButtonFullActionMap(sessionId) = toggleButtonFullActionMap(INITIAL_CONFIG_ID).clone
	cogbotSpeaks(sessionId) = false;
	// Add a blank singularAction for this session
	singularAction(sessionId) = new scala.collection.mutable.HashMap[Int,Ident]
	// Add a blank appVariablesMap for this session
	appVariablesMap(sessionId) = new scala.collection.mutable.HashMap[String, String]
	// Set currentConfig for this session
	currentConfig(sessionId) = currentConfig(INITIAL_CONFIG_ID)
	//Get initial template and request it be set
	currentTemplate(sessionId) = currentTemplate(INITIAL_CONFIG_ID)
	if (!(activeSessions contains sessionId)) {
	  activeSessions += sessionId
	}
  }
  
  def clearState {
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
	toggleButtonFullActionMap.clear
	toggleButtonFullActionMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[Int,Ident]
	singularAction.clear
	appVariablesMap.clear 
	publicAppVariablesMap.clear
	errorMap.clear
	errorMap(INITIAL_CONFIG_ID) = new scala.collection.mutable.HashMap[String, Int]
  }
  
  def clearSession(sessionId:String) {
	controlDefMap(sessionId).clear
	controlsMap(sessionId).clear
	cogbotDisplayers(sessionId).clear
	speechDisplayers(sessionId).clear
	toggleButtonMap(sessionId).clear
	toggleButtonFullActionMap(sessionId).clear
	singularAction(sessionId).clear
	errorMap(sessionId).clear
	lastConfig(sessionId) = currentConfig(sessionId)
  }
  
}
