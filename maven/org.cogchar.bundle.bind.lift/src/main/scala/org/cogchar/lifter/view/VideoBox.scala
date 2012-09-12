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
	import org.cogchar.lifter.model.PageCommander

	object VideoBox extends org.cogchar.lifter.snippet.ControlDefinition {
	  
	  class VideoBoxConfig(val videoResource:String, val mute:Boolean)
				extends PageCommander.InitialControlConfig {
		  controlType = VideoBox.instance
	  }
  
	  //under the covers, .instance() is implemented as a static method on class VideoBox. This lets VideoBoxConfig
	  //pass the object singleton instance via controlType. See http://stackoverflow.com/questions/3845737/how-can-i-pass-a-scala-object-reference-around-in-java
	  def instance = this 
	  
	  def makeBox(videoResource:String, mute: Boolean): NodeSeq = {
		val videoPath: String = "/video/" + videoResource // May want to move this prefix to central location
		val muteText = mute.toString
		// It's all well and good to use a single video resource unless we want to support IE, in which case we'll have to mix in more
		<video src={videoPath} width="100%" height="100%" autoplay="true" muted={muteText}></video>
	  }
	  
	  
	  def makeControl(initialConfig:PageCommander.InitialControlConfig, sessionId: String): NodeSeq = {
		val config = initialConfig match {
		  case config: VideoBoxConfig => config
		  case _ => throw new ClassCastException
		}
		makeBox(config.videoResource, config.mute)
	  }
	}
	
  }
}

