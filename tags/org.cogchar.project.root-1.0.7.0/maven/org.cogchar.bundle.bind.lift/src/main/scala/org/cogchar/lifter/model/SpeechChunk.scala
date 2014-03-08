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
	
	import net.liftweb.common.Box
	import net.liftweb.json.{DefaultFormats,Extraction,JValue}
	import net.liftweb.util.Helpers.tryo

// This is essentially the JSON template. Right now it contains a string labeled speechText and a requesting control id.
// But if we wanted, we could, say, also receive the lower-confidence results which Google speech generates, or etc.
	case class SpeechChunk(speechText: String, requestingId: String)

// Right now this model is very basic: it just sits on the last piece of text sent, 
// plus it notifies PageCommander when speech is PUT, so PageCommander can do something with it if it chooses.	
	object SpeechChunk {

	  private implicit val formats = DefaultFormats

	  // A map of lastSpeech by sessionId
	  private var lastSpeech = new scala.collection.mutable.HashMap[String,String]

	  /**
	   * Convert a JValue to an Item if possible (extracts incoming JSON into SpeechChunk object)
	   */
	  def apply(in: JValue): Box[SpeechChunk] = {
		tryo{in.extract[SpeechChunk]}
	  }
	  /**
	   * Extract a JValue to an Item (allows pattern matching on incoming JSON in SpeechRestListener)
	   */
	  def unapply(in: JValue): Option[SpeechChunk] = {
		apply(in)
	  }

	  /**
	   * The default unapply method for the case class.
	   * We needed to replicate it here because we
	   * have overloaded unapply methods
	   * (so says Simply Lift, not sure I understand the need for this bit which loads SpeechChunk's speechText value into an Option)
	   */
	  def unapply(in: Any): Option[(String, String)] = {
		in match {
		  case i: SpeechChunk => Some((i.speechText, i.requestingId))
		  case _ => None
		}
	  }

	  /**
	   * Convert the item to JSON format.  This is
	   * implicit and in the companion object, so
	   * an Item can be returned easily from a JSON call
	   * This is used implicily to "read back" the JSON in HTTP response to put, generated by setContents
	   */
	  implicit def toJson(item: SpeechChunk): JValue = 
		Extraction.decompose(item)

  
	  // What do we have SpeechRestListener do with the JSON SpeechChunk object it found?
	  // Simple answer for now: just load its text into lastSpeech and let PageCommander know
	  def setContents(chunk: SpeechChunk): SpeechChunk = {
		val idItems = chunk.requestingId.split("_")
		lastSpeech(idItems(0)) = chunk.speechText
		val processThread = new Thread(new Runnable { // A new thread to call back into PageCommander to make sure we don't block Ajax handling
			def run() {
			  PageCommander ! PageCommander.ControlTextInput(idItems(0), idItems(1).toInt, Array(chunk.speechText)) // Let PageCommander know about the text so it can figure out what to do with it
			}
		  })
		processThread.start
		chunk // ... and return the SpeechChunk we just got as a "read back", which gets cast back to JSON in SpeechRestListener using toJson above
	  }
  
	  // If something wants to know what the last speech was, just call this!
	  def getLast(sessionId:String): String = lastSpeech(sessionId)   

	}

  }
}