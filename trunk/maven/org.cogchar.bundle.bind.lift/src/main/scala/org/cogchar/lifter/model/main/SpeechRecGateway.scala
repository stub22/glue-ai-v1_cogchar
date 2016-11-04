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

package org.cogchar.lifter.model.main

/**
 * @author Stu B. <www.texpedient.com>
 */

import org.cogchar.impl.web.util.HasLogger
import net.liftweb.http.ListenerManager

object SpeechRecGateway extends HasLogger {
	
	  private def info(msg: String, params: Any*) {
          myLogger.info(msg, params.map(_.asInstanceOf[Object]).toArray)
      }
	 private val myListenerManager = PageCommander
	
	  case class SpeechOutRequest(sessionId:String, text:String)
	  case class SpeechInRequest(sessionId:String, slotNum:Int)
	  case class ContinuousSpeechInStartRequest(sessionId:String, slotNum:Int)
	  case class ContinuousSpeechInStopRequest(sessionId:String)
	  
	  
	  // Likely should go in different class...
	  def outputSpeech(sessionId:String, text: String) {
		myListenerManager.exposedUpdateListeners(SpeechOutRequest(sessionId, text)) // To tell JavaScriptActor we want Android devices to say the text
	  }
	  
	  // Likely should go in different class...
	  def acquireSpeech(sessionId:String, slotNum:Int) {
		myListenerManager.exposedUpdateListeners(SpeechInRequest(sessionId, slotNum)); // Send this message - JavaScriptActor will use it to attach requesting info to JS Call - allows multiple speech request controls
	  }
	  
	  // Likely should go in different class...
	  def requestContinuousSpeech(sessionId:String, slotNum: Int, desired: Boolean) {
		info("In requestContinuousSpeech, setting to {} for session {}", desired, sessionId)
		if (desired) {
		  myListenerManager.exposedUpdateListeners(ContinuousSpeechInStartRequest(sessionId, slotNum))
		} else {
		  myListenerManager.exposedUpdateListeners(ContinuousSpeechInStopRequest(sessionId))
		}
	  }
	  	
}
