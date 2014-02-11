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

	import org.cogchar.bind.lift.ControlConfig
	import org.cogchar.lifter.model.LifterState
	import org.cogchar.lifter.model.handler.AbstractControlInitializationHandler
	import scala.xml.NodeSeq

	object VideoBox extends AbstractControlInitializationHandler {
	  
	  protected val matchingName = "VIDEOBOX"
  
	  protected def handleHere(state:LifterState, sessionId:String, slotNum:Int, control:ControlConfig): NodeSeq = {
		makeBox(control.resource, false)
	  }
	  
	  def makeBox(videoResource:String, mute: Boolean): NodeSeq = {
      if(mute){
        return makeMutedBox(videoResource);
      }else{
        return makeUnmutedBox(videoResource);
      }
	  }
	  
	  def makeMutedBox(videoResource:String): NodeSeq = {
		val videoPath: String = "/video/" + videoResource // May want to move this prefix to central location
		// It's all well and good to use a single video resource unless we want to support IE, in which case we'll have to mix in more
		<video src={videoPath} width="100%" height="100%" autoplay="true" muted="true"></video>
	  }
	  
	  def makeUnmutedBox(videoResource:String): NodeSeq = {
		val videoPath: String = "/video/" + videoResource // May want to move this prefix to central location
		// It's all well and good to use a single video resource unless we want to support IE, in which case we'll have to mix in more
		<video src={videoPath} width="100%" height="100%" autoplay="true"></video>
	  }
	  
	}
	
  }
}

