/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.lifter.model.main
import scala.xml.NodeSeq
import net.liftweb.actor.LiftActor
import org.appdapter.core.name.{FreeIdent, Ident}


// Case class for messaging ControlActor
case class ControlChange(sessionId:String, slotNum:Int, markup:NodeSeq)
	  
// Case classes for messaging JavaScriptActor
case class HtmlPageRequest(sessionId:String, pagePathOption:Option[String])

case class HtmlPageRefreshRequest(sessionId:String)
	  
// Case classes for controls to message back into PageCommander
case class ControlAction(sessionId:String, slotNum:Int)
case class ControlTextInput(sessionId:String, slotNum:Int, text:Array[String])
case class ControlMultiSelect(sessionId:String, slotNum:Int, subControl:Int)
case class ControlMultiAction(sessionId:String, slotNum:Int, subControl:Int, multiActionFlag:Boolean)
	  
	
