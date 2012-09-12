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
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.view.TextBox

	class TemplateActor extends CometActor with CometListener with Logger {
	  
	  final val DEFAULT_TEMPLATE = "12slots"
	  
	  lazy val mySessionId = {
		S.session match {
		  case Full(myLiftSession) => {
			myLiftSession.uniqueId
		  }
		  case _ => ""
		}
	  }
	  
	  lazy val myUpdateTag = mySessionId + "_301"
	  
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
		  if (desiredTemplate == null) { // If so, things are still initializing, we'll render default template for now (rendering just a message in a paragraph, or blank, breaks JS part of control comet!
			PageCommander.requestStart(mySessionId)
			"@TemplateSlot" #> <lift:surround with={DEFAULT_TEMPLATE} at="content"/>
		  } else {
			"@TemplateSlot" #> <lift:surround with={desiredTemplate} at="content"/>
		  }
		}
	  }
	}
  }
}
