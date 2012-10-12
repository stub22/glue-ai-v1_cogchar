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
	import net.liftweb.http._
	import net.liftweb.util._
	import Helpers._
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.view.TextBox
	import S._

	
	class ControlActor extends CometActor with CometListener {
  
	  lazy val slotNum = (name openOr"-1").toInt
	  
	  lazy val mySessionId = {
		S.session match {
		  case Full(myLiftSession) => {
			myLiftSession.uniqueId
		  }
		  case _ => ""
		}
	  }
	  
	  lazy val fullId = mySessionId + "_" + slotNum 
	 
	 
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {
		case a: String if (a.equals(fullId)) => reRender
		case _: Any => // Do nothing if our ID not matched
	  }

	  def render = {
		if (mySessionId.isEmpty) {
		  error("ControlActor cannot get sessionId, not rendering!")
		  TextBox.makeBox("ControlActor cannot get sessionId, not rendering!", "", true)
	  } else {
		  // On the surface, this is rather bad form. Generally actors should get their update information through
		  // the actor message (which could be a case class containing any needed fields), not a callback into the
		  // very object to which they are registered as an actor!
		  // But there is a reason this works "better" in this case. When a Lifter template is first rendered, the control
		  // actors in the template are not yet active, but the browser will call this
		  // method for that initial render. At that point, as PageCommander is currently set up, the correct control
		  // info is already available, and the page renders correctly.
		  // If the update information is passed in the actor message and not via this callback, we must jump through
		  // additional hoops in a brittle way, and attempt to wait sufficiently long after the template is initially rendered
		  // for Comet to be active on both the client and server sides. Only then can the controls be
		  // updated via Comet actor messages.
		  // This has been shown to work, but the workarounds involved are less elegant than this inelegance:
		  "@ControlSlot" #> PageCommander.getNode(mySessionId, slotNum)
		}
	  }
	}

  }
}
