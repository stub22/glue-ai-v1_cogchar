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

package org.cogchar.lifter.view

import scala.xml.NodeSeq	
import net.liftweb.http._
import net.liftweb.util._
import Helpers._
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.LifterState
import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler

object InsertMarkup extends AbstractControlInitializationHandler {
  
   protected val matchingName = "INSERTMARKUP"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		insert(control.resource)
	  }
	  
	  def insert(resource:String): NodeSeq = {
		val classString = "lift:embed?what=/inserts/" + resource
		<div class={classString}/>
	  }
}
