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

package org.cogchar.lifter {
  package snippet {

	import scala.xml._	
	import net.liftweb.common._
	import net.liftweb.http._
	import net.liftweb.http.SHtml._
	import net.liftweb.util._
	import net.liftweb.http.js.JsCmd
	import net.liftweb.http.js.JsCmds
	import Helpers._
	import S._
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.view.TextBox

	object PushyButton extends Logger with ControlDefinition {
  
	  class PushyButtonConfig(val buttonText:String, val buttonClass:String, val buttonImage:String, val buttonSlot: Int)
				extends PageCommander.InitialControlConfig {
		  controlType = PushyButton.instance
	  }
	  
	  //under the covers, .instance() is implemented as a static method on class PushyButton. This lets PushyButtonConfig
	  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
	  def instance = this   
	  
	  def makeButton(buttonText:String, buttonClass:String, buttonImage:String, sessionId: String, buttonId: Int): NodeSeq = {
		val buttonNum: String = buttonId.toString
		val buttonPath: String = "/images/" + buttonImage // May want to move this prefix to central location
		if (buttonImage.length >= 5) { // needs to be at least this long to have a valid image filename
		  <lift:PushyButton buttonId={buttonNum}><div class="centerVert pushypadding"><div name="pushbutton" class={buttonClass} onclick=""><img src={buttonPath} width="50%"/><br/>{buttonText}</div></div></lift:PushyButton>
		} else {
		  <lift:PushyButton buttonId={buttonNum}><div class="centerVert pushypadding"><div name="pushbutton" class={buttonClass} onclick="">{buttonText}</div></div></lift:PushyButton>
		}
	  }
	  
	  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: String): NodeSeq = {
	  val config = initialConfig match {
		case config: PushyButtonConfig => config
		case _ => throw new ClassCastException
	  }
		makeButton(config.buttonText, config.buttonClass, config.buttonImage, sessionId, config.buttonSlot)
	  }
  
	  def render = {
		S.session match {
		  case Full(myLiftSession) => {
			val sessionId = myLiftSession.uniqueId
			val buttonId: Int = (S.attr("buttonId") openOr "-1").toInt
			"@pushbutton [onclick]" #> SHtml.ajaxInvoke (() => {
				info("Starting action mapped to button " + buttonId + " in session " + sessionId)
				val processThread = new Thread(new Runnable { // A new thread to call back into PageCommander to make sure we don't block Ajax handling
					def run() {
					  PageCommander.triggerAction(sessionId, buttonId)
					}
				  })
				processThread.start
				JsCmds.Noop
			})
		  }
		  case _ => {
			error("PushyButton cannot get sessionId, not rendering!")
			TextBox.makeBox("PushyButton cannot get sessionId, not rendering!", "", true)
		  }
		}
		
	  } 
	}
  }
}

