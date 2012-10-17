package org.cogchar.lifter.snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.S._
import net.liftweb.util._
import Helpers._
import org.cogchar.lifter.model.PageCommander

import xml._


object BrowserReadyIndicator extends Logger {

  // Needs some refactoring
  def render = {
	S.session match {
	  case Full(myLiftSession) => {
		  val sessionId = myLiftSession.uniqueId
		  PageCommander.checkForActiveSessionAndStartIfNot(sessionId)
		  val templateName: String = (S.attr("templateName") openOr "NotFound")
		  val desiredTemplate = PageCommander.getCurrentTemplate(sessionId)
		  //info("Desired template is " + desiredTemplate) // TEST ONLY
		  if (desiredTemplate == null) { // Indicates Lifter has not yet fully initialized
			if (templateName.equals("default")) {
			  //info("Parked at default") // TEST ONLY
			  NodeSeq.Empty
			} else {
			  //info("Redirecting to default (index)") // TEST ONLY
			  Script(RedirectTo("index"))
			}
		  } else if (templateName.equals(desiredTemplate)) {
			//info("Not trying to change template") // TEST ONLY
			NodeSeq.Empty
		  } else {
			//info("Trying to change template") // TEST ONLY
			Script(RedirectTo(desiredTemplate))
		  }
		}
	  case _ => {
		  error("BrowserReadyIndicator cannot get sessionId, not rendering!")
		  // Add error display in browser
		  NodeSeq.Empty
		}
	}
	
  }
  
}

