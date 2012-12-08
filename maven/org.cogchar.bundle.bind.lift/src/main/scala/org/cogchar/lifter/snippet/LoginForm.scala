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
	import net.liftweb._
	import http._
	import common._
	import js._
	import JsCmds._
	import JE._
	import net.liftweb.http.js.JsCmd
	import net.liftweb.util._
	import Helpers._
	import net.liftweb.http.SHtml._
	import org.cogchar.bind.lift.ControlConfig
	import org.cogchar.lifter.model.{ActionStrings,LifterState,PageCommander}
	import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler
	import org.cogchar.lifter.view.TextBox
	import S._
	
	object LoginForm extends AbstractTextFormObject {
	  
	  protected val matchingName = "LOGINFORM"
	  
	}

	class LoginForm extends AbstractTextForm {
	  
	  // Too bad these are required to get prefixes from object - has to be a better way...
	  val labelIdPrefix: String = LoginForm.labelIdPrefix
	  val textBoxIdPrefix: String = LoginForm.textBoxIdPrefix
   
	  override def process(): JsCmd = {
		info("Input text for form #" + formId + ": " + text1 + "; [password hidden] in session " + sessionId)
		super.process();
	  }
	  
	  override def generateSelectors(titleText: Array[String]): CssSel = {
	  labelSelectorText1 #> titleText(0) & labelSelectorText2 #> titleText(1) &
		boxSelectorText1 #> (SHtml.text(text1, text1 = _, "id" -> textBoxInstanceLabel1)) &
		boxSelectorText2 #> (SHtml.password(text2, text2 = _, "id" -> textBoxInstanceLabel2) ++ SHtml.hidden(process))
	  }
	
	}

  }
}
