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
import scala.collection.mutable.{ArrayBuffer,SynchronizedBuffer};
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
  
  
  class ConcHashMapWithCapacity[T,U](initialCapacity:Int) extends 
		  ConcurrentHashMap[T,U](initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL)
		  
  class DfltConcHashMap[T,U] extends ConcHashMapWithCapacity[T,U](DEFAULT_INITIAL_CAPACITY)

  // Needs some additional refactoring, since these default definitions are usually thrown away by getNewSessionState
  class SessionState {
	var controlConfigBySlot:ConcurrentMap[Int,ControlConfig] = new ConcHashMapWithCapacity[Int,ControlConfig](MAX_CONTROL_QUANTITY)
	var controlXmlBySlot:ConcurrentMap[Int,NodeSeq] = new ConcHashMapWithCapacity[Int,NodeSeq](MAX_CONTROL_QUANTITY)
	var currentLiftConfig: LiftConfig = null
	var lastLiftConfig: LiftConfig = null
	// Lists of slotNums which will display text from Cogbot, or from Android speech input
	var cogbotDisplaySlots:ArrayBuffer[Int] = new scala.collection.mutable.ArrayBuffer[Int]
	var speechDisplaySlots:ArrayBuffer[Int] = new scala.collection.mutable.ArrayBuffer[Int]
	var toggleControlStateBySlot:ConcurrentMap[Int,Boolean] = new DfltConcHashMap[Int,Boolean]
	var toggleControlMultiActionsBySlot:ConcurrentMap[Int,Ident] = new DfltConcHashMap[Int,Ident]
	// This associates error source codes with the control on which they should be displayed
	var errorDisplaySlotsByType:ConcurrentMap[String,Int] = new DfltConcHashMap[String,Int]
	// A place to hold per-session variables that can be defined and set dynamically by the apps defined in the lift config files themselves
	var sessionLifterVariablesByName:ConcurrentMap[String,String] = new DfltConcHashMap[String,String]
	var cogbotTextToSpeechActive:Boolean = false
	var currentTemplateName:String = ""
	// Needed to "de-bounce" controls, mainly necessary now for Android 4.1 Webview JavaScript bug:
	val lastTimeAcutatedBySlot:ConcurrentMap[Int,Long] = new DfltConcHashMap[Int,Long]
  }
  
  val stateBySession:ConcurrentMap[String,SessionState] = new DfltConcHashMap[String,SessionState]

  val globalLifterVariablesByName:ConcurrentMap[String,String] = new DfltConcHashMap[String,String]
	  
  // A map to hold paths to pages requested by LiftAmbassador
  // Needs to be eliminated through proper actor usage
  val requestedPage:ConcurrentMap[String,Option[String]] = new DfltConcHashMap[String,Option[String]]
  // Holds speech we want Android to say
  // Needs to be eliminated through proper actor usage
  val outputSpeech:ConcurrentMap[String,String] = new DfltConcHashMap[String,String]
  // id of last control which requested speech - used by JavaScriptActor to add identifying info to request
  // Needs to be eliminated through proper actor usage
  var lastSpeechReqSlotId:ConcurrentMap[String,String] = new DfltConcHashMap[String,String]

  
  var lifterInitialized:Boolean = false // Will be set to true once PageCommander receives initial control config from LiftAmbassador
  val activeSessions = new ArrayBuffer[String] with SynchronizedBuffer[String]
  var sessionsAwaitingStart = new ArrayBuffer[String] with SynchronizedBuffer[String]
  
  // Should only be called after clearAndInitializeState (and loading of the initial state into stateBySession(INITIAL_CONFIG_ID)) 
  def initializeSession(sessionId:String) {
	stateBySession(sessionId) = getNewSessionState
	// Set blank value for session key in lastSpeechReqSlotId (refactoring out soon)
	lastSpeechReqSlotId(sessionId) = ""
	// Add session to activeSessions list
	if (!(activeSessions contains sessionId)) {activeSessions += sessionId}
  }
  
  // New sessions need a state based on the special SessionState with the INITIAL_CONFIG_ID
  // We don't really need a full copy constructor, so provide this.
  // It might be better to use somewhat more verbose for comprehensions to fill the existing maps in newSession from the 
  // contents of those in initialState. That would look messier but would allow all maps in SessionState to be vals
  // and avoid just throwing away and replacing maps created in the SessionState constructor. So likely more refactoring
  // to come.
  private def getNewSessionState = {
	val newSession = new SessionState
	val initialState = stateBySession(INITIAL_CONFIG_ID)
	newSession.controlConfigBySlot = new ConcurrentHashMap[Int,ControlConfig](initialState.controlConfigBySlot)
	newSession.controlXmlBySlot = new ConcurrentHashMap[Int,NodeSeq](initialState.controlXmlBySlot)
	newSession.currentLiftConfig = initialState.currentLiftConfig // we won't be modifying this, so no need to copy
	newSession.cogbotDisplaySlots = initialState.cogbotDisplaySlots.clone
	newSession.speechDisplaySlots = initialState.speechDisplaySlots.clone
	newSession.toggleControlStateBySlot = new ConcurrentHashMap[Int,Boolean](initialState.toggleControlStateBySlot)
	newSession.toggleControlMultiActionsBySlot = new ConcurrentHashMap[Int,Ident](initialState.toggleControlMultiActionsBySlot)
	newSession.errorDisplaySlotsByType = new ConcurrentHashMap[String,Int](initialState.errorDisplaySlotsByType)
	newSession.currentTemplateName = initialState.currentTemplateName
	newSession
  }
  
  def clearAndInitializeState {
	stateBySession.clear
	stateBySession(INITIAL_CONFIG_ID) = new SessionState
	globalLifterVariablesByName.clear
	// The following should be going away soon via further refactoring:
	requestedPage.clear
	outputSpeech.clear
	lastSpeechReqSlotId.clear
  }
  
  def prepareSessionForNewConfig(sessionId:String) {
	val previousSessionState = stateBySession(sessionId)
	// We need to retain sessionLifterVariablesByName
	val variables = previousSessionState.sessionLifterVariablesByName
	// We also need to set lastLiftConfig
	val lastConfig = previousSessionState.currentLiftConfig
	stateBySession(sessionId) = new SessionState
	stateBySession(sessionId).sessionLifterVariablesByName = variables
	stateBySession(sessionId).lastLiftConfig = lastConfig
  }
  
  def removeSession(sessionId:String) {
	// Session is no longer active...
	activeSessions remove sessionId
	stateBySession remove sessionId
	// The following should be going away soon via further refactoring:
	requestedPage remove sessionId
	outputSpeech remove sessionId
	lastSpeechReqSlotId remove sessionId
  }
  
}


