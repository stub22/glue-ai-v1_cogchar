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

import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler
import org.cogchar.bind.lift.ControlConfig
import org.cogchar.lifter.model.{ActionStrings,LifterState,PageCommander}
import org.cogchar.lifter.view.TextBox
import net.liftweb._
import net.liftweb.util._
import http._
import common._
import js._
import JsCmds._
import JE._
import Helpers._
import net.liftweb.http.SHtml._
import S._
import scala.xml._
import scala.collection.mutable.HashMap

trait AbstractMultiSelectControlObject extends AbstractControlInitializationHandler with Logger {
  
  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq =  {
	// From the RDF "text" value we assume a comma separated list with the first item the title and the rest option labels
	val textItems = control.text.split(ActionStrings.stringAttributeSeparator)
	val titleText = textItems(0)
	val labelItems = textItems.tail
	makeMultiControl(state, sessionId, slotNum, titleText, labelItems)
  }
  
  val titlePrefix: String
  
  final def makeMultiControl(state:LifterState, sessionId:String, slotNum: Int, titleText: String, labelList:Array[String]): NodeSeq = {
	initializeMaps(state, titleText, labelList, sessionId, slotNum);
	makeMultiControlImpl(titleText, labelList, slotNum)
  }
  
  def makeMultiControlImpl(titleText: String, labelList:Array[String], slotNum: Int): NodeSeq
  
  def initializeMaps(state:LifterState, titleText: String, labelList:Array[String], sessionId:String, slotNum:Int) {
	val controlData = new MultiSelectControlData
	controlData title = titleText
	controlData labels = labelList
	state.getSnippetDataMapForSession(sessionId)(slotNum) = controlData
  }
}

trait AbstractMultiSelectControl extends StatefulSnippet with Logger {
  var formId: Int = blankId
  val blankId = -1
  var sessionId: String = ""
  
  final def snippetData(sessionId:String) = PageCommander.hackIntoSnippetDataMap(sessionId)
  
  // Override to true to specify a Multi-action control, leave false for Multi-select
  def multiActionFlag = false
  
  def getName: String
  
  def dispatch = {case "render" => render}
  
  def render(xhtml:NodeSeq): NodeSeq = {
	
	S.session match {
	  case Full(myLiftSession) => {
		  sessionId = myLiftSession.uniqueId
		  formId = (S.attr("formId") openOr "-1").toInt
		  var valid = false
		  var selectors:CssSel = "i_eat_yaks_for_breakfast" #> "" // This is just to produce a "Null" CssSel so we can initialize this here, but not add any meaningful info until we have checked for valid form data. (As recommended by the inventor of Lift)
		  var errorSeq: NodeSeq = NodeSeq.Empty
		  snippetData(sessionId)(formId) match {
			case snippetResources: MultiSelectControlData => {
				if (formId != blankId) {
				  valid = true
				  selectors = generateSelectors(sessionId, formId, snippetResources.title, snippetResources.labels)
				} else {
				  errorSeq = produceErrorMessages(getName + ".render cannot find a valid formId!")
				}
			  }
			case _ => {
				errorSeq = produceErrorMessages(getName + " cannot find snippet data in LifterState!")
			}
		  }
		  if (valid) selectors.apply(xhtml) else errorSeq
		}
	  case _ => {
		  produceErrorMessages(getName + " cannot get sessionId, not rendering!")
		}
	}
	
  }
  
  def produceErrorMessages(errorText:String): NodeSeq = {
	error(errorText)
	TextBox.makeBox(errorText, "", true)
  }
  
  def process(result: String): JsCmd = {
	info(getName + " says item number " + result + " on slot " + formId + " is selected in session " + sessionId)
	PageCommander ! PageCommander.ControlMultiAction(sessionId, formId, result.toInt, multiActionFlag)
	JsCmds.Noop
  }
  
  def generateSelectors(sessionId:String, formId:Int, title: String, labels: Array[String]): CssSel
}

class MultiSelectControlData {
  var title: String = null
  var labels: Array[String] = null
}

