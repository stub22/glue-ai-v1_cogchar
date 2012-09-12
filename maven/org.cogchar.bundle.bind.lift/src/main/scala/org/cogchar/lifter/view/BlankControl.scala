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

package org.cogchar.lifter.view

import org.cogchar.lifter.model.PageCommander
import scala.xml.NodeSeq

object BlankControl extends org.cogchar.lifter.snippet.ControlDefinition {
  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: String): NodeSeq = {
	NodeSeq.Empty
  }
  
  //under the covers, .instance() is implemented as a static method on class BlankControl. This lets BlankControlConfig
  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
  def instance = this  
  
  class BlankControlConfig extends PageCommander.InitialControlConfig {
		  controlType = BlankControl.instance
  }
  
}
