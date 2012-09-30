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

package org.cogchar.lifter.model

import org.appdapter.core.name.FreeIdent
import org.cogchar.bind.lift.ControlConfig

// We don't want control-specific logic in PageCommander, but the toggle behavior is handled in a fundamentally
// special way. (Dual actions are stored in special map toggleButtonFullActionMap and controlDefMap is updated with
// "singular" action on toggle. ToggleButtonMap holds states.) We may find a better way to implement this behavior,
// but for now let's put the core toggle behavior here. This doesn't accomplish a whole lot, but makes it clear that
// ControlToggler is a distinct behavior module in Lifter. More than just the original TOGGLEBUTTON could implement
// toggle behavior if desired by simply registering in toggleButtonMap.
// Really the way toggle is implemented is pretty ugly in general, maybe it will get better...

class ControlToggler {
  
  def toggle(sessionId:String, slotNum: Int) {
	val appState = PageCommander.getState
	if (appState.controlDefMap(sessionId) contains slotNum) {
	  val togglingControl = appState.controlDefMap(sessionId)(slotNum)
	  val actionUriPrefix = PageCommander.getUriPrefix(togglingControl.action);
	  var textItems = List.fromArray(togglingControl.text.split(ActionStrings.stringAttributeSeparator))
	  if (appState.toggleButtonFullActionMap(sessionId) contains slotNum) {
		var actionItems = List.fromArray(appState.toggleButtonFullActionMap(sessionId)(slotNum).getLocalName.split(ActionStrings.multiCommandSeparator))
		var styleItems = List.fromArray(togglingControl.style.split(ActionStrings.stringAttributeSeparator))
		var resourceItems = List.fromArray(togglingControl.resource.split(ActionStrings.stringAttributeSeparator))
		if (appState.toggleButtonMap(sessionId) contains slotNum) {
		  if (appState.toggleButtonMap(sessionId)(slotNum)) {
			// Button is "selected" -- change back to "default" and perform action
			// If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
			if (actionItems.length < 2) actionItems ::= actionItems(0)
			// This is a little goofy and may be refactored some more. We are modifying the controlDefMap action for this control
			// so that the usual handler chain can parse it. BUT we must set the action to the "selected" value (with index 1)
			// while the control itself is toggled back to the "default" state using setControl (with index 0)
			togglingControl.action = new FreeIdent(actionUriPrefix + actionItems(1), actionItems(1))
			appState.toggleButtonMap(sessionId)(slotNum) = false
			val newControl = new ControlConfig();
			newControl.controlType = togglingControl.controlType
			newControl.text = textItems(0)
			newControl.style = styleItems(0)
			newControl.resource = resourceItems(0)	
			PageCommander.setControl(sessionId, slotNum, PageCommander.getXmlForControl(sessionId, slotNum, newControl))
		  } else {
			// Button is set as "default" -- set to "selected" and perform action
			// If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
			if (textItems.length < 2) textItems ::= textItems(0)
			if (styleItems.length < 2) styleItems ::= styleItems(0)
			if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
			// This is a little goofy and may be refactored some more. We are modifying the controlDefMap action for this control
			// so that the usual handler chain can parse it. BUT we must set the action to the "default" value (with index 0)
			// while the control itself is toggled to the "selected" state using setControl (with index 1)
			appState.controlDefMap(sessionId)(slotNum).action = new FreeIdent(actionUriPrefix + actionItems(0), actionItems(0))
			appState.toggleButtonMap(sessionId)(slotNum) = true
			val newControl = new ControlConfig();
			newControl.controlType = togglingControl.controlType
			newControl.text = textItems(1)
			newControl.style = styleItems(1)
			newControl.resource = resourceItems(1)	
			PageCommander.setControl(sessionId, slotNum, PageCommander.getXmlForControl(sessionId, slotNum, newControl))
		  }
		} else {
		  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in toggleButtonMap")
		}
	  } else {
		error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in toggleButtonFullActionMap")
	  }
	} else {
	  error("PageCommander.toggleButton called for slotNum " + slotNum + " of session " + sessionId + ", but no entry found in controlDefMap")
	}
  }

  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
  // Blur of responsibity between toggle action and lifter variables, but probably belongs here.
  def setAllPublicLiftvarToggleButtonsToState(varName:String, state:Boolean) {
	val appState = PageCommander.getState
	appState.activeSessions.foreach(sessionId => {
		appState.toggleButtonMap(sessionId).keySet.foreach(slotNum => {
			val control = appState.controlDefMap(sessionId)(slotNum)
			val actionIdent = control.action
			if (ActionStrings.p_liftvar.equals(PageCommander.getUriPrefix(actionIdent)) && varName.equals(actionIdent.getLocalName)) {
			  var textItems = List.fromArray(control.text.split(ActionStrings.stringAttributeSeparator))
			  var styleItems = List.fromArray(control.style.split(ActionStrings.stringAttributeSeparator))
			  var resourceItems = List.fromArray(control.resource.split(ActionStrings.stringAttributeSeparator))
			  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
			  if (textItems.length < 2) textItems ::= textItems(0)
			  if (styleItems.length < 2) styleItems ::= styleItems(0)
			  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
			  val stateIndex = if (state) 1 else 0
			  val newControl = new ControlConfig();
			  newControl.controlType = control.controlType
			  newControl.text = textItems(stateIndex)
			  newControl.style = styleItems(stateIndex)
			  newControl.resource = resourceItems(stateIndex)
			  PageCommander.setControl(sessionId, slotNum, PageCommander.getXmlForControl(sessionId, slotNum, newControl))
			  appState.toggleButtonMap(sessionId)(slotNum) = state
			}
		  })
	  })
  }

}
