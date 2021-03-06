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
import net.liftweb.http.{S,SHtml,StatefulSnippet}
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.util.Helpers._ 
import org.cogchar.api.web.{WebControl}

import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.impl.web.util.HasLogger
import org.cogchar.lifter.model.main.{PageCommander, ControlTextInput}
import org.cogchar.impl.web.wire.{LifterState, SessionOrganizer}
import org.cogchar.lifter.model.control.{AbstractControlSnippet, SnippetHelper}
import org.cogchar.lifter.view.TextBoxFactory
import scala.xml.NodeSeq
	
// This should eventually [er, soon] be refactored as an AbstractTextForm
class TextForm extends AbstractControlSnippet with StatefulSnippet {
	 private val mySessionOrg = SnippetHelper.mySessionOrganizer
	protected val matchingName = "TEXTINPUT"
  
	override protected def generateXmlForControl(sessionId:String, slotNum:Int, control:WebControl): NodeSeq = {
		makeTextForm(mySessionOrg, sessionId, slotNum, control.getText)
	}
	  
	val defaultText = "" // We can add bits to define this in XML if we want
	//val responseText = "Thanks for the input!" // We can add bits to define this in XML if we want - will probably do so soon, but disabling for "operational" demo right now
	val afterEntryText = "" // Right now we just clear text after input; we can do whatever we want
	val submitLabel = "Submit" // We can add bits to define this in XML if we want
	val textBoxRows = 7;
	val blankId = -1
	  
	val labelIdPrefix = "textformlabel"
	val textBoxIdPrefix = "text_in"
	  
	def makeTextForm(sessOrg : SessionOrganizer, sessionId:String, idNum:Int, initialText:String): NodeSeq = {
		val formIdforHtml: String = idNum.toString
		val dataMap = sessOrg.hackIntoSnippetDataMap(sessionId)
		dataMap(idNum) = initialText
		val labelId: String = labelIdPrefix + formIdforHtml // We need a unique ID here, because JavaScript will be updating the label after post
		val inputId: String = textBoxIdPrefix + formIdforHtml // JavaScript may want to do things to the input box too, like clear it
		// For good form and designer-friendliness, it would be nice to have all the XML in a template. But, we need to generate it here in order to set attributes. Maybe I can find a better way eventually.
		<form class="lift:form.ajax"><lift:TextForm formId={formIdforHtml}><div class="labels" id={labelId}></div><input id={inputId}/> <input type="submit" value={submitLabel}/></lift:TextForm></form>
	}
	  
	var text: String = defaultText
	var formId: Int = blankId
	var sessionId: String = ""
	var idItems: Array[String] = new Array[String](2)
	lazy val textFormInstanceLabel = labelIdPrefix + formId
	lazy val textBoxInstanceLabel = textBoxIdPrefix + formId
	  
	final def snippetData(sessionId:String) = PageCommander.hackIntoSnippetDataMap(sessionId)
	 
	def dispatch = {case "render" => render}	  
	  
	def render(xhtml:NodeSeq) = {
   
		def process(): JsCmd = {
			myLogger.info("Input text for form #{}: {} in session {}",
						  Array[AnyRef](formId.asInstanceOf[AnyRef], text, sessionId))
			PageCommander ! ControlTextInput(sessionId, formId, Array(text)) // Let PageCommander know about the text so it can figure out what to do with it
			//SetHtml(textFormInstanceLabel, Text(TextForm.responseText)) & // for now, this is disabled for the "operational" demo requirements
			SetValById(textBoxInstanceLabel, afterEntryText)
		}
		
		S.session match {
			case Full(myLiftSession) => {
					sessionId = myLiftSession.uniqueId
					formId = (S.attr("formId") openOr "-1").toInt
					val labelSelectorText: String = "#"+textFormInstanceLabel+" *"
					val boxSelectorText: String = "#"+textBoxInstanceLabel
					var titleText = ""
					snippetData(sessionId)(formId) match {
						case title: String => titleText = title
						case _ => myLogger.warn("Title for TextForm in session {} and slot {} could not be found in snippet data map",
												sessionId, formId)
					}
					val selectors = labelSelectorText #> titleText &
					boxSelectorText #> (
						SHtml.textarea(text, text = _, "rows" -> textBoxRows.toString, "id" -> textBoxInstanceLabel) 
						++ SHtml.hidden(process))
					selectors.apply(xhtml)
				}
			case _ => {
					val errorString = "TextForm cannot get sessionId, not rendering!"
					error(errorString)
					TextBoxFactory.makeBox(errorString, "", true)
				}
		}
	}
}


