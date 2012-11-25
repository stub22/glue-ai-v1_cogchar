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
	import scala.collection.mutable.{Buffer,HashMap}

	// Needs to be refactored as subclass of a common superclass to this and other "MultiSelect" controls
	// Needs lots of refactoring in general -- ugly stuff in here at moment and lots of room for functionality improvements!
	// But we'll get it going for the moment so we can use it for lists of Cogchar triggerables, and keep improving it later.
	object LinkList extends AbstractControlInitializationHandler with Logger {
	  
	  val matchingName = "LINKLIST"
	  
	  // The maximum number of items which will be displayed in a single column of "links"
	  // Probably should come from RDF config in some way ultimately
	  final val maxPerColumn = 20;
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq =  {
		// From the RDF "text" value we assume a comma separated list with the first item the title and the rest link labels
		// In practice, as currently used, link labels are generated for these controls by a liftquery action and fed into this
		// method. Likely will refine how this is handled to make the mechanism flexible and clean.
		val textItems = control.text.split(ActionStrings.stringAttributeSeparator)
		val titleText = textItems(0)
		val labelItems = textItems.tail
		makeLinkList(titleText, labelItems, sessionId, slotNum)
	  }
	  
	  val titlePrefix = "linkListTitle"
	  val listId = "list_item"
	  
	  val blankId = -1
	  
	  // This stuff needs to go -- evil singleton state. Similarly in other "MultiSelect" controls
	  // Needed to pass info to snippet instances, but there are surely better ways to do this in Lift
	  val titleMap = new HashMap[String,HashMap[Int,String]]
	  val labelMap = new HashMap[String,HashMap[Int,Array[String]]]

	  def makeLinkList(titleText: String, labelList:Array[String], sessionId:String, idNum: Int): NodeSeq = {
		if (!(titleMap contains sessionId)) {
		  titleMap(sessionId) = new HashMap[Int,String]
		  labelMap(sessionId) = new HashMap[Int,Array[String]]
		}
		val formIdForHtml: String = idNum.toString
		titleMap(sessionId)(idNum) = titleText
		labelMap(sessionId)(idNum) = labelList
		val titleId: String = titlePrefix + formIdForHtml
		val numberOfColumns = (labelList.length/maxPerColumn.doubleValue).ceil.intValue max 1
		val columnWidth = 100/numberOfColumns //percent
		var listHtmlString: String = "<lift:LinkList formId=\"" + formIdForHtml + "\"><div class=\"labels\" id=\"" + titleId +"\"></div><ol>"
		for (columnNumber <- 0 until numberOfColumns) {
		  listHtmlString = listHtmlString + "<div style=\"position:relative;float:left;height:100%;width:" + columnWidth + "%\">"
		  var firstIndexNextColumn = (columnNumber+1)*maxPerColumn
		  if (firstIndexNextColumn > labelList.length) {firstIndexNextColumn=labelList.length}
		  for (buttonIndex <- columnNumber*maxPerColumn until firstIndexNextColumn) {
			val itemId = listId + formIdForHtml + "_" + buttonIndex.toString
			listHtmlString = listHtmlString + "<li id=\"" + itemId + "\"/>"
		  }
		  listHtmlString = listHtmlString + "</div>";
		}
		listHtmlString = listHtmlString + "</ol></lift:LinkList>"
		XML.loadString(listHtmlString)
	  }
	}
	
	class LinkList extends StatefulSnippet with Logger {
	  
	  var formId: Int = LinkList.blankId
	  var sessionId: String = ""
	  lazy val linkListInstanceTitleId = LinkList.titlePrefix + formId
	  
	  def dispatch = {case "render" => render}
	  
	  def render(xhtml:NodeSeq): NodeSeq = {
		
		def process(result: String): JsCmd = {
		  info("LinkList says link number " + result + " on formId " + formId + " is selected in session " + sessionId)
		  PageCommander ! PageCommander.ControlMultiAction(sessionId, formId, result.toInt)
		  JsCmds.Noop
		}
		
		S.session match {
		  case Full(myLiftSession) => {
			sessionId = myLiftSession.uniqueId
			formId = (S.attr("formId") openOr "-1").toInt 
			var valid = false
			var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid formId. (As recommended by the inventor of Lift)
			var errorSeq: NodeSeq = NodeSeq.Empty
			if (LinkList.titleMap(sessionId).contains(formId)) {
			  valid = true
			  val titleSelectorText: String = "#"+linkListInstanceTitleId+" *"
			  val buttonTags = (for (i <- 0 until LinkList.labelMap(sessionId)(formId).length) yield i.toString)// There may be a simplier Scala way to do this
			  selectors = titleSelectorText #> LinkList.titleMap(sessionId)(formId)
			  for (buttonIndex <- 0 until LinkList.labelMap(sessionId)(formId).length) {
				val itemId = LinkList.listId + formId.toString + "_" + buttonIndex.toString // Needs refactoring to eliminate repeated code
				val buttonHtml = SHtml.ajaxButton(LinkList.labelMap(sessionId)(formId)(buttonIndex), () => process(buttonIndex.toString))
				// Something like this might work and allow for less button-looking links, but I haven't gotten this idea to work yet:
				//val buttonHtml:NodeSeq = <div onclick={SHtml.ajaxInvoke(() => process(buttonIndex.toString))._2}>{LinkList.labelMap(formId)(buttonIndex)}</div>
				selectors = selectors & ("#"+itemId+" *") #> buttonHtml
			  }
			} else {
			  val errorText = "LinkList.render cannot find a valid formId! Reported formId: " + formId
			  error(errorText)
			  errorSeq = TextBox.makeBox(errorText, "", true)
			}
			if (valid) selectors.apply(xhtml) else errorSeq
		  }
		  case _ => {
			val errorText = "LinkList cannot get sessionId, not rendering!"
			error(errorText)
			TextBox.makeBox(errorText, "", true)
		  }
		}
		
	  }  
	}

  }
}
