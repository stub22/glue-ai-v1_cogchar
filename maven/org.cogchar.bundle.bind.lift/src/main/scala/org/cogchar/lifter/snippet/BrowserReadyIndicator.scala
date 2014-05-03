package org.cogchar.lifter.snippet

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.js.JsCmds.{RedirectTo,Script}
import org.cogchar.lifter.app.LifterLogger
import org.cogchar.lifter.model.main.PageCommander
import xml.NodeSeq


object BrowserReadyIndicator extends LifterLogger {

  final val STARTUP_TEMPLATE = "loading"
  final val TEMPLATE_NAME_ATTRIB_NAME = "templateName"
  
  // Needs some refactoring
  def render = {
	S.session match {
	  case Full(myLiftSession) => {
		  val sessionId = myLiftSession.uniqueId
		  PageCommander.checkForActiveSessionAndStartIfNot(sessionId)
		  val templateName: String = (S.attr(TEMPLATE_NAME_ATTRIB_NAME) openOr "NotFound")
		  val desiredTemplate = PageCommander.getCurrentTemplate(sessionId)
		  //myLogger.info("Desired template is " + desiredTemplate) // TEST ONLY
		  if (desiredTemplate == null) { // Indicates Lifter has not yet fully initialized
			if (templateName.equals(STARTUP_TEMPLATE)) {
			  //myLogger.info("Parked at default") // TEST ONLY
			  NodeSeq.Empty
			} else {
			  //myLogger.info("Redirecting to default (index)") // TEST ONLY
			  Script(RedirectTo("index"))
			}
		  } else if (templateName.equals(desiredTemplate)) {
			//myLogger.info("Not trying to change template") // TEST ONLY
			NodeSeq.Empty
		  } else {
			//myLogger.info("Trying to change template") // TEST ONLY
			Script(RedirectTo(desiredTemplate))
		  }
		}
	  case _ => {
		  myLogger.error("BrowserReadyIndicator cannot get sessionId, not rendering!")
		  // Add error display in browser
		  NodeSeq.Empty
		}
	}
	
  }
  
}

