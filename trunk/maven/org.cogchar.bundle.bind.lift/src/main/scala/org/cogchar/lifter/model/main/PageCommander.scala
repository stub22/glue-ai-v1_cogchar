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
import org.cogchar.name.lifter.{ActionStrings}

	import net.liftweb.http.ListenerManager
	import scala.xml.NodeSeq
	import net.liftweb.actor.LiftActor
	import org.appdapter.core.name.{FreeIdent, Ident}
	import org.cogchar.impl.web.util.LifterLogger
	import org.cogchar.impl.web.wire.{LifterState}
	import org.cogchar.lifter.model.handler.{HandlerConfigurator}
	import org.cogchar.lifter.model.action.{AbstractLifterActionHandler, LifterVariableHandler}
	import org.cogchar.lifter.model.control.{AbstractControlInitializationHandler}
	import org.cogchar.lifter.view.TextBox
	import org.cogchar.api.web.{WebControl}
	import org.cogchar.impl.web.config.{WebControlImpl, LiftAmbassador, LiftConfig, WebInstanceGlob}
	import scala.collection.JavaConverters._
	// import org.cogchar.platform.trigger.CogcharActionBinding
	
	/**
	 *
	 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
	 */
	
	// What do we think about this being an object and not a class?
	// It appears to be standard practice for LiftActors which provide features to all sessions
	// to be singleton Objects.
	// We might eventually want parts of PageCommander to be performed via a class of 
	// actors with an instance of that class created for each session.
	// That would likely be a more natural approach in Lift and is the one I would have selected when starting this
	// project, if I knew what I know now!
	object PageCommander extends LiftActor with ListenerManager with LifterLogger {
      
      def info(msg: String, params: Any*) {
          myLogger.info(msg, params.map(_.asInstanceOf[Object]).toArray)
      }
  
	  private var myAmbassador:LiftAmbassador = null // Probably it makes sense to retain a pointer to the LiftAmbassador since it is used in several methods
	  
	  private var updateInfo: String = ""
	  
	  private val myWebappState = new LifterState
	  private val myHeadActHandler : AbstractLifterActionHandler = HandlerConfigurator.initializeActionHandlers
	  private val myHeadControlInitHandler : AbstractControlInitializationHandler = HandlerConfigurator.initializeControlInitializationHandlers
	  
	  private def getSessionState(sessionId:String) = myWebappState.stateBySession(sessionId)
	  
	  // This hackish thing right here perhaps best illustrates what's wrong with the current PageCommander factoring.
	  // Generally application state is held here in theLifterState and passed to other components only as a method
	  // variable as needed. However, because of the combination of our unique situation of having snippets invoked via
	  // Comet and the fact PageCommander is a non session-aware object natively (so SessionVars don't work here), it's
	  // necessary to expose the state needed to populate snippet invocations here:
	  def hackIntoSnippetDataMap(sessionId:String) = myWebappState.getSnippetDataMapForSession(sessionId)
	  
	  def createUpdate = updateInfo
	  
	  // Case class for messaging ControlActor
	  case class ControlChange(sessionId:String, slotNum:Int, markup:NodeSeq)
	  
	  // Case classes for messaging JavaScriptActor
	  case class HtmlPageRequest(sessionId:String, pagePathOption:Option[String])

	  case class HtmlPageRefreshRequest(sessionId:String)
	  
	  // Case classes for controls to message back into PageCommander
	  case class ControlAction(sessionId:String, slotNum:Int)
	  case class ControlTextInput(sessionId:String, slotNum:Int, text:Array[String])
	  case class ControlMultiSelect(sessionId:String, slotNum:Int, subControl:Int)
	  case class ControlMultiAction(sessionId:String, slotNum:Int, subControl:Int, multiActionFlag:Boolean)
	  
	
	def exposedUpdateListeners(x : Any) : Unit = {updateListeners(x)}
	
	  def getMarkup(sessionId:String, controlId: Int): NodeSeq = {
		var nodeOut = NodeSeq.Empty
		try {
		  nodeOut = getSessionState(sessionId).controlXmlBySlot(controlId)
		} catch {
		  case _: Any => // Implies nothing in map for this controlId, do nothing and return empty nodeOut
		}
		//info("nodeOut for session " + sessionId + " and control " + controlId + " is " + nodeOut) // TEST ONLY
		nodeOut
	  }
	  
	  def getLiftAmbassador = {
		  if (myAmbassador == null) {
			myAmbassador = LiftAmbassador.getLiftAmbassador
		  }
		  myAmbassador
	  }
	  
	  def getInitialConfigId = myWebappState.getSessionInitialConfigID
	  
	  def initializeSession(sessionId:String) {
		  info("Initializing Session {}", sessionId)
		  myWebappState.initializeSession(sessionId)
	  }
	  
	  // This method clears the state info for a session from the state maps.
	  // Performed on session shutdown via LiftSession.onShutdownSession (in Boot.scala)
	  def removeSession(sessionId:String) {
		info("Removing state for session {}", sessionId)
		myWebappState.removeSession(sessionId)
	  }
	  
	  def renderInitialControls {
		  myWebappState.lifterInitialized = true
		  // If this is a restart on config change, activeSessions will have sessions which need to be re-initialized
		  myWebappState.activeSessions.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))
		  // If it's an initial startup, there may be sessions in sessionsAwaitingStart
		  myWebappState.sessionsAwaitingStart.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))
		  myWebappState.sessionsAwaitingStart.clear
	  }
	  
	  def initializeSessionAndRedirectToNewTemplate(sessionId:String) {
		initializeSession(sessionId);
		updateListeners(HtmlPageRequest(sessionId, Some(getCurrentTemplate(sessionId))))
		setControlsFromMap(sessionId)
	  }
	  
	  def requestStart(sessionId:String) {
	   if (myWebappState.lifterInitialized) {
		 initializeSession(sessionId)
	   } else {
		 // Some situations may result in requestStart being called more than once, so we need to check if it's already in buffer
		 if (!(myWebappState.sessionsAwaitingStart contains sessionId)) {
		   myWebappState.sessionsAwaitingStart += sessionId
		 }
	   }
	  }
	  
	  // This method is used by templates to check if session is active on initial render. If not, session should
	  // be initialized and the caller notified it wasn't previously active by returning a false result
	  def checkForActiveSessionAndStartIfNot(sessionId:String):Boolean = {
		var sessionActive = true
		if (!(myWebappState.activeSessions contains sessionId)) {
		  sessionActive = false
		  requestStart(sessionId) // Needs a little more refactoring between this and requestStart
		  if (myWebappState.lifterInitialized) {
			setControlsFromMap(sessionId)
		  }
		} 
		return sessionActive
	  }
									
	  def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session {}", sessionId)
		val initConfigID = getInitialConfigId
		if (sessionId.equals(initConfigID)) {
		  myWebappState.clearAndInitializeState
		} else { // otherwise reset maps for this session
		  myWebappState.prepareSessionForNewConfig(sessionId)
		}
		
		val sessionState = getSessionState(sessionId)
		sessionState.currentLiftConfig = liftConfig

		val sillyMaxControlCount = myWebappState.getMaxControlCount()
		
		val controlList: java.util.List[WebControlImpl] = liftConfig.myCCs

		val controlSet = controlList.asScala.toSet
		controlSet.foreach(controlDef => {
			var slotNum:Int = -1
			try {
			  val finalSplitterIndex = controlDef.myURI_Fragment.lastIndexOf("_")
			  slotNum = controlDef.myURI_Fragment.splitAt(finalSplitterIndex+1)._2.toInt
			} catch {
			  case _: Any =>  myLogger.warn("Unable to get valid slotNum from loaded control; URI fragment was {}", controlDef.myURI_Fragment) // The control will still be loaded into slot -1; could "break" here but it's messy and unnecessary
			}
			
			if (slotNum > sillyMaxControlCount) {
			  myLogger.warn("Maximum number of controls exceeded ({}); some controls may not be cleared upon page change!", sillyMaxControlCount)
			  myLogger.warn("MAX_CONTROL_QUANTITY in LifterState can be increased if this is necessary.")
			}
			loadControlDefToState(sessionId, slotNum, controlDef)
		  })
		// Blank unspecified slots (out to MAX_CONTROL_QUANTITY)
		for (slot <- 1 to sillyMaxControlCount) {
		  sessionState.controlXmlBySlot.putIfAbsent(slot, NodeSeq.Empty)
		}
		sessionState.currentTemplateName = liftConfig.template
		if (sessionId.equals(getInitialConfigId)) { 
		  renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
		  val changedTemplate = !sessionState.currentTemplateName.equals(sessionState.lastLiftConfig.template)
		  // ... load new template if necessary
		  if (changedTemplate) {
			updateListeners(HtmlPageRequest(sessionId, Some(getCurrentTemplate(sessionId))))
		  } 
		  // ... load new controls
		  setControlsFromMap(sessionId)
		}
	  }
	  
	  def getXmlForControl(sessionId: String, slotNum:Int, controlDef:WebControl): NodeSeq = {
		myHeadControlInitHandler.processControlInit(myWebappState, sessionId, slotNum, controlDef)
	  }
					  
	  def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		getSessionState(sessionId).controlXmlBySlot(slotNum) = slotHtml 
		updateListeners(ControlChange(sessionId, slotNum, slotHtml))
	  }
	  
	  def setControlsFromMap(sessionId:String) {
		val slotIterator = getSessionState(sessionId).controlXmlBySlot.keysIterator
		while (slotIterator.hasNext) {
		  val nextSlot = slotIterator.next
		  updateListeners(ControlChange(sessionId, nextSlot, getSessionState(sessionId).controlXmlBySlot(nextSlot)))
		}
	  }							
	  
	  def loadControlDefToState(sessionId:String, slotNum:Int, controlDef:WebControl) {
		val sessionState = getSessionState(sessionId)
		// Below, we clone the controlDef with a copy constructor so the ControlConfigs in the controlDefMap are 
		// not the same objects as in LiftAmbassador's page cache.
		// That's important largly because ToggleButton modifies the actions in the controlDefMap.
		// Pretty darn messy, and likely a topic for further refactoring.
		val clonedWCI =  new WebControlImpl(controlDef)
		sessionState.controlConfigBySlot(slotNum) = clonedWCI
		// Trigger control initialization handler chain to fill proper XML into controlsMap
		sessionState.controlXmlBySlot(slotNum) = getXmlForControl(sessionId, slotNum, controlDef)
		// Check for initial nee "local" actions which PageCommander needs to handle, such as text display
		myHeadActHandler.optionalInitialRendering(myWebappState, sessionId, slotNum, clonedWCI) //  controlDef)
	  }
  
	  def handleAction(sessionId:String, formId:Int, input:Array[String]) {
		//info("Handling action: {}", getSessionState(sessionId).controlConfigBySlot(formId).action) // TEST ONLY
		myHeadActHandler.processAction(myWebappState, sessionId, formId, 
											getSessionState(sessionId).controlConfigBySlot(formId), input)
	  }
	  
	  import java.util.Date // needed for currently implemented "debouncing" function
	  // Maps controls with actions only (buttons) to action handlers
	  def triggerAction(sessionId:String, id: Int) {
		// Check last actuated time for this control, and ignore if it happened less than IGNORE_BOUNCE_TIME ago
		val ignore = checkForBounce(sessionId, id)
		if (!ignore) {
		  if (getSessionState(sessionId).toggleControlStateBySlot contains id) {
			  ControlToggler.getTheToggler.toggle(myWebappState,sessionId,id)
		  }
		  handleAction(sessionId, id, null)		  
		}
	  }
	  
	  val bounceCheckLock: Object = new Object()
	  final val IGNORE_BOUNCE_TIME = 250 //ms
	  def checkForBounce(sessionId:String, id:Int): Boolean = {
		bounceCheckLock.synchronized {
		  val time = new Date().getTime()
		  val bounceMap = myWebappState.lastTimeAcutatedBySlot(sessionId)
		  var ignore = false
		  //info("Checking for bounce with session " + sessionId + " and slot " + id + " time=" + time) // TEST ONLY
		  if (bounceMap contains id) {
			//info("Last time=" + bounceMap(id) + " diff=" + (time - bounceMap(id))) // TEST ONLY
			if ((time - bounceMap(id)) < IGNORE_BOUNCE_TIME) {
			  ignore = true;
			  myLogger.warn("Debouncing control {} in session {}", id, sessionId)
			}
		  }
		  bounceMap(id) = time
		  ignore
		}
	  }
	  
	  // Handles incoming messages from controls
	  override def lowPriority : PartialFunction[Any, Unit]  = {		
		case a:ControlAction => {
			triggerAction(a.sessionId, a.slotNum)
		  }
		case a:ControlTextInput => {
			handleAction(a.sessionId:String, a.slotNum, a.text)
		  }
		// MultiSelectControls send substrings of the action string to the action handlers (as if they were text input) 
		// according to the selected item
		case a:ControlMultiSelect => {
			handleMultiSelectInput(a.sessionId, a.slotNum, a.subControl)
		  }
		// MultiActionControls execute actions, populated in the LifterState.SessionState.multiActionsBySlot by "initial actions"
		// upon control rendering, according to the selected item
		case a:ControlMultiAction => {
			if (!a.multiActionFlag) {
			  // If the multiActionFlag is not set, handle this control as a MultiSelect control.
			  // Allows both MultiSelect and MultiAction controls to extend AbstractMultiSelectControl
			  handleMultiSelectInput(a.sessionId, a.slotNum, a.subControl)
			} else if (myWebappState.stateBySession(a.sessionId).multiActionsBySlot contains a.slotNum) {
			  // Set the "main" action to the currently desired one; a workaround we may want to change to something more elegant in the future
			  myWebappState.stateBySession(a.sessionId).controlConfigBySlot(a.slotNum).action = 
				myWebappState.stateBySession(a.sessionId).multiActionsBySlot(a.slotNum)(a.subControl)
			  // Using triggerAction to check for bounce, which I believe should work properly here...
			  triggerAction(a.sessionId, a.slotNum)
			} else {
			  myLogger.warn("Multi action control is attempting to execute an action, but no multiAction data found in Lifter state. " +
							"Session = {}; slot = {}", a.sessionId, a.slotNum)
			}
		}
		case _: Any =>
	  }
	  
	  def handleMultiSelectInput(sessionId:String, slotNum:Int, subControlSelected:Int) {
		val input:Array[String] = Array(ActionStrings.subControlIdentifier + subControlSelected.toString)
		handleAction(sessionId, slotNum, input)
	  }

	  def getCurrentTemplate(sessionId:String) = {
		var templateToLoad: String = null
		val sessionState = myWebappState.stateBySession
		if (sessionState contains sessionId) templateToLoad = sessionState(sessionId).currentTemplateName
		templateToLoad
	  }
	  
	  // A lot of these "helper" methods below likely belong in separate class:
	  
	  // I think Ident should include this method, but since it doesn't...
	  def getUriPrefix(uri: Ident) : String = {
		uri.getAbsUriString.stripSuffix(uri.getLocalName)
	  }
	  
	  var theMessenger: CogcharMessenger = null
	
	  def getMessenger: WebInstanceGlob = { 
		if (theMessenger == null) {
		  theMessenger = new CogcharMessenger
		}
		theMessenger
	  }

	  class CogcharMessenger extends WebInstanceGlob {
                def info(msg: String, params: Any*) {
                  myLogger.info(msg, params.map(_.asInstanceOf[Object]).toArray)
                }        
    
		override def notifyConfigReady {
			initFromCogcharRDF(getInitialConfigId, getLiftAmbassador.getInitialConfig)
		}
		override def setConfigForSession(sessionId:String, config:LiftConfig) {
		  initFromCogcharRDF(sessionId, config)
		}
		override def setControlForSessionAndSlot(sessionId:String, slotNum:Int, webControl:WebControl) {
		  // Set control on display
		  val newControlXml = getXmlForControl(sessionId, slotNum, webControl)
		  setControl(sessionId, slotNum, newControlXml)
		  // Write control to state
		  loadControlDefToState(sessionId, slotNum, webControl)
		}
		override def loadPage(sessionId:String, pagePath:String) {
		  updateListeners(HtmlPageRequest(sessionId, Some(pagePath)))
		}
		override def getGlobalVariable(key:String): String = { // returns value from "public" (global) app variables map
		  var contents:String = null
		  val globalVariables = myWebappState.globalLifterVariablesByName
		  if (globalVariables contains key) contents = globalVariables(key)
		  contents
		}
		override def getSessionVariable(sessionId:String, key:String): String = { // returns value from "session" app variables map
		  var contents:String = null
		  val sessionVariables = getSessionState(sessionId).sessionLifterVariablesByName
		  if (sessionVariables contains key) contents = sessionVariables(key)
		  contents
		}
		// Show error globally
		override def showGlobalError(errorSourceCode:String, errorText:String) {
		  info("In showError; code = {}; text = {}", errorSourceCode, errorText);
		  val activeSessionIterator = myWebappState.activeSessions.iterator
		  while (activeSessionIterator.hasNext) {
			val sessionId = activeSessionIterator.next
			showSessionError(errorSourceCode, errorText, sessionId)
		  }
		}
		// Show error in session
		override def showSessionError(errorSourceCode:String, errorText:String, sessionId:String) {
		  info("In showError; code = {}; text = {}; session = {}", Array[AnyRef](errorSourceCode, errorText, sessionId));
		  if (myWebappState.stateBySession contains sessionId) {
			  val sessionErrorMap = getSessionState(sessionId).errorDisplaySlotsByType
			  if (sessionErrorMap contains errorSourceCode) {
				val slotNum = sessionErrorMap(errorSourceCode)
				if (errorText.isEmpty) {
				  setControl(sessionId, slotNum, NodeSeq.Empty)
				} else {
				  setControl(sessionId, slotNum, TextBox.makeBox(errorText, 
							getSessionState(sessionId).controlConfigBySlot(slotNum).style, true, false))
				}
			  }
		  }
		}
		// Get list of active sessionIds -- to control independent sessions from repo updates, but not sure how we'll
		// really want to handle that need. This is a temporary(?) idea:
		import collection.JavaConversions._
		override def getActiveSessions() : java.util.List[String] = {
		  myWebappState.activeSessions
		}
	  }
	
	}

  

