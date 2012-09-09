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
	import java.util.Date
	import org.cogchar.lifter.model.PageCommander

	class JavaScriptActor extends CometActor with CometListener with Logger {
      
	  // On initial render, just blank anything in JavaScriptActor comet div
	  def render = "@JSCommandSlot" #> NodeSeq.Empty
  
	  lazy val mySessionId : Int = (name openOr"-1").toInt
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  def triggerId(sessionId:Int, functionId: Int) = PageCommander.controlId(sessionId:Int, functionId: Int)
	  final val SPEECH_REQUEST_TRIGGERID = triggerId(mySessionId, 201)
	  final val LOAD_PAGE_TRIGGERID = triggerId(mySessionId, 202)
	  final val SPEECH_OUT_TRIGGERID = triggerId(mySessionId, 203)
	  final val CONTINUOUS_SPEECH_REQUEST_START_TRIGGERID = triggerId(mySessionId, 204)
	  final val CONTINUOUS_SPEECH_REQUEST_STOP_TRIGGERID = triggerId(mySessionId, 205)

	  override def lowPriority : PartialFunction[Any, Unit] = {
		case SPEECH_REQUEST_TRIGGERID => { // A special "slot" code for speech request. Sort of a workaround, but works OK for now.
			val slotId = PageCommander.getSpeechReqControl
			partialUpdate(new JsCmd { 
				// Put our oddball JS methods in a try block, so non-Proctor browsers are happy!
				def toJsCmd = "try{Android.getSpeechInput(" + slotId + ");} catch(err) {}" 
			  })
		  }
		case LOAD_PAGE_TRIGGERID => { // A special "slot" code for page redirect.
			val newPage = PageCommander.getRequestedPage(mySessionId)
			newPage match {
			  case Some(page) => partialUpdate(JsCmds.RedirectTo(page))
			  case None => JsCmds.Noop
			}  
		  }
		case SPEECH_OUT_TRIGGERID => { // This code results in a request for speech output on Android
			info("Sending speech to Android...")
			val text = PageCommander.getOutputSpeech(mySessionId)
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.outputSpeech(\"" + text + "\");} catch(err) {}"
			  })
		  }
		case CONTINUOUS_SPEECH_REQUEST_START_TRIGGERID => { // This code for starting continuous speech. 
			val slotId = PageCommander.getSpeechReqControl
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.getContinuousSpeechInput(" + slotId + ");} catch(err) {}"
			  })
		  }
		case CONTINUOUS_SPEECH_REQUEST_STOP_TRIGGERID => { // This code for stopping continuous speech. 
			//val slotNum = PageCommander.getSpeechReqControl
			partialUpdate(new JsCmd { 
				def toJsCmd = "try{Android.stopContinuousSpeechInput();} catch(err) {}"
			  })
		  }
		case _ => // Do nothing for other IDs
	  }
	}
  }
}
