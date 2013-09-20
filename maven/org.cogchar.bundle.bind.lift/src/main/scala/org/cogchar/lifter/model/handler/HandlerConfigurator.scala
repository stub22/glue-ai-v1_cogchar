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

import org.cogchar.lifter.snippet._
import org.cogchar.lifter.view._

// A class with methods to initialize the handler chains - may be a cleaner way to do this...
// Stateless, so OK to make this an object?
object HandlerConfigurator {
  def initializeActionHandlers:AbstractLifterActionHandler = {
	// Instantiate each of the action handlers
	val liftConfigHandler = new LiftConfigHandler
	val cinematicHandler = new CinematicHandler
	val variableHandler = new LifterVariableHandler
	val sceneHandler = new SceneTriggerHandler
	val commandHandler = new LifterCommandActionHandler
	val lifterQueryHandler = new LifterQueryActionHandler
	val repoOutputHandler = new RepoOutputHandler
	val robotAnimHandler = new RobotAnimationHandler
    val questionAndAnswerHandler = new QuestionAndAnswerHandler


	// Set up the chain
	liftConfigHandler setNextHandler cinematicHandler
	cinematicHandler setNextHandler variableHandler
	variableHandler setNextHandler sceneHandler
	sceneHandler setNextHandler commandHandler
	commandHandler setNextHandler lifterQueryHandler
	lifterQueryHandler setNextHandler repoOutputHandler
	repoOutputHandler setNextHandler robotAnimHandler
    robotAnimHandler setNextHandler questionAndAnswerHandler
	// Return the first handler in chain
	liftConfigHandler
  }
  def initializeCommandHandlers:AbstractLifterCommandHandler = {
	// Instantiate each of the command handlers
	val speechCommandHandler = new SpeechCommandHandler
	val submitHandler = new SubmitCommandHandler
	val submitTextHandler = new SubmitTextCommandHandler
	val showTextHandler = new ShowTextCommandHandler
	val updateHandler = new UpdateCommandHandler
	val oldDemoHandler = new OldDemoCommandHandler
	val lastConfigHandler = new LastConfigCommandHandler
	val databallsHandler = new DataballsCommandHandler
	// Set up the chain
	speechCommandHandler setNextHandler submitHandler
	submitHandler setNextHandler submitTextHandler
	submitTextHandler setNextHandler showTextHandler
	showTextHandler setNextHandler updateHandler
	updateHandler setNextHandler oldDemoHandler
	oldDemoHandler setNextHandler lastConfigHandler
	lastConfigHandler setNextHandler databallsHandler
	// Return the first handler in chain
	speechCommandHandler
  }
  def initializeControlInitializationHandlers:AbstractControlInitializationHandler = {
	// Set up the chain
	PushyButton setNextHandler ToggleButton
	ToggleButton setNextHandler TextBox
	TextBox setNextHandler TextForm
	TextForm setNextHandler DualTextForm
	DualTextForm setNextHandler InsertMarkup
	InsertMarkup setNextHandler ListBox
	ListBox setNextHandler LoginForm
	LoginForm setNextHandler RadioButtons
	RadioButtons setNextHandler SelectBoxes
	SelectBoxes setNextHandler VideoBox
	VideoBox setNextHandler LinkList
	// Return the first handler in chain
	PushyButton
  }
}
