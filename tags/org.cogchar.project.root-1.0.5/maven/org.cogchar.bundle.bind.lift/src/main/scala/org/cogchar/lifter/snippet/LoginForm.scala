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
	
	object LoginForm extends AbstractControlInitializationHandler {
	  
	  protected val matchingName = "LOGINFORM"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		// From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
		val textItems = List.fromArray(control.text.split(ActionStrings.stringAttributeSeparator))
		val label1 = textItems(0)
		val label2 = textItems(1)
		val submitLabel = textItems(2)
		makeForm(label1, label2, submitLabel, slotNum)
	  }
	  
	  val labelIdPrefix = "loginformlabel_"
	  val textBoxIdPrefix = "login_in_"
	  val defaultText1 = "" // Default text in the entry boxes can be set here if desired, but probably not
	  val defaultText2 = ""
	  val blankId = -1
	  val textMap = new scala.collection.mutable.HashMap[Int, (String, String)]
	  
	  def makeForm(label1: String, label2: String, submitLabel: String, idNum: Int): NodeSeq = {
		val formIdforHtml: String = idNum.toString
		textMap(idNum) = (label1, label2)
		val labelId1: String = labelIdPrefix + formIdforHtml + "A"// We need unique IDs here, because JavaScript may be updating the label after post [future expansion]
		val labelId2: String = labelIdPrefix + formIdforHtml + "B"
		val inputId1: String = textBoxIdPrefix + formIdforHtml + "A"// JavaScript may want to do things to the input boxes too, like clear them
		val inputId2: String = textBoxIdPrefix + formIdforHtml + "B"
		<form class="lift:form.ajax"><lift:LoginForm formId={formIdforHtml}><div class="labels" id={labelId1}></div><input id={inputId1}/><div class="labels" id={labelId2}></div><input id={inputId2}/><br/><input type="submit" value={submitLabel}/></lift:LoginForm></form>
	  }
	}

	class LoginForm extends StatefulSnippet with Logger {
	  var text1: String = LoginForm.defaultText1
	  var text2: String = LoginForm.defaultText2
	  var formId: Int = LoginForm.blankId
	  var sessionId: String = ""
	  var idItems: Array[String] = new Array[String](2)
	  lazy val textFormInstanceLabel1 = LoginForm.labelIdPrefix + formId + "A"
	  lazy val textFormInstanceLabel2 = LoginForm.labelIdPrefix + formId + "B"
	  lazy val textBoxInstanceLabel1 = LoginForm.textBoxIdPrefix + formId + "A"
	  lazy val textBoxInstanceLabel2 = LoginForm.textBoxIdPrefix + formId + "B"
	 
	  def dispatch = {case "render" => render}	  
	  
	  def render(xhtml: NodeSeq) = {
   
		def process(): JsCmd = {
		  info("Input text for form #" + formId + ": " + text1 + "; [password hidden] in session " + sessionId)
		  PageCommander ! PageCommander.ControlTextInput(sessionId, formId, Array(text1, text2)) // Let PageCommander know about the text so it can figure out what to do with it
		  // Clear text in input boxes
		  SetValById(textBoxInstanceLabel1, "") &  SetValById(textBoxInstanceLabel2, "")
		}
		
		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr "-1").toInt 
			val labelSelectorText1: String = "#"+textFormInstanceLabel1+" *"
			val labelSelectorText2: String = "#"+textFormInstanceLabel2+" *"
			val boxSelectorText1: String = "#"+textBoxInstanceLabel1
			val boxSelectorText2: String = "#"+textBoxInstanceLabel2
			val selectors = labelSelectorText1 #> LoginForm.textMap(formId)._1 & labelSelectorText2 #> LoginForm.textMap(formId)._2 &
			  boxSelectorText1 #> (SHtml.text(text1, text1 = _, "id" -> textBoxInstanceLabel1)) &
			  boxSelectorText2 #> (SHtml.password(text2, text2 = _, "id" -> textBoxInstanceLabel2) ++ SHtml.hidden(process))
			selectors.apply(xhtml)
		  }
		  case _ => {
			error("LoginForm cannot get sessionId, not rendering!")
			TextBox.makeBox("LoginForm cannot get sessionId, not rendering!", "", true)
		  }
		}
	  }
	}

  }
}
