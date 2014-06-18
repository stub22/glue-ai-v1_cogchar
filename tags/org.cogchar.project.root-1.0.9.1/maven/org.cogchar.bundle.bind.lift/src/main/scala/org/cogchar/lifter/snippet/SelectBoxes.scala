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

import net.liftweb.http.SHtml
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import scala.xml.{NodeSeq,XML}
import org.cogchar.lifter.model.control.{AbstractMultiSelectControl, AbstractMultiSelectControlObject}

object SelectBoxes extends AbstractMultiSelectControlObject {
	  
	  protected val matchingName = "SELECTBOXES"
	  
	  // Not currently implemented:
	  //val responseText = "Title can change" // We can add bits to define this in XML if we want, or code in more fancy conditionals
  
	  val titlePrefix = "selectformtitle"
	  val labelPrefix = "label"
	  val boxPrefix = "checkbox"
	  
	  def makeMultiControlImpl(labelText: String, labelList:Array[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here in case we'd like JavaScript to update the title after post
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
	  
class SelectBoxes extends AbstractMultiSelectControl {
		
	  def getName: String = SelectBoxes.matchingName
	  
		def generateSelectors(sessionId: String, formId: Int, title: String, labels: Array[String]): CssSel = {
		  val selectBoxesInstanceTitle = SelectBoxes.titlePrefix + formId
		  val titleSelectorText: String = "#"+selectBoxesInstanceTitle+" *"
		  var selectors = titleSelectorText #> title
		  for (boxIndex <- 0 until labels.length) {
			selectors = selectors & makeABox(boxIndex, labels)
		  }
		  selectors
		}

		def makeABox(boxIndex:Int, labels:Array[String]) = {
		  val labelId: String = "#" + SelectBoxes.labelPrefix + boxIndex.toString
		  val boxId: String = "#" + SelectBoxes.boxPrefix + boxIndex.toString
		  labelId #> labels(boxIndex) &
		  boxId #> SHtml.ajaxCheckbox(false, (toggled: Boolean) => processWithToggle(toggled, boxIndex))
		}
		
	  def processWithToggle(result: Boolean, boxNumber:Int) {
		if (result) {
		  process(boxNumber.toString)
		} else {
		  // We may want to expand this class and/or AbstractMultiSelectControl to perform an action on deselect
		  myLogger.info("{} sees that the box was deselected, but currently we do not make use of that information.", getName)
		}
	  }

}
