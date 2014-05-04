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

import org.cogchar.impl.web.config.ControlConfig
import org.cogchar.impl.web.wire.{LifterState}
import org.cogchar.lifter.model.control.AbstractControlInitializationHandler
import scala.xml.NodeSeq

object InsertMarkup extends AbstractControlInitializationHandler {
  
   protected val matchingName = "INSERTMARKUP"
  
	  override protected def handleControlInit(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		insert(control.resource)
	  }
	  
	  def insert(resource:String): NodeSeq = {
		val classString = "lift:embed?what=/inserts/" + resource
		<div class={classString}/>
	  }
}
