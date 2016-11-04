/*
 *  Copyright 2012-2014 by The Cogchar Project (www.cogchar.org).
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
	import org.cogchar.api.web.{WebControl}

    import org.cogchar.impl.web.config.WebControlImpl
    import org.cogchar.impl.web.wire.{LifterState}
    import org.cogchar.lifter.model.control.AbstractControlSnippet
    import scala.xml.NodeSeq

    class VideoBox extends AbstractControlSnippet {
	  
      protected val matchingName = "VIDEOBOX"
  
      // Create a video box
      override protected def generateXmlForControl(sessionId:String, slotNum:Int, control:WebControl): NodeSeq = {
        val videoPath: String =control.getResource // May want to move this prefix to central location
        // It's all well and good to use a single video resource unless we want to support IE, in which case we'll have to mix in more
        <video src={videoPath} width="100%" height="100%" autoplay="true"></video>
      }
    }
  }
}