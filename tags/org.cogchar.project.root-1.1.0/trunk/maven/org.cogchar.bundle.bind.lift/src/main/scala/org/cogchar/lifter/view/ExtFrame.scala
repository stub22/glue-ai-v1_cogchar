/*
 * Copyright 2014 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cogchar.lifter {
  package view {
	import org.cogchar.api.web.{WebControl}

    import org.cogchar.impl.web.config.WebControlImpl
    import org.cogchar.impl.web.wire.{LifterState}
    import org.cogchar.lifter.model.control.AbstractControlSnippet
    import scala.xml.NodeSeq

    class ExtFrame  extends AbstractControlSnippet {
	  
      protected val matchingName = "EXTFRAME"
  
      override protected def generateXmlForControl(sessionId:String, slotNum:Int, control:WebControl): NodeSeq = {
        makeBox(control.getResource)
      }
	  
      def makeBox(externalURL:String): NodeSeq = {
        <iframe class="ext-frame" src="{externalURL}"></iframe>
      }

    }
  }
}
