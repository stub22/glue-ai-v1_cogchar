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

package org.cogchar.lifter.model.command
import org.cogchar.name.lifter.{ActionStrings}
import org.cogchar.lifter.model.main.{LifterState,PageCommander}
import scala.collection.mutable.ArrayBuffer

class UpdateCommandHandler extends AbstractLifterCommandHandler {

  protected val matchingTokens = ArrayBuffer(ActionStrings.update, ActionStrings.refreshLift)
  
 override  protected def handleCommand(state:LifterState, sessionId:String, slotNum:Int, command:String, input:Array[String]) {
	val splitAction = command.split(ActionStrings.commandTokenSeparator)
	splitAction(0) match {
	  case ActionStrings.update => {
		  PageCommander.getLiftAmbassador.performCogCharUpdate(splitAction(1))
		}
	  case ActionStrings.refreshLift => {
		  state.lifterInitialized = false;
		  PageCommander.getLiftAmbassador.clearLiftConfigCache
		  PageCommander.getLiftAmbassador.performCogCharUpdate(ActionStrings.LIFT_REFRESH_UPDATE_NAME)
		}
	  case _ =>
	}
  }
  
}
