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
	import S._
	import net.liftweb.http.js.JsCmd
	import net.liftweb.http.js.JsCmds._
	import net.liftweb.util._
	import Helpers._
	import scala.xml._
	import org.cogchar.lifter.model.PageCommander

	
	class TemplateActor extends CometActor with CometListener {
	  
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {
		case 301 => {reRender();} // A special code to trigger a refresh of template
		case _: Int => // Do nothing if our ID not matched
	  }

	  def render = {
		val desiredTemplate = PageCommander.getCurrentTemplate
		"@TemplateSlot" #> <lift:surround with={desiredTemplate} at="content"/>
	  }
  
	}

  }
}
