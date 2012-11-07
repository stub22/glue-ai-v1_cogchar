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

package org.cogchar.lifter.model.handler

import org.cogchar.lifter.model.{ActionStrings,LifterState,PageCommander}
import org.cogchar.lifter.snippet.{PushyButton, RadioButtons, SelectBoxes, TextForm}
import scala.collection.mutable.ArrayBuffer

// An early, brittle hard coded demo
class OldDemoCommandHandler extends AbstractLifterCommandHandler {
  
  protected val matchingTokens = ArrayBuffer(ActionStrings.oldDemo)
  
  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	input(0).stripPrefix(ActionStrings.subControlIdentifier).toInt match { 
	  case 0 => PageCommander.setControl(sessionId, 6, PushyButton.makeButton("A button", "buttonred", "", 6))
	  case 1 => PageCommander.setControl(sessionId, 6, TextForm.makeTextForm("A text box", 6))
	  case 2 => PageCommander.setControl(sessionId, 6, SelectBoxes.makeSelectBoxes("Checkboxes", List("an option", "and another"), 6))
	  case 3 => PageCommander.setControl(sessionId, 6, RadioButtons.makeRadioButtons("Radio buttons", List("Radio Option 1", "Radio Option 2"), 6))
	  case _ =>
	}
  }
  
}
