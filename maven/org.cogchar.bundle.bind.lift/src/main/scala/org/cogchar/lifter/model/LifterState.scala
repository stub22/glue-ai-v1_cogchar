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
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ConcurrentMap
import scala.collection.JavaConversions._ // required to use java.util.concurrent.ConcurrentHashMap as a scala.collection.mutable.ConcurrentMap


// A class to hold the state maps, etc. for Lifter. It looks like there are no getters/setters, but this is Scala so there are!
class LifterState {
  
  final val INITIAL_CONFIG_ID = "InitialConfig" // "sessionId" for initial config for new sessions
  final val SINGLE_SLOT_TEMPLATE = "singleSlot"
  
  // Determines how many controls are "cleared out" from state upon LiftConfig change and initial capacity of ConcurrentHashMaps
  // May be increased as necessary
  final val MAX_CONTROL_QUANTITY = 20;
  
  // Default parameters for ConcurrentHashMap configuration
  private final val DEFAULT_INITIAL_CAPACITY = 8
  private final val DEFAULT_LOAD_FACTOR = 0.9f
  private final val DEFAULT_CONCURRENCY_LEVEL = 1
  
  var initialTemplate:String = "12slots"
  val controlDefMap:ConcurrentMap[String,ConcurrentMap[Int,ControlConfig]] = 
	new ConcurrentHashMap[String, ConcurrentMap[Int,ControlConfig]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  val controlsMap:ConcurrentMap[String,ConcurrentMap[Int,NodeSeq]] = 
	new ConcurrentHashMap[String, ConcurrentMap[Int, NodeSeq]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  val currentConfig:ConcurrentMap[String, LiftConfig] = 
	new ConcurrentHashMap[String, LiftConfig](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  val lastConfig:ConcurrentMap[String, LiftConfig] =
	new ConcurrentHashMap[String, LiftConfig](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  
  // Holds action for currently enabled state of a multi-state control, such as a TOGGLEBUTTON
  val singularAction:ConcurrentMap[String,ConcurrentMap[Int,Ident]] = 
	new ConcurrentHashMap[String, ConcurrentMap[Int,Ident]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	  
  // These guys hold lists of slotNums which will display text from Cogbot, or from Android speech input
  val cogbotDisplayers:ConcurrentMap[String,scala.collection.mutable.ArrayBuffer[Int]] =
	new ConcurrentHashMap[String, scala.collection.mutable.ArrayBuffer[Int]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  val speechDisplayers:ConcurrentMap[String,scala.collection.mutable.ArrayBuffer[Int]] = 
	new ConcurrentHashMap[String, scala.collection.mutable.ArrayBuffer[Int]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  // ... this one for ToggleButton states
  val toggleButtonMap:ConcurrentMap[String,ConcurrentMap[Int,Boolean]] = 
	new ConcurrentHashMap[String, ConcurrentMap[Int,Boolean]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  // ... and ToggleButton full action URIs
  val toggleButtonFullActionMap:ConcurrentMap[String,ConcurrentMap[Int,Ident]] =
	new ConcurrentHashMap[String, ConcurrentMap[Int,Ident]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	  
  // This associates error source codes with the control on which they should be displayed
  val errorMap:ConcurrentMap[String,ConcurrentMap[String,Int]] = 
	new ConcurrentHashMap[String, ConcurrentMap[String, Int]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	  
  // A place to hold variables that can be defined and set dynamically by the apps defined in the lift config files themselves
  val appVariablesMap:ConcurrentMap[String,ConcurrentMap[String,String]] = // per-session
	new ConcurrentHashMap[String, ConcurrentMap[String, String]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  val publicAppVariablesMap:ConcurrentMap[String,String] = // global
	new ConcurrentHashMap[String, String](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	  
  // A map to hold paths to pages requested by LiftAmbassador
  val requestedPage:ConcurrentMap[String,Option[String]] = 
	new ConcurrentHashMap[String, Option[String]](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  // Holds speech we want Android to say
  val outputSpeech:ConcurrentMap[String,String] = 
	new ConcurrentHashMap[String, String](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
  var lastSpeechReqSlotId:String = ""
  // Determines whether Cogbot speech out also triggers Android speech
  val cogbotSpeaks:ConcurrentMap[String,Boolean] =
	new ConcurrentHashMap[String, Boolean](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  // Name of current template (in /templates-hidden) which corresponds to current liftConfig
  val currentTemplate:ConcurrentMap[String,String] =
	new ConcurrentHashMap[String, String](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
  
  var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
  var sessionsAwaitingStart = new scala.collection.mutable.ArrayBuffer[String] 
  //val activeSessions = new scala.collection.mutable.ArrayBuffer[String] 
  
  def activeSessions = controlDefMap.keySet.toList
  
  // All the cloning in here probably indicates there is a Better Way (TM)
  def initializeSession(sessionId:String) {
	// Fill in the controlsMap for the new session with the initial config
	controlsMap(sessionId) = new ConcurrentHashMap[Int,NodeSeq](controlsMap(INITIAL_CONFIG_ID))
	// Copy initial configuration for ControlDef
	controlDefMap(sessionId) = new ConcurrentHashMap[Int,ControlConfig](controlDefMap(INITIAL_CONFIG_ID))
	// Copy initial configuration for local action trackers
	cogbotDisplayers(sessionId) = cogbotDisplayers(INITIAL_CONFIG_ID).clone
	speechDisplayers(sessionId) = speechDisplayers(INITIAL_CONFIG_ID).clone
	errorMap(sessionId) = new ConcurrentHashMap[String,Int](errorMap(INITIAL_CONFIG_ID))
	toggleButtonMap(sessionId) = new ConcurrentHashMap[Int,Boolean](toggleButtonMap(INITIAL_CONFIG_ID))
	toggleButtonFullActionMap(sessionId) = new ConcurrentHashMap[Int,Ident](toggleButtonFullActionMap(INITIAL_CONFIG_ID))
	cogbotSpeaks(sessionId) = false;
	// Add a blank singularAction for this session
	singularAction(sessionId) = new ConcurrentHashMap[Int,Ident](MAX_CONTROL_QUANTITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	// Add a blank appVariablesMap for this session
	appVariablesMap(sessionId) = new ConcurrentHashMap[String,String](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	// Set currentConfig for this session
	currentConfig(sessionId) = currentConfig(INITIAL_CONFIG_ID)
	//Set initial template
	currentTemplate(sessionId) = currentTemplate(INITIAL_CONFIG_ID)
  }
  
  def clearState {
	controlDefMap.clear
	controlDefMap(INITIAL_CONFIG_ID) = new ConcurrentHashMap[Int,ControlConfig](MAX_CONTROL_QUANTITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	controlsMap.clear
	controlsMap(INITIAL_CONFIG_ID) = new ConcurrentHashMap[Int,NodeSeq](MAX_CONTROL_QUANTITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	currentConfig.clear
	lastConfig.clear
	cogbotDisplayers.clear
	cogbotDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
	speechDisplayers.clear
	speechDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
	toggleButtonMap.clear
	toggleButtonMap(INITIAL_CONFIG_ID) = 
	  new ConcurrentHashMap[Int,Boolean](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	toggleButtonFullActionMap.clear
	toggleButtonFullActionMap(INITIAL_CONFIG_ID) = 
	  new ConcurrentHashMap[Int,Ident](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
	singularAction.clear
	appVariablesMap.clear 
	publicAppVariablesMap.clear
	errorMap.clear
	errorMap(INITIAL_CONFIG_ID) =
	  new ConcurrentHashMap[String,Int](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
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
  
  def removeSession(sessionId:String) {
	controlDefMap remove sessionId
	controlsMap remove sessionId
	cogbotDisplayers remove sessionId
	speechDisplayers remove sessionId
	toggleButtonMap remove sessionId
	toggleButtonFullActionMap remove sessionId
	singularAction remove sessionId
	errorMap remove sessionId
	lastConfig remove sessionId
  }
  
}
