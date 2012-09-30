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

	import scala.xml.NodeSeq
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
	import org.cogchar.lifter.model.PageCommander
	import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler
	import org.cogchar.lifter.view.TextBox
	import S._
	
	object TextForm extends AbstractControlInitializationHandler {
	  
	  protected val matchingName = "TEXTINPUT"
  
	  protected def handleHere(sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		makeTextForm(control.text, slotNum)
	  }
	  
	  val defaultText = "" // We can add bits to define this in XML if we want
	  //val responseText = "Thanks for the input!" // We can add bits to define this in XML if we want - will probably do so soon, but disabling for "operational" demo right now
	  val afterEntryText = "" // Right now we just clear text after input; we can do whatever we want
	  val submitLabel = "Submit" // We can add bits to define this in XML if we want
	  val textBoxRows = 7;
	  val blankId = -1
	  
	  val labelIdPrefix = "textformlabel"
	  val textBoxIdPrefix = "text_in"
	  val textMap = new scala.collection.mutable.HashMap[Int, String]
	  
	  def makeTextForm(initialText: String, idNum: Int): NodeSeq = {
		val formIdforHtml: String = idNum.toString
		textMap(idNum) = initialText
		val labelId: String = labelIdPrefix + formIdforHtml // We need a unique ID here, because JavaScript will be updating the label after post
		val inputId: String = textBoxIdPrefix + formIdforHtml // JavaScript may want to do things to the input box too, like clear it
		// For good form and designer-friendliness, it would be nice to have all the XML in a template. But, we need to generate it here in order to set attributes. Maybe I can find a better way eventually.
		<form class="lift:form.ajax"><lift:TextForm formId={formIdforHtml}><div class="labels" id={labelId}></div><input id={inputId}/> <input type="submit" value={submitLabel}/></lift:TextForm></form>
	  }
	  
	}

	class TextForm extends StatefulSnippet with Logger {
	  var text: String = TextForm.defaultText
	  var formId: Int = TextForm.blankId
	  var sessionId: String = ""
	  var idItems: Array[String] = new Array[String](2)
	  lazy val textFormInstanceLabel = TextForm.labelIdPrefix + formId
	  lazy val textBoxInstanceLabel = TextForm.textBoxIdPrefix + formId
	 
	  def dispatch = {case "render" => render}	  
	  
	  def render(xhtml:NodeSeq) = {
   
		def process(): JsCmd = {
		  info("Input text for form #" + formId + ": " + text + " in session " + sessionId)
		  PageCommander.textInputMapper(sessionId, formId, text) // Let PageCommander know about the text so it can figure out what to do with it
		  //SetHtml(textFormInstanceLabel, Text(TextForm.responseText)) & // for now, this is disabled for the "operational" demo requirements
		  SetValById(textBoxInstanceLabel, TextForm.afterEntryText)
		}
		
		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr "-1").toInt
			val labelSelectorText: String = "#"+textFormInstanceLabel+" *"
			val boxSelectorText: String = "#"+textBoxInstanceLabel
			val selectors = labelSelectorText #> TextForm.textMap(formId) &
			  boxSelectorText #> (SHtml.textarea(text, text = _, "rows" -> TextForm.textBoxRows.toString, "id" -> textBoxInstanceLabel) ++ SHtml.hidden(process))
			selectors.apply(xhtml)
		  }
		  case _ => {
			error("TextForm cannot get sessionId, not rendering!")
			TextBox.makeBox("TextForm cannot get sessionId, not rendering!", "", true)
		  }
		}
	  }
	}

  }
}
