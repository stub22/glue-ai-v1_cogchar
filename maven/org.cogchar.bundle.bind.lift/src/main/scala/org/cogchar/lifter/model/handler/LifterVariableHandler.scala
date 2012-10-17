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

import java.lang.NumberFormatException
import org.appdapter.core.name.Ident
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.{ActionStrings,ControlToggler,LifterState,PageCommander}
import org.cogchar.lifter.snippet.PushyButton
import net.liftweb.common.Logger
import scala.collection.mutable.ArrayBuffer

// A handler for lifter variables
class LifterVariableHandler extends AbstractLifterActionHandler with Logger {
  
  protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_liftvar, ActionStrings.p_liftsessionvar)
  
  protected def handleHere(state:LifterState, sessionId:String, slotId:Int, control:ControlConfig, input:Array[String]) {
	val varName = control.action.getLocalName
	var variablesMap: scala.collection.mutable.Map[String, String] = null;
	var sessionVar = false
	if (PageCommander.getUriPrefix(control.action) equals ActionStrings.p_liftvar) {variablesMap = state.globalLifterVariablesByName}
	else {variablesMap = state.stateBySession(sessionId).sessionLifterVariablesByName; sessionVar = true}
	if (input != null) { // If so, we have a value for the variable
	  val textItems = List.fromArray(control.text.split(","))
	  if (input(0).startsWith(ActionStrings.subControlIdentifier)) {
		// Looks like we have a multiselect control sending us a subControl id
		// We'll set the variable to the label of the subControl selected
		val textIndex = input(0).stripPrefix(ActionStrings.subControlIdentifier).toInt + 1
		variablesMap(varName) = textItems(textIndex)
	  } else {
		variablesMap(varName) = input(0)
	  }
	} else { // If so, a button type control has this Lifter Variable action.
	  var toggleButton = false;
	  if (state.stateBySession contains sessionId) {
		val toggleStateMap = state.stateBySession(sessionId).toggleControlStateBySlot
		if (toggleStateMap contains slotId) {
		  toggleButton = true;
		  val toggleButtonState = toggleStateMap(slotId) // assumes button has already been toggled on press (by toggler)
		  variablesMap(varName) = toggleButtonState.toString
		  if (!sessionVar) ControlToggler.getTheToggler.setAllPublicLiftvarToggleButtonsToState(state, varName, toggleButtonState)
		}
	  }
	  if (!toggleButton) {
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
	}
	var variableTypeString = if (sessionVar) "session" else "global"
	info("Exiting LifterVariableHandler; " + variableTypeString + " lifter variable " + varName + " is now set to " + variablesMap(varName))
  }
}

object LifterVariableHandler {
  // Checks to see if an action links to a boolean state stored in a Lifter Variable
  // Returns this state if so, otherwise returns false
  // Currently used to allow TOGGLEBUTTON state to persist and reload when the control is removed and redisplayed.
  def getStateFromVariable(appState:LifterState, sessionId:String, action:Ident): Boolean = {
	val actionUriPrefix = PageCommander.getUriPrefix(action);
	var state = false
	actionUriPrefix match {
	  case ActionStrings.p_liftvar => {
		  val mappedVariable = action.getLocalName();
		  if (appState.globalLifterVariablesByName contains mappedVariable) {
			try {
			  state = appState.globalLifterVariablesByName(mappedVariable).toBoolean
			} catch {
			  case e: NumberFormatException => // just leave state false if not a boolean value
			}
		  }
		}
	  case ActionStrings.p_liftsessionvar => {
		  val mappedVariable = action.getLocalName();
		  val sessionVariables = appState.stateBySession(sessionId).sessionLifterVariablesByName
		  if (sessionVariables contains mappedVariable) {
			try {
			  state = sessionVariables(mappedVariable).toBoolean
			} catch {
			  case e: NumberFormatException => // just leave state false if not a boolean value
			}
		  }
		}
	  case _ =>
	}
	state
  }
}
