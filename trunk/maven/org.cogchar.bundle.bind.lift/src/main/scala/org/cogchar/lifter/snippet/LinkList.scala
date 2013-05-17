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
	
	import net.liftweb.http.SHtml
	import net.liftweb.util.CssSel
	import net.liftweb.util.Helpers._
	import scala.xml.{NodeSeq,XML}

	object LinkList extends AbstractMultiSelectControlObject {
	  
	  val matchingName = "LINKLIST"
	  
	  // The maximum number of items which will be displayed in a single column of "links"
	  // Probably should come from RDF config in some way ultimately
	  final val maxPerColumn = 20;
	  
	  val titlePrefix = "linkListTitle"
	  val listId = "list_item"

	  def makeMultiControlImpl(titleText: String, labelList:Array[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
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
	
	class LinkList extends AbstractMultiSelectControl {
	  
	  // Specifies that this is a multi-action control; actions will be read from LifterState.SessionState.multiActionsBySlot
	  override def multiActionFlag = true
	  
	  def getName: String = LinkList.matchingName
	  
	  def generateSelectors(sessionId: String, formId: Int, title: String, labels: Array[String]): CssSel = {
		val listLength = labels.length
		val controlInstanceTitleId = LinkList.titlePrefix + formId
		val titleSelectorText: String = "#"+controlInstanceTitleId+" *"
		val buttonTags = (for (i <- 0 until listLength) yield i.toString)// There may be a simplier Scala way to do this
		var selectors = titleSelectorText #> title
		for (buttonIndex <- 0 until listLength) {
		  val itemId = LinkList.listId + formId.toString + "_" + buttonIndex.toString // Needs refactoring to eliminate repeated code
		  val buttonHtml = SHtml.ajaxButton(labels(buttonIndex), () => process(buttonIndex.toString))
		  // Something like this might work and allow for less button-looking links, but I haven't gotten this idea to work yet:
		  //val buttonHtml:NodeSeq = <div onclick={SHtml.ajaxInvoke(() => process(buttonIndex.toString))._2}>{LinkList.labelMap(formId)(buttonIndex)}</div>
		  selectors = selectors & ("#"+itemId+" *") #> buttonHtml
		}
		selectors
	  }
	}
  }
}
