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

	import net.liftweb.http.js.JsCmd
	
	object DualTextForm extends AbstractTextFormObject {
	  
	  protected val matchingName = "DUALTEXTINPUT"
	  
	}

	class DualTextForm extends AbstractTextForm {
	  
	  // Too bad these are required to get prefixes from object - has to be a better way...
	  val labelIdPrefix: String = DualTextForm.labelIdPrefix
	  val textBoxIdPrefix: String = DualTextForm.textBoxIdPrefix
	  
	  override def process(): JsCmd = {
		info("Input text for form " + formId + " for session " + sessionId + ": " + text1 + "; " + text2)
		super.process();
	  } 

	}

  }
}
