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


import org.cogchar.lifter.model.action._
import org.cogchar.lifter.model.command._
import org.cogchar.lifter.model.control._

import org.cogchar.impl.web.config.{LiftAmbassador}
import org.cogchar.impl.web.wire.{WebappGlobalState, SessionOrganizer, WebappCommander}

// A class with methods to initialize the handler chains - may be a cleaner way to do this...
// Stateless, so OK to make this an object?
object HandlerConfigurator {
  def initializeActionHandlers(liftAmb: LiftAmbassador, wgs : WebappGlobalState, sessOrg : SessionOrganizer, wcmdr : WebappCommander):AbstractLifterActionHandler = {
    // Instantiate each of the action handlers
    val liftConfigHandler = new LiftConfigHandler(liftAmb)
    val cinematicHandler = new CinematicHandler(liftAmb)
    val variableHandler = new LifterVariableHandler(liftAmb, wgs, sessOrg)
    val sceneHandler = new SceneTriggerHandler(liftAmb)
    val commandHandler = new CommandInvokerActionHandler(liftAmb, sessOrg)  // Tricky devil - is an actionHandler that invokes command-chain
    val lifterQueryHandler = new LifterQueryActionHandler(liftAmb, sessOrg, wcmdr)
    val repoOutputHandler = new RepoOutputHandler(liftAmb)
    val robotAnimHandler = new RobotAnimationHandler(liftAmb)
    val questionAndAnswerHandler = new QuestionAndAnswerHandler(liftAmb)
    val flowActionHandler = new FlowActionHandler(liftAmb)


    // Set up the chain
    liftConfigHandler setNextActionHandler cinematicHandler
    cinematicHandler setNextActionHandler variableHandler
    variableHandler setNextActionHandler sceneHandler
    sceneHandler setNextActionHandler commandHandler
    commandHandler setNextActionHandler lifterQueryHandler
    lifterQueryHandler setNextActionHandler repoOutputHandler
    repoOutputHandler setNextActionHandler robotAnimHandler
    robotAnimHandler setNextActionHandler questionAndAnswerHandler
    questionAndAnswerHandler setNextActionHandler flowActionHandler
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
    speechCommandHandler setNextCommandHandler submitHandler
    submitHandler setNextCommandHandler submitTextHandler
    submitTextHandler setNextCommandHandler showTextHandler
    showTextHandler setNextCommandHandler updateHandler
    updateHandler setNextCommandHandler oldDemoHandler
    oldDemoHandler setNextCommandHandler lastConfigHandler
    lastConfigHandler setNextCommandHandler databallsHandler
    // Return the first handler in chain
    speechCommandHandler
  }
  def initializeControlInitializationHandlers(sorg : SessionOrganizer):AbstractControlInitializationHandler = {
	 // These are lift-"snippet" instances we keep around as handlers, but they are also isntantiatable 
	 // by LiftWeb directly.
	val pButton = new PushyButton
	val textBox = new TextBox
	val vidBox = new VideoBox
	val vidBoxMuted = new VideoBoxMuted
	val insertMarkup = new InsertMarkup
	val extFrame = new ExtFrame
	// The above snippet-handlers do not use session data, the below ones do.
	// However, we cannot easily reflect this fact using a constructor argument,
	// because the lift framework is free to instantiate these snippets during page handling.
	// Hence we have created the SnippetHelper singleton so that these snippets can
	// find the SessionOrganizer.  
	val togButton = new ToggleButton // (sorg)
	val selBoxes = new SelectBoxes // (sorg)
	val listBox = new ListBox   // (sorg)
	val loginForm = new LoginForm  // (sorg)
	val radioButtons = new RadioButtons // (sorg)
	val linkList = new LinkList   // (sorg)
	val textForm = new TextForm   // (sorg)
	val dualTextForm = new DualTextForm   /// (sorg)
	 
    // Set up the chain
    pButton setNextHandler togButton //  ToggleButton
    // ToggleButton 
	togButton setNextHandler textBox //  TextBox
    textBox setNextHandler textForm // TextForm
    textForm setNextHandler dualTextForm // DualTextForm
    dualTextForm setNextHandler insertMarkup // InsertMarkup
    insertMarkup setNextHandler listBox //  ListBox
    listBox setNextHandler loginForm //  LoginForm
    loginForm setNextHandler radioButtons // RadioButtons
    radioButtons setNextHandler selBoxes // SelectBoxes
    selBoxes setNextHandler vidBox // VideoBox
    vidBox setNextHandler vidBoxMuted // VideoBoxMuted 
    vidBoxMuted setNextHandler extFrame //  ExtFrame
    extFrame setNextHandler linkList //  LinkList
    // Return the first handler in chain
    pButton
  }
}
