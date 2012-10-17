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

package org.cogchar.lifter.snippet

import org.appdapter.core.name.FreeIdent
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.{ControlToggler,LifterState}
import org.cogchar.lifter.model.handler.{AbstractControlInitializationHandler,LifterVariableHandler}
import scala.xml.NodeSeq

object ToggleButton extends AbstractControlInitializationHandler {
	  
  protected val matchingName = "TOGGLEBUTTON"
  
  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
	val sessionState = state.stateBySession(sessionId)
	// Load the "full" action (with an action local name containing actions for each state) into toggleButtonFullActionMap
	sessionState.toggleControlMultiActionsBySlot(slotNum) = control.action
	// Next we need to see if an app variable linked to this toggle button is already set and set the button state to match if so
	val buttonState = LifterVariableHandler.getStateFromVariable(state, sessionId, control.action)
	// Flag the fact this is a toggle button and set current state via toggleButtonMap
	sessionState.toggleControlStateBySlot(slotNum) = buttonState
	// A TOGGLEBUTTON trick: we have copied the full action for this control to toggleButtonFullActionMap - now
	// we rewrite this control's action in the controlDefMap depending on its state.
	// A bit problematic and there may be a better way, but this lets the action handler chain work the same for 
	// TOGGLEBUTTONS as for everything else.
	val toggler = ControlToggler.getTheToggler
	toggler.setSingularAction(state, sessionId, slotNum, buttonState)
	// Another trick: we set the type to PUSHYBUTTON in the controlDefMap so ControlToggler will render control as
	// a PushyButton
	val myControlDef = sessionState.controlConfigBySlot(slotNum)
	myControlDef.controlType = "PUSHYBUTTON"
	// Get control NodeSeq to render for state
	toggler.getSingularControlXml(sessionId, slotNum, myControlDef, buttonState)
  }
	 
}
