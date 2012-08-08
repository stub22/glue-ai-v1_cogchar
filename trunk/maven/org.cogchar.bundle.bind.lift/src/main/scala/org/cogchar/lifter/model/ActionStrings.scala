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
  package model {

// A resource of action string components interpreted by PageCommander
	object ActionStrings {
	  final val getContinuousSpeech = "startgetspeech"
	  final val stopContinuousSpeech = "stopgetspeech"
	  final val acquireSpeech = "getspeech"
	  final val cogbotSpeech = "cogbotspeech"
	  final val submitText = "submittext"
	  final val showText = "showtext"
	  final val setVariable = "setvariable"
	  final val oldDemo = "olddemo"
	  
	  final val COGBOT_TOKEN = "cogbot" // These token definitiions will probably not live here permanently
	  final val ANDROID_SPEECH_TOKEN = "androidSpeech" // for Android speech recognition
	  final val ENABLE_TOKEN = "enable" 
	  final val DISABLE_TOKEN = "disable"
	  final val ERROR_TOKEN = "error"
	}

  }
}