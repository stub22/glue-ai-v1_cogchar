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
	import net.liftweb.http.js.JsCmds
	import net.liftweb.util._
	import Helpers._
	import net.liftweb.http._
	import net.liftweb.http.SHtml._
	import S._

	// This snippet handles the "Scene Playing" webapp page
	object SceneInfo {
	  
	  var infoClass = ""
	  var infoImage = ""
	  var infoText = ""

	  
	  def render = {
		val infoImagePath: String = "/images/" + infoImage // May want to move this prefix to central location
		val infoPlayingText: String = "Playing " + infoText
		//<lift:PushyButton buttonId="101"><div class={infoClass} onclick=""><br/><img src={infoImagePath} width="50%"/><br/>{infoPlayingText}</div></lift:PushyButton>
		PushyButton.makeButton(infoPlayingText, infoClass, infoImage, 101)
	  }
	  
	}

  }
}