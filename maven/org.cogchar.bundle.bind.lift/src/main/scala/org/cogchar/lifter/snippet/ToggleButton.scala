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
import org.cogchar.lifter.model.{ActionStrings,PageCommander}
import org.cogchar.lifter.model.handler.{AbstractControlInitializationHandler,LifterVariableHandler}

object ToggleButton extends AbstractControlInitializationHandler {
	  
  protected val matchingName = "TOGGLEBUTTON"
  
  protected def handleHere(sessionId:String, slotNum:Int, control:ControlConfig) {
	// For a ToggleButton, the first item in CSV text, action, style, image corresponds to the default condition, the second to the "toggled" condition
	var textItems = List.fromArray(control.text.split(ActionStrings.stringAttributeSeparator))
	var styleItems = List.fromArray(control.style.split(ActionStrings.stringAttributeSeparator))
	var resourceItems = List.fromArray(control.resource.split(ActionStrings.stringAttributeSeparator))
	var actionItems = List.fromArray(control.action.getLocalName.split(ActionStrings.multiCommandSeparator))
	PageCommander.getState.toggleButtonFullActionMap(sessionId)(slotNum) = control.action
	// Next we need to see if an app variable linked to this toggle button is already set and set the button state to match if so
	val buttonState = LifterVariableHandler.getToggleButtonStateFromVariable(sessionId, control.action)
	// Flag the fact this is a toggle button and set current state
	PageCommander.getState.toggleButtonMap(sessionId)(slotNum) = buttonState
	// Set control for state
	// If only one parameter is specified in RDF, duplicate the first and use that parameter for the other state too (really we are prepending the one item in the list to itself, but that works ok here)
	if (textItems.length < 2) textItems ::= textItems(0)
	if (styleItems.length < 2) styleItems ::= styleItems(0)
	if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
	if (actionItems.length < 2) actionItems ::= actionItems(0)
	val stateIndex = if (buttonState) 1 else 0
	PageCommander.getState.controlsMap(sessionId)(slotNum) = 
	  PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum)
	// A TOGGLEBUTTON trick: we have copied the full action for this control to toggleButtonFullActionMap - now
	// we rewrite this control's action in the controlDefMap depending on its state.
	// A bit problematic and there may be a better way, but this lets the action handler chain work the same for 
	// TOGGLEBUTTONS as for everything else.
	PageCommander.getState.controlDefMap(sessionId)(slotNum).action =
	  new FreeIdent(PageCommander.getUriPrefix(control.action) + actionItems(stateIndex), actionItems(stateIndex))
  }
	 
}
