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
	import org.cogchar.lifter.model.PageCommander
	import S._
	
	object DualTextForm {
	  val defaultText = "" // We can add bits to define this in RDF if we want
	  val afterEntryText = "" // Right now we just clear text after input; we can do whatever we want
	  val textBoxRows = 3;
	  
	  val labelIdPrefix = "dualtextformlabel_"
	  val textBoxIdPrefix = "dualtext_in_"
	  val blankId: Int = -1
	  val textMap = scala.collection.mutable.HashMap(blankId -> ("No label text found", "No label text found")) // Map to hold the labels for each form handled by this snippet, plus default
	  
	  def makeForm(label1: String, label2: String, submitLabel: String, idNum: Int): NodeSeq = {
		textMap(idNum) = (label1, label2)
		val formIdforHtml: String = idNum.toString
		val labelId1: String = labelIdPrefix + formIdforHtml + "A"// We need unique IDs here, because JavaScript may be updating the label after post [future expansion]
		val labelId2: String = labelIdPrefix + formIdforHtml + "B"
		val inputId1: String = textBoxIdPrefix + formIdforHtml + "A"// JavaScript may want to do things to the input boxes too, like clear them
		val inputId2: String = textBoxIdPrefix + formIdforHtml + "B"
		<form class="lift:form.ajax"><lift:DualTextForm formId={formIdforHtml}><div class="labels" id={labelId1}></div><input id={inputId1}/><div class="labels" id={labelId2}></div><input id={inputId2}/><br/><input type="submit" value={submitLabel}/></lift:DualTextForm></form>
	  }
	}

	class DualTextForm extends StatefulSnippet with Logger {
	  var text1: String = DualTextForm.defaultText
	  var text2: String = DualTextForm.defaultText
	  var formId: Int = DualTextForm.blankId
	  lazy val textFormInstanceLabel1 = DualTextForm.labelIdPrefix + formId + "A"
	  lazy val textFormInstanceLabel2 = DualTextForm.labelIdPrefix + formId + "B"
	  lazy val textBoxInstanceLabel1 = DualTextForm.textBoxIdPrefix + formId + "A"
	  lazy val textBoxInstanceLabel2 = DualTextForm.textBoxIdPrefix + formId + "B"
	 
	  def dispatch = {case "render" => render}	  
	  
	  def render = {
   
		def process(): JsCmd = {
		  info("Input text for form #" + formId + ": " + text1 + "; " + text2)
		  val processThread = new Thread(new Runnable { // A new thread to call back into PageCommander to make sure we don't block Ajax handling
			  def run() {
				PageCommander.multiTextInputMapper(formId, Array(text1, text2)) // Let PageCommander know about the text so it can figure out what to do with it
			  }
			})
		  processThread.start
		  SetValById(textBoxInstanceLabel1, DualTextForm.afterEntryText) &  SetValById(textBoxInstanceLabel2, DualTextForm.afterEntryText)
		}
		
		formId = (S.attr("formId") openOr "-1").toInt
		val labelSelectorText1: String = "#"+textFormInstanceLabel1+" *"
		val labelSelectorText2: String = "#"+textFormInstanceLabel2+" *"
		val boxSelectorText1: String = "#"+textBoxInstanceLabel1
		val boxSelectorText2: String = "#"+textBoxInstanceLabel2
		labelSelectorText1 #> DualTextForm.textMap(formId)._1 & labelSelectorText2 #> DualTextForm.textMap(formId)._2 &
		boxSelectorText1 #> (SHtml.textarea(text1, text1 = _, "rows" -> DualTextForm.textBoxRows.toString, "id" -> textBoxInstanceLabel1)) &
		boxSelectorText2 #> (SHtml.textarea(text2, text2 = _, "rows" -> DualTextForm.textBoxRows.toString, "id" -> textBoxInstanceLabel2) ++ SHtml.hidden(process))
	  }
	}

  }
}
