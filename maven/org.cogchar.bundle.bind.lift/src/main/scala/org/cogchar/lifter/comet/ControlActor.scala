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

	
	class ControlActor extends CometActor with CometListener {
  
	  lazy val elementName = name openOr"NA"
	  
	  def registerWith = org.cogchar.lifter.model.PageCommander
	  
	  override def lowPriority : PartialFunction[Any, Unit]  = {
		case a: String if (a.equals(elementName)) => reRender
		case _: Any => // Do nothing if our ID not matched
	  }

	  def render = {
		val idItems = elementName.split("_")
		"@ControlSlot" #> PageCommander.getNode(idItems(0).toInt, idItems(1).toInt)
	  }
  
	}

  }
}
