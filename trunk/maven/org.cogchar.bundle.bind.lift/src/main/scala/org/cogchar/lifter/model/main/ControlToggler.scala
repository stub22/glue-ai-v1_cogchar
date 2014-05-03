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

package org.cogchar.lifter.model.main

import org.appdapter.core.name.FreeIdent
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.app.LifterLogger
import org.cogchar.name.lifter.ActionStrings
import scala.xml.NodeSeq

// We don't want control-specific logic in PageCommander, but the toggle behavior is handled in a fundamentally
// special way. (Dual actions are stored in special map toggleButtonFullActionMap and controlDefMap is updated with
// "singular" action on toggle. ToggleButtonMap holds states.) We may find a better way to implement this behavior,
// but for now let's put the core toggle behavior here. This doesn't accomplish a whole lot, but makes it clear that
// ControlToggler is a distinct behavior module in Lifter. More than just the original TOGGLEBUTTON could implement
// toggle behavior if desired by simply registering in toggleButtonMap.
// Really the way toggle is implemented is pretty ugly in general, maybe it will get better...

object ControlToggler {
  lazy val theControlToggler = new ControlToggler();
  def getTheToggler = theControlToggler;
}


class ControlToggler extends LifterLogger {
  
  def toggle(appState:LifterState, sessionId:String, slotNum: Int) {
	val sessionState = appState.stateBySession(sessionId)
	val controls = sessionState.controlConfigBySlot
	if (controls contains slotNum) {
	  if (sessionState.toggleControlMultiActionsBySlot contains slotNum) {
		val toggleStateMap = sessionState.toggleControlStateBySlot
		if (toggleStateMap contains slotNum) {
		  val state = toggleStateMap(slotNum)
		  setSingularAction(appState, sessionId, slotNum, state)
		  getSingularControlXmlAndRender(sessionId, slotNum, controls(slotNum), !state)
		  toggleStateMap(slotNum) = !state
		} else {
		  togglerMapError(sessionId, slotNum, "toggleControlStateBySlot")
		}
	  } else {
		togglerMapError(sessionId, slotNum, "toggleControlMultiActionsBySlot")
	  }
	} else {
	  togglerMapError(sessionId, slotNum, "controlConfigBySlot")
	}
  }

  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
  // Blur of responsibity between toggle action and lifter variables, but probably belongs here.
  def setAllPublicLiftvarToggleButtonsToState(appState:LifterState, varName:String, state:Boolean) {
	appState.activeSessions.foreach(sessionId => {
		val toggleStateMap = appState.stateBySession(sessionId).toggleControlStateBySlot
		toggleStateMap.keySet.foreach(slotNum => {
			val control = appState.stateBySession(sessionId).controlConfigBySlot(slotNum)
			val actionIdent = control.action
			if (ActionStrings.p_liftvar.equals(PageCommander.getUriPrefix(actionIdent))
				&& varName.equals(actionIdent.getLocalName)) {  
			  getSingularControlXmlAndRender(sessionId, slotNum, control, state)
			  toggleStateMap(slotNum) = state
			}
		  })
	  })
  }
  
  def getSingularControlXml(sessionId:String, slotNum:Int, control:ControlConfig, state:Boolean): NodeSeq = {
	val newControl = new ControlConfig();
	newControl.controlType = control.controlType
	newControl.text = getSubstringForToggleState(control.text, state)
	newControl.style = getSubstringForToggleState(control.style, state)
	newControl.resource = getSubstringForToggleState(control.resource, state)
	PageCommander.getXmlForControl(sessionId, slotNum, newControl)
  }
  
  def getSingularControlXmlAndRender(sessionId:String, slotNum:Int, control:ControlConfig, state:Boolean) {
	PageCommander.setControl(sessionId, slotNum, getSingularControlXml(sessionId, slotNum, control, state))
  }
  
  def setSingularAction(appState:LifterState, sessionId:String, slotNum:Int, state:Boolean) {
	val sessionState = appState.stateBySession(sessionId)
	val controls = sessionState.controlConfigBySlot
	val actionItem = getSubstringForToggleState(sessionState.toggleControlMultiActionsBySlot(slotNum).getLocalName,
												state, ActionStrings.multiCommandSeparator)
	val actionUriPrefix = PageCommander.getUriPrefix(controls(slotNum).action)
	controls(slotNum).action = new FreeIdent(actionUriPrefix + actionItem, actionItem)
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
	myLogger.error("ControlToggler called for slotNum {} of session {}" + 
		  ", but no entry found in {}", Array[AnyRef](slotNum.asInstanceOf[AnyRef], sessionId, mapName))
  }
  
}
