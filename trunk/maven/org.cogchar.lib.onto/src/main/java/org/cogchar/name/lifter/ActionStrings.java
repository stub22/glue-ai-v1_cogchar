/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.name.lifter;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ActionStrings {
	  final public static String getContinuousSpeech = "startgetspeech";
	  final public static String stopContinuousSpeech = "stopgetspeech";
	  final public static String acquireSpeech = "getspeech";
	  final public static String cogbotSpeech = "cogbotspeech";
	  final public static String submitText = "submittext";
	  final public static String showText = "showtext";
	  final public static String oldDemo = "olddemo";
	  final public static String submit = "submit";
	  final public static String lastConfig = "lastScreen";
	  final public static String update = "reload";
	  final public static String refreshLift = "refreshliftcache";
	  final public static String databalls = "databalls";
	  
	  final public static String commandTokenSeparator = "_";
	  final public static String stringAttributeSeparator = ",";
	  final public static String multiCommandSeparator = "__";
	  
	  final public static String subControlIdentifier = "[subControl:]";
	  
	  // Not technically "action strings": strings related to network config function
	  final public static String encryptionTypeVar = "networkEncType";
	  final public static String noEncryptionName = "NONE";
	  
	  final public static String COGBOT_TOKEN = "cogbot"; // These token definitions may not live here permanently
	  final public static String ANDROID_SPEECH_TOKEN = "androidSpeech"; // for Android speech recognition
	  final public static String ENABLE_TOKEN = "enable" ;
	  final public static String DISABLE_TOKEN = "disable";
	  final public static String ERROR_TOKEN = "error";
	  final public static String NETWORK_CONFIG_TOKEN = "networkconfig";
	  final public static String LOGIN_TOKEN = "login";
	  final public static String DATABALLS_TOKEN = "databalls";
	  
	  // These are URI prefixes for actions
	  final public static String p_liftcmd = "http://www.cogchar.org/lift/config/command#";
	  final public static String p_liftvar = "http://www.cogchar.org/lift/config/variable#";
	  final public static String p_liftsessionvar = "http://www.cogchar.org/lift/config/sessionVariable#";
	  final public static String p_scenetrig = "http://www.cogchar.org/schema/scene/trigger#";
	  final public static String p_liftconfig = "http://www.cogchar.org/lift/config/configroot#";
	  final public static String p_cinematic = "http://www.cogchar.org/schema/path/definition#";
	  final public static String p_thinganim = "http://www.cogchar.org/schema/thinganim/definition#";
	  final public static String p_lifterQuery = "http://www.cogchar.org/lift/action/query#";
	  // Right now animations use this prefix, but other things do too.
	  // Really we should pick a unique one for animations (p_scenetrig derivative?) to avoid confusion
	  final public static String p_anim = "http://www.hrkind.com/model#" ;
	  
	  final public static String LIFT_REFRESH_UPDATE_NAME = "ManagedGlobalConfigService";
	  
}
