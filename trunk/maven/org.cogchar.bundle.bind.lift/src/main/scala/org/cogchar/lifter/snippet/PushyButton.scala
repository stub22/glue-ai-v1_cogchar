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

package org.cogchar.lifter.snippet 

	
import net.liftweb.common.Full
import net.liftweb.http.{S,SHtml}
import net.liftweb.http.js.JsCmds
import net.liftweb.util.Helpers._
import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.lifter.model.main.{PageCommander, ControlAction}
import org.cogchar.impl.web.wire.{LifterState}
import org.cogchar.lifter.model.control.{AbstractControlInitializationHandler}
import org.cogchar.lifter.view.TextBoxFactory
import scala.xml.NodeSeq
import org.cogchar.api.web.{WebControl}

class PushyButton extends AbstractControlInitializationHandler {
	  
	protected val matchingName = "PUSHYBUTTON"
  
	override protected def handleControlInit(sessionId:String, slotNum:Int, control:WebControl): NodeSeq = {
		makeButton(control.getText, control.getStyle, control.getResource, slotNum)
	}
	  
	def makeButton(buttonText:String, buttonClass:String, buttonImage:String, buttonId: Int): NodeSeq = {
		val buttonNum: String = buttonId.toString
		val buttonPath: String = "/images/" + buttonImage // May want to move this prefix to central location
		val buttonName: String = "pushbutton" + buttonNum
		if (buttonImage.length >= 5) { // needs to be at least this long to have a valid image filename
			<lift:PushyButton buttonId={buttonNum}><div class="centerVert pushypadding"><div name={buttonName} type="button" class={buttonClass} onclick=""><img src={buttonPath} width="50%"/><br/>{buttonText}</div></div></lift:PushyButton>
		} else {
			<lift:PushyButton buttonId={buttonNum}><div class="centerVert pushypadding"><div name={buttonName} type="button" class={buttonClass} onclick="">{buttonText}</div></div></lift:PushyButton>
		}
	}
  
	def render = {
		S.session match {
			case Full(myLiftSession) => {
					val sessionId = myLiftSession.uniqueId
					val buttonId: Int = (S.attr("buttonId") openOr "-1").toInt
					val buttonName: String = "pushbutton" + buttonId
					val selectorString = "@" + buttonName + " [onclick]"
					selectorString #> SHtml.ajaxInvoke (() => {
							myLogger.info("Starting action mapped to button {} in session {}", buttonId, sessionId)
							PageCommander ! ControlAction(sessionId, buttonId)
							JsCmds.Noop
						})
				}
			case _ => {
					val errorString = "PushyButton cannot get sessionId, not rendering!"
					myLogger.error(errorString)
					TextBoxFactory.makeBox(errorString, "", true)
				}
		}
	} 
}
 

