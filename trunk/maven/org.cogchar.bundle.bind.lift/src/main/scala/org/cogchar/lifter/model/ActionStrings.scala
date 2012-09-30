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
	  final val oldDemo = "olddemo"
	  final val submit = "submit"
	  final val lastConfig = "lastScreen"
	  final val update = "reload"
	  final val refreshLift = "refreshliftcache"
	  final val databalls = "databalls"
	  
	  final val commandTokenSeparator = "_"
	  final val stringAttributeSeparator = ","
	  final val multiCommandSeparator = "__"
	  
	  final val subControlIdentifier = "[subControl:]"
	  
	  // Not technically "action strings": strings related to network config function
	  final val encryptionTypeVar = "networkEncType"
	  final val noEncryptionName = "NONE"
	  
	  final val COGBOT_TOKEN = "cogbot" // These token definitions may not live here permanently
	  final val ANDROID_SPEECH_TOKEN = "androidSpeech" // for Android speech recognition
	  final val ENABLE_TOKEN = "enable" 
	  final val DISABLE_TOKEN = "disable"
	  final val ERROR_TOKEN = "error"
	  final val NETWORK_CONFIG_TOKEN = "networkconfig"
	  final val LOGIN_TOKEN = "login"
	  final val DATABALLS_TOKEN = "databalls"
	  
	  // These are URI prefixes for actions
	  final val p_liftcmd = "http://www.cogchar.org/lift/config/command#"
	  final val p_liftvar = "http://www.cogchar.org/lift/config/variable#"
	  final val p_liftsessionvar = "http://www.cogchar.org/lift/config/sessionVariable#"
	  final val p_scenetrig = "http://www.cogchar.org/schema/scene/trigger#"
	  final val p_liftconfig = "http://www.cogchar.org/lift/config/configroot#"
	  final val p_cinematic = "http://www.cogchar.org/schema/cinematic/definition#"
	  
	  final val LIFT_REFRESH_UPDATE_NAME = "ManagedGlobalConfigService"
	  
	}

  }
}