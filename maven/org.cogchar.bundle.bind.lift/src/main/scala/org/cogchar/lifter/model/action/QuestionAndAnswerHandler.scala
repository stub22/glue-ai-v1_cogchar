
/**  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
import org.cogchar.impl.web.wire.{LifterState}
import scala.collection.mutable.ArrayBuffer

/**
 * This Handler deals with the Question and Answer page pushy buttons, which 
 * push ThingActions to the repo in response to the buttons pressed.
 * 
 * 
 * @author Jason R. Eads <jeads362@gmail.com>
 */
class QuestionAndAnswerHandler extends AbstractLifterActionHandler {

  //TODO: migrate to ActionStrings
  override protected val matchingPrefixes = ArrayBuffer("http://www.cogchar.org/lift/question_and_answer#") 
  
  override protected def handleAction(state:LifterState, sessionId:String, slotNum:Int, control:WebControlImpl, input:Array[String]) {
	val success = myLiftAmbassador.sendActionViaRepo(control.action, sessionId)
  }
  
}
