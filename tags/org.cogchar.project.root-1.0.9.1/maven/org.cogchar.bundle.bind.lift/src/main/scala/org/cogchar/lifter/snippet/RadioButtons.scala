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

	import net.liftweb.common.Empty
	import net.liftweb.http.SHtml
	import net.liftweb.util.CssSel
	import net.liftweb.util.Helpers._
	import scala.xml.NodeSeq
import org.cogchar.lifter.model.control.{AbstractMultiSelectControl, AbstractMultiSelectControlObject}	

	object RadioButtons extends AbstractMultiSelectControlObject {
	  
	  protected val matchingName = "RADIOBUTTONS"
  
	  //val responseText = "I see you!" // Currently ignored; waiting for future expansion to control RDF definition if we want to use this
	  
	  val titlePrefix = "radiotitle"

	  def makeMultiControlImpl(labelText: String, labelList:Array[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here, because JavaScript will be updating the title after post
		<form class="lift:form.ajax"><lift:RadioButtons formId={formIdForHtml}><div class="labels" id={titleId}></div><div id="buttonshere"></div></lift:RadioButtons></form>
	  }
	}
	
	class RadioButtons extends AbstractMultiSelectControl {
	  
	  def getName: String = RadioButtons.matchingName
	  
	  def generateSelectors(sessionId: String, formId: Int, title: String, labels: Array[String]): CssSel = {
		val listLength = labels.length
		val radioButtonsInstanceTitleId = RadioButtons.titlePrefix + formId
		val titleSelectorText: String = "#"+radioButtonsInstanceTitleId+" *"
		val buttonTags = (for (i <- 0 until listLength) yield i.toString)// There may be a simplier Scala way to do this
		val theButtons = SHtml.ajaxRadio(buttonTags, Empty, process _)
		var buttonHtml: NodeSeq = NodeSeq.Empty
		for (buttonIndex <- 0 until listLength) {
		  buttonHtml = buttonHtml ++ <div><span class="formlabels">{labels(buttonIndex)}</span><span>{theButtons(buttonIndex)}</span></div>
		}
		titleSelectorText #> title & "#buttonshere" #> buttonHtml
	  }
	}
  }
}
