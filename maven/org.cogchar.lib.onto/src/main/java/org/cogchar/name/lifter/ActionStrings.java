/*
 *  Copyright 2013-2014 by The Cogchar Project (www.cogchar.org).
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

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.dir.TempNamespaceDir;

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
	  final public static String p_liftcmd = NamespaceDir.NS_LifterCmd;
	  final public static String p_liftvar = NamespaceDir.NS_LifterVar;
	  final public static String p_liftsessionvar = NamespaceDir.NS_LifterSessionVar;
	  final public static String p_scenetrig = NamespaceDir.NS_SceneTrig;
	  final public static String p_liftconfig = NamespaceDir.NS_LifterConfig;
	  final public static String p_cinematic = NamespaceDir.NS_CinePathDef;
	  final public static String p_thinganim = NamespaceDir.NS_ThingAnim;
	  final public static String p_lifterQuery = NamespaceDir.NS_LifterQuery;
	  final public static String p_requestRepoOutput = NamespaceDir.NS_RequestLifterRepoOutput;
	  // Not sure if this is exactly what we want, schema-wise. The repoSender identifies the control from which a user
	  // action is received, currently in the form of text. So maybe calling this NS_LifterUserAction makes sense, maybe not...
	  final public static String p_repoSender = NamespaceDir.NS_LifterUserAction; 
	  // Right now animations use this prefix, but other things do too.
	  // Really we should pick a unique one for animations (p_scenetrig derivative?) to avoid confusion
	  final public static String p_anim = TempNamespaceDir.NS_TestAnimSrc;
	  
	  final public static String LIFT_REFRESH_UPDATE_NAME = 
                  "ManagedGlobalConfigService";
      
      final public static String BEHAVIOR_MASTER_SCENE_PREFIXES = 
              NamespaceDir.NS_ccScnInst;
      final public static String BEHAVIOR_MASTER_ADMIN_PREFIXES =
              NamespaceDir.NS_ccScnAdminInst;
      
      final public static String SPEECH_RECOGNITION_PREFIX = 
              NamespaceDir.NS_LifterSpeechRecognition;

    
    // The lifter outgoing  URI
    // FIXME:    Confusingly, the word "action" occurs at several places in this naming hierarchy.
	  
	public static String FOLDER_CGC_PUSHY = "http://www.cogchar.org/lift/";
	public static String FOLDER_PUSHY_ACTION =  FOLDER_CGC_PUSHY + "action/";
	public static final String PREFIX_REGISTRATION = FOLDER_PUSHY_ACTION + "registration#";
				//         "http://www.cogchar.org/lift/action/registration#";

	public static String FOLDER_PUSHY_USER = FOLDER_CGC_PUSHY + "user/"; // http://www.cogchar.org/lift/user/
	public static String FOLDER_PUSHY_USER_ACTION = FOLDER_PUSHY_USER + "action/"; // http://www.cogchar.org/lift/user/action/
	public static String PREFIX_PUSHY_USER_ACTION = FOLDER_PUSHY_USER + "action#"; // http://www.cogchar.org/lift/user/action#

    public static final Ident PUSHY_USER_ACTION_ACTION = new FreeIdent(PREFIX_PUSHY_USER_ACTION + "action");
				//        "http://www.cogchar.org/lift/user/action#action");
    
    public static final Ident PUSHY_USER_ACTION_SESSION = new FreeIdent(PREFIX_PUSHY_USER_ACTION + "session");
				//         "http://www.cogchar.org/lift/user/action#session");
    
    // IDs that can be registered as lifter sessions listed below    
    public static final String DEFAULT_REGISTRATION = "action";
	
    public static final String STUDENT_REGISTRATION = "student";
    public static final Ident STUDENT_ACITON = new FreeIdent(PREFIX_PUSHY_USER_ACTION + "student");
       //     "http://www.cogchar.org/lift/user/action#student");
	
	public static final String PREFIX_CONFIG_ROOT = FOLDER_CGC_PUSHY + "config/configroot#";
    public static final Ident STUDENT_START_PAGE = new FreeIdent(PREFIX_CONFIG_ROOT + "student-start-page-config");
          //  "http://www.cogchar.org/lift/config/configroot#student-start-page-config");
    
    public static final String FACILITATOR_REGISTRATION = "facilitator";
    public static final Ident FACILITATOR_ACITON = new FreeIdent(PREFIX_PUSHY_USER_ACTION + "facilitator");
           //   "http://www.cogchar.org/lift/user/action#facilitator");
    public static final Ident FACILITATOR_START_PAGE = new FreeIdent(PREFIX_CONFIG_ROOT + "facilitator-start-page-config");
           //  "http://www.cogchar.org/lift/config/configroot#facilitator-start-page-config");
}
