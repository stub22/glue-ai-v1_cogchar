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

import org.cogchar.lifter.model.PageCommander
import scala.xml.NodeSeq

// This is used extensively but not really necessary any more with new way of doing sessions; it will be refactored out soon.
trait ControlDefinition {
  def makeControl(config:PageCommander.InitialControlConfig, sessionId: String): NodeSeq
}
