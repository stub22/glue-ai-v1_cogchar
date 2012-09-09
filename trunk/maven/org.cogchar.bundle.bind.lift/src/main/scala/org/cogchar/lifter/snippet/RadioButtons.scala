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

	object RadioButtons extends Logger with ControlDefinition {
	  
	  class RadioButtonsConfig(val titleText: String, val labelList:List[String], val slotNum: Int)
				extends PageCommander.InitialControlConfig {
		  controlType = RadioButtons.instance
	  }
	  
	  //under the covers, .instance() is implemented as a static method on class RadioButtons. This lets RadioButtonsConfig
	  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
	  def instance = this   
  
	  val responseText = "I see you!"
	  
	  val titlePrefix = "radiotitle"
	
	  val titleMap = new scala.collection.mutable.HashMap[String,String]
	  val labelMap = new scala.collection.mutable.HashMap[String,List[String]] // Map to hold all the labels for each RadioButtons control rendered

	  def makeRadioButtons(titleText: String, labelList:List[String], sessionId:Int, idNum: Int): NodeSeq = {
		val formIdForHtml: String = sessionId.toString + "_" + idNum.toString
		titleMap(formIdForHtml) = titleText
		labelMap(formIdForHtml) = labelList
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here, because JavaScript will be updating the title after post
		<form class="lift:form.ajax"><lift:RadioButtons formId={formIdForHtml}><div class="labels" id={titleId}></div><div id="buttonshere"></div></lift:RadioButtons></form>
	  }
	  
	  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: Int): NodeSeq = {
		val config = initialConfig match {
		  case config: RadioButtonsConfig => config
		  case _ => throw new ClassCastException
		}
		makeRadioButtons(config.titleText, config.labelList, sessionId, config.slotNum)
	  }
	}
	
	class RadioButtons extends StatefulSnippet with Logger {
	  
	  //var formId: Int = RadioButtons.blankId
	  var formId: String = ""
	  var idItems: Array[String] = new Array[String](2)
	  lazy val radioButtonsInstanceTitleId = RadioButtons.titlePrefix + formId
	  
	  def dispatch = {case "render" => render}
	  
	  def render(xhtml:NodeSeq): NodeSeq = {
		
		def process(result: String): JsCmd = {
		  info("RadioButtons says option number " + result + " on formId " + formId + " is selected")
		  //SetHtml(radioButtonsInstanceTitleId, Text(RadioButtons.responseText)) //... or not for now
		  val processThread = new Thread(new Runnable { // A new thread to call back into PageCommander to make sure we don't block Ajax handling
			  def run() {
				PageCommander.controlActionMapper(idItems(0).toInt, idItems(1).toInt, result.toInt)
			  }
			})
		  processThread.start
		  JsCmds.Noop
		}
		formId = (S.attr("formId") openOr "_")
		idItems = formId.split("_")
		var valid = false
		var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
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
		} else error("RadioButtons.render cannot find a valid formId! Reported formId: " + formId)
		if (valid) selectors.apply(xhtml) else NodeSeq.Empty // Blanks control if something is wrong with formId
		//selectors.apply(xhtml) // This would be ok too, and would just apply the "null" selector transform to html if something is broken
	  }  
	}

  }
}
