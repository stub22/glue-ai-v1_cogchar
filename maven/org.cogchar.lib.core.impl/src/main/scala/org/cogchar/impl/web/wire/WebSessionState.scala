/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.web.wire
import org.cogchar.impl.web.config.{ControlConfig, LiftConfig}
import scala.xml.NodeSeq
import org.appdapter.core.name.Ident

import scala.collection.mutable.{ArrayBuffer,HashMap,Map,SynchronizedBuffer};
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ConcurrentMap
import scala.collection.JavaConversions._ // required to use java.util.concurrent.ConcurrentHashMap as a scala.collection.mutable.ConcurrentMap

/**
 */


class WebSessionState(private val mySessionId : String) {
	var controlConfigBySlot:ConcurrentMap[Int,ControlConfig] = new ConcHashMapWithCapacity[Int,ControlConfig](HashMapBindings.MAX_CONTROL_QUANTITY)
	var controlXmlBySlot:ConcurrentMap[Int,NodeSeq] = new ConcHashMapWithCapacity[Int,NodeSeq](HashMapBindings.MAX_CONTROL_QUANTITY)
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
	// The multiActionsBySlot map holds arrays of action idents for "MultiAction" controls which allow
	// multiple actions. Sort of an ugly construct, so may be supplanted by a better way.
	// No need to copy this from initial config in getNewSessionState, since it will be populated as necessary 
	// by control "initial actions" in each session.
	var multiActionsBySlot:ConcurrentMap[Int,Array[Ident]] = new DfltConcHashMap[Int,Array[Ident]]
 
	def initUsingContents(sourceState : WebSessionState) {
		controlConfigBySlot = new ConcurrentHashMap[Int,ControlConfig](sourceState.controlConfigBySlot)
		controlXmlBySlot = new ConcurrentHashMap[Int,NodeSeq](sourceState.controlXmlBySlot)
		currentLiftConfig = sourceState.currentLiftConfig // we won't be modifying this, so no need to copy
		cogbotDisplaySlots = sourceState.cogbotDisplaySlots.clone
		speechDisplaySlots = sourceState.speechDisplaySlots.clone
		toggleControlStateBySlot = new ConcurrentHashMap[Int,Boolean](sourceState.toggleControlStateBySlot)
		toggleControlMultiActionsBySlot = new ConcurrentHashMap[Int,Ident](sourceState.toggleControlMultiActionsBySlot)
		errorDisplaySlotsByType = new ConcurrentHashMap[String,Int](sourceState.errorDisplaySlotsByType)
		currentTemplateName = sourceState.currentTemplateName		
	}
	
	def propagateStateCore(sourceState : WebSessionState)  { 
		// We need to retain sessionLifterVariablesByName
		sessionLifterVariablesByName = sourceState.sessionLifterVariablesByName
		// We also need to set lastLiftConfig
		lastLiftConfig = sourceState.currentLiftConfig
	}
	
}
