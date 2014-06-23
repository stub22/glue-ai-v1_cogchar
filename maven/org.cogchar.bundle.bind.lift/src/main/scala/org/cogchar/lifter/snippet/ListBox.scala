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

import net.liftweb.common.Empty
import net.liftweb.http.SHtml
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq
import org.cogchar.lifter.model.control.{AbstractMultiSelectControl, AbstractMultiSelectControlObject, SnippetHelper}
import org.cogchar.impl.web.wire.{SessionOrganizer}

class ListBox extends AbstractMultiSelectControlObject (SnippetHelper.mySessionOrganizer) with AbstractMultiSelectControl {
	  
	protected val matchingName = "LISTBOX"
  
	// Not currently implemented:
	//val responseText = "Title can change" // We can add bits to define this in XML if we want, or code in more fancy conditionals
	val titlePrefix = "listformtitle"
	val boxId = "listbox"
	  
	def makeMultiControlImpl(labelText: String, labelList:Array[String], idNum: Int): NodeSeq = {
		val formIdForHtml: String = idNum.toString
		val titleId: String = titlePrefix + formIdForHtml // We need a unique ID here in case we'd like JavaScript to update the title after post
		(
			<form class='lift:form.ajax'>
				<lift:ListBox formId={formIdForHtml}>
					<div id={titleId} class='labels'></div>
					<input id={boxId} class='formlabels'/>
				</lift:ListBox>
			</form>
		)
	}
	def getName: String = matchingName
	  
	def generateSelectors(sessionId: String, formId: Int, title: String, labels: Array[String]): CssSel = {
		val listLength = labels.length
		val listBoxInstanceTitle = titlePrefix + formId
		val titleSelectorText: String = "#"+listBoxInstanceTitle+" *"
		val boxSelectorText: String = "#" + boxId
		val rows = if (listLength < 8) listLength else 7
		val listPairs = (for (i <- 0 until listLength) yield (i.toString, labels(i)))// There may be a simplier Scala way to do this
		titleSelectorText #> title & boxSelectorText #> SHtml.ajaxSelect(listPairs, Empty, process _, "class" -> "formlabels", "size" -> rows.toString)
	}	  
}


