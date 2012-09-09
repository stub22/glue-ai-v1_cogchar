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
	import scala.xml._
	import org.cogchar.lifter.model.PageCommander

	
	class TemplateActor extends CometActor with CometListener with Logger {
	  
	  lazy val mySessionId : Int = (name openOr"-1").toInt
	  lazy val myUpdateTag = mySessionId + "_301"
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {
		
		case a: String if (a.equals(myUpdateTag)) => {reRender();} // A special code to trigger a refresh of template
		case _: String => // Do nothing if our ID not matched
	  }

	  def render = {
		
		val desiredTemplate = PageCommander.getCurrentTemplate(mySessionId)
		if (desiredTemplate == null) { // If so, things are still initializing, and we'll just exit without rendering for now
		  NodeSeq.Empty
		} else {
		  try {
		  } catch {
			case e: Exception => error("Error reading Lift template file: " + e)
		  }
		  var templateString = io.Source.fromInputStream(getClass.getResourceAsStream("/templates-hidden/"+desiredTemplate+".html")).mkString
		  templateString = templateString.replace("[SESSIONID]", mySessionId.toString)
		  //info("Filled template is: " + templateString) // TEST ONLY
		  XML.loadString(templateString)
		}
	  }
  
	}

  }
}
