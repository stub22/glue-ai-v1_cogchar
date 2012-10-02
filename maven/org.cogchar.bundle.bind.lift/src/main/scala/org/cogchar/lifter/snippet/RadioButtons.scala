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

	object RadioButtons extends AbstractControlInitializationHandler with Logger {
	  
	  protected val matchingName = "RADIOBUTTONS"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq =  {
		// From the RDF "text" value we assume a comma separated list with the first item the title and the rest radiobutton labels
		val textItems = List.fromArray(control.text.split(ActionStrings.stringAttributeSeparator))
		val titleText = textItems(0)
		val labelItems = textItems.tail
		makeRadioButtons(titleText, labelItems, slotNum)
	  }
  
	  val responseText = "I see you!"
	  
	  val titlePrefix = "radiotitle"
	  
	  val blankId = -1
	
	  val titleMap = new scala.collection.mutable.HashMap[Int,String]
	  val labelMap = new scala.collection.mutable.HashMap[Int,List[String]] // Map to hold all the labels for each RadioButtons control rendered

	  def makeRadioButtons(titleText: String, labelList:List[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
		titleMap(idNum) = titleText
		labelMap(idNum) = labelList
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here, because JavaScript will be updating the title after post
		<form class="lift:form.ajax"><lift:RadioButtons formId={formIdForHtml}><div class="labels" id={titleId}></div><div id="buttonshere"></div></lift:RadioButtons></form>
	  }
	}
	
	class RadioButtons extends StatefulSnippet with Logger {
	  
	  var formId: Int = RadioButtons.blankId
	  var sessionId: String = ""
	  var idItems: Array[String] = new Array[String](2)
	  lazy val radioButtonsInstanceTitleId = RadioButtons.titlePrefix + formId
	  
	  def dispatch = {case "render" => render}
	  
	  def render(xhtml:NodeSeq): NodeSeq = {
		
		def process(result: String): JsCmd = {
		  info("RadioButtons says option number " + result + " on formId " + formId + " is selected in session " + sessionId)
		  //SetHtml(radioButtonsInstanceTitleId, Text(RadioButtons.responseText)) //... or not for now
		  PageCommander.multiSelectControlActionMapper(sessionId, formId, result.toInt)
		  JsCmds.Noop
		}
		
		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr "-1").toInt 
			var valid = false
			var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
			var errorSeq: NodeSeq = NodeSeq.Empty
			if (RadioButtons.titleMap.contains(formId)) {
			  valid = true
			  val titleSelectorText: String = "#"+radioButtonsInstanceTitleId+" *"
			  val buttonTags = (for (i <- 0 until RadioButtons.labelMap(formId).length) yield i.toString)// There may be a simplier Scala way to do this
			  val theButtons = SHtml.ajaxRadio(buttonTags, Empty, process _)
			  var buttonHtml: NodeSeq = NodeSeq.Empty
			  for (buttonIndex <- 0 until RadioButtons.labelMap(formId).length) {
				buttonHtml = buttonHtml ++ <div><span class="formlabels">{RadioButtons.labelMap(formId)(buttonIndex)}</span><span>{theButtons(buttonIndex)}</span></div>
			  }
			  selectors = titleSelectorText #> RadioButtons.titleMap(formId) & "#buttonshere" #> buttonHtml
			} else {
			  error("RadioButtons.render cannot find a valid formId! Reported formId: " + formId)
			  errorSeq = TextBox.makeBox("RadioButtons.render cannot find a valid formId! Reported formId: " + formId, "", true)
			}
			if (valid) selectors.apply(xhtml) else errorSeq // Blanks control if something is wrong with formId
			//selectors.apply(xhtml) // This would be ok too, and would just apply the "null" selector transform to html if something is broken
		  }
		  case _ => {
			error("RadioButtons cannot get sessionId, not rendering!")
			TextBox.makeBox("RadioButtons cannot get sessionId, not rendering!", "", true)
		  }
		}
		
	  }  
	}

  }
}
