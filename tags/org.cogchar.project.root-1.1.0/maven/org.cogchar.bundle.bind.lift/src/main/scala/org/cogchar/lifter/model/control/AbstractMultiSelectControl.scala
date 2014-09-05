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

package org.cogchar.lifter.model.control

import org.cogchar.impl.web.config.WebControlImpl
import org.cogchar.impl.web.util.HasLoggerConv
import org.cogchar.lifter.model.main.{PageCommander, ControlMultiAction}
import org.cogchar.impl.web.wire.{LifterState, SessionOrganizer}

import org.cogchar.lifter.view.TextBoxFactory
import org.cogchar.name.lifter.ActionStrings
import net.liftweb.common.Full
import net.liftweb.http.{S, StatefulSnippet}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.util.CssSel
import net.liftweb.util.Helpers._ // This wildcard import is the way Lift Helpers roll for CssSel operations and etc.
import scala.collection.mutable.HashMap
import scala.xml.NodeSeq

import org.cogchar.api.web.{WebControl}


abstract class AbstractMultiSelectControlObject(mySessOrg: SessionOrganizer) 
	extends AbstractControlSnippet with AbstractMultiSelectControl {
  

  override protected def generateXmlForControl(sessionId:String, slotNum:Int, control:WebControl): NodeSeq =  {
	// From the RDF "text" value we assume a comma separated list with the first item the title and the rest option labels
	val textItems = control.getText.split(ActionStrings.stringAttributeSeparator)
	val titleText = textItems(0)
	val labelItems = textItems.tail
	makeMultiControl(mySessOrg, sessionId, slotNum, titleText, labelItems)
  }
  
  val titlePrefix: String
  
  final def makeMultiControl(sessOrg: SessionOrganizer, sessionId:String, slotNum: Int, titleText: String, labelList:Array[String]): NodeSeq = {
	initializeMaps(sessOrg, titleText, labelList, sessionId, slotNum);
	makeMultiControlImpl(titleText, labelList, slotNum)
  }
  
  def makeMultiControlImpl(titleText: String, labelList:Array[String], slotNum: Int): NodeSeq
  
  def initializeMaps(sessOrg: SessionOrganizer, titleText: String, labelList:Array[String], sessionId:String, slotNum:Int) {
	val controlData = new MultiSelectControlData
	controlData title = titleText
	controlData labels = labelList
	val snipDataMap = sessOrg.hackIntoSnippetDataMap(sessionId)
	snipDataMap(slotNum) = controlData
  }
}

trait AbstractMultiSelectControl extends StatefulSnippet with HasLoggerConv {
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
	myLogger.error(errorText)
	TextBoxFactory.makeBox(errorText, "", true)
  }
  
  def process(result: String): JsCmd = {
	info4("{} says item number {} on slot {} is selected in session {}",
		getName, result.asInstanceOf[AnyRef], formId.asInstanceOf[AnyRef], sessionId)
	PageCommander ! ControlMultiAction(sessionId, formId, result.toInt, multiActionFlag)
	JsCmds.Noop
  }
  
  def generateSelectors(sessionId:String, formId:Int, title: String, labels: Array[String]): CssSel
}

class MultiSelectControlData {
  var title: String = null
  var labels: Array[String] = null
}

