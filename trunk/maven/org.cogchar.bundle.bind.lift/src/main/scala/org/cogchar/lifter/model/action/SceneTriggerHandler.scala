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

package org.cogchar.lifter.model.action

import org.appdapter.core.name.FreeIdent
import org.cogchar.impl.web.config.{WebControlImpl, LiftConfig}
import org.cogchar.name.lifter.{ActionStrings}
import org.cogchar.lifter.model.main.{PageCommander}
import org.cogchar.impl.web.wire.{LifterState}
import scala.collection.mutable.ArrayBuffer

// A handler for action URIs consisting of a "scene trigger"
// Is this really just an old paradigm for the functionality in RobotAnimationHandler?
class SceneTriggerHandler extends AbstractLifterActionHandler {

  override protected val matchingPrefixes = ArrayBuffer(ActionStrings.p_scenetrig)
  
  override protected def handleAction(state:LifterState, sessionId:String, slotNum:Int, control:WebControlImpl, input:Array[String]) {
	val success = myLiftAmbassador.triggerScene(control.action.getLocalName)
	if (success) {
	  val sceneRunningScreen = createSceneInfoScreen(state, sessionId, control)
	  PageCommander.initFromCogcharRDF(sessionId, sceneRunningScreen)
	}
  }
  
  // A method to create a liftconfig locally to serve as a "Scene Playing" info screen
  // This is an early hard coded demo that needs to be refactored or eliminated
  def createSceneInfoScreen(state:LifterState, sessionId:String, control:WebControlImpl): LiftConfig = {
	val singleSlotTemplateName = state.getSingleSlotTemplateName
	val sceneInfoConfig = new LiftConfig(singleSlotTemplateName)
	val infoButton = new WebControlImpl()
	infoButton.myURI_Fragment = "info_control_1"
	infoButton.controlType = "PUSHYBUTTON"
	infoButton.action = new FreeIdent(ActionStrings.p_liftcmd + ActionStrings.lastConfig, ActionStrings.lastConfig)
	infoButton.text = "Playing " + control.text
	infoButton.style = control.style
	infoButton.resource = control.resource
	sceneInfoConfig.myCCs.add(infoButton)
	sceneInfoConfig
  }
  
}
