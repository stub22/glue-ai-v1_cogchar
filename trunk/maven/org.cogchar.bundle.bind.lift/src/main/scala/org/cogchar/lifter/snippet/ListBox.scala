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

	object ListBox extends ControlDefinition {
	  
	  class ListBoxConfig(val labelText: String, val labelList:List[String], val slotNum: Int)
				extends PageCommander.InitialControlConfig {
		  controlType = ListBox.instance
	  }
	  
	  //under the covers, .instance() is implemented as a static method on class ListBox. This lets ListBoxConfig
	  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
	  def instance = this  
	  
	  val responseText = "Title can change" // We can add bits to define this in XML if we want, or code in more fancy conditionals (Currently ignored here for demo purposes)
  
	  val titlePrefix = "listformtitle"
	  val boxId = "listbox"
	  val titleMap = new scala.collection.mutable.HashMap[String, String]
	  val labelMap = new scala.collection.mutable.HashMap[String, List[String]] // Map to hold all the labels for each ListBox control rendered
	  
	  def makeListBox(labelText: String, labelList:List[String], sessionId:Int, idNum: Int): NodeSeq = {
		val formIdForHtml: String = sessionId.toString + "_" + idNum.toString
		titleMap(formIdForHtml) = labelText
		labelMap(formIdForHtml) = labelList
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here, because JavaScript may be updating the title after post
		(
		  <form class='lift:form.ajax'>
			<lift:ListBox formId={formIdForHtml}>
			  <div id={titleId} class='labels'></div>
			  <input id={boxId} class='formlabels'/>
			</lift:ListBox>
		  </form>
		)
	  }
	  
	  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: Int): NodeSeq = {
		val config = initialConfig match {
		  case config: ListBoxConfig => config
		  case _ => throw new ClassCastException
		}
		makeListBox(config.labelText, config.labelList, sessionId, config.slotNum)
	  }
	}
	  
	class ListBox extends StatefulSnippet {
		
	  var formId: String = ""
	  var idItems: Array[String] = new Array[String](2)
	  lazy val listBoxInstanceTitle = ListBox.titlePrefix + formId
		
	  def dispatch = {case "render" => render}
		
	  def render(xhtml:NodeSeq): NodeSeq = {

		def process(result: String): JsCmd = {
		  println("ListBox says option number " + result + " on formId " + formId + " is selected.")
		  //SetHtml(listBoxInstanceTitle, Text(ListBox.responseText)) // We'll leave the title the same for the demo
		  val processThread = new Thread(new Runnable { // A new thread to call back into PageCommander to make sure we don't block Ajax handling
			  def run() {
				PageCommander.controlActionMapper(idItems(0).toInt, idItems(1).toInt, result.toInt)
			  }
			})
		  processThread.start
		}

		formId = (S.attr("formId") openOr "_")
		idItems = formId.split("_")
		var valid = false
		var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
		if (ListBox.titleMap.contains(formId)) {
		  valid = true
		  val titleSelectorText: String = "#"+listBoxInstanceTitle+" *"
		  val boxSelectorText: String = "#" + ListBox.boxId
		  val rows = if (ListBox.labelMap(formId).length < 8) ListBox.labelMap(formId).length else 7
		  val listPairs = (for (i <- 0 until ListBox.labelMap(formId).length) yield (i.toString, ListBox.labelMap(formId)(i)))// There may be a simplier Scala way to do this
		  selectors = titleSelectorText #> ListBox.titleMap(formId) & boxSelectorText #> SHtml.ajaxSelect(listPairs, Empty, process _, "class" -> "formlabels", "size" -> rows.toString)
		} else println("ListBox.render cannot find a valid formId! Reported formId: " + formId) //; NodeSeq.Empty
		if (valid) selectors.apply(xhtml) else NodeSeq.Empty // Blanks control if something is wrong with formId
		//selectors.apply(xhtml) // This would be ok too, and would just apply the "null" selector transform to html if something is broken
	  }
	}

  }
}
