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

	object SelectBoxes extends AbstractControlInitializationHandler {
	  
	  protected val matchingName = "SELECTBOXES"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		// From the RDF "text" value we assume a comma separated list with the first item the title and the rest checkbox labels
		val textItems = List.fromArray(control.text.split(ActionStrings.stringAttributeSeparator))
		val titleText = textItems(0)
		val labelItems = textItems.tail
		makeSelectBoxes(titleText, labelItems, slotNum)
	  }
	  
	  val responseText = "Title can change" // We can add bits to define this in XML if we want, or code in more fancy conditionals
  
	  val titlePrefix = "selectformtitle"
	  val labelPrefix = "label"
	  val boxPrefix = "checkbox"
	  val blankId = -1

	  val titleMap = new scala.collection.mutable.HashMap[Int,String]
	  val labelMap = new scala.collection.mutable.HashMap[Int,List[String]] // Map to hold all the labels for each SelectBoxes control rendered
	  
	  def makeSelectBoxes(labelText: String, labelList:List[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
		titleMap(idNum) = labelText
		labelMap(idNum) = labelList
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here, because JavaScript will be updating the title after post
		var boxesHtmlString: String = "<form class='lift:form.ajax'><lift:SelectBoxes formId='" + formIdForHtml +"'><div id='" + titleId + "' class='labels'></div>"
		// Add html for each box
		for (boxIndex <- 0 until labelList.length) {
		  val labelId: String = labelPrefix + boxIndex.toString
		  val boxId: String = boxPrefix + boxIndex.toString
		  boxesHtmlString += "<div><label class='formlabels' for='" + boxId + "' id='" + labelId + "'></label><input id='" + boxId + "'/></div>" //The CSS class for the labels is not being applied, I bet there's a simple reason why
		}
		boxesHtmlString += "</lift:SelectBoxes></form>"
		XML.loadString(boxesHtmlString)
	  }
	}
	  
	class SelectBoxes extends StatefulSnippet with Logger {
		
	  var formId: Int = SelectBoxes.blankId
	  var sessionId: String = ""
	  lazy val selectBoxesInstanceTitle = SelectBoxes.titlePrefix + formId
		
	  def dispatch = {case "render" => render}
		
	  def render(xhtml:NodeSeq): NodeSeq = {

		def process(result: Boolean, boxNumber: Int): JsCmd = {
		  // This control is set up as demo, but we yet need to have it do something on input other than print the result!
		  info("SelectBoxes says box number " + boxNumber + " on formId " + formId + " is " + result + " for session " + sessionId)
		  SetHtml(selectBoxesInstanceTitle, Text(SelectBoxes.responseText))
		}

		def makeABox(boxIndex:Int) = {
		  val labelId: String = "#" + SelectBoxes.labelPrefix + boxIndex.toString
		  val boxId: String = "#" + SelectBoxes.boxPrefix + boxIndex.toString
		  labelId #> SelectBoxes.labelMap(formId)(boxIndex) &
		  boxId #> SHtml.ajaxCheckbox(false, (toggled: Boolean) => process(toggled, boxIndex))
		}

		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr "-1").toInt
			var valid = false
			var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
			var errorSeq: NodeSeq = NodeSeq.Empty
			if (SelectBoxes.titleMap.contains(formId)) {
			  valid = true
			  val titleSelectorText: String = "#"+selectBoxesInstanceTitle+" *"
			  selectors = titleSelectorText #> SelectBoxes.titleMap(formId)
			  for (boxIndex <- 0 until SelectBoxes.labelMap(formId).length) {
				selectors = selectors & makeABox(boxIndex)
			  }
			} else {
			  error("SelectBox.render cannot find a valid formId! Reported formId: " + formId)
			  errorSeq = TextBox.makeBox("SelectBox.render cannot find a valid formId! Reported formId: " + formId, "", true)
			}
			if (valid) selectors.apply(xhtml) else errorSeq // Blanks control if something is wrong with formId
			//selectors.apply(xhtml) // This would be ok too, and would just apply the "null" selector transform to html if something is broken
		  }
		  case _ => {
			error("SelectBoxes cannot get sessionId, not rendering!")
			TextBox.makeBox("SelectBoxes cannot get sessionId, not rendering!", "", true)
		  }
		}
		
	  }
	}

  }
}