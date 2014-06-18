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

package org.cogchar.lifter.model.control
	
	import net.liftweb.common.Full
	import net.liftweb.http.{S,SHtml,StatefulSnippet}
	import net.liftweb.http.js.JsCmd
	import net.liftweb.http.js.JsCmds.SetValById
	import net.liftweb.util.CssSel
	import net.liftweb.util.Helpers._ // This wildcard import is the way Lift Helpers roll for CssSel operations and etc.
	import org.cogchar.impl.web.config.ControlConfig
	import org.cogchar.impl.web.util.LifterLogger
	import org.cogchar.lifter.model.main.{PageCommander}
import org.cogchar.impl.web.wire.{LifterState}
	
	import org.cogchar.lifter.view.TextBox
	import org.cogchar.name.lifter.ActionStrings
	import scala.xml.{NodeSeq,XML}
	
	// An abstracted text form control. Right now only supports two entry fields, but will be refactored soon to generalize
	// to an arbitrary number of entry fields
	// Still needs additional refactoring for clarity and concision as well
	trait AbstractTextFormObject extends AbstractControlInitializationHandler {
	  
	  protected val matchingName: String
	  
	  val labelIdPrefix = "textformlabel_"
	  val textBoxIdPrefix = "textform_in_"
  
	  override protected def handleControlInit(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		// From the RDF "text" value we assume a comma separated list with the items Label 1,Label2,Submit Label
		val textItems = control.text.split(ActionStrings.stringAttributeSeparator)
		val label1 = textItems(0)
		val label2 = textItems(1)
		val submitLabel = textItems(2)
		makeForm(state, sessionId, slotNum, label1, label2, submitLabel)
	  }
	  
	  def makeForm(state:LifterState, sessionId:String, idNum:Int, label1:String, label2:String, submitLabel:String): NodeSeq = {
		val formIdforHtml: String = idNum.toString
		val dataMap = state.getSnippetDataMapForSession(sessionId)
		dataMap(idNum) = Array(label1, label2) // Soon: ArrayBuffer
		val labelId1: String = labelIdPrefix + formIdforHtml + "A"// We need unique IDs here, because JavaScript may be updating the label after post [future expansion]
		val labelId2: String = labelIdPrefix + formIdforHtml + "B"
		val inputId1: String = textBoxIdPrefix + formIdforHtml + "A"// JavaScript may want to do things to the input boxes too, like clear them
		val inputId2: String = textBoxIdPrefix + formIdforHtml + "B"
		val snippetTag = "lift:" + this.getClass.getName.split('.').last.dropRight(1) // Might this reflection be a bit problematic?
		val xmlText = "<form class=\"lift:form.ajax\"><" + snippetTag + " formId=\"" + formIdforHtml + "\"><div class=\"labels\" id=\"" +
		  labelId1 + "\"></div><input id=\"" + inputId1 + "\"/><div class=\"labels\" id=\"" + labelId2 + "\"></div><input id=\"" +
		  inputId2 + "\"/><br/><input type=\"submit\" value=\"" + submitLabel + "\"/></" + snippetTag + "></form>"
		XML.loadString(xmlText)
	  }
	}

	trait AbstractTextForm extends StatefulSnippet with LifterLogger {
	  
	  // Right now these must be set equal to the object values in the subclass - nasty... :(
	  val labelIdPrefix: String
	  val textBoxIdPrefix: String
	  
	  // Overridable parameters
	  val defaultText1 = "" // Default text in the entry boxes can be set here if desired, but probably not
	  val defaultText2 = ""
	  val afterEntryText = "" // Right now we just clear text after input; we can do whatever we want
	  val textBoxRows = 3; // Can be overriden to specify number of rows in text boxes
	  val blankId = -1
	  
	  // Used internally
	  var sessionId: String = ""
	  var formId: Int = blankId
	  
	  var text1 = defaultText1
	  var text2 = defaultText2
	  
	  // This mess could use a clean up:
	  lazy val textBoxInstanceLabel1 = textBoxIdPrefix + formId + "A"
	  lazy val textBoxInstanceLabel2 = textBoxIdPrefix + formId + "B"
	  
	  lazy val labelSelectorText1: String = "#" + labelIdPrefix + formId + "A" + " *"
	  lazy val labelSelectorText2: String = "#" + labelIdPrefix + formId + "B" + " *"
	  lazy val boxSelectorText1: String = "#" + textBoxInstanceLabel1
	  lazy val boxSelectorText2: String = "#" + textBoxInstanceLabel2
	  
	  // Ugly hack to get the snippet rendering data
	  final def snippetData(sessionId:String) = PageCommander.hackIntoSnippetDataMap(sessionId)
	 
	 
	  def dispatch = {case "render" => render}	  
	  
	  def render(xhtml: NodeSeq) = {
		
		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr blankId.toString).toInt 
			var titleText = new Array[String](2) // Soon: ArrayBuffer
			snippetData(sessionId)(formId) match {
			  case titles: Array[String] => titleText = titles
			  case _ => myLogger.warn("Title(s) for text form in session {} and slot {} could not be found in snippet data map",
				sessionId, formId)
			}
			val selectors = generateSelectors(titleText)
			selectors.apply(xhtml)
		  }
		  case _ => {
			// This common code needs to be refactored into a common location:
			val errorString = "Text form cannot get sessionId, not rendering!"
			myLogger.error(errorString)
			TextBox.makeBox(errorString, "", true)
		  }
		}
	  }
	  
	  // May be overriden to customize rendering
	  def generateSelectors(titleText: Array[String]): CssSel = {
		labelSelectorText1 #> titleText(0) & labelSelectorText2 #> titleText(1) &
		  boxSelectorText1 #> (SHtml.textarea(text1, text1 = _, "rows" -> textBoxRows.toString, "id" -> textBoxInstanceLabel1)) &
		  boxSelectorText2 #> (SHtml.textarea(text2, text2 = _, "rows" -> textBoxRows.toString, "id" -> textBoxInstanceLabel2) ++ SHtml.hidden(process))
	  }
	  
	  // May be overriden to customize processing
	  def process(): JsCmd = {
		  PageCommander ! PageCommander.ControlTextInput(sessionId, formId, Array(text1, text2)) // Let PageCommander know about the text so it can figure out what to do with it
		  // Clear text in input boxes (or set if afterEntryText is overridden) 
		  SetValById(textBoxInstanceLabel1, afterEntryText) &  SetValById(textBoxInstanceLabel2, afterEntryText)
	  }
	  
	}

