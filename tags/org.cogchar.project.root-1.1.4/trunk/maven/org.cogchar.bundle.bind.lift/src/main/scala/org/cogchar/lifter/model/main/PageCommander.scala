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
import org.cogchar.impl.web.util.HasLogger
import org.cogchar.impl.web.wire.{LifterState, SessionOrganizer, WebappGlobalState, WebSessionState, WebappCommander}
import org.cogchar.lifter.model.handler.{HandlerConfigurator}
import org.cogchar.lifter.model.action.{AbstractLifterActionHandler, LifterVariableHandler}
import org.cogchar.lifter.model.control.{AbstractControlSnippet}
import org.cogchar.lifter.view.TextBox
import org.cogchar.api.web.{WebControl}
import org.cogchar.impl.web.config.{WebControlImpl, LiftAmbassador, LiftConfig, WebInstanceGlob}
import scala.collection.JavaConverters._
// import org.cogchar.platform.trigger.CogcharActionBinding
	
/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
	
// We might eventually want parts of PageCommander to be performed via a class of 
// actors with an instance of that class created for each session.

object PageCommander extends LiftActor with ListenerManager with HasLogger with WebappCommander {
      
	def info(msg: String, params: Any*) {
		myLogger.info(msg, params.map(_.asInstanceOf[Object]).toArray)
	}
  
	private lazy val myAmbassador:LiftAmbassador = LiftAmbassador.getLiftAmbassador
	def getLiftAmbassador = myAmbassador
	  
	private var updateInfo: String = ""
	  
	def createUpdate = updateInfo

	private lazy val myGlobalStateMgr = new WebappGlobalState
		
	override def  getInitConfig() : LiftConfig = getLiftAmbassador.getInitialConfig
	override def getInitialConfigId : String = getSessionOrg.getInitialConfigID

	def exposedUpdateListeners(x : Any) : Unit = {updateListeners(x)}

	private lazy val myMessenger: CogcharMessenger =  new CogcharMessenger(this, myGlobalStateMgr)
	def getMessenger: WebInstanceGlob = myMessenger
	def getSessionOrg : SessionOrganizer = myMessenger
	
	
	private def getSessionState(sessionId:String) = getSessionOrg.getSessionState(sessionId)
	def hackIntoSnippetDataMap(sessionId:String) =   getSessionOrg.hackIntoSnippetDataMap(sessionId)

	private val myHeadActHandler : AbstractLifterActionHandler = HandlerConfigurator.initializeActionHandlers(getLiftAmbassador, myGlobalStateMgr, getSessionOrg, this)
	private val myHeadControlSnippet : AbstractControlSnippet = HandlerConfigurator.initializeControlSnippets(getSessionOrg)
	  

	
	
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
	  
	
	def renderInitialControls {
		// myWebappState.lifterInitialized = true
		val so = getSessionOrg
		so.markSessionGroupInitFlag(true)
		so.renderInitControlsOnAllSessions
		/*
		// If this is a restart on config change, activeSessions will have sessions which need to be re-initialized
		myWebappState.activeSessions.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))
		// If it's an initial startup, there may be sessions in sessionsAwaitingStart
		myWebappState.sessionsAwaitingStart.foreach(sessionId => initializeSessionAndRedirectToNewTemplate(sessionId))
		myWebappState.sessionsAwaitingStart.clear
		 */
	}
	  
	override def initializeSessionAndRedirectToNewTemplate(sessionId:String) {
		getSessionOrg.initializeSession(sessionId);
		val currentTemplateName : String = getSessionOrg.getCurrentTemplateForSession(sessionId)
		// updateListeners(HtmlPageRequest(sessionId, Some(currentTemplateName))
		sendSessionPage(sessionId, Some(currentTemplateName))
		
		setControlsFromMap(sessionId)
	}
	  
	  
	// This method is used by templates to check if session is active on initial render. If not, session should
	// be initialized and the caller notified it wasn't previously active by returning a false result
	def checkForActiveSessionAndStartIfNot(sessionId:String):Boolean = {
		val so = getSessionOrg
		var sessionActive = true
		if (!(so.hasActiveSession(sessionId))) {
			sessionActive = false
			so.requestStart(sessionId) // Needs a little more refactoring between this and requestStart
			if (so.isSessionGroupStarted()) {
				setControlsFromMap(sessionId)
			}
		} 
		return sessionActive
	}
	override def initFromCogcharRDF(sessionId:String, liftConfig:LiftConfig) {
		info("Loading LiftConfig for session {}", sessionId)

		val so = getSessionOrg
		so.initSession(sessionId)
		val sessionState : WebSessionState = getSessionState(sessionId)
		sessionState.currentLiftConfig = liftConfig

		val sillyMaxControlCount = myGlobalStateMgr.getMaxControlCount()
		
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
		val curTemplName = sessionState.currentTemplateName
		if (sessionId.equals(getInitialConfigId)) { 
			renderInitialControls; // Required to get things started if pages are loaded in browsers before config is initialized
		} else { // otherwise...
			
			val changedTemplate = !curTemplName.equals(sessionState.lastLiftConfig.template)
			// ... load new template if necessary
			if (changedTemplate) {
	
				sendSessionPage(sessionId, Some(curTemplName))
			} 
			// ... load new controls
			setControlsFromMap(sessionId)
		}
	}
	override def getXmlForControl(sessionId: String, slotNum:Int, controlDef:WebControl): NodeSeq = {
		myHeadControlSnippet.generateOutputXml(sessionId, slotNum, controlDef)
	}
	
	def setControl(sessionId: String, slotNum: Int, slotHtml: NodeSeq) {
		getSessionState(sessionId).controlXmlBySlot(slotNum) = slotHtml 
		sendSessionControlChange(sessionId, slotNum, slotHtml)
	}
	def sendSessionPage(sessionId : String, templateName_opt : Option[String]) {
		updateListeners(HtmlPageRequest(sessionId, templateName_opt))
	}

	def sendSessionControlChange(sessionId : String, slotNum : Int, nodeSeq : NodeSeq) {
		updateListeners(ControlChange(sessionId, slotNum, nodeSeq))
	}	  
	def setControlsFromMap(sessionId:String) {
		val sstate = getSessionState(sessionId)
		val cxbs = sstate.controlXmlBySlot
		val slotIterator = cxbs.keysIterator
		while (slotIterator.hasNext) {
			val nextSlot : Int = slotIterator.next
			val nodeSeq : NodeSeq = cxbs(nextSlot)
			sendSessionControlChange(sessionId, nextSlot, nodeSeq)
		}
	}							

	override def loadControlDefToState(sessionId:String, slotNum:Int, controlDef:WebControl) {
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
		myHeadActHandler.optionalInitialRendering(sessionId, slotNum, clonedWCI) //  controlDef)
	}
  
	def handleAction(sessionId:String, formId:Int, input:Array[String]) {
		//info("Handling action: {}", getSessionState(sessionId).controlConfigBySlot(formId).action) // TEST ONLY
		myHeadActHandler.processAction(sessionId, formId, 
									   getSessionState(sessionId).controlConfigBySlot(formId), input)
	}
	  
	import java.util.Date // needed for currently implemented "debouncing" function
	// Maps controls with actions only (buttons) to action handlers
	def triggerAction(sessionId:String, id: Int) {
		// Check last actuated time for this control, and ignore if it happened less than IGNORE_BOUNCE_TIME ago
		val ignore = checkForBounce(sessionId, id)
		if (!ignore) {
			val sState = getSessionState(sessionId)
			if (sState.toggleControlStateBySlot contains id) {
				ControlToggler.getTheToggler.toggle(sState,id)
			}
			handleAction(sessionId, id, null)		  
		}
	}
	  
	val bounceCheckLock: Object = new Object()
	final val IGNORE_BOUNCE_TIME = 250 //ms
	def checkForBounce(sessionId:String, id:Int): Boolean = {
		bounceCheckLock.synchronized {
			val time = new Date().getTime()
			val bounceMapMap = getSessionOrg.getLastTimeAcutatedBySlot
			val sessionBounceMap = bounceMapMap(sessionId)
			var ignore = false
			//info("Checking for bounce with session " + sessionId + " and slot " + id + " time=" + time) // TEST ONLY
			if (sessionBounceMap contains id) {
				//info("Last time=" + bounceMap(id) + " diff=" + (time - bounceMap(id))) // TEST ONLY
				if ((time - sessionBounceMap(id)) < IGNORE_BOUNCE_TIME) {
					ignore = true;
					myLogger.warn("Debouncing control {} in session {}", id, sessionId)
				}
			}
			sessionBounceMap(id) = time
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
				val state = getSessionOrg.getSessionState(a.sessionId)
				if (!a.multiActionFlag) {
					// If the multiActionFlag is not set, handle this control as a MultiSelect control.
					// Allows both MultiSelect and MultiAction controls to extend AbstractMultiSelectControl
					handleMultiSelectInput(a.sessionId, a.slotNum, a.subControl)
				} else if (state.multiActionsBySlot contains a.slotNum) {
					// Set the "main" action to the currently desired one; a workaround we may want to change to something more elegant in the future
					state.controlConfigBySlot(a.slotNum).action = 
						state.multiActionsBySlot(a.slotNum)(a.subControl)
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

	  

	  
	
	
}

  

