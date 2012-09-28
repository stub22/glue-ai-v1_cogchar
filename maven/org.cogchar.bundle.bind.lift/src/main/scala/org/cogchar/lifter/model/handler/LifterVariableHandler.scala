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

import org.appdapter.core.name.Ident
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.{ActionStrings,PageCommander}
import org.cogchar.lifter.snippet.PushyButton
import net.liftweb.common.Logger
import scala.collection.mutable.ArrayBuffer

// A handler for lifter variables
class LifterVariableHandler extends AbstractLifterActionHandler with Logger {
  
  protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_liftvar, ActionStrings.p_liftsessionvar)
  
  protected def handleHere(sessionId:String, slotId:Int, control:ControlConfig, input:Array[String]) {
	val varName = control.action.getLocalName
	val controlType = PageCommander.getControlType(control)
	var variablesMap: scala.collection.mutable.HashMap[String, String] = null;
	var sessionVar = false
	if (PageCommander.getUriPrefix(control.action) equals ActionStrings.p_liftvar) {variablesMap = PageCommander.getState.publicAppVariablesMap}
	else {variablesMap = PageCommander.getState.appVariablesMap(sessionId); sessionVar = true}
	controlType match {
	  case PageCommander.ControlType.PUSHYBUTTON => {
		  if (variablesMap contains varName) {
			if (variablesMap(varName).toBoolean) {
			  variablesMap(varName) = false.toString 
			} else {
			  variablesMap(varName) = true.toString
			}
		  } else {
			variablesMap(varName) = true.toString
		  }
		}
	  case PageCommander.ControlType.TOGGLEBUTTON => {
		  val toggleButtonState = PageCommander.getState.toggleButtonMap(sessionId)(slotId) // assumes button has already been toggled on press
		  variablesMap(varName) = toggleButtonState.toString
		  if (!sessionVar) PageCommander.setAllPublicLiftvarToggleButtonsToState(varName, toggleButtonState)
		}
	  case PageCommander.ControlType.LISTBOX | PageCommander.ControlType.RADIOBUTTONS => {
		  val textItems = List.fromArray(control.text.split(","))
		  val textIndex = input(0).toInt + 1
		  variablesMap(varName) = textItems(textIndex)
		}
	  case _ => {
		  warn("Lifter Variable action found, but control type was not valid: " + controlType)
		}
	}
	var variableTypeString = "global"
	if (sessionVar) variableTypeString = "session"
	try {
	  info("Exiting LifterVariableHandler; " + variableTypeString + " lifter variable " + varName + " is now set to " + variablesMap(varName))
	} catch {
	  case _: Any => // If this fails it's probably because control type was invalid and lifter variable was not set -- just exit
	}
  }
  
  // A method to synchronize the state of toggle buttons in all sessions which are connected to the state of a global lifter variable
  def setAllPublicLiftvarToggleButtonsToState(varName:String, state:Boolean) {
	val lifterState = PageCommander.getState
	lifterState.activeSessions.foreach(sessionId => {
		lifterState.toggleButtonMap(sessionId).keySet.foreach(slotNum => {
			val actionIdent = lifterState.controlDefMap(sessionId)(slotNum).action
			if (ActionStrings.p_liftvar.equals(PageCommander.getUriPrefix(actionIdent)) && varName.equals(actionIdent.getLocalName)) {
			  var textItems = List.fromArray(lifterState.controlDefMap(sessionId)(slotNum).text.split(","))
			  var styleItems = List.fromArray(lifterState.controlDefMap(sessionId)(slotNum).style.split(","))
			  var resourceItems = List.fromArray(lifterState.controlDefMap(sessionId)(slotNum).resource.split(","))
			  // If only one parameter is specified in RDF, duplicate the first and use that parameter here too (really we are prepending the one item in the list to itself, but that works ok here)
			  if (textItems.length < 2) textItems ::= textItems(0)
			  if (styleItems.length < 2) styleItems ::= styleItems(0)
			  if (resourceItems.length < 2) resourceItems ::= resourceItems(0)
			  val stateIndex = if (state) 1 else 0
			  PageCommander.setControl(sessionId, slotNum, PushyButton.makeButton(textItems(stateIndex), styleItems(stateIndex), resourceItems(stateIndex), slotNum))
			  lifterState.toggleButtonMap(sessionId)(slotNum) = state
			}
		  })
	  })
  }
  
}

object LifterVariableHandler {
  // Checks to see if a toggle button already has a state defined by a Lifter variable
  // Returns this state if so, otherwise returns false
  def getToggleButtonStateFromVariable(sessionId:String, action:Ident): Boolean = {
	val actionUriPrefix = PageCommander.getUriPrefix(action);
	var buttonState = false
	val appState = PageCommander.getState
	actionUriPrefix match {
	  case ActionStrings.p_liftvar => {
		  val mappedVariable = action.getLocalName();
		  if (appState.publicAppVariablesMap contains mappedVariable) {
			buttonState = appState.publicAppVariablesMap(mappedVariable).toBoolean
		  }
		}
	  case ActionStrings.p_liftsessionvar => {
		  val mappedVariable = action.getLocalName();
		  if (appState.appVariablesMap(sessionId) contains mappedVariable) {
			buttonState = appState.appVariablesMap(sessionId)(mappedVariable).toBoolean
		  }
		}
	  case _ =>
	}
	buttonState
  }
}
