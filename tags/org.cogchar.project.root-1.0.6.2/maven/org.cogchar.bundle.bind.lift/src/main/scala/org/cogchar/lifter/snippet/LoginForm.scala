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
	import net.liftweb.http.js.JsCmd
   	import net.liftweb.util.CssSel
	import net.liftweb.util.Helpers._
	
	object LoginForm extends AbstractTextFormObject {
	  
	  protected val matchingName = "LOGINFORM"
	  
	}

	class LoginForm extends AbstractTextForm {
	  
	  // Too bad these are required to get prefixes from object - has to be a better way...
	  val labelIdPrefix: String = LoginForm.labelIdPrefix
	  val textBoxIdPrefix: String = LoginForm.textBoxIdPrefix
   
	  override def process(): JsCmd = {
		myLogger.info("Input text for form #{}: {}; [password hidden] in session {}",
					  Array[AnyRef](formId.asInstanceOf[AnyRef], text1, sessionId))
		super.process();
	  }
	  
	  override def generateSelectors(titleText: Array[String]): CssSel = {
		labelSelectorText1 #> titleText(0) & labelSelectorText2 #> titleText(1) &
		boxSelectorText1 #> (SHtml.text(text1, text1 = _, "id" -> textBoxInstanceLabel1)) &
		boxSelectorText2 #> (SHtml.password(text2, text2 = _, "id" -> textBoxInstanceLabel2) ++ SHtml.hidden(process))
	  }
	
	}

  }
}
