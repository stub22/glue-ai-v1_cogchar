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
  package comet {

	import net.liftweb.common._
	import net.liftweb.http.js.JE._
	import net.liftweb.http._
	import net.liftweb.http.js.JsCmd
	import net.liftweb.http.js.JsCmds
	import net.liftweb.util._
	import Helpers._
	import scala.xml._
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.model.PageCommander._

	class JavaScriptActor extends CometActor with CometListener with Logger {
      
	  // On initial render, just blank anything in JavaScriptActor comet div
	  def render = "@JSCommandSlot" #> NodeSeq.Empty
  
	  lazy val mySessionId = {
		S.session match {
		  case Full(myLiftSession) => {
			myLiftSession.uniqueId
		  }
		  case _ => ""
		}
	  }
	  def registerWith = org.cogchar.lifter.model.PageCommander

	  override def lowPriority : PartialFunction[Any, Unit] = {
		case req: SpeechInRequest if (req.sessionId == mySessionId) => {
			partialUpdate(new JsCmd { 
				// Put our oddball JS methods in a try block, so non-Proctor browsers are happy!
				def toJsCmd = "try{Android.getSpeechInput(\"" + controlId(req.sessionId,req.slotNum) + "\");} catch(err) {}" 
			  })
		  }
		case req: HtmlPageRequest if (req.sessionId == mySessionId) => {
			val newPage = req.pagePathOption
			newPage match {
			  case Some(page) => partialUpdate(JsCmds.RedirectTo(page))
			  case None => JsCmds.Noop
			}  
		  }
		case req: SpeechOutRequest if (req.sessionId == mySessionId) => {
			info("Sending speech to Android...")
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.outputSpeech(\"" + req.text + "\");} catch(err) {}"
			  })
		  }
		case req: ContinuousSpeechInStartRequest if (req.sessionId == mySessionId) => {
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.getContinuousSpeechInput(\"" + controlId(req.sessionId,req.slotNum) + "\");} catch(err) {}"
			  })
		  }
		case req: ContinuousSpeechInStopRequest if (req.sessionId == mySessionId) => {
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.stopContinuousSpeechInput();} catch(err) {}"
			  })
		  }
		case req: HtmlPageRefreshRequest if (req.sessionId == mySessionId) => {
			partialUpdate(new JsCmd { 
				def toJsCmd = "window.location.reload();"
			  })
		  }
		case _ => // Do nothing for other IDs
	  }
	   
	  def controlId(sessionId:String, controlId: Int): String = {
		sessionId + "_" + controlId
	  }
	}
  }
}
