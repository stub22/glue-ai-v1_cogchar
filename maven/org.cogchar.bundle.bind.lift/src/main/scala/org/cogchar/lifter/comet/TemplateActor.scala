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
	import S._
	import net.liftweb.util._
	import Helpers._
	import scala.xml._
	import org.cogchar.lifter.model.{ActorCodes, PageCommander}
	import org.cogchar.lifter.view.TextBox

	class TemplateActor extends CometActor with CometListener with Logger {
	  
	  lazy val mySessionId = {
		S.session match {
		  case Full(myLiftSession) => {
			myLiftSession.uniqueId
		  }
		  case _ => ""
		}
	  }
	  
	  lazy val myUpdateTag = mySessionId + "_" + ActorCodes.TEMPLATE_CODE
	  
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {
		case a: String if (a.equals(myUpdateTag)) => {reRender();} // A special code to trigger a refresh of template
		case _: String => // Do nothing if our ID not matched
	  }

	  def render = {
		if (mySessionId.isEmpty) {
		  error("TemplateActor cannot get sessionId, not rendering!")
		  TextBox.makeBox("TemplateActor cannot get sessionId, not rendering!", "", true)
		} else {
		  val desiredTemplate = PageCommander.getCurrentTemplate(mySessionId)
		  if (desiredTemplate == null) { // If so, things are still initializing, we'll render loading message:
			"@TemplateSlot" #> <h1>Loading, please wait...</h1>
		  } else {
			// On the surface, this is rather bad form. Generally actors should get their update information through
			// the actor message (which could be a case class containing any needed fields), not a callback into the
			// very object to which they are registered as an actor!
			// But there is a reason this works "better" in this case. When a session is first rendered, the template
			// actor in the default.html base Lift template is not yet active, but the browser will call this
			// method for that initial render. At that point, as PageCommander is currently set up, the correct template
			// info is already available, and the page renders correctly.
			// If the update information is passed in the actor message and not via this callback, we must jump through
			// additional hoops in a brittle way, and attempt to wait sufficiently long after the basic Lift template is initially rendered
			// for Comet to be active on both the client and server sides. Only then can the embedded Lifter template be
			// updated via Comet actor messages.
			// This has been shown to work, but the workarounds involved are less elegant than this inelegance:
			"@TemplateSlot" #> <lift:surround with={desiredTemplate} at="content"/>
		  }
		}
	  }
	}
  }
}
