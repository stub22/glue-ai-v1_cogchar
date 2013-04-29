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
  package view {

	import scala.xml.NodeSeq	
	import net.liftweb.http._
	import net.liftweb.util._
	import Helpers._
	import org.cogchar.bind.lift.ControlConfig
	import org.cogchar.lifter.model.LifterState;
	import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler

	object TextBox extends AbstractControlInitializationHandler {
	  
	  protected val matchingName = "TEXTBOX"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		makeBox(control.text, control.style)
	  }
	  
	  def makeBox(text:String, style:String, centered:Boolean, displayAsCell:Boolean): NodeSeq = {
		if (centered) {
		  if (displayAsCell) {
			<div class="centerVert"><div class={style}>{text}</div></div>
		  } else {
			// The extra div makes the contents not display as a table cell, for example, for when we don't want background to fill whole cell
			<div class="centerVert"><div><div class={style}>{text}</div></div></div> 
		  }
		  
		} else {
		  <div class={style}>{text}</div>
		}
	  }
	  
	  def makeBox(text:String, style:String, centered:Boolean): NodeSeq = {
		makeBox(text, style, centered, true)
	  }
	  
	  def makeBox(text:String, style:String): NodeSeq = {
		makeBox(text, style, false);
	  }

	}
  }
}

