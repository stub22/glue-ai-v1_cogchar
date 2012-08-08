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

	import scala.xml._	
	import net.liftweb.util._
	import Helpers._
	import net.liftweb.http._

	object VideoBox {
  
	  def makeBox(videoResource:String, mute: Boolean): NodeSeq = {
		val videoPath: String = "/video/" + videoResource // May want to move this prefix to central location
		val muteText = mute.toString
		// It's all well and good to use a single video resource unless we want to support IE, in which case we'll have to mix in more
		<video src={videoPath} width="100%" height="100%" autoplay="true" muted={muteText}></video>
	  } 
	}
	
  }
}

