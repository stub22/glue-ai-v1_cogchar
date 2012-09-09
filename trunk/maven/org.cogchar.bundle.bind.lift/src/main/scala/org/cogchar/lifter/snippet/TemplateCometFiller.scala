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

import scala.xml._
import org.cogchar.lifter.model.PageCommander

class TemplateCometFiller {
  // There may be a better way to do this: using string concatenation to form the XML since we need to add a variable attribute
  // within the quoted section
  def render = {
	val sessionId = PageCommander.getNextSessionId
	var xmlString = "<div style=\"height: 100%;\"><div class=\"lift:comet?type=TemplateActor;name="
	xmlString += sessionId
	xmlString += "\"><span name=\"TemplateSlot\">Template fills here</span></div>"
	// Now, let's add the JavaScript "push" Comet declaration here too
	xmlString += "<div id=\"SpeechRequest\"><div class=\"lift:comet?type=JavaScriptActor;name="
	xmlString += sessionId
	xmlString += "\"><div name=\"JSCommandSlot\">A hidden div to fire JS command</div></div></div></div>"
	XML.loadString(xmlString)
  }
}
