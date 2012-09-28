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
	// Set up the chain
	liftConfigHandler setNextHandler cinematicHandler
	cinematicHandler setNextHandler variableHandler
	variableHandler setNextHandler sceneHandler
	sceneHandler setNextHandler commandHandler
	// Return the first handler in chain
	liftConfigHandler
  }
  def initializeCommandHandlers:AbstractLifterCommandHandler = {
	// Instantiate each of the action handlers
	val speechCommandHandler = new SpeechCommandHandler
	val submitHander = new SubmitCommandHandler
	val submitTextHandler = new SubmitTextCommandHandler
	val updateHandler = new UpdateCommandHandler
	val oldDemoHandler = new OldDemoCommandHandler
	val lastConfigHandler = new LastConfigCommandHandler
	val databallsHandler = new DataballsCommandHandler
	// Set up the chain
	speechCommandHandler setNextHandler submitHander
	submitHander setNextHandler submitTextHandler
	submitTextHandler setNextHandler updateHandler
	updateHandler setNextHandler oldDemoHandler
	oldDemoHandler setNextHandler lastConfigHandler
	lastConfigHandler setNextHandler databallsHandler
	// Return the first handler in chain
	speechCommandHandler
  }
}
