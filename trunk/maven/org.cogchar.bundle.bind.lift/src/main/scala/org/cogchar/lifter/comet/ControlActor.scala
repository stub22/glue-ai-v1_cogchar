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

	import net.liftweb.common.Full
	import net.liftweb.http.{CometActor, CometListener, S}
	import net.liftweb.http.js.JsCmds.SetHtml
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.view.TextBox
	import org.slf4j.LoggerFactory

	
	class ControlActor extends CometActor with CometListener {
	  
	  private val myLogger = LoggerFactory.getLogger(this.getClass); //ControlActor.getClass is not found -- why?
	  
	  final val SLOT_ID_PREFIX = "slot"
  
	  lazy val slotNum = (name openOr"-1").toInt
	  
	  lazy val slotId = SLOT_ID_PREFIX + slotNum.toString
	  
	  lazy val mySessionId = {
		S.session match {
		  case Full(myLiftSession) => {
			myLiftSession.uniqueId
		  }
		  case _ => ""
		}
	  }	 
	 
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {		
		case a: PageCommander.ControlChange if ((a.sessionId.equals(mySessionId)) && (a.slotNum == slotNum)) => {
			partialUpdate(SetHtml(slotId, a.markup)) // Works without full reRender! But requires separate id and name for each slot in template...
		}
		case _: Any => // Do nothing if our ID not matched
	  }

	  def render = {
		if (mySessionId.isEmpty) {
		  myLogger.error("ControlActor cannot get sessionId, not rendering!")
		  TextBox.makeBox("ControlActor cannot get sessionId, not rendering!", "", true)
	  } else {
		  ("#" + slotId + " *") #> PageCommander.getMarkup(mySessionId, slotNum)
		}
	  }
	}

  }
}
