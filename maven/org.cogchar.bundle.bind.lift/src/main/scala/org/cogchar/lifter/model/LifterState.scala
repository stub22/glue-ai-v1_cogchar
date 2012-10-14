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
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.SynchronizedBuffer;
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
  // For more info on these parameters, see http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/
  private final val DEFAULT_INITIAL_CAPACITY = 8
  private final val DEFAULT_LOAD_FACTOR = 0.9f
  private final val DEFAULT_CONCURRENCY_LEVEL = 1
  
  class DefaultConcurrentHashMap[T,U] extends ConcurrentHashMap[T,U](DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL) {}
  class ConcurrentHashMapWithCapacity[T,U](initialCapacity:Int) extends 
		  ConcurrentHashMap[T,U](initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL) {}

  var initialTemplate:String = "12slots"

  val controlDefMap:ConcurrentMap[String,ConcurrentMap[Int,ControlConfig]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,ControlConfig]]
  val controlsMap:ConcurrentMap[String,ConcurrentMap[Int,NodeSeq]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,NodeSeq]]
  val currentConfig:ConcurrentMap[String, LiftConfig] = new DefaultConcurrentHashMap[String,LiftConfig]
  val lastConfig:ConcurrentMap[String, LiftConfig] = new DefaultConcurrentHashMap[String,LiftConfig]
  
  // Holds action for currently enabled state of a multi-state control, such as a TOGGLEBUTTON
  val singularAction:ConcurrentMap[String,ConcurrentMap[Int,Ident]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,Ident]]
	  
  // These guys hold lists of slotNums which will display text from Cogbot, or from Android speech input
  val cogbotDisplayers:ConcurrentMap[String,ArrayBuffer[Int]] = new DefaultConcurrentHashMap[String,ArrayBuffer[Int]]
  val speechDisplayers:ConcurrentMap[String,ArrayBuffer[Int]] = new DefaultConcurrentHashMap[String,ArrayBuffer[Int]]
  // ... this one for ToggleButton states
  val toggleButtonMap:ConcurrentMap[String,ConcurrentMap[Int,Boolean]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,Boolean]]
  // ... and ToggleButton full action URIs
  val toggleButtonFullActionMap:ConcurrentMap[String,ConcurrentMap[Int,Ident]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,Ident]]
	  
  // This associates error source codes with the control on which they should be displayed
  val errorMap:ConcurrentMap[String,ConcurrentMap[String,Int]] = new DefaultConcurrentHashMap[String,ConcurrentMap[String,Int]]
	  
  // A place to hold variables that can be defined and set dynamically by the apps defined in the lift config files themselves
  // per-session
  val appVariablesMap:ConcurrentMap[String,ConcurrentMap[String,String]] = new DefaultConcurrentHashMap[String,ConcurrentMap[String,String]]
  // global
  val publicAppVariablesMap:ConcurrentMap[String,String] = new DefaultConcurrentHashMap[String,String]
	  
  // A map to hold paths to pages requested by LiftAmbassador
  val requestedPage:ConcurrentMap[String,Option[String]] = new DefaultConcurrentHashMap[String,Option[String]]
  // Holds speech we want Android to say
  val outputSpeech:ConcurrentMap[String,String] = new DefaultConcurrentHashMap[String,String]
  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
  var lastSpeechReqSlotId:ConcurrentMap[String,String] = new DefaultConcurrentHashMap[String,String]
  // Determines whether Cogbot speech out also triggers Android speech
  val cogbotSpeaks:ConcurrentMap[String,Boolean] = new DefaultConcurrentHashMap[String,Boolean]
  // Name of current template (in /templates-hidden) which corresponds to current liftConfig
  val currentTemplate:ConcurrentMap[String,String] = new DefaultConcurrentHashMap[String,String]
  // Tracks recently actuated controls, so "bounced" or double clicked buttons will only excute their actions once
  val bounceMap:ConcurrentMap[String,ConcurrentMap[Int,Long]] = new DefaultConcurrentHashMap[String,ConcurrentMap[Int,Long]]
  
  var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
  val activeSessions = new ArrayBuffer[String] with SynchronizedBuffer[String]
  var sessionsAwaitingStart = new ArrayBuffer[String] with SynchronizedBuffer[String]
  
  // A temporary list to handle required page refresh on first LiftConfig change to ensure proper Comet behavior 
  // in previously started browsers already displaying Lifter at time of Lifter start
  val firstConfigChanged = new ArrayBuffer[String] with SynchronizedBuffer[String]
  
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
	singularAction(sessionId) = new ConcurrentHashMapWithCapacity[Int,Ident](MAX_CONTROL_QUANTITY)
	// Add a blank appVariablesMap for this session
	appVariablesMap(sessionId) = new DefaultConcurrentHashMap[String,String]
	// Set currentConfig for this session
	currentConfig(sessionId) = currentConfig(INITIAL_CONFIG_ID)
	// Set initial template
	currentTemplate(sessionId) = currentTemplate(INITIAL_CONFIG_ID)
	// Set blank value for session key in lastSpeechReqSlotId
	lastSpeechReqSlotId(sessionId) = ""
	// Make fresh bounce tracking map
	bounceMap(sessionId) = new DefaultConcurrentHashMap[Int,Long]
	// Add session to activeSessions list
	if (!(activeSessions contains sessionId)) {activeSessions += sessionId}
  }
  
  def clearState {
	controlDefMap.clear
	controlDefMap(INITIAL_CONFIG_ID) = new ConcurrentHashMapWithCapacity[Int,ControlConfig](MAX_CONTROL_QUANTITY)
	controlsMap.clear
	controlsMap(INITIAL_CONFIG_ID) = new ConcurrentHashMapWithCapacity[Int,NodeSeq](MAX_CONTROL_QUANTITY)
	currentConfig.clear
	lastConfig.clear
	cogbotDisplayers.clear
	cogbotDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
	speechDisplayers.clear
	speechDisplayers(INITIAL_CONFIG_ID) = new scala.collection.mutable.ArrayBuffer[Int]
	lastSpeechReqSlotId.clear
	toggleButtonMap.clear
	toggleButtonMap(INITIAL_CONFIG_ID) = new DefaultConcurrentHashMap[Int,Boolean]
	toggleButtonFullActionMap.clear
	toggleButtonFullActionMap(INITIAL_CONFIG_ID) = new DefaultConcurrentHashMap[Int,Ident]
	singularAction.clear
	appVariablesMap.clear 
	publicAppVariablesMap.clear
	errorMap.clear
	errorMap(INITIAL_CONFIG_ID) = new DefaultConcurrentHashMap[String,Int]
	bounceMap.clear
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
	lastSpeechReqSlotId(sessionId) = ""
  }
  
  def removeSession(sessionId:String) {
	// Session is no longer active...
	activeSessions remove sessionId
	controlDefMap remove sessionId
	controlsMap remove sessionId
	cogbotDisplayers remove sessionId
	speechDisplayers remove sessionId
	toggleButtonMap remove sessionId
	toggleButtonFullActionMap remove sessionId
	singularAction remove sessionId
	errorMap remove sessionId
	bounceMap remove sessionId
	lastConfig remove sessionId
	lastSpeechReqSlotId remove sessionId
  }
  
}
