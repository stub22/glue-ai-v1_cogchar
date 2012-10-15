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
import scala.xml.NodeSeq

// We don't want control-specific logic in PageCommander, but the toggle behavior is handled in a fundamentally
// special way. (Dual actions are stored in special map toggleButtonFullActionMap and controlDefMap is updated with
// "singular" action on toggle. ToggleButtonMap holds states.) We may find a better way to implement this behavior,
// but for now let's put the core toggle behavior here. This doesn't accomplish a whole lot, but makes it clear that
// ControlToggler is a distinct behavior module in Lifter. More than just the original TOGGLEBUTTON could implement
// toggle behavior if desired by simply registering in toggleButtonMap.
// Really the way toggle is implemented is pretty ugly in general, maybe it will get better...

object ControlToggler {
  
  def toggle(appState:LifterState, sessionId:String, slotNum: Int) {
	if (appState.controlDefMap(sessionId) contains slotNum) {
	  if (appState.toggleButtonFullActionMap(sessionId) contains slotNum) {
		if (appState.toggleButtonMap(sessionId) contains slotNum) {
		  val state = appState.toggleButtonMap(sessionId)(slotNum)
		  setSingularAction(appState, sessionId, slotNum, state)
		  getSingularControlXmlAndRender(sessionId, slotNum, appState.controlDefMap(sessionId)(slotNum), !state)
		  appState.toggleButtonMap(sessionId)(slotNum) = !state
		} else {
		  ControlToggler.togglerMapError(sessionId, slotNum, "toggleButtonMap")
		}
	  } else {
		ControlToggler.togglerMapError(sessionId, slotNum, "toggleButtonFullActionMap")
	  }
	} else {
	  ControlToggler.togglerMapError(sessionId, slotNum, "controlDefMap")
	}
  }

  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
  // Blur of responsibity between toggle action and lifter variables, but probably belongs here.
  def setAllPublicLiftvarToggleButtonsToState(appState:LifterState, varName:String, state:Boolean) {
	appState.activeSessions.foreach(sessionId => {
		appState.toggleButtonMap(sessionId).keySet.foreach(slotNum => {
			val control = appState.controlDefMap(sessionId)(slotNum)
			val actionIdent = control.action
			if (ActionStrings.p_liftvar.equals(PageCommander.getUriPrefix(actionIdent))
				&& varName.equals(actionIdent.getLocalName)) {  
			  getSingularControlXmlAndRender(sessionId, slotNum, control, state)
			  appState.toggleButtonMap(sessionId)(slotNum) = state
			}
		  })
	  })
  }
  
  def getSingularControlXml(sessionId:String, slotNum:Int, control:ControlConfig, state:Boolean): NodeSeq = {
	val newControl = new ControlConfig();
	newControl.controlType = control.controlType
	newControl.text = ControlToggler.getSubstringForToggleState(control.text, state)
	newControl.style = ControlToggler.getSubstringForToggleState(control.style, state)
	newControl.resource = ControlToggler.getSubstringForToggleState(control.resource, state)
	PageCommander.getXmlForControl(sessionId, slotNum, newControl)
  }
  
  def getSingularControlXmlAndRender(sessionId:String, slotNum:Int, control:ControlConfig, state:Boolean) {
	PageCommander.setControl(sessionId, slotNum, getSingularControlXml(sessionId, slotNum, control, state))
  }
  
  def setSingularAction(appState:LifterState, sessionId:String, slotNum:Int, state:Boolean) {
	val actionItem = 
	  ControlToggler.getSubstringForToggleState(appState.toggleButtonFullActionMap(sessionId)(slotNum).getLocalName,
												state, ActionStrings.multiCommandSeparator)
	val actionUriPrefix = PageCommander.getUriPrefix(appState.controlDefMap(sessionId)(slotNum).action)
	appState.controlDefMap(sessionId)(slotNum).action = new FreeIdent(actionUriPrefix + actionItem, actionItem)
  }
  
  def getStateIndex(state:Boolean) = if (state) 1 else 0
  
  def getSubstringForToggleState(stringWithSeparators:String, state:Boolean):String = {
	getSubstringForToggleState(stringWithSeparators, state, ActionStrings.stringAttributeSeparator)
  }
  
  def getSubstringForToggleState(stringWithSeparators:String, state:Boolean, separator:String): String = {
	var theList = List.fromArray(stringWithSeparators.split(separator))
	if (theList.length < 2) theList ::= theList(0)
	theList(getStateIndex(state))
  }
  
  private def togglerMapError(sessionId:String, slotNum:Int, mapName:String) {
	error("ControlToggler called for slotNum " + slotNum + " of session " + sessionId + 
		  ", but no entry found in " + mapName)
  }

}
