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

	object SelectBoxes extends ControlDefinition {
	  
	  class SelectBoxesConfig(val labelText: String, val labelList:List[String], val slotNum: Int)
				extends PageCommander.InitialControlConfig {
		  controlType = SelectBoxes.instance
	  }
	  
	  //under the covers, .instance() is implemented as a static method on class SelectBoxes. This lets SelectBoxesConfig
	  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
	  def instance = this 
	  
	  val responseText = "Title can change" // We can add bits to define this in XML if we want, or code in more fancy conditionals
  
	  val titlePrefix = "selectformtitle"
	  val labelPrefix = "label"
	  val boxPrefix = "checkbox"

	  val titleMap = new scala.collection.mutable.HashMap[String,String]
	  val labelMap = new scala.collection.mutable.HashMap[String,List[String]] // Map to hold all the labels for each SelectBoxes control rendered
	  
	  def makeSelectBoxes(labelText: String, labelList:List[String], sessionId:Int, idNum: Int): NodeSeq = {
		val formIdForHtml: String = sessionId.toString + "_" + idNum.toString
		titleMap(formIdForHtml) = labelText
		labelMap(formIdForHtml) = labelList
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
	  
	  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: Int): NodeSeq = {
		val config = initialConfig match {
		  case config: SelectBoxesConfig => config
		  case _ => throw new ClassCastException
		}
		makeSelectBoxes(config.labelText, config.labelList, sessionId, config.slotNum)
	  }
	}
	  
	class SelectBoxes extends StatefulSnippet {
		
	  var formId: String = ""
	  lazy val selectBoxesInstanceTitle = SelectBoxes.titlePrefix + formId
		
	  def dispatch = {case "render" => render}
		
	  def render(xhtml:NodeSeq): NodeSeq = {

		def process(result: Boolean, boxNumber: Int): JsCmd = {
		  // This control is set up as demo, but we yet need to have it do something on input other than print the result!
		  println("SelectBoxes says box number " + boxNumber + " on formId " + formId + " is " + result)
		  SetHtml(selectBoxesInstanceTitle, Text(SelectBoxes.responseText))
		}

		def makeABox(boxIndex:Int) = {
		  val labelId: String = "#" + SelectBoxes.labelPrefix + boxIndex.toString
		  val boxId: String = "#" + SelectBoxes.boxPrefix + boxIndex.toString
		  labelId #> SelectBoxes.labelMap(formId)(boxIndex) &
		  boxId #> SHtml.ajaxCheckbox(false, (toggled: Boolean) => process(toggled, boxIndex))
		}

		formId = (S.attr("formId") openOr "_")
		val idItems = formId.split("_")
		var valid = false
		var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
		if (SelectBoxes.titleMap.contains(formId)) {
		  valid = true
		  val titleSelectorText: String = "#"+selectBoxesInstanceTitle+" *"
		  selectors = titleSelectorText #> SelectBoxes.titleMap(formId)
		  for (boxIndex <- 0 until SelectBoxes.labelMap(formId).length) {
			selectors = selectors & makeABox(boxIndex)
		  }
		} else println("SelectBox.render cannot find a valid formId! Reported formId: " + formId)
		if (valid) selectors.apply(xhtml) else NodeSeq.Empty // Blanks control if something is wrong with formId
		//selectors.apply(xhtml) // This would be ok too, and would just apply the "null" selector transform to html if something is broken
	  }
	}

  }
}
